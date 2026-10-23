package com.example.crypto_tracker.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.ui.components.GlassDefaults
import com.example.crypto_tracker.ui.components.ScreenHeader

/**
 * Home screen: top coins ranked by market cap, with expandable cards and per-coin notification pins.
 * Stateful entry point - collects [HomeViewModel] state and shows one-off events. All rendering
 * lives in the stateless [HomeContent].
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pinnedCoinIds by viewModel.pinnedCoinIds.collectAsStateWithLifecycle()
    val expandedCoinId by viewModel.expandedCoinId.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.PinLimitReached -> Toast.makeText(
                    context,
                    "Maximum of ${event.max} cryptocurrencies allowed for notifications",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    HomeContent(
        uiState = uiState,
        pinnedCoinIds = pinnedCoinIds,
        expandedCoinId = expandedCoinId,
        onToggleExpanded = viewModel::toggleExpanded,
        onTogglePinned = viewModel::togglePinned,
        onRetry = viewModel::refresh,
        modifier = modifier
    )
}

/** Stateless Home layout: header, status line and the state-specific body. */
@Composable
fun HomeContent(
    uiState: HomeUiState,
    pinnedCoinIds: Set<String>,
    expandedCoinId: String?,
    onToggleExpanded: (String) -> Unit,
    onTogglePinned: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(GlassDefaults.ScreenPadding)
    ) {
        ScreenHeader(
            title = "Top ${AppConfig.Market.TOP_COINS_COUNT} Cryptocurrencies",
            subtitle = "Pin up to ${AppConfig.Notifications.MAX_NOTIFIED_COINS} live prices to your notification bar"
        )

        StatusRow(uiState)

        Spacer(modifier = Modifier.height(12.dp))

        when (uiState) {
            is HomeUiState.Loading -> LoadingState()
            is HomeUiState.Error -> ErrorState(message = uiState.message, onRetry = onRetry)
            is HomeUiState.Success -> CoinList(
                state = uiState,
                pinnedCoinIds = pinnedCoinIds,
                expandedCoinId = expandedCoinId,
                onToggleExpanded = onToggleExpanded,
                onTogglePinned = onTogglePinned
            )
        }
    }
}

/** Loading indicator / refresh note on the left, last-updated timestamp on the right. */
@Composable
private fun StatusRow(uiState: HomeUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (uiState is HomeUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                StatusText("Updating rankings...")
            } else {
                StatusText("Auto-refreshes every 1 min")
            }
        }

        if (uiState is HomeUiState.Success) {
            StatusText(if (uiState.isStale) "Saved data from ${uiState.lastUpdated}" else "Updated: ${uiState.lastUpdated}")
        }
    }
}

@Composable
private fun StatusText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ColumnScope.LoadingState() {
    CenteredColumn {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Loading top ${AppConfig.Market.TOP_COINS_COUNT} market rankings...",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ColumnScope.ErrorState(message: String, onRetry: () -> Unit) {
    CenteredColumn {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text(text = "Try Again")
        }
    }
}

/** Fills the remaining height and centers its content. */
@Composable
private fun ColumnScope.CenteredColumn(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, content = content)
    }
}

@Composable
private fun CoinList(
    state: HomeUiState.Success,
    pinnedCoinIds: Set<String>,
    expandedCoinId: String?,
    onToggleExpanded: (String) -> Unit,
    onTogglePinned: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(GlassDefaults.ListSpacing)
    ) {
        items(items = state.coins, key = { it.id }) { coin ->
            CryptoRankCard(
                coin = coin,
                isNotified = coin.id in pinnedCoinIds,
                isExpanded = coin.id == expandedCoinId,
                onToggleExpand = { onToggleExpanded(coin.id) },
                onToggleNotification = { onTogglePinned(coin.id) }
            )
        }
    }
}
