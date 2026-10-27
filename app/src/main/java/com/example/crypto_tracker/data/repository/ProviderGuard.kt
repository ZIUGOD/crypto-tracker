package com.example.crypto_tracker.data.repository

import com.example.crypto_tracker.config.AppConfig

/**
 * Protects one data provider from excessive requests. After a failure the provider is left alone for
 * a growing time (exponential back-off, or the server's own `Retry-After`), so a rate-limited or down
 * provider is not hit again on every refresh. A success resets it.
 *
 * @param clock Monotonic milliseconds; injected so tests do not have to wait.
 */
class ProviderGuard(private val clock: () -> Long) {
    private var failures = 0
    private var blockedUntil = Long.MIN_VALUE

    @Synchronized
    fun canRequest(): Boolean = clock() >= blockedUntil

    @Synchronized
    fun recordSuccess() {
        failures = 0
        blockedUntil = Long.MIN_VALUE
    }

    @Synchronized
    fun recordFailure(retryAfterMs: Long? = null) {
        failures++
        val backoff = AppConfig.RateLimit.BACKOFF_INITIAL_MS shl (failures - 1).coerceAtMost(MAX_SHIFT)
        val wait = (retryAfterMs ?: backoff).coerceIn(AppConfig.RateLimit.BACKOFF_INITIAL_MS, AppConfig.RateLimit.BACKOFF_MAX_MS)
        blockedUntil = clock() + wait
    }

    private companion object {
        /** Keeps the shift well below overflow; the cap is applied right after anyway. */
        const val MAX_SHIFT = 10
    }
}
