package com.lyecdevelopers.sync.presentation.event

import com.lyecdevelopers.core.model.cohort.Attribute
import com.lyecdevelopers.core.model.cohort.Cohort
import com.lyecdevelopers.core.model.cohort.Indicator
import com.lyecdevelopers.core.model.o3.o3Form
import java.time.LocalDate

sealed class SyncEvent {
    data class FilterForms(val query: String) : SyncEvent()
    data class ToggleFormSelection(val uuid: String) : SyncEvent()

    data class FormsDownloaded(val selectedForms: List<o3Form>) : SyncEvent()

    object ClearSelection : SyncEvent()
    object DownloadForms : SyncEvent()


    data class SelectedCohortChanged(val cohort: Cohort) : SyncEvent()
    data class IndicatorSelected(val indicator: Indicator) : SyncEvent()
    data class DateRangeSelected(val range: Pair<LocalDate, LocalDate>) : SyncEvent()
    object ApplyFilters : SyncEvent()

    data class ToggleHighlightAvailable(val item: Attribute) : SyncEvent()
    data class ToggleHighlightSelected(val item: Attribute) : SyncEvent()
    object MoveRight : SyncEvent()
    object MoveLeft : SyncEvent()
}
