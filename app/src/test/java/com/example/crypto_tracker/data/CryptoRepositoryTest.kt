package com.example.crypto_tracker.data

import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.repository.CryptoRepository
import com.example.crypto_tracker.data.repository.FetchState
import com.example.crypto_tracker.data.repository.HttpGet
import com.example.crypto_tracker.data.repository.HttpStatusException
import com.example.crypto_tracker.data.repository.ProviderGuard
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.UnknownHostException

class CryptoRepositoryTest {

    private val geckoUrl = AppConfig.Network.COINGECKO_TOP_COINS_URL
    private val paprikaUrl = AppConfig.Network.COINPAPRIKA_TOP_COINS_URL

    private val geckoJson = """[{"id":"bitcoin","name":"Bitcoin","symbol":"btc","market_cap_rank":1,"current_price":1.0}]"""
    private val paprikaJson = """
        [{"id":"btc-bitcoin","name":"Bitcoin","symbol":"BTC","rank":1,
          "quotes":{"USD":{"price":2.0,"volume_24h":1.0,"market_cap":1.0,"percent_change_24h":0.0}}}]
    """.trimIndent()

    private var now = 0L
    private val state = FetchState { now }

    private val calls = mutableListOf<String>()
    private var gecko: () -> String = { geckoJson }
    private var paprika: () -> String = { paprikaJson }

    private val http = HttpGet { url ->
        calls += url
        if (url == geckoUrl) gecko() else paprika()
    }

    private fun repository() = CryptoRepository(http, state) { _, _ -> }

    private suspend fun CryptoRepository.price() = fetchTopCryptos().getOrThrow().single().currentPriceUsd

    @Test
    fun usesCoinGeckoFirst() = runTest {
        assertEquals(1.0, repository().price(), 0.0)
        assertEquals(listOf(geckoUrl), calls)
    }

    @Test
    fun fallsBackToCoinPaprikaWhenCoinGeckoIsRateLimited() = runTest {
        gecko = { throw HttpStatusException(429, null) }

        assertEquals(2.0, repository().price(), 0.0)
        assertEquals(listOf(geckoUrl, paprikaUrl), calls)
    }

    @Test
    fun aGoodResultIsReusedInsideTheMinimumInterval() = runTest {
        val repo = repository()
        repo.price()
        now = AppConfig.RateLimit.MIN_FETCH_INTERVAL_MS - 1
        repo.price()

        assertEquals(1, calls.size)
    }

    @Test
    fun aNewRequestIsMadeOnceTheIntervalHasPassed() = runTest {
        val repo = repository()
        repo.price()
        now = AppConfig.RateLimit.MIN_FETCH_INTERVAL_MS
        repo.price()

        assertEquals(2, calls.size)
    }

    @Test
    fun aFailingProviderIsLeftAloneDuringItsBackoff() = runTest {
        gecko = { throw HttpStatusException(429, null) }
        val repo = repository()
        repo.price() // gecko fails -> cooling down, paprika answers

        now = AppConfig.RateLimit.MIN_FETCH_INTERVAL_MS + 1 // new fetch allowed, gecko still blocked (30 s)
        repo.price()
        assertEquals(1, calls.count { it == geckoUrl })

        now = AppConfig.RateLimit.BACKOFF_INITIAL_MS + 1 // gecko is tried again
        gecko = { geckoJson }
        assertEquals(1.0, repo.price(), 0.0)
        assertEquals(2, calls.count { it == geckoUrl })
    }

    @Test
    fun retryAfterFromTheServerIsHonouredUpToTheCap() = runTest {
        gecko = { throw HttpStatusException(429, 5 * 60_000L) }
        val repo = repository()
        repo.price()

        now = 4 * 60_000L
        repo.price()
        assertEquals(1, calls.count { it == geckoUrl })

        now = 5 * 60_000L + 1
        repo.price()
        assertEquals(2, calls.count { it == geckoUrl })
    }

    @Test
    fun failsWithTheLastErrorWhenEveryProviderFails() = runTest {
        gecko = { throw HttpStatusException(503, null) }
        paprika = { throw HttpStatusException(500, null) }

        val result = repository().fetchTopCryptos()

        assertTrue(result.exceptionOrNull() is HttpStatusException)
        assertEquals(500, (result.exceptionOrNull() as HttpStatusException).code)
    }

    @Test
    fun offlineKeepsTheUnknownHostErrorAndDoesNotPenaliseTheProviders() = runTest {
        gecko = { throw UnknownHostException("host") }
        paprika = { throw UnknownHostException("host") }
        val repo = repository()

        assertTrue(repo.fetchTopCryptos().exceptionOrNull() is UnknownHostException)

        // Back online right away: both providers are still available.
        gecko = { geckoJson }
        assertEquals(1.0, repo.price(), 0.0)
    }

    @Test
    fun backoffDoublesAndStopsAtTheCap() {
        var clock = 0L
        val guard = ProviderGuard { clock }

        repeat(3) { guard.recordFailure() } // 30 s, 60 s, 120 s -> blocked for 120 s
        clock = 119_999
        assertFalse(guard.canRequest())
        clock = 120_000
        assertTrue(guard.canRequest())

        repeat(20) { guard.recordFailure() }
        clock += AppConfig.RateLimit.BACKOFF_MAX_MS - 1
        assertFalse(guard.canRequest())
        clock += 1
        assertTrue(guard.canRequest())

        guard.recordSuccess()
        assertTrue(guard.canRequest())
    }
}
