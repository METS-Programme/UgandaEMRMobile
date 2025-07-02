package com.lyecdevelopers.core.model.encounter

import com.lyecdevelopers.core.model.OpenmrsObs

data class EncounterPayload(
    val id: String,
    val visitId: String,
    val type: String,
    val date: String,
    val patientUuid: String,
    val locationUuid: String,
    val obs: List<OpenmrsObs>,
)

