package com.lyecdevelopers.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lyecdevelopers.auth.presentation.event.LoginEvent
import com.lyecdevelopers.auth.presentation.event.LoginUIEvent
import com.lyecdevelopers.auth.presentation.login.LoginViewModel
import com.lyecdevelopers.core.model.BottomNavItem
import com.lyecdevelopers.core.ui.components.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController()
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val topBarTitle = when (currentRoute) {
        BottomNavItem.Worklist.route -> BottomNavItem.Worklist.label
        BottomNavItem.Sync.route -> BottomNavItem.Sync.label
        BottomNavItem.Settings.route -> BottomNavItem.Settings.label
        else -> ""
    }

    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            if (event is LoginUIEvent.LoggedOut) {
                navController.navigate("auth") {
                    popUpTo(0) // Clear backstack
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = topBarTitle) },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        when (currentRoute) {
                            BottomNavItem.Worklist.route -> {
                                DropdownMenuItem(
                                    text = { Text("Sync") },
                                    onClick = {
                                        menuExpanded = false
                                        navController.navigate(BottomNavItem.Sync.route)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = {
                                        menuExpanded = false
                                        navController.navigate(BottomNavItem.Settings.route)
                                    }
                                )
                            }

                            BottomNavItem.Settings.route -> {
                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.onEvent(LoginEvent.Logout)
                                    }
                                )
                            }

                            BottomNavItem.Sync.route -> {
                                DropdownMenuItem(
                                    text = { Text("Back") },
                                    onClick = {
                                        menuExpanded = false
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        MainNavGraph(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}




