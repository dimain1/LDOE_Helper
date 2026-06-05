package com.example.kotlinclient.local_cache.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy

class MapConverter {
    // Настраиваем Gson для корректного чтения целых чисел
    private val gson = GsonBuilder()
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create()

    @TypeConverter
    fun fromMap(map: Map<String, Any>?): String? {
        return map?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toMap(value: String?): Map<String, Any>? {
        // Защита от null, пустых строк и строк из одних пробелов
        if (value.isNullOrBlank()) return null

        return try {
            val mapType = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson(value, mapType)
        } catch (e: Exception) {
            e.printStackTrace()
            null // Или верните emptyMap(), чтобы не ломать логику приложения
        }
    }
}
