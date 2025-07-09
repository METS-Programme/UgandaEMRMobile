package com.lyecdevelopers.core.model.encounter

import com.lyecdevelopers.core.model.cohort.Attribute
import com.squareup.moshi.Json

data class EncounterTypeListResponse(
    @Json(name = "results") val results: List<EncounterType>,
)

data class EncounterType(
    @Json(name = "uuid") val uuid: String,
    @Json(name = "display") val display: String,
    @Json(name = "links") val links: List<Links>,
)

data class Links(
    @Json(name = "rel") val rel: String,
    @Json(name = "uri") val uri: String,
    @Json(name = "resourceAlias") val resourcealias: String,
)

fun EncounterType.toAttribute(): Attribute = Attribute(
    id = uuid,
    label = display,
    type = "EncounterType",
    modifier = 0,
    showModifierPanel = false,
    extras = emptyList(),
    attributes = emptyList()
)

fun Attribute.toEncounterType(): EncounterType = EncounterType(
    uuid = id,
    display = label,
    links = emptyList()
)

fun List<EncounterType>.toAttributes(): List<Attribute> =
    map { it.toAttribute() }

fun List<Attribute>.toEncounterTypes(): List<EncounterType> =
    map { it.toEncounterType() }
