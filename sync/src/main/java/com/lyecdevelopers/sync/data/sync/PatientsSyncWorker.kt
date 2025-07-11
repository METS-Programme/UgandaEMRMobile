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
import org.hl7.fhir.r4.model.Patient

@HiltWorker
class PatientsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncUseCase: SyncUseCase,
    private val api: FormApi,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        AppLogger.d("🔄 PatientsSyncWorker started")

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
                                }.collect {
                                    AppLogger.d("✅ Patient ${entity.id} marked synced.")
                                }
                            } else {
                                AppLogger.e("❌ API rejected: ${response.code()} ${response.message()}")
                                shouldRetry = true
                            }

                        } catch (e: Exception) {
                            AppLogger.e("❌ Error syncing patient ${entity.id}: ${e.message}")
                            shouldRetry = true
                        }
                    }
                }
            }

            if (shouldRetry) {
                AppLogger.d("🔁 Retrying PatientsSyncWorker...")
                Result.retry()
            } else {
                AppLogger.d("✅ PatientsSyncWorker success")
                Result.success()
            }

        } catch (e: Exception) {
            AppLogger.e("❌ PatientsSyncWorker failed: ${e.localizedMessage}")
            Result.retry()
        }
    }
}

