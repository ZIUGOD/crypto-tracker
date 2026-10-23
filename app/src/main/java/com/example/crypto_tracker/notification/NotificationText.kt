package com.example.crypto_tracker.notification

import com.example.crypto_tracker.data.model.CryptoCoin
import java.util.Locale

/** Body of a coin's notification, e.g. "$60,000.50 (+2.5%)". Pure, so it can be unit-tested. */
fun notificationPriceText(coin: CryptoCoin): String {
    val sign = if (coin.priceChangePercentage24h >= 0) "+" else ""
    val price = String.format(Locale.US, "%,.2f", coin.currentPriceUsd)
    val change = String.format(Locale.US, "%,.1f", coin.priceChangePercentage24h)
    return "$$price ($sign$change%)"
}

/**
 * Remembers what was last posted per notification so identical updates can be skipped,
 * instead of re-posting every notification on each polling tick.
 */
class PostedNotificationCache {
    private val lastTextById = mutableMapOf<Int, String>()

    /** True (and records [text]) when [text] differs from what is currently shown for [id]. */
    fun shouldPost(id: Int, text: String): Boolean {
        if (lastTextById[id] == text) return false
        lastTextById[id] = text
        return true
    }

    fun forget(id: Int) {
        lastTextById.remove(id)
    }
}
