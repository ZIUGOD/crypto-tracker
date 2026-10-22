package com.example.crypto_tracker.notification

import android.content.Context
import com.example.crypto_tracker.config.AppConfig

/**
 * Manages user preferences for which cryptocurrencies should be tracked in the persistent notification.
 * Dynamically derives default selections from the #1 ranked market cryptocurrency.
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
     * Returns empty set if no preferences have been saved or initialized yet.
     */
    fun getSelectedCoinIds(): Set<String> {
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
     * Ensures a default notification selection using the #1 ranked cryptocurrency ID
     * if the user has not explicitly configured custom preferences yet.
     *
     * @param top1CoinId ID of the #1 market cap ranked cryptocurrency.
     * @return Effective set of selected coin IDs.
     */
    fun ensureDefaultSelectionFromTop1(top1CoinId: String): Set<String> {
        val currentIds = getSelectedCoinIds()
        if (currentIds.isEmpty() && !prefs.getBoolean(KEY_HAS_INITIALIZED_DEFAULT, false)) {
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
