package com.lyecdevelopers.form.presentation.questionnaire

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.lyecdevelopers.core.common.scheduler.SchedulerProvider
import com.lyecdevelopers.core.model.Result
import com.lyecdevelopers.form.domain.mapper.FormMapper
import com.lyecdevelopers.form.domain.usecase.FormsUseCase
import com.lyecdevelopers.form.presentation.state.QuestionnaireState
import com.lyecdevelopers.form.utils.QuestionnaireResponseConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.QuestionnaireResponse
import javax.inject.Inject

@HiltViewModel
class QuestionnaireViewModel @Inject constructor(
    private var fhirEngine: FhirEngine,
    private val formsUseCase: FormsUseCase,
    private val schedulerProvider: SchedulerProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(QuestionnaireState(isLoading = true))
    val state: StateFlow<QuestionnaireState> = _state.asStateFlow()

    private var questionnaireResponse: QuestionnaireResponse? = null

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


    fun savePatient(response: QuestionnaireResponse) {
        viewModelScope.launch {
            try {
                val patient = QuestionnaireResponseConverter.toPatient(response)
                fhirEngine.create(patient)
            } catch (e: Exception) {
                Log.e("RegisterPatient", "Failed to save patient: ${e.localizedMessage}")
            }
        }
    }

    fun getQuestionnaireResponse(): QuestionnaireResponse? = questionnaireResponse
}






