package com.lyecdevelopers.form.presentation.event

import com.lyecdevelopers.core.model.Form


sealed class FormsEvent {
    object LoadForms : FormsEvent()
    data class SelectForm(val form: Form) : FormsEvent()
    data class SearchQueryChanged(val query: String) : FormsEvent()
}
