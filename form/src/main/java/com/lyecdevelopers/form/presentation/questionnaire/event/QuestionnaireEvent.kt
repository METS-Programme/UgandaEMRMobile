package com.lyecdevelopers.form.presentation.questionnaire.event

import org.hl7.fhir.r4.model.Patient

sealed class QuestionnaireEvent {
    object Load : QuestionnaireEvent()
    data class UpdateAnswer(val linkId: String, val answer: Any?) : QuestionnaireEvent()
    object Submit : QuestionnaireEvent()
    data class SubmitWithResponse(val questionnaireResponseJson: String) :
        QuestionnaireEvent()

    data class LoadForEdit(val patient: Patient) : QuestionnaireEvent()
    object Reset : QuestionnaireEvent()
}


