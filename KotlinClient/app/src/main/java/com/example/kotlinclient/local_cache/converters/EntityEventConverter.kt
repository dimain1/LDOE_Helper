package com.example.kotlinclient.local_cache.converters

import com.example.kotlinclient.local_cache.entity.EventEntity
import com.example.kotlinclient.local_cache.entity.relationExtension.EventWithUserAndTemplate
import com.example.kotlinclient.state_management.entity.Event
import java.time.LocalDateTime
import java.time.ZoneId


fun Event.toEntity(): EventEntity{
    return EventEntity(
        id = this.id,
        templateId = this.template?.id,
        userId = this.user!!.id,
        name = this.name,
        description = this.description,
        imageUrl = this.image,
        startTime = this.startTime.atZone(ZoneId.systemDefault()).toInstant(),
        endTime = this.endTime.atZone(ZoneId.systemDefault()).toInstant()

    )
}

fun EventEntity.toModel() : Event{
    return Event(
        id = this.id,
        user = null,
        template = null ,
        name = this.name,
        description = this.description,
        image = this.imageUrl,
        startTime = LocalDateTime.ofInstant(this.startTime, ZoneId.systemDefault()),
        endTime = LocalDateTime.ofInstant(this.endTime, ZoneId.systemDefault()),
    )
}

fun EventWithUserAndTemplate.toModel(): Event{
    return Event(
        id = this.event.id,
        user = this.user?.toModel(),
        template = this.template?.toModel(this.user?.toModel()),
        name = this.event.name,
        description = this.event.description,
        image = this.event.imageUrl,
        startTime = LocalDateTime.ofInstant(this.event.startTime, ZoneId.systemDefault()),
        endTime = LocalDateTime.ofInstant(this.event.endTime, ZoneId.systemDefault())
    )
}