package com.example.kotlinclient.presentation.template

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.ui.theme.Typography

@Composable
fun TemplateScreen(paddingValues: PaddingValues){

    val templateList: List<String> =listOf("Bunker", "Raid", "Farm")

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
                modifier= Modifier.fillMaxWidth()                .padding(horizontal = 16.dp)
            )
            {
                // Заголовок экрана
                Text(text ="Event Templates", style= Typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color= colorScheme.primary)
                // Кнопка экрана(Создание ивента)
                Button(
                    colors= ButtonDefaults.buttonColors(containerColor = colorScheme.tertiary, contentColor = Color.White),
                    shape= RoundedCornerShape(25),
                    modifier= Modifier,

                    onClick = {}
                )
                {
                    // Иконка внутри кнопки
                    Icon(Icons.Default.Add, "Add", tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    // Текст внутри кнопки
                    Text(text= "New Template",style= Typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color= Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))

            HorizontalDivider(thickness = 1.dp, color= colorScheme.outline)

            // Список ивентов
            LazyColumn(Modifier                .padding(horizontal = 16.dp)) {
                item {
                    Spacer(Modifier.height(16.dp))
                }
                items(templateList.size) { item ->
                    // Контейнер ивента
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier= Modifier
                            .fillMaxWidth()
                            .background(color=colorScheme.surface, shape = RoundedCornerShape(10))
                            .border(width = 2.dp, color= colorScheme.outline, shape=RoundedCornerShape(10)),
                    )
                    {
                        // Изображение ивента(Сейчас иконка)
                        Icon(
                            Icons.Default.Clear,
                            "icon",
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth()
                                .border(1.dp,colorScheme.tertiary)
                        )

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
                                Text(text =templateList[item], style= Typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color= colorScheme.primary)

                                Spacer(Modifier.height(12.dp))

                                // Описание
                                Row(
                                    modifier= Modifier.fillMaxWidth()
                                )
                                {
                                    Text(text= "Bunker reset", style=Typography.bodyMedium, color=colorScheme.secondary)
                                }

                                Spacer(Modifier.height(12.dp))

                                // Время
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                ){
                                    Icon(
                                        Icons.Default.Star,
                                        "Time",
                                        modifier= Modifier.size(16.dp),
                                        tint=colorScheme.secondary)

                                    Spacer(Modifier.width(10.dp))

                                    Text(text= "30 Minutes", style=Typography.bodyMedium, color=colorScheme.secondary)
                                }
                            }
                            // Столбец Кнопок
                            Row(
                                modifier= Modifier
                            ){
                                Icon(Icons.Default.Create, "delete", Modifier.size(18.dp), tint= colorScheme.secondary)
                                Spacer(Modifier.width(12.dp))
                                Icon(Icons.Default.Delete, "delete", Modifier.size(18.dp), tint= colorScheme.secondary)
                            }
                        }
                        Spacer(Modifier.height(32.dp))

                    }

                    if( item != templateList.size -1 ){
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