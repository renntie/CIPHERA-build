package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CipheraColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = Color.Black,
    primaryContainer = Emerald950,
    onPrimaryContainer = Emerald300,
    secondary = Cyan500,
    onSecondary = Color.Black,
    secondaryContainer = Cyan950,
    onSecondaryContainer = Cyan300,
    tertiary = Purple500,
    onTertiary = Color.White,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
    outline = Slate700,
    error = Rose500,
    onError = Color.White
)

@Composable
fun CIPHERATheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CipheraColorScheme,
        typography = Typography,
        content = content
    )
}
