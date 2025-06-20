package com.lyecdevelopers.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lyecdevelopers.core.model.VisitStatus

@Entity(
    tableName = "visits", foreignKeys = [ForeignKey(
        entity = PatientEntity::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index("patientId")]
)
data class VisitEntity(
    @PrimaryKey val id: String,
    val patientId: String,
    val visitType: String,
    val status: VisitStatus,
    val scheduledTime: String, // or use `LocalDateTime` with type converters
)



