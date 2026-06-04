package com.example.kotlinclient.presentation.utility.uiComponent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.example.kotlinclient.R
import com.example.kotlinclient.api_client.NetworkConfig
import java.io.File

/**
 * Универсальный компонент для отображения изображений.
 *
 * Стратегия выбора модели для Coil3:
 *  • [fileName] null/пусто          → null (показывается заглушка)
 *  • локальный файл, который существует → [File] (Coil3 читает напрямую)
 *  • серверный относительный путь   → полный URL через [NetworkConfig.imageUrl]
 *
 * Передавать bare String нельзя: Coil3 интерпретирует её как URL,
 * а `/images/xxx.jpg` — это не URL и не локальный путь с точки зрения HTTP-клиента.
 */
@Composable
fun LocalImage(
    fileName: String?,
    modifier: Modifier,
    fillAll: Boolean = false,
    size: Int? = null,
) {
    var isError by remember { mutableStateOf(false) }

    // Вычисляем модель один раз (при изменении fileName) — не на каждой рекомпозиции
    val model: Any? = remember(fileName) {
        when {
            fileName.isNullOrBlank() -> null
            File(fileName).exists() -> File(fileName)        // локальный файл
            else -> NetworkConfig.imageUrl(fileName)         // серверный путь → полный URL
        }
    }

    AsyncImage(
        model = model,
        contentDescription = null,
        modifier = modifier,
        contentScale = if (fillAll) ContentScale.FillBounds else ContentScale.Fit,
        error = if (size == null) painterResource(R.drawable.maxresdefault)
                else painterResource(R.drawable.icon),
        onError   = { isError = true },
        onSuccess = { isError = false }
    )
}
