package com.example.kotlinclient.api_client

import android.util.Base64
import com.example.kotlinclient.api_client.dto.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject

/**
 * Добавляет заголовок `Authorization: Bearer <access_token>` ко всем запросам,
 * кроме публичных эндпоинтов.
 *
 * Если access-токен истёк или истекает через менее чем 60 секунд —
 * превентивно вызывает /auth/refresh перед отправкой запроса.
 * Это предотвращает 401 и гарантирует, что logout всегда доходит до сервера
 * без Bearer-заголовка (он не нужен: сервер идентифицирует сессию по refresh-токену в теле).
 */
class AuthInterceptor(
    private val tokenStorage: TokenStorage,
    private val apiServiceProvider: () -> ApiService
) : Interceptor {

    // auth/logout намеренно исключён: сервер идентифицирует сессию по refresh-токену в теле,
    // отправка просроченного Bearer-токена вызывала 401 и мешала сереверу удалить запись.
    private val noAuthPaths = setOf("auth/login", "auth/register", "auth/refresh", "auth/logout")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath.trimStart('/')

        if (noAuthPaths.any { path.startsWith(it) }) {
            return chain.proceed(request)
        }

        val accessToken = tokenStorage.getAccessToken()
            ?: return chain.proceed(request)

        val token = if (isExpiredOrExpiringSoon(accessToken)) {
            // Preventiavely refresh; on failure fall back to the stale token so
            // TokenAuthenticator can still handle the resulting 401.
            tryRefresh() ?: accessToken
        } else {
            accessToken
        }

        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        )
    }

    /** Обновляет токены; возвращает новый access-токен или null при ошибке. */
    fun tryRefresh(): String? {
        val refreshToken = tokenStorage.getRefreshToken() ?: return null
        return synchronized(this) {
            // Другой поток мог уже обновить токен пока ждали блокировку.
            val current = tokenStorage.getAccessToken()
            if (current != null && !isExpiredOrExpiringSoon(current)) {
                return@synchronized current
            }
            runBlocking {
                runCatching {
                    val newTokens = apiServiceProvider().refresh(RefreshRequest(refreshToken))
                    tokenStorage.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                    newTokens.accessToken
                }.getOrNull()
            }
        }
    }

    /** true если до истечения JWT осталось менее 60 секунд (или токен нечитаем). */
    fun isExpiredOrExpiringSoon(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return true
            val payload = String(
                Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            )
            val exp = JSONObject(payload).getLong("exp")
            System.currentTimeMillis() / 1000 >= exp - 60
        } catch (e: Exception) {
            true
        }
    }
}
