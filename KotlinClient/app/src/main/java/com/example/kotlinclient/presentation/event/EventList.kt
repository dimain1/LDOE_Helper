package com.example.kotlinclient.presentation.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.R
import com.example.kotlinclient.state_management.entity.Event
import java.time.LocalDateTime

@Composable
fun EventList(
    events: List<Event>,
    imageSize: Int,
    onDeleteClick: (Long) -> Unit,
    onEditClick: (Event) -> Unit,
    onEventClick: (Event) -> Unit,
    currentTime: LocalDateTime,
) {
    LazyColumn(Modifier.padding(horizontal = 16.dp)) {

        val sortedEvents = events.sortedByDescending { event -> event.endTime }


        items(events.size) { item ->

            val event: Event = sortedEvents[item]
            // Контейнер ивента

            // Столбец информации
            EventItem(
                event,
                200,
                onEventClick= onEventClick,
                isNow = currentTime >= event.startTime && currentTime <= event.endTime,
                )
            {
                // Столбец Кнопок
                Row(
                    modifier = Modifier
                )
                {
                    Image(
                        painterResource(R.drawable.pencil),
                        contentDescription = "Edit Event",
                        modifier = Modifier.size(16.dp)
                            .clickable(onClick = { onEditClick(event) }),
                        colorFilter = ColorFilter.tint(colorScheme.secondary)
                    )

                    Spacer(Modifier.width(16.dp))

                    Image(
                        painterResource(R.drawable.trash_event),
                        contentDescription = "Delete Event",
                        modifier = Modifier.size(16.dp)
                            .clickable(onClick = { onDeleteClick(event.id!!) }),
                        colorFilter = ColorFilter.tint(colorScheme.secondary)
                    )
                }
            }

            if (item != events.size - 1) {
                Spacer(Modifier.height(16.dp))
            }

        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}