package com.example.crypto_tracker.data

import com.example.crypto_tracker.data.cache.CachedCoins
import com.example.crypto_tracker.data.cache.CoinCache
import com.example.crypto_tracker.data.model.CryptoCoin
import com.example.crypto_tracker.data.repository.CachingCryptoRepository
import com.example.crypto_tracker.data.repository.ICryptoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CachingCryptoRepositoryTest {

    private class RecordingCache : CoinCache {
        var saved: List<CryptoCoin>? = null
        override fun load(): CachedCoins? = null
        override fun save(coins: List<CryptoCoin>, savedAtMillis: Long) {
            saved = coins
        }
    }

    private class FakeRepository(val result: Result<List<CryptoCoin>>) : ICryptoRepository {
        override suspend fun fetchTopCryptos() = result
    }

    private val coins = listOf(CryptoCoin("bitcoin", 1, "BTC", "Bitcoin", 100.0, 1.0))

    @Test
    fun savesAndReturnsCoinsOnSuccess() = runTest {
        val cache = RecordingCache()

        val result = CachingCryptoRepository(FakeRepository(Result.success(coins)), cache).fetchTopCryptos()

        assertEquals(coins, result.getOrNull())
        assertEquals(coins, cache.saved)
    }

    @Test
    fun savesNothingAndPropagatesTheErrorOnFailure() = runTest {
        val cache = RecordingCache()

        val result = CachingCryptoRepository(FakeRepository(Result.failure(Exception("offline"))), cache).fetchTopCryptos()

        assertTrue(result.isFailure)
        assertNull(cache.saved)
    }
}
