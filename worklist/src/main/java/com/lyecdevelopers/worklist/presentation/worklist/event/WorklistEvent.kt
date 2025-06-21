package com.lyecdevelopers.worklist.presentation.worklist.event

import com.lyecdevelopers.core.model.VisitStatus

sealed class WorklistEvent {
    data class OnNameFilterChanged(val name: String) : WorklistEvent()
    data class OnGenderFilterChanged(val gender: String?) : WorklistEvent()
    data class OnStatusFilterChanged(val status: VisitStatus?) : WorklistEvent()
    object OnClearFilters : WorklistEvent()
    object OnRefresh : WorklistEvent()
    data class OnPatientSelected(val patientId: String) : WorklistEvent()
}
