package com.example.kotlinclient.presentation.utility


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(showDateTimePicker: Boolean, onDismiss: ()-> Unit, onConfirm: (String) -> Unit){

    if(showDateTimePicker) {

        var dateText by remember { mutableStateOf<String?>(null) }
        var timeText by remember { mutableStateOf<String?>(null) }

        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }

        DatePickerModal(showDatePicker, {showDatePicker = false}, {date -> dateText = date})

        TimePickerModal(showTimePicker, {showTimePicker = false}, {time -> timeText = time})

        AlertDialog(
            onDismissRequest = onDismiss,
            title= { Text("Выберите дату и время") },
            text= {
                Row(){
                    Text(text=if(dateText == null) "Дата" else dateText!!, modifier=Modifier.clickable(onClick={ showDatePicker = true }))
                    Spacer(Modifier.width(32.dp))
                    Text(text=if(timeText == null) "Время" else timeText!!, modifier=Modifier.clickable(onClick={ showTimePicker = true }))
                }
            },
            confirmButton =
                {
                    Button(onClick= {
                        onConfirm("${dateText} - ${timeText}")
                        onDismiss()
                    })
                    { Text("Сохранить") }
                },
            dismissButton = {
                Button(onClick = onDismiss)
                { Text("Отменить")}
            }
        )

    }






}