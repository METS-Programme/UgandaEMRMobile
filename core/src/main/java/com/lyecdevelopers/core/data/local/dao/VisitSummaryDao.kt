package com.lyecdevelopers.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lyecdevelopers.core.data.local.entity.EncounterEntity
import com.lyecdevelopers.core.data.local.entity.VisitSummaryEntity
import com.lyecdevelopers.core.data.local.entity.VitalsEntity
import com.lyecdevelopers.core.model.VisitWithDetails

@Dao
interface VisitSummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitSummary(visit: VisitSummaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitals(vitals: VitalsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEncounters(encounters: List<EncounterEntity>)

    @Transaction
    @Query("SELECT * FROM visit_summaries WHERE patientId = :patientId")
    suspend fun getVisitSummariesForPatient(patientId: String): List<VisitWithDetails>
}
