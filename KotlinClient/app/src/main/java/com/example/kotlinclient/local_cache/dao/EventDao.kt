package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.kotlinclient.local_cache.entity.EventEntity
import com.example.kotlinclient.local_cache.entity.SyncStatus
import com.example.kotlinclient.local_cache.entity.relationExtension.EventWithUserAndTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    // ── Queries для UI ────────────────────────────────────────────────────────

    @Transaction
    @Query("""
        SELECT * FROM events
        WHERE end_time > :currentTimeMillis
          AND user_id = :userId
          AND sync_status != 'PENDING_DELETE'
    """)
    fun getAllEventsUpcomingWithTemplate(
        userId: Long,
        currentTimeMillis: Long
    ): Flow<List<EventWithUserAndTemplate>>

    @Transaction
    @Query("""
        SELECT * FROM events
        WHERE user_id = :userId
          AND sync_status != 'PENDING_DELETE'
    """)
    fun getAllEventsWithTemplate(userId: Long): Flow<List<EventWithUserAndTemplate>>

    // ── Мутации ───────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEvent(event: EventEntity): Long

    @Query("""
        UPDATE events
        SET name = :name, description = :description, image = :imageUrl,
            start_time = :startTime, end_time = :endTime,
            sync_status = 'PENDING_UPDATE'
        WHERE id = :id
    """)
    suspend fun updateEvent(id: Long, name: String?, description: String?, imageUrl: String?, startTime: Long, endTime: Long)

    @Query("SELECT server_id FROM events WHERE id = :localId LIMIT 1")
    suspend fun getServerIdByLocalId(localId: Long): Long?

    @Query("UPDATE events SET sync_status = 'PENDING_DELETE' WHERE id = :localId AND user_id = :userId")
    suspend fun markDeleted(userId: Long, localId: Long)

    @Query("DELETE FROM events WHERE id = :localId")
    suspend fun deleteById(localId: Long)

    // ── Sync helpers ──────────────────────────────────────────────────────────

    /** После создания на сервере: сохранить serverId и отметить как SYNCED. */
    @Query("UPDATE events SET server_id = :serverId, sync_status = 'SYNCED' WHERE id = :localId")
    suspend fun confirmCreated(localId: Long, serverId: Long)

    @Query("UPDATE events SET sync_status = 'SYNCED' WHERE id = :localId")
    suspend fun confirmUpdated(localId: Long)

    @Query("SELECT * FROM events WHERE sync_status = 'PENDING_CREATE' AND user_id = :userId")
    suspend fun getPendingCreate(userId: Long): List<EventEntity>

    @Query("SELECT * FROM events WHERE sync_status = 'PENDING_UPDATE' AND user_id = :userId")
    suspend fun getPendingUpdate(userId: Long): List<EventEntity>

    @Query("SELECT * FROM events WHERE sync_status = 'PENDING_DELETE' AND user_id = :userId AND server_id IS NOT NULL")
    suspend fun getPendingDelete(userId: Long): List<EventEntity>

    /** Upsert события по serverId — для обновлений, пришедших с сервера. */
    @Query("""
        UPDATE events
        SET name = :name, description = :description, image = :imageUrl,
            start_time = :startTime, end_time = :endTime, sync_status = 'SYNCED'
        WHERE server_id = :serverId AND user_id = :userId
    """)
    suspend fun updateByServerId(
        serverId: Long,
        userId: Long,
        name: String?,
        description: String?,
        imageUrl: String?,
        startTime: Long,
        endTime: Long
    )

    @Query("SELECT id FROM events WHERE server_id = :serverId AND user_id = :userId LIMIT 1")
    suspend fun getLocalIdByServerId(serverId: Long, userId: Long): Long?

    /**
     * Все локальные ID событий пользователя — для отмены алармов при logout.
     * Синхронный (не-suspend): вызывается из suspend-контекста с allowMainThreadQueries().
     */
    @Query("SELECT id FROM events WHERE user_id = :userId")
    fun getAllEventIdsSync(userId: Long): List<Long?>

    /**
     * Синхронный (не-Flow, не-suspend) запрос для BootReceiver:
     * возвращает все активные события пользователя, которые ещё не завершились.
     */
    @Query("""
        SELECT * FROM events
        WHERE end_time > :nowMillis
          AND user_id = :userId
          AND sync_status != 'PENDING_DELETE'
    """)
    fun getActiveEventsSync(userId: Long, nowMillis: Long): List<EventEntity>
}
