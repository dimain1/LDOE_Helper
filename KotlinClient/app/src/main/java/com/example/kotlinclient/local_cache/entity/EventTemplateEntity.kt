package com.example.kotlinclient.local_cache.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "event_template",
    indices = [
        Index(value = ["name", "creator_id"], unique = true),
        Index(value = ["creator_id"]),
        Index(value = ["server_id"])
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

    /** ID на сервере. null — шаблон ещё не синхронизирован. */
    @ColumnInfo(name = "server_id")
    val serverId: Long? = null,

    /** Статус синхронизации. */
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,

    @ColumnInfo(name = "creator_id")
    val creatorId: Long?,

    val name: String,
    val description: String?,

    /** Серверный URL изображения (/images/xxx.jpg). Null до успешной загрузки на сервер. */
    @ColumnInfo(name = "image")
    val imageUrl: String?,

    /** Абсолютный путь к локальному файлу. Null после успешной загрузки на сервер. */
    @ColumnInfo(name = "local_image_path")
    val localImagePath: String? = null,

    val duration: Long
)
