package com.example.kotlinclient.local_cache.converters

import com.example.kotlinclient.local_cache.entity.EventEntity
import com.example.kotlinclient.local_cache.entity.relationExtension.EventWithUserAndTemplate
import com.example.kotlinclient.state_management.entity.Event


fun Event.toEntity(): EventEntity{
    return EventEntity(
        id = this.id,
        templateId = this.template?.id,
        userId = this.user!!.id,
        name = this.name,
        startTime = this.start_time,
        endTime = this.end_time
    )
}

fun EventEntity.toModel() : Event{
    return Event(
        id = this.id,
        user = null,
        template = null ,
        name = this.name,
        start_time = this.startTime,
        end_time = this.endTime,
    )
}

fun EventWithUserAndTemplate.toModel(): Event{
    return Event(
        id = this.event.id,
        user = this.user.toModel(),
        template = this.template.toModel(this.user.toModel()),
        name = this.event.name,
        start_time = this.event.startTime,
        end_time = this.event.endTime
    )
}