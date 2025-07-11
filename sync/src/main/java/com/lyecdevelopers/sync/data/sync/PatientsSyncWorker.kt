package com.lyecdevelopers.sync.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ca.uhn.fhir.context.FhirContext
import com.lyecdevelopers.core.data.remote.FormApi
import com.lyecdevelopers.core.utils.AppLogger
import com.lyecdevelopers.form.utils.toFhirPatient
import com.lyecdevelopers.sync.domain.usecase.SyncUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.catch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

@HiltWorker
class PatientsSyncWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncUseCase: SyncUseCase,
    private val api: FormApi,
) : CoroutineWorker(context, workerParams) {


    override suspend fun doWork(): Result {
        return try {
            var shouldRetry = false

            syncUseCase.getUnsyncedPatients().catch { e ->
                AppLogger.e("❌ DB read failed: ${e.message}")
                shouldRetry = true
            }.collect { unsyncedList ->
                if (unsyncedList.isEmpty()) {
                    AppLogger.d("✅ No unsynced patients. Nothing to sync.")
                } else {
                    AppLogger.d("🔄 Syncing ${unsyncedList.size} unsynced patients...")

                    unsyncedList.forEach { entity ->
                        try {
                            // ➜ Convert to FHIR JSON
                            val fhirPatient = entity.toFhirPatient()
                            val patientJson = FhirContext.forR4().newJsonParser()
                                .encodeResourceToString(fhirPatient)
                            val requestBody = patientJson.toRequestBody(
                                "application/fhir+json".toMediaType()
                            )

                            val response = api.savePatient(requestBody)

                            if (response.isSuccessful) {
                                syncUseCase.markSyncedPatient(entity).catch { e ->
                                    AppLogger.e(
                                        "⚠️ Synced ${entity.id} but failed to mark local: ${e.message}"
                                    )
                                    shouldRetry = true
                                }.collect {
                                    AppLogger.d("✅ Patient ${entity.id} marked as synced.")
                                }
                            } else {
                                AppLogger.e(
                                    "❌ API rejected patient ${entity.id}: ${response.code()} ${response.message()}"
                                )
                                shouldRetry = true
                            }

                        } catch (e: Exception) {
                            AppLogger.e(
                                "❌ Error syncing patient ${entity.id}: ${e.message}"
                            )
                            shouldRetry = true
                        }
                    }
                }
            }

            if (shouldRetry) Result.retry() else Result.success()

        } catch (e: Exception) {
            AppLogger.e("❌ SyncWorker failed: ${e.localizedMessage}")
            Result.retry()
        }
    }
}

