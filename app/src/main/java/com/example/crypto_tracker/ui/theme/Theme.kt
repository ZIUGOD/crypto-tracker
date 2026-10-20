package com.example.crypto_tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccentLight,
    secondary = GoldAccent,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = GoldAccent,
    secondary = GoldAccentDark,
    tertiary = Pink40
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
