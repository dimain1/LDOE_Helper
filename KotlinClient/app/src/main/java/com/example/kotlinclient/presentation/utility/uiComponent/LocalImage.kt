package com.example.kotlinclient.presentation.utility.uiComponent

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.example.kotlinclient.R
import com.example.kotlinclient.api_client.NetworkConfig
import java.io.File

@Composable
fun LocalImage(
    fileName: String?,
    modifier: Modifier,
    fillAll: Boolean = false
) {
    var isError by remember { mutableStateOf(false) }

    AsyncImage(
        model = fileName,
        contentDescription = null,
        modifier = modifier,
        contentScale = if (isError) ContentScale.FillBounds else (if (fillAll) ContentScale.FillBounds else ContentScale.Fit),
        error = painterResource(R.drawable.maxresdefault),
        onError = { isError = true },
        onSuccess = { isError = false }
    )

}
