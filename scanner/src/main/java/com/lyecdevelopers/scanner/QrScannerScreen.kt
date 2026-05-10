package com.lyecdevelopers.scanner

import android.Manifest
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.lyecdevelopers.scanner.camera.CameraPreviewContent
import com.lyecdevelopers.scanner.config.ScannerConfig

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScannerScreen(
    onUrlDetected: (String) -> Unit,
    onCancel: () -> Unit,
    viewModel: QrScannerViewModel = hiltViewModel(),
    config: ScannerConfig = ScannerConfig.DEFAULT
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val scannedUrl by viewModel.scannedUrl.collectAsState()
    var hasHandledUrl by remember { mutableStateOf(false) }
    val view = LocalView.current
    var forceRecompose by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(scannedUrl) {
        if (scannedUrl != null && !hasHandledUrl) {
            hasHandledUrl = true
            // Haptic feedback on successful scan
            if (config.hapticFeedback) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                } else {
                    @Suppress("DEPRECATION")
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
            }
            scannedUrl?.let { onUrlDetected(it) }
        }
    }

    fun performHapticFeedback() {
        if (config.hapticFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black),
                contentAlignment = Alignment.TopStart
            ) {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancel",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        when (val status = cameraPermissionState.status) {
            is com.google.accompanist.permissions.PermissionStatus.Granted -> {
                CameraPreviewContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onUrlDetected = { url ->
                        viewModel.onUrlDetected(url)
                    },
                    config = config,
                    onCameraClick = {
                        // Force recompose to rebind camera with different lens facing
                        forceRecompose = !forceRecompose
                    },
                    onHapticFeedback = {
                        performHapticFeedback()
                    }
                )
            }
            is com.google.accompanist.permissions.PermissionStatus.Denied -> {
                PermissionDeniedContent(
                    onRequestPermission = {
                        cameraPermissionState.launchPermissionRequest()
                    },
                    onCancel = onCancel,
                    showRationale = status.shouldShowRationale
                )
            }
        }
    }
}

@Composable
fun PermissionDeniedContent(
    onRequestPermission: () -> Unit,
    onCancel: () -> Unit,
    showRationale: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (showRationale) {
                "Camera permission is required to scan QR codes."
            } else {
                "Camera permission denied. Please enable it in app settings."
            },
            textAlign = TextAlign.Center
        )
    }
}
