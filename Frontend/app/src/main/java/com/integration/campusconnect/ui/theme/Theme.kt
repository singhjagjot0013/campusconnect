package com.integration.campusconnect.ui.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme

private val LightColorScheme = lightColorScheme(
    primary = KPU_Red,
    onPrimary = White,
    secondary = KPU_Orange,
    onSecondary = White,
    background = KPU_Red,
    onBackground = White,
    surface = KPU_Red,
    onSurface = White
)

private val DarkColorScheme = darkColorScheme(
    primary = KPU_Red,
    onPrimary = White,
    secondary = KPU_Orange,
    onSecondary = White,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White
)

@Composable
fun CampusConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
