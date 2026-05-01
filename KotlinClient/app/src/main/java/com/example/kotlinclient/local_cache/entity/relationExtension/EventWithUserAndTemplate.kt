package com.example.kotlinclient.local_cache.entity.relationExtension

import androidx.room.Embedded
import androidx.room.Relation
import com.example.kotlinclient.local_cache.entity.EventEntity
import com.example.kotlinclient.local_cache.entity.EventTemplateEntity
import com.example.kotlinclient.local_cache.entity.UserEntity

data class EventWithUserAndTemplate(
    @Embedded val event: EventEntity,
    @Relation(
        parentColumn = "template_id",
        entityColumn = "id"
    )
    val template: EventTemplateEntity,
    @Relation(
        parentColumn = "user_id",
        entityColumn = "id"
    )
    val user: UserEntity

)
