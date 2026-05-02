package com.example.kotlinclient.local_cache.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "type_to_content",
    primaryKeys = ["game_content_id", "content_type_id"],
    indices = [
        Index( value= ["game_content_id"] ),
        Index( value= ["content_type_id"] )
    ]
)
data class GameContentTypeCrossRef(

    @ColumnInfo(name= "game_content_id")
    val gameContentId: Long,
    @ColumnInfo(name= "content_type_id")
    val contentTypeId: Long,
)
