package com.example.kotlinclient.local_cache.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.kotlinclient.local_cache.entity.relationExtension.EventTemplateWithUser

@Dao
interface EventTemplateDao {

    @Transaction
    @Query("SELECT * FROM event_template")
    fun getAllEventTemplateWithUser(): EventTemplateWithUser
}