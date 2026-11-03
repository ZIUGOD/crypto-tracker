package com.example.crypto_tracker.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.preferences.AppTheme

/**
 * What a visual theme does beyond colours: which expensive effects it uses. Composables read these
 * switches from [LocalThemeStyle] instead of hard-coding the glass look, so a lighter theme only needs
 * another instance of this class. Colours live in the per-theme palettes (see `colorSchemeFor`).
 */
data class ThemeStyle(
    /** Real backdrop blur behind fixed surfaces such as the bottom bar (Android 12+). */
    val backdropBlur: Boolean,
    /** Backdrop blur on the cards of scrolling lists. The costliest effect; see [AppConfig.Design]. */
    val listBlur: Boolean,
    /** Silk ribbons, dot grid and grain behind the app. When false only the plain gradient is drawn. */
    val backgroundTexture: Boolean,
    /** Diagonal specular highlight painted on glass surfaces. */
    val sheen: Boolean
) {
    companion object {
        /** The frosted-glass look; its switches come from [AppConfig.Design]. */
        val Glass = ThemeStyle(
            backdropBlur = AppConfig.Design.GLASS_BACKDROP_BLUR_ENABLED,
            listBlur = AppConfig.Design.GLASS_LIST_BLUR_ENABLED,
            backgroundTexture = AppConfig.Design.BACKGROUND_TEXTURE_ENABLED,
            sheen = true
        )
    }
}

/** Style of the active theme. Provided by [CryptotrackerTheme]; defaults to the glass look. */
val LocalThemeStyle = staticCompositionLocalOf { ThemeStyle.Glass }

/** Style for each theme. [AppTheme.LITE] is reserved and never reached while it is unavailable. */
fun AppTheme.style(): ThemeStyle = when (this) {
    AppTheme.LITE -> ThemeStyle.Glass // TODO(lite): flat surfaces, no texture/blur/sheen, for older phones.
    AppTheme.GLASS -> ThemeStyle.Glass
}
