package com.lyecdevelopers.form.presentation.registration

import android.content.Context
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.FhirEngine
import com.lyecdevelopers.core._base.BaseViewModel
import com.lyecdevelopers.core.common.scheduler.SchedulerProvider
import com.lyecdevelopers.core.utils.AppLogger
import com.lyecdevelopers.form.presentation.state.QuestionnaireState
import com.lyecdevelopers.form.utils.QuestionnaireResponseConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import javax.inject.Inject


@HiltViewModel
class RegisterPatientViewModel @Inject constructor(
    private var fhirEngine: FhirEngine,
    private val schedulerProvider: SchedulerProvider,
    @ApplicationContext private val context: Context,
) : BaseViewModel() {

    private val _state = MutableStateFlow(QuestionnaireState())
    val state: StateFlow<QuestionnaireState> = _state.asStateFlow()

    private lateinit var questionnaire: Questionnaire
    private var questionnaireResponse: QuestionnaireResponse? = null

    /**
     * Load questionnaire JSON from assets and parse it.
     */
    fun loadRegisterPatientQuestionnaireFromAssets(filename: String = "questionnaires/register-patient-questionnaire.json") {
        viewModelScope.launch(schedulerProvider.io) {
            _state.value = QuestionnaireState(isLoading = true)
            try {
                val json = context.assets.open(filename).bufferedReader().use { it.readText() }
                loadRegisterPatientQuestionnaireFromJson(json)
            } catch (e: Exception) {
                _state.value = QuestionnaireState(
                    isLoading = false, error = "Failed to load form: ${e.localizedMessage}"

                )
                AppLogger.e("FHIR_PARSE_ERROR", e.message ?: "Unknown error", e)

            }
        }
    }

    /**
     * Parse a JSON string into a FHIR Questionnaire.
     */
    private fun loadRegisterPatientQuestionnaireFromJson(questionnaireJson: String) {
        try {
            val fhirContext = FhirContext.forR4()
            val parser = fhirContext.newJsonParser()

            questionnaire = parser.parseResource(Questionnaire::class.java, questionnaireJson)
            questionnaireResponse = QuestionnaireResponse()

            AppLogger.d("FHIR_PARSE" + parser.encodeResourceToString(questionnaire))

            _state.value = QuestionnaireState(
                isLoading = false, questionnaireJson = parser.encodeResourceToString(questionnaire)
            )
        } catch (e: Exception) {
            AppLogger.e("FHIR_PARSE_ERROR", e.message ?: "Unknown error", e)
            _state.value = QuestionnaireState(
                isLoading = false, error = e.localizedMessage
            )
        }
    }


    fun savePatient(response: QuestionnaireResponse) {
        viewModelScope.launch {
            try {
                val patient = QuestionnaireResponseConverter.toPatient(response)
                fhirEngine.create(patient)
            } catch (e: Exception) {
                AppLogger.e("RegisterPatient", "Failed to save patient: ${e.localizedMessage}")
            }
        }
    }

    fun getQuestionnaireResponse(): QuestionnaireResponse? = questionnaireResponse
}
