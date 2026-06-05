package com.example.kotlinclient.presentation.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.ui.theme.Typography

/**
 * Базовая поверхность для всех overlay-модалок.
 *
 * @param maxWidth       максимальная ширина панели (dp)
 * @param scrollable     false для модалок с Lazy-контентом (DatePicker, LazyColumn) —
 *                       иначе будет краш «infinite height in lazy container»
 * @param onOutsideClick вызывается при нажатии на тёмный фон за пределами панели
 */
@Composable
fun OverlayModalSurface(
    maxWidth: Int = 520,
    scrollable: Boolean = true,
    onOutsideClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val maxPanelHeight = LocalConfiguration.current.screenHeightDp.dp * 0.88f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.32f))
            .clickable(onClick = onOutsideClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth.dp)
                .heightIn(max = maxPanelHeight)
                .background(colorScheme.secondaryContainer, RoundedCornerShape(14.dp))
                .border(1.dp, colorScheme.outline, RoundedCornerShape(14.dp))
                // Блокируем всплытие кликов на тёмный фон
                .clickable(onClick = {})
                .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(16.dp),
            content = content
        )
    }
}

/**
 * Единый стиль кнопки для всего overlay — красная, как в основном приложении.
 */
@Composable
fun OverlayButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(25),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.tertiary,
            contentColor = Color.White,
            disabledContainerColor = colorScheme.outline,
            disabledContentColor = colorScheme.secondary
        )
    ) {
        Text(
            text = text,
            style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = if (enabled) Color.White else colorScheme.secondary
        )
    }
}
