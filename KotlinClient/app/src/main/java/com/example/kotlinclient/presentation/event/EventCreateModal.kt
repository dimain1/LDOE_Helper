package com.example.kotlinclient.presentation.event


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.R
import com.example.kotlinclient.presentation.LocalImage
import com.example.kotlinclient.presentation.template.TemplateItem
import com.example.kotlinclient.presentation.utility.DateTimePicker
import com.example.kotlinclient.state_management.entity.Event
import com.example.kotlinclient.state_management.entity.EventTemplate
import com.example.kotlinclient.ui.theme.Typography


@Composable
fun EventCreateModal(
    showModal: Boolean,
    onDismiss: ()-> Unit,
    templates: List<EventTemplate>,
    onCreateClick: (Event) -> Unit){
    if(showModal){

        val nameFieldState = rememberTextFieldState()
        val descriptionFielddState = rememberTextFieldState()
        val startTimeFieldState = rememberTextFieldState()
        val endTimeFieldState = rememberTextFieldState()

        var showTemplateModal by remember { mutableStateOf(false) }
        val onDismissTemplate = {showTemplateModal = !showTemplateModal}

        var selectedTemplateId by remember { mutableStateOf<Long?>(null) }
        var selected_template: EventTemplate? = if(selectedTemplateId != null) templates.find { template -> template.id == selectedTemplateId } else null


        EventTemplatePicker(showTemplateModal= showTemplateModal,templates=templates, onDismiss = onDismissTemplate, onClick = {id ->selectedTemplateId = id} )

        var showDateTimePicker by remember { mutableStateOf(false) }

        DateTimePicker(
            showDateTimePicker,
            onDismiss= {showDateTimePicker = false},
            onConfirm = { text -> startTimeFieldState.edit {replace(0,length,text)} }
        )

        AlertDialog(
            onDismissRequest={onDismiss()},
            title= {
                Text(text="Создать событие")
                   },
            text= {
                Column() {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(10)
                            )
                            .padding(12.dp)

                    ) {
                        if (selectedTemplateId == null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {}
                            Image(
                                painter = painterResource(R.drawable.pencil),
                                contentDescription = "Delete Image",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable(onClick = { onDismissTemplate() }),
                                colorFilter = ColorFilter.tint(colorScheme.primary)
                            )
                        }
                        else
                        {
                            // Изображение события !!!!!!!!!!!!!!!!
                            LocalImage(
                                selected_template?.image,
                                Modifier.size(48.dp)
                            )

                            Spacer(Modifier.width(12.dp))
                            // Содержание события
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                            {
                                // Название события
                                Text(
                                    text = selected_template?.name ?: "",
                                    style = TextStyle(
                                        fontSize = Typography.bodyMedium.fontSize,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colorScheme.primary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                // Длительность
                                Text(
                                    text = (selected_template?.duration!! / 1000).toString() + " Minutes",
                                    style = TextStyle(
                                        fontSize = Typography.bodySmall.fontSize,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = colorScheme.secondary,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )

                            }
                            Image(
                                painter = painterResource(R.drawable.pencil),
                                contentDescription = "Delete Image",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable(onClick = { onDismissTemplate() }),
                                colorFilter = ColorFilter.tint(colorScheme.primary)
                            )
                        }
                    }
                    TextField(
                        value = nameFieldState.text.toString(),
                        onValueChange = {text -> nameFieldState.edit {replace(0,length,text)}},
                        label= {Text(text= "Название")}
                    )
                    TextField(
                        value = descriptionFielddState.text.toString(),
                        onValueChange = {text -> descriptionFielddState.edit {replace(0,length,text)}},
                        label= {Text(text= "Описание")}
                    )
                    TextField(
                        value = startTimeFieldState.text.toString(),
                        onValueChange = {text -> startTimeFieldState.edit {replace(0,length,text)}},
                        label= {Text(text= "Время начала")},
                        readOnly = true,
                        enabled = false,
                        modifier=Modifier.fillMaxWidth().clickable(onClick = {showDateTimePicker = true})
                    )
                    TextField(
                        value = endTimeFieldState.text.toString(),
                        onValueChange = {text -> endTimeFieldState.edit {replace(0,length,text)}},
                        label= {Text(text= "Время окончания")},
                        readOnly = if(selectedTemplateId != null) true else false
                    )
                }
                  },
            confirmButton=
                {
                    Button(
                        onClick={

                        }
                    )
                    {
                        Text("Создать")
                    }
                },
            dismissButton=
                {
                    Button(
                        onClick = { onDismiss() }
                    )
                    {
                        Text("Отменить")
                    }
                },
            containerColor = colorScheme.secondaryContainer
        )
    }
}