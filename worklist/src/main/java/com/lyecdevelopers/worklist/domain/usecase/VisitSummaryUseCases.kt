package com.lyecdevelopers.worklist.domain.usecase

import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.worklist.domain.model.VisitSummary
import com.lyecdevelopers.worklist.domain.repository.VisitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VisitSummaryUseCase @Inject constructor(private val visitRepository: VisitRepository) {

    suspend fun getVisitSummariesForPatient(patientId: String): Flow<Result<List<VisitSummary>>> {
        return visitRepository.getVisitSummariesForPatient(patientId)
    }

    suspend fun saveSummary(visitSummary: VisitSummary): Flow<Result<Boolean>> {
        return visitRepository.saveVisitSummary(visitSummary)
    }
}