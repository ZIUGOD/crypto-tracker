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
}

class ThemePreferences(context: Context) : ThemeStore {
    private val prefs = context.getSharedPreferences(AppConfig.Storage.PREF_NAME_THEME, Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
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
}
