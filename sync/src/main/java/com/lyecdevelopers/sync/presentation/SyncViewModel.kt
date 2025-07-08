package com.lyecdevelopers.sync.presentation

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.lyecdevelopers.core._base.BaseViewModel
import com.lyecdevelopers.core.common.scheduler.SchedulerProvider
import com.lyecdevelopers.core.data.preference.PreferenceManager
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
import com.lyecdevelopers.core.model.cohort.formatReportArray
import com.lyecdevelopers.core.model.o3.o3Form
import com.lyecdevelopers.sync.domain.usecase.SyncUseCase
import com.lyecdevelopers.sync.presentation.event.SyncEvent
import com.lyecdevelopers.sync.presentation.state.SyncUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncUseCase: SyncUseCase,
    private val schedulerProvider: SchedulerProvider,
    private val preferenceManager: PreferenceManager,
    private val context: Application,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()


    init {
        loadForms()
        loadCohorts()
        loadOrderTypes()
        loadEncounterTypes()
        restoreSelectedForms()
        loadReportIndicators()
        updateFormCount()
        updatePatientCount()
        updateEncounterCount()
        restoreAutoSyncSettings()
    }

    fun onEvent(event: SyncEvent) {
        when (event) {
            is SyncEvent.FilterForms -> filterForms(event.query)
            is SyncEvent.ToggleFormSelection -> toggleFormSelection(event.uuid)
            is SyncEvent.FormsDownloaded -> TODO()
            SyncEvent.ClearSelection -> clearSelection()
            SyncEvent.DownloadForms -> onDownloadClick()

            is SyncEvent.SelectedCohortChanged -> updateUi { copy(selectedCohort = event.cohort) }
            is SyncEvent.IndicatorSelected -> onIndicatorSelected(event.indicator)
            SyncEvent.ApplyFilters -> onApplyFilters()

            is SyncEvent.ToggleHighlightAvailable -> toggleHighlightAvailable(event.item)
            is SyncEvent.ToggleHighlightSelected -> toggleHighlightSelected(event.item)
            SyncEvent.MoveRight -> moveRight()
            SyncEvent.MoveLeft -> moveLeft()
            is SyncEvent.UpdateLastSyncTime -> {
                _uiState.update { it.copy(lastSyncTime = event.time) }
            }

            is SyncEvent.UpdateLastSyncStatus -> {
                _uiState.update { it.copy(lastSyncStatus = event.status) }
            }

            is SyncEvent.UpdateLastSyncBy -> {
                _uiState.update { it.copy(lastSyncBy = event.user) }
            }

            is SyncEvent.UpdateLastSyncError -> {
                _uiState.update { it.copy(lastSyncError = event.error) }
            }

            is SyncEvent.ToggleAutoSync -> handleToggleAutoSync(event.enabled)


            is SyncEvent.UpdateAutoSyncInterval -> updateAutoSyncInterval(event.interval)

        }

    }

    private fun loadForms() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.loadForms().collect { result ->
                _uiState.value = _uiState.value.copy(isLoading = true)
                handleResult(
                    result = result, onSuccess = { forms ->
                        updateUi {
                            copy(
                                isLoading = false,
                                formItems = forms,
                                selectedFormIds = emptySet(),
                                searchQuery = ""
                            )
                        }
                    }, errorMessage = (result as? Result.Error)?.message
                )
            }
        }
    }

    private fun filterForms(query: String) {
        updateUi { copy(searchQuery = query) }
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.filterForms(query).collect { result ->
                _uiState.value = _uiState.value.copy(isLoading = true)
                handleResult(
                    result = result, onSuccess = { forms ->
                        updateUi {
                            copy(formItems = forms, selectedFormIds = emptySet())
                        }
                    }, errorMessage = (result as? Result.Error)?.message
                )

            }
        }
    }

    private fun toggleFormSelection(uuid: String) {
        val newIds = uiState.value.selectedFormIds.toMutableSet().apply {
            if (!add(uuid)) remove(uuid)
        }
        updateUi { copy(selectedFormIds = newIds) }

        viewModelScope.launch {
            preferenceManager.saveSelectedForms(context, newIds)
        }
    }

    private fun clearSelection() {
        updateUi { copy(selectedFormIds = emptySet()) }
    }

    private fun onDownloadClick() {
        val selectedForms = getSelectedForms()
        if (selectedForms.isEmpty()) {
            return
        }

        viewModelScope.launch(schedulerProvider.io) {
            updateUi { copy(isLoading = true) }
            val loadedForms = mutableListOf<o3Form>()
            coroutineScope {
                selectedForms.forEach { form ->
                    launch {
                        syncUseCase.loadFormByUuid(form.uuid).collect { result ->
                            when (result) {
                                is Result.Success -> loadedForms.add(result.data)
                                is Result.Error -> {}
                                else -> {}
                            }
                        }
                    }
                }
            }

            if (loadedForms.isEmpty()) {
                updateUi { copy(isLoading = false) }
                return@launch
            }

            syncUseCase.saveFormsLocally(loadedForms).collect { saveResult ->
                withContext(schedulerProvider.main) {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    handleResult(
                        result = saveResult,
                        onSuccess = { data ->
//                                                isSheetVisible = false
                            clearSelection()
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }, successMessage = "Successfully downloaded selected forms",
                        errorMessage = (saveResult as? Result.Error)?.message
                    )
                }
            }
        }
    }

    private fun getSelectedForms() =
        uiState.value.formItems.filter { uiState.value.selectedFormIds.contains(it.uuid) }

    private fun loadCohorts() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.getCohorts().collect { result ->
                handleResult(
                    result = result, onSuccess = { cohorts ->
                        updateUi { copy(cohorts = cohorts) }
                    }, errorMessage = (result as? Result.Error)?.message
                )
            }
        }
    }

    private fun loadEncounterTypes() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.getEncounterTypes().collect { result ->
                handleResult(
                    result = result, onSuccess = { encounterTypes ->
                        updateUi { copy(encounterTypes = encounterTypes) }
                    }, errorMessage = (result as? Result.Error)?.message
                )
            }
        }
    }

    private fun loadOrderTypes() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.getOrderTypes().collect { result ->
                handleResult(
                    result = result, onSuccess = { orderTypes ->
                        updateUi { copy(orderTypes = orderTypes) }
                    }, errorMessage = (result as? Result.Error)?.message
                )
            }
        }
    }

    private fun onIndicatorSelected(indicator: Indicator) {
        updateUi {
            copy(
                selectedIndicator = indicator,
                availableParameters = indicator.attributes,
                selectedParameters = emptyList(),
                highlightedAvailable = emptyList(),
                highlightedSelected = emptyList()
            )
        }
    }

    fun onApplyFilters() {
        viewModelScope.launch(schedulerProvider.io) {
            val error = validateFilters()
            if (error != null) {
                return@launch
            }

            val indicator = uiState.value.selectedIndicator!!
            val cohort = uiState.value.selectedCohort!!
            val (start, end) = uiState.value.selectedDateRange!!

            val reportRequest = buildReportRequest(cohort, start, end)
            val payload = buildDataDefinitionPayload(reportRequest, indicator)

            syncUseCase.createDataDefinition(payload).collect { result ->
                _uiState.value = _uiState.value.copy(isLoading = true)
                handleResult(
                    result = result,
                    onSuccess = { data ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    },
                    successMessage = "Successfully created data definition",
                    errorMessage = (result as? Result.Error)?.message
                )
            }
        }
    }

    private fun toggleHighlightAvailable(item: Attribute) {
        val new = uiState.value.highlightedAvailable.toMutableSet().apply {
            if (!add(item)) remove(item)
        }.toList()
        updateUi { copy(highlightedAvailable = new) }
    }

    private fun toggleHighlightSelected(item: Attribute) {
        val new = uiState.value.highlightedSelected.toMutableSet().apply {
            if (!add(item)) remove(item)
        }.toList()
        updateUi { copy(highlightedSelected = new) }
    }

    private fun moveRight() {
        val items = uiState.value.highlightedAvailable
        updateUi {
            copy(
                availableParameters = availableParameters - items.toSet(),
                selectedParameters = selectedParameters + items,
                highlightedAvailable = emptyList()
            )
        }
    }

    private fun moveLeft() {
        val items = uiState.value.highlightedSelected
        updateUi {
            copy(
                selectedParameters = selectedParameters - items.toSet(),
                availableParameters = availableParameters + items,
                highlightedSelected = emptyList()
            )
        }
    }

    private fun loadReportIndicators() {
        viewModelScope.launch(schedulerProvider.io) {
            val indicators = IndicatorRepository.reportIndicators
            updateUi { copy(availableParameters = indicators.flatMap { it.attributes }) }
        }
    }

    private fun restoreSelectedForms() {
        viewModelScope.launch {
            val ids = preferenceManager.loadSelectedForms(context)
            updateUi { copy(selectedFormIds = ids) }
        }
    }


    private fun updateFormCount() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.getFormCount().collect { result ->
                withContext(schedulerProvider.main) {
                    handleResult(
                        result = result, onSuccess = { formCount ->
                            updateUi { copy(formCount = formCount) }
                        }, errorMessage = (result as? Result.Error)?.message
                    )
                }

            }
        }
    }

    private fun updatePatientCount() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.getPatientCount().collect { result ->
                withContext(schedulerProvider.main) {
                    handleResult(
                        result = result, onSuccess = { patientCount ->
                            updateUi { copy(patientCount = patientCount) }
                        }, errorMessage = (result as? Result.Error)?.message
                    )

                }
            }
        }
    }


    private fun updateEncounterCount() {
        viewModelScope.launch(schedulerProvider.io) {
            syncUseCase.getEncounterCount().collect { result ->
                withContext(schedulerProvider.main) {
                    handleResult(
                        result = result, onSuccess = { encounterCount ->
                            updateUi { copy(encounterCount = encounterCount) }
                        }, errorMessage = (result as? Result.Error)?.message
                    )

                }
            }
        }
    }


    private fun buildReportRequest(cohort: Cohort, start: LocalDate, end: LocalDate) =
        ReportRequest(
            uuid = cohort.uuid,
            startDate = start.format(DateTimeFormatter.ISO_DATE),
            endDate = end.format(DateTimeFormatter.ISO_DATE),
            type = "cohort",
            reportCategory = ReportCategoryWrapper(ReportCategory.FACILITY, RenderType.JSON),
            reportIndicators = uiState.value.selectedParameters,
            reportType = ReportType.DYNAMIC,
            reportingCohort = CQIReportingCohort.PATIENTS_WITH_ENCOUNTERS
        )

    private fun buildDataDefinitionPayload(
        reportRequest: ReportRequest,
        indicator: Indicator,
    ) = DataDefinition(
        cohort = CohortResponse(
            type = reportRequest.type,
            clazz = "",
            uuid = reportRequest.uuid,
            name = "",
            description = "",
            parameters = listOf(
                Parameters(startdate = reportRequest.startDate, enddate = reportRequest.endDate)
            )
        ), columns = formatReportArray(indicator.attributes)
    )

    private fun validateFilters(): String? = when {
        uiState.value.selectedIndicator == null -> "Please select an indicator"
        uiState.value.selectedCohort == null -> "Please select a cohort"
        uiState.value.selectedDateRange == null -> "Please select a valid date range"
        else -> null
    }

    private fun updateUi(reducer: SyncUiState.() -> SyncUiState) {
        _uiState.update { it.reducer() }
    }

    private fun saveDownloadedForms(selectedForms: List<o3Form>) {
        viewModelScope.launch(schedulerProvider.io) {
            updateUi { copy(isLoading = true) }
            syncUseCase.saveFormsLocally(selectedForms).collect { result ->
                withContext(schedulerProvider.main) {
                    handleResult(
                        result = result,
                        onSuccess = {
                            clearSelection()
                            updateUi { copy(isLoading = false) }
                        },
                        successMessage = "Forms downloaded successfully",
                        errorMessage = (result as? Result.Error)?.message
                    )
                }
            }
        }
    }


    private fun restoreAutoSyncSettings() {
        viewModelScope.launch {
            val enabled = preferenceManager.loadAutoSyncEnabled()
            val interval = preferenceManager.loadAutoSyncInterval()
            updateUi { copy(autoSyncEnabled = enabled, autoSyncInterval = interval) }
        }
    }

    private fun handleToggleAutoSync(enabled: Boolean) {
        updateUi { copy(autoSyncEnabled = enabled) }
        viewModelScope.launch {
            preferenceManager.saveAutoSyncEnabled(enabled)
        }
    }

    private fun updateAutoSyncInterval(newInterval: String) {
        updateUi { copy(autoSyncInterval = newInterval) }
        viewModelScope.launch {
            preferenceManager.saveAutoSyncInterval(newInterval)
        }
    }

//    fun syncNow() {
//        viewModelScope.launch(schedulerProvider.io) {
//            updateUi { copy(isLoading = true) }
//            val syncResult = syncUseCase.syncNow()
//            withContext(schedulerProvider.main) {
//                updateUi {
//                    copy(
//                        isLoading = false,
//                        lastSyncTime = getCurrentFormattedTime(),
//                        lastSyncStatus = if (syncResult.isSuccess) "Success" else "Failed",
//                        lastSyncBy = "You",
//                        lastSyncError = if (syncResult.isSuccess) null else syncResult.errorMessage
//                    )
//                }
//            }
//        }
//    }

    private fun getCurrentFormattedTime(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }





}




