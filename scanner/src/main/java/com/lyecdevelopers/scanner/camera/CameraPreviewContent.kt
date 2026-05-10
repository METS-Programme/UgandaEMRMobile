package com.lyecdevelopers.scanner.camera

import android.content.Context
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import androidx.compose.ui.Alignment
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.common.Barcode
import com.lyecdevelopers.scanner.QrScannerOverlay
import com.lyecdevelopers.scanner.config.ScannerConfig

private const val TAG = "CameraPreviewContent"

@Composable
fun CameraPreviewContent(
    modifier: Modifier = Modifier,
    onUrlDetected: (String) -> Unit,
    config: com.lyecdevelopers.scanner.config.ScannerConfig = com.lyecdevelopers.scanner.config.ScannerConfig.DEFAULT,
    onCameraClick: (() -> Unit)? = null,
    onHapticFeedback: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Track executor to properly shut it down
    var executor by remember { mutableStateOf<ExecutorService?>(null) }
    // Track analyzer for cleanup
    var analyzer by remember { mutableStateOf<QrCameraAnalyzer?>(null) }
    // Track camera for torch control
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    // Track torch state
    var torchState by remember { mutableStateOf(false) }
    // Track front camera state
    var isFrontCamera by remember { mutableStateOf(config.useFrontCamera) }

    // Cleanup on disposal
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            analyzer?.close()
            executor?.shutdown()
        }
    }

    // Safely get display rotation from WindowManager
    fun getDisplayRotation(): Int {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.rotation
        } catch (e: Exception) {
            Log.e(TAG, "[ERROR] Failed to get display rotation, using default", e)
            Surface.ROTATION_0
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                }
            },
            update = { previewView ->
                val rotation = getDisplayRotation()

                // Create new analyzer on each update
                val newAnalyzer = QrCameraAnalyzer(
                    onSuccess = { barcode ->
                        // Clear analyzer on success (Quickie approach)
                        barcode.rawValue?.let { onUrlDetected(it) }
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Scan failed", e)
                    },
                    onPassCompleted = { failureOccurred ->
                        // Optional: Show loading state or error indicator
                    },
                    onHapticFeedback = onHapticFeedback
                )
                analyzer = newAnalyzer

                // Get camera provider
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    // Preview use case
                    val preview = Preview.Builder()
                        .setTargetRotation(rotation)
                        .build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    // Image analysis - let CameraX choose optimal resolution
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetRotation(rotation)
                        .build()

                    // Single thread executor for analysis
                    val newExecutor = Executors.newSingleThreadExecutor { r ->
                        Thread(r, "QRScanner").apply { isDaemon = true }
                    }
                    executor = newExecutor
                    imageAnalysis.setAnalyzer(newExecutor, newAnalyzer)

                    // Camera selector based on config
                    val cameraSelector = if (isFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    try {
                        cameraProvider.unbindAll()
                        val boundCamera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        camera = boundCamera

                        // Observe torch state
                        if (config.showTorchToggle && boundCamera.cameraInfo.hasFlashUnit()) {
                            boundCamera.cameraInfo.torchState.observe(lifecycleOwner) { state ->
                                torchState = state == TorchState.ON
                            }
                        }
                    } catch (exc: Exception) {
                        Log.e(TAG, "Camera binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(context))
            },
            modifier = Modifier.fillMaxSize()
        )

        // Scanner overlay with frame and instructions
        QrScannerOverlay(
            instructions = config.overlayInstructions,
            modifier = Modifier.fillMaxSize()
        )

        // Torch toggle button (bottom right)
        if (config.showTorchToggle) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                IconButton(
                    onClick = {
                        camera?.cameraControl?.enableTorch(!torchState)
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (torchState) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = if (torchState) "Flash On" else "Flash Off",
                        tint = Color.White
                    )
                }
            }
        }

        // Camera switch button (bottom left)
        if (onCameraClick != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                IconButton(
                    onClick = {
                        isFrontCamera = !isFrontCamera
                        onCameraClick()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
