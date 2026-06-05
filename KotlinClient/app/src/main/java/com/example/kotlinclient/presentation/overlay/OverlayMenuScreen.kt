package com.example.kotlinclient.presentation.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.ui.theme.Typography

@Composable
fun OverlayMenuScreen(
    onEvents: () -> Unit,
    onCreate: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 720.dp)
            .background(colorScheme.secondaryContainer, RoundedCornerShape(14.dp))
            .border(1.dp, colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LDOE Helper",
                style = TextStyle(fontSize = Typography.titleMedium.fontSize, fontWeight = FontWeight.Bold),
                color = colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            OverlayButton(text = "Закрыть", onClick = onClose)
        }

        Spacer(Modifier.height(14.dp))

        val isHorizontal = with(LocalConfiguration.current) { screenWidthDp > screenHeightDp }

        if (isHorizontal) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OverlayMenuCard(
                    title = "События",
                    description = "Активные события с обратным отсчётом",
                    onClick = onEvents,
                    modifier = Modifier.weight(1f)
                )
                OverlayMenuCard(
                    title = "Создать событие",
                    description = "Новое событие прямо сейчас",
                    onClick = onCreate,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OverlayMenuCard(
                    title = "События",
                    description = "Активные события с обратным отсчётом",
                    onClick = onEvents,
                    modifier = Modifier.fillMaxWidth()
                )
                OverlayMenuCard(
                    title = "Создать событие",
                    description = "Новое событие прямо сейчас",
                    onClick = onCreate,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun OverlayMenuCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = BorderStroke(1.dp, colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = TextStyle(fontSize = Typography.titleMedium.fontSize, fontWeight = FontWeight.Bold),
                color = colorScheme.primary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                style = Typography.bodyMedium,
                color = colorScheme.secondary
            )
        }
    }
}
