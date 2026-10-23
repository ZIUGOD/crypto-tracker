package com.example.crypto_tracker.data.preferences

import android.content.Context
import com.example.crypto_tracker.config.AppConfig
import java.util.Locale

/**
 * Languages the user can pick in Settings. To add one: add an entry here and a matching
 * `res/values-<qualifier>/strings.xml` (e.g. `values-es` for "es", `values-pt-rBR` for "pt-BR").
 * [nativeName] is deliberately not translated, so every language stays recognisable in the picker.
 */
enum class AppLanguage(val tag: String, val nativeName: String) {
    ENGLISH("en", "English"),
    PORTUGUESE_BRAZIL("pt-BR", "Português (Brasil)");

    val locale: Locale get() = Locale.forLanguageTag(tag)

    companion object {
        /** Used on first launch and whenever the stored value is unknown. Also the resource fallback (`res/values`). */
        val Default = ENGLISH

        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag == tag } ?: Default
    }
}

/** Contract for persisting the user's language choice. */
interface LanguageStore {
    fun getLanguage(): AppLanguage
    fun setLanguage(language: AppLanguage)
}

/** SharedPreferences-backed [LanguageStore]. Defaults to [AppLanguage.Default], not the device language. */
class LanguagePreferences(context: Context) : LanguageStore {
    private val prefs = context.getSharedPreferences(AppConfig.Storage.PREF_NAME_LANGUAGE, Context.MODE_PRIVATE)

    override fun getLanguage(): AppLanguage = AppLanguage.fromTag(prefs.getString(KEY_LANGUAGE_TAG, null))

    override fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE_TAG, language.tag).apply()
    }

    private companion object {
        const val KEY_LANGUAGE_TAG = "language_tag"
    }
}
