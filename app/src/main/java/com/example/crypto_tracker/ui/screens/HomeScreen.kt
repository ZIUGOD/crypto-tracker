package com.example.crypto_tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.crypto_tracker.data.model.CryptoCoin
import com.example.crypto_tracker.data.repository.CryptoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main Home Screen displaying the top 10 cryptocurrencies ranked dynamically by market cap.
 *
 * Automatically refreshes price and market rankings every 60 seconds (1 minute).
 *
 * @param modifier Layout modifier passed from parent scaffold padding.
 */
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val repository = remember { CryptoRepository() }

    // State holders for UI data
    var topCoins by remember { mutableStateOf<List<CryptoCoin>>(emptyList()) }
    var lastUpdated by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableIntStateOf(0) }

    // LaunchedEffect handles initial data fetch and 1-minute auto-refresh loop.
    // Re-triggers if retryKey changes (e.g. user clicks "Try Again").
    LaunchedEffect(retryKey) {
        isLoading = true
        while (isActive) {
            val result = repository.fetchTop10Cryptos()
            result.onSuccess { coins ->
                topCoins = coins
                lastUpdated = getCurrentTimeFormatted()
                errorMessage = null
            }.onFailure {
                if (topCoins.isEmpty()) {
                    errorMessage = "Failed to load rankings. Please check your connection."
                }
            }
            isLoading = false
            delay(60_000L) // Auto-refresh interval (1 minute)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Section Header
        Text(
            text = "Top 10 Cryptocurrencies 🏆",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Global market ranking by market capitalization",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status Bar (Loading Indicator / Refresh Note / Timestamp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Updating rankings...", fontSize = 12.sp, color = Color.Gray)
                } else {
                    Text(
                        text = "🔄 Auto-refreshes every 1 min",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            if (lastUpdated != null) {
                Text(
                    text = "Updated: $lastUpdated",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Ranking List or Fallback UI (Error / Loading)
        if (errorMessage != null && topCoins.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { retryKey++ }) {
                        Text(text = "Try Again")
                    }
                }
            }
        } else if (isLoading && topCoins.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Loading top 10 market rankings...", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(topCoins) { coin ->
                    CryptoRankCard(coin = coin)
                }
            }
        }
    }
}

/**
 * Card composable representing a single cryptocurrency in the market ranking list.
 *
 * Displays a discreet rank number (#1-#10), remote API image logo, coin name, symbol, spot price, and 24h change %.
 *
 * @param coin [CryptoCoin] model containing rank, symbol, name, price, 24h change %, and imageUrl.
 */
@Composable
fun CryptoRankCard(coin: CryptoCoin) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Discreet Rank Number + API Coin Logo Image + Name & Symbol
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Discreet Rank Number (#1, #2, ... #10)
                Text(
                    text = "#${coin.rank}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.width(30.dp)
                )

                // Coin Remote Logo Image via Coil
                if (!coin.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = coin.imageUrl,
                        contentDescription = coin.name,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                    )
                } else {
                    // Fallback Circle if image URL is unavailable
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = coin.symbol.take(1),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Symbol
                Column {
                    Text(
                        text = coin.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = coin.symbol,
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }

            // Right Side: Spot Price and 24-Hour Price Change Percentage
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrencyUsd(coin.currentPriceUsd),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                val isPositive = coin.priceChangePercentage24h >= 0
                val changeColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFE53935)
                val changeSign = if (isPositive) "+" else ""

                Text(
                    text = "$changeSign${String.format(Locale.US, "%.2f", coin.priceChangePercentage24h)}%",
                    color = changeColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * Formats a raw numeric double value into US Dollar ($) currency format.
 */
private fun formatCurrencyUsd(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(value)
}

/**
 * Returns the current device local time formatted as "HH:mm:ss".
 */
private fun getCurrentTimeFormatted(): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(Date())
}
