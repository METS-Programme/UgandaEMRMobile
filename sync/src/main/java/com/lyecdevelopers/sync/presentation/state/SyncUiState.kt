package com.lyecdevelopers.sync.presentation.state

import com.lyecdevelopers.core.model.Form
import com.lyecdevelopers.core.model.cohort.Attribute
import com.lyecdevelopers.core.model.cohort.Cohort
import com.lyecdevelopers.core.model.cohort.Indicator
import com.lyecdevelopers.core.model.encounter.EncounterType
import com.lyecdevelopers.core.model.order.OrderType
import java.time.LocalDate

data class SyncUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val formItems: List<Form> = emptyList(),
    val selectedFormIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val formCount: Int = 0,
    val patientCount: Int = 0,
    val encounterCount: Int = 0,

    val cohorts: List<Cohort> = emptyList(),
    val selectedCohort: Cohort? = null,

    val selectedDateRange: Pair<LocalDate, LocalDate>? = null,
    val selectedIndicator: Indicator? = null,

    val encounterTypes: List<EncounterType> = emptyList(),
    val orderTypes: List<OrderType> = emptyList(),

    val availableParameters: List<Attribute> = emptyList(),
    val selectedParameters: List<Attribute> = emptyList(),
    val highlightedAvailable: List<Attribute> = emptyList(),
    val highlightedSelected: List<Attribute> = emptyList(),
)


