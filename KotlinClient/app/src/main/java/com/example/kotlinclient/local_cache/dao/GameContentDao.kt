package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.kotlinclient.local_cache.entity.GameContentEntity
import com.example.kotlinclient.local_cache.entity.relationExtension.GameContentWithTypes
import com.example.kotlinclient.state_management.entity.GameContent
import kotlinx.coroutines.flow.Flow

@Dao
interface GameContentDao {

    @Transaction
    @Query("SELECT * FROM game_content")
    fun getAllContentWithTypes(): List<GameContentWithTypes>

    @Query("SELECT * FROM game_content")
    fun getAllContent(): Flow<List<GameContentEntity>>

    @Query(""" SELECT * FROM game_content g
        WHERE g.name like('%' || :query || '%')
        AND :type IN (SELECT content_type_id FROM type_to_content WHERE game_content_id = g.id )
    """)
    fun getFilteredContent(query:String, type: Long): Flow<List<GameContentEntity>>

    @Query("SELECT * FROM game_content WHERE pinned = 1")
    fun getPinnedContent(): Flow<List<GameContentEntity>>

    @Query("UPDATE game_content SET pinned = :pinStatus WHERE id = :id")
    fun updateContentPin(id: Long, pinStatus: Boolean)

}