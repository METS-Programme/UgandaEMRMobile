package com.lyecdevelopers.form.domain.repository

import com.lyecdevelopers.core.data.local.entity.PatientEntity
import com.lyecdevelopers.core.model.PatientWithVisits
import com.lyecdevelopers.core.model.Result
import kotlinx.coroutines.flow.Flow
import org.hl7.fhir.r4.model.Patient

interface PatientRepository {
    suspend fun getPatientWithVisits(patientId: String): Flow<Result<PatientWithVisits?>>
    suspend fun getAllPatientsWithVisits(): Flow<Result<List<PatientWithVisits>>>
    suspend fun createInFhir(patient: Patient)
    suspend fun updateInFhir(patient: Patient)
    suspend fun saveToLocalDb(entity: PatientEntity)
    suspend fun getLocalPatient(id: String): PatientEntity?
    suspend fun loadPatients(): Flow<Result<List<PatientEntity>>>
}