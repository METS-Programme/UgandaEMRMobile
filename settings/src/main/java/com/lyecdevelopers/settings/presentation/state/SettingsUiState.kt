package com.lyecdevelopers.settings.presentation.state

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val isSyncing: Boolean = false,
    val username: String = "",
    val serverUrl: String = "",
    val syncIntervalInMinutes: Int = 15,
    val versionName: String = "v1.0.0",
)
