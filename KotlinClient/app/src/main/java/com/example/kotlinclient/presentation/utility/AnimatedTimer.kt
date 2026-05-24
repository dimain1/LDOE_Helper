package com.example.kotlinclient.presentation.utility

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinclient.ui.theme.Typography
import kotlinx.coroutines.delay

@Composable
fun AnimatedTimer(
    targetTime: Long,
    modifier: Modifier = Modifier
) {


    var remaining by remember(targetTime) {
        mutableLongStateOf(targetTime - System.currentTimeMillis())
    }

    if (remaining < 0)
        remaining = 0

    LaunchedEffect(targetTime) {
        while (remaining > 0) {
            delay(
                if (remaining > 86_400_000)
                    86_400_000
                else
                    60_000
            )

            remaining = targetTime - System.currentTimeMillis()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val animatedColor by animateColorAsState(
        targetValue = when {
            remaining <= 0 -> colorScheme.secondary
            else -> colorScheme.primary
        },
        label = "color"
    )

    val days = remaining / 86_400_000
    val hours = (remaining % 86_400_000) / 3_600_000
    val minutes = (remaining % 3_600_000) / 60_000
    val seconds = (remaining % 60_000) / 1_000

    Row(
        modifier = modifier
            .scale(1f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (remaining == 0L) "Закончено" else "Закончится через: ${
                if (days == 0L) String.format(
                    "%2d:%02d",
                    hours,
                    minutes
                ) else {
                    String.format("%2d дня", days)
                }
            }",
            color = animatedColor,
            style = Typography.bodyMedium
        )
    }
}