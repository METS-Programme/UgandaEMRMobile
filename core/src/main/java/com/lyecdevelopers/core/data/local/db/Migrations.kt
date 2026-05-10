package com.lyecdevelopers.core.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Future migrations will be added here
        // Example:
        // database.execSQL("ALTER TABLE patients ADD COLUMN new_column TEXT")
    }
}
