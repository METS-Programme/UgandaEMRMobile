package com.lyecdevelopers.settings.presentation.event

sealed class SettingsUiEvent {
    object LogoutSuccess : SettingsUiEvent()
    data class ShowError(val message: String) : SettingsUiEvent()
    object Loading : SettingsUiEvent()
    object Idle : SettingsUiEvent()
}
