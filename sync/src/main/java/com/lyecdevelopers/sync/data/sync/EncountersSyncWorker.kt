package com.lyecdevelopers.sync.data.sync

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.lyecdevelopers.core.R
import com.lyecdevelopers.core.data.local.entity.EncounterEntity
import com.lyecdevelopers.core.data.remote.FormApi
import com.lyecdevelopers.core.model.encounter.EncounterPayload
import com.lyecdevelopers.core.utils.AppLogger
import com.lyecdevelopers.core.utils.NotificationConstants.ENCOUNTER_SYNC_NOTIFICATION_ID
import com.lyecdevelopers.core.utils.NotificationConstants.SYNC_NOTIFICATION_CHANNEL_DESCRIPTION
import com.lyecdevelopers.core.utils.NotificationConstants.SYNC_NOTIFICATION_CHANNEL_ID
import com.lyecdevelopers.core.utils.NotificationConstants.SYNC_NOTIFICATION_CHANNEL_NAME
import com.lyecdevelopers.sync.domain.usecase.SyncUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.catch

@HiltWorker
class EncountersSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncUseCase: SyncUseCase,
    private val api: FormApi,
) : CoroutineWorker(appContext, workerParams) {

    private val notificationManager
        get() = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun ensureNotificationChannelExists() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val channel = android.app.NotificationChannel(
                SYNC_NOTIFICATION_CHANNEL_ID,
                SYNC_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = SYNC_NOTIFICATION_CHANNEL_DESCRIPTION
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(
        title: String,
        content: String,
        progress: Int = 0,
        maxProgress: Int = 0,
    ): Notification {
        ensureNotificationChannelExists()

        val builder = NotificationCompat.Builder(applicationContext, SYNC_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title).setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (progress > 0 && maxProgress > 0) {
            builder.setProgress(maxProgress, progress, false)
        } else {
            builder.setProgress(0, 0, true)
        }

        return builder.build()
    }


    override suspend fun doWork(): Result {
        AppLogger.d("🔄 EncountersSyncWorker started")

        val initialNotification = createForegroundNotification(
            "Syncing Encounter Data", "Starting synchronization...", 0, 0
        )
        val foregroundInfo = ForegroundInfo(ENCOUNTER_SYNC_NOTIFICATION_ID, initialNotification)
        setForeground(foregroundInfo)

        return try {
            var shouldRetry = false
            var totalEncountersToSync = 0
            var syncedEncountersCount = 0

            syncUseCase.getUnsynced().catch { e ->
                AppLogger.e("❌ DB read failed: ${e.message}")
                shouldRetry = true
                val errorNotification = createForegroundNotification(
                    "Encounter Sync Failed", "Error reading local data: ${e.message}", 0, 0
                )
                notificationManager.notify(ENCOUNTER_SYNC_NOTIFICATION_ID, errorNotification)
            }.collect { unsyncedList ->
                totalEncountersToSync = unsyncedList.size

                if (totalEncountersToSync == 0) {
                    AppLogger.d("✅ No unsynced encounters. Nothing to sync.")
                    val completedNotification = createForegroundNotification(
                        "Encounter Sync Complete", "No unsynced encounters found.", 1, 1
                    )
                    notificationManager.notify(
                        ENCOUNTER_SYNC_NOTIFICATION_ID, completedNotification
                    )
                } else {
                    AppLogger.d("🔄 Syncing ${unsyncedList.size} unsynced encounters...")

                    val progressNotification = createForegroundNotification(
                        "Syncing Encounter Data",
                        "Syncing 0 of $totalEncountersToSync encounters...",
                        0,
                        totalEncountersToSync
                    )
                    notificationManager.notify(
                        ENCOUNTER_SYNC_NOTIFICATION_ID, progressNotification
                    )


                    unsyncedList.forEach { entity ->
                        try {
                            val payload = buildEncounterPayload(entity)
                            val response = api.saveEncounter(payload)

                            if (response.isSuccessful) {
                                syncUseCase.markSynced(entity).catch { markErr ->
                                    AppLogger.e("⚠️ Mark local failed: ${markErr.message}")
                                    shouldRetry = true
                                    val currentProgressNotification = createForegroundNotification(
                                        "Syncing Encounter Data",
                                        "Syncing $syncedEncountersCount of $totalEncountersToSync encounters. Error marking synced: ${markErr.message}",
                                        syncedEncountersCount,
                                        totalEncountersToSync
                                    )
                                    notificationManager.notify(
                                        ENCOUNTER_SYNC_NOTIFICATION_ID, currentProgressNotification
                                    )
                                }.collect {
                                    AppLogger.d("✅ Encounter ${entity.id} marked synced.")
                                    syncedEncountersCount++
                                    val currentProgressNotification = createForegroundNotification(
                                        "Syncing Encounter Data",
                                        "Syncing $syncedEncountersCount of $totalEncountersToSync encounters...",
                                        syncedEncountersCount,
                                        totalEncountersToSync
                                    )
                                    notificationManager.notify(
                                        ENCOUNTER_SYNC_NOTIFICATION_ID, currentProgressNotification
                                    )
                                }
                            } else {
                                AppLogger.e(
                                    "❌ API rejected encounter ${entity.id}: ${response.code()} ${response.message()}"
                                )
                                shouldRetry = true
                                val currentProgressNotification = createForegroundNotification(
                                    "Syncing Encounter Data (with issues)",
                                    "Failed to sync encounter ${entity.id}: ${response.message()} ($syncedEncountersCount of $totalEncountersToSync)",
                                    syncedEncountersCount,
                                    totalEncountersToSync
                                )
                                notificationManager.notify(
                                    ENCOUNTER_SYNC_NOTIFICATION_ID, currentProgressNotification
                                )
                            }

                        } catch (e: Exception) {
                            AppLogger.e("❌ Error syncing encounter ${entity.id}: ${e.message}")
                            shouldRetry = true
                            val currentProgressNotification = createForegroundNotification(
                                "Syncing Encounter Data (with errors)",
                                "Error syncing encounter ${entity.id}: ${e.message} ($syncedEncountersCount of $totalEncountersToSync)",
                                syncedEncountersCount,
                                totalEncountersToSync
                            )
                            notificationManager.notify(
                                ENCOUNTER_SYNC_NOTIFICATION_ID, currentProgressNotification
                            )
                        }
                    }
                }
            }

            if (shouldRetry) {
                AppLogger.d("🔁 Retrying EncountersSyncWorker...")
                val retryNotification = createForegroundNotification(
                    "Encounter Sync Needs Retry",
                    "Some encounters failed to sync. Retrying...",
                    syncedEncountersCount,
                    totalEncountersToSync
                )
                notificationManager.notify(ENCOUNTER_SYNC_NOTIFICATION_ID, retryNotification)
                Result.retry()
            } else {
                AppLogger.d("✅ EncountersSyncWorker success")
                val successNotification = createForegroundNotification(
                    "Encounter Sync Complete",
                    "All $syncedEncountersCount encounters synced successfully.",
                    syncedEncountersCount, totalEncountersToSync
                )
                notificationManager.notify(ENCOUNTER_SYNC_NOTIFICATION_ID, successNotification)
                Result.success()
            }

        } catch (e: Exception) {
            AppLogger.e("❌ EncountersSyncWorker failed: ${e.localizedMessage}")
            val failedNotification = createForegroundNotification(
                "Encounter Sync Failed",
                "Synchronization encountered an error: ${e.localizedMessage}",
                0,
                0
            )
            notificationManager.notify(ENCOUNTER_SYNC_NOTIFICATION_ID, failedNotification)
            Result.retry()
        }
    }

    private fun buildEncounterPayload(entity: EncounterEntity): EncounterPayload {
        return EncounterPayload(
            uuid = entity.id,
            visitUuid = entity.visitUuid,
            encounterType = entity.encounterTypeUuid,
            encounterDatetime = entity.encounterDatetime,
            patientUuid = entity.patientUuid,
            locationUuid = entity.locationUuid,
            provider = entity.providerUuid,
            obs = entity.obs,
            orders = entity.orders,
            formUuid = entity.formUuid,
        )
    }
}




