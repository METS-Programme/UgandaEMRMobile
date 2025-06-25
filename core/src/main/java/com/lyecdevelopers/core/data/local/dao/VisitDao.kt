package com.lyecdevelopers.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lyecdevelopers.core.data.local.entity.VisitEntity
import com.lyecdevelopers.core.model.VisitStatus

@Dao
interface VisitDao {

    // ➕ Add a single visit
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: VisitEntity)

    // ➕ Add multiple visits (e.g., from sync)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(visits: List<VisitEntity>)

    // 🔍 Get all visits for a patient (most common)
    @Query("SELECT * FROM visits WHERE patientId = :patientId ORDER BY scheduledTime DESC")
    suspend fun getVisitsForPatient(patientId: String): List<VisitEntity>

    // 🔍 Get a single visit by ID
    @Query("SELECT * FROM visits WHERE id = :visitId")
    suspend fun getVisitById(visitId: String): VisitEntity?

    // 🔄 Update visit status
    @Query("UPDATE visits SET status = :status WHERE id = :visitId")
    suspend fun updateVisitStatus(visitId: String, status: VisitStatus)

    // ❌ Delete a visit
    @Delete
    suspend fun deleteVisit(visit: VisitEntity)

    // 🔍 Get unsynced or pending visits (for syncing)
    @Query("SELECT * FROM visits WHERE status = :status")
    suspend fun getVisitsByStatus(status: VisitStatus): List<VisitEntity>
}

