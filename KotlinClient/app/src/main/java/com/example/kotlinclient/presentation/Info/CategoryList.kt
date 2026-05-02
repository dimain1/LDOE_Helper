package com.example.kotlinclient.presentation.Info

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.state_management.entity.ContentType
import com.example.kotlinclient.ui.theme.Typography

// Список категорий
@Composable
fun CategoryList(
    categoryList: List<ContentType> = listOf(),
    selectedCategory: Long = 0,
    onCategoryClick: (Long) -> Unit
)
{

    LazyRow(modifier=
        Modifier
            .fillMaxWidth(),

        )
    {
        items(categoryList.size) {item ->
            // Выбранная категория должна быть красного(Контрастного) цвета
            var colorCategory: Color
            var textColor: Color
            if(categoryList[item].id == selectedCategory) {
                colorCategory= colorScheme.tertiary
                textColor = Color.White
            }
            else {
                colorCategory = colorScheme.surface
                textColor = colorScheme.primary
            }
            // Контейнер категории
            Box(
                contentAlignment = Alignment.Center,
                modifier= Modifier
                    .clickable(onClick = { onCategoryClick(categoryList[item].id) })
                    .background(colorCategory, shape=RoundedCornerShape(35))
                    .border(1.dp, colorScheme.outline, RoundedCornerShape(35))
                    .padding(16.dp)
            ){
                //Текст категории
                Text(text = categoryList[item].name, style= Typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color= textColor)

            }

            if(item != categoryList.size-1){
                Spacer(Modifier.width(12.dp))
            }
        }

    }
}