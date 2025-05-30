package com.lyecdevelopers.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lyecdevelopers.core.data.local.entity.FormEntity

import androidx.room.*

@Dao
interface FormDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForm(form: FormEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForms(forms: List<FormEntity>)

    @Query("SELECT * FROM forms WHERE uuid = :uuid")
    suspend fun getFormById(uuid: String): FormEntity?

    @Query("SELECT * FROM forms ORDER BY createdAt DESC")
    suspend fun getAllForms(): List<FormEntity>

    @Update
    suspend fun updateForm(form: FormEntity)

    @Delete
    suspend fun deleteForm(form: FormEntity)

    @Query("DELETE FROM forms")
    suspend fun deleteAllForms()
}
