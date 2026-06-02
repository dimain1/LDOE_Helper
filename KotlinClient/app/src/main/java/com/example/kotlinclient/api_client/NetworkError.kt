package com.example.kotlinclient.api_client

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
 * Тело ответа сервера (строка), если доступно.
 */
fun Throwable.serverMessage(): String? =
    runCatching { (this as? HttpException)?.response()?.errorBody()?.string() }.getOrNull()
