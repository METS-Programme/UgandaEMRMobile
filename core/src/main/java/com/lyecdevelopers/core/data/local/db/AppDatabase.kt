package com.lyecdevelopers.core.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lyecdevelopers.core.data.local.dao.FormDao
import com.lyecdevelopers.core.data.local.dao.PatientDao
import com.lyecdevelopers.core.data.local.entity.FormEntity
import com.lyecdevelopers.core.data.local.entity.PatientEntity

@Database(
    entities = [FormEntity::class, PatientEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun formDao(): FormDao
    abstract fun patientDao(): PatientDao
}
