package com.example.kotlinclient

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.kotlinclient.presentation.home.HomeScreen
import com.example.kotlinclient.presentation.navigation.ApplicationNavHost
import com.example.kotlinclient.presentation.navigation.Routes
import com.example.kotlinclient.ui.theme.KotlinClientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Логика реализации смены темы
            val preferences = getSharedPreferences("my_app_preferences", MODE_PRIVATE)
            val key: String = "theme"

            var darkTheme by remember { mutableStateOf(preferences.getBoolean("theme",false)) }


            DisposableEffect(key) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, changedKey ->
                    if (key == changedKey) {
                        darkTheme = preferences.getBoolean(key, false) ?: false
                    }
                }

                preferences.registerOnSharedPreferenceChangeListener(listener)

                // Удаляем слушатель, когда компонент исчезает с экрана
                onDispose {
                    preferences.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }
            // Конец логики реализации смены темы

            KotlinClientTheme(darkTheme) {

                val navController = rememberNavController()
                val startDestination = Routes.HomePage.route

                ApplicationNavHost(navController, startDestination)
            }
        }
    }
}

