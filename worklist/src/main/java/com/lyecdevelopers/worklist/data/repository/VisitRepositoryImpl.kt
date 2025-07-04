package com.lyecdevelopers.worklist.data.repository

import com.lyecdevelopers.core.data.local.dao.VisitDao
import com.lyecdevelopers.core.data.local.entity.VisitEntity
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.worklist.domain.repository.VisitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VisitRepositoryImpl @Inject constructor(
    private val visitDao: VisitDao,
) : VisitRepository {
//    override suspend fun getVisitSummariesForPatient(patientId: String): Flow<Result<List<VisitEntity>>> =
//        flow<Result<List<VisitEntity>>> {
//            visitDao.getVisitDetailsForPatient(patientId).map {
//                visitWithDetails ->
//                visitWithDetails.visit.toDomain(
//                    encounters = visitWithDetails.encounters, vitals = visitWithDetails.vitals
//                )
//            }
//        }.flowOn(Dispatchers.IO)

    //    override suspend fun saveVisitSummary(visit: VisitSummary): Flow<Result<Boolean>> = flow {
//        visitDao.insertVisit(visit.toEntity(visit.patientId))
//        visit.vitals?.let {
//            visitDao.insertVitals(it.toEntity(visit.id))
//        }
//    }.flowOn(Dispatchers.IO)
    override suspend fun getVisitSummariesForPatient(patientId: String): Flow<Result<List<VisitEntity>>> {
        TODO("Not yet implemented")
    }

    override suspend fun saveVisitSummary(visit: VisitEntity): Flow<Result<Boolean>> {
        TODO("Not yet implemented")
    }
}