package com.lyecdevelopers.sync.presentation.event

import com.lyecdevelopers.core.model.cohort.Attribute
import com.lyecdevelopers.core.model.cohort.Cohort
import com.lyecdevelopers.core.model.cohort.Indicator
import com.lyecdevelopers.core.model.o3.o3Form

sealed class SyncEvent {
    data class FilterForms(val query: String) : SyncEvent()
    data class ToggleFormSelection(val uuid: String) : SyncEvent()

    data class FormsDownloaded(val selectedForms: List<o3Form>) : SyncEvent()

    object ClearSelection : SyncEvent()
    object DownloadForms : SyncEvent()

    data class SelectedCohortChanged(val cohort: Cohort) : SyncEvent()
    data class IndicatorSelected(val indicator: Indicator) : SyncEvent()
    object ApplyFilters : SyncEvent()

    data class ToggleHighlightAvailable(val item: Attribute) : SyncEvent()
    data class ToggleHighlightSelected(val item: Attribute) : SyncEvent()
    object MoveRight : SyncEvent()
    object MoveLeft : SyncEvent()

    // ✅ NEW: Sync metadata updates
    data class UpdateLastSyncTime(val time: String) : SyncEvent()
    data class UpdateLastSyncStatus(val status: String) : SyncEvent()
    data class UpdateLastSyncBy(val user: String) : SyncEvent()
    data class UpdateLastSyncError(val error: String?) : SyncEvent()

    data class ToggleAutoSync(val enabled: Boolean) : SyncEvent()
    data class UpdateAutoSyncInterval(val interval: String) : SyncEvent()
}

