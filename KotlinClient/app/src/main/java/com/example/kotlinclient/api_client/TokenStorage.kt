package com.example.kotlinclient.api_client

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Потокобезопасное хранилище JWT-токенов на основе EncryptedSharedPreferences.
 */
class TokenStorage(context: Context) {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "ldoe_secure_tokens",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getAccessToken(): String?  = prefs.getString(KEY_ACCESS, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .putString(KEY_REFRESH, refreshToken)
            .apply()
    }

    fun updateAccessToken(accessToken: String) {
        prefs.edit().putString(KEY_ACCESS, accessToken).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_ACCESS).remove(KEY_REFRESH).apply()
    }

    fun hasTokens(): Boolean = getAccessToken() != null && getRefreshToken() != null

    companion object {
        private const val KEY_ACCESS  = "access_token"
        private const val KEY_REFRESH = "refresh_token"
    }
}
