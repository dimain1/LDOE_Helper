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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.kotlinclient.R
import com.example.kotlinclient.presentation.utility.uiComponent.BasicTextFieldInModal
import com.example.kotlinclient.presentation.utility.uiComponent.LocalImage
import com.example.kotlinclient.presentation.utility.uiComponent.ValidatedBasicTextFieldInModal
import com.example.kotlinclient.state_management.viewModel.EventTemplateFormAction
import com.example.kotlinclient.state_management.viewModel.EventTemplateFormUiState
import com.example.kotlinclient.state_management.viewModel.EventTemplateFormValidation
import com.example.kotlinclient.ui.theme.Typography

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

            onFormAction(EventTemplateFormAction.SaveImageInLocal(it))
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        }
    }

    val focusManager = LocalFocusManager.current
    val modalFocusRequester = remember { FocusRequester() }

    val clearFocusModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            modalFocusRequester.requestFocus()
            focusManager.clearFocus()
        })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(modalFocusRequester)
                    .focusable()
                    .then(clearFocusModifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (formUiState.id == null) "Создание шаблона" else "Редактирование шаблона",
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(clearFocusModifier),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(2.dp, colorScheme.outline, RoundedCornerShape(10.dp))
                        .clickable {
                            modalFocusRequester.requestFocus()
                            focusManager.clearFocus()

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
                            contentDescription = "Выбрать изображение",
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
                    ValidatedBasicTextFieldInModal(
                        formUiState.name,
                        onFocusLost = {
                            onFormAction(
                                EventTemplateFormAction.OnFormValidation(
                                    EventTemplateFormValidation.ValidateName
                                )
                            )
                        },
                        onFocused = {
                            onFormAction(
                                EventTemplateFormAction.OnFormValidation(
                                    EventTemplateFormValidation.ClearNameError
                                )
                            )
                        },
                        placeholder = "Название",
                        errorText = formUiState.errors.nameError,
                        maxlines = 2
                    )
                    Spacer(Modifier.height(16.dp))

                    BasicTextFieldInModal(formUiState.description, "Описание")

                    Spacer(Modifier.height(16.dp))

                    ValidatedBasicTextFieldInModal(
                        formUiState.duration,
                        onFocusLost = {
                            onFormAction(
                                EventTemplateFormAction.OnFormValidation(
                                    EventTemplateFormValidation.ValidateDuration
                                )
                            )
                        },
                        onFocused = {
                            onFormAction(
                                EventTemplateFormAction.OnFormValidation(
                                    EventTemplateFormValidation.ClearDurationError
                                )
                            )
                        },
                        placeholder = "Длительность в минутах",
                        keyboardOption = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        errorText = formUiState.errors.durationError,
                        maxlines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onFormAction(EventTemplateFormAction.CreateEventTemplate) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.tertiary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (formUiState.id == null) "Создать" else "Изменить",
                    style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.tertiary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Отменить",
                    style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        },
        containerColor = colorScheme.secondaryContainer
    )

}