package com.example.crypto_tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    background = DarkBgToken,
    surface = DarkSurfaceToken,
    surfaceVariant = DarkElevatedSurfaceToken,
    surfaceContainerLow = DarkSurfaceToken,
    surfaceContainer = DarkSurfaceToken,
    surfaceContainerHigh = DarkElevatedSurfaceToken,
    surfaceContainerHighest = DarkElevatedSurfaceToken,
    outline = DarkBorderToken,
    outlineVariant = DarkBorderToken,
    onBackground = DarkTextPrimaryToken,
    onSurface = DarkTextPrimaryToken,
    onSurfaceVariant = DarkTextSecondaryToken,
    primary = DarkPrimaryTerminalToken,
    onPrimary = Color.Black,
    primaryContainer = DarkPrimaryContainerToken,
    onPrimaryContainer = DarkOnPrimaryContainerToken,
    secondary = DarkGoldToken,
    onSecondary = Color.Black,
    secondaryContainer = DarkSecondaryContainerToken,
    onSecondaryContainer = DarkOnSecondaryContainerToken,
    tertiary = DarkCyanToken,
    onTertiary = Color.Black,
    tertiaryContainer = DarkTertiaryContainerToken,
    onTertiaryContainer = DarkOnTertiaryContainerToken,
    error = DarkErrorToken,
    onError = Color.Black,
    errorContainer = DarkErrorContainerToken,
    onErrorContainer = DarkOnErrorContainerToken
)

private val LightColorScheme = lightColorScheme(
    background = LightBgToken,
    surface = LightSurfaceToken,
    surfaceVariant = LightElevatedSurfaceToken,
    surfaceContainerLow = LightSurfaceToken,
    surfaceContainer = LightSurfaceToken,
    surfaceContainerHigh = LightSurfaceToken,
    surfaceContainerHighest = LightElevatedSurfaceToken,
    outline = LightBorderToken,
    outlineVariant = LightBorderToken,
    onBackground = LightTextPrimaryToken,
    onSurface = LightTextPrimaryToken,
    onSurfaceVariant = LightTextSecondaryToken,
    primary = LightPrimaryTerminalToken,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryContainerToken,
    onPrimaryContainer = LightOnPrimaryContainerToken,
    secondary = LightGoldToken,
    onSecondary = Color.White,
    secondaryContainer = LightSecondaryContainerToken,
    onSecondaryContainer = LightOnSecondaryContainerToken,
    tertiary = LightCyanToken,
    onTertiary = Color.White,
    tertiaryContainer = LightTertiaryContainerToken,
    onTertiaryContainer = LightOnTertiaryContainerToken,
    error = LightErrorToken,
    onError = Color.White,
    errorContainer = LightErrorContainerToken,
    onErrorContainer = LightOnErrorContainerToken
)

/** Shape scale shared by Material components (buttons, dialogs, inputs) and glass surfaces. */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
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
        shapes = AppShapes,
        content = content
    )
}
