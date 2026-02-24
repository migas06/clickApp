package com.magicteamdev0.viralclicker.ui.game.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magicteamdev0.viralclicker.R
import com.magicteamdev0.viralclicker.domain.model.NextMilestoneProgress
import com.magicteamdev0.viralclicker.domain.model.ViralPoints
import com.magicteamdev0.viralclicker.ui.theme.*

// ═══════════════════════════════════════════════
//  STATS BAR — Compact horizontal top bar
// ═══════════════════════════════════════════════

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
    val perSecLabel = stringResource(R.string.stats_per_sec)
    val perTapLabel = stringResource(R.string.stats_per_tap)
    val isBoostActive = boostActiveUntil != null && System.currentTimeMillis() < (boostActiveUntil)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // Bottom neon line
                drawLine(
                    color = NeonCyan.copy(alpha = 0.15f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1f
                )
                // Bottom glow spread
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, NeonCyan.copy(alpha = 0.06f)),
                        startY = size.height * 0.5f,
                        endY = size.height
                    )
                )
            }
            .background(DarkSurface.copy(alpha = 0.88f))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // ── Primary row: score pill + stats chips ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Score display with neon coin dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // Coin accent dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GoldAccent)
                )
                Text(
                    text = viralPoints.toDisplayString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = OnDark,
                    letterSpacing = (-0.5).sp
                )
                if (isBoostActive) {
                    Text(
                        text = "2×",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GoldAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // Stats chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactStatChip(
                    value = pointsPerSecond.toDisplayString(),
                    suffix = perSecLabel,
                    color = NeonCyan
                )
                CompactStatChip(
                    value = pointsPerClick.toDisplayString(),
                    suffix = perTapLabel,
                    color = NeonPurple
                )
                if (prestigeMultiplier > 1.0) {
                    CompactStatChip(
                        value = "×${String.format("%.1f", prestigeMultiplier)}",
                        suffix = "",
                        color = GoldAccent
                    )
                }
                if (dailyStreak > 0) {
                    DailyStreakBadge(streak = dailyStreak)
                }
            }
        }

        // ── Next milestone compact progress row ──
        nextMilestone?.let { nm ->
            Spacer(Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = nm.name,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = GoldAccent.copy(alpha = 0.75f),
                    modifier = Modifier.widthIn(max = 88.dp),
                    maxLines = 1
                )
                val animatedProgress by animateFloatAsState(
                    targetValue = nm.progress,
                    animationSpec = tween(500),
                    label = "milestone_progress"
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp)),
                    color = GoldAccent,
                    trackColor = GoldAccent.copy(alpha = 0.12f)
                )
                Text(
                    text = "${(nm.progress * 100).toInt()}%",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent.copy(alpha = 0.65f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Compact stat chip
// ─────────────────────────────────────────────

@Composable
private fun CompactStatChip(value: String, suffix: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
        if (suffix.isNotEmpty()) {
            Text(
                text = suffix,
                fontSize = 9.sp,
                color = color.copy(alpha = 0.60f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Daily streak badge (public — used by GameScreen)
// ─────────────────────────────────────────────

@Composable
fun DailyStreakBadge(streak: Int) {
    if (streak <= 0) return
    val fires = when {
        streak >= 7 -> 3
        streak >= 3 -> 2
        else -> 1
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(GoldAccent.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "\uD83D\uDD25".repeat(fires), fontSize = 9.sp)
        Spacer(Modifier.width(2.dp))
        Text(
            text = "$streak",
            fontSize = 9.sp,
            color = GoldAccent,
            fontWeight = FontWeight.Black
        )
    }
}
