/*package com.integration.campusconnect.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}*/

package com.integration.campusconnect.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

val KPU_Red = Color(0xFFB5121B)
val KPU_Orange = Color(0xFFFFA726)
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

private val LightColorScheme = lightColorScheme(
    primary = KPU_Red,
    onPrimary = White,
    secondary = KPU_Orange,
    onSecondary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black
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
    dynamicColor: Boolean = false,  // keep false since we're forcing KPU branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
