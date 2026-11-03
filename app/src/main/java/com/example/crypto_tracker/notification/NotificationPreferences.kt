package com.example.crypto_tracker.notification

import android.content.Context
import com.example.crypto_tracker.config.AppConfig

/**
 * SharedPreferences-backed [PinnedCoinsStore]. Nothing is pinned until the user chooses,
 * allowing up to [AppConfig.Notifications.MAX_NOTIFIED_COINS] selections thereafter.
 */
class NotificationPreferences(context: Context) : PinnedCoinsStore {
    private val prefs = context.getSharedPreferences(AppConfig.Storage.PREF_NAME_NOTIFICATIONS, Context.MODE_PRIVATE)

    private companion object {
        const val KEY_SELECTED_IDS = "selected_coin_ids"
    }

    override fun getSelectedCoinIds(): Set<String> =
        prefs.getStringSet(KEY_SELECTED_IDS, emptySet()) ?: emptySet()

    override fun setSelectedCoinIds(coinIds: Set<String>) {
        val limitedIds = coinIds.take(AppConfig.Notifications.MAX_NOTIFIED_COINS).toSet()
        prefs.edit()
            .putStringSet(KEY_SELECTED_IDS, limitedIds)
            .apply()
    }
}
