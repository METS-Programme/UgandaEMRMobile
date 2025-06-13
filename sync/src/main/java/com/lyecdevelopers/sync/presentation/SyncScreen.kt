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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lyecdevelopers.core.model.cohort.Indicator
import com.lyecdevelopers.core.model.cohort.IndicatorRepository
import com.lyecdevelopers.core.model.o3.o3Form
import com.lyecdevelopers.sync.presentation.forms.DownloadFormsScreen
import com.lyecdevelopers.sync.presentation.forms.event.DownloadFormsUiEvent
import com.lyecdevelopers.sync.presentation.patients.PatientFilterSectionContent
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    lastSyncTime: String = "Not synced yet",
    lastSyncStatus: String = "Never Synced",
    lastSyncBy: String = "N/A",
    lastSyncError: String? = null,
    formsSynced: Int = 0,
    patientsSynced: Int = 0,
    autoSyncEnabled: Boolean = false,
    autoSyncInterval: String = "15 minutes",
    onToggleAutoSync: (Boolean) -> Unit = {},
    onBack: () -> Unit = {},
    onSyncNow: () -> Unit = {},
    onFormsSelected: (List<o3Form>) -> Unit = {},
) {
    val viewModel: SyncViewModel = hiltViewModel()

    val snackbarHostState = remember { SnackbarHostState() }


    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetVisible by rememberSaveable { mutableStateOf(false) }

    var showPatientFilterDialog by rememberSaveable { mutableStateOf(false) }


    val selectedCohort by viewModel.selectedCohort.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedIndicator by viewModel.selectedIndicator.collectAsState()
    val cohortOptions by viewModel.cohorts.collectAsState()
    val indicatorOptions: List<Indicator> = IndicatorRepository.reportIndicators

    // parameters
    val availableParameters by viewModel.availableParameters.collectAsState()
    val selectedParameters by viewModel.selectedParameters.collectAsState()
    val highlightedAvailable by viewModel.highlightedAvailable.collectAsState()
    val highlightedSelected by viewModel.highlightedSelected.collectAsState()


    // 👇 Listen for UI events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is DownloadFormsUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is DownloadFormsUiEvent.FormsDownloaded -> {
                    onFormsSelected(event.selectedForms)
                    isSheetVisible = false
                }
            }
        }
    }

    if (isSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isSheetVisible = false }, sheetState = sheetState
        ) {
            DownloadFormsScreen(
                viewModel = viewModel, onDownloadSelected = {
                    isSheetVisible = false
                })
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
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
                                Text("Last Error: $it", color = MaterialTheme.colorScheme.error)
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
                                Text("$formsSynced")
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
                                    checked = autoSyncEnabled, onCheckedChange = onToggleAutoSync
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
                            Spacer(Modifier.height(16.dp))
                            Text("Download Forms", style = MaterialTheme.typography.titleMedium)
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
                            Text("Download Patients", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))

                            // Button to open Patient Filter Dialog
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

    // Patient Filter Dialog implementation
    if (showPatientFilterDialog) {
        AlertDialog(onDismissRequest = { showPatientFilterDialog = false }, confirmButton = {
            TextButton(
                onClick = {
                    viewModel.onApplyFilters()
                    showPatientFilterDialog = false
                }) {
                Text("Apply")
            }
        }, dismissButton = {
            TextButton(onClick = { showPatientFilterDialog = false }) {
                Text("Cancel")
            }
        }, text = {
            PatientFilterSectionContent(
                cohortOptions = cohortOptions,
                selectedCohort = selectedCohort,
                onSelectedCohortChanged = viewModel::onSelectedCohortChanged,
                indicatorOptions = indicatorOptions,
                selectedIndicator = selectedIndicator,
                onIndicatorSelected = viewModel::onIndicatorSelected,
                selectedDate = selectedDate,
                onDateSelected = viewModel::onDateSelected,
                availableParameters = availableParameters,
                selectedParameters = selectedParameters,
                highlightedAvailable = highlightedAvailable,
                highlightedSelected = highlightedSelected,
                onHighlightAvailableToggle = viewModel.toggleHighlightAvailable,
                onHighlightSelectedToggle = viewModel.toggleHighlightSelected,
                onMoveRight = viewModel.moveRight,
                onMoveLeft = viewModel.moveLeft,
                onFilter = {} // filter triggered by Apply button above
            )
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

