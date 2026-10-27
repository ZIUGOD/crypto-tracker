package com.example.crypto_tracker.data.repository

import android.util.Log
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.model.CryptoCoin
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.UnknownHostException

/**
 * Contract for the market data source. Depend on this interface (not [CryptoRepository]) so the
 * data layer can be faked in tests or swapped for another provider.
 */
interface ICryptoRepository {
    suspend fun fetchTopCryptos(): Result<List<CryptoCoin>>
}

/** Process-wide request state, shared by every [CryptoRepository] (screen and background worker). */
class FetchState(val clock: () -> Long = { System.nanoTime() / 1_000_000 }) {
    internal val mutex = Mutex()
    internal val primary = ProviderGuard(clock)
    internal val fallback = ProviderGuard(clock)
    internal var lastCoins: List<CryptoCoin>? = null
    internal var lastFetchAt = 0L

    companion object {
        val shared = FetchState()
    }
}

/**
 * Fetches market data from CoinGecko and falls back to CoinPaprika when the primary provider is
 * rate-limited, unavailable or unreachable.
 *
 * Excessive-request protection (limits in [AppConfig.RateLimit]):
 * - one request at a time; callers that arrive while another is running wait and share its result;
 * - a fetch made less than `MIN_FETCH_INTERVAL_MS` after the last good one returns that result;
 * - a provider that fails (HTTP 429/5xx, timeout, bad payload) is skipped for a growing time.
 */
class CryptoRepository(
    private val http: HttpGet = UrlConnectionGet,
    private val state: FetchState = FetchState.shared,
    private val log: (String, Throwable) -> Unit = { message, error -> Log.w(TAG, message, error) }
) : ICryptoRepository {

    private companion object {
        const val TAG = "CryptoRepository"
    }

    private class Provider(
        val name: String,
        val guard: ProviderGuard,
        val url: String,
        val parse: (String) -> List<CryptoCoin>
    )

    private val providers = listOf(
        Provider("CoinGecko", state.primary, AppConfig.Network.COINGECKO_TOP_COINS_URL, CoinParser::fromCoinGecko),
        Provider("CoinPaprika", state.fallback, AppConfig.Network.COINPAPRIKA_TOP_COINS_URL, CoinParser::fromCoinPaprika)
    )

    override suspend fun fetchTopCryptos(): Result<List<CryptoCoin>> = withContext(Dispatchers.IO) {
        state.mutex.withLock {
            recentCoins()?.let { return@withLock Result.success(it) }
            fetchFromProviders().onSuccess {
                state.lastCoins = it
                state.lastFetchAt = state.clock()
            }
        }
    }

    private fun recentCoins(): List<CryptoCoin>? {
        val coins = state.lastCoins ?: return null
        val age = state.clock() - state.lastFetchAt
        return coins.takeIf { age >= 0 && age < AppConfig.RateLimit.MIN_FETCH_INTERVAL_MS }
    }

    private fun fetchFromProviders(): Result<List<CryptoCoin>> {
        var failure: Throwable = IOException("All providers are cooling down after errors")
        for (provider in providers) {
            if (!provider.guard.canRequest()) continue
            try {
                val coins = provider.parse(http.get(provider.url))
                if (coins.isEmpty()) throw IOException("${provider.name} returned no coins")
                provider.guard.recordSuccess()
                return Result.success(coins)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // No network at all says nothing about the provider, so it is not penalised.
                if (e !is UnknownHostException) {
                    provider.guard.recordFailure((e as? HttpStatusException)?.retryAfterMs)
                }
                log("${provider.name} failed", e)
                failure = e
            }
        }
        return Result.failure(failure)
    }
}
