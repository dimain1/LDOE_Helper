package com.example.kotlinclient.presentation.event.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.presentation.utility.uiComponent.LocalImage
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.viewModel.EventAction
import com.example.kotlinclient.ui.theme.Typography
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EventViewDetails(onAction: (EventAction) -> Unit, onDismiss: () -> Unit, initialData: Event?) {

    val titleVS = rememberScrollState()
    val descVS = rememberScrollState()
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM HH:mm:ss", Locale("ru"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title =
            {
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
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                LocalImage(
                    initialData?.image,
                    Modifier
                        .height(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(2.dp, colorScheme.outline, RoundedCornerShape(20.dp))
                        .fillMaxWidth()
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

                    Row {

                        Text(
                            "Начнётся:",
                            style = Typography.bodyMedium,
                            color = colorScheme.secondary
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = initialData?.startTime?.format(dateFormatter) ?: "",
                            style = Typography.bodyMedium,
                            color = colorScheme.secondary
                        )
                    }

                    Spacer(Modifier.height(12.dp))


                    Row {

                        Text(
                            "Закончится:",
                            style = Typography.bodyMedium,
                            color = colorScheme.secondary
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = initialData?.endTime?.format(dateFormatter) ?: "",
                            style = Typography.bodyMedium,
                            color = colorScheme.secondary
                        )
                    }

                }
            }


        },
        confirmButton = {},
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
        modifier = Modifier.fillMaxWidth(),
        containerColor = colorScheme.secondaryContainer

    )

}