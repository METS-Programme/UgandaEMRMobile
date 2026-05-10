package com.lyecdevelopers.scanner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QrScannerOverlay(
    instructions: String? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Semi-transparent background with transparent scanning area
        ScannerMask()

        // Top instruction area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                instructions?.let { text ->
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                    )
                } ?: Text(
                    text = "Position QR code within the frame",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }

        // Corner accents for the scanning frame
        ScanningFrameCorners()
    }
}

@Composable
private fun ScannerMask() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val frameSize = size.minDimension * 0.65f
        val frameLeft = (width - frameSize) / 2
        val frameTop = (height - frameSize) / 2

        // Draw semi-transparent background outside the scanning frame
        drawRect(
            color = Color.Black.copy(alpha = 0.7f),
            size = Size(width, frameTop) // Top
        )
        drawRect(
            color = Color.Black.copy(alpha = 0.7f),
            topLeft = Offset(0f, frameTop + frameSize),
            size = Size(width, height - frameTop - frameSize) // Bottom
        )
        drawRect(
            color = Color.Black.copy(alpha = 0.7f),
            topLeft = Offset(0f, frameTop),
            size = Size(frameLeft, frameSize) // Left
        )
        drawRect(
            color = Color.Black.copy(alpha = 0.7f),
            topLeft = Offset(frameLeft + frameSize, frameTop),
            size = Size(width - frameLeft - frameSize, frameSize) // Right
        )
    }
}

@Composable
private fun ScanningFrameCorners() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val frameSize = size.minDimension * 0.65f
        val frameLeft = (width - frameSize) / 2
        val frameTop = (height - frameSize) / 2
        val cornerLength = frameSize * 0.15f
        val cornerThickness = 4.dp.toPx()

        // Corner color (can be made customizable)
        val cornerColor = Color.White

        // Draw 4 corners
        // Top-left
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft, frameTop),
            end = Offset(frameLeft + cornerLength, frameTop),
            strokeWidth = cornerThickness,
            cap = Stroke.DefaultCap
        )
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft, frameTop),
            end = Offset(frameLeft, frameTop + cornerLength),
            strokeWidth = cornerThickness,
            cap = Stroke.DefaultCap
        )

        // Top-right
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft + frameSize - cornerLength, frameTop),
            end = Offset(frameLeft + frameSize, frameTop),
            strokeWidth = cornerThickness,
            cap = Stroke.DefaultCap
        )
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft + frameSize, frameTop),
            end = Offset(frameLeft + frameSize, frameTop + cornerLength),
            strokeWidth = cornerThickness,
            cap = Stroke.DefaultCap
        )

        // Bottom-left
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft, frameTop + frameSize - cornerLength),
            end = Offset(frameLeft, frameTop + frameSize),
            strokeWidth = cornerThickness,
            cap = Stroke.DefaultCap
        )
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft, frameTop + frameSize),
            end = Offset(frameLeft + cornerLength, frameTop + frameSize),
            strokeWidth = cornerThickness,
            cap = Stroke.DefaultCap
        )

        // Bottom-right
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft + frameSize - cornerLength, frameTop + frameSize),
            end = Offset(frameLeft + frameSize, frameTop + frameSize),
            strokeWidth = cornerThickness,
            cap = Stroke.DefaultCap
        )
        drawLine(
            color = cornerColor,
            start = Offset(frameLeft + frameSize, frameTop + frameSize - cornerLength),
            end = Offset(frameLeft + frameSize, frameTop + frameSize),
            strokeWidth = cornerThickness,
            cap = Stroke.DefaultCap
        )
    }
}
