package com.example.kotlinclient.local_cache.entity.relationExtension

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.kotlinclient.local_cache.entity.ContentTypeEntity
import com.example.kotlinclient.local_cache.entity.GameContentEntity
import com.example.kotlinclient.local_cache.entity.GameContentTypeCrossRef

data class GameContentFull(
    @Embedded val content: GameContentEntity,

    // Статус пина: если в таблице pinned_content есть запись для этого юзера, будет true
    val isPinned: Boolean,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = GameContentTypeCrossRef::class,
            parentColumn = "game_content_id",
            entityColumn = "content_type_id"
        )
    )
    val types: List<ContentTypeEntity>
)