package com.example.kotlinclient.presentation.info.modal

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.presentation.utility.uiComponent.LocalImage
import com.example.kotlinclient.state_management.entity.GameContent
import com.example.kotlinclient.ui.theme.Typography

@Composable
fun GameContentViewDetails(onDismiss: () -> Unit, initialData: GameContent?){

    val titleVS = rememberScrollState()
    val descVS = rememberScrollState()
    val bodyVS = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title= {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 55.dp)
            ) {

                Box(modifier = Modifier.weight(1f)) {


                    Text(
                        text = initialData?.name ?: "Название отсутствует",
                        style = Typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            lineBreak = LineBreak.Heading
                        ),
                        color = colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(titleVS),


                        )
                }
            }
        },
        text= {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().verticalScroll(bodyVS)
            ) {
                LocalImage(
                    initialData?.image,
                    Modifier
                        .height(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(2.dp, colorScheme.outline, RoundedCornerShape(20.dp))
                        .fillMaxWidth(),
                    fillAll = true
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(16.dp))

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 90.dp)
                    ) {
                        Text(
                            text = initialData?.description ?: "Описание отсутствует",
                            color = colorScheme.primary,
                            style = Typography.bodyMedium,
                            modifier = Modifier.verticalScroll(descVS)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Выводим заголовок для секции характеристик
                    Text(
                        text = "Характеристики:",
                        style = Typography.titleSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    // Вставляем рекурсивный рендеринг атрибутов
                    initialData?.attributes?.let { attributesMap ->
                        RenderAttributes(attributes = attributesMap)
                    }
                }
            }
        },
        confirmButton= {},
        dismissButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.tertiary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Закрыть",
                    style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        },
        containerColor= colorScheme.secondaryContainer,
    )
}

@Composable
fun RenderAttributes(
    attributes: Map<String, Any>,
    indentLevel: Int = 0 // Управляет отступом в глубину
) {
    val paddingStart = (indentLevel * 12).dp

    Column(
        modifier = Modifier
            .padding(start = paddingStart)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        attributes.forEach { (key, value) ->
            when (value) {
                // 1. Если это вложенный документ (Map)
                is Map<*, *> -> {
                    Text(
                        text = "- $key:",
                        style = Typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.secondary
                    )
                    // Уходим в рекурсию, увеличивая отступ
                    RenderAttributes(
                        attributes = value as Map<String, Any>,
                        indentLevel = indentLevel + 1
                    )
                }

                // 2. Если это массив (List)
                is List<*> -> {
                    Text(
                        text = "- $key: ${value.joinToString(", ")}",
                        style = Typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.secondary
                    )
                }

                // 3. Если это примитив (Long, Double, String, Boolean)
                else -> {
                    // Красиво убираем ".0" у дробных чисел, если они по факту целые
                    val formattedValue = if (value is Double && value % 1 == 0.0) {
                        value.toLong().toString()
                    } else {
                        value.toString()
                    }

                    Text(
                        text = "- $key: $formattedValue",
                        style = Typography.bodyMedium,
                        color = colorScheme.onSurface
                    )
                }
            }
        }
    }
}
