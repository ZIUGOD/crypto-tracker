package com.example.crypto_tracker.notification

import android.content.Context
import com.example.crypto_tracker.config.AppConfig

/**
 * Manages user preferences for which cryptocurrencies should be tracked in the persistent notification.
 * Pre-selects the #1 ranked market cryptocurrency on first launch, allowing 0 to 5 selections thereafter.
 */
class NotificationPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(AppConfig.Storage.PREF_NAME_NOTIFICATIONS, Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SELECTED_IDS = "selected_coin_ids"
        private const val KEY_HAS_INITIALIZED_DEFAULT = "has_initialized_default"

        /**
         * Centralized reference to maximum notified coins.
         */
        val MAX_NOTIFIED_COINS: Int
            get() = AppConfig.Notifications.MAX_NOTIFIED_COINS
    }

    /**
     * Returns the set of coin IDs selected by the user for notifications.
     * Defaults to Bitcoin ("bitcoin") on first ever app launch.
     */
    fun getSelectedCoinIds(): Set<String> {
        val hasInitialized = prefs.getBoolean(KEY_HAS_INITIALIZED_DEFAULT, false)
        if (!hasInitialized) {
            val defaultSet = setOf("bitcoin")
            prefs.edit()
                .putStringSet(KEY_SELECTED_IDS, defaultSet)
                .putBoolean(KEY_HAS_INITIALIZED_DEFAULT, true)
                .apply()
            return defaultSet
        }
        return prefs.getStringSet(KEY_SELECTED_IDS, emptySet()) ?: emptySet()
    }

    /**
     * Saves the set of coin IDs selected by the user for notifications, strictly capped at [MAX_NOTIFIED_COINS].
     */
    fun setSelectedCoinIds(coinIds: Set<String>) {
        val limitedIds = coinIds.take(AppConfig.Notifications.MAX_NOTIFIED_COINS).toSet()
        prefs.edit()
            .putStringSet(KEY_SELECTED_IDS, limitedIds)
            .putBoolean(KEY_HAS_INITIALIZED_DEFAULT, true)
            .apply()
    }

    /**
     * Ensures default notification selection using the #1 ranked cryptocurrency ID on first launch.
     */
    fun ensureDefaultSelectionFromTop1(top1CoinId: String): Set<String> {
        val hasInitialized = prefs.getBoolean(KEY_HAS_INITIALIZED_DEFAULT, false)
        val currentIds = getSelectedCoinIds()
        if (!hasInitialized) {
            val defaultSet = setOf(top1CoinId)
            prefs.edit()
                .putStringSet(KEY_SELECTED_IDS, defaultSet)
                .putBoolean(KEY_HAS_INITIALIZED_DEFAULT, true)
                .apply()
            return defaultSet
        }
        return currentIds
    }
}
