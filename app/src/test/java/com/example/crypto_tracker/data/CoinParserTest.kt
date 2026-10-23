package com.example.crypto_tracker.data

import com.example.crypto_tracker.data.repository.CoinParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoinParserTest {

    private val coinGeckoJson = """
        [
          {"id":"ethereum","name":"Ethereum","symbol":"eth","market_cap_rank":2,"current_price":3000.5,
           "price_change_percentage_24h":-1.25,"image":"https://img/eth.png","high_24h":3100.0,"low_24h":2900.0,
           "market_cap":360000000000,"total_volume":15000000000},
          {"id":"bitcoin","name":"Bitcoin","symbol":"btc","market_cap_rank":1,"current_price":60000.0,
           "price_change_percentage_24h":2.5,"image":"","high_24h":null}
        ]
    """.trimIndent()

    @Test
    fun coinGecko_isSortedByRankAndSymbolUppercased() {
        val coins = CoinParser.fromCoinGecko(coinGeckoJson)

        assertEquals(listOf("bitcoin", "ethereum"), coins.map { it.id })
        assertEquals("BTC", coins.first().symbol)
    }

    @Test
    fun coinGecko_missingOptionalFieldsBecomeNull() {
        val bitcoin = CoinParser.fromCoinGecko(coinGeckoJson).first()

        assertNull(bitcoin.high24hUsd)
        assertNull(bitcoin.low24hUsd)
        assertNull(bitcoin.imageUrl)
    }

    @Test
    fun coinGecko_mapsNumericFields() {
        val ethereum = CoinParser.fromCoinGecko(coinGeckoJson).last()

        assertEquals(3000.5, ethereum.currentPriceUsd, 0.0)
        assertEquals(-1.25, ethereum.priceChangePercentage24h, 0.0)
        assertEquals(3100.0, ethereum.high24hUsd!!, 0.0)
    }

    @Test
    fun coinCap_parsesStringEncodedNumbers() {
        val json = """
            {"data":[{"id":"bitcoin","rank":"1","symbol":"BTC","name":"Bitcoin","priceUsd":"60000.12",
              "changePercent24Hr":"1.5","marketCapUsd":"1000","volumeUsd24Hr":"500"}]}
        """.trimIndent()

        val coin = CoinParser.fromCoinCap(json).single()

        assertEquals(60000.12, coin.currentPriceUsd, 0.0)
        assertEquals(1000.0, coin.marketCapUsd!!, 0.0)
        assertTrue(coin.imageUrl!!.endsWith("btc@2x.png"))
    }
}
