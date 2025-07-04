package com.lyecdevelopers.worklist.domain.usecase

import com.lyecdevelopers.worklist.domain.repository.VisitRepository
import javax.inject.Inject

class VisitUseCases @Inject constructor(private val visitRepository: VisitRepository) {

//    suspend fun getVisitSummariesForPatient(patientId: String): Flow<Result<List<VisitSummary>>> {
//        return visitRepository.getVisitSummariesForPatient(patientId)
//    }

//    suspend fun saveSummary(visitSummary: VisitSummary): Flow<Result<Boolean>> {
//        return visitRepository.saveVisitSummary(visitSummary)
//    }
}