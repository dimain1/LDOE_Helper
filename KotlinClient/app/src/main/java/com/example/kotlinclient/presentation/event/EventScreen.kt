package com.example.kotlinclient.presentation.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.R
import com.example.kotlinclient.presentation.LocalImage
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.ui.theme.Typography
import java.time.format.DateTimeFormatter

@Composable
fun EventScreen(
    events:List<Event>,
    onDeleteClick: (Long) -> Unit,
    onEditClick: (Long) -> Unit,
    onCreateClick: () -> Unit,
    paddingValues: PaddingValues){

    // Контейнер всего экрана
    Column(
        modifier=Modifier
            .fillMaxSize()
            .padding(paddingValues)
    )
    {
        HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)
        // Основной контейнер экрана
        Column(
            modifier=Modifier
                .background(color = colorScheme.secondaryContainer)
                .fillMaxWidth()
                .weight(1f)

        )
        {
            Spacer(Modifier.height(16.dp))
            // Контейнер верхней части(Заголовок + Кнопка)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier= Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            {
                // Заголовок экрана
                Text(text ="Event Notifications", style= Typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color= colorScheme.primary)
                // Кнопка экрана(Создание ивента)
                Button(
                    colors= ButtonDefaults.buttonColors(containerColor = colorScheme.tertiary, contentColor = Color.White),
                    shape= RoundedCornerShape(25),
                    modifier= Modifier,

                    onClick = {onCreateClick()}
                )
                {
                    // Иконка внутри кнопки
                    Icon(Icons.Default.Add, "Add", tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    // Текст внутри кнопки
                    Text(text= "Create Event",style= Typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color= Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))

            HorizontalDivider(thickness = 1.dp, color= colorScheme.outline)

            // Список ивентов
            LazyColumn(Modifier                .padding(horizontal = 16.dp)) {
                item {
                    Spacer(Modifier.height(16.dp))
                }
                items(events.size) {item ->
                    // Контейнер ивента
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier= Modifier
                            .fillMaxWidth()
                            .background(color=colorScheme.surface, shape = RoundedCornerShape(10))
                            .border(width = 2.dp, color= colorScheme.outline, shape=RoundedCornerShape(10)),
                    )
                    {

                        LocalImage(events[item].image, 200, true)


                        HorizontalDivider(thickness = 1.dp, color=colorScheme.outline)

                        Spacer(Modifier.height(20.dp))
                        // Контейнер для описания ивента
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier= Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)

                        )
                        {
                            // Столбец информации
                            Column(
                                modifier= Modifier.weight(1f)
                            ) {
                                // Название ивента
                                Text(text =events[item]?.name ?: "", style= Typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color= colorScheme.primary)

                                Spacer(Modifier.height(16.dp))

                                // Дата
                                Row(
                                    modifier= Modifier.fillMaxWidth()
                                )
                                {
                                    Icon(
                                        Icons.Default.DateRange,
                                        "Date",
                                        modifier= Modifier.size(16.dp),
                                        tint=colorScheme.secondary)

                                    Spacer(Modifier.width(10.dp))

                                    Text(
                                        text= events[item].start_time.month.toString() + " " + events[item].start_time.dayOfMonth.toString(),
                                        style=Typography.bodyMedium,
                                        color=colorScheme.secondary)
                                }

                                Spacer(Modifier.height(8.dp))

                                // Время
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ){
                                    Icon(
                                        Icons.Default.DateRange,
                                        "Date",
                                        modifier= Modifier.size(16.dp),
                                        tint=colorScheme.secondary)

                                    Spacer(Modifier.width(10.dp))

                                    Text(text= "${events[item].start_time.format(DateTimeFormatter.ISO_LOCAL_TIME)} - ${events[item].end_time.format(DateTimeFormatter.ISO_LOCAL_TIME)}", style=Typography.bodyMedium, color=colorScheme.secondary)
                                }
                            }
                            // Столбец Кнопок
                            Row(
                                modifier= Modifier
                            ){
                                Image(
                                    painterResource(R.drawable.pencil),
                                    contentDescription = "Edit Event",
                                    modifier=Modifier.size(16.dp)
                                        .clickable(onClick = { onEditClick(events[item].id)} ),
                                    colorFilter = ColorFilter.tint(colorScheme.secondary)
                                )
                                Spacer(Modifier.width(16.dp))
                                Image(
                                    painterResource(R.drawable.trash_event),
                                    contentDescription = "Edit Event",
                                    modifier=Modifier.size(16.dp)
                                        .clickable(onClick = { onDeleteClick(events[item].id)} ),
                                    colorFilter = ColorFilter.tint(colorScheme.secondary)
                                )

                            }
                        }
                        Spacer(Modifier.height(32.dp))

                    }

                    if( item != events.size -1 ){
                        Spacer(Modifier.height(16.dp))
                    }

                }
                item {
                    Spacer(Modifier.height(16.dp))
                }
            }

        }

        HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)
    }

}