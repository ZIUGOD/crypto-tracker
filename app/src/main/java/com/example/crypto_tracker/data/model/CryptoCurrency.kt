package com.example.crypto_tracker.data.model

enum class CryptoCurrency(
    val id: String,
    val symbol: String,
    val displayName: String,
    val iconSymbol: String,
    val colorHex: Long
) {
    BITCOIN("bitcoin", "BTC", "Bitcoin", "₿", 0xFFF7931A),
    ETHEREUM("ethereum", "ETH", "Ethereum", "Ξ", 0xFF627EEA),
    SOLANA("solana", "SOL", "Solana", "◎", 0xFF14F195),
    CARDANO("cardano", "ADA", "Cardano", "₳", 0xFF0033AD),
    RIPPLE("ripple", "XRP", "XRP", "✕", 0xFF23292F),
    DOGECOIN("dogecoin", "DOGE", "Dogecoin", "Ð", 0xFFC2A633);

    companion object {
        val DEFAULT_SELECTED = setOf(BITCOIN)
    }
}
