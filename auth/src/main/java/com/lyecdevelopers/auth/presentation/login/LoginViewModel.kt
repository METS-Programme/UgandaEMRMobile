package com.lyecdevelopers.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyecdevelopers.auth.domain.usecase.LoginUseCase
import com.lyecdevelopers.auth.presentation.event.LoginEvent
import com.lyecdevelopers.auth.presentation.event.LoginUIEvent
import com.lyecdevelopers.auth.presentation.state.LoginUIState
import com.lyecdevelopers.core.common.scheduler.SchedulerProvider
import com.lyecdevelopers.core.data.preference.PreferenceManager
import com.lyecdevelopers.core.model.Result
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

        }
    }

    private fun login() {
        val currentState = _uiState.value
        val username = currentState.username.trim()
        val password = currentState.password

        // Basic validations
        when {
            username.isBlank() || password.isBlank() -> {
                emitErrorDialog("Login", "Username or password can't be empty")
                return
            }

            password.length < 6 -> {
                emitErrorDialog("Login", "Password must be at least 6 characters")
                return
            }

            password.lowercase() in listOf("123456", "password", "admin") -> {
                emitErrorDialog("Login", "Please choose a stronger password")
                return
            }
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
                            saveLogin(username, password)
                            _uiEvent.emit(LoginUIEvent.ShowSuccess("Login successful"))
                        }

                        is Result.Error -> {
                            _uiState.update { it.copy(isLoading = false) }
                            emitErrorDialog("Login Failed", result.message)
                        }
                    }
                }
            }
        }
    }


    private fun emitErrorDialog(title: String, message: String) {
        viewModelScope.launch(schedulerProvider.main) {
            _uiEvent.emit(
                LoginUIEvent.ShowGlobalDialog(
                    title = title, message = message
                )
            )
        }
    }

    private fun saveLogin(username: String, password: String) {
        viewModelScope.launch(schedulerProvider.io) {
            preferenceManager.saveUsername(username)
            preferenceManager.savePassword(password)
            preferenceManager.setIsLoggedIn(true)
        }
    }
}
