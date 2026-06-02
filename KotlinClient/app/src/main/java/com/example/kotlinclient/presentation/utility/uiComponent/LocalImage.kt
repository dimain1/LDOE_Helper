package com.example.kotlinclient.presentation.utility.uiComponent

import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.example.kotlinclient.R
import com.example.kotlinclient.api_client.NetworkConfig
import java.io.File

/**
 * Универсальный компонент изображения.
 *
 * Если путь указывает на существующий локальный файл — показывает его напрямую.
 * Иначе — трактует как серверный путь (/images/…) и строит полный URL.
 * Coil кэширует сетевые изображения на диске — они доступны офлайн после первой загрузки.
 */
@Composable
fun LocalImage(
    fileName: String?,
    modifier: Modifier,
    fillAll: Boolean = false
) {
    val model = remember(fileName) {
        when {
            fileName.isNullOrBlank() -> null
            File(fileName).exists() -> File(fileName)
            else -> NetworkConfig.imageUrl(fileName)
        }
    }

    AsyncImage(
        model = model,
        contentDescription = null,
        modifier = modifier,
        error = painterResource(R.drawable.plus),
        contentScale = if (fillAll) ContentScale.FillBounds else ContentScale.Fit
    )
}
