package com.lyecdevelopers.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forms")
data class FormEntity(
    @PrimaryKey val uuid: String,
    val display: String,
    val description: String?,
    val creator: String?,
    val dateChanged: String?,
    val published: Boolean,
    val createdAt: Long = System.currentTimeMillis() // Unix timestamp in milliseconds
)


