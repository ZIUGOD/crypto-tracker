package com.example.crypto_tracker.notification

import com.example.crypto_tracker.data.model.CryptoCoin

/**
 * Persistence contract for the coins the user pinned to the notification bar.
 * Lets ViewModels depend on an abstraction instead of SharedPreferences.
 */
interface PinnedCoinsStore {
    /** Pure read: never writes. Empty until the first selection exists. */
    fun getSelectedCoinIds(): Set<String>

    /** Stores [coinIds], capped at the configured maximum. */
    fun setSelectedCoinIds(coinIds: Set<String>)

    /** On first launch pins [top1CoinId]; afterwards returns the stored selection untouched. */
    fun ensureDefaultSelectionFromTop1(top1CoinId: String): Set<String>
}

/**
 * Pins the #1 ranked coin (or the first listed one) on first launch and returns the current
 * selection. Shared by the foreground ViewModel and the background worker.
 */
fun PinnedCoinsStore.ensureDefaultSelection(coins: List<CryptoCoin>): Set<String> {
    val topCoin = coins.firstOrNull { it.rank == 1 } ?: coins.firstOrNull() ?: return getSelectedCoinIds()
    return ensureDefaultSelectionFromTop1(topCoin.id)
}

/** Presentation contract for live-price notifications. */
interface PriceNotifier {
    /** Shows one notification per selected coin and cancels the ones that are no longer selected. */
    fun showPortfolioNotification(coins: List<CryptoCoin>, selectedCoinIds: Set<String>)
}
