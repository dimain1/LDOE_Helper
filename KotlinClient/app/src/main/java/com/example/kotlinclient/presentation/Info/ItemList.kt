package com.example.kotlinclient.presentation.Info

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.ui.theme.Typography


// Список предметов(Сущностей игры)
@Composable
fun ItemList(Items: List<String> = listOf()){

    var Items = listOf("ak-47", "Glock-18","Cloth","ak-47", "Glock-18","Cloth")

    LazyColumn(modifier=Modifier
        .fillMaxWidth()

    )
    {
        items(Items.size) {item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier=Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface, shape = RoundedCornerShape(10))
                    .border(2.dp, colorScheme.outline, RoundedCornerShape(10))
                    .padding(all = 12.dp)
            ){
                // Картинка предмета(Замениться на Image)
                Icon(Icons.Default.Clear, "image",Modifier.size(48.dp).border(2.dp, colorScheme.tertiary))

                Spacer(Modifier.width(16.dp))
                // Описание предмета
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier= Modifier.weight(1f)
                )

                {
                    Text(text= Items[item].toUpperCase(), style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color= colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(text= Items[item].toLowerCase(), style = Typography.bodySmall, color=colorScheme.secondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }

                Spacer(Modifier.width(8.dp))
                // Кнопка закрепления сущности для домашнего экрана или отдельного экрана
                Icon(Icons.Default.Add, "image",Modifier.size(32.dp).border(2.dp, colorScheme.tertiary))

            }
            // Между последним элементом и краем экрана отступ не добавляем
            if(item != Items.size - 1){
                Spacer(Modifier.height(16.dp))
            }

        }
        item {
            Spacer(Modifier.height(16.dp))
        }
    }


}