package com.example.kotlinclient.presentation.utility

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ImageStorageManager(private val context: Context) {

    // Метод возвращает путь к файлу String? или выбрасывает исключение, если что-то пошло не так
    fun saveImageToLocal(uri: Uri?): String? {
        if (uri == null) return null

        val imagesDir = File(context.filesDir, "template_images").apply {
            if (!exists()) mkdirs()
        }

        val fileName = "${UUID.randomUUID()}.jpg"
        val destinationFile = File(imagesDir, fileName)

        context.contentResolver.openInputStream(uri).use { inputStream ->
            if (inputStream == null) throw IllegalStateException("Не удалось открыть InputStream для Uri: $uri")

            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return destinationFile.absolutePath
    }
}