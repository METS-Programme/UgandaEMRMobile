package com.lyecdevelopers.form.domain.mapper

import com.lyecdevelopers.core.model.FieldType
import com.lyecdevelopers.core.model.o3.o3Form
import org.hl7.fhir.r4.model.Questionnaire

object FormMapper {
    fun toQuestionnaire(form: o3Form): Questionnaire {
        val questionnaire = Questionnaire().apply {
            id = form.uuid
            title = form.name
            description = form.description
            version = form.version
        }

        form.pages?.forEach { page ->
            val pageGroup = Questionnaire.QuestionnaireItemComponent().apply {
                linkId = page.label
                text = page.label
                type = Questionnaire.QuestionnaireItemType.GROUP
            }

            page.sections.forEach { section ->
                val sectionGroup = Questionnaire.QuestionnaireItemComponent().apply {
                    linkId = section.label
                    text = section.label
                    type = Questionnaire.QuestionnaireItemType.GROUP
                }

                section.questions.forEach { question ->
                    val item = Questionnaire.QuestionnaireItemComponent().apply {
                        linkId = question.id ?: question.label
                        text = question.label
                        type = when (question.questionoptions.rendering) {
                            FieldType.NUMBER -> Questionnaire.QuestionnaireItemType.DECIMAL
                            FieldType.DATE -> Questionnaire.QuestionnaireItemType.DATE
                            FieldType.TEXT -> Questionnaire.QuestionnaireItemType.STRING
                            FieldType.TEXTAREA -> Questionnaire.QuestionnaireItemType.STRING
                            FieldType.DATETIME -> Questionnaire.QuestionnaireItemType.DATE
                            FieldType.DROPDOWN,
                            FieldType.SELECT,
                            FieldType.RADIO,
                                -> Questionnaire.QuestionnaireItemType.CHOICE

                            FieldType.CHECKBOX,
                            FieldType.MULTI_CHECKBOX,
                                -> Questionnaire.QuestionnaireItemType.GROUP

                            else -> Questionnaire.QuestionnaireItemType.STRING
                        }
                    }


                    sectionGroup.addItem(item)
                }

                pageGroup.addItem(sectionGroup)
            }

            questionnaire.addItem(pageGroup)
        }

        return questionnaire
    }
}
