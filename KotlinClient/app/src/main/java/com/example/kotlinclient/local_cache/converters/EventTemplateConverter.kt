package com.example.kotlinclient.local_cache.converters

import com.example.kotlinclient.local_cache.entity.EventTemplateEntity
import com.example.kotlinclient.local_cache.entity.relationExtension.EventTemplateWithUser
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.entity.User

fun EventTemplate.toEntity() : EventTemplateEntity{
    return EventTemplateEntity(
        id = this.id,
        creatorId = this.creator?.id,
        name = this.name,
        description = this.description,
        imageUrl = this.image,
        duration = this.duration
    )
}

fun EventTemplateEntity.toModel(user: User? = null): EventTemplate{
    return EventTemplate(
        id = this.id,
        creator = user,
        name = this.name,
        description = this.description,
        image = this.imageUrl,
        duration = this.duration
    )
}

fun EventTemplateWithUser.toModel(): EventTemplate{
    return EventTemplate(
        id = this.eventTemplate.id,
        creator = this.user.toModel(),
        name = this.eventTemplate.name,
        description = this.eventTemplate.description,
        image = this.eventTemplate.imageUrl,
        duration = this.eventTemplate.duration
    )
}