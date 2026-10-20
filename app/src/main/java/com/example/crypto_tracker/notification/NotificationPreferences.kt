package com.example.crypto_tracker.notification

import android.content.Context

/**
 * Manages user preferences for which cryptocurrencies should be tracked in the persistent notification.
 * Organized cleanly for future maintainability and data retrieval.
 */
class NotificationPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("crypto_notif_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SELECTED_IDS = "selected_coin_ids"

        /**
         * Maximum number of cryptocurrencies the user is allowed to select for notifications.
         * Configured as a centralized constant for easy future modifications.
         */
        const val MAX_NOTIFIED_COINS = 5
    }

    /**
     * Returns the set of coin IDs selected by the user for notifications.
     * Defaults to Bitcoin ("bitcoin") if nothing is selected.
     */
    fun getSelectedCoinIds(): Set<String> {
        return prefs.getStringSet(KEY_SELECTED_IDS, setOf("bitcoin")) ?: setOf("bitcoin")
    }

    /**
     * Saves the set of coin IDs selected by the user for notifications, strictly capped at [MAX_NOTIFIED_COINS].
     */
    fun setSelectedCoinIds(coinIds: Set<String>) {
        val limitedIds = coinIds.take(MAX_NOTIFIED_COINS).toSet()
        prefs.edit().putStringSet(KEY_SELECTED_IDS, limitedIds).apply()
    }
}
