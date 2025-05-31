package com.lyecdevelopers.ugandaemrmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lyecdevelopers.auth.presentation.AuthScreen
import com.lyecdevelopers.core.ui.theme.UgandaEMRMobileTheme
import com.lyecdevelopers.core_navigation.navigation.Destinations
import com.lyecdevelopers.main.MainScreen
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
                            navController.navigate(Destinations.MAIN) {
                                popUpTo(Destinations.AUTH) { inclusive = true }
                            }
                        })
                    }
                    composable(Destinations.MAIN) {
                        MainScreen()
                    }
                }


            }
    }
}}

