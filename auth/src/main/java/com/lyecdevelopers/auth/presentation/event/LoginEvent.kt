package com.lyecdevelopers.auth.presentation.event

sealed  class LoginEvent {
    data class Login(val username: String, val password : String) : LoginEvent()
    data class SetServerUrl(val url: String) : LoginEvent()
    object Submit : LoginEvent()
}