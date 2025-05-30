package com.lyecdevelopers.ugandaemrmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lyecdevelopers.auth.presentation.AuthScreen
import com.lyecdevelopers.core.navigation.Destinations
import com.lyecdevelopers.settings.presentation.SettingsScreen
import com.lyecdevelopers.sync.presentation.SyncScreen
import com.lyecdevelopers.core.ui.theme.UgandaEMRMobileTheme
import com.lyecdevelopers.worklist.presentation.worklist.WorklistScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UgandaEMRMobileTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Destinations.AUTH) {
                    composable(Destinations.AUTH) {
                        AuthScreen(onLoginSuccess = {
                            navController.navigate(Destinations.WORKLIST) {
                                popUpTo(Destinations.AUTH) { inclusive = true }
                            }
                        })
                    }
                    composable(Destinations.WORKLIST) {
                        WorklistScreen(
                            onSyncClick = { navController.navigate(Destinations.SYNC) },
                            onSettingsClick = { navController.navigate(Destinations.SETTINGS) }
                        )
                    }
                    composable(Destinations.SYNC) {
                        SyncScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Destinations.SETTINGS) {
                        SettingsScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

