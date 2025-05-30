package com.lyecdevelopers.core.model

data class Config(
    val baseUrl: String,
    val apiKey: String? = null,
    val username: String,
    val password: String,
)