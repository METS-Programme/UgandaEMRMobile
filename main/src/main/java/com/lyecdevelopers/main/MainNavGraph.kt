package com.lyecdevelopers.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lyecdevelopers.core.model.BottomNavItem
import com.lyecdevelopers.settings.presentation.SettingsScreen
import com.lyecdevelopers.sync.presentation.SyncScreen
import com.lyecdevelopers.worklist.presentation.worklist.WorklistScreen

@Composable
fun MainNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Worklist.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Worklist.route) {
            WorklistScreen(
                onSyncClick = {
                    navController.navigate(BottomNavItem.Sync.route)
                },
                onSettingsClick = {
                    navController.navigate(BottomNavItem.Settings.route)
                }
            )
        }

        composable(BottomNavItem.Sync.route) { SyncScreen(onBack = { navController.popBackStack() }) }
        composable(BottomNavItem.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
