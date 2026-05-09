package com.example.kotlinclient.presentation.utility

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(showDatePicker:Boolean, onDismiss: ()-> Unit, onConfirm: (String) -> Unit){

    val datePickerState = rememberDatePickerState()
    var selectedDateText by remember { mutableStateOf("Дата не выбрана") }

    if(showDatePicker){
        DatePickerDialog(
            onDismissRequest= { onDismiss() },
            confirmButton= {
                TextButton(onClick = {
                    // 3. Обработка выбранной даты
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        onConfirm(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            .format(Date(selectedDateMillis)))
                    }
                    onDismiss()
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text("Отмена")
                }
            }

        ){
            DatePicker(datePickerState)
        }
    }
}