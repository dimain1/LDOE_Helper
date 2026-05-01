package com.example.kotlinclient.local_cache.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromMap(map: Map<String, Object>?): String? {
        return map?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toMap(value: String?): Map<String, Object>? {
        if (value == null) return null

        // Используем TypeToken, чтобы Gson знал, в какой тип данных десериализовать
        val mapType = object : TypeToken<Map<String, Object>>() {}.type
        return gson.fromJson(value, mapType)
    }
}