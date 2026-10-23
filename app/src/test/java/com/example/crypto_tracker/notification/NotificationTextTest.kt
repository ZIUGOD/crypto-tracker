package com.example.crypto_tracker.notification

import com.example.crypto_tracker.data.model.CryptoCoin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationTextTest {

    private fun coin(price: Double, change: Double) =
        CryptoCoin("bitcoin", 1, "BTC", "Bitcoin", price, change)

    @Test
    fun priceText_positiveChangeHasPlusSign() {
        assertEquals("$60,000.50 (+2.5%)", notificationPriceText(coin(60000.5, 2.5)))
    }

    @Test
    fun priceText_negativeChangeKeepsItsSign() {
        assertEquals("$1,234.00 (-1.3%)", notificationPriceText(coin(1234.0, -1.26)))
    }

    @Test
    fun cache_postsFirstTimeAndOnChangeOnly() {
        val cache = PostedNotificationCache()

        assertTrue(cache.shouldPost(1, "$10"))
        assertFalse(cache.shouldPost(1, "$10"))
        assertTrue(cache.shouldPost(1, "$11"))
    }

    @Test
    fun cache_tracksEachNotificationIndependently() {
        val cache = PostedNotificationCache()

        cache.shouldPost(1, "$10")

        assertTrue(cache.shouldPost(2, "$10"))
    }

    @Test
    fun cache_forgetAllowsReposting() {
        val cache = PostedNotificationCache()
        cache.shouldPost(1, "$10")

        cache.forget(1)

        assertTrue(cache.shouldPost(1, "$10"))
    }
}
