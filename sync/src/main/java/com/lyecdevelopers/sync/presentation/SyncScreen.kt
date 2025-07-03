package com.lyecdevelopers.sync.presentation

// Sync module
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lyecdevelopers.core.model.cohort.IndicatorRepository
import com.lyecdevelopers.core.model.o3.o3Form
import com.lyecdevelopers.core.ui.components.BaseScreen
import com.lyecdevelopers.sync.presentation.event.SyncEvent
import com.lyecdevelopers.sync.presentation.forms.DownloadFormsScreen
import com.lyecdevelopers.sync.presentation.patients.PatientFilterSectionContent


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    lastSyncTime: String = "Not synced yet",
    lastSyncStatus: String = "Never Synced",
    lastSyncBy: String = "N/A",
    lastSyncError: String? = null,
    patientsSynced: Int = 0,
    autoSyncEnabled: Boolean = false,
    autoSyncInterval: String = "15 minutes",
    onToggleAutoSync: (Boolean) -> Unit = {},
    onBack: () -> Unit = {},
    onSyncNow: () -> Unit = {},
    onFormsSelected: (List<o3Form>) -> Unit = {},
) {
    val viewModel: SyncViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetVisible by rememberSaveable { mutableStateOf(false) }
    var showPatientFilterDialog by rememberSaveable { mutableStateOf(false) }


    if (isSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isSheetVisible = false }, sheetState = sheetState
        ) {
            DownloadFormsScreen(
                viewModel = viewModel, onDownloadSelected = { isSheetVisible = false })
        }
    }

    BaseScreen(
        uiEventFlow = viewModel.uiEvent,
        isLoading = uiState.isLoading,
        showLoading = { /* handled by uiState */ }) {
        Scaffold { padding ->
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    SyncSection(title = "Sync Status") {
                        Surface(
                            tonalElevation = 1.dp,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Last Sync: $lastSyncTime")
                                Text("Status: $lastSyncStatus")
                                Text("Synced By: $lastSyncBy")
                                lastSyncError?.let {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Last Error: $it", color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = onSyncNow, modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Sync, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Sync Now")
                                }
                            }
                        }
                    }
                }

                item {
                    SyncSection(title = "Data Summary") {
                        Surface(
                            tonalElevation = 1.dp,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Forms Synced:")
                                    Text("${uiState.formCount}")
                                }
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Patients Synced:")
                                    Text("$patientsSynced")
                                }
                            }
                        }
                    }
                }

                item {
                    SyncSection(title = "Auto Sync") {
                        Surface(
                            tonalElevation = 1.dp,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Enable Auto-Sync")
                                    Spacer(Modifier.weight(1f))
                                    Switch(
                                        checked = autoSyncEnabled,
                                        onCheckedChange = onToggleAutoSync
                                    )
                                }
                                if (autoSyncEnabled) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Interval: $autoSyncInterval",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    SyncSection(title = "Manual Download") {
                        Surface(
                            tonalElevation = 1.dp,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Download Forms", style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { isSheetVisible = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Download Forms")
                                }

                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Download Patients",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { showPatientFilterDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Download Patients")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPatientFilterDialog) {
        AlertDialog(onDismissRequest = { showPatientFilterDialog = false }, confirmButton = {
            TextButton(
                onClick = {
                    viewModel.onEvent(SyncEvent.ApplyFilters)
                    showPatientFilterDialog = false
                }) { Text("Apply") }
        }, dismissButton = {
            TextButton(onClick = { showPatientFilterDialog = false }) {
                Text("Cancel")
            }
        }, text = {
            PatientFilterSectionContent(
                cohortOptions = uiState.cohorts,
                selectedCohort = uiState.selectedCohort,
                onSelectedCohortChanged = {
                    viewModel.onEvent(SyncEvent.SelectedCohortChanged(it))
                },
                indicatorOptions = IndicatorRepository.reportIndicators,
                selectedIndicator = uiState.selectedIndicator,
                onIndicatorSelected = {
                    viewModel.onEvent(SyncEvent.IndicatorSelected(it))
                },
                selectedDateRange = uiState.selectedDateRange,
                onDateRangeSelected = { startDate, endDate ->
                    viewModel.onEvent(SyncEvent.DateRangeSelected(Pair(startDate, endDate)))
                },
                availableParameters = uiState.availableParameters,
                selectedParameters = uiState.selectedParameters,
                highlightedAvailable = uiState.highlightedAvailable,
                highlightedSelected = uiState.highlightedSelected,
                onHighlightAvailableToggle = {
                    viewModel.onEvent(SyncEvent.ToggleHighlightAvailable(it))
                },
                onHighlightSelectedToggle = {
                    viewModel.onEvent(SyncEvent.ToggleHighlightSelected(it))
                },
                onMoveRight = { viewModel.onEvent(SyncEvent.MoveRight) },
                onMoveLeft = { viewModel.onEvent(SyncEvent.MoveLeft) },
                onFilter = {
                    viewModel.onEvent(SyncEvent.ApplyFilters)
                    showPatientFilterDialog = false
                })
        })
    }
}


@Composable
fun SyncSection(
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

