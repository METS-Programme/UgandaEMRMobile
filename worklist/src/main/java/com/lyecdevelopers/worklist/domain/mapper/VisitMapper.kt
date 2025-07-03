package com.lyecdevelopers.worklist.domain.mapper

import com.lyecdevelopers.core.data.local.entity.EncounterEntity
import com.lyecdevelopers.core.data.local.entity.VisitSummaryEntity
import com.lyecdevelopers.core.data.local.entity.VitalsEntity
import com.lyecdevelopers.worklist.domain.model.Encounter
import com.lyecdevelopers.worklist.domain.model.VisitSummary
import com.lyecdevelopers.worklist.domain.model.Vitals
import java.time.Instant

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
fun EncounterEntity.toDomain(): Encounter = Encounter(
    uuid = id,
    encounterTypeUuid = encounterTypeUuid,
    encounterDatetime = Instant.parse(encounterDatetime),
    patientUuid = patientUuid,
    locationUuid = locationUuid,
    providerUuid = providerUuid,
    obs = obs,
    orders = orders,
    formUuid = formUuid,
    visitUuid = visitUuid,
    voided = voided,
    synced = synced,
    createdAt = Instant.parse(createdAt)
)


fun Encounter.toEntity(): EncounterEntity = EncounterEntity(
    id = uuid,
    encounterTypeUuid = encounterTypeUuid,
    encounterDatetime = encounterDatetime.toString(),
    patientUuid = patientUuid,
    locationUuid = locationUuid,
    providerUuid = providerUuid,
    obs = obs,
    orders = orders,
    formUuid = formUuid,
    visitUuid = visitUuid,
    voided = voided,
    synced = synced,
    createdAt = createdAt.toString()
)


// Vitals
fun VitalsEntity.toDomain(): Vitals = Vitals(bloodPressure, pulse, temperature)

fun Vitals.toEntity(visitUuid: String): VitalsEntity = VitalsEntity(
    visitUuid = visitUuid, temperature = temperature, pulse = 0, bloodPressure = bloodPressure
)
