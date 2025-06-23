package com.lyecdevelopers.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "vitals", foreignKeys = [ForeignKey(
        entity = VisitSummaryEntity::class,
        parentColumns = ["id"],
        childColumns = ["visitId"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index(value = ["visitId"])]
)
data class VitalsEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val visitId: String,
    val temperature: Double?,
    val pulse: Int?,
    val bloodPressure: String?,
)
