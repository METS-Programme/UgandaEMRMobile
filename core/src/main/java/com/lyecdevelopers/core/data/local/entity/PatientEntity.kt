package com.lyecdevelopers.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lyecdevelopers.core.model.VisitStatus
import java.util.UUID

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey val id: String = UUID.randomUUID()
        .toString(),  // auto-generate UUID if none provided
    val patientIdentifier: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val dateOfBirth: String,
    val phoneNumber: String? = null,
    val address: String? = null,
    val status: VisitStatus,
    val synced: Boolean = false,
)


