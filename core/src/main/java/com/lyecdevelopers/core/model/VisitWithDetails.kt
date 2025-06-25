package com.lyecdevelopers.core.model

import androidx.room.Embedded
import androidx.room.Relation
import com.lyecdevelopers.core.data.local.entity.EncounterEntity
import com.lyecdevelopers.core.data.local.entity.VisitSummaryEntity
import com.lyecdevelopers.core.data.local.entity.VitalsEntity

data class VisitWithDetails(
    @Embedded val visit: VisitSummaryEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "visitId"
    )
    val encounters: List<EncounterEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "visitId"
    )
    val vitals: VitalsEntity? = null,
)
