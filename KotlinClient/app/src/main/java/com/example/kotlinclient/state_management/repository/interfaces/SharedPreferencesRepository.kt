package com.example.kotlinclient.state_management.repository.interfaces

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface SharedPreferencesRepository {

    fun observeBoolean(key: String, defaultValue: Boolean): Flow<Boolean>

    fun observeLong(key: String, defaultValue: Long): Flow<Long>

    suspend fun getBooleanByKey(key: String): Boolean

    suspend fun getLongByKey(key: String): Long

    suspend fun putLongByKey(key: String, value: Long)

    suspend fun switchBooleanValueByKey(key: String)

}