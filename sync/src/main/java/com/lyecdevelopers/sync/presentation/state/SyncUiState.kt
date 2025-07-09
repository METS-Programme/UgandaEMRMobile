package com.lyecdevelopers.sync.presentation.state

import com.lyecdevelopers.core.model.Form
import com.lyecdevelopers.core.model.cohort.Attribute
import com.lyecdevelopers.core.model.cohort.Cohort
import com.lyecdevelopers.core.model.cohort.Indicator
import com.lyecdevelopers.core.model.cohort.IndicatorRepository

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

    val selectedIndicator: Indicator? = null,

    val encounterTypes: List<Attribute> = emptyList(),
    val orderTypes: List<Attribute> = emptyList(),

    val identifiers: List<Attribute> = emptyList(),
    val personAttributeTypes: List<Attribute> = emptyList(),

    val customAvailableParameters: List<Attribute>? = null,

    val selectedParameters: List<Attribute> = IndicatorRepository.defaultSelectedAttributes,
    val highlightedAvailable: List<Attribute> = emptyList(),
    val highlightedSelected: List<Attribute> = emptyList(),

    val lastSyncTime: String = "Not synced yet",
    val lastSyncStatus: String = "Never Synced",
    val lastSyncBy: String = "N/A",
    val lastSyncError: String? = null,
    val autoSyncEnabled: Boolean = false,
    val autoSyncInterval: String = "15 minutes",
) {

    val availableParameters: List<Attribute>
        get() = customAvailableParameters
            ?: (encounterTypes + orderTypes + identifiers + personAttributeTypes)
                .distinctBy { it.id }
}




