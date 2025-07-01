package com.lyecdevelopers.form.domain.mapper

import com.lyecdevelopers.core.model.FieldType
import com.lyecdevelopers.core.model.o3.o3Form
import com.lyecdevelopers.form.domain.model.OpenmrsObs
import com.lyecdevelopers.form.utils.FhirExtensions
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import java.time.Instant

object FormMapper {

    fun toQuestionnaire(form: o3Form): Questionnaire {
        val questionnaire = Questionnaire().apply {
            id = form.uuid
            title = form.name
            description = form.description
            version = form.version
        }

        form.pages?.forEachIndexed { pageIndex, page ->
            val pageGroup = Questionnaire.QuestionnaireItemComponent().apply {
                linkId = "${pageIndex + 1}"
                type = Questionnaire.QuestionnaireItemType.GROUP
                text = page.label
                extension.add(FhirExtensions.pageItemControlExtension())
            }

            page.sections.forEachIndexed { sectionIndex, section ->
                // Section header as display
                val sectionHeading = Questionnaire.QuestionnaireItemComponent().apply {
                    linkId = "${pageIndex + 1}.${sectionIndex + 1}"
                    type = Questionnaire.QuestionnaireItemType.DISPLAY
                    text = section.label
                }
                pageGroup.addItem(sectionHeading)

                section.questions.forEachIndexed { questionIndex, question ->
                    val questionItem = Questionnaire.QuestionnaireItemComponent().apply {
                        linkId = "${pageIndex + 1}.${sectionIndex + 1}.${questionIndex + 1}"
                        type = FhirExtensions.mapFieldType(question.questionoptions.rendering)
                        text = question.label
                        required = question.required.toBooleanStrict()
                        repeats = when (question.questionoptions.rendering) {
                            FieldType.MULTI_CHECKBOX -> true
                            else -> false
                        }
                        FhirExtensions.addItemControlExtension(
                            this, question.questionoptions.rendering
                        )

                        if (!question.questionoptions.answers.isNullOrEmpty()) {
                            question.questionoptions.answers?.forEach { ans ->
                                addAnswerOption(
                                    Questionnaire.QuestionnaireItemAnswerOptionComponent().apply {
                                        value = Coding().apply {
                                            code = ans.concept
                                            display = ans.label
                                        }
                                    })
                            }
                        }
                    }

                    pageGroup.addItem(questionItem)
                }
            }

            questionnaire.addItem(pageGroup)
        }

        return questionnaire
    }


    fun extractObsFromResponse(
        response: QuestionnaireResponse,
        patientUuid: String,
        encounterDateTime: String = Instant.now().toString(),
    ): List<OpenmrsObs> {

        val obsList = mutableListOf<OpenmrsObs>()

        fun processItems(items: List<QuestionnaireResponse.QuestionnaireResponseItemComponent>) {
            for (item in items) {
                if (item.hasAnswer()) {
                    item.answer.forEach { answer ->
                        val concept: String
                        val value: Any

                        when {
                            // ✅ For coded answers, use the selected option’s code
                            answer.hasValueCoding() -> {
                                concept = answer.valueCoding.code
                                value = answer.valueCoding.display ?: answer.valueCoding.code
                            }

                            // ✅ For primitive answers, use the question’s linkId as concept
                            answer.hasValueStringType() -> {
                                concept = item.linkId
                                value = answer.valueStringType.value
                            }

                            answer.hasValueDateType() -> {
                                concept = item.linkId
                                value = answer.valueDateType.valueAsString
                            }

                            answer.hasValueIntegerType() -> {
                                concept = item.linkId
                                value = answer.valueIntegerType.value
                            }

                            answer.hasValueDecimalType() -> {
                                concept = item.linkId
                                value = answer.valueDecimalType.value
                            }

                            answer.hasValueBooleanType() -> {
                                concept = item.linkId
                                value = answer.valueBooleanType.value
                            }

                            answer.hasValueDateTimeType() -> {
                                concept = item.linkId
                                value = answer.valueDateTimeType.valueAsString
                            }

                            else -> {
                                // Skip unsupported answer type
                                return@forEach
                            }
                        }

                        obsList.add(
                            OpenmrsObs(
                                person = patientUuid,
                                concept = concept,
                                obsDatetime = encounterDateTime,
                                value = value
                            )
                        )
                    }
                }

                if (item.hasItem()) {
                    processItems(item.item)
                }
            }
        }

        processItems(response.item)
        return obsList
    }


    fun String?.toBooleanStrict(): Boolean = this?.equals("true", ignoreCase = true) == true


}



