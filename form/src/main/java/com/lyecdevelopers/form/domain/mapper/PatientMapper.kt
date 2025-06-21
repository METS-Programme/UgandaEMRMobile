package com.lyecdevelopers.form.domain.mapper

import com.lyecdevelopers.core.data.local.entity.PatientEntity
import com.lyecdevelopers.core.model.VisitStatus
import org.hl7.fhir.r4.model.Address
import org.hl7.fhir.r4.model.ContactPoint
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Patient
import java.text.SimpleDateFormat
import java.util.Locale


fun Patient.toQuestionnaireAnswers(): Map<String, Any?> {
    return buildMap {
        this["first_name"] = nameFirst()
        this["last_name"] = nameLast()
        this["gender"] = gender?.toCode()
        this["birth_date"] = birthDateElement?.valueAsString
        this["nin"] = getIdentifier("http://health.go.ug/nin")

        address?.firstOrNull()?.let { addr ->
            this["village"] = addr.city
            this["parish"] = addr.district
            this["sub_county"] = addr.state
            this["district"] = addr.country
        }
    }
}

fun Map<String, Any?>.toPatient(existingId: String? = null): Patient {
    return Patient().apply {
        id = existingId

        val firstName = this@toPatient["first_name"] as? String
        val lastName = this@toPatient["last_name"] as? String

        if (!firstName.isNullOrBlank() || !lastName.isNullOrBlank()) {
            addName(
                HumanName().apply {
                    family = lastName
                    addGiven(firstName)
                    use = HumanName.NameUse.OFFICIAL
                })
        }

        gender = (this@toPatient["gender"] as? String)?.let {
            Enumerations.AdministrativeGender.fromCode(it)
        }

        birthDate = (this@toPatient["birth_date"] as? String)?.let {
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)
            } catch (e: Exception) {
                null
            }
        }

        val nin = this@toPatient["nin"] as? String
        if (!nin.isNullOrBlank()) {
            addIdentifier(
                Identifier().apply {
                    system = "http://health.go.ug/nin"
                    value = nin
                })
        }

        val address = Address().apply {
            city = this@toPatient["village"] as? String
            district = this@toPatient["parish"] as? String
            state = this@toPatient["sub_county"] as? String
            country = this@toPatient["district"] as? String
        }

        if (!address.isEmpty) addAddress(address)
    }
}

fun Patient.nameFirst(): String? = name.firstOrNull()?.given?.firstOrNull()?.value
fun Patient.nameLast(): String? = name.firstOrNull()?.family
fun Patient.getIdentifier(system: String): String? {
    return identifier.firstOrNull { it.system == system }?.value
}

fun Patient.toPatientEntity(
    visitHistory: String = "[]", // Optional: JSON-encoded list
    encounters: String = "[]",   // Optional: JSON-encoded list
): PatientEntity {
    val humanName = this.nameFirstRep
    val identifier = this.identifier.firstOrNull()?.value ?: this.idElement.idPart
    val phone =
        this.telecom.firstOrNull { it.system == ContactPoint.ContactPointSystem.PHONE }?.value
    val gender = this.gender?.toCode() ?: "unknown"
    val dob = this.birthDate?.toInstant()?.toString()?.substring(0, 10) ?: ""
    val address = this.addressFirstRep?.let {
        listOfNotNull(
            it.line.joinToString(", ") { line -> line.value }, it.city, it.district
        ).joinToString(", ")
    }

    return PatientEntity(
        id = this.idElement.idPart,
        patientIdentifier = identifier,
        firstName = humanName.given?.joinToString(" ") ?: "",
        lastName = humanName.family ?: "",
        gender = gender,
        dateOfBirth = dob,
        phoneNumber = phone,
        address = address,
        status = VisitStatus.PENDING,
        isSynced = false,
        visitHistory = visitHistory,
        encounters = encounters,
    )
}

