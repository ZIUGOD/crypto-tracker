package com.example.crypto_tracker.data.preferences

import android.content.Context

/**
 * Manages user authentication session state locally.
 */
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

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
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Saves authenticated user session data.
     */
    fun saveUserSession(email: String, name: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    fun savePrivacyConsent() {
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
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    /**
     * Returns the logged-in user's email.
     */
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    /**
     * Returns the logged-in user's display name.
     */
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
}
