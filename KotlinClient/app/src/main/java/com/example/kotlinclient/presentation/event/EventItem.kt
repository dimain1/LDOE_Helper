package com.example.kotlinclient.presentation.event

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.presentation.utility.uiComponent.LocalImage
import com.example.kotlinclient.presentation.utility.uiComponent.NowIndicator
import com.example.kotlinclient.presentation.utility.uiComponent.AnimatedTimer
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.ui.theme.Typography
import java.time.ZoneId

@Composable
fun EventItem(
    event: Event,
    imageSize: Int,
    onEventClick: (Event) -> Unit,
    isNow: Boolean = false,
    rightColumn: @Composable () -> Unit = {}

) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colorScheme.surface, shape = RoundedCornerShape(30.dp))
            .border(
                width = if (isNow) 3.dp else 2.dp,
                color = if (isNow) colorScheme.tertiary else colorScheme.outline,
                shape = RoundedCornerShape(30.dp)
            )
            .clickable(onClick = { onEventClick(event) })
    )
    {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(imageSize.dp)
        ) {
            LocalImage(
                event.image,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
                true
            )
            if (isNow) {
                Box(modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)) {
                    NowIndicator()
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)

        Spacer(Modifier.height(20.dp))
        // Контейнер для описания ивента
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)

        )
        {
            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Название ивента
                Text(
                    text = event?.name ?: "",
                    style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(16.dp))

//                // Дата
//                Row(
//                    modifier = Modifier.fillMaxWidth()
//                )
//                {
//                    Icon(
//                        Icons.Default.DateRange,
//                        "Date",
//                        modifier = Modifier.size(16.dp),
//                        tint = colorScheme.secondary
//                    )
//
//                    Spacer(Modifier.width(10.dp))
//
//                    Text(
//                        text = "${event.start_time.dayOfMonth} ${event.start_time.month} - ${event.end_time.dayOfMonth} ${event.end_time.month}",
//                        style = Typography.bodyMedium,
//                        color = colorScheme.secondary
//                    )
//                }
//
//                Spacer(Modifier.height(8.dp))
//
//                // Время
//                Row(
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Icon(
//                        Icons.Default.DateRange,
//                        "Date",
//                        modifier = Modifier.size(16.dp),
//                        tint = colorScheme.secondary
//                    )
//
//                    Spacer(Modifier.width(10.dp))
//
//                    Text(
//                        text = "${event.start_time.format(DateTimeFormatter.ofPattern("HH:mm:ss"))} - ${
//                            event.end_time.format(
//                                DateTimeFormatter.ofPattern("HH:mm:ss")
//                            )
//                        }", style = Typography.bodyMedium, color = colorScheme.secondary
//                    )
//                }

                AnimatedTimer(
                    event.start_time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    event.end_time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                )

                Spacer(Modifier.height(16.dp))

            }
            // Столбец Кнопок
            rightColumn()
        }

    }
}