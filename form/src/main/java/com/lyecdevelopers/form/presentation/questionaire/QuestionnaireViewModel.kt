package com.lyecdevelopers.form.presentation.questionaire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.lyecdevelopers.core.common.scheduler.SchedulerProvider
import com.lyecdevelopers.form.presentation.state.QuestionnaireState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import javax.inject.Inject

@HiltViewModel
class QuestionnaireViewModel @Inject constructor(
    private val fhirEngine: FhirEngine,
    private val schedulerProvider: SchedulerProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(QuestionnaireState(isLoading = true))
    val state: StateFlow<QuestionnaireState> = _state.asStateFlow()

    private lateinit var questionnaire: Questionnaire
    private var questionnaireResponse: QuestionnaireResponse? = null

    // Load questionnaire JSON string and parse it
    fun loadQuestionnaire(questionnaireJson: String) {
        viewModelScope.launch(schedulerProvider.io) {
            try {
                val parser = FhirContext.forR4().newJsonParser()
                questionnaire = parser.parseResource(Questionnaire::class.java, questionnaireJson)
                questionnaireResponse = QuestionnaireResponse()

                _state.value = QuestionnaireState(
                    isLoading = false, questionnaire = questionnaire
                )
            } catch (e: Exception) {
                // handle error (you can expand the state with error message)
                _state.value = QuestionnaireState(
                    isLoading = false, errorMessage = e.localizedMessage
                )
            }
        }
    }

    fun saveQuestionnaireResponse(response: QuestionnaireResponse) {
        viewModelScope.launch(schedulerProvider.io) {
            fhirEngine.create(response)
        }
    }

    fun getQuestionnaireResponse(): QuestionnaireResponse? = questionnaireResponse
}




