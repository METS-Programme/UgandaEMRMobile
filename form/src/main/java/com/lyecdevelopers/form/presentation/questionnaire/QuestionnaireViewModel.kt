package com.lyecdevelopers.form.presentation.questionnaire

import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.lyecdevelopers.core._base.BaseViewModel
import com.lyecdevelopers.core.common.scheduler.SchedulerProvider
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.core.utils.AppLogger
import com.lyecdevelopers.form.domain.mapper.FormMapper
import com.lyecdevelopers.form.domain.mapper.toPatient
import com.lyecdevelopers.form.domain.mapper.toPatientEntity
import com.lyecdevelopers.form.domain.mapper.toQuestionnaireAnswers
import com.lyecdevelopers.form.domain.usecase.FormsUseCase
import com.lyecdevelopers.form.presentation.questionnaire.event.QuestionnaireEvent
import com.lyecdevelopers.form.presentation.questionnaire.state.QuestionnaireState
import com.lyecdevelopers.form.utils.QuestionnaireResponseConverter
import com.lyecdevelopers.form.utils.QuestionnaireUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.QuestionnaireResponse
import javax.inject.Inject

@HiltViewModel
class QuestionnaireViewModel @Inject constructor(
    private val formsUseCase: FormsUseCase,
    private val schedulerProvider: SchedulerProvider,
    private val fhirEngine: FhirEngine,
) : BaseViewModel() {

    private val _state = MutableStateFlow(QuestionnaireState(isLoading = true))
    val state: StateFlow<QuestionnaireState> = _state.asStateFlow()

    private var questionnaireResponse: QuestionnaireResponse? = null


    fun onEvent(event: QuestionnaireEvent) {
        when (event) {
            is QuestionnaireEvent.Load -> loadQuestionnaireByUuid("")
            is QuestionnaireEvent.LoadForEdit -> loadPatientForEdit(event.patient)
            is QuestionnaireEvent.UpdateAnswer -> updateAnswer(event.linkId, event.answer)
            is QuestionnaireEvent.Submit -> submit()
            is QuestionnaireEvent.Reset -> reset()
            is QuestionnaireEvent.SubmitWithResponse -> handleSubmitWithResponse(event.questionnaireResponseJson)
        }
    }

    /**
     * Load a questionnaire using its UUID from formsUseCase.
     */
    fun loadQuestionnaireByUuid(uuid: String) {
        viewModelScope.launch(schedulerProvider.io) {
            formsUseCase.getO3FormByUuid(uuid).collect { result ->
                withContext(schedulerProvider.main) {
                    when (result) {
                        is Result.Success -> {
                            val form = result.data
                            try {
                                val questionnaire = FormMapper.toQuestionnaire(form)

                                val questionnaireJson = FhirContext.forR4().newJsonParser()
                                    .encodeResourceToString(questionnaire)

                                questionnaireResponse = QuestionnaireResponse()

                                _state.value = QuestionnaireState(
                                    isLoading = false, questionnaireJson = questionnaireJson
                                )
                            } catch (e: Exception) {
                                _state.value = QuestionnaireState(
                                    isLoading = false,
                                    error = e.localizedMessage ?: "Failed to parse form"
                                )
                            }
                        }

                        is Result.Error -> {
                            _state.value = QuestionnaireState(
                                isLoading = false, error = result.message
                            )
                        }

                        is Result.Loading -> {
                            _state.value = QuestionnaireState(isLoading = true)
                        }
                    }
                }
            }
        }
    }

    private fun loadPatientForEdit(patient: Patient) {
        val prefillAnswers = patient.toQuestionnaireAnswers()

        _state.value = _state.value.copy(
            isEditMode = true, patientId = patient.idElement.idPart, answers = prefillAnswers
        )
    }

    private fun updateAnswer(linkId: String, answer: Any?) {
        _state.update {
            it.copy(answers = it.answers.toMutableMap().apply { put(linkId, answer) })
        }

        questionnaireResponse?.let {
            QuestionnaireUtils.updateResponseItem(it, linkId, answer)
        }
    }

    private fun submit() {
        viewModelScope.launch(schedulerProvider.io) {
            _state.update { it.copy(isLoading = true) }
            withContext(schedulerProvider.main) {
                showLoading()
            }
            try {
                val patient = _state.value.answers.toPatient(_state.value.patientId)

                if (_state.value.isEditMode) {
                    fhirEngine.update(patient)
                } else {
                    fhirEngine.create(patient)
                }

                // ✅ Save to Room DB
                val entity = patient.toPatientEntity(
                    visitHistory = "[]", // You can pass real data here
                    encounters = "[]"
                )
//                patientsUseCase.saveLocallyOnly(entity)

                _state.update { it.copy(isLoading = false, isSubmitted = true) }

                withContext(schedulerProvider.main) {
                    hideLoading()
                }
                navigate("worklist_main")

            } catch (e: Exception) {
                hideLoading()
                AppLogger.e("RegisterPatient", "Failed to save patient: ${e.localizedMessage}")
                _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }


    private fun handleSubmitWithResponse(responseJson: String) {
        viewModelScope.launch(schedulerProvider.io) {
            _state.update { it.copy(isLoading = true) }
            withContext(schedulerProvider.main) {
                showLoading()
            }
            try {
                val parser = FhirContext.forR4().newJsonParser()
                val response = parser.parseResource(QuestionnaireResponse::class.java, responseJson)
                questionnaireResponse = response

                val patient = QuestionnaireResponseConverter.toPatient(response)

                if (_state.value.isEditMode) {
                    fhirEngine.update(patient)
                } else {
                    fhirEngine.create(patient)
                }

                // ✅ Save to Room DB
                val entity = patient.toPatientEntity(
                    visitHistory = "[]", // You can pass real data here
                    encounters = "[]"
                )

//                patientsUseCase.saveLocallyOnly(entity)
                _state.update { it.copy(isLoading = false, isSubmitted = true) }
                withContext(schedulerProvider.main) {
                    hideLoading()
                }
                navigate("worklist_main")

            } catch (e: Exception) {
                withContext(schedulerProvider.main) {
                    hideLoading()
                }
                AppLogger.e("RegisterPatient", "Submit failed: ${e.localizedMessage}")
                _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    private fun reset() {
        _state.value = QuestionnaireState()
        viewModelScope.launch {}
    }

    fun getQuestionnaireResponse(): QuestionnaireResponse? = questionnaireResponse

}






