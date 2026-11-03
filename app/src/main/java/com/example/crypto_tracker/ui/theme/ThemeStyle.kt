package com.example.crypto_tracker.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.preferences.AppTheme

/**
 * What a visual theme does beyond colours: which effects it uses. Composables read these switches from
 * [LocalThemeStyle] instead of hard-coding one look, so a new theme only needs another instance of this
 * class. Colours live in the per-theme palettes (see `colorSchemeFor`).
 */
data class ThemeStyle(
    /** Real backdrop blur behind fixed surfaces such as the bottom bar (Android 12+). */
    val backdropBlur: Boolean,
    /** Backdrop blur on the cards of scrolling lists. The costliest effect; see [AppConfig.Design]. */
    val listBlur: Boolean,
    /** Silk ribbons, dot grid and grain behind the app. */
    val backgroundTexture: Boolean,
    /** Vertical gradient behind the app; false paints one flat colour. */
    val backgroundGradient: Boolean,
    /** Diagonal specular highlight painted on glass surfaces. */
    val sheen: Boolean,
    /**
     * Plain surfaces: one opaque colour with a one-colour border, so nothing is blended and no
     * brushes, sheen or luminous edge are built. The cheapest way to draw a card.
     */
    val opaqueSurfaces: Boolean,
    /** Card expand/collapse animation, fades, rotating arrow and price cross-fade. False changes state instantly. */
    val richMotion: Boolean
) {
    companion object {
        /** The frosted-glass look; its effect switches come from [AppConfig.Design]. */
        val Glass = ThemeStyle(
            backdropBlur = AppConfig.Design.GLASS_BACKDROP_BLUR_ENABLED,
            listBlur = AppConfig.Design.GLASS_LIST_BLUR_ENABLED,
            backgroundTexture = AppConfig.Design.BACKGROUND_TEXTURE_ENABLED,
            backgroundGradient = true,
            sheen = true,
            opaqueSurfaces = false,
            richMotion = true
        )

        /** Flat and as cheap as possible: every effect above is off. */
        val Lite = ThemeStyle(
            backdropBlur = false,
            listBlur = false,
            backgroundTexture = false,
            backgroundGradient = false,
            sheen = false,
            opaqueSurfaces = true,
            richMotion = false
        )
    }
}

/** Style of the active theme. Provided by [CryptotrackerTheme]; defaults to the cheapest look. */
val LocalThemeStyle = staticCompositionLocalOf { ThemeStyle.Lite }

/** Style for each theme. */
fun AppTheme.style(): ThemeStyle = when (this) {
    AppTheme.LITE -> ThemeStyle.Lite
    AppTheme.GLASS -> ThemeStyle.Glass
}
