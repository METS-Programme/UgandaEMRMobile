package com.lyecdevelopers.core.model

import com.squareup.moshi.Json

data class IdentifierListResponse(
    @Json(name = "results") val results: List<Identifier>,
)

data class Identifier(
    @Json(name = "uuid") val uuid: String,
    @Json(name = "display") val display: String,
    @Json(name = "links") val links: List<Links>,
)


