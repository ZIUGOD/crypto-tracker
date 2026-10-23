package com.example.crypto_tracker.config

/**
 * Centralized Application Configuration object.
 *
 * Stores all global constants, network endpoints, timing intervals, and business logic caps
 * in a single source of truth for easy future adjustments and long-term project maintainability.
 *
 * Rule of thumb: if a value is something a developer may want to tweak without reading the code
 * that uses it (a number, a flag, a URL, an ID), it belongs here.
 */
object AppConfig {

    // Visual design switches (tokens such as radii and opacities live in GlassDefaults)
    object Design {
        /** Real backdrop blur behind glass surfaces (Android 12+). When false, glass uses the opaque fallback. */
        const val GLASS_BACKDROP_BLUR_ENABLED = true

        /** Silk ribbons, dot grid and grain behind the app. When false, only the plain gradient is drawn. */
        const val BACKGROUND_TEXTURE_ENABLED = true
    }

    // Bottom Navigation Bar Configuration
    object Navigation {
        /**
         * Boolean flag controlling whether to display text labels under bottom navigation icons.
         * Set to true to display screen titles under icons, or false for icon-only navigation bar.
         */
        const val SHOW_NAVIGATION_LABELS = false

        /**
         * Single master height value (X) for the bottom navigation bar in dp.
         * Standard Material 3 height is 80dp. Adjusting this value automatically scales icon sizes proportionally.
         */
        const val NAV_BAR_HEIGHT_DP = 60

        /**
         * Empty space in dp to the left and right of the bottom navigation bar.
         * 0 makes the bar span the full screen width.
         */
        const val NAV_BAR_OUTER_PADDING_HORIZONTAL_DP = 10

        /**
         * Empty space in dp above and below the bottom navigation bar (added on top of the system
         * navigation-bar inset). 0 makes the bar touch the content and the screen's bottom edge.
         */
        const val NAV_BAR_OUTER_PADDING_VERTICAL_DP = 0

        /**
         * Automatically calculates icon size proportionally based on [NAV_BAR_HEIGHT_DP].
         * Maintains standard Material 3 ratio (~30% of bar height, e.g., 24dp for 80dp height).
         */
        val NAV_BAR_ICON_SIZE_DP: Int
            get() = (NAV_BAR_HEIGHT_DP * 0.70f).toInt().coerceAtLeast(16)
    }

    // Market data
    object Market {
        /** How many top-ranked coins are requested and displayed. */
        const val TOP_COINS_COUNT = 10
    }

    // Network & API Endpoints
    object Network {
        const val CONNECT_TIMEOUT_MS = 8_000
        const val READ_TIMEOUT_MS = 8_000

        val COINGECKO_TOP_COINS_URL =
            "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=${Market.TOP_COINS_COUNT}&page=1&sparkline=false"

        val COINCAP_TOP_COINS_URL =
            "https://api.coincap.io/v2/assets?limit=${Market.TOP_COINS_COUNT}"

        /** Public web page of a coin; append the coin id. */
        const val COIN_DETAILS_PAGE_URL = "https://www.coingecko.com/en/coins/"

        fun coincapIconUrl(symbolLower: String): String =
            "https://assets.coincap.io/assets/icons/$symbolLower@2x.png"
    }

    // Polling & Background Sync Timings
    object Timing {
        /** UI Auto-refresh interval in milliseconds (1 minute) */
        const val UI_POLLING_INTERVAL_MS = 60_000L

        /** Background WorkManager periodic sync interval in minutes */
        const val BACKGROUND_SYNC_INTERVAL_MINUTES = 15L

        /** Pattern of the "Updated: ..." timestamp shown on the Home screen. */
        const val LAST_UPDATED_TIME_PATTERN = "HH:mm:ss"

        /** Same label for cached data saved on an earlier day, where the time alone would mislead. */
        const val CACHED_DATE_TIME_PATTERN = "dd/MM HH:mm"
    }

    // Notification & Portfolio Limits
    object Notifications {
        /** Maximum number of cryptocurrencies allowed for notification pinning */
        const val MAX_NOTIFIED_COINS = 5

        const val CHANNEL_ID = "crypto_price_channel"

        /** Base for per-coin notification IDs; each coin gets BASE + (hash % ID_RANGE). */
        const val NOTIFICATION_BASE_ID = 2000
        const val NOTIFICATION_ID_RANGE = 1000
    }

    // Advertising
    object Ads {
        /** Google test banner unit. Replace with the real unit ID (and the manifest APPLICATION_ID) before release. */
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val BANNER_HEIGHT_DP = 50
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
        const val PREF_NAME_COIN_CACHE = "coin_cache_prefs"
    }
}
