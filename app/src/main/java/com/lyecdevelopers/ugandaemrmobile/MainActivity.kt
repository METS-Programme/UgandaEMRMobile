package com.lyecdevelopers.ugandaemrmobile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lyecdevelopers.auth.presentation.AuthScreen
import com.lyecdevelopers.core.data.preference.PreferenceManager
import com.lyecdevelopers.core.ui.SharedViewModel
import com.lyecdevelopers.core.ui.components.SplashScreen
import com.lyecdevelopers.core.ui.event.UiEvent
import com.lyecdevelopers.core.ui.theme.UgandaEMRMobileTheme
import com.lyecdevelopers.core_navigation.navigation.Destinations
import com.lyecdevelopers.main.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UgandaEMRMobileTheme {
                val navController = rememberNavController()
                val navBarNavController = rememberNavController()
                val isLoggedIn by preferenceManager.isLoggedIn().collectAsState(initial = false)
                val sharedViewModel: SharedViewModel = hiltViewModel()

                var showSplash by remember { mutableStateOf(true) }

                // dialog state
                var showDialog by remember { mutableStateOf(false) }
                var dialogTitle by remember { mutableStateOf("") }
                var dialogMessage by remember { mutableStateOf("") }
                var confirmText by remember { mutableStateOf("OK") }
                val onConfirmAction = remember { mutableStateOf<(() -> Unit)?>(null) }

                LaunchedEffect(Unit) {
                    sharedViewModel.uiEvent.collect { event ->
                        when (event) {
                            is UiEvent.ShowDialog -> {
                                dialogTitle = event.title
                                dialogMessage = event.message
                                confirmText = event.confirmText
                                onConfirmAction.value = event.onConfirm
                                showDialog = true

                                event.autoDismissAfterMillis?.let { delayMillis ->
                                    delay(delayMillis)
                                    showDialog = false
                                    event.onConfirm?.invoke()
                                }
                            }

                            is UiEvent.DismissDialog -> {
                                showDialog = false
                            }

                            else -> {}
                        }
                    }
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(dialogTitle) },
                        text = { Text(dialogMessage) },
                        confirmButton = {
                            TextButton(onClick = {
                                showDialog = false
                                onConfirmAction.value?.invoke()
                            }) {
                                Text(confirmText)
                            }
                        })
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








