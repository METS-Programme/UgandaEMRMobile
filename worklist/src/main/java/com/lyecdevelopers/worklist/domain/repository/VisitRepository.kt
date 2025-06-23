package com.lyecdevelopers.worklist.domain.repository

import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.worklist.domain.model.VisitSummary
import kotlinx.coroutines.flow.Flow

interface VisitRepository {
    suspend fun getVisitSummariesForPatient(patientId: String): Flow<Result<List<VisitSummary>>>
    suspend fun saveVisitSummary(visit: VisitSummary): Flow<Result<Boolean>>
}
