package com.example.kotlinclient.presentation.utility.modal


import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.kotlinclient.R
import com.example.kotlinclient.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    showDateTimePicker: Boolean,
    initialDateTime: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {



    if (showDateTimePicker) {

        var initialDate: String? = null
        var initialTime: String? = null

        if(initialDateTime?.isNotEmpty()?: false){
            initialDate = if(initialDateTime.split(" - ")[0] == "null") null else initialDateTime.split(" - ")[0]
            initialTime = if(initialDateTime.split(" - ")[1] == "null") null else initialDateTime.split(" - ")[1]
        }

        var dateText by remember { mutableStateOf<String?>(initialDate) }
        var timeText by remember { mutableStateOf<String?>(initialTime) }

        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }

        DatePickerModal(showDatePicker, dateText ,{ showDatePicker = false }, { date -> dateText = date })

        TimePickerModal(showTimePicker, timeText ,{ showTimePicker = false }, { time -> timeText = time })


        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = colorScheme.secondaryContainer,
            title = { Text("Выберите дату и время") },
            text = {
                Column() {

                    Text("Дата", style=Typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color=colorScheme.primary)

                    Spacer(Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(colorScheme.surface, RoundedCornerShape(10))
                            .border(
                                2.dp, colorScheme.outline,
                                RoundedCornerShape(10)
                            )
                            .clickable(onClick = { showDatePicker = true })
                            .padding(horizontal = 16.dp)
                    ){
                        Text(
                            text = if (dateText == null) "" else dateText!!,
                            style = Typography.bodyLarge.copy(color=colorScheme.primary)
                        )
                        Image(painterResource(R.drawable.pencil), "Дата", modifier = Modifier.size(24.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("Время", style=Typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color=colorScheme.primary)

                    Spacer(Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(colorScheme.surface, RoundedCornerShape(10))
                            .border(
                                2.dp, colorScheme.outline,
                                RoundedCornerShape(10)
                            )
                            .clickable(onClick = { showTimePicker = true })
                            .padding(horizontal = 16.dp)
                    ){
                        Text(
                            text = if (timeText == null) "" else timeText!!,
                            style = Typography.bodyLarge.copy(color=colorScheme.primary)
                        )
                        Image(painterResource(R.drawable.pencil), "Время", modifier = Modifier.size(24.dp))
                    }
                }
            },
            confirmButton =
                {
                    Button(onClick = {
                        onConfirm("${dateText} - ${timeText}")
                        onDismiss()
                    },
                        enabled = if(timeText == null || dateText == null) false else true
                    )
                    { Text("Сохранить") }
                },
            dismissButton = {
                Button(onClick = onDismiss)
                { Text("Отменить") }
            }
        )

    }


}