package com.example.kotlinclient.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.kotlinclient.R
import java.io.File

@Composable
fun LocalImage(fileName: String?, modifier: Modifier ,fillAll: Boolean = false) {
    val context = LocalContext.current
    val imageFile: String =  if(fileName == null) "" else "https://zornet.ru/_fr/81/9480131.jpg"
    //val imageFile = File(context.filesDir, "images/$fileName.jpg")

    AsyncImage(
        model = imageFile,
        contentDescription = "Downloaded Image",
        modifier = modifier,
        error = painterResource(R.drawable.plus),
        contentScale = if(fillAll) ContentScale.FillBounds else ContentScale.Fit
    )
}