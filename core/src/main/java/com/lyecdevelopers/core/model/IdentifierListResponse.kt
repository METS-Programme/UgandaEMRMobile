package com.lyecdevelopers.core.model

import com.lyecdevelopers.core.model.cohort.Attribute
import com.squareup.moshi.Json

data class IdentifierListResponse(
    @Json(name = "results") val results: List<Identifier>,
)

data class Identifier(
    @Json(name = "uuid") val uuid: String,
    @Json(name = "display") val display: String,
    @Json(name = "links") val links: List<Links>,
)

fun Identifier.toAttribute(): Attribute {
    return Attribute(
        id = this.uuid,
        label = this.display,
        type = "default",
        modifier = 0,
        showModifierPanel = false,
        extras = emptyList(),
        attributes = emptyList()
    )
}

fun Attribute.toIdentifier(): Identifier {
    return Identifier(
        uuid = this.id, display = this.label, links = emptyList()
    )
}

fun List<Identifier>.toAttributes(): List<Attribute> = map { it.toAttribute() }

fun List<Attribute>.toIdentifiers(): List<Identifier> = map { it.toIdentifier() }


