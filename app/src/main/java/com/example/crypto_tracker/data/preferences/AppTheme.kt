package com.example.crypto_tracker.data.preferences

/**
 * The app's visual theme (look and feel), independent of [ThemeMode] (light/dark/system).
 *
 * The order is the order shown to the user, so new themes are added in place:
 * 1. [LITE] - the default. Flat and as cheap as possible: no blur, texture, gradients, translucency
 *    or animation. Runs smoothly on old phones.
 * 2. [GLASS] - the richer frosted-glass look, for phones that can afford it.
 *
 * To add a theme: implement its `ThemeStyle` and palettes in `ui/theme`, map it in `AppTheme.style()` and
 * `colorSchemeFor`, add its texts to Settings, then set [available] to true.
 */
enum class AppTheme(val available: Boolean) {
    LITE(available = true),
    GLASS(available = true);

    companion object {
        /** Theme for a new install and the fallback for any stored value that is unknown or unavailable. */
        val Default: AppTheme = LITE

        /** Themes the user can pick, in display order. */
        val selectable: List<AppTheme> get() = entries.filter { it.available }

        /** Maps a stored name to a usable theme: unknown, removed or unavailable themes become [Default]. */
        fun fromStored(name: String?): AppTheme =
            entries.firstOrNull { it.name == name && it.available } ?: Default
    }
}
