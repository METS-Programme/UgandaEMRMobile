package com.lyecdevelopers.worklist.domain.model

data class VisitSummary(
    val id: String,
    val patientId: String,
    val type: String,
    val date: String,
    val status: String,
    val notes: String,
    val encounters: List<Encounter>,
    val vitals: Vitals? = null,
)
