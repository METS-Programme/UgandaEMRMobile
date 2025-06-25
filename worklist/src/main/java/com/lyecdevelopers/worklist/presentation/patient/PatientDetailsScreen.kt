package com.lyecdevelopers.worklist.presentation.patient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lyecdevelopers.core.data.local.entity.PatientEntity
import com.lyecdevelopers.core.ui.components.BaseScreen
import com.lyecdevelopers.core.ui.components.EmptyStateView
import com.lyecdevelopers.worklist.domain.model.VisitSummary
import com.lyecdevelopers.worklist.domain.model.Vitals
import com.lyecdevelopers.worklist.presentation.visit.VisitCard
import com.lyecdevelopers.worklist.presentation.visit.VisitDetailsDialog
import com.lyecdevelopers.worklist.presentation.worklist.WorklistViewModel
import java.time.LocalDate
import java.time.Period


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(
    patientId: String,
    onStartVisit: (PatientEntity) -> Unit,
    onStartEncounter: (PatientEntity?, Any?) -> Unit,
    onAddVitals: (PatientEntity) -> Unit,
    viewModel: WorklistViewModel = hiltViewModel(),
    navController: NavController,

    ) {

    var isLoading by remember { mutableStateOf(false) }
    val state by viewModel.uiState.collectAsState()


    var selectedVisit by remember { mutableStateOf<VisitSummary?>(null) }
    var fabExpanded by remember { mutableStateOf(false) }

    var dropdownExpanded by remember { mutableStateOf(false) }



    BaseScreen(
        uiEventFlow = viewModel.uiEvent,
        navController = navController,
        isLoading = isLoading,
        showLoading = { isLoading = it },
    ) {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text("Patient Details") })
        }, floatingActionButton = {
            Box(contentAlignment = Alignment.BottomEnd) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(bottom = 72.dp, end = 0.dp)
                ) {
                    AnimatedVisibility(visible = fabExpanded) {
                        SmallFabButton(
                            icon = Icons.Outlined.MonitorHeart, // or any medical icon
                            label = "New Vitals",
                            onClick = {
                                fabExpanded = false
                                state.selectedPatient?.let(onAddVitals)
                            },
                        )
                    }

                    AnimatedVisibility(visible = fabExpanded) {
                        SmallFabButton(
                            icon = Icons.Default.Info, label = "New Encounter",
                            onClick = {
                                fabExpanded = false
                                onStartEncounter(state.selectedPatient, null)
                            },
                        )
                    }

                    AnimatedVisibility(visible = fabExpanded) {
                        SmallFabButton(
                            icon = Icons.Default.Add, label = "New Visit",
                            onClick = {
                                fabExpanded = false
                                state.selectedPatient?.let(onStartVisit)
                            },
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Crossfade(
                        targetState = fabExpanded, label = "FAB Icon Crossfade"
                    ) { expanded ->
                        Icon(
                            imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (expanded) "Close" else "Add"
                        )
                    }
                }
            }
        }

        ) { padding ->
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Patient Info and Vitals
                item {
                    val patient = state.selectedPatient

                    if (patient != null) {
                        val age = calculateAge(patient.dateOfBirth)
                        val ageText = if (age >= 0) "$age years" else "Unknown age"
                        val demographics = "$ageText • ${patient.gender}"

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Patient name
                            Text(
                                text = "${patient.firstName} • ${patient.lastName}",
                                style = MaterialTheme.typography.titleMedium
                            )

                            // Age and gender
                            Text(
                                text = demographics, style = MaterialTheme.typography.bodyMedium
                            )

                            // Vitals or Empty State
                            if (state.vitals != null) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Latest Vitals",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    HorizontalDivider()
                                    VitalsInfo(vitals = state.vitals!!)
                                }
                            } else {
                                EmptyStateView(message = "No vitals recorded.")
                            }
                        }
                    } else {
                        EmptyStateView(message = "No patient selected.")
                    }
                }


                // Current Visit Section
                item {
                    Text("Current Visit", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    if (state.visits.isNotEmpty()) {
                        val currentVisit = state.visits.first()
                        VisitCard(
                            visit = currentVisit,
                            isCurrent = true,
                            onClick = { selectedVisit = currentVisit })
                    } else {
                        EmptyStateView("No ongoing visit.")
                    }

                }

                // Visit History
                val pastVisits = if (state.visits.size > 1) state.visits.drop(1) else emptyList()
                if (pastVisits.isNotEmpty()) {
                    item {
                        Text("Visit History", style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider()
                    }

                    items(pastVisits, key = { it.id }) { visit ->
                        VisitCard(visit = visit, onClick = { /* Handle */ })
                    }
                }

                // Encounter Section
                item {
                    Text("Encounters", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()
                }

                if (state.encounters.isNotEmpty()) {
                    item {
                        EncounterSection(
                            title = "Current Encounter", encounters = state.encounters
                        )
                    }

                    item {
                        EncounterSection(
                            title = "Previous Encounters", encounters = state.encounters
                        )
                    }
                } else {
                    item {
                        Column {
                            EmptyStateView("No encounters available.")
                            Spacer(modifier = Modifier.height(8.dp))

                        }
                    }
                }
            }

            selectedVisit?.let {
                VisitDetailsDialog(visit = it, onDismiss = { selectedVisit = null })
            }
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


@Composable
fun VisitActionButtons(
    onStartVisit: () -> Unit,
    onStartEncounter: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        OutlinedButton(
            onClick = onStartVisit, modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("New Visit")
        }

        OutlinedButton(
            onClick = onStartEncounter, modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Info, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("New Encounter")
        }
    }
}


@Composable
fun SmallFabButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        text = { Text(label) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.shadow(4.dp, RoundedCornerShape(16.dp))
    )
}

fun calculateAge(dateOfBirth: String): Int {
    return try {
        val dob = LocalDate.parse(dateOfBirth)
        val today = LocalDate.now()
        Period.between(dob, today).years
    } catch (e: Exception) {
        -1 // fallback if parsing fails
    }
}


