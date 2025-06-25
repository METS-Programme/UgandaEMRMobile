package com.lyecdevelopers.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "visit_summaries", foreignKeys = [ForeignKey(
        entity = PatientEntity::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index(value = ["patientId"])]
)
data class VisitSummaryEntity(
    @PrimaryKey val id: String,
    val type: String,
    val date: String,
    val status: String,
    val notes: String,
    val patientId: String,
)
