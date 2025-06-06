package com.lyecdevelopers.worklist.domain.model

data class PatientVisit(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val visitType: String,
    val status: VisitStatus,
    val scheduledTime: String, // or use `LocalTime`
)

enum class VisitStatus {
    PENDING, IN_PROGRESS, COMPLETED, FOLLOW_UP
}
