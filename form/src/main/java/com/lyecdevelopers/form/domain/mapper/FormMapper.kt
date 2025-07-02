package com.lyecdevelopers.form.domain.mapper

import com.lyecdevelopers.core.BuildConfig
import com.lyecdevelopers.core.model.FieldType
import com.lyecdevelopers.core.model.o3.o3Form
import com.lyecdevelopers.core.utils.AppLogger
import com.lyecdevelopers.form.domain.model.OpenmrsObs
import com.lyecdevelopers.form.utils.FhirExtensions
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.UriType
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
                linkId = "page-${pageIndex + 1}"
                type = Questionnaire.QuestionnaireItemType.GROUP
                text = page.label
                extension.add(FhirExtensions.pageItemControlExtension())
            }

            page.sections.forEachIndexed { sectionIndex, section ->
                val sectionHeading = Questionnaire.QuestionnaireItemComponent().apply {
                    linkId = "page-${pageIndex + 1}.section-${sectionIndex + 1}"
                    type = Questionnaire.QuestionnaireItemType.DISPLAY
                    text = section.label
                }
                pageGroup.addItem(sectionHeading)

                section.questions.forEachIndexed { questionIndex, question ->
                    val questionItem = Questionnaire.QuestionnaireItemComponent().apply {
                        linkId =
                            "page-${pageIndex + 1}.section-${sectionIndex + 1}.q-${questionIndex + 1}"
                        type = FhirExtensions.mapFieldType(question.questionoptions.rendering)
                        text = question.label
                        required = question.required?.toBooleanStrictOrNull() ?: false
                        repeats = when (question.questionoptions.rendering) {
                            FieldType.MULTI_CHECKBOX -> true
                            else -> false
                        }

                        FhirExtensions.addItemControlExtension(
                            this, question.questionoptions.rendering
                        )

                        val conceptUuid =
                            question.questionoptions.concept?.takeIf { it.isNotBlank() }

                        if (conceptUuid != null) {
                            definition = "${BuildConfig.API_BASE_URL}concept#$conceptUuid"

                            addCode(
                                Coding().apply {
                                    system = "${BuildConfig.API_BASE_URL}concept"
                                    code = conceptUuid
                                    display = question.label
                                })

                            extension.add(
                                Extension().apply {
                                    url = "${BuildConfig.API_BASE_URL}concept"
                                    setValue(UriType("${BuildConfig.API_BASE_URL}concept#$conceptUuid"))
                                })

                            AppLogger.d("Mapped question '${question.label}' with concept: $conceptUuid")
                        } else {
                            AppLogger.w("Question '${question.id}' has no concept UUID. Skipping concept metadata.")
                        }

                        question.questionoptions.answers?.forEach { ans ->
                            ans.concept?.takeIf { it.isNotBlank() }?.let { ansConcept ->
                                addAnswerOption(
                                    Questionnaire.QuestionnaireItemAnswerOptionComponent().apply {
                                        value = Coding().apply {
                                            system = "${BuildConfig.API_BASE_URL}concept"
                                            code = ansConcept
                                            display = ans.label
                                        }
                                    })
                            }
                                ?: AppLogger.w("Answer '${ans.label}' for question '${question.id}' has no concept.")
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
        questionnaireItems: List<Questionnaire.QuestionnaireItemComponent>,
        patientUuid: String,
        encounterDateTime: String = Instant.now().toString(),
    ): List<OpenmrsObs> {

        val obsList = mutableListOf<OpenmrsObs>()

        fun processItems(items: List<QuestionnaireResponse.QuestionnaireResponseItemComponent>) {
            for (item in items) {

                val matchingQuestionItem =
                    findQuestionnaireItemByLinkId(item.linkId, questionnaireItems)

                if (matchingQuestionItem == null) {
                    println("Warning: No matching Questionnaire item for linkId '${item.linkId}'. Skipping.")
                    if (item.hasItem()) processItems(item.item)
                    continue
                }

                // ✅ Resolve concept from Questionnaire definition, code or extension:
                val questionConceptIdentifier = matchingQuestionItem.code.firstOrNull()?.code
                    ?: matchingQuestionItem.extension.find {
                        it.url == "${BuildConfig.API_BASE_URL}concept"
                    }?.value?.primitiveValue()
                    ?: matchingQuestionItem.definition?.substringAfterLast("#")

                if (questionConceptIdentifier.isNullOrBlank()) {
                    println("Warning: Questionnaire item for linkId '${item.linkId}' has no concept. Skipping.")
                    if (item.hasItem()) processItems(item.item)
                    continue
                }

                if (item.hasAnswer()) {
                    item.answer.forEach { answer ->
                        val obsConcept: String
                        val obsValue: Any

                        when {
                            answer.hasValueCoding() -> {
                                val codedValueCode = answer.valueCoding.code
                                if (codedValueCode == null) {
                                    println("Warning: Coded answer for linkId '${item.linkId}' has null code. Skipping.")
                                    return@forEach
                                }
                                obsConcept = codedValueCode
                                obsValue = answer.valueCoding.display ?: codedValueCode
                            }
                            answer.hasValueStringType() -> {
                                obsConcept = questionConceptIdentifier
                                obsValue = answer.valueStringType.value
                            }
                            answer.hasValueDateType() -> {
                                obsConcept = questionConceptIdentifier
                                obsValue = answer.valueDateType.valueAsString
                            }
                            answer.hasValueIntegerType() -> {
                                obsConcept = questionConceptIdentifier
                                obsValue = answer.valueIntegerType.value
                            }
                            answer.hasValueDecimalType() -> {
                                obsConcept = questionConceptIdentifier
                                obsValue = answer.valueDecimalType.value
                            }
                            answer.hasValueBooleanType() -> {
                                obsConcept = questionConceptIdentifier
                                obsValue = answer.valueBooleanType.value
                            }
                            answer.hasValueDateTimeType() -> {
                                obsConcept = questionConceptIdentifier
                                obsValue = answer.valueDateTimeType.valueAsString
                            }
                            else -> {
                                println("Warning: Unsupported answer type for linkId '${item.linkId}'. Skipping.")
                                return@forEach
                            }
                        }

                        obsList.add(
                            OpenmrsObs(
                                person = patientUuid,
                                concept = obsConcept,
                                obsDatetime = encounterDateTime,
                                value = obsValue,
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

    /**
     * Finds a Questionnaire item by linkId (recursive).
     */
    fun findQuestionnaireItemByLinkId(
        linkId: String,
        items: List<Questionnaire.QuestionnaireItemComponent>,
    ): Questionnaire.QuestionnaireItemComponent? {
        for (item in items) {
            if (item.linkId == linkId) {
                return item
            }
            val found = findQuestionnaireItemByLinkId(linkId, item.item)
            if (found != null) return found
        }
        return null
    }


}



