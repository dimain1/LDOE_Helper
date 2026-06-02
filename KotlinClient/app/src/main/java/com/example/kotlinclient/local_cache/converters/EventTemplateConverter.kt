package com.example.kotlinclient.local_cache.converters

import com.example.kotlinclient.local_cache.entity.EventTemplateEntity
import com.example.kotlinclient.local_cache.entity.relationExtension.EventTemplateWithUser
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.entity.User
import java.io.File

fun EventTemplate.toEntity(): EventTemplateEntity {
    val isLocalFile = !this.image.isNullOrBlank() && File(this.image).exists()
    return EventTemplateEntity(
        id = this.id,
        creatorId = this.creator?.id,
        name = this.name,
        description = this.description,
        imageUrl = if (isLocalFile) null else this.image,
        localImagePath = if (isLocalFile) this.image else null,
        duration = this.duration
    )
}

fun EventTemplateEntity.toModel(user: User? = null): EventTemplate {
    return EventTemplate(
        id = this.id,
        creator = user,
        name = this.name,
        description = this.description,
        image = this.localImagePath ?: this.imageUrl,
        duration = this.duration
    )
}

fun EventTemplateWithUser.toModel(): EventTemplate {
    return EventTemplate(
        id = this.eventTemplate.id,
        creator = this.user?.toModel(),
        name = this.eventTemplate.name,
        description = this.eventTemplate.description,
        image = this.eventTemplate.localImagePath ?: this.eventTemplate.imageUrl,
        duration = this.eventTemplate.duration
    )
}