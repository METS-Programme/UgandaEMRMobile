package com.lyecdevelopers.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyecdevelopers.core.ui.event.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun showDialog(
        title: String,
        message: String,
        confirmText: String = "OK",
        onConfirm: (() -> Unit)? = null,
        autoDismissAfterMillis: Long? = null,
    ) {
        viewModelScope.launch {
            _uiEvent.emit(
                UiEvent.ShowDialog(
                    title, message, confirmText, onConfirm, autoDismissAfterMillis
                )
            )
        }
    }

    fun dismissDialog() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.DismissDialog)
        }
    }
}

