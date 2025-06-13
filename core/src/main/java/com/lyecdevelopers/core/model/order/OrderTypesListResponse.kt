package com.lyecdevelopers.core.model.order

import com.squareup.moshi.Json


data class OrderTypeListResponse(
    @Json(name = "results") val results: List<OrderType>,
)

data class OrderType(
    @Json(name = "uuid") val uuid: String,
    @Json(name = "display") val display: String,
    @Json(name = "name") val name: String,
)

