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
import com.lyecdevelopers.core.model.cohort.Attribute
import com.lyecdevelopers.core.model.cohort.CQIReportingCohort
import com.lyecdevelopers.core.model.cohort.Cohort
import com.lyecdevelopers.core.model.cohort.CohortResponse
import com.lyecdevelopers.core.model.cohort.DataDefinition
import com.lyecdevelopers.core.model.cohort.Indicator
import com.lyecdevelopers.core.model.cohort.IndicatorRepository
import com.lyecdevelopers.core.model.cohort.Parameters
import com.lyecdevelopers.core.model.cohort.RenderType
import com.lyecdevelopers.core.model.cohort.ReportCategory
import com.lyecdevelopers.core.model.cohort.ReportCategoryWrapper
import com.lyecdevelopers.core.model.cohort.ReportRequest
import com.lyecdevelopers.core.model.cohort.ReportType
import com.lyecdevelopers.core.model.encounter.EncounterType
import com.lyecdevelopers.core.model.o3.o3Form
import com.lyecdevelopers.core.model.order.OrderType
import com.lyecdevelopers.core.utils.AppLogger
import com.lyecdevelopers.sync.domain.usecase.SyncUseCase
import com.lyecdevelopers.sync.presentation.forms.event.DownloadFormsUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncUseCase: SyncUseCase,
    private val schedulerProvider: SchedulerProvider,
    private val preferenceManager: PreferenceManager,
    private val context: Application,
) : ViewModel() {

    // UI States
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> get() = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> get() = _error

    private val _uiEvent = MutableSharedFlow<DownloadFormsUiEvent>()
    val uiEvent: SharedFlow<DownloadFormsUiEvent> = _uiEvent.asSharedFlow()

    // Forms
    private val _formItems = mutableStateListOf<Form>()
    val formItems: List<Form> get() = _formItems

    private val _selectedFormIds = mutableStateOf(setOf<String>())
    val selectedFormIds: State<Set<String>> get() = _selectedFormIds

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> get() = _searchQuery


    // form
    private val _formCount = MutableStateFlow<Int>(0)
    val formCount: StateFlow<Int> = _formCount.asStateFlow()

    // Cohorts
    private val _cohorts = MutableStateFlow<List<Cohort>>(emptyList())
    val cohorts: StateFlow<List<Cohort>> = _cohorts

    private val _selectedCohort = MutableStateFlow<Cohort?>(null)
    val selectedCohort: StateFlow<Cohort?> = _selectedCohort

    private val _selectedDateRange = MutableStateFlow<Pair<LocalDate, LocalDate>?>(null)
    val selectedDateRange: StateFlow<Pair<LocalDate, LocalDate>?> = _selectedDateRange

    private val _selectedIndicator = MutableStateFlow<Indicator?>(null)
    val selectedIndicator: StateFlow<Indicator?> = _selectedIndicator

    // Encounters and Orders
    private val _encounterTypes = MutableStateFlow<List<EncounterType>>(emptyList())
    val encounterTypes: StateFlow<List<EncounterType>> = _encounterTypes

    private val _orderTypes = MutableStateFlow<List<OrderType>>(emptyList())
    val orderTypes: StateFlow<List<OrderType>> = _orderTypes

    // Indicator Parameters
    private val _availableParameters = MutableStateFlow<List<Attribute>>(emptyList())
    val availableParameters: StateFlow<List<Attribute>> = _availableParameters

    private val _selectedParameters = MutableStateFlow<List<Attribute>>(emptyList())
    val selectedParameters: StateFlow<List<Attribute>> = _selectedParameters

    private val _highlightedAvailable = MutableStateFlow<List<Attribute>>(emptyList())
    val highlightedAvailable: StateFlow<List<Attribute>> = _highlightedAvailable

    private val _highlightedSelected = MutableStateFlow<List<Attribute>>(emptyList())
    val highlightedSelected: StateFlow<List<Attribute>> = _highlightedSelected

    init {
        loadForms()
        loadCohorts()
        loadOrderTypes()
        loadEncounterTypes()
        restoreSelectedForms()
        loadReportIndicators()
        formCount()

    }

    private fun loadReportIndicators() {
        viewModelScope.launch(schedulerProvider.io) {
            val indicators = IndicatorRepository.reportIndicators
            withContext(schedulerProvider.main) {
                _availableParameters.value = indicators.flatMap { it.attributes }
            }
        }
    }


    private fun restoreSelectedForms() {
        viewModelScope.launch {
            _selectedFormIds.value = preferenceManager.loadSelectedForms(context)
        }
    }

    private fun updateUiState(loading: Boolean, errorMsg: String? = null) {
        _isLoading.value = loading
        _error.value = errorMsg
    }

    private suspend fun <T> collectResult(
        result: Result<T>,
        onSuccess: suspend (T) -> Unit,
    ) = withContext(schedulerProvider.main) {
        when (result) {
            is Result.Loading -> updateUiState(true)
            is Result.Success -> {
                updateUiState(false)
                onSuccess(result.data)
            }

            is Result.Error -> updateUiState(false, result.message)
        }
    }

    private fun loadForms() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.loadForms().collect { result ->
                collectResult(result) {
                    _formItems.clear()
                    _formItems.addAll(it)
                    _selectedFormIds.value = emptySet()
                }
            }
        }
    }

    fun filterForms(query: String) {
        _searchQuery.value = query
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.filterForms(query).collect { result ->
                collectResult(result) {
                    _formItems.clear()
                    _formItems.addAll(it)
                    _selectedFormIds.value = emptySet()
                }
            }
        }
    }

    fun toggleFormSelection(uuid: String) {
        _selectedFormIds.value = _selectedFormIds.value.toMutableSet().apply {
            if (!add(uuid)) remove(uuid)
        }

        viewModelScope.launch {
            preferenceManager.saveSelectedForms(context, _selectedFormIds.value)
        }
    }

    fun clearSelection() {
        _selectedFormIds.value = emptySet()
    }

    fun onDownloadClick() {
        val selectedForms = getSelectedForms()
        if (selectedForms.isEmpty()) {
            viewModelScope.launch { _uiEvent.emit(DownloadFormsUiEvent.ShowSnackbar("Please select at least one form")) }
            return
        }

        viewModelScope.launch(schedulerProvider.io) {
            updateUiState(true)

            val loadedForms = mutableListOf<o3Form>()

            coroutineScope {
                selectedForms.forEach { form ->
                    launch {
                        syncUseCase.loadFormByUuid(form.uuid).collect { result ->
                            when (result) {
                                is Result.Success -> loadedForms.add(result.data)
                                is Result.Error -> _uiEvent.emit(
                                    DownloadFormsUiEvent.ShowSnackbar("Failed to load form '${form.name ?: form.uuid}': ${result.message}")
                                )

                                else -> {}
                            }
                        }
                    }
                }
            }

            if (loadedForms.isEmpty()) {
                _uiEvent.emit(DownloadFormsUiEvent.ShowSnackbar("No forms could be loaded."))
                updateUiState(false)
                return@launch
            }

            syncUseCase.saveFormsLocally(loadedForms).collect { saveResult ->
                collectResult(saveResult) {
                    _uiEvent.emit(DownloadFormsUiEvent.FormsDownloaded(loadedForms))
                }
            }
        }
    }

    private fun getSelectedForms(): List<Form> =
        _formItems.filter { _selectedFormIds.value.contains(it.uuid) }

    private fun loadCohorts() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.getCohorts().collect { result ->
                collectResult(result) { _cohorts.value = it }
            }
        }
    }

    private fun loadEncounterTypes() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.getEncounterTypes().collect { result ->
                if (result is Result.Success) _encounterTypes.value = result.data
                else if (result is Result.Error) _error.value = result.message
            }
        }
    }

    private fun loadOrderTypes() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.getOrderTypes().collect { result ->
                if (result is Result.Success) _orderTypes.value = result.data
                else if (result is Result.Error) _error.value = result.message
            }
        }
    }

    // Filter logic
    fun onSelectedCohortChanged(cohort: Cohort) {
        _selectedCohort.value = cohort
    }

    fun onIndicatorSelected(indicator: Indicator) {
        _selectedIndicator.value = indicator
        _availableParameters.value = indicator.attributes
        _selectedParameters.value = emptyList()
        _highlightedAvailable.value = emptyList()
        _highlightedSelected.value = emptyList()
    }


    fun onDateRangeSelected(range: Pair<LocalDate, LocalDate>) {
        _selectedDateRange.value = range
    }

    fun onApplyFilters() {
        viewModelScope.launch(schedulerProvider.io) {
            val validationError = validateFilters()
            if (validationError != null) {
                withContext(schedulerProvider.main) {
                    _uiEvent.emit(DownloadFormsUiEvent.ShowSnackbar(validationError))
                }
                return@launch
            }

            // Safe to unwrap here because validateFilters() ensures no nulls
            val indicator = _selectedIndicator.value!!
            val cohort = _selectedCohort.value!!
            val dateRange = selectedDateRange.value!!
            val (dateStart, dateEnd) = dateRange

            val reportRequest = buildReportRequest(cohort, dateStart, dateEnd)
            val dataDefinitionPayload = buildDataDefinitionPayload(reportRequest, indicator)

            syncUseCase.createDataDefinition(dataDefinitionPayload).collect { result ->
                withContext(schedulerProvider.main) {
                    when (result) {
                        is Result.Loading -> _isLoading.value = true

                        is Result.Success -> {
                            _isLoading.value = false
                            _uiEvent.emit(
                                DownloadFormsUiEvent.ShowSnackbar("Data definition created successfully")
                            )
                        }

                        is Result.Error -> {
                            _isLoading.value = false
                            _uiEvent.emit(
                                DownloadFormsUiEvent.ShowSnackbar("Error: ${result.message}")
                            )
                        }
                    }
                }
            }
        }
    }




    // Dual ListBox Logic for Indicator Parameters
    val toggleHighlightAvailable: (Attribute) -> Unit = { item ->
        _highlightedAvailable.value = _highlightedAvailable.value.toMutableSet().apply {
            if (!add(item)) remove(item)
        }.toList()
    }

    val toggleHighlightSelected: (Attribute) -> Unit = { item ->
        _highlightedSelected.value = _highlightedSelected.value.toMutableSet().apply {
            if (!add(item)) remove(item)
        }.toList()
    }

    val moveRight: () -> Unit = {
        val items = _highlightedAvailable.value
        _availableParameters.value -= items
        _selectedParameters.value += items
        _highlightedAvailable.value = emptyList()
    }

    val moveLeft: () -> Unit = {
        val items = _highlightedSelected.value
        _selectedParameters.value -= items
        _availableParameters.value += items
        _highlightedSelected.value = emptyList()
    }

    private fun buildReportRequest(
        cohort: Cohort,
        start: LocalDate,
        end: LocalDate,
    ): ReportRequest {
        val formatter = DateTimeFormatter.ISO_DATE
        return ReportRequest(
            uuid = cohort.uuid,
            startDate = start.format(formatter),
            endDate = end.format(formatter),
            type = "cohort",
            reportCategory = ReportCategoryWrapper(
                category = ReportCategory.FACILITY, renderType = RenderType.JSON
            ),
            reportIndicators = selectedParameters.value,
            reportType = ReportType.DYNAMIC,
            reportingCohort = CQIReportingCohort.PATIENTS_WITH_ENCOUNTERS
        )
    }

    private fun buildDataDefinitionPayload(
        reportRequest: ReportRequest,
        indicator: Indicator,
    ): DataDefinition {
        return DataDefinition(
            cohort = CohortResponse(
                type = reportRequest.type,
                clazz = "",
                uuid = reportRequest.uuid,
                name = "",
                description = "",
                parameters = listOf(
                    Parameters(
                        startdate = reportRequest.startDate, enddate = reportRequest.endDate
                    )
                )
            ), columns = indicator.attributes.joinToString(",") { it.label })
    }

    private fun validateFilters(): String? {
        val indicator = _selectedIndicator.value
        val cohort = _selectedCohort.value
        val dateRange = selectedDateRange.value

        return when {
            indicator == null -> "Please select an indicator"
            cohort == null -> "Please select a cohort"
            dateRange == null -> "Please select a valid date range"

            else -> null
        }
    }

    private fun formCount() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.getFormCount().collect { result ->
                withContext(schedulerProvider.main) {
                    when (result) {
                        is Result.Success -> {
                            val count = result.data
                            AppLogger.d("Total forms in DB: $count")
                            _formCount.value = count
                        }

                        is Result.Error -> {
                            AppLogger.e("Form count failed: ${result.message}")
                        }

                        Result.Loading -> TODO()
                    }
                }

            }
        }
    }

}



