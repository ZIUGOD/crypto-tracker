package com.example.crypto_tracker.data.preferences

import android.content.Context
import android.content.res.Configuration

/**
 * Applies the stored [AppLanguage] to a [Context]. Wrap every context that loads strings:
 * the Activity (in `attachBaseContext`) and anything that runs without one, such as the
 * notification manager used by the background worker.
 */
object AppLocale {

    fun wrap(base: Context): Context = wrap(base, LanguagePreferences(base).getLanguage())

    fun wrap(base: Context, language: AppLanguage): Context {
        val locale = language.locale
        java.util.Locale.setDefault(locale)
        val configuration = Configuration(base.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        return base.createConfigurationContext(configuration)
    }
}
