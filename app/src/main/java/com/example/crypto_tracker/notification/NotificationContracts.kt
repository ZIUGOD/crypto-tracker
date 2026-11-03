package com.example.crypto_tracker.notification

import com.example.crypto_tracker.data.model.CryptoCoin

/**
 * Persistence contract for the coins the user pinned to the notification bar.
 * Lets ViewModels depend on an abstraction instead of SharedPreferences.
 */
interface PinnedCoinsStore {
    /** Pure read: never writes. Empty until the user pins a coin (nothing is pinned by default). */
    fun getSelectedCoinIds(): Set<String>

    /** Stores [coinIds], capped at the configured maximum. */
    fun setSelectedCoinIds(coinIds: Set<String>)
}

/** Presentation contract for live-price notifications. */
interface PriceNotifier {
    /** Shows one notification per selected coin and cancels the ones that are no longer selected. */
    fun showPortfolioNotification(coins: List<CryptoCoin>, selectedCoinIds: Set<String>)
}
