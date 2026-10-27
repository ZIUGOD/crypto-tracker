package com.example.crypto_tracker.data.repository

import com.example.crypto_tracker.config.AppConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Minimal HTTP GET so the repository can be tested without a network. */
fun interface HttpGet {
    /** @throws IOException on connection problems; [HttpStatusException] on any non-200 response. */
    fun get(url: String): String
}

/** A non-200 answer. [retryAfterMs] is the server's `Retry-After` hint, when it sent one. */
class HttpStatusException(val code: Int, val retryAfterMs: Long?) : IOException("HTTP $code")

object UrlConnectionGet : HttpGet {
    override fun get(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = AppConfig.Network.CONNECT_TIMEOUT_MS
            readTimeout = AppConfig.Network.READ_TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
        }
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                val retryAfterSeconds = connection.getHeaderField("Retry-After")?.trim()?.toLongOrNull()
                throw HttpStatusException(connection.responseCode, retryAfterSeconds?.let { it * 1_000 })
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
