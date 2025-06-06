package com.lyecdevelopers.worklist.presentation.patient

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lyecdevelopers.worklist.domain.model.PatientDetails
import com.lyecdevelopers.worklist.domain.model.VisitSummary
import com.lyecdevelopers.worklist.domain.model.Vitals
import com.lyecdevelopers.worklist.presentation.visit.VisitCard
import com.lyecdevelopers.worklist.presentation.visit.VisitDetailsDialog


@Composable
fun PatientDetailsScreen(
    patient: PatientDetails,
    onStartVisit: (PatientDetails) -> Unit,
    onStartEncounter: (PatientDetails?, Any?) -> Unit,
) {
    var selectedVisit by remember { mutableStateOf<VisitSummary?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val currentEncounters = patient.currentVisit?.encounters.orEmpty()
    val previousEncounters = patient.visitHistory.flatMap { it.encounters }

    Scaffold { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(patient.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${patient.age} years • ${patient.gender}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        patient.vitals?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Latest Vitals", style = MaterialTheme.typography.titleMedium)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            VitalsInfo(vitals = patient.vitals)
                        }
                    }
                }
            }

            patient.currentVisit?.let { current ->
                item {
                    Text("Current Visit", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        VisitCard(
                            visit = current,
                            isCurrent = true,
                            onClick = { selectedVisit = current })

                        Box(modifier = Modifier.align(Alignment.End)) {
                            IconButton(onClick = { dropdownExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More actions")
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }) {
                                DropdownMenuItem(text = { Text("New Visit") }, onClick = {
                                    dropdownExpanded = false
                                    onStartVisit(patient)
                                }, leadingIcon = {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                })
                                DropdownMenuItem(text = { Text("New Encounter") }, onClick = {
                                    dropdownExpanded = false
                                    onStartEncounter(patient, null)
                                }, leadingIcon = {
                                    Icon(Icons.Default.Info, contentDescription = null)
                                })

                            }
                        }
                    }
                }
            }

            if (patient.visitHistory.isNotEmpty()) {
                item {
                    Text("Visit History", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()
                }

                items(patient.visitHistory, key = { it.id }) { visit ->
                    VisitCard(
                        visit = visit, onClick = { selectedVisit = visit })
                }
            }

            if (currentEncounters.isNotEmpty() || previousEncounters.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Encounters", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()
                }

                if (currentEncounters.isNotEmpty()) {
                    item {
                        EncounterSection(
                            title = "Current Encounter", encounters = currentEncounters
                        )
                    }
                }

                if (previousEncounters.isNotEmpty()) {
                    item {
                        EncounterSection(
                            title = "Previous Encounters", encounters = previousEncounters
                        )
                    }
                }
            }
        }

        selectedVisit?.let {
            VisitDetailsDialog(visit = it, onDismiss = { selectedVisit = null })
        }

    }
}


@Composable
fun VitalsInfo(vitals: Vitals) {
    Column {
        Text("Vitals", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()
        ) {
            vitals.bloodPressure?.let {
                Text(
                    "BP: $it", style = MaterialTheme.typography.bodySmall
                )
            }
            vitals.heartRate?.let {
                Text(
                    "HR: $it bpm", style = MaterialTheme.typography.bodySmall
                )
            }
            vitals.temperature?.let {
                Text(
                    "Temp: $it °C", style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


