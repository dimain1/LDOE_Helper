package com.example.kotlinclient.presentation.utility.uiComponent

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.example.kotlinclient.R

@Composable
fun LocalImage(fileName: String?, modifier: Modifier ,fillAll: Boolean = true) {
    val imageFile: String =  if(fileName == null) "" else fileName
    //val imageFile = File(context.filesDir, "images/$fileName.jpg")

    AsyncImage(
        model = imageFile,
        contentDescription = "Downloaded Image",
        modifier = modifier,
        error = painterResource(R.drawable.plus),
        contentScale = if(fillAll) ContentScale.FillBounds else ContentScale.Fit
    )
}