package com.lyecdevelopers.form.presentation.questionnaire

import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import com.lyecdevelopers.core._base.BaseViewModel
import com.lyecdevelopers.core.common.scheduler.SchedulerProvider
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.core.utils.AppLogger
import com.lyecdevelopers.form.domain.mapper.FormMapper
import com.lyecdevelopers.form.domain.usecase.FormsUseCase
import com.lyecdevelopers.form.presentation.questionnaire.event.QuestionnaireEvent
import com.lyecdevelopers.form.presentation.questionnaire.state.QuestionnaireState
import com.lyecdevelopers.form.utils.QuestionnaireUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import javax.inject.Inject

@HiltViewModel
class QuestionnaireViewModel @Inject constructor(
    private val formsUseCase: FormsUseCase,
    private val schedulerProvider: SchedulerProvider,
) : BaseViewModel() {

    private val _state = MutableStateFlow(QuestionnaireState(isLoading = true))
    val state: StateFlow<QuestionnaireState> = _state.asStateFlow()

    private var questionnaireResponse: QuestionnaireResponse? = null


    fun onEvent(event: QuestionnaireEvent) {
        when (event) {
            is QuestionnaireEvent.Load -> loadQuestionnaireByUuid("")
            is QuestionnaireEvent.LoadForEdit -> loadPatientForEdit(event.questionnaire)
            is QuestionnaireEvent.UpdateAnswer -> updateAnswer(event.linkId, event.answer)
            is QuestionnaireEvent.Reset -> reset()
            is QuestionnaireEvent.SubmitWithResponse -> handleSubmitWithResponse(event.questionnaireResponseJson)
        }
    }

    /**
     * Load a questionnaire using its UUID from formsUseCase.
     */
    fun loadQuestionnaireByUuid(uuid: String) {
        viewModelScope.launch(schedulerProvider.io) {
            formsUseCase.getLocalFormById(uuid).collect { result ->
                withContext(schedulerProvider.main) {

                    _state.update {
                        it.copy(
                            isLoading = true,
                        )
                    }

                    handleResult(
                        result = result,
                        onSuccess = { formEntity ->
                            try {
                                val questionnaire =
                                    formEntity?.let { FormMapper.toQuestionnaire(it) }
                                val questionnaireJson = FhirContext.forR4().newJsonParser()
                                    .encodeResourceToString(questionnaire)

                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        questionnaireJson = questionnaireJson,
                                        error = null
                                    )
                                }
                            } catch (e: Exception) {
                                AppLogger.e(
                                    "FormMapper",
                                    "Error parsing form to questionnaire: ${e.message}",
                                    e
                                )
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        error = "Failed to parse questionnaire: ${e.localizedMessage}"
                                    )
                                }
                            }
                        },
                        successMessage = "Successfully loaded form",
                        errorMessage = (result as? Result.Error)?.message
                    )

                    hideLoading()
                }
            }
        }
    }


    private fun loadPatientForEdit(questionnaire: Questionnaire) {


    }

    private fun updateAnswer(linkId: String, answer: Any?) {
        _state.update {
            it.copy(answers = it.answers.toMutableMap().apply { put(linkId, answer) })
        }

        questionnaireResponse?.let {
            QuestionnaireUtils.updateResponseItem(it, linkId, answer)
        }
    }


    private fun handleSubmitWithResponse(responseJson: String) {
        viewModelScope.launch(schedulerProvider.io) {
            _state.update { it.copy(isLoading = true) }
            try {
                val parser = FhirContext.forR4().newJsonParser()
                val response = parser.parseResource(QuestionnaireResponse::class.java, responseJson)
                questionnaireResponse = response


            } catch (e: Exception) {

            }
        }
    }

    private fun reset() {
        _state.value = QuestionnaireState()
    }


}






