package com.example.crypto_tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    background = DarkBgToken,
    surface = DarkSurfaceToken,
    surfaceVariant = DarkElevatedSurfaceToken,
    outline = DarkBorderToken,
    onBackground = DarkTextPrimaryToken,
    onSurface = DarkTextPrimaryToken,
    onSurfaceVariant = DarkTextSecondaryToken,
    primary = DarkPrimaryTerminalToken,
    onPrimary = Color.Black,
    secondary = DarkGoldToken,
    tertiary = DarkCyanToken,
    error = DarkErrorToken
)

private val LightColorScheme = lightColorScheme(
    background = LightBgToken,
    surface = LightSurfaceToken,
    surfaceVariant = LightElevatedSurfaceToken,
    outline = LightBorderToken,
    onBackground = LightTextPrimaryToken,
    onSurface = LightTextPrimaryToken,
    onSurfaceVariant = LightTextSecondaryToken,
    primary = LightPrimaryTerminalToken,
    onPrimary = Color.White,
    secondary = LightGoldToken,
    tertiary = LightCyanToken,
    error = LightErrorToken
)

@Composable
fun CryptotrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
