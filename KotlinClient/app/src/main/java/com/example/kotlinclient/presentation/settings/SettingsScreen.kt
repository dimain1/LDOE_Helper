package com.example.kotlinclient.presentation.settings

import android.content.Context
import android.content.SharedPreferences
import android.text.Layout
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.kotlinclient.ui.theme.Typography


@Composable
fun SettingsScreen(paddingValues: PaddingValues){

    val scrollState: ScrollState = rememberScrollState()
    val context = LocalContext.current

    val preferences: SharedPreferences = context.getSharedPreferences("my_app_preferences", Context.MODE_PRIVATE)

    var notifications by remember {  mutableStateOf(preferences.getBoolean("notification",false))}
    var sounds by remember {  mutableStateOf(preferences.getBoolean("sound",false))}
    var theme by remember {  mutableStateOf(preferences.getBoolean("theme",false))}



    // Контейнер всего экрана
    Column(modifier= Modifier
        .fillMaxSize()
        .padding(paddingValues)
    )
    {

        HorizontalDivider(thickness = 1.dp,color = colorScheme.outline)


        // Основной контейнер экрана
        Column(
            modifier=Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .background(color = colorScheme.secondaryContainer)
                .padding(horizontal = 16.dp)
        )
        {
            Spacer(Modifier.height(16.dp))
            // Контейнер пользователя
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier=Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface, shape = RoundedCornerShape(10))
                    .border(1.dp, colorScheme.outline, shape = RoundedCornerShape(10))
                    .padding(24.dp)
            )
            {
                //Контейнер иконки
                Box(
                    contentAlignment = Alignment.Center,
                    modifier= Modifier
                        .background(colorScheme.tertiary, shape= CircleShape)
                ){
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
                    modifier= Modifier
                        .fillMaxWidth()
                ){
                    Text(text="Username", style= Typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color=colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Text(text="UserEmail@mail.ru", style=Typography.bodyMedium,color= colorScheme.secondary)
                }
            }

            SettingsBlockTitle("Account")

            SettingsBlock()
            {
                SettingsBlockRow(Icons.Default.Person, "Edit Profile")
                {
                    Icon(Icons.Default.KeyboardArrowRight, "Arrow Right", modifier=Modifier.size(16.dp) ,tint=colorScheme.secondary)
                }

                HorizontalDivider(thickness = 1.dp, color=colorScheme.outline)

                SettingsBlockRow(Icons.Default.ExitToApp, "Sign Out", true) {}
            }

            SettingsBlockTitle("Preferences")

            SettingsBlock()
            {
                SettingsBlockRow(Icons.Default.Clear, "Push Notifications")
                {
                    CustomSwitcher(notifications, {changeNotifications(context)})
                }
                HorizontalDivider(thickness =  1.dp, color= colorScheme.outline)
                SettingsBlockRow(Icons.Default.Clear, "Sound Effects")
                {
                    CustomSwitcher(sounds, {changeSound(context)})
                }
                HorizontalDivider(thickness =  1.dp, color= colorScheme.outline)
                SettingsBlockRow(Icons.Default.Clear, "Language")
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text("English", style=Typography.bodyMedium,color= colorScheme.secondary)
                        Spacer(Modifier.width(12.dp))
                        Icon(Icons.Default.KeyboardArrowRight, "Arrow Right", modifier=Modifier.size(16.dp) ,tint=colorScheme.secondary)
                    }
                }
                HorizontalDivider(thickness =  1.dp, color= colorScheme.outline)
                SettingsBlockRow(Icons.Default.Clear, "Dark Theme")
                {
                    CustomSwitcher(theme, {changeTheme(context) })
                }
            }


            SettingsBlockTitle("About")

            SettingsBlock()
            {
                SettingsBlockRow(Icons.Default.Info, "Version")
                {
                    Text("1.0.0", style=Typography.bodyMedium,color= colorScheme.secondary)
                }
                HorizontalDivider(thickness =  1.dp, color= colorScheme.outline)
                SettingsBlockRow(Icons.Default.Info, "Terms of Service")
                {
                    Icon(Icons.Default.KeyboardArrowRight, "Arrow Right", modifier=Modifier.size(16.dp) ,tint=colorScheme.secondary)
                }
                HorizontalDivider(thickness =  1.dp, color= colorScheme.outline)
                SettingsBlockRow(Icons.Default.Info, "Privacy Policy")
                {
                    Icon(Icons.Default.KeyboardArrowRight, "Arrow Right", modifier=Modifier.size(16.dp) ,tint=colorScheme.secondary)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        HorizontalDivider(thickness = 1.dp,color = colorScheme.outline)
    }

}

// Заголовок блока настроек
@Composable
fun SettingsBlockTitle(title:String){
    Spacer(Modifier.height(28.dp))
    // Заголовок Account-пунктов настроек
    Text(text= title, style=Typography.bodyMedium,color= colorScheme.secondary, modifier=Modifier.padding(start=8.dp) )

    Spacer(Modifier.height(16.dp))
}

// Строка блока настроек
@Composable
fun SettingsBlockRow(icon: ImageVector, text: String, isImportant: Boolean = false,  content: @Composable ()-> Unit){

    val importantColor = if(isImportant) colorScheme.tertiary else colorScheme.secondary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier=Modifier
            .fillMaxWidth()

            .padding(all = 16.dp)

    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ){
            Icon(icon, text,modifier=Modifier.size(32.dp) ,tint=importantColor)
            Spacer(Modifier.width(12.dp))
            Text(text, style= Typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color= if(isImportant) importantColor else colorScheme.primary)
        }

        content()
    }
}

// Контейнер блока настроек
@Composable
fun SettingsBlock(content: @Composable () -> Unit){
    Column(
        modifier= Modifier
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
fun CustomSwitcher(checked: Boolean ,onClick: () -> Unit = {}){
    var checkedState by remember { mutableStateOf<Boolean>(checked)}
    val width by animateDpAsState(
        targetValue = if (checkedState) 20.dp else 0.dp,
        animationSpec = spring(dampingRatio = 2f)
    )
    Row(
        modifier= Modifier
            .background(
                color = if (checkedState) colorScheme.tertiary else colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = {
                checkedState = !checkedState
                onClick()
            })
            .width(50.dp)
            .padding(all = 5.dp)

    ){
        Spacer(Modifier.width(width))
        Box(
            modifier=Modifier
                .size(20.dp)
                .background(color = colorScheme.primary, shape = CircleShape)

        ){}
    }
}

// Перенести в ViewModel !!!!!!!!!!!!!!!!!!!!!!!
fun changeNotifications(context: Context): Unit{
    val preferences: SharedPreferences = context.getSharedPreferences("my_app_preferences", Context.MODE_PRIVATE)

    preferences.edit { putBoolean("notification", !preferences.getBoolean("notification", false)) }
}

fun changeSound(context: Context): Unit{
    val preferences: SharedPreferences = context.getSharedPreferences("my_app_preferences", Context.MODE_PRIVATE)

    preferences.edit { putBoolean("sound", !preferences.getBoolean("sound", false)) }
}

fun changeTheme(context: Context): Unit{
    val preferences: SharedPreferences = context.getSharedPreferences("my_app_preferences", Context.MODE_PRIVATE)

    preferences.edit { putBoolean("theme", !preferences.getBoolean("theme", false)) }
}


