package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.kotlinclient.local_cache.entity.EventTemplateEntity
import com.example.kotlinclient.local_cache.entity.SyncStatus
import com.example.kotlinclient.local_cache.entity.relationExtension.EventTemplateWithUser
import kotlinx.coroutines.flow.Flow

@Dao
interface EventTemplateDao {

    @Transaction
    @Query("""
        SELECT * FROM event_template
        WHERE creator_id = :userId
          AND sync_status != 'PENDING_DELETE'
    """)
    fun getAllEventTemplateWithUser(userId: Long): Flow<List<EventTemplateWithUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createTemplate(template: EventTemplateEntity): Long

    @Query("""
        UPDATE event_template
        SET name = :name, description = :description, image = :imageUrl,
            local_image_path = :localImagePath, duration = :duration, sync_status = 'PENDING_UPDATE'
        WHERE id = :id
    """)
    suspend fun updateTemplate(id: Long, name: String, description: String?, imageUrl: String?, localImagePath: String?, duration: Long)

    @Query("SELECT server_id FROM event_template WHERE id = :localId LIMIT 1")
    suspend fun getServerIdByLocalId(localId: Long): Long?

    /** Шаблоны, у которых ещё не загружено изображение на сервер. */
    @Query("""
        SELECT * FROM event_template
        WHERE sync_status = 'SYNCED'
          AND local_image_path IS NOT NULL
          AND server_id IS NOT NULL
          AND creator_id = :userId
    """)
    suspend fun getSyncedWithLocalImage(userId: Long): List<EventTemplateEntity>

    /** Вызывается после успешной загрузки изображения: фиксирует серверный URL и очищает локальный путь. */
    @Query("UPDATE event_template SET image = :imageUrl, local_image_path = NULL WHERE id = :localId")
    suspend fun confirmImageUploaded(localId: Long, imageUrl: String?)

    @Query("UPDATE event_template SET sync_status = 'PENDING_DELETE' WHERE id = :localId AND creator_id = :userId")
    suspend fun markDeleted(userId: Long, localId: Long)

    @Query("DELETE FROM event_template WHERE id = :localId")
    suspend fun deleteById(localId: Long)

    @Query("UPDATE event_template SET server_id = :serverId, sync_status = 'SYNCED' WHERE id = :localId")
    suspend fun confirmCreated(localId: Long, serverId: Long)

    @Query("UPDATE event_template SET sync_status = 'SYNCED' WHERE id = :localId")
    suspend fun confirmUpdated(localId: Long)

    @Query("SELECT * FROM event_template WHERE sync_status = 'PENDING_CREATE' AND creator_id = :userId")
    suspend fun getPendingCreate(userId: Long): List<EventTemplateEntity>

    @Query("SELECT * FROM event_template WHERE sync_status = 'PENDING_UPDATE' AND creator_id = :userId")
    suspend fun getPendingUpdate(userId: Long): List<EventTemplateEntity>

    @Query("SELECT * FROM event_template WHERE sync_status = 'PENDING_DELETE' AND creator_id = :userId AND server_id IS NOT NULL")
    suspend fun getPendingDelete(userId: Long): List<EventTemplateEntity>

    @Query("SELECT id FROM event_template WHERE server_id = :serverId AND creator_id = :userId LIMIT 1")
    suspend fun getLocalIdByServerId(serverId: Long, userId: Long): Long?

    @Query("""
        UPDATE event_template
        SET name = :name, description = :description, image = :imageUrl,
            local_image_path = NULL, duration = :duration, sync_status = 'SYNCED'
        WHERE server_id = :serverId AND creator_id = :userId
    """)
    suspend fun updateByServerId(
        serverId: Long,
        userId: Long,
        name: String,
        description: String?,
        imageUrl: String?,
        duration: Long
    )
}
