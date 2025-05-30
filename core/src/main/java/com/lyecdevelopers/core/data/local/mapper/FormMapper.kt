package com.lyecdevelopers.core.data.local.mapper

import com.lyecdevelopers.core.data.local.entity.FormEntity
import com.lyecdevelopers.core.model.Form


fun FormEntity.toDomain(): Form {
    return Form(
        uuid = TODO(),
        display = TODO(),
        name = TODO(),
        description = TODO(),
        encountertype = TODO(),
        version = TODO(),
        build = TODO(),
        published = TODO(),
        retired = TODO(),
        auditinfo = TODO(),
        resources = TODO(),
        links = TODO(),
        resourceversion = TODO()
    )
}

fun Form.toEntity(): FormEntity {
    return FormEntity(
        uuid = TODO(),
        display = TODO(),
        description = TODO(),
        creator = TODO(),
        dateChanged = TODO(),
        published = TODO(),
        createdAt = TODO()
    )
}
