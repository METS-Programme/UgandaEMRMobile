package com.lyecdevelopers.form.utils

import com.lyecdevelopers.form.domain.mapper.FormMapper.extractObsFromResponse
import com.lyecdevelopers.form.domain.model.OpenmrsEncounter
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import java.time.Instant

object EncounterExtensions {

    fun buildOpenmrsEncounter(
        response: QuestionnaireResponse,
        questionnaireItems: List<Questionnaire.QuestionnaireItemComponent>,
        patientUuid: String,
        encounterTypeUuid: String,
        locationUuid: String,
        encounterDateTime: String = Instant.now().toString(),
    ): OpenmrsEncounter {
        val obs = extractObsFromResponse(
            response = response,
            questionnaireItems = questionnaireItems,
            patientUuid = patientUuid,
            encounterDateTime = encounterDateTime
        )
        return OpenmrsEncounter(
            patient = patientUuid,
            encounterType = encounterTypeUuid,
            location = locationUuid,
            encounterDatetime = encounterDateTime,
            obs = obs
        )
    }

    fun QuestionnaireResponse.toOpenmrsEncounter(
        questionnaireItems: List<Questionnaire.QuestionnaireItemComponent>,
        patientUuid: String,
        encounterTypeUuid: String,
        locationUuid: String,
    ): OpenmrsEncounter = buildOpenmrsEncounter(
        this, questionnaireItems, patientUuid, encounterTypeUuid, locationUuid
    )

}
