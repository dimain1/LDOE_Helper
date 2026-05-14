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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.presentation.LocalImage
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.viewModel.EventAction

@Composable
fun EventViewDetails(onAction: (EventAction) -> Unit, onDismiss: () -> Unit, initialData: Event){

    AlertDialog(
        onDismissRequest = onDismiss,
        title=
            {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier=Modifier.fillMaxWidth()) {
                    Text("Полная информация")

                    Icon(Icons.Default.Close, "Закрыть", modifier= Modifier.clickable(onClick = {onDismiss()}))
                }
               },
        text= {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                LocalImage(null,
                    Modifier.size(250.dp).clip(RoundedCornerShape(20.dp))
                        .border(2.dp, colorScheme.outline,RoundedCornerShape(20.dp))
                )

                Column(modifier=Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(16.dp))

                    Text("Название")

                    Spacer(Modifier.height(16.dp))

                    Text("Описание")

                    Spacer(Modifier.height(16.dp))

                    Text("Время")

                    Spacer(Modifier.height(200.dp))
                }
            }




        },
        confirmButton = {},
        dismissButton = {},
        modifier=Modifier.fillMaxWidth()

    )

}