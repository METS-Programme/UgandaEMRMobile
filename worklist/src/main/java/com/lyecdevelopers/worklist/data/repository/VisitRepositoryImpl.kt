package com.lyecdevelopers.worklist.data.repository

import com.lyecdevelopers.core.data.local.dao.VisitSummaryDao
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.worklist.domain.mapper.toDomain
import com.lyecdevelopers.worklist.domain.mapper.toEntity
import com.lyecdevelopers.worklist.domain.model.VisitSummary
import com.lyecdevelopers.worklist.domain.repository.VisitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class VisitRepositoryImpl @Inject constructor(
    private val visitSummaryDao: VisitSummaryDao,
) : VisitRepository {
    override suspend fun getVisitSummariesForPatient(patientId: String): Flow<Result<List<VisitSummary>>> =
        flow<Result<List<VisitSummary>>> {
            visitSummaryDao.getVisitSummariesForPatient(patientId).map { visitWithDetails ->
                visitWithDetails.visit.toDomain(
                    encounters = visitWithDetails.encounters, vitals = visitWithDetails.vitals
                )
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun saveVisitSummary(visit: VisitSummary): Flow<Result<Boolean>> = flow {
        visitSummaryDao.insertVisitSummary(visit.toEntity(visit.patientId))
        visit.vitals?.let {
            visitSummaryDao.insertVitals(it.toEntity(visit.id))
        }
        visitSummaryDao.insertEncounters(visit.encounters.map { it.toEntity(visit.id) })
    }
}