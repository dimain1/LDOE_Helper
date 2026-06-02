package com.example.kotlinclient.state_management.repository.interfaces

import kotlinx.coroutines.flow.Flow

interface SharedPreferencesRepository {

    fun observeBoolean(key: String, defaultValue: Boolean): Flow<Boolean>

    fun observeLong(key: String, defaultValue: Long): Flow<Long>

    fun getBooleanByKey(key: String): Boolean

    fun getLongByKey(key: String): Long

    fun putLongByKey(key: String, value: Long)

    fun switchBooleanValueByKey(key: String)

}