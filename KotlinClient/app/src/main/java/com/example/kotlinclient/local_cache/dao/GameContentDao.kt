package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.kotlinclient.local_cache.entity.relationExtension.GameContentWithTypes

@Dao
interface GameContentDao {

    @Transaction
    @Query("SELECT * FROM game_content")
    fun getAllContentWithTypes(): List<GameContentWithTypes>

}