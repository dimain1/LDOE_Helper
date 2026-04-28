package com.example.kotlinclient.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = textMain_darkTheme,
    inversePrimary = textMain_lightTheme,
    primaryContainer = background_darkTheme,
    surface = containerBackground_darkTheme,
    secondary = textSecondary_darkTheme,
    secondaryContainer = mainContainerBackground_darkTheme,
    tertiary = contrast_darkTheme,
    tertiaryContainer = backgroundInContainer_darkTheme,
    outline = divider_darkTheme,
)

private val LightColorScheme = lightColorScheme(
    primary = textMain_lightTheme,
    inversePrimary = textMain_darkTheme,
    primaryContainer = background_lightTheme,
    surface = containerBackground_lightTheme,
    secondary = textSecondary_lightTheme,
    secondaryContainer = mainContainerBackground_lightTheme,
    tertiary = contrast_lightTheme,
    tertiaryContainer = backgroundInContainer_lightTheme,
    outline = divider_lightTheme,




    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)



@Composable
fun KotlinClientTheme(

    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}