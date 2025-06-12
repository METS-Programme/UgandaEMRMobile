package com.lyecdevelopers.sync.presentation.forms.event

import com.lyecdevelopers.core.model.o3.o3Form

sealed class DownloadFormsUiEvent {
    data class ShowSnackbar(val message: String) : DownloadFormsUiEvent()
    data class FormsDownloaded(val selectedForms: List<o3Form>) : DownloadFormsUiEvent()
}
