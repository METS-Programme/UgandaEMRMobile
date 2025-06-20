package com.lyecdevelopers.form.domain.usecase

import com.lyecdevelopers.core.data.local.entity.PatientEntity
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.form.domain.repository.PatientRepository
import kotlinx.coroutines.flow.Flow
import org.hl7.fhir.r4.model.Patient
import javax.inject.Inject


class PatientsUseCase @Inject constructor(
    private val repository: PatientRepository,
) {

    private suspend fun createPatient(patient: Patient, entity: PatientEntity) {
        repository.createInFhir(patient)
        repository.saveToLocalDb(entity)
    }

    private suspend fun updatePatient(patient: Patient, entity: PatientEntity) {
        repository.updateInFhir(patient)
        repository.saveToLocalDb(entity)
    }

    suspend fun saveLocallyOnly(entity: PatientEntity) {
        repository.saveToLocalDb(entity)
    }

    suspend fun getLocalPatient(id: String): PatientEntity? {
        return repository.getLocalPatient(id)
    }

    suspend fun loadPatients(): Flow<Result<List<PatientEntity>>> {
        return repository.loadPatients()
    }

    /**
     * Common submit logic for patient registration
     */
    suspend fun submitPatient(
        patient: Patient,
        isEdit: Boolean,
        entity: PatientEntity,
    ) {
        if (isEdit) {
            updatePatient(patient, entity)
        } else {
            createPatient(patient, entity)
        }
    }
}
