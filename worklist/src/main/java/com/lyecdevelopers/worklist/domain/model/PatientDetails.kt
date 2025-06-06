package com.lyecdevelopers.worklist.domain.model

data class PatientDetails(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val vitals: Vitals?,
    val visitHistory: List<VisitSummary>,
    val currentVisit: VisitSummary? = null,
) {
    companion object {
        fun empty() = PatientDetails(
            id = "",
            name = "",
            age = 0,
            gender = "",
            vitals = null,
            visitHistory = emptyList(),
            currentVisit = null
        )
    }
}
