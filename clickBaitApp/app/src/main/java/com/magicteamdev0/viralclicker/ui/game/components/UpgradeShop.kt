package com.magicteamdev0.viralclicker.ui.game.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magicteamdev0.viralclicker.R
import com.magicteamdev0.viralclicker.domain.catalog.UpgradeCatalog
import com.magicteamdev0.viralclicker.domain.model.UpgradeCategory
import com.magicteamdev0.viralclicker.domain.model.ViralPoints
import com.magicteamdev0.viralclicker.ui.game.PurchaseEvent
import com.magicteamdev0.viralclicker.ui.theme.*
import com.magicteamdev0.viralclicker.ads.AdManager
import com.magicteamdev0.viralclicker.ads.RewardedAdState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.max

// ═══════════════════════════════════════════════
//  UPGRADE SHOP — Neon Marketplace
// ═══════════════════════════════════════════════

@Composable
fun UpgradeShop(
    viralPoints: ViralPoints,
    allTimePoints: ViralPoints,
    ownedUpgrades: Map<String, Int>,
    onPurchase: (String) -> Unit,
    purchaseEvents: SharedFlow<PurchaseEvent> = kotlinx.coroutines.flow.MutableSharedFlow(),
    rewardedAdState: RewardedAdState = RewardedAdState.NotAvailable,
    onWatchAd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(UpgradeCategory.CLICKER) }
    var lastPurchasedId by remember { mutableStateOf<String?>(null) }

    // Collect purchase events for feedback
    LaunchedEffect(Unit) {
        purchaseEvents.collectLatest { event ->
            lastPurchasedId = event.upgradeId
            kotlinx.coroutines.delay(600)
            lastPurchasedId = null
        }
    }

    val available = UpgradeCatalog.all.filter {
        allTimePoints.isGreaterThanOrEqual(it.unlockAtAllTimePoints)
    }
    val filtered = available.filter { it.category == selectedCategory }

    Column(modifier = modifier.fillMaxSize()) {

        // ── Shop Header with Balance ──
        ShopHeader(viralPoints = viralPoints)

        // ── Category Toggle ──
        CategoryToggle(
            selected = selectedCategory,
            onSelect = { selectedCategory = it },
            clickerCount = available.count { it.category == UpgradeCategory.CLICKER },
            generatorCount = available.count { it.category == UpgradeCategory.GENERATOR }
        )

        // ── Upgrade List ──
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Rewarded Ad Card ──
            item(key = "rewarded_ad") {
                RewardedAdCard(
                    state = rewardedAdState,
                    onWatchAd = onWatchAd
                )
            }
            items(filtered, key = { it.id }) { def ->
                val owned = ownedUpgrades[def.id] ?: 0
                val cost = def.costForCount(owned)
                val canAfford = viralPoints.isGreaterThanOrEqual(cost)
                val justPurchased = lastPurchasedId == def.id
                val affordProgress = if (!canAfford && cost.value.signum() > 0) {
                    (viralPoints.value.toFloat() / cost.value.toFloat()).coerceIn(0f, 0.99f)
                } else 0f

                NeonUpgradeCard(
                    emoji = def.emoji,
                    name = def.name,
                    description = def.description,
                    cost = cost.toDisplayString(),
                    owned = owned,
                    canAfford = canAfford,
                    justPurchased = justPurchased,
                    affordProgress = affordProgress,
                    accentColor = if (def.category == UpgradeCategory.CLICKER) NeonPurple else NeonCyan,
                    onBuy = { onPurchase(def.id) }
                )
            }

            // Empty state
            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔒", fontSize = 40.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.upgrade_keep_earning),
                                color = OnDarkSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Rewarded Ad Card — Boost 2× opt-in
// ─────────────────────────────────────────────

@Composable
fun RewardedAdCard(
    state: RewardedAdState,
    onWatchAd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isReady = state is RewardedAdState.Ready
    val isLoading = state is RewardedAdState.Loading
    val cooldownMs = (state as? RewardedAdState.Cooldown)?.remainingMs ?: 0L
    val cooldownSec = (cooldownMs / 1000).toInt()

    val glowAlpha by animateFloatAsState(
        targetValue = if (isReady) 0.25f else 0f,
        animationSpec = tween(600), label = "ad_glow"
    )
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f, targetValue = if (isReady) 1.02f else 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse_scale"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
            .then(
                if (isReady) Modifier.border(
                    1.dp,
                    Brush.linearGradient(listOf(GoldAccent.copy(alpha = 0.5f), NeonPink.copy(alpha = 0.3f))),
                    RoundedCornerShape(16.dp)
                ) else Modifier.border(1.dp, OnDarkSecondary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isReady) Modifier.clickable { onWatchAd() } else Modifier)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isReady) Brush.radialGradient(
                            listOf(GoldAccent.copy(alpha = 0.2f + glowAlpha), NeonPink.copy(alpha = 0.05f))
                        ) else Brush.radialGradient(
                            listOf(OnDarkSecondary.copy(alpha = 0.08f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("📺", fontSize = 22.sp,
                    modifier = Modifier.graphicsLayer(alpha = if (isReady) 1f else 0.4f))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "2× Boost",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isReady) OnDark else OnDarkSecondary.copy(alpha = 0.5f)
                )
                Text(
                    when (state) {
                        is RewardedAdState.Ready        -> "Watch a short ad · 30 min boost"
                        is RewardedAdState.Loading      -> "Loading ad…"
                        is RewardedAdState.Cooldown     -> "Available in ${cooldownSec / 60}m ${cooldownSec % 60}s"
                        is RewardedAdState.NotAvailable -> "Ad not available"
                        is RewardedAdState.Showing      -> "Watching…"
                    },
                    fontSize = 12.sp,
                    color = OnDarkSecondary.copy(alpha = 0.45f)
                )
            }

            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = OnDarkSecondary.copy(alpha = 0.3f)
                )
                isReady -> Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.verticalGradient(listOf(GoldAccent, NeonPink.copy(alpha = 0.8f))))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("WATCH", fontSize = 11.sp, fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp, color = DarkBackground)
                }
                else -> Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(OnDarkSecondary.copy(alpha = 0.08f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("—", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = OnDarkSecondary.copy(alpha = 0.25f))
                }
            }
        }
    }
}
// ─────────────────────────────────────────────
//  Shop Header — Glowing balance display
// ─────────────────────────────────────────────

@Composable
private fun ShopHeader(viralPoints: ViralPoints) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Gradient underline glow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, NeonPurple.copy(alpha = 0.08f)),
                        startY = 0f,
                        endY = size.height
                    )
                )
            }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    stringResource(R.string.shop_header),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = OnDarkSecondary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(R.string.shop_subtitle),
                    fontSize = 12.sp,
                    color = OnDarkSecondary.copy(alpha = 0.5f)
                )
            }
            // Balance pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = NeonGreen.copy(alpha = 0.08f),
                modifier = Modifier.border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(NeonGreen.copy(alpha = 0.3f), NeonGreen.copy(alpha = 0.1f))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("💰", fontSize = 14.sp)
                    Text(
                        viralPoints.toDisplayString(),
                        color = NeonGreen,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Category Toggle — Segmented neon switch
// ─────────────────────────────────────────────

@Composable
private fun CategoryToggle(
    selected: UpgradeCategory,
    onSelect: (UpgradeCategory) -> Unit,
    clickerCount: Int,
    generatorCount: Int
) {
    val categories = listOf(
        Triple(UpgradeCategory.CLICKER, "👆 ${stringResource(R.string.category_clickers)}", clickerCount),
        Triple(UpgradeCategory.GENERATOR, "🤖 ${stringResource(R.string.category_generators)}", generatorCount)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        categories.forEach { (category, label, count) ->
            val isSelected = selected == category
            val accentColor = if (category == UpgradeCategory.CLICKER) NeonPurple else NeonCyan
            val bgAlpha by animateFloatAsState(
                targetValue = if (isSelected) 0.15f else 0f,
                animationSpec = tween(200), label = "cat_bg"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = bgAlpha))
                    .then(
                        if (isSelected) Modifier.border(
                            1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)
                        ) else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(category) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                        letterSpacing = 1.sp,
                        color = if (isSelected) accentColor else OnDarkSecondary.copy(alpha = 0.45f)
                    )
                    // Count badge
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) accentColor.copy(alpha = 0.2f)
                                else OnDarkSecondary.copy(alpha = 0.08f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$count",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) accentColor else OnDarkSecondary.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Neon Upgrade Card — Premium glass card
// ─────────────────────────────────────────────

@Composable
private fun NeonUpgradeCard(
    emoji: String,
    name: String,
    description: String,
    cost: String,
    owned: Int,
    canAfford: Boolean,
    justPurchased: Boolean,
    affordProgress: Float,
    accentColor: Color,
    onBuy: () -> Unit
) {
    // Purchase flash animation
    val purchaseScale by animateFloatAsState(
        targetValue = if (justPurchased) 1.03f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 600f),
        label = "purchase_scale"
    )
    val purchaseGlow by animateFloatAsState(
        targetValue = if (justPurchased) 0.4f else 0f,
        animationSpec = tween(300), label = "purchase_glow"
    )
    val layoutDirection = LocalLayoutDirection.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = purchaseScale, scaleY = purchaseScale)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = DarkSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (canAfford || justPurchased) Modifier.border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                accentColor.copy(alpha = 0.4f + purchaseGlow),
                                accentColor.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) else Modifier.border(
                        1.dp,
                        OnDarkSecondary.copy(alpha = 0.08f),
                        RoundedCornerShape(16.dp)
                    )
                )
                .drawBehind {
                    // Accent bar — switches side for RTL
                    val accentX = if (layoutDirection == LayoutDirection.Rtl) size.width - 4f else 0f
                    drawRoundRect(
                        color = if (canAfford) accentColor.copy(alpha = 0.6f)
                        else OnDarkSecondary.copy(alpha = 0.15f),
                        topLeft = Offset(accentX, 12f),
                        size = Size(4f, size.height - 24f),
                        cornerRadius = CornerRadius(2f)
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji icon with glow background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (canAfford) accentColor.copy(alpha = 0.1f)
                            else DarkSurface
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        emoji,
                        fontSize = 24.sp,
                        modifier = Modifier.graphicsLayer(
                            alpha = if (canAfford) 1f else 0.4f
                        )
                    )
                }

                Spacer(Modifier.width(14.dp))

                // Info column
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canAfford) OnDark else OnDarkSecondary.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (owned > 0) {
                            Spacer(Modifier.width(8.dp))
                            // Level badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = accentColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    "Lv.$owned",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = accentColor
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        description,
                        fontSize = 12.sp,
                        color = OnDarkSecondary.copy(alpha = 0.55f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )

                    // Progress bar for unaffordable upgrades (Zeigarnik effect)
                    if (!canAfford && affordProgress > 0f) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { affordProgress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.5.dp)),
                                color = accentColor.copy(alpha = 0.7f),
                                trackColor = accentColor.copy(alpha = 0.08f)
                            )
                            Text(
                                "${(affordProgress * 100).toInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(10.dp))

                // Buy button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (canAfford) Brush.verticalGradient(
                                listOf(accentColor, accentColor.copy(alpha = 0.7f))
                            ) else Brush.verticalGradient(
                                listOf(
                                    OnDarkSecondary.copy(alpha = 0.12f),
                                    OnDarkSecondary.copy(alpha = 0.06f)
                                )
                            )
                        )
                        .clickable(enabled = canAfford) { onBuy() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            cost,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (canAfford) DarkBackground else OnDarkSecondary.copy(alpha = 0.35f)
                        )
                        Text(
                            stringResource(R.string.upgrade_buy),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = if (canAfford) DarkBackground.copy(alpha = 0.7f) else OnDarkSecondary.copy(
                                alpha = 0.25f
                            )
                        )
                    }
                }
            }
        }
    }
}
