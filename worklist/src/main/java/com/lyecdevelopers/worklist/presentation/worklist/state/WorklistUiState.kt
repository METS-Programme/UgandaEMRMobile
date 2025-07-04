package com.lyecdevelopers.worklist.presentation.worklist.state

import com.lyecdevelopers.core.data.local.entity.EncounterEntity
import com.lyecdevelopers.core.data.local.entity.PatientEntity
import com.lyecdevelopers.core.data.local.entity.VisitEntity
import com.lyecdevelopers.worklist.domain.model.Vitals

data class WorklistUiState(
    val patients: List<PatientEntity> = emptyList(),
    val selectedPatient: PatientEntity? = null,
    val visits: List<VisitEntity> = emptyList(),
    val encounters: List<EncounterEntity> = emptyList(),
    val vitals: Vitals? = null,
    val error: String? = null,
)

