package com.lyecdevelopers.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lyecdevelopers.core.model.BottomNavItem
import com.lyecdevelopers.core.ui.components.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    fragmentManager: FragmentManager,
    navController: NavHostController,
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: ""

    val topBarTitle = when {
        currentRoute.startsWith(BottomNavItem.Worklist.route) -> BottomNavItem.Worklist.label
        currentRoute.startsWith(BottomNavItem.Sync.route) -> BottomNavItem.Sync.label
        currentRoute.startsWith(BottomNavItem.Settings.route) -> BottomNavItem.Settings.label
        else -> ""
    }

    var menuExpanded by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            if (shouldShowBottomBar(currentRoute)) {
                TopAppBar(title = { Text(topBarTitle) }, actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        // Route-based menus here
                    }
                })
            }
        },
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        MainNavGraph(
            fragmentManager = fragmentManager,
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

fun shouldShowBottomBar(route: String): Boolean {
    val bottomRoutes = listOf(
        BottomNavItem.Worklist.route,
        BottomNavItem.Sync.route,
        BottomNavItem.Settings.route,
    )

    return bottomRoutes.any { route.startsWith(it) }
}






