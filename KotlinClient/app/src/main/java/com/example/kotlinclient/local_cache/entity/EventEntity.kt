package com.example.kotlinclient.local_cache.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.OffsetDateTime

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = EventTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["template_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("template_id"),
        Index("user_id")

    ]
    )
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,

    @ColumnInfo(name= "template_id")
    val templateId: Long?,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    val name: String?,
    val description: String?,
    @ColumnInfo(name = "image")
    val imageUrl: String?,
    @ColumnInfo(name="start_time")
    val startTime: Instant,
    @ColumnInfo(name="end_time")
    val endTime: Instant
)
