package com.example.kotlinclient.api_client

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Добавляет заголовок `Authorization: Bearer <access_token>` ко всем запросам.
 * Пропускает /auth/login, /auth/register и /auth/refresh без токена.
 */
class AuthInterceptor(private val tokenStorage: TokenStorage) : Interceptor {

    private val noAuthPaths = setOf("auth/login", "auth/register", "auth/refresh")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath.trimStart('/')

        if (noAuthPaths.any { path.startsWith(it) }) {
            return chain.proceed(request)
        }

        val token = tokenStorage.getAccessToken()
            ?: return chain.proceed(request)

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
