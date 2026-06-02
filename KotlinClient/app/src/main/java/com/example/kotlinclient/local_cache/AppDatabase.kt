package com.example.kotlinclient.local_cache

import androidx.room.Database
import androidx.room.RoomDatabase

import androidx.room.TypeConverters
import com.example.kotlinclient.local_cache.converters.DateTimeConverter
import com.example.kotlinclient.local_cache.converters.MapConverter
import com.example.kotlinclient.local_cache.dao.ContentTypeDao
import com.example.kotlinclient.local_cache.dao.EventDao
import com.example.kotlinclient.local_cache.dao.EventTemplateDao
import com.example.kotlinclient.local_cache.dao.GameContentDao
import com.example.kotlinclient.local_cache.dao.UserDao
import com.example.kotlinclient.local_cache.entity.ContentTypeEntity
import com.example.kotlinclient.local_cache.entity.EventEntity
import com.example.kotlinclient.local_cache.entity.EventTemplateEntity
import com.example.kotlinclient.local_cache.entity.GameContentEntity
import com.example.kotlinclient.local_cache.entity.GameContentTypeCrossRef
import com.example.kotlinclient.local_cache.entity.UserEntity
import com.example.kotlinclient.local_cache.entity.UserPinnedGameContentCrossRef

@Database(
    entities= [
        ContentTypeEntity::class,
        GameContentEntity::class,
        EventEntity::class,
        EventTemplateEntity::class,
        UserEntity::class,
        GameContentTypeCrossRef::class,
        UserPinnedGameContentCrossRef::class
    ],
    version = 18   // +local_image_path в EventTemplate для двухфазной загрузки изображений
)
@TypeConverters(MapConverter::class, DateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun UserDao(): UserDao

    abstract fun EventDao() : EventDao

    abstract fun EventTemplateDao() : EventTemplateDao

    abstract fun GameContentDao() : GameContentDao

    abstract fun ContentTypeDao() : ContentTypeDao

}