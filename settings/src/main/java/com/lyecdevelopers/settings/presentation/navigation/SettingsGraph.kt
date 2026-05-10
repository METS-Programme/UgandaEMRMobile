package com.lyecdevelopers.settings.presentation.navigation

import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lyecdevelopers.core.model.BottomNavItem
import com.lyecdevelopers.scanner.navigation.qrScannerGraph
import com.lyecdevelopers.settings.presentation.SettingsScreen

private const val TAG = "SettingsGraph"

fun NavGraphBuilder.settingsGraph(navController: NavController) {
    composable(BottomNavItem.Settings.route) {
        SettingsScreen(navController = navController)
    }

    qrScannerGraph(
        onUrlScanned = { url ->
            Log.d(TAG, "[QR_RESULT] URL scanned: $url")

            // Navigate back to settings with the scanned URL
            val previousEntry = navController.previousBackStackEntry
            if (previousEntry != null) {
                Log.d(TAG, "[QR_RESULT] Setting scanned_url in previous back stack entry")
                previousEntry.savedStateHandle?.set("scanned_url", url)
                Log.d(TAG, "[QR_RESULT] Popping back stack")
                navController.popBackStack()
            } else {
                Log.e(TAG, "[QR_RESULT] ERROR: No previous back stack entry to return to!")
            }
        },
        onNavigateBack = {
            Log.d(TAG, "[QR_RESULT] User cancelled, popping back stack")
            navController.popBackStack()
        }
    )
}
