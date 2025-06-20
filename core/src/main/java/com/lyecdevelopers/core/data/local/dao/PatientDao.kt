package com.lyecdevelopers.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.lyecdevelopers.core.data.local.entity.PatientEntity
import com.lyecdevelopers.core.model.PatientWithVisits
import kotlinx.coroutines.flow.Flow


@Dao
interface PatientDao {

    // ✅ Return Flow without suspend for reactive observation
    @Transaction
    @Query("SELECT * FROM patients WHERE id = :patientId")
    fun observePatientWithVisits(patientId: String): Flow<PatientWithVisits?>

    @Transaction
    @Query("SELECT * FROM patients")
    fun observeAllPatientsWithVisits(): Flow<List<PatientWithVisits>>

    // ✅ For one-shot fetch (used in UseCase/ViewModel)
    @Transaction
    @Query("SELECT * FROM patients WHERE id = :patientId")
    suspend fun getPatientWithVisits(patientId: String): PatientWithVisits?

    @Transaction
    @Query("SELECT * FROM patients")
    suspend fun getAllPatientsWithVisits(): List<PatientWithVisits>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<PatientEntity>)

    @Update
    suspend fun updatePatient(patient: PatientEntity)

    @Delete
    suspend fun deletePatient(patient: PatientEntity)

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientById(id: String): PatientEntity?

    @Query("SELECT * FROM patients ORDER BY lastName ASC")
    suspend fun getAllPatients(): List<PatientEntity>

    @Query("DELETE FROM patients")
    suspend fun clearAll()
}

