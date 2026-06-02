package com.example.kotlinclient.state_management.repository.implementation

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.kotlinclient.state_management.repository.interfaces.SharedPreferencesRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SharedPreferencesRepositoryImpl(
    val sharedPreferences: SharedPreferences
): SharedPreferencesRepository
{
    override fun observeBoolean(key: String, defaultValue: Boolean): Flow<Boolean> =
        callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener {
                prefs, changedKey ->
                if(key == changedKey){
                    trySend(prefs.getBoolean(key,defaultValue))
                }
            }

            trySend(sharedPreferences.getBoolean(key, defaultValue))

            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

            awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
        }

    override fun observeLong(key: String, defaultValue: Long): Flow<Long> =
        callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener {
                    prefs, changedKey ->
                if(key == changedKey){
                    trySend(prefs.getLong(key,defaultValue))
                }
            }

            trySend(sharedPreferences.getLong(key, defaultValue))

            sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

            awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
        }

    override fun getBooleanByKey(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    override fun getLongByKey(key: String): Long {
        return sharedPreferences.getLong(key, 0)
    }

    override fun putLongByKey(key: String , value: Long) {
        sharedPreferences.edit { putLong(key , value) }
    }

    override fun switchBooleanValueByKey(key: String) {
        sharedPreferences.edit { putBoolean(key, !getBooleanByKey(key)) }
    }


}