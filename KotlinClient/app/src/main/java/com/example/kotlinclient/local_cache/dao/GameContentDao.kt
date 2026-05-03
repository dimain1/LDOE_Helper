package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.kotlinclient.local_cache.entity.GameContentEntity
import com.example.kotlinclient.local_cache.entity.UserPinnedGameContentCrossRef
import com.example.kotlinclient.local_cache.entity.relationExtension.GameContentFull
import com.example.kotlinclient.local_cache.entity.relationExtension.GameContentWithTypes
import com.example.kotlinclient.state_management.entity.GameContent
import kotlinx.coroutines.flow.Flow

@Dao
interface GameContentDao {

    @Transaction
    @Query("""
        SELECT gc.*, 
        (pc.user_id IS NOT NULL) as isPinned 
        FROM game_content AS gc
        LEFT JOIN pinned_content AS pc ON gc.id = pc.game_content_id AND pc.user_id = :userId
        WHERE isPinned = 1
    """)
    fun getPinnedContent(userId: Long): Flow<List<GameContentFull>>

    // Общий поиск/фильтрация с учетом статуса пина
    @Transaction
    @Query("""
        SELECT gc.*, (pc.user_id IS NOT NULL) as isPinned 
        FROM game_content AS gc
        LEFT JOIN pinned_content AS pc ON gc.id = pc.game_content_id AND pc.user_id = :userId
        WHERE gc.name LIKE '%' || :query || '%'
        AND :typeId IN (SELECT content_type_id FROM type_to_content ttc WHERE ttc.game_content_id = gc.id )
    """)
    fun getFilteredContent(userId: Long, query: String, typeId: Long): Flow<List<GameContentFull>>

    // Логика пина теперь — это вставка или удаление из CrossRef таблицы
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun pinContent(crossRef: UserPinnedGameContentCrossRef)

    @Query("DELETE FROM pinned_content WHERE user_id = :userId AND game_content_id = :contentId")
    suspend fun unpinContent(userId: Long, contentId: Long)
}
