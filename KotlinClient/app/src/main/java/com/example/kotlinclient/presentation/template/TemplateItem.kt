package com.example.kotlinclient.presentation.template

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.presentation.utility.uiComponent.LocalImage
import com.example.kotlinclient.presentation.utility.getStringTimeByDuration
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.state_management.viewModel.EventTemplateAction
import com.example.kotlinclient.state_management.viewModel.EventTemplateDialogType
import com.example.kotlinclient.ui.theme.Typography

@Composable
fun TemplateItem(
    template: EventTemplate?,
    onAction: (EventTemplateAction) -> Unit,
    imageSize: Int,
    tempModifierConteiner: Modifier = Modifier,
    onClick: (EventTemplate?) -> Unit,
    rightColumn: @Composable () -> Unit = {}

) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = tempModifierConteiner
            .fillMaxWidth()
            .background(color = colorScheme.surface, shape = RoundedCornerShape(30.dp))
            .border(2.dp, colorScheme.outline, RoundedCornerShape(30.dp))
            .clickable(onClick = { onClick(template) })
    )
    {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(imageSize.dp)
        ) {
            LocalImage(
                template?.image,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
                true
            )
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
            )
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Название ивента
                    Box(modifier=Modifier.weight(1f)) {
                        Text(
                            text = template?.name ?: "",
                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = colorScheme.primary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(12.dp))

                    rightColumn()

                }
                Spacer(Modifier.height(12.dp))

                Spacer(
                    Modifier
                        .heightIn(min = 0.dp)
                        .weight(1f)
                )

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

                    val duration = template?.duration ?: 0

                    Text(
                        text =
                            if(duration < 60_000L) "Меньше минуты"
                            else if(duration > 86_400_000L * 30) "Больше месяца"
                            else {
                                getStringTimeByDuration(duration)
                            },
                        style = Typography.bodyMedium,
                        color = colorScheme.secondary
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

        }

    }
}


