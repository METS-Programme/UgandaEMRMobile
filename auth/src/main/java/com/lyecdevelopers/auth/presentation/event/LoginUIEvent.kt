package com.lyecdevelopers.auth.presentation.event


sealed class LoginUIEvent {
    data class ShowSuccess(val message: String) : LoginUIEvent()
    data class ShowError(val message: String) : LoginUIEvent()

    data class ShowGlobalDialog(
        val title: String,
        val message: String,
        val confirmText: String = "OK",
        val onConfirm: (() -> Unit)? = null,
        val autoDismissAfterMillis: Long? = null,
    ) : LoginUIEvent()

}


