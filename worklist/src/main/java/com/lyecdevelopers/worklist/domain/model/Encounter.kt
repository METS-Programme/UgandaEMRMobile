package com.lyecdevelopers.worklist.domain.model

data class Encounter(
    val id: String,
    val type: String, // e.g., "Vitals", "Lab Results"
    val date: String,
    val observations: List<String> = emptyList(),
)
