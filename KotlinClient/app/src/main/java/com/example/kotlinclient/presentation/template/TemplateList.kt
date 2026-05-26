package com.example.kotlinclient.presentation.template

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
import com.example.kotlinclient.state_management.entity.EventTemplate

@Composable
fun TemplateList(
    templates: List<EventTemplate>,
    imageSize: Int,
    onDeleteClick: (Long) -> Unit,
    onEditClick: (EventTemplate) -> Unit,
) {
    LazyColumn(Modifier.padding(horizontal = 16.dp)) {

        items(templates.size) { item ->

            val template: EventTemplate = templates[item]
            // Контейнер ивента

            // Столбец информации
            TemplateItem(
                template.name,
                template.description,
                template.duration,
                template.image,
                imageSize
            )
            {
                // Столбец Кнопок
                Row(
                )
                {
                    Image(
                        painterResource(R.drawable.pencil),
                        contentDescription = "Edit Event",
                        modifier = Modifier.size(16.dp)
                            .clickable(onClick = { onEditClick(template) }),
                        colorFilter = ColorFilter.tint(colorScheme.secondary)
                    )

                    Spacer(Modifier.width(16.dp))

                    Image(
                        painterResource(R.drawable.trash_event),
                        contentDescription = "Delete Event",
                        modifier = Modifier.size(16.dp)
                            .clickable(onClick = { onDeleteClick(template.id!!) }),
                        colorFilter = ColorFilter.tint(colorScheme.secondary)
                    )
                }
            }


            if (item != templates.size - 1) {
                Spacer(Modifier.height(16.dp))
            }

        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

