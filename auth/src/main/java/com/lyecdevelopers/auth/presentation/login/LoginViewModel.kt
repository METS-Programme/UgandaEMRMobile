package com.lyecdevelopers.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyecdevelopers.auth.domain.model.Result
import com.lyecdevelopers.auth.domain.usecase.LoginUseCase
import com.lyecdevelopers.auth.domain.usecase.LogoutUseCase
import com.lyecdevelopers.auth.presentation.event.LoginEvent
import com.lyecdevelopers.auth.presentation.event.LoginUIEvent
import com.lyecdevelopers.auth.presentation.state.LoginUIState
import com.lyecdevelopers.core.common.scheduler.SchedulerProvider
import com.lyecdevelopers.core.data.preference.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val schedulerProvider: SchedulerProvider,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUIState())
    val uiState: StateFlow<LoginUIState> = _uiState

    private val _uiEvent = MutableSharedFlow<LoginUIEvent>()
    val uiEvent: SharedFlow<LoginUIEvent> = _uiEvent

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.Login -> {
                _uiState.update { it.copy(username = event.username, password = event.password) }
            }
            is LoginEvent.Submit -> {
                _uiState.update { it.copy(hasSubmitted = true) }
                login()
            }
            is LoginEvent.logout -> TODO()
        }
    }

    private fun login() {
        val currentState = _uiState.value
        val username = currentState.username.trim()
        val password = currentState.password

        // Basic empty check
        if (username.isBlank() || password.isBlank()) {
            emitError("Username or password can't be empty")
            return
        }

        // Password strength checks
        if (password.length < 6) {
            emitError("Password must be at least 6 characters")
            return
        }

        if (password.lowercase() in listOf("123456", "password", "admin")) {
            emitError("Please choose a stronger password")
            return
        }

        // Launch login process
        viewModelScope.launch(schedulerProvider.io) {
            loginUseCase(username, password).collect { result ->
                withContext(schedulerProvider.main) {
                    when (result) {
                        is Result.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }

                        is Result.Success -> {
                            _uiState.update {
                                it.copy(isLoading = false, isLoginSuccessful = true)
                            }
                            saveLogin()
                            _uiEvent.emit(LoginUIEvent.ShowSuccess("Login successful"))
                        }

                        is Result.Error -> {
                            _uiState.update { it.copy(isLoading = false) }
                            _uiEvent.emit(LoginUIEvent.ShowError(result.message))
                        }
                    }
                }

            }
        }
    }


    fun logout() {
        viewModelScope.launch(schedulerProvider.io) {
            logoutUseCase(username = "", password = "").collect { result ->
                withContext(schedulerProvider.main) {
                    when (result) {
                        is Result.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }

                        is Result.Success -> {
                            preferenceManager.clear()
                            _uiState.update { it.copy(isLoading = false) }
                            _uiEvent.emit(LoginUIEvent.LoggedOut)
                        }

                        is Result.Error -> {
                            _uiState.update { it.copy(isLoading = false) }
                            _uiEvent.emit(LoginUIEvent.ShowError(result.message))
                        }
                    }
                }
            }
        }
    }


    private fun emitError(message: String) {
        viewModelScope.launch(schedulerProvider.main) {
            _uiEvent.emit(LoginUIEvent.ShowError(message))
        }
    }


    private fun saveLogin() {
        viewModelScope.launch(schedulerProvider.io) {
            preferenceManager.setIsLoggedIn(true)
        }
    }


}