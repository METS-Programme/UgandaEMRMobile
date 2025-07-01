package com.lyecdevelopers.form.domain.model

data class OpenmrsObs(
    val person: String,
    val concept: String,
    val obsDatetime: String,
    val value: Any,
)
