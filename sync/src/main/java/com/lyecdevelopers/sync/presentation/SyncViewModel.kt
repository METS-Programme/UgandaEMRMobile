package com.lyecdevelopers.sync.presentation

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyecdevelopers.core.common.scheduler.SchedulerProvider
import com.lyecdevelopers.core.data.preference.PreferenceManager
import com.lyecdevelopers.core.model.Form
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.core.model.cohort.Cohort
import com.lyecdevelopers.core.model.cohort.Indicator
import com.lyecdevelopers.core.model.encounter.EncounterType
import com.lyecdevelopers.core.model.o3.o3Form
import com.lyecdevelopers.core.model.order.OrderType
import com.lyecdevelopers.sync.domain.usecase.SyncUseCase
import com.lyecdevelopers.sync.presentation.forms.event.DownloadFormsUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncUseCase: SyncUseCase,
    private val schedulerProvider: SchedulerProvider,
    private val preferenceManager: PreferenceManager,
    private val context: Application,
) : ViewModel() {


    // forms
    private val _formItems = mutableStateListOf<Form>()
    val formItems: List<Form> get() = _formItems

    private val _selectedFormIds = mutableStateOf(setOf<String>())
    val selectedFormIds: State<Set<String>> get() = _selectedFormIds

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> get() = _searchQuery

    // cohorts
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    private val _cohorts = MutableStateFlow<List<Cohort>>(emptyList())
    val cohorts: StateFlow<List<Cohort>> = _cohorts

    private val _selectedCohort = MutableStateFlow<Cohort?>(null)
    val selectedCohort: StateFlow<Cohort?> = _selectedCohort


    private val _selectedIndicator = MutableStateFlow<Indicator?>(null)
    val selectedIndicator: StateFlow<Indicator?> = _selectedIndicator

    // encounters
    private val _encounterTypes = MutableStateFlow<List<EncounterType>>(emptyList())
    val encounterTypes: StateFlow<List<EncounterType>> = _encounterTypes

    // orders
    private val _orderTypes = MutableStateFlow<List<OrderType>>(emptyList())
    val orderTypes: StateFlow<List<OrderType>> = _orderTypes


    // parameters

    // Parameters state
    private val _availableParameters = MutableStateFlow<List<String>>(
        listOf(
            "email",
            "Birthplace",
            "Marital Status",
            "FingerPrint",
            "Nationality",
            "Citizenship",
            "Health Center",
            "Mother's Name",
            "Race",
            "Health Facility/District",
            "Patient Identifier",
            "Date of Birth",
            "Gender",
            "Telephone Number",
            "Address",
            "Occupation",
            "Education Level",
            "HIV Status"
        )
    )
    val availableParameters: StateFlow<List<String>> = _availableParameters

    private val _selectedParameters = MutableStateFlow<List<String>>(emptyList())
    val selectedParameters: StateFlow<List<String>> = _selectedParameters

    private val _highlightedAvailable = MutableStateFlow<List<String>>(emptyList())
    val highlightedAvailable: StateFlow<List<String>> = _highlightedAvailable

    private val _highlightedSelected = MutableStateFlow<List<String>>(emptyList())
    val highlightedSelected: StateFlow<List<String>> = _highlightedSelected


    // general

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> get() = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> get() = _error


    // 👇 UI Event Flow
    private val _uiEvent = MutableSharedFlow<DownloadFormsUiEvent>()
    val uiEvent: SharedFlow<DownloadFormsUiEvent> = _uiEvent.asSharedFlow()

    init {
        // forms
        loadForms()
        viewModelScope.launch {
            _selectedFormIds.value = preferenceManager.loadSelectedForms(context)
        }

        // Cohorts
        loadCohorts()

        // orders
        loadOrderTypes()

        // encounters
        loadEncounterTypes()
    }


    // Toggle highlight for available parameters (add/remove from highlightedAvailable)
    val toggleHighlightAvailable: (String) -> Unit = { item ->
        _highlightedAvailable.value = _highlightedAvailable.value.toMutableSet().also {
            if (!it.add(item)) it.remove(item)
        }.toList()
    }

    // Toggle highlight for selected parameters (add/remove from highlightedSelected)
    val toggleHighlightSelected: (String) -> Unit = { item ->
        _highlightedSelected.value = _highlightedSelected.value.toMutableSet().also {
            if (!it.add(item)) it.remove(item)
        }.toList()
    }

    // Move highlighted items from available to selected
    val moveRight: () -> Unit = {
        val itemsToMove = _highlightedAvailable.value
        _availableParameters.value = _availableParameters.value - itemsToMove
        _selectedParameters.value = _selectedParameters.value + itemsToMove
        _highlightedAvailable.value = emptyList()
    }

    // Move highlighted items from selected back to available
    val moveLeft: () -> Unit = {
        val itemsToMove = _highlightedSelected.value
        _selectedParameters.value = _selectedParameters.value - itemsToMove
        _availableParameters.value = _availableParameters.value + itemsToMove
        _highlightedSelected.value = emptyList()
    }


    private fun loadForms() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.loadForms().collect { result ->
                withContext(schedulerProvider.main) {
                    when (result) {
                        is Result.Loading -> {
                            _isLoading.value = true
                            _error.value = null
                        }

                        is Result.Success -> {
                            _isLoading.value = false
                            _formItems.clear()
                            _formItems.addAll(result.data)
                            _selectedFormIds.value = emptySet()
                        }

                        is Result.Error -> {
                            _isLoading.value = false
                            _error.value = result.message
                        }
                    }
                }
            }
        }
    }

    fun filterForms(query: String) {
        _searchQuery.value = query
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.filterForms(query).collect { result ->
                withContext(schedulerProvider.main) {
                    when (result) {
                        is Result.Loading -> {
                            _isLoading.value = true
                            _error.value = null
                        }

                        is Result.Success -> {
                            _isLoading.value = false
                            _formItems.clear()
                            _formItems.addAll(result.data)
                            _selectedFormIds.value = emptySet()
                        }

                        is Result.Error -> {
                            _isLoading.value = false
                            _error.value = result.message
                        }
                    }
                }
            }
        }
    }

    fun toggleFormSelection(uuid: String) {
        val newSet = _selectedFormIds.value.toMutableSet().apply {
            if (contains(uuid)) remove(uuid) else add(uuid)
        }
        _selectedFormIds.value = newSet

        viewModelScope.launch {
            preferenceManager.saveSelectedForms(context, newSet)
        }
    }

    fun clearSelection() {
        _selectedFormIds.value = emptySet()
    }

    fun onDownloadClick() {
        val selectedForms = getSelectedForms()

        viewModelScope.launch(schedulerProvider.io) {
            if (selectedForms.isEmpty()) {
                _uiEvent.emit(DownloadFormsUiEvent.ShowSnackbar("Please select at least one form"))
                return@launch
            }

            _isLoading.value = true
            _error.value = null

            val successfullyLoadedForms = mutableListOf<o3Form>()

            coroutineScope {
                selectedForms.forEach { form ->
                    launch {
                        syncUseCase.loadFormByUuid(form.uuid).collect { result ->
                            when (result) {
                                is Result.Success -> {
                                    successfullyLoadedForms.add(result.data)
                                }

                                is Result.Error -> {
                                    _uiEvent.emit(
                                        DownloadFormsUiEvent.ShowSnackbar(
                                            "Failed to load form '${form.name ?: form.uuid}': ${result.message}"
                                        )
                                    )
                                }

                                is Result.Loading -> {
                                    // Optional: show individual loading if needed
                                }
                            }
                        }
                    }
                }
            }

            // Save all successfully loaded forms
            if (successfullyLoadedForms.isNotEmpty()) {
                syncUseCase.saveFormsLocally(successfullyLoadedForms).collect { saveResult ->
                    withContext(schedulerProvider.main) {
                        when (saveResult) {
                            is Result.Loading -> {
                                _isLoading.value = true
                                _error.value = null
                            }

                            is Result.Success -> {
                                _isLoading.value = false
                                _uiEvent.emit(
                                    DownloadFormsUiEvent.FormsDownloaded(successfullyLoadedForms)
                                )
                            }

                            is Result.Error -> {
                                _isLoading.value = false
                                _uiEvent.emit(
                                    DownloadFormsUiEvent.ShowSnackbar(
                                        saveResult.message
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                _uiEvent.emit(
                    DownloadFormsUiEvent.ShowSnackbar("No forms could be loaded.")
                )
            }

        }
    }


    private fun getSelectedForms(): List<Form> =
        _formItems.filter { _selectedFormIds.value.contains(it.uuid) }

    private fun loadCohorts() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.getCohorts().collect { result ->
                withContext(schedulerProvider.main) {
                    when (result) {
                        is Result.Error -> {
                            _isLoading.value = false
                            _cohorts.value = emptyList()
                        }

                        is Result.Loading -> {
                            _isLoading.value = true
                            _error.value = null
                            _cohorts.value = emptyList()
                        }

                        is Result.Success<*> -> {
                            _isLoading.value = false
                            _cohorts.value = result.data as List<Cohort>

                        }
                    }
                }
            }
        }
    }


    fun onSelectedCohortChanged(cohort: Cohort) {
        _selectedCohort.value = cohort
    }

    fun onIndicatorSelected(indicator: Indicator) {
        _selectedIndicator.value = indicator
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun onApplyFilters() {
        // Trigger filter logic here
    }

    private fun loadEncounterTypes() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.getEncounterTypes().collect { result ->
                when (result) {
                    is Result.Loading -> {}
                    is Result.Success -> {
                        _encounterTypes.value = result.data
                        _error.value = null
                    }

                    is Result.Error -> {
                        _error.value = result.message
                    }
                }
            }
        }
    }

    private fun loadOrderTypes() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.getOrderTypes().collect { result ->
                when (result) {
                    is Result.Loading -> {}
                    is Result.Success -> {
                        _orderTypes.value = result.data
                        _error.value = null
                    }

                    is Result.Error -> {
                        _error.value = result.message
                    }
                }
            }
        }
    }

}


