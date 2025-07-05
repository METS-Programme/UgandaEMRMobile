package com.lyecdevelopers.worklist.domain.usecase

import com.lyecdevelopers.core.data.local.entity.VisitEntity
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.core.model.VisitWithDetails
import com.lyecdevelopers.worklist.domain.repository.VisitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class VisitUseCases @Inject constructor(private val visitRepository: VisitRepository) {

    suspend fun getVisitSummariesForPatient(patientId: String): Flow<Result<List<VisitWithDetails>>> {
        return visitRepository.getVisitSummariesForPatient(patientId)
    }

    fun saveVisit(visit: VisitEntity): Flow<Result<Boolean>> {
        return visitRepository.saveVisit(visit)
    }

    fun getMostRecentVisitForPatient(patientId: String): Flow<Result<VisitWithDetails>> {
        return visitRepository.getMostRecentForVisitPatient(patientId)
    }

}