package com.example.kotlinclient.presentation.template

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.R
import com.example.kotlinclient.presentation.LocalImage
import com.example.kotlinclient.ui.theme.Typography

@Composable
fun TemplateItem(name: String, description: String?, duration: Long, image: String?, imageSize: Int , tempModifierConteiner: Modifier = Modifier ,rightColumn : @Composable ()->Unit = {})
{
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = tempModifierConteiner
            .fillMaxWidth()
            .background(color=colorScheme.surface, shape = RoundedCornerShape(30.dp))
            .border(2.dp, colorScheme.outline, RoundedCornerShape(30.dp))
    )
    {

        Box(
            contentAlignment = Alignment.Center,
            modifier=Modifier
                .fillMaxWidth()
                .height(imageSize.dp)
        ) {
            LocalImage(
                image,
                modifier=  Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
                true)
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
                modifier= Modifier.weight(1f)
            )
            {
                // Название ивента
                Text(
                    text = name,
                    style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(12.dp))

                // Описание
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
                {
                    Text(
                        text = description ?: "",
                        style = Typography.bodyMedium,
                        color = colorScheme.secondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.heightIn(min = 0.dp).weight(1f))

                // Время
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Star,
                        "Time",
                        modifier = Modifier.size(16.dp),
                        tint = colorScheme.secondary
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = (duration / 1000 / 60).toString() + " " + "Minutes",
                        style = Typography.bodyMedium,
                        color = colorScheme.secondary
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            rightColumn()
        }

    }
}
