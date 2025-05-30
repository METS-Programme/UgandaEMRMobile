package com.lyecdevelopers.core.model
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Patient(
    val uuid: String,
    val name: String,
    val gender: String,
    val birthDate: String
)
