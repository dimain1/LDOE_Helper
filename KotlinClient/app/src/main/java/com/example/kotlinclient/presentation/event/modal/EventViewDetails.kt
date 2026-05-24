package com.example.kotlinclient.presentation.event.modal

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.presentation.LocalImage
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.viewModel.EventAction
import com.example.kotlinclient.ui.theme.Typography
import java.time.format.DateTimeFormatter
import kotlin.math.max

@Composable
fun EventViewDetails(onAction: (EventAction) -> Unit, onDismiss: () -> Unit, initialData: Event?) {

    AlertDialog(
        onDismissRequest = onDismiss,
        title =
            {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.width(10.dp))

                    Text("Событие")

                    Icon(
                        Icons.Default.Close,
                        "Закрыть",
                        modifier = Modifier.clickable(onClick = { onDismiss() })
                    )
                }
            },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                LocalImage(
                    null,
                    Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(2.dp, colorScheme.outline, RoundedCornerShape(20.dp))
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = initialData?.name ?: "Название отсутствует",
                        style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.primary,
                        maxLines = 2
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = initialData?.description ?: "Описание отсутствует",
                        color = colorScheme.primary,
                        style = Typography.bodyMedium,
                        maxLines = 4
                    )

                    Spacer(Modifier.height(12.dp))

                    Row {

                        Text(
                            "Началось:",
                            style = Typography.bodyMedium,
                            color = colorScheme.secondary
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = "${initialData?.start_time?.dayOfMonth} ${initialData?.start_time?.month} ${
                                initialData?.start_time?.format(
                                    DateTimeFormatter.ofPattern("HH:mm:ss")
                                )
                            }", style = Typography.bodyMedium, color = colorScheme.secondary
                        )
                    }

                    Spacer(Modifier.height(12.dp))


                    Row {

                        Text(
                            "Закончилось:",
                            style = Typography.bodyMedium,
                            color = colorScheme.secondary
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = "${initialData?.end_time?.dayOfMonth} ${initialData?.end_time?.month} ${
                                initialData?.end_time?.format(
                                    DateTimeFormatter.ofPattern("HH:mm:ss")
                                )
                            }", style = Typography.bodyMedium, color = colorScheme.secondary
                        )
                    }

                }
            }


        },
        confirmButton = {},
        dismissButton = {},
        modifier = Modifier.fillMaxWidth()

    )

}