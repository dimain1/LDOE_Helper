package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.kotlinclient.local_cache.entity.ContentTypeEntity
import com.example.kotlinclient.local_cache.entity.GameContentEntity
import com.example.kotlinclient.local_cache.entity.GameContentTypeCrossRef
import com.example.kotlinclient.local_cache.entity.UserPinnedGameContentCrossRef
import com.example.kotlinclient.local_cache.entity.relationExtension.GameContentFull
import kotlinx.coroutines.flow.Flow

@Dao
interface GameContentDao {

    // ── Queries для UI ────────────────────────────────────────────────────────

    @Transaction
    @Query("""
        SELECT gc.*, (pc.user_id IS NOT NULL) as isPinned
        FROM game_content AS gc
        LEFT JOIN pinned_content AS pc
            ON gc.id = pc.game_content_id AND pc.user_id = :userId
        WHERE isPinned = 1
    """)
    fun getPinnedContent(userId: Long): Flow<List<GameContentFull>>

    @Transaction
    @Query("""
        SELECT gc.*, (pc.user_id IS NOT NULL) as isPinned
        FROM game_content AS gc
        LEFT JOIN pinned_content AS pc
            ON gc.id = pc.game_content_id AND pc.user_id = :userId
        WHERE gc.name LIKE '%' || :query || '%'
          AND (:typeId IS NULL OR :typeId IN (
                SELECT content_type_id FROM type_to_content ttc
                WHERE ttc.game_content_id = gc.id
              ))
    """)
    fun getFilteredContent(userId: Long, query: String, typeId: Long?): Flow<List<GameContentFull>>

    // ── Pin / Unpin ───────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun pinContent(crossRef: UserPinnedGameContentCrossRef)

    @Query("DELETE FROM pinned_content WHERE user_id = :userId AND game_content_id = :contentId")
    suspend fun unpinContent(userId: Long, contentId: Long)

    @Query("DELETE FROM pinned_content WHERE user_id = :userId")
    suspend fun clearAllPins(userId: Long)

    // ── Sync (upsert от сервера) ──────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContent(content: GameContentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContentList(list: List<GameContentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTypes(types: List<ContentTypeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTypeRefs(refs: List<GameContentTypeCrossRef>)

    @Query("DELETE FROM type_to_content WHERE game_content_id = :contentId")
    suspend fun clearTypeRefs(contentId: Long)

    @Query("DELETE FROM game_content WHERE id NOT IN (:serverIds)")
    suspend fun deleteNotIn(serverIds: List<Long>)

    @Query("SELECT local_image_path FROM game_content WHERE id = :id LIMIT 1")
    suspend fun getLocalImagePath(id: Long): String?

    @Query("UPDATE game_content SET local_image_path = :path WHERE id = :id")
    suspend fun updateLocalImagePath(id: Long, path: String?)
}
