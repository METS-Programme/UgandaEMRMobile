package com.lyecdevelopers.core.data.local.mapper

import com.lyecdevelopers.core.data.local.entity.PatientEntity
import com.lyecdevelopers.core.model.Patient


fun PatientEntity.toDomain(): Patient {
    return Patient(
        uuid = TODO(),
        name = TODO(),
        gender = TODO(),
        birthDate = TODO()
    )
}

fun Patient.toEntity(): PatientEntity {
    return PatientEntity(
        id = TODO(),
        firstName = TODO(),
        lastName = TODO(),
        gender = TODO(),
        dateOfBirth = TODO(),
        phoneNumber = TODO(),
        address = TODO(),
        isSynced = TODO()
    )
}

