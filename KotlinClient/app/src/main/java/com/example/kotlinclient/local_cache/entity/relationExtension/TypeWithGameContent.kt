package com.example.kotlinclient.local_cache.entity.relationExtension

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.kotlinclient.local_cache.entity.ContentTypeEntity
import com.example.kotlinclient.local_cache.entity.GameContentEntity
import com.example.kotlinclient.local_cache.entity.GameContentTypeCrossRef
import com.example.kotlinclient.state_management.entity.ContentType
import com.example.kotlinclient.state_management.entity.GameContent

data class TypeWithGameContent(
    @Embedded val contentType: ContentTypeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = GameContentTypeCrossRef::class,
            parentColumn = "content_type_id",
            entityColumn = "game_content_id"
        )
    )
    val gameContents: List<GameContentEntity>
)