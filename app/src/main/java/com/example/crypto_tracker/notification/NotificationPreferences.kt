package com.example.crypto_tracker.notification

import android.content.Context

/**
 * Manages user preferences for which cryptocurrencies should be tracked in the persistent notification.
 */
class NotificationPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("crypto_notif_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SELECTED_IDS = "selected_coin_ids"
    }

    /**
     * Returns the set of coin IDs selected by the user for notifications.
     * Defaults to Bitcoin ("bitcoin") if nothing is selected.
     */
    fun getSelectedCoinIds(): Set<String> {
        return prefs.getStringSet(KEY_SELECTED_IDS, setOf("bitcoin")) ?: setOf("bitcoin")
    }

    /**
     * Saves the set of coin IDs selected by the user for notifications.
     */
    fun setSelectedCoinIds(coinIds: Set<String>) {
        prefs.edit().putStringSet(KEY_SELECTED_IDS, coinIds).apply()
    }
}
