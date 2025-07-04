package com.lyecdevelopers.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "vitals",
    foreignKeys = [ForeignKey(
        entity = VisitEntity::class,
        parentColumns = ["id"],
        childColumns = ["visitUuid"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["visitUuid"])]
)
data class VitalsEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val visitUuid: String,
    val temperature: Double?,
    val pulse: Int?,
    val bloodPressure: String?,
)


