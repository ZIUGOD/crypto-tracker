package com.example.crypto_tracker.notification

import android.content.Context
import com.example.crypto_tracker.config.AppConfig

/**
 * SharedPreferences-backed [PinnedCoinsStore]. The #1 ranked coin is pinned on first launch,
 * allowing up to [AppConfig.Notifications.MAX_NOTIFIED_COINS] selections thereafter.
 */
class NotificationPreferences(context: Context) : PinnedCoinsStore {
    private val prefs = context.getSharedPreferences(AppConfig.Storage.PREF_NAME_NOTIFICATIONS, Context.MODE_PRIVATE)

    private companion object {
        const val KEY_SELECTED_IDS = "selected_coin_ids"
        const val KEY_HAS_INITIALIZED_DEFAULT = "has_initialized_default"
    }

    override fun getSelectedCoinIds(): Set<String> =
        prefs.getStringSet(KEY_SELECTED_IDS, emptySet()) ?: emptySet()

    override fun setSelectedCoinIds(coinIds: Set<String>) {
        val limitedIds = coinIds.take(AppConfig.Notifications.MAX_NOTIFIED_COINS).toSet()
        prefs.edit()
            .putStringSet(KEY_SELECTED_IDS, limitedIds)
            .putBoolean(KEY_HAS_INITIALIZED_DEFAULT, true)
            .apply()
    }

    override fun ensureDefaultSelectionFromTop1(top1CoinId: String): Set<String> {
        if (prefs.getBoolean(KEY_HAS_INITIALIZED_DEFAULT, false)) {
            return getSelectedCoinIds()
        }
        val selection = setOf(top1CoinId)
        setSelectedCoinIds(selection)
        return selection
    }
}
