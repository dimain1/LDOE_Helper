package com.example.kotlinclient.local_cache.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "event_template",
    indices =
        [
        Index(value = ["name", "creator_id"], unique = true),
        Index(value = ["creator_id"])
        ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["creator_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EventTemplateEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,

    @ColumnInfo(name= "creator_id")
    val creatorId: Long?,
    val name: String,
    val description: String?,
    @ColumnInfo(name="image")
    val imageUrl: String?,
    val duration: Long

)
