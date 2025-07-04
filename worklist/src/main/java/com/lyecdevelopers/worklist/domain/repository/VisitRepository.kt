package com.lyecdevelopers.worklist.domain.repository

import com.lyecdevelopers.core.data.local.entity.VisitEntity
import com.lyecdevelopers.core.model.Result
import kotlinx.coroutines.flow.Flow

interface VisitRepository {
    suspend fun getVisitSummariesForPatient(patientId: String): Flow<Result<List<VisitEntity>>>
    suspend fun saveVisitSummary(visit: VisitEntity): Flow<Result<Boolean>>
}
