package com.lyecdevelopers.auth.presentation.event

sealed class LoginUIEvent {
    data class ShowError(val message: String) : LoginUIEvent()
    data class ShowSuccess(val message: String) : LoginUIEvent()
}