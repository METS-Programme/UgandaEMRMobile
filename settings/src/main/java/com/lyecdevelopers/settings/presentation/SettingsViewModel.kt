package com.lyecdevelopers.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyecdevelopers.core.common.scheduler.SchedulerProvider
import com.lyecdevelopers.core.data.preference.PreferenceManager
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.settings.domain.usecase.SettingsUseCase
import com.lyecdevelopers.settings.presentation.event.SettingsUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsUseCase: SettingsUseCase,
    private val preferenceManager: PreferenceManager,
    private val schedulerProvider: SchedulerProvider,
) : ViewModel() {

    private val _logoutSuccess = MutableStateFlow(false)
    val logoutSuccess: StateFlow<Boolean> = _logoutSuccess.asStateFlow()

    // user details
    private val _userName = MutableStateFlow<String>("")
    val username: StateFlow<String> = _userName.asStateFlow()

    // general
    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent: SharedFlow<SettingsUiEvent> = _uiEvent

    init {
        accountDetails()
    }

    fun logout() {
        viewModelScope.launch(schedulerProvider.io) {
            val username = preferenceManager.getUsername().firstOrNull() ?: ""
            val password = preferenceManager.getPassword().firstOrNull() ?: ""
            settingsUseCase.logout(
                username, password
            ).collect { result ->
                withContext(schedulerProvider.main) {
                    when (result) {
                        is Result.Error -> {
                            _uiEvent.emit(
                                SettingsUiEvent.ShowError(
                                    result.message
                                )
                            )
                        }

                        is Result.Loading -> {
                            _uiEvent.emit(SettingsUiEvent.Loading)
                        }

                        is Result.Success<*> -> {
                            preferenceManager.clear()
                            _uiEvent.emit(SettingsUiEvent.LogoutSuccess)
                        }
                    }
                }
            }
        }
    }


    private fun accountDetails() {
        viewModelScope.launch(schedulerProvider.io) {
            _userName.value = preferenceManager.getUsername().firstOrNull() ?: ""
        }
    }

}