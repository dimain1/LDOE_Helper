package com.example.kotlinclient.presentation

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.kotlinclient.R
import java.io.File

@Composable
fun LocalImage(fileName: String?,size: Int) {
    val context = LocalContext.current
    val imageFile = File(context.filesDir, "images/$fileName.jpg")

    AsyncImage(
        model = imageFile,
        contentDescription = "Downloaded Image",
        modifier = Modifier.size(size.dp),
        error = painterResource(R.drawable.plus)
    )
}