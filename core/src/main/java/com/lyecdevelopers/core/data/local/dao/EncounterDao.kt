package com.lyecdevelopers.core.data.local.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lyecdevelopers.core.data.local.entity.EncounterEntity

interface EncounterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(encounter: EncounterEntity)

    @Query("SELECT * FROM encounters WHERE synced = 0")
    suspend fun getUnsynced(): List<EncounterEntity>

    @Update
    suspend fun update(encounter: EncounterEntity)
}
