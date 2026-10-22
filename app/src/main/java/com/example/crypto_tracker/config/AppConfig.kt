package com.example.crypto_tracker.config

/**
 * Centralized Application Configuration object.
 *
 * Stores all global constants, network endpoints, timing intervals, and business logic caps
 * in a single source of truth for easy future adjustments and long-term project maintainability.
 */
object AppConfig {

    // Network & API Endpoints
    object Network {
        const val CONNECT_TIMEOUT_MS = 8_000
        const val READ_TIMEOUT_MS = 8_000

        const val COINGECKO_TOP_10_URL =
            "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=10&page=1&sparkline=false"

        const val COINCAP_TOP_10_URL =
            "https://api.coincap.io/v2/assets?limit=10"

        fun coincapIconUrl(symbolLower: String): String =
            "https://assets.coincap.io/assets/icons/$symbolLower@2x.png"
    }

    // Polling & Background Sync Timings
    object Timing {
        /** UI Auto-refresh interval in milliseconds (1 minute) */
        const val UI_POLLING_INTERVAL_MS = 60_000L

        /** Background WorkManager periodic sync interval in minutes */
        const val BACKGROUND_SYNC_INTERVAL_MINUTES = 15L
    }

    // Notification & Portfolio Limits
    object Notifications {
        /** Maximum number of cryptocurrencies allowed for notification pinning */
        const val MAX_NOTIFIED_COINS = 5

        const val CHANNEL_ID = "crypto_price_channel"
        const val NOTIFICATION_ID = 1001
    }

    // Legal & External Policy Links
    object Legal {
        const val PRIVACY_POLICY_URL = "https://example.com/privacy"
        const val TERMS_OF_USE_URL = "https://example.com/terms"
    }

    // SharedPreferences Keys
    object Storage {
        const val PREF_NAME_THEME = "theme_prefs"
        const val PREF_NAME_NOTIFICATIONS = "crypto_notif_prefs"
        const val PREF_NAME_SESSION = "session_prefs"
    }
}
