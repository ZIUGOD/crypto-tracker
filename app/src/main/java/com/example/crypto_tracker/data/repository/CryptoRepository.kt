package com.example.crypto_tracker.data.repository

import android.util.Log
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.model.CryptoCoin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Contract for the market data source. Depend on this interface (not [CryptoRepository]) so the
 * data layer can be faked in tests or swapped for another provider.
 */
interface ICryptoRepository {
    suspend fun fetchTopCryptos(): Result<List<CryptoCoin>>
}

/**
 * Fetches market data from CoinGecko and automatically falls back to CoinCap when the primary
 * provider is rate-limited, unavailable or unreachable.
 */
class CryptoRepository : ICryptoRepository {

    private companion object {
        const val TAG = "CryptoRepository"
    }

    override suspend fun fetchTopCryptos(): Result<List<CryptoCoin>> = withContext(Dispatchers.IO) {
        runCatching { fetchFromPrimary() }
            .onFailure { Log.w(TAG, "Primary provider (CoinGecko) failed, trying fallback", it) }
            .recoverCatching { fetchFromFallback() }
            .onFailure { Log.w(TAG, "Fallback provider (CoinCap) failed", it) }
    }

    private fun fetchFromPrimary(): List<CryptoCoin> =
        CoinParser.fromCoinGecko(httpGet(AppConfig.Network.COINGECKO_TOP_COINS_URL))

    private fun fetchFromFallback(): List<CryptoCoin> =
        CoinParser.fromCoinCap(httpGet(AppConfig.Network.COINCAP_TOP_COINS_URL))

    /** @throws IOException on connection problems or any non-200 response. */
    private fun httpGet(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = AppConfig.Network.CONNECT_TIMEOUT_MS
            readTimeout = AppConfig.Network.READ_TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
        }
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode} from $url")
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
