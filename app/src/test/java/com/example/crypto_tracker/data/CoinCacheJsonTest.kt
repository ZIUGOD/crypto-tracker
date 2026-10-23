package com.example.crypto_tracker.data

import com.example.crypto_tracker.data.cache.CoinCacheJson
import com.example.crypto_tracker.data.model.CryptoCoin
import org.junit.Assert.assertEquals
import org.junit.Test

class CoinCacheJsonTest {

    @Test
    fun roundTripKeepsEveryField() {
        val full = CryptoCoin(
            id = "bitcoin", rank = 1, symbol = "BTC", name = "Bitcoin",
            currentPriceUsd = 65000.5, priceChangePercentage24h = -1.25,
            imageUrl = "https://img/btc.png", high24hUsd = 66000.0, low24hUsd = 64000.0,
            marketCapUsd = 1.2e12, totalVolumeUsd = 3.4e10, description = "Digital gold"
        )
        val minimal = CryptoCoin("eth", 2, "ETH", "Ethereum", 3000.0, 0.5)

        val decoded = CoinCacheJson.decode(CoinCacheJson.encode(listOf(full, minimal)))

        assertEquals(listOf(full, minimal), decoded)
    }
}
