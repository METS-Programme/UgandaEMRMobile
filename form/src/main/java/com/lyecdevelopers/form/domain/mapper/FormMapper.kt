package com.lyecdevelopers.form.domain.mapper

import com.lyecdevelopers.core.data.local.entity.FormEntity
import com.lyecdevelopers.core.model.FieldType
import org.hl7.fhir.r4.model.CodeType
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Questionnaire

object FormMapper {
    fun toQuestionnaire(form: FormEntity): Questionnaire {
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
                            FieldType.DATETIME -> Questionnaire.QuestionnaireItemType.DATETIME
                            FieldType.TEXT,
                            FieldType.TEXTAREA,
                                -> {
                                this.extension.add(
                                    Extension().apply {
                                        url =
                                            "http://hl7.org/fhir/StructureDefinition/questionnaire-itemControl"
                                        setValue(
                                            CodeableConcept().addCoding(
                                                Coding().apply {
                                                    system =
                                                        "http://hl7.org/fhir/questionnaire-item-control"
                                                    code = "text-box"
                                                    display = "text box"
                                                })
                                        )

                                    })


                                Questionnaire.QuestionnaireItemType.STRING
                            }
                            FieldType.DROPDOWN,
                            FieldType.SELECT,
                                -> {
                                extension.add(
                                    Extension().apply {
                                        url =
                                            "http://hl7.org/fhir/StructureDefinition/questionnaire-itemControl"
                                        setValue(
                                            CodeableConcept().addCoding(
                                                Coding().apply {
                                                    system =
                                                        "http://hl7.org/fhir/questionnaire-item-control"
                                                    code = "drop-down"
                                                    display = "Dropdown"
                                                })
                                        )

                                    },
                                )

                                // Add options
                                question.questionoptions.answers?.forEach { answer ->
                                    addAnswerOption(
                                        Questionnaire.QuestionnaireItemAnswerOptionComponent()
                                            .apply {
                                                value = Coding().apply {
                                                    code = answer.concept
                                                    display = answer.label
                                                }
                                            },
                                    )
                                }

                                Questionnaire.QuestionnaireItemType.CHOICE
                            }

                            FieldType.RADIO -> {
                                extension.add(
                                    Extension().apply {
                                        url =
                                            "http://hl7.org/fhir/StructureDefinition/questionnaire-itemControl"
                                        setValue(
                                            CodeableConcept().addCoding(
                                                Coding().apply {
                                                    system =
                                                        "http://hl7.org/fhir/questionnaire-item-control"
                                                    code = "radio-button"
                                                    display = "radio button"
                                                },
                                            )
                                        )
                                    },
                                )

                                extension.add(
                                    Extension().apply {
                                        url =
                                            "http://hl7.org/fhir/StructureDefinition/questionnaire-choiceOrientation"
                                        setValue(
                                            CodeType(
                                                "horizontal"
                                            )
                                        )

                                    },
                                )

                                // Add options
                                question.questionoptions.answers?.forEach { answer ->
                                    addAnswerOption(
                                        Questionnaire.QuestionnaireItemAnswerOptionComponent()
                                            .apply {
                                                value = Coding().apply {
                                                    code = answer.concept
                                                    display = answer.label
                                                }
                                            },
                                    )
                                }

                                Questionnaire.QuestionnaireItemType.CHOICE
                            }

                            FieldType.CHECKBOX -> {
                                extension.add(
                                    Extension().apply {
                                        url =
                                            "http://hl7.org/fhir/StructureDefinition/questionnaire-itemControl"
                                        setValue(
                                            CodeableConcept().addCoding(
                                                Coding().apply {
                                                    system =
                                                        "http://hl7.org/fhir/questionnaire-item-control"
                                                    code = "check-box"
                                                    display = "Checkbox"
                                                },
                                            )
                                        )
                                    },
                                )

                                // Add options
                                question.questionoptions.answers?.forEach { answer ->
                                    addAnswerOption(
                                        Questionnaire.QuestionnaireItemAnswerOptionComponent()
                                            .apply {
                                                value = Coding().apply {
                                                    code = answer.concept
                                                    display = answer.label
                                                }
                                            },
                                    )
                                }
                                Questionnaire.QuestionnaireItemType.CHOICE
                            }

                            FieldType.MULTI_CHECKBOX -> {
                                repeats = true
                                extension.add(
                                    Extension().apply {
                                        url =
                                            "http://hl7.org/fhir/StructureDefinition/questionnaire-itemControl"
                                        setValue(
                                            CodeableConcept().addCoding(
                                                Coding().apply {
                                                    system =
                                                        "http://hl7.org/fhir/questionnaire-item-control"
                                                    code = "check-box"
                                                    display = "Checkbox"
                                                },
                                            )
                                        )
                                    },
                                )

                                // Add answer options
                                question.questionoptions.answers?.forEach { answer ->
                                    addAnswerOption(
                                        Questionnaire.QuestionnaireItemAnswerOptionComponent()
                                            .apply {
                                                value = Coding().apply {
                                                    code = answer.concept
                                                    display = answer.label
                                                }
                                            },
                                    )
                                }

                                Questionnaire.QuestionnaireItemType.CHOICE
                            }


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
