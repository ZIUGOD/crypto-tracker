package com.example.crypto_tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto_tracker.config.AppConfig
import com.example.crypto_tracker.ui.components.glass

/**
 * Glass navigation bar listing every [AppDestinations] entry. Sizes and outer spacing come
 * from [AppConfig.Navigation].
 */
@Composable
fun AppNavigationBar(
    current: AppDestinations,
    onSelect: (AppDestinations) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(
                horizontal = AppConfig.Navigation.NAV_BAR_OUTER_PADDING_HORIZONTAL_DP.dp,
                vertical = AppConfig.Navigation.NAV_BAR_OUTER_PADDING_VERTICAL_DP.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppConfig.Navigation.NAV_BAR_HEIGHT_DP.dp)
                .glass(shape = RoundedCornerShape(36.dp), strong = true)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppDestinations.entries.forEach { destination ->
                NavigationItem(
                    destination = destination,
                    isSelected = destination == current,
                    onClick = { onSelect(destination) }
                )
            }
        }
    }
}

@Composable
private fun NavigationItem(
    destination: AppDestinations,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .then(
                if (isSelected) {
                    Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                            RoundedCornerShape(28.dp)
                        )
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(destination.icon),
            contentDescription = destination.label,
            tint = contentColor,
            modifier = Modifier.size(AppConfig.Navigation.NAV_BAR_ICON_SIZE_DP.dp)
        )
        if (AppConfig.Navigation.SHOW_NAVIGATION_LABELS) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = destination.label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}
