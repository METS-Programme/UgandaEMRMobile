package com.lyecdevelopers.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyecdevelopers.auth.domain.usecase.LoginUseCase
import com.lyecdevelopers.auth.presentation.event.LoginEvent
import com.lyecdevelopers.auth.presentation.event.LoginUIEvent
import com.lyecdevelopers.auth.presentation.state.LoginUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
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
                login()
            }
        }
    }

    private fun login() {
        val currentState = _uiState.value

        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            viewModelScope.launch {
                _uiEvent.emit(LoginUIEvent.ShowError("Username or password can't be empty"))
            }
            return
        }

        viewModelScope.launch {
            loginUseCase(currentState.username, currentState.password).collect { result ->
                when (result) {
//                    is Result.Loading -> {
//                        _uiState.update { it.copy(isLoading = true) }
//                    }
//                    is Result.Success -> {
//                        _uiState.update {
//                            it.copy(isLoading = false, isLoginSuccessful = true)
//                        }
//                        _uiEvent.emit(LoginUIEvent.ShowSuccess("Login successful"))
//                    }
//                    is Result.Error -> {
//                        _uiState.update { it.copy(isLoading = false) }
//                        _uiEvent.emit(LoginUIEvent.ShowError(result.message))
//                    }
                }
            }
        }
    }
}

