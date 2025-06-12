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
import com.lyecdevelopers.core.model.o3.o3Form
import com.lyecdevelopers.core.utils.AppLogger
import com.lyecdevelopers.sync.domain.usecase.SyncUseCase
import com.lyecdevelopers.sync.presentation.forms.event.DownloadFormsUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncUseCase: SyncUseCase,
    private val schedulerProvider: SchedulerProvider,
    private val preferenceManager: PreferenceManager,
    private val context: Application,
) : ViewModel() {

    private val _formItems = mutableStateListOf<Form>()
    val formItems: List<Form> get() = _formItems

    private val _selectedFormIds = mutableStateOf(setOf<String>())
    val selectedFormIds: State<Set<String>> get() = _selectedFormIds

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> get() = _searchQuery

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> get() = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> get() = _error

    // 👇 UI Event Flow
    private val _uiEvent = MutableSharedFlow<DownloadFormsUiEvent>()
    val uiEvent: SharedFlow<DownloadFormsUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadForms()
        viewModelScope.launch {
            _selectedFormIds.value = preferenceManager.loadSelectedForms(context)
        }

        AppLogger.d("formItems$formItems")
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
}


/*
*
* is Result.Success -> {
                        val detailedForm = result.data
                        // Save the successful form locally
                        when (val saveResult = syncUseCase.saveFormLocally(detailedForm)) {
                            is Result.Success -> downloadedForms.add(detailedForm)
                            is Result.Error -> {
                                _uiEvent.emit(
                                    DownloadFormsUiEvent.ShowSnackbar(
                                        "Failed to save form ${form.name ?: form.uuid}: ${saveResult.message}"
                                    )
                                )
                            }

                            else -> Unit
                        }
                    }

                    is Result.Error -> {
                        _uiEvent.emit(
                            DownloadFormsUiEvent.ShowSnackbar(
                                "Failed to fetch form ${form.name ?: form.uuid}: ${result.message}"
                            )
                        )
                    }

                    else -> Unit
                    * */