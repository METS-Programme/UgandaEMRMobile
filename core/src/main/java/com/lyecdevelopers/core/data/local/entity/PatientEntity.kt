package com.lyecdevelopers.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val dateOfBirth: String, // Could use LocalDate with proper converters
    val phoneNumber: String?,
    val address: String?,
    val isSynced: Boolean = false
)

