package com.example.kotlinclient.state_management.utility

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ImageStorageManager(
    private val context: Context,
    private val httpClient: OkHttpClient
) {

    private fun imagesDir(): File =
        File(context.filesDir, "template_images").apply { if (!exists()) mkdirs() }

    /**
     * Копирует изображение из [uri] (content://) в приватное хранилище приложения.
     * Возвращает абсолютный путь к сохранённому файлу или null при ошибке.
     */
    fun saveImageToLocal(uri: Uri?): String? {
        if (uri == null) return null

        val destinationFile = File(imagesDir(), "${UUID.randomUUID()}.jpg")

        context.contentResolver.openInputStream(uri).use { inputStream ->
            if (inputStream == null) return null
            FileOutputStream(destinationFile).use { inputStream.copyTo(it) }
        }

        return destinationFile.absolutePath
    }

    /**
     * Скачивает изображение по абсолютному [absoluteUrl] и сохраняет в приватное хранилище.
     * Используется при синхронизации с сервером для офлайн-доступа к картинкам.
     *
     * Возвращает абсолютный путь к сохранённому файлу или null при ошибке.
     */
    suspend fun downloadFromUrl(absoluteUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(absoluteUrl).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) return@withContext null

            val body = response.body ?: return@withContext null

            val destinationFile = File(imagesDir(), "${UUID.randomUUID()}.jpg")
            body.byteStream().use { inputStream ->
                FileOutputStream(destinationFile).use { inputStream.copyTo(it) }
            }

            destinationFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }
}
