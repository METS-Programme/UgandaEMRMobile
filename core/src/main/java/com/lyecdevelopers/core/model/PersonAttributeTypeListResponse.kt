package com.lyecdevelopers.core.model

import com.squareup.moshi.Json

data class PersonAttributeTypeListResponse(
    @Json(name = "results") val results: List<PersonAttributeType>,
)

data class PersonAttributeType(
    @Json(name = "uuid") val uuid: String,
    @Json(name = "display") val display: String,
    @Json(name = "links") val links: List<Links>,
)
