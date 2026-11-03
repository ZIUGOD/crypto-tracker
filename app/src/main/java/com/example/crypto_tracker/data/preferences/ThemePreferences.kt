package com.example.crypto_tracker.data.preferences

import android.content.Context
import com.example.crypto_tracker.config.AppConfig

/**
 * Enum representing the theme mode options available in the app.
 */
enum class ThemeMode {
    SYSTEM, // Automatic (follows system theme)
    LIGHT,  // Light theme
    DARK    // Dark theme
}

/**
 * Manages user theme preferences using SharedPreferences.
 */
/** Contract for persisting the user's theme choice. */
interface ThemeStore {
    fun getThemeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)

    /** The visual theme (look and feel); always an available one. */
    fun getAppTheme(): AppTheme
    fun setAppTheme(theme: AppTheme)
}

class ThemePreferences(context: Context) : ThemeStore {
    private val prefs = context.getSharedPreferences(AppConfig.Storage.PREF_NAME_THEME, Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_APP_THEME = "app_theme"
    }

    /**
     * Retrieves the saved theme mode. Defaults to [ThemeMode.SYSTEM].
     */
    override fun getThemeMode(): ThemeMode {
        val modeName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return runCatching { ThemeMode.valueOf(modeName) }.getOrDefault(ThemeMode.SYSTEM)
    }

    /**
     * Saves the selected theme mode.
     */
    override fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    override fun getAppTheme(): AppTheme = AppTheme.fromStored(prefs.getString(KEY_APP_THEME, null))

    override fun setAppTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_APP_THEME, theme.name).apply()
    }
}
