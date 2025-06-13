package com.lyecdevelopers.sync.presentation.patients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lyecdevelopers.core.model.cohort.Cohort
import com.lyecdevelopers.core.model.cohort.Indicator
import com.lyecdevelopers.core.ui.components.IndicatorAttributesScreen
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientFilterSectionContent(
    cohortOptions: List<Cohort>,
    selectedCohort: Cohort?,
    onSelectedCohortChanged: (Cohort) -> Unit,
    indicatorOptions: List<Indicator>,
    selectedIndicator: Indicator?,
    onIndicatorSelected: (Indicator) -> Unit,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    availableParameters: List<String>,
    selectedParameters: List<String>,
    highlightedAvailable: List<String>,
    highlightedSelected: List<String>,
    onHighlightAvailableToggle: (String) -> Unit,
    onHighlightSelectedToggle: (String) -> Unit,
    onMoveRight: () -> Unit,
    onMoveLeft: () -> Unit,
    onFilter: () -> Unit,
) {
    var expandedCohort by remember { mutableStateOf(false) }
    var expandedIndicator by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()
            ?.toEpochMilli()
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
        // -- All your dropdowns, buttons, and content here --
        // Cohort Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedCohort, onExpandedChange = { expandedCohort = !expandedCohort }) {
            TextField(
                readOnly = true,
                value = selectedCohort?.display ?: "Select Cohort",
                onValueChange = {},
                label = { Text("Cohort") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCohort) },
                modifier = Modifier
                    .menuAnchor(type, enabled)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedCohort, onDismissRequest = { expandedCohort = false }) {
                cohortOptions.forEach { cohort ->
                    DropdownMenuItem(
                        text = { Text(cohort.display ?: "Unnamed Cohort") },
                        onClick = {
                            onSelectedCohortChanged(cohort)
                            expandedCohort = false
                        })
                }
            }
        }

        // Indicator Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedIndicator,
            onExpandedChange = { expandedIndicator = !expandedIndicator }) {
            TextField(
                readOnly = true,
                value = selectedIndicator?.label ?: "Select Indicator",
                onValueChange = {},
                label = { Text("Indicator") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedIndicator) },
                modifier = Modifier
                    .menuAnchor(type, enabled)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedIndicator, onDismissRequest = { expandedIndicator = false }) {
                indicatorOptions.forEach { indicator ->
                    DropdownMenuItem(text = { Text(indicator.label) }, onClick = {
                        onIndicatorSelected(indicator)
                        expandedIndicator = false
                    })
                }
            }
        }

        // Date Picker Button
        OutlinedButton(
            onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(selectedDate?.toString() ?: "Select Date")
        }

        if (showDatePicker) {
            DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            }, dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }) {
                DatePicker(state = datePickerState)
            }
        }

        // You can uncomment and adjust if you want to show this inside dialog
        IndicatorAttributesScreen(
            availableParameters = availableParameters,
            selectedParameters = selectedParameters,
            highlightedAvailable = highlightedAvailable,
            highlightedSelected = highlightedSelected,
            toggleHighlightAvailable = onHighlightAvailableToggle,
            toggleHighlightSelected = onHighlightSelectedToggle,
            moveRight = onMoveRight,
            moveLeft = onMoveLeft,
        )

        // Apply Button
        Button(
            onClick = onFilter, modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Apply Filters")
        }
    }
}