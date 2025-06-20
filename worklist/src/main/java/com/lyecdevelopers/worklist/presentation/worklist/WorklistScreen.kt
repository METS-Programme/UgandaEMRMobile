package com.lyecdevelopers.worklist.presentation.worklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lyecdevelopers.core.data.local.entity.PatientEntity
import com.lyecdevelopers.core.ui.components.BaseScreen
import com.lyecdevelopers.worklist.domain.model.PatientFilters
import com.lyecdevelopers.worklist.presentation.worklist.event.WorklistEvent

@Composable
fun WorklistScreen(
    onPatientClick: (PatientEntity) -> Unit,
    onStartVisit: (PatientEntity) -> Unit,
    onRegisterPatient: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorklistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }

    var filters by rememberSaveable(stateSaver = PatientFilters.Saver) {
        mutableStateOf(PatientFilters())
    }

    // Send filter changes to ViewModel
    LaunchedEffect(filters) {
        viewModel.onEvent(WorklistEvent.OnNameFilterChanged(filters.nameQuery))
        viewModel.onEvent(WorklistEvent.OnGenderFilterChanged(filters.gender))
//        viewModel.onEvent(WorklistEvent.OnStatusFilterChanged(filters.visitStatus.toString()))
    }

    BaseScreen(
        uiEventFlow = viewModel.uiEvent, isLoading = isLoading, showLoading = { isLoading = it }) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onRegisterPatient,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add, contentDescription = "Register Patient"
                    )
                }
            }) { padding ->
            LazyColumn(
                contentPadding = padding,
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    FilterSection(filters = filters, onFiltersChanged = { filters = it })
                }

                item {
                    WorklistSummary(patients = uiState.patients)
                }

                items(uiState.patients, key = { it.id }) { patient ->
                    PatientCard(
                        patient = patient,
                        onStartVisit = { onStartVisit(patient) },
                        onViewDetails = { onPatientClick(patient) })
                }
            }

        }
    }


}





