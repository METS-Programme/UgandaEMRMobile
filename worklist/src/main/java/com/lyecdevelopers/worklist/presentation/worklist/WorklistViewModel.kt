package com.lyecdevelopers.worklist.presentation.worklist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.lyecdevelopers.core._base.BaseViewModel
import com.lyecdevelopers.core.common.scheduler.SchedulerProvider
import com.lyecdevelopers.core.data.local.entity.PatientEntity
import com.lyecdevelopers.core.data.local.entity.VisitEntity
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.core.model.VisitStatus
import com.lyecdevelopers.core.ui.event.UiEvent.Navigate
import com.lyecdevelopers.form.domain.usecase.PatientsUseCase
import com.lyecdevelopers.worklist.domain.usecase.VisitUseCases
import com.lyecdevelopers.worklist.presentation.worklist.event.WorklistEvent
import com.lyecdevelopers.worklist.presentation.worklist.state.WorklistUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class WorklistViewModel @Inject constructor(
    private val patientsUseCase: PatientsUseCase,
    private val visitUseCases: VisitUseCases,
    private val schedulerProvider: SchedulerProvider,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(WorklistUiState())
    val uiState: StateFlow<WorklistUiState> = _uiState.asStateFlow()

    // paging
    private val _pagingData = MutableStateFlow<PagingData<PatientEntity>>(PagingData.empty())
    val pagingData: StateFlow<PagingData<PatientEntity>> = _pagingData.asStateFlow()

    // search
    private var searchJob: Job? = null

    // Filters stored as state variables
    private var nameFilter: String? = null
    private var genderFilter: String? = null
    private var statusFilter: String? = null

    init {
        loadPatients()
        getForms()

        val patientId = savedStateHandle.get<String>("patientId")
        if (patientId != null) {
            loadPatient(patientId)
            loadPatientVisits(patientId)
            loadPatientMostRecentVisit(patientId)
            getEncountersByPatientIdAndVisitId(
                patientId, uiState.value.mostRecentVisit?.visit?.id ?: ""
            )
        } else {
            _uiState.update { it.copy(error = "Patient ID not provided") }
        }
    }

    fun onEvent(event: WorklistEvent) {
        when (event) {
            is WorklistEvent.OnNameFilterChanged -> {
                nameFilter = event.name.takeIf { it.isNotBlank() }
                debounceLoadPatients()
            }

            is WorklistEvent.OnGenderFilterChanged -> {
                genderFilter = event.gender
                debounceLoadPatients()
            }

            is WorklistEvent.OnStatusFilterChanged -> {
                statusFilter = event.status?.name
                debounceLoadPatients()
            }

            is WorklistEvent.OnClearFilters -> {
                nameFilter = null
                genderFilter = null
                statusFilter = null
                debounceLoadPatients()
            }

            is WorklistEvent.OnPatientSelected -> {
                emitUiEvent(Navigate("patient_details/${event.patientId}"))
            }

            is WorklistEvent.OnRefresh -> debounceLoadPatients()

            is WorklistEvent.OnVisitTypeChanged -> {
                _uiState.update { it.copy(visitType = event.type) }
            }

            is WorklistEvent.OnVisitLocationChanged -> {
                _uiState.update { it.copy(visitLocation = event.location) }
            }

            is WorklistEvent.OnVisitStatusChanged -> {
                _uiState.update { it.copy(visitStatus = event.status) }
            }

            is WorklistEvent.OnStartDateChanged -> {
                _uiState.update { it.copy(startDate = event.date) }
            }

            is WorklistEvent.OnStartTimeChanged -> {
                _uiState.update { it.copy(startTime = event.time) }
            }

            is WorklistEvent.OnAmPmChanged -> {
                _uiState.update { it.copy(amPm = event.amPm) }
            }

            is WorklistEvent.OnVisitNotesChanged -> {
                _uiState.update { it.copy(visitNotes = event.notes) }
            }

            is WorklistEvent.OnLocationMenuExpandedChanged -> {
                _uiState.update { it.copy(locationMenuExpanded = event.expanded) }
            }

            is WorklistEvent.OnAmPmMenuExpandedChanged -> {
                _uiState.update { it.copy(amPmMenuExpanded = event.expanded) }
            }

            WorklistEvent.StartVisit -> startPatientVisit()
        }
    }

    private fun loadPatients() {
        viewModelScope.launch(schedulerProvider.io) {
            withContext(schedulerProvider.main) { showLoading() }

            patientsUseCase.searchPatients(
                name = nameFilter, gender = genderFilter, status = statusFilter
            ).collect { result ->
                withContext(schedulerProvider.main) {
                    handleResult(
                        result = result,
                        onSuccess = { patients ->
                            _uiState.update {
                                it.copy(
                                    patients = patients,
                                    error = null,
                                )
                            }
                        },
                        successMessage = "Successfully loaded patients",
                        errorMessage = (result as? Result.Error)?.message
                    )
                    hideLoading()
                }
            }
        }
    }

    private fun loadPatient(patientId: String) {
        viewModelScope.launch(schedulerProvider.io) {
            patientsUseCase.getPatientById(patientId).collect { result ->
                withContext(schedulerProvider.main) {
                    handleResult(
                        result = result,
                        onSuccess = { patient ->
                            _uiState.update { it.copy(selectedPatient = patient) }
                        },
                        successMessage = "Patient selected",
                        errorMessage = (result as? Result.Error)?.message
                    )
                    hideLoading()
                }
            }
        }
    }

    private fun startPatientVisit() {
        viewModelScope.launch(schedulerProvider.io) {
            val state = _uiState.value

            val patientId = state.selectedPatient?.id
            if (patientId == null) {
                _uiState.update { it.copy(error = "No patient selected") }
                return@launch
            }

            val visitId = UUID.randomUUID().toString()
            val status = when (state.visitStatus) {
                "New" -> VisitStatus.PENDING
                "Ongoing" -> VisitStatus.IN_PROGRESS
                "In the past" -> VisitStatus.COMPLETED
                else -> VisitStatus.PENDING
            }

            val visit = VisitEntity(
                id = visitId,
                patientId = patientId,
                status = status,
                scheduledTime = "${state.startDate} ${state.startTime} ${state.amPm}",
                type = state.visitType,
                date = state.startDate,
                notes = state.visitNotes
            )

            visitUseCases.saveVisit(visit).collect { result ->
                withContext(schedulerProvider.main) {
                    handleResult(
                        result = result,
                        onSuccess = {
                            loadPatients()
                        },
                        successMessage = "Saved visit successfully",
                        errorMessage = (result as? Result.Error)?.message
                    )
                }

            }


        }
    }

    private fun debounceLoadPatients(delayMillis: Long = 300L) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(delayMillis)
            loadPatients()
        }
    }

    fun loadPagedPatients() {
        viewModelScope.launch {
            patientsUseCase.getPagedPatients(nameFilter, genderFilter, statusFilter)
                .cachedIn(viewModelScope).collectLatest {
                    _pagingData.value = it
                }
        }
    }

    private fun loadPatientVisits(patientId: String) {
        viewModelScope.launch(schedulerProvider.io) {
            visitUseCases.getVisitSummariesForPatient(patientId).collect { result ->
                withContext(schedulerProvider.main) {
                    handleResult(
                        result = result, onSuccess = { visits ->
                            _uiState.update {
                                it.copy(
                                    visits = visits,
                                    encounters = visits.firstOrNull()?.encounters ?: emptyList(),

                                    )
                            }
                        },

                        errorMessage = (result as? Result.Error)?.message
                    )
                }
            }
        }
    }

    private fun loadPatientMostRecentVisit(patientId: String) {
        viewModelScope.launch(schedulerProvider.io) {
            visitUseCases.getMostRecentVisitForPatient(patientId).collect { result ->
                withContext(schedulerProvider.main) {
                    handleResult(
                        result = result, onSuccess = { visit ->
                            _uiState.update { it.copy(mostRecentVisit = visit) }

                        }, errorMessage = (result as? Result.Error)?.message
                    )
                }
            }
        }
    }

    // get encounters by patient id and visit id
    private fun getEncountersByPatientIdAndVisitId(patientId: String, visitId: String) {
        viewModelScope.launch(schedulerProvider.io) {
            visitUseCases.getEncountersByPatientIdAndVisitId(patientId, visitId).collect { result ->
                withContext(schedulerProvider.main) {
                    handleResult(
                        result = result, onSuccess = { encounters ->
                        }, errorMessage = (result as? Result.Error)?.message
                    )
                }
            }
        }
    }

    private fun getForms() {
        viewModelScope.launch(schedulerProvider.io) {
            visitUseCases.getForms().collect { result ->
                withContext(schedulerProvider.main) {
                    handleResult(
                        result = result, onSuccess = { forms ->
                            _uiState.update { it.copy(forms = forms) }
                        }, errorMessage = (result as? Result.Error)?.message
                    )
                }
            }
        }
    }
}




