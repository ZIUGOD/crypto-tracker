package com.example.crypto_tracker.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.crypto_tracker.data.model.CryptoCoin
import com.example.crypto_tracker.data.repository.CryptoRepository
import com.example.crypto_tracker.notification.CryptoNotificationManager
import com.example.crypto_tracker.notification.NotificationPreferences
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
 * Allows the user to toggle which coins they want tracked in the persistent status bar notification.
 *
 * @param modifier Layout modifier passed from parent scaffold padding.
 */
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember { CryptoRepository() }
    val notificationPrefs = remember { NotificationPreferences(context) }
    val notificationManager = remember { CryptoNotificationManager(context) }

    // State holders for UI data
    var topCoins by remember { mutableStateOf<List<CryptoCoin>>(emptyList()) }
    var selectedNotifIds by remember { mutableStateOf(notificationPrefs.getSelectedCoinIds()) }
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
                // Update persistent notification with current prices of selected coins
                notificationManager.showPortfolioNotification(coins, selectedNotifIds)
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
            text = "Select 🔔 to pin live prices to your notification bar",
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
                    val isNotified = selectedNotifIds.contains(coin.id)
                    CryptoRankCard(
                        coin = coin,
                        isNotified = isNotified,
                        onToggleNotification = {
                            selectedNotifIds = if (isNotified) {
                                selectedNotifIds - coin.id
                            } else {
                                selectedNotifIds + coin.id
                            }
                            notificationPrefs.setSelectedCoinIds(selectedNotifIds)
                            // Update notification immediately when user toggles selection
                            if (topCoins.isNotEmpty()) {
                                notificationManager.showPortfolioNotification(topCoins, selectedNotifIds)
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Card composable representing a single cryptocurrency in the market ranking list.
 *
 * Displays minimalist podium badge for Top 3 (Gold, Silver, Bronze), remote API image logo, name, symbol, spot price, 24h change %, and a notification bell toggle.
 *
 * @param coin [CryptoCoin] model.
 * @param isNotified Whether this coin is currently pinned to the persistent notification.
 * @param onToggleNotification Callback invoked when the bell icon is clicked.
 */
@Composable
fun CryptoRankCard(
    coin: CryptoCoin,
    isNotified: Boolean,
    onToggleNotification: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
            // Left Side: Minimalist Podium Badge (Top 3) or Discreet Rank + API Coin Logo Image + Name & Symbol
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Minimalist podium styling for Top 3 (Gold #1, Silver #2, Bronze #3)
                val podiumColors = when (coin.rank) {
                    1 -> Pair(Color(0xFFFFD700), Color(0xFF3E2723)) // Gold (Ouro)
                    2 -> Pair(Color(0xFFCFD8DC), Color(0xFF263238)) // Silver (Prata)
                    3 -> Pair(Color(0xFFD77A31), Color(0xFFFFFFFF)) // Bronze (Bronze)
                    else -> null
                }

                if (podiumColors != null) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(podiumColors.first, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = coin.rank.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = podiumColors.second
                        )
                    }
                } else {
                    // Discreet rank number for ranks 4 to 10
                    Text(
                        text = "#${coin.rank}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.width(26.dp)
                    )
                }

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

            // Right Side: Spot Price (with animation), 24h Change, and Polished Notification Bell Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    // AnimatedContent creates a smooth fade transition effect when price updates
                    AnimatedContent(
                        targetState = coin.currentPriceUsd,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "priceAnimation"
                    ) { price ->
                        Text(
                            text = formatCurrencyUsd(price),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

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

                // Polished Notification Bell Toggle Button
                IconButton(
                    onClick = onToggleNotification,
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = if (isNotified) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = if (isNotified) "🔔" else "🔕",
                        fontSize = 18.sp
                    )
                }
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
