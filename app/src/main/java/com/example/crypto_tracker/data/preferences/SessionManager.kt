package com.example.crypto_tracker.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.crypto_tracker.config.AppConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Contract for the locally stored user session (lets ViewModels be tested without Android). */
interface SessionStore {
    fun isLoggedIn(): Boolean
    fun getUserName(): String?
    fun getUserEmail(): String?
    fun saveUserSession(email: String, name: String)
    fun savePrivacyConsent()
    fun clearSession()
}

/** SharedPreferences-backed [SessionStore]: manages the authentication session locally. */
class SessionManager(context: Context) : SessionStore {
    private val prefs = context.getSharedPreferences(AppConfig.Storage.PREF_NAME_SESSION, Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PRIVACY_CONSENT_VERSION = "privacy_consent_version"
        private const val KEY_PRIVACY_CONSENT_AT = "privacy_consent_at"
        const val CURRENT_PRIVACY_CONSENT_VERSION = 1
    }

    /**
     * Checks if a user session is currently active.
     */
    override fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Saves authenticated user session data.
     */
    override fun saveUserSession(email: String, name: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    /** Label for the account badge: the user's name when signed in with current consent, otherwise null (guest). */
    fun accountLabel(): String? =
        if (isLoggedIn() && hasCurrentPrivacyConsent()) getUserName() ?: "Logged In" else null

    /** Emits [accountLabel] now and whenever the stored session changes, from any [SessionManager] instance. */
    fun observeAccountLabel(): Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> trySend(accountLabel()) }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(accountLabel())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    override fun savePrivacyConsent() {
        prefs.edit()
            .putInt(KEY_PRIVACY_CONSENT_VERSION, CURRENT_PRIVACY_CONSENT_VERSION)
            .putLong(KEY_PRIVACY_CONSENT_AT, System.currentTimeMillis())
            .apply()
    }

    fun hasCurrentPrivacyConsent(): Boolean =
        prefs.getInt(KEY_PRIVACY_CONSENT_VERSION, 0) == CURRENT_PRIVACY_CONSENT_VERSION

    fun revokePrivacyConsent() {
        prefs.edit()
            .remove(KEY_PRIVACY_CONSENT_VERSION)
            .remove(KEY_PRIVACY_CONSENT_AT)
            .apply()
    }

    /**
     * Clears user session (Sign out).
     */
    override fun clearSession() {
        prefs.edit().clear().apply()
    }

    /**
     * Returns the logged-in user's email.
     */
    override fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    /**
     * Returns the logged-in user's display name.
     */
    override fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
}
