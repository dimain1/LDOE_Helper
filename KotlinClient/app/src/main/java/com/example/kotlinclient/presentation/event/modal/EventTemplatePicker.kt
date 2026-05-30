package com.example.kotlinclient.presentation.event.modal

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.presentation.template.TemplateItem
import com.example.kotlinclient.state_management.entity.EventTemplate

@Composable
fun EventTemplatePicker(
    showTemplateModal: Boolean,
    templates: List<EventTemplate>,
    selectedTemplate: EventTemplate?,
    onDismiss: ()-> Unit,
    onClick: (EventTemplate?)->Unit)
{

    val horizontalScroll: ScrollState = rememberScrollState()

    if(showTemplateModal){
        AlertDialog(
            modifier=Modifier.fillMaxWidth(),
            onDismissRequest = { onDismiss() },
            title=
                {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier= Modifier.fillMaxWidth()
                    ){
                        Text("Выбери шаблон")
                        Icon(Icons.Default.Close, "Close", modifier=Modifier
                            .size(32.dp)
                            .clickable(onClick = onDismiss))
                    }
                },
            text= {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max)
                        .horizontalScroll(horizontalScroll)) {

                    for(item in 0 .. templates.size - 1){

                        val template = templates[item]

                        Box(Modifier
                            .clickable(onClick = {
                                onClick(template)
                                Log.d("DEBUG", "selected Id ${template.id!!}")
                            }
                            )
                            .border(
                                2.dp,
                                if (template.id == selectedTemplate?.id) colorScheme.tertiary else colorScheme.outline,
                                RoundedCornerShape(30.dp)
                            )
                        ) {
                            TemplateItem(
                                template,
                                {},
                                160,
                                Modifier
                                    .width(200.dp)
                                    .fillMaxHeight(),
                                onClick = onClick
                                )
                        }
                        if(item != templates.size -1){
                            Spacer(Modifier.width(16.dp))
                        }

                    }
                }
            },
            confirmButton = {},
            containerColor = colorScheme.secondaryContainer
        )
    }
}