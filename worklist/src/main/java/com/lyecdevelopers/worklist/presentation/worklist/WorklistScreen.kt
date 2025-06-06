package com.lyecdevelopers.worklist.presentation.worklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lyecdevelopers.worklist.domain.model.PatientFilters
import com.lyecdevelopers.worklist.domain.model.PatientVisit

@Composable
fun WorklistScreen(
    patients: List<PatientVisit>,
    onPatientClick: (PatientVisit) -> Unit,
    onStartVisit: (PatientVisit) -> Unit,
    modifier: Modifier = Modifier,
) {

    val viewModel: WorklistViewModel = viewModel()

    var filters by rememberSaveable(stateSaver = PatientFilters.Saver) {
        mutableStateOf(PatientFilters())
    }

    val filteredPatients = patients.filter {
        (filters.nameQuery.isBlank() || it.name.contains(
            filters.nameQuery, ignoreCase = true
        )) && (filters.gender == null || it.gender == filters.gender) && (filters.visitStatus == null || it.status == filters.visitStatus)
    }

    Scaffold { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FilterSection(
                    filters = filters, onFiltersChanged = { filters = it })
            }

            item {
                WorklistSummary(patients = filteredPatients)
            }

            if (filteredPatients.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "No results icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No patients found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your filters or search terms.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

            } else {
                items(filteredPatients, key = { it.id }) { patient ->
                    PatientCard(
                        patient = patient,
                        onStartVisit = { onStartVisit(patient) },
                        onViewDetails = { onPatientClick(patient) })
                }
            }
        }
    }
}



