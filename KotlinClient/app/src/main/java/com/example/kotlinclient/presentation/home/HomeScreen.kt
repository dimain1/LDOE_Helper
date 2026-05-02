package com.example.kotlinclient.presentation.home

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinclient.R
import com.example.kotlinclient.presentation.LocalImage
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.entity.GameContent
import com.example.kotlinclient.ui.theme.InfoIconColor
import com.example.kotlinclient.ui.theme.LinearGradientStartColor
import com.example.kotlinclient.ui.theme.NotificationIconColor
import com.example.kotlinclient.ui.theme.ServiceFloatingButtonColor
import com.example.kotlinclient.ui.theme.TemplateIconColor
import com.example.kotlinclient.ui.theme.Typography
import com.example.kotlinclient.ui.theme.linearGradientEndColor



//Главный экран приложения
@Composable
fun HomeScreen(
    upcomingEvents: List<Event>,
    onDeleteClick: (Long) -> Unit,
    pinnedEntity: List<GameContent>,
    onPinClick: (Long, Boolean) -> Unit,
    onNewEventClick: () -> Unit,
    onTemplateClick: () -> Unit,
    onMyEventClick: () -> Unit,
    onDatabaseClick: () -> Unit,
    paddingValues: PaddingValues){

    val scrollState = rememberScrollState()
    val content: Context = LocalContext.current
    //var emptyList: List<String> = emptyList<String>()

    Column(
        Modifier
            .fillMaxSize()
            .padding(paddingValues)
    )
    {

//        // хедер всего приложения
//        AppHeader()

        HorizontalDivider(thickness = 1.dp, color = colorScheme.outline) // Разделитель между заголовком приложения и контейнером
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    // Контейнер иконки сервиса
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
                    onClick = { },
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
                        color= Color.Red,
                        onClick = onNewEventClick
                    )

                    Spacer(Modifier.width(12.dp))

                    //Правая верхняя иконка
                    QuickBlock(
                        R.drawable.template,
                        "Templates",
                        "Your saved templates",
                        Modifier.weight(1f),
                        TemplateIconColor,
                        onTemplateClick
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
                        "${upcomingEvents.size} upcoming",
                        Modifier.weight(1f),
                        NotificationIconColor,
                        onMyEventClick
                    )

                    Spacer(Modifier.width(12.dp))

                    //Правая нижняя иконка
                    QuickBlock(
                        R.drawable.info,
                        "Database",
                        "Browse items",
                        Modifier.weight(1f),
                        InfoIconColor,
                        onDatabaseClick
                    )

                }
            }

            Spacer(Modifier.height(16.dp))

            // Upcoming events
            QuickList("Upcoming Events"){
                if (upcomingEvents.size == 0) {
                    Text(text = "Oops. Maybe you don't have upcoming events. Go to create one")
                } else {
                    // Если сущности есть, то отрисовывается контейнер с ними
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(0.dp, 200.dp)
                    ) {
                        items(upcomingEvents.size) { item ->
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
                                // Изображение события !!!!!!!!!!!!!!!!
                                LocalImage(upcomingEvents[item].image, 48)

                                Spacer(Modifier.width(12.dp))
                                // Содержание события
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                                {
                                    // Название события
                                    Text(
                                        text = upcomingEvents[item]?.name ?: "",
                                        style = TextStyle(
                                            fontSize = Typography.bodyMedium.fontSize,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = colorScheme.primary
                                    )
                                    // Время конца события
                                    Text(
                                        text = upcomingEvents[item].end_time.toString(),
                                        style = TextStyle(
                                            fontSize = Typography.bodySmall.fontSize,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = colorScheme.secondary,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1
                                    )

                                }
                                Image(
                                    painter= painterResource(R.drawable.trash),
                                    contentDescription = "Delete Image",
                                    modifier= Modifier.size(32.dp).clickable(onClick = { onDeleteClick(upcomingEvents[item].id) }),
                                    colorFilter = ColorFilter.tint(colorScheme.primary)
                                )
                            }
                            if (item != upcomingEvents.size - 1) {
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Pinned entities Контейнер

            QuickList("Pinned Entity"){
                if (pinnedEntity.size == 0) {

                    Text(text = "Oops. Maybe you don't have an pinned entities. Go to pin one")
                } else {
                    // Если сущности есть, то отрисовывается контейнер с ними
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(0.dp, 200.dp)
                    ) {
                        items(pinnedEntity.size) { item ->
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
                                // Изображение события !!!!!!!!!!!!!!!!
                                LocalImage(pinnedEntity[item].image, 48)

                                Spacer(Modifier.width(12.dp))
                                // Содержание события
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                                {
                                    // Название события
                                    Text(
                                        text = pinnedEntity[item].name,
                                        style = TextStyle(
                                            fontSize = Typography.bodyMedium.fontSize,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = colorScheme.primary
                                    )
                                    // Краткое описание
                                    Text(
                                        text = pinnedEntity[item]?.description ?: "",
                                        style = TextStyle(
                                            fontSize = Typography.bodySmall.fontSize,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = colorScheme.secondary,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 2
                                    )

                                }
                                Image(
                                    painter= painterResource ( if(pinnedEntity[item].pinned == false) R.drawable.pinned_off else R.drawable.pinned_on),
                                    contentDescription = "Pinned Image",
                                    modifier= Modifier.size(32.dp).clickable(onClick = { onPinClick(pinnedEntity[item].id, !pinnedEntity[item].pinned) }),
                                    colorFilter= if(pinnedEntity[item].pinned == false) null else ColorFilter.tint(colorScheme.tertiary)
                                )
                            }
                            if (item != pinnedEntity.size - 1) {
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

@Composable
fun QuickList(title: String, list: @Composable () -> Unit){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorScheme.surface, shape = RoundedCornerShape(10))
            .border(
                BorderStroke(2.dp, colorScheme.outline),
                shape = RoundedCornerShape(10)
            )
            .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)

    )
    {
        // Заголовок блока
        Text(
            text = title,
            style = TextStyle(
                fontSize = Typography.titleMedium.fontSize,
                fontWeight = FontWeight.Bold
            ),
            color = colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        list()


        // Проверка на наличие сущностей

    }
}


// шаблон контейнера иконки быстрого доступа
@Composable
fun QuickBlock(image: Int, title: String, desc: String, modifier: Modifier, color:Color, onClick: () -> Unit){
    // контейнер шаблона иконки быстрого доступа
    Column(
        verticalArrangement = Arrangement.Center,
        modifier= modifier
            .height(80.dp)
            .border(BorderStroke(2.dp, color = colorScheme.outline), shape = RoundedCornerShape(10))
            .background(color = colorScheme.surface, shape = RoundedCornerShape(10))
            .clickable(onClick = {onClick() })
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


