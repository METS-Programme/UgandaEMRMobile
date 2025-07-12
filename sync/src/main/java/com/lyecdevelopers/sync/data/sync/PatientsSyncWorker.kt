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


    private fun ensureNotificationChannelExists() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SYNC_NOTIFICATION_CHANNEL_ID,
                SYNC_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = SYNC_NOTIFICATION_CHANNEL_DESCRIPTION
                setSound(null, null)
                enableVibration(false)
            }
            // Create the channel. If it already exists, this call does nothing.
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Helper function to create the foreground notification
    private fun createForegroundNotification(
        title: String,
        content: String,
        progress: Int = 0,
        maxProgress: Int = 0,
    ): Notification {

        ensureNotificationChannelExists()
        val builder = NotificationCompat.Builder(applicationContext, SYNC_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title).setContentText(content)
            .setSmallIcon(com.lyecdevelopers.core.R.drawable.ic_launcher_foreground)
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

        // 1. Create and set the initial foreground notification
        val initialNotification = createForegroundNotification(
            "Syncing Patient Data", "Starting synchronization...", 0, // progress
            0  // maxProgress (indeterminate initially)
        )
        // This promotes the worker to a foreground service
        val foregroundInfo = ForegroundInfo(PATIENT_SYNC_NOTIFICATION_ID, initialNotification)
        setForeground(foregroundInfo)

        return try {
            var shouldRetry = false
            var totalPatientsToSync = 0
            var syncedPatientsCount = 0

            syncUseCase.getUnsyncedPatients().catch { e ->
                AppLogger.e("❌ DB read failed: ${e.message}")
                shouldRetry = true
                // Update notification on error
                val errorNotification = createForegroundNotification(
                    "Sync Failed", "Error reading local data: ${e.message}", 0, 0
                )
                notificationManager.notify(PATIENT_SYNC_NOTIFICATION_ID, errorNotification)
            }.collect { unsyncedList ->
                totalPatientsToSync = unsyncedList.size

                if (totalPatientsToSync == 0) {
                    AppLogger.d("✅ No unsynced patients. Nothing to sync.")
                    // Update notification for completion
                    val completedNotification = createForegroundNotification(
                        "Sync Complete", "No unsynced patients found.", 1, 1 // Indicate completion
                    )
                    notificationManager.notify(PATIENT_SYNC_NOTIFICATION_ID, completedNotification)
                } else {
                    AppLogger.d("🔄 Syncing ${unsyncedList.size} unsynced patients...")

                    // Update notification with initial progress count
                    val progressNotification = createForegroundNotification(
                        "Syncing Patient Data",
                        "Syncing 0 of $totalPatientsToSync patients...",
                        0,
                        totalPatientsToSync
                    )
                    notificationManager.notify(PATIENT_SYNC_NOTIFICATION_ID, progressNotification)


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
                                    // Optionally update notification on inner error
                                }.collect {
                                    AppLogger.d("✅ Patient ${entity.id} marked synced.")
                                    syncedPatientsCount++
                                    // Update notification with current progress
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
                                // Optionally update notification on API rejection
                            }

                        } catch (e: Exception) {
                            AppLogger.e("❌ Error syncing patient ${entity.id}: ${e.message}")
                            shouldRetry = true
                            // Optionally update notification on patient-specific error
                        }
                    }
                }
            }

            // Final notification update based on result
            if (shouldRetry) {
                AppLogger.d("🔁 Retrying PatientsSyncWorker...")
                val retryNotification = createForegroundNotification(
                    "Sync Needs Retry",
                    "Some patients failed to sync. Retrying...",
                    syncedPatientsCount,
                    totalPatientsToSync
                )
                notificationManager.notify(PATIENT_SYNC_NOTIFICATION_ID, retryNotification)
                Result.retry()
            } else {
                AppLogger.d("✅ PatientsSyncWorker success")
                val successNotification = createForegroundNotification(
                    "Sync Complete",
                    "All $syncedPatientsCount patients synced successfully.",
                    syncedPatientsCount,
                    totalPatientsToSync // Show final progress
                )
                notificationManager.notify(PATIENT_SYNC_NOTIFICATION_ID, successNotification)
                Result.success()
            }

        } catch (e: Exception) {
            AppLogger.e("❌ PatientsSyncWorker failed: ${e.localizedMessage}")
            val failedNotification = createForegroundNotification(
                "Sync Failed", "Synchronization encountered an error: ${e.localizedMessage}", 0, 0
            )
            notificationManager.notify(PATIENT_SYNC_NOTIFICATION_ID, failedNotification)
            Result.retry()
        }
    }

    private val notificationManager
        get() = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

}

