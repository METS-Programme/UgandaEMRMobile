package com.lyecdevelopers.scanner.camera

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * QR code analyzer using Google ML Kit
 * Based on Quickie library approach for reliable QR scanning
 */
class QrCameraAnalyzer(
    private val onSuccess: ((Barcode) -> Unit),
    private val onFailure: ((Exception) -> Unit)? = null,
    private val onPassCompleted: ((Boolean) -> Unit)? = null,
    private val onHapticFeedback: (() -> Unit)? = null
) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "QrScanner"
        private const val ERROR_COOLDOWN_MS = 1000L
    }

    private val scanner: BarcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC, Barcode.FORMAT_DATA_MATRIX)
            .build()
        try {
            BarcodeScanning.getClient(options)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ML Kit scanner", e)
            onFailure?.invoke(e)
            throw e
        }
    }

    private var failureOccurred = false
    private var failureTimestamp = 0L
    private var frameCount = 0

    init {
        Log.d(TAG, "QrCameraAnalyzer initialized with ML Kit barcode scanner")
    }

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        // Log every 30th frame for diagnostics
        frameCount++
        if (frameCount % 30 == 0) {
            Log.d(TAG, "Analyzing frame #$frameCount: width=${imageProxy.width}, height=${imageProxy.height}, " +
                    "rotation=${imageProxy.imageInfo.rotationDegrees}, format=${mediaImage?.format}")
        }

        // Skip analysis if no media image or in error cooldown period
        if (mediaImage == null || (failureOccurred && System.currentTimeMillis() - failureTimestamp < ERROR_COOLDOWN_MS)) {
            imageProxy.close()
            return
        }

        failureOccurred = false

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isEmpty()) {
                    Log.d(TAG, "Scan complete, no barcodes found")
                } else {
                    Log.d(TAG, "Scan complete, found ${barcodes.size} barcode(s)")
                    barcodes.forEach { barcode ->
                        Log.d(TAG, "Barcode format: ${barcode.format}, valueType: ${barcode.valueType}, rawValue: ${barcode.rawValue}")
                    }
                    // Take first valid barcode (Quickie approach)
                    barcodes.firstNotNullOfOrNull { it }?.let { barcode ->
                        barcode.rawValue?.let { value ->
                            Log.i(TAG, "QR Code detected: $value")
                            onHapticFeedback?.invoke()
                            onSuccess(barcode)
                        } ?: Log.w(TAG, "Barcode found but rawValue is null. Format: ${barcode.format}, valueType: ${barcode.valueType}")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Barcode detection failed", e)
                failureOccurred = true
                failureTimestamp = System.currentTimeMillis()
                onFailure?.invoke(e)
            }
            .addOnCompleteListener {
                onPassCompleted?.invoke(failureOccurred)
                imageProxy.close()
            }
    }

    /**
     * Stop scanning by clearing the scanner resources
     */
    fun close() {
        try {
            scanner.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing scanner", e)
        }
    }
}
