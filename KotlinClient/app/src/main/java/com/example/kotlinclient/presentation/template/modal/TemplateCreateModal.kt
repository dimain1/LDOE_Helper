package com.example.kotlinclient.presentation.template.modal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.kotlinclient.R
import com.example.kotlinclient.presentation.utility.uiComponent.BasicTextFieldInModal
import com.example.kotlinclient.presentation.utility.uiComponent.LocalImage
import com.example.kotlinclient.state_management.viewModel.EventTemplateFormAction
import com.example.kotlinclient.state_management.viewModel.EventTemplateFormUiState

@Composable
fun TemplateCreateModal(
    formUiState: EventTemplateFormUiState,
    onFormAction: (EventTemplateFormAction) -> Unit,
    onDismiss: () -> Unit
) {

    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            onFormAction(EventTemplateFormAction.SaveImageInLocal(context, it))
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        }
    }



    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (formUiState.id == null) "Создать шаблон" else "Редактировать шаблон") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(2.dp, colorScheme.outline, RoundedCornerShape(10.dp))
                        .clickable {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                imagePickerLauncher.launch("image/*")
                            } else {
                                when {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    ) == PackageManager.PERMISSION_GRANTED -> {
                                        imagePickerLauncher.launch("image/*")
                                    }

                                    else -> {
                                        permissionLauncher.launch(
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                        )
                                    }
                                }
                            }
                        }
                ) {
                    if (formUiState.image != null) {
                        LocalImage(
                            formUiState.image,
                            Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(10.dp)),
                            true
                        )

                    } else {
                        Image(
                            painterResource(R.drawable.pencil),
                            contentDescription = "Choose image",
                            Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                Column(
                    Modifier.fillMaxWidth()
                ) {
                    BasicTextFieldInModal(formUiState.name, "Название")

                    Spacer(Modifier.height(16.dp))

                    BasicTextFieldInModal(formUiState.description, "Описание")

                    Spacer(Modifier.height(16.dp))

                    BasicTextFieldInModal(formUiState.duration, "Длительность")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onFormAction(EventTemplateFormAction.CreateEventTemplate(context)) }) {
                Text(
                    text = if (formUiState.id == null) "Создать" else "Изменить"
                )
            }
        },
        dismissButton = { Button(onClick = onDismiss) { Text(text = "Отменить") } },
        containerColor = colorScheme.secondaryContainer
    )

}