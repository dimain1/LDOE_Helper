package com.example.kotlinclient.local_cache.entity.relationExtension

import androidx.room.Embedded
import androidx.room.Relation
import com.example.kotlinclient.local_cache.entity.EventTemplateEntity
import com.example.kotlinclient.local_cache.entity.UserEntity
import com.example.kotlinclient.state_management.entity.EventTemplate

data class EventTemplateWithUser(
    @Embedded val eventTemplate: EventTemplateEntity,
    @Relation(
        parentColumn = "creator_id",
        entityColumn = "id"
    )
    val user: UserEntity
)
