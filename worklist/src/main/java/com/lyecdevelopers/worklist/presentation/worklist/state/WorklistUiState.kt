package com.lyecdevelopers.worklist.presentation.worklist.state

import com.lyecdevelopers.core.data.local.entity.PatientEntity
import com.lyecdevelopers.worklist.domain.model.Encounter
import com.lyecdevelopers.worklist.domain.model.VisitSummary
import com.lyecdevelopers.worklist.domain.model.Vitals

data class WorklistUiState(
    val patients: List<PatientEntity> = emptyList(),
    val selectedPatient: PatientEntity? = null,
    val visits: List<VisitSummary> = emptyList(),
    val encounters: List<Encounter> = emptyList(),
    val vitals: Vitals? = null,
    val error: String? = null,
)

