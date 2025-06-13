package com.lyecdevelopers.core.model.encounter

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

