package com.example.kotlinclient.local_cache.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "pinned_content",
    primaryKeys = ["user_id", "game_content_id"],
    indices = [
        Index( value =["user_id"] ),
        Index( value =["game_content_id"] )
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = GameContentEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_content_id"],
            onDelete = ForeignKey.CASCADE,
        )

    ]
)
data class UserPinnedGameContentCrossRef (

    @ColumnInfo(name="user_id")
    val userId: Long,
    @ColumnInfo(name="game_content_id")
    val gameContentId: Long
)
