package com.lyecdevelopers.sync.data.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import ca.uhn.fhir.context.FhirContext
import com.lyecdevelopers.core.R
import com.lyecdevelopers.core.data.remote.FormApi
import com.lyecdevelopers.core.utils.AppLogger
import com.lyecdevelopers.core.utils.NotificationConstants.PATIENT_SYNC_NOTIFICATION_ID
import com.lyecdevelopers.core.utils.NotificationConstants.SYNC_NOTIFICATION_CHANNEL_DESCRIPTION
import com.lyecdevelopers.core.utils.NotificationConstants.SYNC_NOTIFICATION_CHANNEL_ID
import com.lyecdevelopers.core.utils.NotificationConstants.SYNC_NOTIFICATION_CHANNEL_NAME
import com.lyecdevelopers.form.utils.toFhirPatient
import com.lyecdevelopers.sync.domain.usecase.SyncUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.catch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.hl7.fhir.r4.model.Patient


@HiltWorker
class PatientsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncUseCase: SyncUseCase,
    private val api: FormApi,
) : CoroutineWorker(appContext, workerParams) {

    private val notificationManager
        get() = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun ensureNotificationChannelExists() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SYNC_NOTIFICATION_CHANNEL_ID, // Use constant
                SYNC_NOTIFICATION_CHANNEL_NAME, // Use constant
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = SYNC_NOTIFICATION_CHANNEL_DESCRIPTION // Use constant
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
        val builder = NotificationCompat.Builder(
            applicationContext, SYNC_NOTIFICATION_CHANNEL_ID // Use constant
        ).setContentTitle(title).setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true).setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (progress > 0 && maxProgress > 0) {
            builder.setProgress(maxProgress, progress, false)
        } else {
            builder.setProgress(0, 0, true)
        }
        return builder.build()
    }

    override suspend fun doWork(): Result {
        AppLogger.d("🔄 PatientsSyncWorker started")

        val initialNotification = createForegroundNotification(
            "Syncing Patient Data", "Starting synchronization...", 0, 0 // Indeterminate initially
        )
        val foregroundInfo = ForegroundInfo(PATIENT_SYNC_NOTIFICATION_ID, initialNotification)
        setForeground(foregroundInfo)

        return try {
            var shouldRetry = false
            var totalPatientsToSync = 0
            var syncedPatientsCount = 0

            syncUseCase.getUnsyncedPatients().catch { e ->
                AppLogger.e("❌ DB read failed: ${e.message}")
                shouldRetry = true
                val errorNotification = createForegroundNotification(
                    "Patient Sync Failed", "Error reading local data: ${e.message}", 0, 0
                )
                notificationManager.notify(
                    PATIENT_SYNC_NOTIFICATION_ID, errorNotification
                )
            }.collect { unsyncedList ->
                totalPatientsToSync = unsyncedList.size

                if (totalPatientsToSync == 0) {
                    AppLogger.d("✅ No unsynced patients. Nothing to sync.")
                    val completedNotification = createForegroundNotification(
                        "Patient Sync Complete", "No unsynced patients found.", 1, 1
                    )
                    notificationManager.notify(
                        PATIENT_SYNC_NOTIFICATION_ID, completedNotification
                    )
                } else {
                    AppLogger.d("🔄 Syncing ${unsyncedList.size} unsynced patients...")

                    val progressNotification = createForegroundNotification(
                        "Syncing Patient Data",
                        "Syncing 0 of $totalPatientsToSync patients...",
                        0,
                        totalPatientsToSync
                    )
                    notificationManager.notify(
                        PATIENT_SYNC_NOTIFICATION_ID, progressNotification
                    )

                    unsyncedList.forEach { entity ->
                        try {
                            val fhirPatient: Patient = entity.toFhirPatient()
                            val patientJson = FhirContext.forR4().newJsonParser()
                                .encodeResourceToString(fhirPatient)
                            val requestBody = patientJson.toRequestBody(
                                "application/fhir+json".toMediaType()
                            )

                            val response = api.savePatient(requestBody)

                            if (response.isSuccessful) {
                                syncUseCase.markSyncedPatient(entity).catch { markErr ->
                                    AppLogger.e("⚠️ Mark local failed: ${markErr.message}")
                                    shouldRetry = true
                                    val currentProgressNotification = createForegroundNotification(
                                        "Syncing Patient Data (with issues)",
                                        "Syncing ${syncedPatientsCount} of ${totalPatientsToSync}. Error marking synced: ${markErr.message}",
                                        syncedPatientsCount,
                                        totalPatientsToSync
                                    )
                                    notificationManager.notify(
                                        PATIENT_SYNC_NOTIFICATION_ID, currentProgressNotification
                                    )
                                }.collect {
                                    AppLogger.d("✅ Patient ${entity.id} marked synced.")
                                    syncedPatientsCount++
                                    val currentProgressNotification = createForegroundNotification(
                                        "Syncing Patient Data",
                                        "Syncing $syncedPatientsCount of $totalPatientsToSync patients...",
                                        syncedPatientsCount,
                                        totalPatientsToSync
                                    )
                                    notificationManager.notify(
                                        PATIENT_SYNC_NOTIFICATION_ID, currentProgressNotification
                                    )
                                }
                            } else {
                                AppLogger.e("❌ API rejected: ${response.code()} ${response.message()}")
                                shouldRetry = true
                                val currentProgressNotification = createForegroundNotification(
                                    "Syncing Patient Data (with errors)",
                                    "Failed to sync patient ${entity.id}: ${response.message()} (${syncedPatientsCount} of ${totalPatientsToSync})",
                                    syncedPatientsCount,
                                    totalPatientsToSync
                                )
                                notificationManager.notify(
                                    PATIENT_SYNC_NOTIFICATION_ID, currentProgressNotification
                                )
                            }

                        } catch (e: Exception) {
                            AppLogger.e("❌ Error syncing patient ${entity.id}: ${e.message}")
                            shouldRetry = true
                            val currentProgressNotification = createForegroundNotification(
                                "Syncing Patient Data (with errors)",
                                "Error syncing patient ${entity.id}: ${e.message} (${syncedPatientsCount} of ${totalPatientsToSync})",
                                syncedPatientsCount,
                                totalPatientsToSync
                            )
                            notificationManager.notify(
                                PATIENT_SYNC_NOTIFICATION_ID, currentProgressNotification
                            )
                        }
                    }
                }
            }

            if (shouldRetry) {
                AppLogger.d("🔁 Retrying PatientsSyncWorker...")
                val retryNotification = createForegroundNotification(
                    "Patient Sync Needs Retry",
                    "Some patients failed to sync. Retrying...",
                    syncedPatientsCount,
                    totalPatientsToSync
                )
                notificationManager.notify(
                    PATIENT_SYNC_NOTIFICATION_ID, retryNotification
                )
                Result.retry()
            } else {
                AppLogger.d("✅ PatientsSyncWorker success")
                val successNotification = createForegroundNotification(
                    "Patient Sync Complete",
                    "All $syncedPatientsCount patients synced successfully.",
                    syncedPatientsCount,
                    totalPatientsToSync
                )
                notificationManager.notify(
                    PATIENT_SYNC_NOTIFICATION_ID, successNotification
                )
                Result.success()
            }

        } catch (e: Exception) {
            AppLogger.e("❌ PatientsSyncWorker failed: ${e.localizedMessage}")
            val failedNotification = createForegroundNotification(
                "Patient Sync Failed",
                "Synchronization encountered an error: ${e.localizedMessage}",
                0,
                0
            )
            notificationManager.notify(
                PATIENT_SYNC_NOTIFICATION_ID, failedNotification
            )
            Result.retry()
        }
    }
}

