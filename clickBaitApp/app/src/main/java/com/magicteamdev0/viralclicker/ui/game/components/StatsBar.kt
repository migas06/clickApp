package com.magicteamdev0.viralclicker.ui.game.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.magicteamdev0.viralclicker.R
import com.magicteamdev0.viralclicker.domain.model.NextMilestoneProgress
import com.magicteamdev0.viralclicker.domain.model.ViralPoints
import com.magicteamdev0.viralclicker.ui.theme.*

@Composable
fun StatsBar(
    viralPoints: ViralPoints,
    pointsPerSecond: ViralPoints,
    pointsPerClick: ViralPoints,
    prestigeMultiplier: Double,
    boostActiveUntil: Long?,
    nextMilestone: NextMilestoneProgress? = null,
    dailyStreak: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = viralPoints.toDisplayString(),
            style = MaterialTheme.typography.displayLarge,
            color = NeonGreen,
            fontWeight = FontWeight.Black
        )
        Text(
            text = stringResource(R.string.viral_points_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        val perSecLabel = stringResource(R.string.stats_per_sec)
        val perTapLabel = stringResource(R.string.stats_per_tap)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatChip(label = "${pointsPerSecond.toDisplayString()}$perSecLabel", color = NeonCyan)
            StatChip(label = "+${pointsPerClick.toDisplayString()}$perTapLabel", color = MaterialTheme.colorScheme.secondary)
            if (prestigeMultiplier > 1.0) {
                StatChip(label = "×${String.format("%.1f", prestigeMultiplier)}", color = GoldAccent)
            }
            if (dailyStreak > 0) {
                DailyStreakBadge(streak = dailyStreak)
            }
        }

        // Next milestone progress bar
        nextMilestone?.let { nm ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.stats_next, nm.name),
                color = GoldAccent,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.height(2.dp))
            val animatedProgress by animateFloatAsState(
                targetValue = nm.progress,
                animationSpec = tween(500),
                label = "milestone_progress"
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = GoldAccent,
                trackColor = GoldAccent.copy(alpha = 0.15f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(nm.currentValue, style = MaterialTheme.typography.labelSmall, color = OnDarkSecondary)
                Text(nm.targetValue, style = MaterialTheme.typography.labelSmall, color = OnDarkSecondary)
            }
        }
    }
}

@Composable
private fun StatChip(label: String, color: Color) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DailyStreakBadge(streak: Int) {
    if (streak <= 0) return
    val fires = when {
        streak >= 7 -> 3
        streak >= 3 -> 2
        else -> 1
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = GoldAccent.copy(alpha = 0.15f)
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                text = "\uD83D\uDD25".repeat(fires),
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.width(2.dp))
            Text(
                text = "$streak",
                color = GoldAccent,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
