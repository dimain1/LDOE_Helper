package com.example.kotlinclient.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.R
import com.example.kotlinclient.ui.theme.Typography


//Нижняя часть приложения(Bottom App Bar)
@Composable
fun MyBottomAppBar(
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onInfoClick: () -> Unit,
    onEventClick: () -> Unit,
    onTemplateClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    BottomAppBar(
        containerColor = colorScheme.primaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Системный отступ от кнопок навигации


    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp)

        ) {
            // Иконки
            BottomIcons(R.drawable.home, "Home",currentRoute ,onHomeClick)
            BottomIcons(R.drawable.info, "Database",currentRoute , onInfoClick)
            BottomIcons(R.drawable.notification, "Events",currentRoute ,onEventClick)
            BottomIcons(R.drawable.template, "Templates",currentRoute ,onTemplateClick)
            BottomIcons(R.drawable.settings, "Settings",currentRoute ,onSettingsClick)

        }

    }
}

// Шаблон иконок
@Composable
fun BottomIcons(image: Int, title: String,currentScreen: String?,onClick: ()-> Unit){

    val color = if(title == currentScreen) colorScheme.tertiary else colorScheme.secondary
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier= Modifier
            .clickable(
                onClick = {
                    onClick()
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    color = colorScheme.tertiary,
                    radius = 36.dp
                ) // Изменение эффекта нажатия на иконку
            )
            .background(shape = RoundedCornerShape(15), color = colorScheme.primaryContainer)
            .padding(10.dp)
    )
    {
        // Сама иконка
        Image(
            painter = painterResource(image),
            contentDescription = title,
            colorFilter=ColorFilter.tint(color),
            modifier= Modifier.size(20.dp)
        )

        Spacer(Modifier.height(8.dp))
        // Подпись к иконке
        Text(text= title, style = Typography.bodySmall, color= color )
    }
}