package com.lyecdevelopers.ugandaemrmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lyecdevelopers.auth.presentation.AuthScreen
import com.lyecdevelopers.core.data.preference.PreferenceManager
import com.lyecdevelopers.core.ui.theme.UgandaEMRMobileTheme
import com.lyecdevelopers.core_navigation.navigation.Destinations
import com.lyecdevelopers.main.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            UgandaEMRMobileTheme {
                val navController = rememberNavController()
                val navBarNavController = rememberNavController()
                val isLoggedIn by preferenceManager.isLoggedIn().collectAsState(initial = false)
                val startDestination = if (isLoggedIn) Destinations.MAIN else Destinations.AUTH

                NavHost(navController = navController, startDestination = startDestination) {
                    composable(Destinations.AUTH) {
                        AuthScreen(onLoginSuccess = {
                            navController.navigate(Destinations.MAIN) {
                                popUpTo(Destinations.AUTH) { inclusive = true }
                            }
                        })
                    }
                    composable(Destinations.MAIN) {
                        MainScreen(navController = navBarNavController)
                    }
                }
            }


        }
    }
}


