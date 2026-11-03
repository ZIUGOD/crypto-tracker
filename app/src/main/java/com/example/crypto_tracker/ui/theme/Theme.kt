package com.example.crypto_tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.crypto_tracker.data.preferences.AppTheme

private val GlassDarkColorScheme = darkColorScheme(
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

private val GlassLightColorScheme = lightColorScheme(
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

/** Colour scheme of [appTheme] in the requested brightness. [AppTheme.LITE] will get its own palettes. */
private fun colorSchemeFor(appTheme: AppTheme, darkTheme: Boolean): ColorScheme = when (appTheme) {
    AppTheme.LITE, AppTheme.GLASS -> if (darkTheme) GlassDarkColorScheme else GlassLightColorScheme
}

/**
 * @param darkTheme Light or dark brightness (the user's [com.example.crypto_tracker.data.preferences.ThemeMode]).
 * @param appTheme Visual theme: picks the palette and the [ThemeStyle] effects.
 */
@Composable
fun CryptotrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: AppTheme = AppTheme.Default,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorSchemeFor(appTheme, darkTheme),
        typography = Typography,
        shapes = AppShapes
    ) {
        CompositionLocalProvider(LocalThemeStyle provides appTheme.style(), content = content)
    }
}
