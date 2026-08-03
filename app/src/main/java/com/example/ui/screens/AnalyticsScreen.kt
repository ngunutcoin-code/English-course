package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.theme.TealAccent
import com.example.viewmodel.StimulerViewModel

@Composable
fun AnalyticsScreen(
    viewModel: StimulerViewModel,
    modifier: Modifier = Modifier
) {
    val userStats by viewModel.userStats.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()

    val totalSpeakingMins = ((userStats?.totalSpeakingSeconds ?: 0) / 60).toInt()
    val totalSessions = userStats?.totalSessionsCount ?: 0
    val avgScore = userStats?.averageScore?.toInt() ?: 0
    val streakDays = userStats?.streakDays ?: 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("analytics_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Speaking Progress & Stats",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Track your fluency growth and speech metrics over time.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Key Stat Badges ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                title = "Total Time",
                value = "$totalSpeakingMins mins",
                icon = Icons.Default.Timer,
                color = PurplePrimary,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                title = "Sessions",
                value = "$totalSessions completed",
                icon = Icons.Default.Mic,
                color = TealAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox(
                title = "Avg Score",
                value = "$avgScore PTS",
                icon = Icons.Default.Star,
                color = EmeraldSuccess,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                title = "Streak",
                value = "$streakDays Days",
                icon = Icons.Default.LocalFireDepartment,
                color = AmberAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Fluency Progression Breakdown ---
        Text(
            text = "Average Metric Progression",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val avgFluency = if (allSessions.isEmpty()) 78 else allSessions.map { it.fluencyScore }.average().toInt()
                val avgGrammar = if (allSessions.isEmpty()) 72 else allSessions.map { it.grammarScore }.average().toInt()
                val avgVocab = if (allSessions.isEmpty()) 80 else allSessions.map { it.vocabularyScore }.average().toInt()
                val avgPronunciation = if (allSessions.isEmpty()) 75 else allSessions.map { it.pronunciationScore }.average().toInt()

                MetricBar(title = "Fluency & Smoothness", score = avgFluency, color = PurplePrimary)
                MetricBar(title = "Grammar Precision", score = avgGrammar, color = TealAccent)
                MetricBar(title = "Vocabulary Richness", score = avgVocab, color = EmeraldSuccess)
                MetricBar(title = "Pronunciation & Enunciation", score = avgPronunciation, color = AmberAccent)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Daily Speaking Goal Card ---
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDarkVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Daily Practice Goal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                val targetMins = 10
                val progress = (totalSpeakingMins.toFloat() / targetMins).coerceIn(0f, 1f)
                Text(
                    text = "$totalSpeakingMins of $targetMins mins practice completed today",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PurplePrimary,
                    trackColor = SurfaceDark
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MetricBar(title: String, score: Int, color: androidx.compose.ui.graphics.Color) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$score%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = SurfaceDarkVariant
        )
    }
}
