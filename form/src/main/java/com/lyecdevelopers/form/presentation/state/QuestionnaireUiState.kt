package com.lyecdevelopers.form.presentation.state

data class QuestionnaireState(
    val isLoading: Boolean = false,
    val questionnaireJson: String? = null,
    val error: String? = null,
)