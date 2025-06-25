package com.lyecdevelopers.ugandaemrmobile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lyecdevelopers.auth.presentation.AuthScreen
import com.lyecdevelopers.core.data.preference.PreferenceManager
import com.lyecdevelopers.core.data.remote.interceptor.AuthInterceptor
import com.lyecdevelopers.core.ui.components.SplashScreen
import com.lyecdevelopers.core.ui.theme.UgandaEMRMobileTheme
import com.lyecdevelopers.core_navigation.navigation.Destinations
import com.lyecdevelopers.main.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var authInterceptor: AuthInterceptor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UgandaEMRMobileTheme {
                val navController = rememberNavController()
                val navBarNavController = rememberNavController()
                val isLoggedIn by preferenceManager.isLoggedIn().collectAsState(initial = false)
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    val username = preferenceManager.getUsername().firstOrNull()
                    val password = preferenceManager.getPassword().firstOrNull()

                    if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
                        authInterceptor.updateCredentials(username, password)
                    }
                }

                if (showSplash) {
                    SplashScreen(
                        isLoggedIn = isLoggedIn,
                        onSplashFinished = {
                            showSplash = false
                        },
                    )
                } else {
                    // Navigate reactively on login status changes
                    LaunchedEffect(isLoggedIn) {
                        if (isLoggedIn) {
                            navController.navigate(Destinations.MAIN) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate(Destinations.AUTH) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }

                    NavHost(navController = navController, startDestination = Destinations.AUTH) {
                        composable(Destinations.SPLASH) {
                            SplashScreen(
                                isLoggedIn = isLoggedIn,
                                onSplashFinished = {
                                    showSplash = false
                                },
                            )
                        }

                        composable(Destinations.AUTH) {
                            AuthScreen(onLoginSuccess = {
                                navController.navigate(Destinations.MAIN) {
                                    popUpTo(Destinations.AUTH) { inclusive = true }
                                }
                            })
                        }

                        composable(Destinations.MAIN) {
                            MainScreen(
                                fragmentManager = supportFragmentManager,
                                navController = navBarNavController
                            )
                        }
                    }
                }
            }
        }
    }
}








