package com.lyecdevelopers.form.data.repository

import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteException
import com.lyecdevelopers.core.data.local.dao.PatientDao
import com.lyecdevelopers.core.data.local.entity.PatientEntity
import com.lyecdevelopers.core.model.PatientWithVisits
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.core.utils.AppLogger
import com.lyecdevelopers.form.domain.repository.PatientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.hl7.fhir.r4.model.Patient
import javax.inject.Inject

class PatientRepositoryImpl @Inject constructor(
    private val patientDao: PatientDao,
) : PatientRepository {
    override suspend fun getPatientWithVisits(patientId: String): Flow<Result<PatientWithVisits?>> =
        patientDao.getPatientWithVisits(patientId).map { Result.Success(it) }.catch { e ->
            AppLogger.e("getPatientWithVisits", e.message ?: "Unknown error", e)
        }.flowOn(Dispatchers.IO)


    override suspend fun getAllPatientsWithVisits(): Flow<Result<List<PatientWithVisits>>> =
        patientDao.getAllPatientsWithVisits().map { Result.Success(it) }.catch { e ->
            AppLogger.e("getAllPatientsWithVisits", e.message ?: "Unknown error", e)
        }.flowOn(Dispatchers.IO)


    override suspend fun createInFhir(patient: Patient) {
    }

    override suspend fun updateInFhir(patient: Patient) {
    }

    override suspend fun saveToLocalDb(entity: PatientEntity) {
        patientDao.insertPatient(entity)
    }

    override suspend fun getLocalPatient(id: String): PatientEntity? {
        return patientDao.getPatientById(id)
    }

    override suspend fun loadPatients(): Flow<Result<List<PatientEntity>>> = flow {
        emit(Result.Loading)

        try {
            val patients = patientDao.getAllPatients()
            emit(Result.Success(patients))

        } catch (e: SQLiteDatabaseLockedException) {
            AppLogger.e("loadPatients", "Database is locked", e)
            emit(Result.Error("Database is currently locked. Please try again."))

        } catch (e: SQLiteDatabaseCorruptException) {
            AppLogger.e("loadPatients", "Database is corrupt", e)
            emit(Result.Error("Database is corrupt. Please restore or reinstall."))

        } catch (e: SQLiteException) {
            AppLogger.e("loadPatients", "SQLite error", e)
            emit(Result.Error("Database error: ${e.localizedMessage}"))

        } catch (e: Exception) {
            AppLogger.e("loadPatients", "Unexpected error", e)
            emit(Result.Error("Unexpected error: ${e.localizedMessage ?: "Unknown"}"))
        }

    }.flowOn(Dispatchers.IO)

}
