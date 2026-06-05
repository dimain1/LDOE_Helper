package com.example.kotlinclient.api_client

/**
 * Единая точка конфигурации сервера.
 *
 * Для эмулятора:  BASE_URL = "http://10.0.2.2:8080/"
 * Для устройства: BASE_URL = "http://<IP_машины>:8080/"
 */
object NetworkConfig {
    const val BASE_URL = "http://192.168.0.10:8080/"

    /**
     * Строит полный URL для изображения.
     * Если уже полная ссылка (http/https) — возвращает как есть.
     * Если относительный путь от сервера (/images/...) — добавляет base.
     * Если null/пусто — null (показывается заглушка).
     */
    fun imageUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return if (path.startsWith("http://") || path.startsWith("https://")) {
            path
        } else {
            BASE_URL.trimEnd('/') + path
        }
    }
}
