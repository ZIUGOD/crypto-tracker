package com.example.crypto_tracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Data model representing a single crypto news article headline item.
 *
 * @property title Headline title of the news article.
 * @property source Name of the publishing media source.
 * @property timeAgo Elapsed publication time string (e.g., "2h ago").
 * @property summary Brief summary paragraph of the news content.
 */
data class NewsItem(
    val title: String,
    val source: String,
    val timeAgo: String,
    val summary: String
)

/**
 * News Screen composable displaying recent market news headlines.
 *
 * @param modifier Layout modifier passed from parent scaffold padding.
 */
@Composable
fun NewsScreen(modifier: Modifier = Modifier) {

}

/**
 * Card composable representing an individual news article item.
 *
 * @param news [NewsItem] model containing article details.
 */
@Composable
fun NewsCard(news: NewsItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${news.source} • ${news.timeAgo}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = news.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = news.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }
    }
}
