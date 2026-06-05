package com.example.kotlinclient.api_client

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

/**
 * true — ошибка транспортного уровня: нет интернета, таймаут, DNS.
 * Отличать от HTTP-ошибок (4xx/5xx), которые означают, что сервер ответил.
 */
fun Throwable.isNetworkError(): Boolean =
    this is IOException || cause is IOException

/**
 * HTTP-статус ответа сервера, или null если это не HTTP-ошибка.
 */
fun Throwable.httpCode(): Int? = (this as? HttpException)?.code()

/**
 * Человекочитаемое сообщение из тела ответа сервера.
 *
 * Сначала пробует распарсить JSON и вернуть поле "message"/"error".
 * Если тело — не JSON, возвращает сырую строку (если непустая).
 */
fun Throwable.serverMessage(): String? = runCatching {
    val body = (this as? HttpException)
        ?.response()?.errorBody()?.string()
        ?.takeIf { it.isNotBlank() }
        ?: return@runCatching null

    // Пробуем распарсить {"message": "..."} или {"error": "..."}
    try {
        val json = JSONObject(body)
        json.optString("message").takeIf { it.isNotBlank() }
            ?: json.optString("error").takeIf { it.isNotBlank() }
            ?: body
    } catch (_: Exception) {
        body
    }
}.getOrNull()
