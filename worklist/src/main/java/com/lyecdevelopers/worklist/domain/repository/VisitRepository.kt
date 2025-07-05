package com.lyecdevelopers.worklist.domain.repository

import com.lyecdevelopers.core.data.local.entity.EncounterEntity
import com.lyecdevelopers.core.data.local.entity.VisitEntity
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.core.model.VisitWithDetails
import kotlinx.coroutines.flow.Flow

interface VisitRepository {
    fun getVisitSummariesForPatient(patientId: String): Flow<Result<List<VisitWithDetails>>>

    // most recent visit
    fun getMostRecentForVisitPatient(patientId: String): Flow<Result<VisitWithDetails>>

    fun saveVisit(visit: VisitEntity): Flow<Result<Boolean>>


    fun getEncountersByPatientIdAndVisitId(
        patientId: String,
        visitId: String,
    ): Flow<Result<List<EncounterEntity>>>

}
