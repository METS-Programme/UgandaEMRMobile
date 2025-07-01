package com.lyecdevelopers.form.utils

import com.lyecdevelopers.form.domain.mapper.FormMapper.extractObsFromResponse
import com.lyecdevelopers.form.domain.model.OpenmrsEncounter
import org.hl7.fhir.r4.model.QuestionnaireResponse
import java.time.Instant

object EncounterExtensions {
    fun buildOpenmrsEncounter(
        response: QuestionnaireResponse,
        patientUuid: String,
        encounterTypeUuid: String,
        locationUuid: String,
        encounterDateTime: String = Instant.now().toString(),
    ): OpenmrsEncounter {
        val obs = extractObsFromResponse(response, patientUuid, encounterDateTime)
        return OpenmrsEncounter(
            patient = patientUuid,
            encounterType = encounterTypeUuid,
            location = locationUuid,
            encounterDatetime = encounterDateTime,
            obs = obs
        )
    }


    fun QuestionnaireResponse.toOpenmrsEncounter(
        patientUuid: String,
        encounterTypeUuid: String,
        locationUuid: String,
    ): OpenmrsEncounter = buildOpenmrsEncounter(this, patientUuid, encounterTypeUuid, locationUuid)


}