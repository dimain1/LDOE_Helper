package com.example.kotlinclient.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.ui.theme.Typography

//Верхняя часть приложения
@Composable
fun AppHeader(){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier= Modifier
            .fillMaxWidth()
            .background(color= colorScheme.primaryContainer)
            .statusBarsPadding() // Системный отступ от верхней панели
            .padding(bottom = 12.dp)
    )
    {
        Text(text= "Last Day on Earth", style= TextStyle(fontSize = Typography.titleLarge.fontSize, fontWeight = FontWeight.Bold), color= colorScheme.primary)
        Text(text= "Справочник выживания", style = Typography.titleSmall, color= colorScheme.secondary )
    }
}