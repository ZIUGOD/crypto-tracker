package com.example.crypto_tracker.data.repository

import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.data.model.CryptoCoin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Interface defining cryptocurrency repository contracts for clean testing and future data source swapping.
 */
interface ICryptoRepository {
    suspend fun fetchTop10Cryptos(): Result<List<CryptoCoin>>
}

/**
 * Primary repository implementation responsible for fetching cryptocurrency market data from remote REST APIs.
 *
 * Implements primary fetching from CoinGecko API and automatic fallback to CoinCap API
 * in case of rate-limiting or service unavailability.
 */
class CryptoRepository : ICryptoRepository {

    /**
     * Fetches the top 10 cryptocurrencies ordered dynamically by market capitalization rank.
     *
     * Executes asynchronously off the main thread using [Dispatchers.IO].
     *
     * @return [Result] containing a sorted list of [CryptoCoin] on success, or an Exception on failure.
     */
    override suspend fun fetchTop10Cryptos(): Result<List<CryptoCoin>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(AppConfig.Network.COINGECKO_TOP_10_URL)

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = AppConfig.Network.CONNECT_TIMEOUT_MS
                readTimeout = AppConfig.Network.READ_TIMEOUT_MS
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(responseText)

                val coins = mutableListOf<CryptoCoin>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    coins.add(
                        CryptoCoin(
                            id = obj.getString("id"),
                            rank = obj.optInt("market_cap_rank", i + 1),
                            symbol = obj.getString("symbol").uppercase(),
                            name = obj.getString("name"),
                            currentPriceUsd = obj.getDouble("current_price"),
                            priceChangePercentage24h = obj.optDouble("price_change_percentage_24h", 0.0),
                            imageUrl = obj.optString("image").takeIf { it.isNotBlank() }
                        )
                    )
                }
                // Ensure coins are strictly sorted by their market cap rank
                coins.sortedBy { it.rank }
            } else {
                // Secondary API Fallback: CoinCap API in case CoinGecko is rate-limited (HTTP 429)
                fetchTop10FromCoinCap()
            }
        }
    }

    /**
     * Fallback method to fetch top 10 coins from CoinCap API.
     *
     * @return List of [CryptoCoin] items sorted by rank.
     * @throws Exception if connection fails or HTTP code is not OK.
     */
    private fun fetchTop10FromCoinCap(): List<CryptoCoin> {
        val url = URL(AppConfig.Network.COINCAP_TOP_10_URL)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = AppConfig.Network.CONNECT_TIMEOUT_MS
            readTimeout = AppConfig.Network.READ_TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
        }

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)
            val dataArray = json.getJSONArray("data")

            val coins = mutableListOf<CryptoCoin>()
            for (i in 0 until dataArray.length()) {
                val obj = dataArray.getJSONObject(i)
                val symbolLower = obj.getString("symbol").lowercase()
                coins.add(
                    CryptoCoin(
                        id = obj.getString("id"),
                        rank = obj.optInt("rank", i + 1),
                        symbol = obj.getString("symbol").uppercase(),
                        name = obj.getString("name"),
                        currentPriceUsd = obj.getString("priceUsd").toDoubleOrNull() ?: 0.0,
                        priceChangePercentage24h = obj.getString("changePercent24Hr").toDoubleOrNull() ?: 0.0,
                        imageUrl = AppConfig.Network.coincapIconUrl(symbolLower)
                    )
                )
            }
            return coins.sortedBy { it.rank }
        } else {
            throw Exception("API HTTP error code: ${connection.responseCode}")
        }
    }
}
