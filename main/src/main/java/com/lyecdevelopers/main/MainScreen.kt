package com.lyecdevelopers.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lyecdevelopers.core.model.BottomNavItem
import com.lyecdevelopers.core.ui.components.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route


    val topBarTitle = when (currentRoute) {
        BottomNavItem.Worklist.route -> BottomNavItem.Worklist.label
        BottomNavItem.Sync.route -> BottomNavItem.Sync.label
        BottomNavItem.Settings.route -> BottomNavItem.Settings.label
        else -> ""
    }

    // State to control menu expanded/collapsed
    var menuExpanded by remember { mutableStateOf(false) }

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
//                                        viewModel.onLogoutClicked()
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



