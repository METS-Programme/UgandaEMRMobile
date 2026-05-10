package com.lyecdevelopers.scanner.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.lyecdevelopers.core_navigation.navigation.Destinations
import com.lyecdevelopers.scanner.QrScannerScreen

private const val TAG = "QrScannerNavigation"

fun NavGraphBuilder.qrScannerGraph(
    onUrlScanned: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(Destinations.QR_SCANNER) {
        Log.d(TAG, "[NAV] QrScannerScreen composable called")
        QrScannerScreen(
            onUrlDetected = { url ->
                Log.d(TAG, "[NAV] URL scanned callback triggered: $url")
                onUrlScanned(url)
            },
            onCancel = {
                Log.d(TAG, "[NAV] Cancel callback triggered, navigating back")
                onNavigateBack()
            }
        )
    }
}

fun NavController.navigateToQrScanner() {
    Log.d(TAG, "[NAV] Navigating to QR Scanner screen: ${Destinations.QR_SCANNER}")
    this.navigate(Destinations.QR_SCANNER)
}
