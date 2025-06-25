package com.lyecdevelopers.worklist.domain.mapper

import com.lyecdevelopers.core.data.local.entity.EncounterEntity
import com.lyecdevelopers.core.data.local.entity.VisitSummaryEntity
import com.lyecdevelopers.core.data.local.entity.VitalsEntity
import com.lyecdevelopers.worklist.domain.model.Encounter
import com.lyecdevelopers.worklist.domain.model.VisitSummary
import com.lyecdevelopers.worklist.domain.model.Vitals

fun VisitSummaryEntity.toDomain(
    encounters: List<EncounterEntity>,
    vitals: VitalsEntity?,
): VisitSummary = VisitSummary(
    id = id,
    type = type,
    date = date,
    status = status,
    notes = notes,
    encounters = encounters.map { it.toDomain() },
    vitals = vitals?.toDomain(),
    patientId = patientId
)

fun VisitSummary.toEntity(patientId: String): VisitSummaryEntity = VisitSummaryEntity(
    id = id, type = type, date = date, status = status, notes = notes, patientId = patientId
)

// Encounters
fun EncounterEntity.toDomain(): Encounter = Encounter(id, type, date)

fun Encounter.toEntity(visitId: String): EncounterEntity = EncounterEntity(id, visitId, type, date)

// Vitals
fun VitalsEntity.toDomain(): Vitals = Vitals(bloodPressure, pulse, temperature)

fun Vitals.toEntity(visitId: String): VitalsEntity = VitalsEntity(
    visitId = visitId, temperature = temperature, pulse = 0, bloodPressure = bloodPressure
)
