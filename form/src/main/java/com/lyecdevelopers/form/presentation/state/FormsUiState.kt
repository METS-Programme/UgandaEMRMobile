package com.lyecdevelopers.form.presentation.state

import com.lyecdevelopers.core.model.Form


data class FormsUiState(
    val isLoading: Boolean = false,
    val allForms: List<Form> = emptyList(),
    val filteredForms: List<Form> = emptyList(),
    val selectedForm: Form? = null,
    val searchQuery: String = "",
    val errorMessage: String? = null,        // Add error display support
    val isEmpty: Boolean = false,
)
