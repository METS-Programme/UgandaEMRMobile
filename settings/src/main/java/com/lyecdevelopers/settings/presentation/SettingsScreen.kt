package com.lyecdevelopers.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryEditable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lyecdevelopers.core.ui.components.BaseScreen
import com.lyecdevelopers.core.ui.components.SettingsItem
import com.lyecdevelopers.settings.presentation.event.SettingsEvent

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    var showServerDialog by remember { mutableStateOf(false) }
    val state by viewModel.uiState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }

    BaseScreen(
        uiEventFlow = viewModel.uiEvent, isLoading = isLoading, showLoading = { isLoading = it }) {
        Scaffold { padding ->
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    SettingsSection(title = "Account") {
                        SettingsItem(
                            title = "Username",
                            subtitle = state.username,
                            onClick = { /* Optional: open editable dialog */ })
                        SettingsItem(
                            title = "Facility", subtitle = "Kampala Health Center", onClick = {})
                    }
                }

                item {
                    SettingsSection(title = "Preferences") {
                        SettingsItem(
                            title = "Language", subtitle = "English", onClick = { /* TODO */ })
                        SettingsItem(
                            title = "Dark Mode",
                            subtitle = if (state.isDarkMode) "On" else "Off",
                            onClick = {
                                viewModel.onEvent(SettingsEvent.ToggleDarkMode(!state.isDarkMode))
                            })
                    }
                }

                item {
                    SettingsSection(title = "Server") {
                        SettingsItem(
                            title = "Server URL",
                            subtitle = state.serverUrl,
                            onClick = { showServerDialog = true })
                        SettingsItem(
                            title = "Sync Interval",
                            subtitle = "${state.syncIntervalInMinutes} mins",
                            onClick = { showServerDialog = true })
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.onEvent(SettingsEvent.Logout) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Logout", color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }

        if (showServerDialog) {
            SettingsServerConfigurationDialog(
                currentUrl = state.serverUrl,
                currentInterval = state.syncIntervalInMinutes,
                onDismiss = { showServerDialog = false },
                onSave = { newUrl, newInterval ->
                    viewModel.onEvent(SettingsEvent.UpdateServerUrl(newUrl))
                    viewModel.onEvent(SettingsEvent.UpdateSyncInterval(newInterval))
                    showServerDialog = false
                })
        }
    }
}



@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsServerConfigurationDialog(
    currentUrl: String,
    currentInterval: Int,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit,
) {
    var url by remember { mutableStateOf(currentUrl) }
    var selectedInterval by remember { mutableIntStateOf(currentInterval) }

    val intervalOptions = listOf(5, 15, 30, 60)
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(url, selectedInterval) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
        }
        },
        title = { Text("Server Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Server URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        readOnly = true,
                        value = "$selectedInterval mins",
                        onValueChange = {},
                        label = { Text("Sync Interval") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor(type = PrimaryEditable, enabled = true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded, onDismissRequest = { expanded = false }) {
                        intervalOptions.forEach { interval ->
                            DropdownMenuItem(text = { Text("$interval mins") }, onClick = {
                                selectedInterval = interval
                                expanded = false
                            })
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(12.dp),
    )
}


