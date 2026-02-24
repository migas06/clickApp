package com.example.viralclicker.ui.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.viralclicker.R
import com.example.viralclicker.domain.catalog.MilestoneCatalog
import com.example.viralclicker.domain.model.MilestoneState
import com.example.viralclicker.domain.model.MilestoneTrigger
import com.example.viralclicker.domain.model.ViralPoints
import com.example.viralclicker.ui.theme.*
import kotlinx.coroutines.delay
import java.math.BigDecimal

// ═══════════════════════════════════════════════
//  ACHIEVEMENT VAULT — Cyberpunk Trophy Room
// ═══════════════════════════════════════════════

// ── Trigger type → neon color mapping ──

private fun triggerColor(trigger: MilestoneTrigger): Color = when (trigger) {
    MilestoneTrigger.ALL_TIME_POINTS -> NeonCyan
    MilestoneTrigger.PRESTIGE_COUNT -> NeonPurple
    MilestoneTrigger.TOTAL_UPGRADES_OWNED -> NeonPink
}


/** Extract emoji from milestone name like "Going Viral 🎉" → ("Going Viral", "🎉") */
private fun extractEmoji(name: String): Pair<String, String> {
    val lastSpace = name.lastIndexOf(' ')
    if (lastSpace >= 0) {
        val potential = name.substring(lastSpace + 1)
        if (potential.isNotEmpty() && !potential[0].isLetterOrDigit()) {
            return name.substring(0, lastSpace) to potential
        }
    }
    return name to "🏅"
}

// ── Main Panel ──

@Composable
fun MilestonePanel(
    milestones: List<MilestoneState>,
    allTimePoints: ViralPoints,
    prestigeCount: Int,
    totalUpgradesOwned: Int,
    modifier: Modifier = Modifier
) {
    val unlockedCount = milestones.count { it.unlocked }
    val totalCount = milestones.size
    val completionFraction = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f

    // Look up catalog defs for trigger types + thresholds
    val catalogMap = remember { MilestoneCatalog.all.associateBy { it.id } }

    // Group milestones by trigger type (ordered)
    val grouped = remember(milestones) {
        val triggerOrder = listOf(
            MilestoneTrigger.ALL_TIME_POINTS,
            MilestoneTrigger.PRESTIGE_COUNT,
            MilestoneTrigger.TOTAL_UPGRADES_OWNED
        )
        triggerOrder.mapNotNull { trigger ->
            val items = milestones.filter { ms -> catalogMap[ms.id]?.triggerType == trigger }
            if (items.isNotEmpty()) trigger to items else null
        }
    }

    // Staggered entry visibility
    val visibleItems = remember { mutableStateMapOf<Int, Boolean>() }
    val totalItems = 1 + grouped.sumOf { 1 + it.second.size }
    LaunchedEffect(totalItems) {
        for (i in 0 until totalItems) {
            visibleItems[i] = true
            delay(60L)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Trophy Vault Header ──
        item {
            AnimatedVisibility(
                visible = visibleItems[0] == true,
                enter = fadeIn(tween(500)) + slideInVertically(tween(400)) { -60 }
            ) {
                TrophyVaultHeader(
                    unlocked = unlockedCount,
                    total = totalCount,
                    fraction = completionFraction
                )
            }
        }

        // ── Grouped Sections ──
        var globalIndex = 1

        grouped.forEach { (trigger, items) ->
            val sectionIndex = globalIndex
            val color = triggerColor(trigger)

            // Section header — label resolved inside composable scope
            item {
                AnimatedVisibility(
                    visible = visibleItems[sectionIndex] == true,
                    enter = fadeIn(tween(300, delayMillis = sectionIndex * 50)) +
                            slideInVertically(tween(300, delayMillis = sectionIndex * 50)) { -40 }
                ) {
                    val emoji = when (trigger) {
                        MilestoneTrigger.ALL_TIME_POINTS -> "🏆"
                        MilestoneTrigger.PRESTIGE_COUNT -> "🔥"
                        MilestoneTrigger.TOTAL_UPGRADES_OWNED -> "🛍️"
                    }
                    val label = when (trigger) {
                        MilestoneTrigger.ALL_TIME_POINTS -> stringResource(R.string.trigger_viral_points)
                        MilestoneTrigger.PRESTIGE_COUNT -> stringResource(R.string.trigger_prestige)
                        MilestoneTrigger.TOTAL_UPGRADES_OWNED -> stringResource(R.string.trigger_upgrades)
                    }
                    Column {
                        Spacer(Modifier.height(4.dp))
                        NeonSectionHeader(title = label, emoji = emoji, accentColor = color)
                    }
                }
            }
            globalIndex++

            // Milestone cards
            itemsIndexed(items) { index, ms ->
                val itemIndex = sectionIndex + 1 + index
                AnimatedVisibility(
                    visible = visibleItems[itemIndex] == true,
                    enter = fadeIn(tween(300, delayMillis = itemIndex * 50)) +
                            slideInVertically(tween(300, delayMillis = itemIndex * 50)) { 60 }
                ) {
                    val def = catalogMap[ms.id]
                    val progress = if (!ms.unlocked && def != null) {
                        computeProgress(
                            def.triggerType, def.threshold,
                            allTimePoints, prestigeCount, totalUpgradesOwned
                        )
                    } else 0f

                    NeonMilestoneCard(
                        ms = ms,
                        triggerType = def?.triggerType ?: MilestoneTrigger.ALL_TIME_POINTS,
                        progress = progress
                    )
                }
            }
            globalIndex += items.size
        }

        // Bottom spacer
        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ── Progress computation for locked milestones ──

private fun computeProgress(
    trigger: MilestoneTrigger,
    threshold: Long,
    allTimePoints: ViralPoints,
    prestigeCount: Int,
    totalUpgradesOwned: Int
): Float {
    if (threshold <= 0) return 0f
    val current = when (trigger) {
        MilestoneTrigger.ALL_TIME_POINTS ->
            allTimePoints.value.toFloat() / BigDecimal(threshold).toFloat()
        MilestoneTrigger.PRESTIGE_COUNT ->
            prestigeCount.toFloat() / threshold.toFloat()
        MilestoneTrigger.TOTAL_UPGRADES_OWNED ->
            totalUpgradesOwned.toFloat() / threshold.toFloat()
    }
    return current.coerceIn(0f, 0.99f)
}

// ═══════════════════════════════════════════════
//  TROPHY VAULT HEADER — Animated arc + count
// ═══════════════════════════════════════════════

@Composable
private fun TrophyVaultHeader(unlocked: Int, total: Int, fraction: Float) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "arc_sweep"
    )

    // Pulsing glow on the arc
    val infiniteTransition = rememberInfiniteTransition(label = "header_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "header_glow_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Arc ring with count
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(130.dp)
        ) {
            Canvas(modifier = Modifier.size(130.dp)) {
                val strokeWidth = 8f
                val arcSize = Size(size.width - strokeWidth * 2, size.height - strokeWidth * 2)
                val topLeft = Offset(strokeWidth, strokeWidth)

                // Background track (dim)
                drawArc(
                    color = OnDarkSecondary.copy(alpha = 0.12f),
                    startAngle = -225f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Outer glow of the filled arc
                if (animatedFraction > 0f) {
                    drawArc(
                        color = GoldAccent.copy(alpha = 0.12f * glowPulse),
                        startAngle = -225f,
                        sweepAngle = 270f * animatedFraction,
                        useCenter = false,
                        topLeft = Offset(topLeft.x - 4, topLeft.y - 4),
                        size = Size(arcSize.width + 8, arcSize.height + 8),
                        style = Stroke(width = strokeWidth + 10, cap = StrokeCap.Round)
                    )
                }

                // Filled arc (gold)
                drawArc(
                    color = GoldAccent.copy(alpha = glowPulse),
                    startAngle = -225f,
                    sweepAngle = 270f * animatedFraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Count text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$unlocked",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldAccent
                )
                Text(
                    "/ $total",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnDarkSecondary
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Title
        Text(
            stringResource(R.string.vault_title),
            letterSpacing = 4.sp,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = OnDarkSecondary.copy(alpha = 0.6f)
        )

        // Percentage
        Text(
            stringResource(R.string.vault_complete, (fraction * 100).toInt()),
            fontSize = 12.sp,
            color = GoldAccent.copy(alpha = 0.6f)
        )
    }
}

// ═══════════════════════════════════════════════
//  SECTION HEADER — Neon dividers
// ═══════════════════════════════════════════════

@Composable
private fun NeonSectionHeader(title: String, emoji: String, accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = accentColor.copy(alpha = 0.4f)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "$emoji $title",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            color = accentColor
        )
        Spacer(Modifier.width(12.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = accentColor.copy(alpha = 0.4f)
        )
    }
}

// ═══════════════════════════════════════════════
//  NEON MILESTONE CARD
// ═══════════════════════════════════════════════

@Composable
private fun NeonMilestoneCard(
    ms: MilestoneState,
    triggerType: MilestoneTrigger,
    progress: Float
) {
    val color = triggerColor(triggerType)
    val isUnlocked = ms.unlocked
    val (cleanName, emoji) = extractEmoji(ms.name)

    // Pulsing glow for unlocked milestones
    val glowAlpha = if (isUnlocked) {
        val inf = rememberInfiniteTransition(label = "ms_glow_${ms.id}")
        val alpha by inf.animateFloat(
            initialValue = 0.06f,
            targetValue = 0.20f,
            animationSpec = infiniteRepeatable(
                tween(1800, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ),
            label = "ms_glow_alpha_${ms.id}"
        )
        alpha
    } else 0f

    val contentAlpha = if (isUnlocked) 1f else 0.45f

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) DarkSurfaceVariant else DarkSurface
        ),
        border = BorderStroke(
            width = if (isUnlocked) 1.5.dp else 1.dp,
            color = if (isUnlocked) color.copy(alpha = 0.6f) else color.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (glowAlpha > 0f) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(color.copy(alpha = glowAlpha), Color.Transparent),
                            center = Offset(size.width * 0.15f, size.height / 2),
                            radius = size.maxDimension * 0.5f
                        )
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .graphicsLayer(alpha = contentAlpha),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji with radial glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(48.dp)
            ) {
                if (isUnlocked) {
                    Canvas(modifier = Modifier.size(48.dp)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(color.copy(alpha = 0.3f), Color.Transparent),
                                radius = 60f
                            )
                        )
                    }
                }
                Text(
                    text = if (isUnlocked) emoji else "🔒",
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                    modifier = if (!isUnlocked)
                        Modifier.graphicsLayer(alpha = 0.5f)
                    else Modifier
                )
            }

            Spacer(Modifier.width(12.dp))

            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        cleanName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) color else OnDarkSecondary
                    )
                    if (isUnlocked) {
                        Spacer(Modifier.width(8.dp))
                        UnlockedBadge(color = color)
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    ms.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnDarkSecondary.copy(alpha = if (isUnlocked) 0.8f else 0.5f)
                )

                // Progress bar for locked milestones
                if (!isUnlocked && progress > 0f) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = color.copy(alpha = 0.6f),
                            trackColor = color.copy(alpha = 0.08f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${(progress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = color.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
//  UNLOCKED BADGE
// ═══════════════════════════════════════════════

@Composable
private fun UnlockedBadge(color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            stringResource(R.string.unlocked_badge),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = color
        )
    }
}
