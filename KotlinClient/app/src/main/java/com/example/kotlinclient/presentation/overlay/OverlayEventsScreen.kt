package com.example.kotlinclient.presentation.overlay

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.R
import com.example.kotlinclient.presentation.utility.uiComponent.AnimatedTimer
import com.example.kotlinclient.presentation.utility.uiComponent.LocalImage
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.ui.theme.Typography
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun OverlayEventsScreen(
    controller: OverlayController,
    onBack: () -> Unit
) {
    val events = remember { mutableStateListOf<Event>() }

    LaunchedEffect(Unit) {
        controller.observeEvents {
            events.clear()
            events.addAll(it.filter { e -> e.endTime > LocalDateTime.now() })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .background(colorScheme.secondaryContainer, RoundedCornerShape(14.dp))
            .border(1.dp, colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Активные события",
                style = TextStyle(fontSize = Typography.titleMedium.fontSize, fontWeight = FontWeight.Bold),
                color = colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            OverlayButton(text = "Назад", onClick = onBack)
        }

        Spacer(Modifier.height(14.dp))

        if (events.isEmpty()) {
            Text(
                text = "Нет активных событий",
                style = Typography.bodyMedium,
                color = colorScheme.secondary
            )
        } else {
            // Высота списка: 38% высоты экрана, зажатая в [180, 360] dp
            // На широких телефонах список не раздувается, на маленьких — остаётся удобным
            val listMaxHeight = (LocalConfiguration.current.screenHeightDp * 0.38f).dp
                .coerceIn(180.dp, 360.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = listMaxHeight),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(events.size) { index ->
                    OverlayEventItem(
                        event = events[index],
                        onDelete = {}
                    )
                }
            }
        }
    }
}

@Composable
fun OverlayEventItem(
    event: Event,
    onDelete: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surface, RoundedCornerShape(10.dp))
            .border(1.dp, colorScheme.outline, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        LocalImage(event.image, Modifier.size(46.dp))

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.name ?: "Без названия",
                style = TextStyle(fontSize = Typography.bodyMedium.fontSize, fontWeight = FontWeight.Bold),
                color = colorScheme.primary,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
            Spacer(Modifier.height(5.dp))
            AnimatedTimer(
                startTime = event.startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                targetTime = event.endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        }

        Spacer(Modifier.width(8.dp))

        Image(
            painter = painterResource(R.drawable.trash),
            contentDescription = "Удалить событие",
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onDelete),
            colorFilter = ColorFilter.tint(colorScheme.tertiary)
        )
    }
}
