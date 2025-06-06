package com.lyecdevelopers.form.presentation.state

import org.hl7.fhir.r4.model.Questionnaire

data class QuestionnaireState(
    val isLoading: Boolean = false,
    val questionnaire: Questionnaire? = null,
    val errorMessage: String? = null,
)