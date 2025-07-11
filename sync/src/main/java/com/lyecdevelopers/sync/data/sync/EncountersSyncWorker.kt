package com.lyecdevelopers.sync.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lyecdevelopers.core.data.local.entity.EncounterEntity
import com.lyecdevelopers.core.data.remote.FormApi
import com.lyecdevelopers.core.model.encounter.EncounterPayload
import com.lyecdevelopers.core.utils.AppLogger
import com.lyecdevelopers.sync.domain.usecase.SyncUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.catch


@HiltWorker
class EncountersSyncWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncUseCase: SyncUseCase,
    private val api: FormApi,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            var shouldRetry = false
            syncUseCase.getUnsynced().catch { e ->
                AppLogger.e("❌ DB read failed: ${e.message}")
                shouldRetry = true
            }.collect { unsyncedList ->
                if (unsyncedList.isEmpty()) {
                    AppLogger.d("✅ No unsynced encounters. Nothing to sync.")
                } else {
                    AppLogger.d("🔄 Syncing ${unsyncedList.size} unsynced encounters...")

                    unsyncedList.forEach { entity ->
                        try {
                            val payload = buildEncounterPayload(entity)
                            val response = api.saveEncounter(payload)

                            if (response.isSuccessful) {
                                syncUseCase.markSynced(entity).catch { e ->
                                    AppLogger.e("⚠️ Synced ${entity.id} but failed to mark local: ${e.message}")
                                    shouldRetry = true
                                }.collect {
                                    AppLogger.d("✅ Encounter ${entity.id} marked as synced.")
                                }
                            } else {
                                AppLogger.e(
                                    "❌ API rejected encounter ${entity.id}: ${response.code()} ${response.message()}"
                                )
                                shouldRetry = true
                            }

                        } catch (e: Exception) {
                            AppLogger.e("❌ Error syncing encounter ${entity.id}: ${e.message}")
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

    private fun buildEncounterPayload(entity: EncounterEntity): EncounterPayload {
        return EncounterPayload(
            uuid = this.id.toString(),
            visitUuid = entity.id,
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



