package com.example.kotlinclient.api_client

import com.example.kotlinclient.api_client.dto.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * При получении 401 автоматически пробует обновить access token через refresh token.
 *
 * Паттерн Lazy<ApiService> разрывает циклическую зависимость:
 * ApiService → OkHttp → Authenticator → ApiService.
 */
class TokenAuthenticator(
    private val tokenStorage: TokenStorage,
    private val apiServiceProvider: () -> ApiService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization") == null) return null
        if (responseCount(response) >= 2) return null

        val refreshToken = tokenStorage.getRefreshToken() ?: return null

        val newTokens = synchronized(this) {
            val currentAccess = tokenStorage.getAccessToken()
            val requestAccess = response.request.header("Authorization")
                ?.removePrefix("Bearer ")?.trim()

            if (currentAccess != null && currentAccess != requestAccess) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccess")
                    .build()
            }

            runBlocking {
                try {
                    apiServiceProvider().refresh(RefreshRequest(refreshToken))
                } catch (e: Exception) {
                    null
                }
            }
        } ?: run {
            tokenStorage.clear()
            return null
        }

        tokenStorage.saveTokens(newTokens.accessToken, newTokens.refreshToken)

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newTokens.accessToken}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) { count++; prior = prior.priorResponse }
        return count
    }
}
