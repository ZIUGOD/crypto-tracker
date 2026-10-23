package com.example.crypto_tracker.data.repository

/**
 * Short, hand-written summaries for well-known coins. To add a coin, add one entry to [KNOWN]
 * keyed by its CoinGecko/CoinCap id; unknown coins get a generic sentence.
 */
object CoinDescriptions {

    private val KNOWN = mapOf(
        "bitcoin" to "Bitcoin (BTC) is the world's first decentralized peer-to-peer digital currency, designed to act as a store of value and digital gold.",
        "ethereum" to "Ethereum (ETH) is a decentralized smart contract platform that powers decentralized applications (dApps), DeFi, and Web3 ecosystems.",
        "tether" to "Tether (USDt) is a pioneer fiat-collateralized stablecoin pegged 1:1 to the US Dollar to provide stability in crypto markets.",
        "binancecoin" to "BNB powers the BNB Chain ecosystem, offering utility for trading fee discounts, DeFi protocols, and smart contracts.",
        "solana" to "Solana (SOL) is a high-performance blockchain built for speed and low-cost transactions, supporting NFTs, DeFi, and Web3.",
        "ripple" to "XRP is designed for fast, low-cost global cross-border payments and financial institution remittances.",
        "usd-coin" to "USDC is a fully backed US Dollar stablecoin providing dollar liquidity and instant settlement across Web3.",
        "cardano" to "Cardano (ADA) is a proof-of-stake blockchain platform designed for security, sustainability, and peer-reviewed smart contracts.",
        "dogecoin" to "Dogecoin (DOGE) is an open-source peer-to-peer cryptocurrency born from internet meme culture with a vibrant community.",
        "avalanche-2" to "Avalanche (AVAX) is an ultra-fast smart contract platform designed for scalable enterprise and DeFi applications."
    )

    // Ids that different data providers use for the same coin.
    private val ALIASES = mapOf(
        "bnb" to "binancecoin",
        "xrp" to "ripple",
        "avalanche" to "avalanche-2"
    )

    fun forCoin(id: String, name: String, symbol: String): String {
        val key = id.lowercase().let { ALIASES[it] ?: it }
        return KNOWN[key] ?: "$name ($symbol) is one of the top cryptocurrencies globally by market capitalization."
    }
}
