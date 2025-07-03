package com.lyecdevelopers.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lyecdevelopers.core.model.OpenmrsObs
import com.lyecdevelopers.core.model.encounter.Order

@Entity(
    tableName = "encounters", foreignKeys = [ForeignKey(
        entity = VisitSummaryEntity::class,
        parentColumns = ["id"], childColumns = ["visitUuid"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index(value = ["visitUuid"])]
)

data class EncounterEntity(
    @PrimaryKey val id: String,
    val encounterTypeUuid: String,
    val encounterDatetime: String,
    val patientUuid: String,
    val locationUuid: String,
    val providerUuid: String?,
    val obs: List<OpenmrsObs> = emptyList(),
    val orders: List<Order> = emptyList(),
    val formUuid: String? = null,
    val visitUuid: String,
    val voided: Boolean = false,
    val synced: Boolean = false,
    val createdAt: String,
)



