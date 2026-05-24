package com.example.kotlinclient.presentation.settings

import android.content.Context
import android.content.SharedPreferences
import android.text.Layout
import android.util.Log
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.kotlinclient.state_management.entity.User
import com.example.kotlinclient.state_management.viewModel.SettingsAction
import com.example.kotlinclient.state_management.viewModel.SettingsUiState
import com.example.kotlinclient.ui.theme.Typography


@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    paddingValues: PaddingValues
) {

    val scrollState: ScrollState = rememberScrollState()
    val textFieldState = rememberTextFieldState("")
    var showModal by remember { mutableStateOf(false) }



    EditProfileModal(
        showModal,
        uiState.user,
        { showModal = false },
        { login, email -> onAction(SettingsAction.EditUserInfo(login, email)) })

    // Контейнер всего экрана
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    )
    {

        HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)

        // Основной контейнер экрана
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .background(color = colorScheme.secondaryContainer)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            // Контейнер пользователя
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface, shape = RoundedCornerShape(10))
                    .border(1.dp, colorScheme.outline, shape = RoundedCornerShape(10))
                    .padding(24.dp)
            )
            {
                //Контейнер иконки
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(colorScheme.tertiary, shape = CircleShape)
                ) {
                    Icon(
                        Icons.Default.Person,
                        "Person Icon",
                        modifier = Modifier
                            .padding(16.dp)
                            .size(48.dp)

                    )
                }

                Spacer(Modifier.width(16.dp))

                //Столбец инфорации о пользователе
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    if (uiState.user == null) {
                        TextField(
                            value = textFieldState.text.toString(),
                            onValueChange = { text: String ->
                                textFieldState.edit {
                                    replace(
                                        0,
                                        length,
                                        text
                                    )
                                }
                            }
                        )
                        Button(
                            onClick = {
                                onAction(SettingsAction.SetUserId(textFieldState.text.toString().toLong()))
                                Log.e("UserName", "${uiState.user.toString()}")
                            }
                        )
                        {
                            Text("Auth")
                        }

                    } else {
                        Text(
                            text = uiState.user.login,
                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = uiState.user.email,
                            style = Typography.bodyMedium,
                            color = colorScheme.secondary
                        )
                    }
                }
            }

            SettingsBlockTitle("Account")

            SettingsBlock()
            {
                SettingsBlockRow(
                    Icons.Default.Person,
                    "Edit Profile",
                    onRowClick = {
                        if (uiState.user == null) {
                        } else {
                            showModal = !showModal
                        }
                    })
                {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        "Arrow Right",
                        modifier = Modifier.size(16.dp),
                        tint = colorScheme.secondary
                    )
                }

                HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)

                SettingsBlockRow(
                    Icons.Default.ExitToApp,
                    "Sign Out",
                    true,
                    onRowClick = { onAction(SettingsAction.ExitProfile) }
                ) {}
            }

            SettingsBlockTitle("Preferences")

            SettingsBlock()
            {
                SettingsBlockRow(Icons.Default.Clear, "Push Notifications")
                {
                    CustomSwitcher(uiState.notification, { onAction(SettingsAction.SwitchPreference("notification")) })
                }
                HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)
                SettingsBlockRow(Icons.Default.Clear, "Sound Effects")
                {
                    CustomSwitcher(uiState.sound, { onAction(SettingsAction.SwitchPreference("sound")) })
                }
                HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)
                SettingsBlockRow(Icons.Default.Clear, "Language")
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "English",
                            style = Typography.bodyMedium,
                            color = colorScheme.secondary
                        )
                        Spacer(Modifier.width(12.dp))
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            "Arrow Right",
                            modifier = Modifier.size(16.dp),
                            tint = colorScheme.secondary
                        )
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)
                SettingsBlockRow(Icons.Default.Clear, "Dark Theme")
                {
                    CustomSwitcher(uiState.theme, { onAction(SettingsAction.SwitchPreference("theme")) })
                }
            }


            SettingsBlockTitle("About")

            SettingsBlock()
            {
                SettingsBlockRow(Icons.Default.Info, "Version")
                {
                    Text("1.0.0", style = Typography.bodyMedium, color = colorScheme.secondary)
                }
                HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)
                SettingsBlockRow(Icons.Default.Info, "Terms of Service")
                {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        "Arrow Right",
                        modifier = Modifier.size(16.dp),
                        tint = colorScheme.secondary
                    )
                }
                HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)
                SettingsBlockRow(Icons.Default.Info, "Privacy Policy")
                {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        "Arrow Right",
                        modifier = Modifier.size(16.dp),
                        tint = colorScheme.secondary
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)
    }

}

// Заголовок блока настроек
@Composable
fun SettingsBlockTitle(title: String) {
    Spacer(Modifier.height(28.dp))
    // Заголовок Account-пунктов настроек
    Text(
        text = title,
        style = Typography.bodyMedium,
        color = colorScheme.secondary,
        modifier = Modifier.padding(start = 8.dp)
    )

    Spacer(Modifier.height(16.dp))
}

// Строка блока настроек
@Composable
fun SettingsBlockRow(
    icon: ImageVector,
    text: String,
    isImportant: Boolean = false,
    onRowClick: () -> Unit = {},
    content: @Composable () -> Unit
) {

    val importantColor = if (isImportant) colorScheme.tertiary else colorScheme.secondary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onRowClick() })
            .padding(all = 16.dp)

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, text, modifier = Modifier.size(32.dp), tint = importantColor)
            Spacer(Modifier.width(12.dp))
            Text(
                text,
                style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isImportant) importantColor else colorScheme.primary
            )
        }

        content()
    }
}

// Контейнер блока настроек
@Composable
fun SettingsBlock(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surface, RoundedCornerShape(10))
            .border(1.dp, colorScheme.outline, RoundedCornerShape(10))

    )
    {
        content()
    }
}

// Кастомный переключатель
@Composable
fun CustomSwitcher(checked: Boolean, onClick: () -> Unit = {}) {
    val width by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = spring(dampingRatio = 2f)
    )
    Row(
        modifier = Modifier
            .background(
                color = if (checked) colorScheme.tertiary else colorScheme.outline,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = {
                onClick()
            })
            .width(50.dp)
            .padding(all = 5.dp)

    ) {
        Spacer(Modifier.width(width))
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color = colorScheme.primary, shape = CircleShape)

        ) {}
    }
}

// Перенести в ViewModel !!!!!!!!!!!!!!!!!!!!!!!


