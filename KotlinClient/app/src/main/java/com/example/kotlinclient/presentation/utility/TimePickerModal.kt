package com.example.kotlinclient.presentation.utility

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerModal(showTimePicker: Boolean, initialTime: String? ,onDismiss: () ->Unit, onConfirm: (String)->Unit ){

    val time: LocalTime = if(initialTime != null) LocalTime.parse(initialTime, DateTimeFormatter.ofPattern("HH:mm")) else LocalTime.MIDNIGHT

    val timePickerState = rememberTimePickerState(time.hour, time.minute, true)

    if(showTimePicker) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm(
                            String.format(
                                "%02d:%02d",
                                timePickerState.hour,
                                timePickerState.minute
                            )
                        )
                        onDismiss()
                    }
                ){ Text("Сохранить")}
            },
            dismissButton = {
                Button(onDismiss){
                    Text("Отменить")
                }
            },
            title = {Text("Выберите время")},
            text = {
                TimeInput(timePickerState)
            }
        )
    }
}