package com.lyecdevelopers.worklist.presentation.worklist

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.lyecdevelopers.core._base.BaseViewModel
import com.lyecdevelopers.core.common.scheduler.SchedulerProvider
import com.lyecdevelopers.core.data.local.entity.PatientEntity
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.core.ui.event.UiEvent
import com.lyecdevelopers.form.domain.usecase.PatientsUseCase
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
import javax.inject.Inject


@HiltViewModel
class WorklistViewModel @Inject constructor(
    private val patientsUseCase: PatientsUseCase,
    private val schedulerProvider: SchedulerProvider,
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


            is WorklistEvent.OnClearFilters -> {
                nameFilter = null
                genderFilter = null
                statusFilter = null
                debounceLoadPatients()

            }

            is WorklistEvent.OnRefresh -> loadPatients()
            is WorklistEvent.OnPatientSelected -> {
                emitUiEvent(UiEvent.Navigate("patient_details/${event.patientId}"))
            }

            is WorklistEvent.OnStatusFilterChanged -> {
//                statusFilter = event.status.takeIf { it.isNotBlank() }

            }
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


}

