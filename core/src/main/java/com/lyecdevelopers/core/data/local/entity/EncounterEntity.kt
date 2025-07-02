package com.lyecdevelopers.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lyecdevelopers.core.model.OpenmrsObs

@Entity(
    tableName = "encounters", foreignKeys = [ForeignKey(
        entity = VisitSummaryEntity::class,
        parentColumns = ["id"],
        childColumns = ["visitId"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index(value = ["visitId"])]
)
data class EncounterEntity(
    @PrimaryKey val id: String,
    val visitId: String,
    val encounterTypeUuid: String,
    val encounterDatetime: String,
    val patientUuid: String,
    val locationUuid: String,
    val obs: List<OpenmrsObs>,
    val synced: Boolean = false,
    val createdAt: String,
)

