package com.example.kotlinclient.presentation.home

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.example.kotlinclient.R
import com.example.kotlinclient.presentation.AppHeader
import com.example.kotlinclient.ui.theme.InfoIconColor
import com.example.kotlinclient.ui.theme.LinearGradientStartColor
import com.example.kotlinclient.ui.theme.NotificationIconColor
import com.example.kotlinclient.ui.theme.ServiceFloatingButtonColor
import com.example.kotlinclient.ui.theme.TemplateIconColor
import com.example.kotlinclient.ui.theme.Typography
import com.example.kotlinclient.ui.theme.linearGradientEndColor



@Preview
@Composable
fun HomeScreen(){

    val scrollState = rememberScrollState()
    var emptyList: MutableList<String> = mutableListOf("Daily Raid", "Not a nothing","Not a nothing","Not a nothing","Not a nothing")
    val content: Context = LocalContext.current
    //var emptyList: List<String> = emptyList<String>()

    Scaffold(containerColor = colorScheme.primaryContainer,
        bottomBar = {
            BottomAppBar(
                containerColor= colorScheme.primaryContainer,
                modifier= Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()


            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier= Modifier
                        .fillMaxWidth()
                        .padding(vertical = 0.dp)

                ) {

                    BottomIcons(R.drawable.home, "Home")
                    BottomIcons(R.drawable.info, "Database")
                    BottomIcons(R.drawable.notification, "Events")
                    BottomIcons(R.drawable.template, "Templates")
                    BottomIcons(R.drawable.settings, "Settings")

                }

            }
        }

    )
    { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
        {

            // хедер всего приложения
            AppHeader()

            HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)
            // Основной контейнер домашнего экрана
            Column(
                modifier = Modifier

                    .fillMaxWidth()
                    .weight(1f)
                    .background(color = colorScheme.secondaryContainer)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            )
            {
                Spacer(Modifier.height(16.dp))
                // Контейнер для сервиса оверлея
                Column(
                    modifier = Modifier

                        .background(
                            shape = RoundedCornerShape(10),
                            brush = Brush.linearGradient(
                                colors = listOf(LinearGradientStartColor, linearGradientEndColor),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                        .padding(22.dp)
                )
                {
                    //Верхняя часть с иконкой и надписями
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .background(color = ServiceFloatingButtonColor, shape = CircleShape)
                                .size(48.dp),
                        )
                        {
                            // Иконка сервиса
                            Image(
                                painter= painterResource(R.drawable.service_button),
                                contentDescription = "Service Icon",
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier=Modifier
                                    .size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        // Контейнер текста
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "In-Game Overlay",
                                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = "Create Events while playing",
                                style = TextStyle(
                                    fontSize = Typography.bodyMedium.fontSize,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    // Нижняя часть с кнопкой
                    Button(
                        onClick = { changeTheme(content)},
                        shape = RoundedCornerShape(15),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = colorScheme.tertiary
                        ),
                        modifier = Modifier
                            .height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()

                        ) {
                            // Иконка в кнопке
                            Image(
                                modifier = Modifier.size(20.dp),
                                painter= painterResource(R.drawable.service_start_button),
                                contentDescription = "Play Arrow in button",
                                colorFilter = ColorFilter.tint(colorScheme.tertiary)
                            )
                            Spacer(Modifier.width(10.dp))
                            // Текст на кнопке
                            Text(
                                text = "Launch Overlay",
                                style = TextStyle(
                                    fontSize = Typography.bodyLarge.fontSize,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colorScheme.tertiary
                            )
                        }
                    }
                }


                Spacer(Modifier.height(16.dp))

                //Контейнер для иконок быстрого доступа
                Column(
                    modifier = Modifier
                        .fillMaxWidth()

                )
                {
                    //Первая линия
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        //Левая верхняя иконка
                        QuickBlock(
                            image =R.drawable.plus,
                            title= "New Event",
                            desc ="Quick create",
                            modifier= Modifier.weight(1f),
                            color= Color.Red
                        )

                        Spacer(Modifier.width(12.dp))

                        //Правая верхняя иконка
                        QuickBlock(
                            R.drawable.template,
                            "Templates",
                            "Your saved templates",
                            Modifier.weight(1f),
                            TemplateIconColor
                        )

                    }

                    Spacer(Modifier.height(12.dp))

                    //Вторая линия
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    {

                        //Левая нижняя иконка
                        QuickBlock(
                            R.drawable.notification,
                            "My Events",
                            "2 upcoming",
                            Modifier.weight(1f),
                            NotificationIconColor
                        )

                        Spacer(Modifier.width(12.dp))

                        //Правая нижняя иконка
                        QuickBlock(
                            R.drawable.info,
                            "Database",
                            "Browse items",
                            Modifier.weight(1f),
                            InfoIconColor
                        )

                    }
                }

                Spacer(Modifier.height(16.dp))


                // Upcoming Events Контейнер
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = colorScheme.surface, shape = RoundedCornerShape(10))
                        .border(BorderStroke(2.dp, colorScheme.outline), shape = RoundedCornerShape(10))
                        .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)

                )
                {
                    // Заголовок блока
                    Text(
                        text = "Upcoming Events",
                        style = TextStyle(
                            fontSize = Typography.titleMedium.fontSize,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colorScheme.primary
                    )

                    Spacer(Modifier.height(16.dp))
                    // Проверка на наличие событий
                    if (emptyList.size == 0) {
                        Text(text = "Oops. Maybe you don't have an active events. Go to create one")
                    } else {
                        // Если события есть, то отрисовывается контейнер с ними
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(0.dp, 200.dp)
                        ) {
                            items(emptyList.size) { item ->
                                // Контейнер события
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = colorScheme.tertiaryContainer,
                                            shape = RoundedCornerShape(10)
                                        )
                                        .padding(12.dp)

                                ) {
                                    // Изображение события
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = emptyList[item],
                                        modifier = Modifier.size(48.dp)
                                    )

                                    Spacer(Modifier.width(12.dp))
                                    // Содержание события
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    )
                                    {
                                        // Название события
                                        Text(
                                            text = emptyList[item],
                                            style = TextStyle(
                                                fontSize = Typography.bodyMedium.fontSize,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = colorScheme.primary
                                        )
                                        // Время начала события
                                        Text(
                                            text = emptyList[item],
                                            style = TextStyle(
                                                fontSize = Typography.bodySmall.fontSize,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            color = colorScheme.secondary
                                        )

                                    }
                                }
                                if (item != emptyList.size) {
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Pinned entities Контейнер
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = colorScheme.surface, shape = RoundedCornerShape(10))
                        .border(BorderStroke(2.dp, colorScheme.outline), shape = RoundedCornerShape(10))
                        .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)

                )
                {
                    // Заголовок блока
                    Text(
                        text = "Pinned entites",
                        style = TextStyle(
                            fontSize = Typography.titleMedium.fontSize,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colorScheme.primary
                    )

                    Spacer(Modifier.height(16.dp))
                    // Проверка на наличие событий
                    if (emptyList.size == 0) {
                        Text(text = "Oops. Maybe you don't have an active events. Go to create one")
                    } else {
                        // Если сущности есть, то отрисовывается контейнер с ними
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(0.dp, 200.dp)
                        ) {
                            items(emptyList.size) { item ->
                                // Контейнер события
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = colorScheme.tertiaryContainer,
                                            shape = RoundedCornerShape(10)
                                        )
                                        .padding(12.dp)

                                ) {
                                    // Изображение события
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = emptyList[item],
                                        modifier = Modifier.size(48.dp)
                                    )

                                    Spacer(Modifier.width(12.dp))
                                    // Содержание события
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    )
                                    {
                                        // Название события
                                        Text(
                                            text = emptyList[item],
                                            style = TextStyle(
                                                fontSize = Typography.bodyMedium.fontSize,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = colorScheme.primary
                                        )
                                        // Время начала события
                                        Text(
                                            text = emptyList[item],
                                            style = TextStyle(
                                                fontSize = Typography.bodySmall.fontSize,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            color = colorScheme.secondary
                                        )

                                    }
                                }
                                if (item != emptyList.size) {
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            HorizontalDivider(thickness = 1.dp, color= colorScheme.outline)
        }

    }
}




// шаблон контейнера иконки быстрого доступа
@Composable
fun QuickBlock(image: Int, title: String, desc: String, modifier: Modifier, color:Color ){
    // контейнер шаблона иконки быстрого доступа
    Column(
        verticalArrangement = Arrangement.Center,
        modifier= modifier
            .height(80.dp)
            .border(BorderStroke(2.dp, color=colorScheme.outline), shape = RoundedCornerShape(10))
            .background(color= colorScheme.surface, shape = RoundedCornerShape(10))
            .padding(20.dp)
    ){
        // Строка с иконкой и заголовком
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            // Иконка
            Image(
                painter= painterResource(image),
                contentDescription = desc,
                modifier=Modifier.size(16.dp),
                colorFilter = ColorFilter.tint(color)
            )

            Spacer(Modifier.width(8.dp))
            // Текст заголовка
            Text(text = title, style= TextStyle(fontSize = Typography.bodyMedium.fontSize, fontWeight = FontWeight.Bold), color=colorScheme.primary)
        }

        Spacer(Modifier.height(10.dp))
        // Текст описания
        Text(text = desc, style= TextStyle(fontSize = Typography.bodySmall.fontSize, fontWeight = FontWeight.Normal), color=colorScheme.secondary)
    }
}

@Composable
fun BottomIcons(image: Int, title: String){

    var color = if(title == "home") colorScheme.tertiary else colorScheme.secondary
    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {

        Image(
            painter = painterResource(image),
            contentDescription = title,
            colorFilter=ColorFilter.tint(color),
            modifier= Modifier.size(20.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(text= title, style = Typography.bodySmall, color= color )
    }
}

fun changeTheme(context: Context): Unit{
    val preferences: SharedPreferences = context.getSharedPreferences("my_app_preferences", Context.MODE_PRIVATE)

    preferences.edit { putBoolean("theme", !preferences.getBoolean("theme", false)) }
}