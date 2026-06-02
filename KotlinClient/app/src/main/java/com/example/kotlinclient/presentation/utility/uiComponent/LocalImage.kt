package com.example.kotlinclient.presentation.utility.uiComponent

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.example.kotlinclient.R
import com.example.kotlinclient.api_client.NetworkConfig

/**
 * Универсальный компонент изображения.
 *
 * Принимает либо локальный путь к файлу, либо путь с сервера (/images/…).
 * NetworkConfig.imageUrl() достраивает полный URL если нужно.
 * Coil кэширует загруженные изображения на диске — они доступны офлайн.
 */
@Composable
fun LocalImage(
    fileName: String?,
    modifier: Modifier,
    fillAll: Boolean = false
) {
    AsyncImage(
        model = NetworkConfig.imageUrl(fileName),
        contentDescription = null,
        modifier = modifier,
        error = painterResource(R.drawable.plus),
        contentScale = if (fillAll) ContentScale.FillBounds else ContentScale.Fit
    )
}
