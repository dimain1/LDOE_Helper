package com.example.kotlinclient.local_cache.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

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
        Index("user_id"),
        Index("server_id")
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,

    /** ID на сервере. null — событие ещё не синхронизировано. */
    @ColumnInfo(name = "server_id")
    val serverId: Long? = null,

    /** Статус синхронизации с сервером. */
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,

    @ColumnInfo(name = "template_id")
    val templateId: Long?,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    val name: String?,
    val description: String?,

    @ColumnInfo(name = "image")
    val imageUrl: String?,

    @ColumnInfo(name = "start_time")
    val startTime: Instant,

    @ColumnInfo(name = "end_time")
    val endTime: Instant
)
