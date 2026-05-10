package com.lyecdevelopers.scanner.config

/**
 * Configuration options for the QR scanner
 */
data class ScannerConfig(
    val showTorchToggle: Boolean = true,
    val useFrontCamera: Boolean = false,
    val hapticFeedback: Boolean = true,
    val overlayInstructions: String? = null,
    val keepScreenOn: Boolean = false
) {
    companion object {
        val DEFAULT = ScannerConfig()
    }

    class Builder {
        private var showTorchToggle: Boolean = true
        private var useFrontCamera: Boolean = false
        private var hapticFeedback: Boolean = true
        private var overlayInstructions: String? = null
        private var keepScreenOn: Boolean = false

        fun setShowTorchToggle(show: Boolean) = apply { showTorchToggle = show }
        fun setUseFrontCamera(use: Boolean) = apply { useFrontCamera = use }
        fun setHapticFeedback(enable: Boolean) = apply { hapticFeedback = enable }
        fun setOverlayInstructions(instructions: String) = apply { overlayInstructions = instructions }
        fun setKeepScreenOn(keep: Boolean) = apply { keepScreenOn = keep }

        fun build() = ScannerConfig(
            showTorchToggle = showTorchToggle,
            useFrontCamera = useFrontCamera,
            hapticFeedback = hapticFeedback,
            overlayInstructions = overlayInstructions,
            keepScreenOn = keepScreenOn
        )
    }
}
