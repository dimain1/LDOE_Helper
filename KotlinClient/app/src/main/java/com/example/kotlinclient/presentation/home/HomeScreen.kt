package com.example.kotlinclient.presentation.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.runtime.collectAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import android.widget.Toast
import com.example.kotlinclient.presentation.overlay.OverlayService
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import com.example.kotlinclient.ui.theme.ServiceStopButtonColor
import com.example.kotlinclient.presentation.utility.uiComponent.LocalImage
import org.koin.compose.koinInject
import com.example.kotlinclient.presentation.utility.uiComponent.AnimatedTimer
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.entity.GameContent
import com.example.kotlinclient.state_management.viewModel.DialogType
import com.example.kotlinclient.state_management.viewModel.HomeAction
import com.example.kotlinclient.state_management.viewModel.HomeUiState
import com.example.kotlinclient.ui.theme.InfoIconColor
import com.example.kotlinclient.ui.theme.LinearGradientStartColor
import com.example.kotlinclient.ui.theme.NotificationIconColor
import com.example.kotlinclient.ui.theme.ServiceFloatingButtonColor
import com.example.kotlinclient.ui.theme.TemplateIconColor
import com.example.kotlinclient.ui.theme.Typography
import com.example.kotlinclient.ui.theme.linearGradientEndColor
import java.time.ZoneId


//Главный экран приложения
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
    paddingValues: PaddingValues
) {

    val scrollState = rememberScrollState()
    val context: Context = LocalContext.current
    val isOverlayRunning by OverlayService.isRunning.collectAsState()
    val session: UserSessionProvider = koinInject()
    val user by session.user.collectAsState()

    var showModalEvent by remember { mutableStateOf(false) }



    Column(
        Modifier
            .fillMaxSize()
            .padding(paddingValues)
    )
    {

//        // хедер всего приложения
//        AppHeader()

        HorizontalDivider(
            thickness = 1.dp,
            color = colorScheme.outline
        ) // Разделитель между заголовком приложения и контейнером
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
                            painter = painterResource(R.drawable.service_button),
                            contentDescription = "Иконка сервиса",
                            colorFilter = ColorFilter.tint(Color.White),
                            modifier = Modifier
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
                            text = "Игровой оверлей",
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = "Создавай события прямо во время игры",
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
                    onClick = {
                        when {
                            isOverlayRunning -> context.stopService(
                                Intent(context, OverlayService::class.java)
                            )
                            user == null -> Toast.makeText(
                                context,
                                "Войдите в аккаунт, чтобы запустить оверлей",
                                Toast.LENGTH_SHORT
                            ).show()
                            else -> launchOverlay(context)
                        }
                    },
                    shape = RoundedCornerShape(15),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = if (isOverlayRunning) ServiceStopButtonColor else colorScheme.tertiary
                    ),
                    modifier = Modifier.height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(
                                if (isOverlayRunning) R.drawable.service_stop_button
                                else R.drawable.service_start_button
                            ),
                            contentDescription = if (isOverlayRunning) "Закрыть" else "Запустить",
                            colorFilter = ColorFilter.tint(
                                if (isOverlayRunning) ServiceStopButtonColor else colorScheme.tertiary
                            )
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (isOverlayRunning) "Закрыть оверлей" else "Запустить оверлей",
                            style = TextStyle(
                                fontSize = Typography.bodyLarge.fontSize,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isOverlayRunning) ServiceStopButtonColor else colorScheme.tertiary
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
                    //Левая верхняя иконка — переход на экран настроек
                    QuickBlock(
                        image = R.drawable.settings,
                        title = "Настройки",
                        desc = "Аккаунт и параметры",
                        modifier = Modifier.weight(1f),
                        color = colorScheme.secondary,
                        onClick = { onAction(HomeAction.ToSettings) }
                    )

                    Spacer(Modifier.width(12.dp))

                    //Правая верхняя иконка
                    QuickBlock(
                        R.drawable.template,
                        "Шаблоны",
                        "Ваши шаблоны",
                        Modifier.weight(1f),
                        TemplateIconColor,
                        { onAction(HomeAction.ToTemplate) }
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
                        "Мои события",
                        "${uiState.upcomingEvents.size} предстоящих",
                        Modifier.weight(1f),
                        NotificationIconColor,
                        { onAction(HomeAction.ToEvent) }
                    )

                    Spacer(Modifier.width(12.dp))

                    //Правая нижняя иконка
                    QuickBlock(
                        R.drawable.info,
                        "База данных",
                        "Просмотр",
                        Modifier.weight(1f),
                        InfoIconColor,
                        { onAction(HomeAction.ToInfo) }
                    )

                }
            }

            Spacer(Modifier.height(16.dp))
            // Upcoming events

            QuickList(
                "Предстоящие события",
                "Предстоящих событий нет. Создайте новое!",
                uiState.upcomingEvents,
                onAction,
            )

            Spacer(Modifier.height(16.dp))

            QuickList(
                "Закреплённые",
                "Нет закреплённых элементов. Закрепите что-нибудь!",
                uiState.pinnedEntity,
                onAction,
            )

            Spacer(Modifier.height(16.dp))
        }

        HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)
    }

}

@Composable
fun <T> QuickList(
    title: String,
    emptyErrorText: String,
    listOfValue: List<T>,
    onAction: (HomeAction) -> Unit,
) {

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

        if (listOfValue.size == 0) {

            Text(text = emptyErrorText, style= Typography.bodyMedium, color=colorScheme.secondary)
        } else {


            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(0.dp, 200.dp)
            ) {
                items(listOfValue.size) { item ->

                    if (listOfValue[item] is Event) {
                        val event: Event = listOfValue[item] as Event
                        QuickListItem(
                            event?.image, event?.name ?: "",
                            { onAction(HomeAction.OpenDialog(DialogType.EventViewDetailsDialog(event))) },
                            description = {
                                AnimatedTimer(event.startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),event.endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                            }
                        )
                        {
                            Image(
                                painter = painterResource(R.drawable.trash),
                                contentDescription = "Удалить",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable(onClick = { onAction(HomeAction.DeleteEvent(event.id!!)) }),
                                colorFilter = ColorFilter.tint(colorScheme.primary)
                            )
                        }
                    }

                    if (listOfValue[item] is GameContent) {
                        val content: GameContent = listOfValue[item] as GameContent
                        QuickListItem(
                            content?.image ?: "",
                            content.name,
                            onItemClick = {onAction(HomeAction.OpenDialog(DialogType.GameContentViewDetailsDialog(content)))},
                            description = {
                                Text(
                                    text = content?.description ?: "",
                                    style = TextStyle(
                                        fontSize = Typography.bodySmall.fontSize,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = colorScheme.secondary,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 2
                                )
                            }
                        )
                        {
                            Image(
                                painter = painterResource(if (content.pinned == false) R.drawable.pinned_off else R.drawable.pinned_on),
                                contentDescription = "Закреплено",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable(onClick = {
                                        onAction(HomeAction.TogglePin(content.id, !content.pinned))
                                    }),
                                colorFilter = if (content.pinned == false) null else ColorFilter.tint(
                                    colorScheme.tertiary
                                )
                            )
                        }
                    }


                    if (item != listOfValue.size - 1) {
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

        }

    }
}

@Composable
fun QuickListItem(
    fileName: String?,
    name: String,
    onItemClick: () -> Unit = {},
    description: @Composable () -> Unit,
    image: @Composable () -> Unit
) {
    // Контейнер события
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(10)
            )
            .border(2.dp,colorScheme.outline, RoundedCornerShape(10))
            .padding(12.dp)
            .clickable(onClick = onItemClick)

    ) {
        Box(
            modifier = Modifier
                .border(1.dp, colorScheme.outline, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
        ) {
            LocalImage(fileName, Modifier.size(48.dp),size=48)
        }


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
                text = name,
                style = TextStyle(
                    fontSize = Typography.bodyMedium.fontSize,
                    fontWeight = FontWeight.Bold
                ),
                color = colorScheme.primary,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )

            Spacer(Modifier.height(5.dp))

            // Краткое описание
            description()
        }
        image()
    }
}


// шаблон контейнера иконки быстрого доступа
@Composable
fun QuickBlock(
    image: Int,
    title: String,
    desc: String,
    modifier: Modifier,
    color: Color,
    onClick: () -> Unit
) {
    // контейнер шаблона иконки быстрого доступа
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .height(80.dp)
            .border(BorderStroke(2.dp, color = colorScheme.outline), shape = RoundedCornerShape(10))
            .background(color = colorScheme.surface, shape = RoundedCornerShape(10))
            .clickable(onClick = { onClick() })
            .padding(20.dp)
    ) {
        // Строка с иконкой и заголовком
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка
            Image(
                painter = painterResource(image),
                contentDescription = desc,
                modifier = Modifier.size(16.dp),
                colorFilter = ColorFilter.tint(color)
            )

            Spacer(Modifier.width(8.dp))
            // Текст заголовка
            Text(
                text = title,
                style = TextStyle(
                    fontSize = Typography.bodyMedium.fontSize,
                    fontWeight = FontWeight.Bold
                ),
                color = colorScheme.primary
            )
        }

        Spacer(Modifier.height(10.dp))
        // Текст описания
        Text(
            text = desc,
            style = TextStyle(
                fontSize = Typography.bodySmall.fontSize,
                fontWeight = FontWeight.Normal
            ),
            color = colorScheme.secondary
        )
    }
}

private fun launchOverlay(context: Context){
    if (!Settings.canDrawOverlays(context)) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
    }
    else{
        val intent = Intent(context, OverlayService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}


