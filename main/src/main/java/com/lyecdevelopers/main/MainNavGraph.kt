package com.lyecdevelopers.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.lyecdevelopers.core.model.BottomNavItem
import com.lyecdevelopers.settings.presentation.navigation.settingsGraph
import com.lyecdevelopers.sync.presentation.navigation.syncGraph
import com.lyecdevelopers.worklist.presentation.navigation.worklistGraph


@Composable
fun MainNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Worklist.route,
        modifier = modifier
    ) {
        worklistGraph(navController)
        syncGraph(navController)
        settingsGraph(navController)
    }
}


