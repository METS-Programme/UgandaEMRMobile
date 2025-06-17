package com.lyecdevelopers.core.ui.event

sealed class UiEvent {
    data class ShowDialog(
        val title: String,
        val message: String,
        val confirmText: String = "OK",
        val onConfirm: (() -> Unit)? = null,
        val autoDismissAfterMillis: Long? = null,
    ) : UiEvent()

    object DismissDialog : UiEvent()
}

