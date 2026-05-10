package com.lyecdevelopers.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyecdevelopers.core.utils.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrScannerViewModel @Inject constructor() : ViewModel() {

    private val _scannedUrl = MutableStateFlow<String?>(null)
    val scannedUrl: StateFlow<String?> = _scannedUrl.asStateFlow()

    fun onUrlDetected(url: String) {
        viewModelScope.launch {
            AppLogger.d("QR Code detected: $url")
            _scannedUrl.value = url
        }
    }

    fun clearUrl() {
        _scannedUrl.value = null
    }
}
