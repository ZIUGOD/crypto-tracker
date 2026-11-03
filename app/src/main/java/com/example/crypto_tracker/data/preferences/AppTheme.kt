package com.example.crypto_tracker.data.preferences

/**
 * The app's visual theme (look and feel), independent of [ThemeMode] (light/dark/system).
 *
 * The order is the order shown to the user, so new themes are added in place:
 * 1. [LITE] - a lighter version for older phones (flat surfaces, no texture, no blur, simpler motion).
 *    **Reserved, not built yet**: [available] is false, so it is never offered or applied.
 * 2. [GLASS] - the current frosted-glass look.
 *
 * To add a theme: implement its `ThemeStyle` and palettes in `ui/theme`, map it in `AppTheme.style()` and
 * `colorSchemeFor`, then set [available] to true. Nothing else in the screens changes.
 */
enum class AppTheme(val available: Boolean) {
    LITE(available = false),
    GLASS(available = true);

    companion object {
        /** Theme for a new install and the fallback for any stored value that is unknown or unavailable. */
        val Default: AppTheme = GLASS

        /** Themes the user can pick, in display order. */
        val selectable: List<AppTheme> get() = entries.filter { it.available }

        /** Maps a stored name to a usable theme: unknown, removed or not-yet-built themes become [Default]. */
        fun fromStored(name: String?): AppTheme =
            entries.firstOrNull { it.name == name && it.available } ?: Default
    }
}
