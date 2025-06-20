package com.lyecdevelopers.worklist.presentation.worklist.state

import com.lyecdevelopers.core.data.local.entity.PatientEntity

data class WorklistUiState(
    val patients: List<PatientEntity> = emptyList(),
    val error: String? = null,
)
