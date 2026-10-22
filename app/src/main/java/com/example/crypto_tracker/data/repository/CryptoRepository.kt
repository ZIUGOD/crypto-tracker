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
                    val id = obj.getString("id")
                    val name = obj.getString("name")
                    val symbol = obj.getString("symbol").uppercase()

                    coins.add(
                        CryptoCoin(
                            id = id,
                            rank = obj.optInt("market_cap_rank", i + 1),
                            symbol = symbol,
                            name = name,
                            currentPriceUsd = obj.getDouble("current_price"),
                            priceChangePercentage24h = obj.optDouble("price_change_percentage_24h", 0.0),
                            imageUrl = obj.optString("image").takeIf { it.isNotBlank() },
                            high24hUsd = obj.optDouble("high_24h", 0.0).takeIf { !it.isNaN() },
                            low24hUsd = obj.optDouble("low_24h", 0.0).takeIf { !it.isNaN() },
                            marketCapUsd = obj.optDouble("market_cap", 0.0).takeIf { !it.isNaN() },
                            totalVolumeUsd = obj.optDouble("total_volume", 0.0).takeIf { !it.isNaN() },
                            description = getCoinDescription(id, name, symbol)
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
                val id = obj.getString("id")
                val name = obj.getString("name")
                val symbol = obj.getString("symbol").uppercase()
                val symbolLower = symbol.lowercase()

                coins.add(
                    CryptoCoin(
                        id = id,
                        rank = obj.optInt("rank", i + 1),
                        symbol = symbol,
                        name = name,
                        currentPriceUsd = obj.getString("priceUsd").toDoubleOrNull() ?: 0.0,
                        priceChangePercentage24h = obj.getString("changePercent24Hr").toDoubleOrNull() ?: 0.0,
                        imageUrl = AppConfig.Network.coincapIconUrl(symbolLower),
                        marketCapUsd = obj.getString("marketCapUsd").toDoubleOrNull(),
                        totalVolumeUsd = obj.getString("volumeUsd24Hr").toDoubleOrNull(),
                        description = getCoinDescription(id, name, symbol)
                    )
                )
            }
            return coins.sortedBy { it.rank }
        } else {
            throw Exception("API HTTP error code: ${connection.responseCode}")
        }
    }

    /**
     * Returns a concise, helpful summary description for top cryptocurrencies.
     */
    private fun getCoinDescription(id: String, name: String, symbol: String): String {
        return when (id.lowercase()) {
            "bitcoin" -> "Bitcoin (BTC) is the world's first decentralized peer-to-peer digital currency, designed to act as a store of value and digital gold."
            "ethereum" -> "Ethereum (ETH) is a decentralized smart contract platform that powers decentralized applications (dApps), DeFi, and Web3 ecosystems."
            "tether" -> "Tether (USDt) is a pioneer fiat-collateralized stablecoin pegged 1:1 to the US Dollar to provide stability in crypto markets."
            "binancecoin", "bnb" -> "BNB powers the BNB Chain ecosystem, offering utility for trading fee discounts, DeFi protocols, and smart contracts."
            "solana" -> "Solana (SOL) is a high-performance blockchain built for speed and low-cost transactions, supporting NFTs, DeFi, and Web3."
            "ripple", "xrp" -> "XRP is designed for fast, low-cost global cross-border payments and financial institution remittances."
            "usd-coin" -> "USDC is a fully backed US Dollar stablecoin providing dollar liquidity and instant settlement across Web3."
            "cardano" -> "Cardano (ADA) is a proof-of-stake blockchain platform designed for security, sustainability, and peer-reviewed smart contracts."
            "dogecoin" -> "Dogecoin (DOGE) is an open-source peer-to-peer cryptocurrency born from internet meme culture with a vibrant community."
            "avalanche-2", "avalanche" -> "Avalanche (AVAX) is an ultra-fast smart contract platform designed for scalable enterprise and DeFi applications."
            else -> "$name ($symbol) is one of the top cryptocurrencies globally by market capitalization."
        }
    }
}
