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
import com.magicteamdev0.viralclicker.ads.RewardedAdState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

// ═══════════════════════════════════════════════
//  UPGRADE SHOP — Cyberpunk Marketplace
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

        // ── Shop Header ──
        CyberShopHeader(viralPoints = viralPoints)

        // ── Resource bars ──
        ResourceBarsSection(
            viralPoints = viralPoints,
            allTimePoints = allTimePoints,
            totalOwnedUpgrades = ownedUpgrades.values.sum(),
            totalCatalogSize = UpgradeCatalog.all.size
        )

        // ── Category Toggle ──
        CyberCategoryToggle(
            selected = selectedCategory,
            onSelect = { selectedCategory = it },
            clickerCount = available.count { it.category == UpgradeCategory.CLICKER },
            generatorCount = available.count { it.category == UpgradeCategory.GENERATOR }
        )

        // ── Upgrade list ──
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Rewarded Ad row
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

                val iconType = when {
                    def.emoji.contains("👆") -> NavIconType.TAP
                    def.emoji.contains("👍") -> NavIconType.LIKE
                    def.emoji.contains("🧤") -> NavIconType.GLOVE
                    def.emoji.contains("🧠") -> NavIconType.BRAIN
                    def.emoji.contains("⚛️") -> NavIconType.ATOM
                    def.emoji.contains("🤖") -> NavIconType.BOT
                    def.emoji.contains("🌟") -> NavIconType.STAR
                    def.emoji.contains("📊") -> NavIconType.CHART
                    def.emoji.contains("🏭") -> NavIconType.FACTORY
                    def.emoji.contains("🌌") -> NavIconType.ORBIT
                    else -> NavIconType.SHOP
                }

                NeonUpgradeRow(
                    iconType = iconType,
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
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CyberpunkNavIcon(
                                type = NavIconType.LOCK,
                                isSelected = true,
                                color = OnDarkSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                stringResource(R.string.upgrade_keep_earning),
                                color = OnDarkSecondary,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Cyber Shop Header
// ─────────────────────────────────────────────

@Composable
private fun CyberShopHeader(viralPoints: ViralPoints) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface.copy(alpha = 0.9f))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Title
        Text(
            text = stringResource(R.string.shop_header),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            color = OnDark
        )

        // Balance pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(NeonGreen.copy(alpha = 0.08f))
                .border(1.dp, NeonGreen.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            CyberpunkNavIcon(
                type = NavIconType.COIN,
                isSelected = true,
                color = NeonGreen,
                modifier = Modifier.size(16.dp)
            )
            Text(
                viralPoints.toDisplayString(),
                color = NeonGreen,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }
    }
}

// ─────────────────────────────────────────────
//  Resource Bars Section
// ─────────────────────────────────────────────

@Composable
private fun ResourceBarsSection(
    viralPoints: ViralPoints,
    allTimePoints: ViralPoints,
    totalOwnedUpgrades: Int,
    totalCatalogSize: Int
) {
    // Calculate progress values (logarithmic scale for clicker game numbers)
    val creditsDouble = max(1.0, viralPoints.value.toDouble())
    val allTimeDouble = max(1.0, allTimePoints.value.toDouble())

    val cyberCreditsProgress by animateFloatAsState(
        targetValue = min(1f, (log10(creditsDouble) / 9.0).toFloat()),  // full at 1B
        animationSpec = tween(800),
        label = "credits_progress"
    )
    val nanotechProgress by animateFloatAsState(
        targetValue = if (totalCatalogSize > 0)
            min(1f, totalOwnedUpgrades / (totalCatalogSize * 3f))  // avg 3 per upgrade
        else 0f,
        animationSpec = tween(800),
        label = "nanotech_progress"
    )
    val energyProgress by animateFloatAsState(
        targetValue = min(1f, (log10(allTimeDouble) / 12.0).toFloat()),  // full at 1T
        animationSpec = tween(800),
        label = "energy_progress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        ResourceBar(
            iconType = NavIconType.GEM,
            label = "CYBER-CREDITS",
            value = viralPoints.toDisplayString(),
            progress = cyberCreditsProgress,
            color = NeonCyan
        )
        ResourceBar(
            iconType = NavIconType.GEAR,
            label = "NANOTECH",
            value = "$totalOwnedUpgrades",
            progress = nanotechProgress,
            color = NeonGreen
        )
        ResourceBar(
            iconType = NavIconType.LIGHTNING,
            label = "ENERGY CELL",
            value = allTimePoints.toDisplayString(),
            progress = energyProgress,
            color = NeonPurple
        )
    }
}

@Composable
private fun ResourceBar(
    iconType: NavIconType,
    label: String,
    value: String,
    progress: Float,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.30f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CyberpunkNavIcon(
                type = iconType,
                isSelected = true,
                color = color,
                modifier = Modifier.size(14.dp)
            )
        }

        // Label
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            color = color.copy(alpha = 0.85f),
            modifier = Modifier.width(88.dp)
        )

        // Progress bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(2.5.dp))
                .background(color.copy(alpha = 0.10f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(color, color.copy(alpha = 0.70f))
                        )
                    )
            )
        }

        // Value
        Text(
            text = value,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = OnDarkSecondary,
            modifier = Modifier.widthIn(min = 30.dp),
            maxLines = 1
        )
    }
}

// ─────────────────────────────────────────────
//  Category Toggle — Cyberpunk segmented control
// ─────────────────────────────────────────────

@Composable
private fun CyberCategoryToggle(
    selected: UpgradeCategory,
    onSelect: (UpgradeCategory) -> Unit,
    clickerCount: Int,
    generatorCount: Int
) {
    val categories = listOf(
        Triple(UpgradeCategory.CLICKER, stringResource(R.string.category_clickers), clickerCount),
        Triple(UpgradeCategory.GENERATOR, stringResource(R.string.category_generators), generatorCount)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        categories.forEach { (category, label, count) ->
            val isSelected = selected == category
            val accentColor = if (category == UpgradeCategory.CLICKER) NeonPurple else NeonCyan
            val bgAlpha by animateFloatAsState(
                targetValue = if (isSelected) 0.18f else 0f,
                animationSpec = tween(180),
                label = "cat_bg_$category"
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = bgAlpha))
                    .then(
                        if (isSelected) Modifier.border(
                            1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp)
                        ) else Modifier
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(category) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    label.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                    letterSpacing = 1.5.sp,
                    color = if (isSelected) accentColor else OnDarkSecondary.copy(alpha = 0.40f)
                )
                Spacer(Modifier.width(6.dp))
                // Count badge
                Text(
                    "$count",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isSelected) accentColor.copy(alpha = 0.20f)
                            else OnDarkSecondary.copy(alpha = 0.08f)
                        )
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                    color = if (isSelected) accentColor else OnDarkSecondary.copy(alpha = 0.35f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Neon Upgrade Row — Full-width list item
// ─────────────────────────────────────────────

@Composable
private fun NeonUpgradeRow(
    iconType: NavIconType,
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
    val purchaseScale by animateFloatAsState(
        targetValue = if (justPurchased) 1.025f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 600f),
        label = "purchase_scale"
    )
    val purchaseGlow by animateFloatAsState(
        targetValue = if (justPurchased) 0.50f else 0f,
        animationSpec = tween(300),
        label = "purchase_glow"
    )
    val layoutDirection = LocalLayoutDirection.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = purchaseScale, scaleY = purchaseScale)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceVariant)
                .then(
                    if (canAfford || justPurchased) Modifier.border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                accentColor.copy(alpha = 0.45f + purchaseGlow),
                                accentColor.copy(alpha = 0.12f)
                            )
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) else Modifier.border(
                        1.dp,
                        OnDarkSecondary.copy(alpha = 0.07f),
                        RoundedCornerShape(10.dp)
                    )
                )
                .drawBehind {
                    // Left accent bar (RTL: right)
                    val accentX = if (layoutDirection == LayoutDirection.Rtl) size.width - 3.5f else 0f
                    drawRoundRect(
                        color = if (canAfford) accentColor.copy(alpha = 0.65f)
                        else OnDarkSecondary.copy(alpha = 0.12f),
                        topLeft = Offset(accentX, 10f),
                        size = Size(3.5f, size.height - 20f),
                        cornerRadius = CornerRadius(2f)
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 10.dp, top = 11.dp, bottom = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (canAfford) accentColor.copy(alpha = 0.12f)
                            else DarkSurface
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CyberpunkNavIcon(
                        type = iconType,
                        isSelected = canAfford,
                        color = if (canAfford) accentColor else OnDarkSecondary.copy(alpha = 0.38f),
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Info column
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            name.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = if (canAfford) OnDark else OnDarkSecondary.copy(alpha = 0.55f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (owned > 0) {
                            Spacer(Modifier.width(7.dp))
                            Text(
                                "LV.$owned",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(accentColor.copy(alpha = 0.14f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        description,
                        fontSize = 11.sp,
                        color = OnDarkSecondary.copy(alpha = 0.50f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(10.dp))

                // Buy button (fixed width, no arrow)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 75.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (canAfford) Brush.verticalGradient(
                                listOf(accentColor, accentColor.copy(alpha = 0.75f))
                            ) else Brush.verticalGradient(
                                listOf(
                                    OnDarkSecondary.copy(alpha = 0.10f),
                                    OnDarkSecondary.copy(alpha = 0.06f)
                                )
                            )
                        )
                        .clickable(enabled = canAfford) { onBuy() }
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Text(
                        cost,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (canAfford) DarkBackground else OnDarkSecondary.copy(alpha = 0.30f)
                    )
                }
            }

            // Affordability progress bar at bottom (Zeigarnik effect)
            if (!canAfford && affordProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(accentColor.copy(alpha = 0.07f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(affordProgress)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(accentColor.copy(alpha = 0.5f), accentColor.copy(alpha = 0.30f))
                                )
                            )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Rewarded Ad Card — 2× Boost opt-in
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
        initialValue = 1f, targetValue = if (isReady) 1.015f else 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse_scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .then(
                if (isReady) Modifier.border(
                    1.dp,
                    Brush.linearGradient(listOf(GoldAccent.copy(alpha = 0.45f + glowAlpha * 0.3f), NeonPink.copy(alpha = 0.25f))),
                    RoundedCornerShape(10.dp)
                ) else Modifier.border(1.dp, OnDarkSecondary.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            )
            .then(if (isReady) Modifier.clickable { onWatchAd() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isReady) Brush.radialGradient(
                        listOf(GoldAccent.copy(alpha = 0.18f + glowAlpha), NeonPink.copy(alpha = 0.05f))
                    ) else Brush.radialGradient(
                        listOf(OnDarkSecondary.copy(alpha = 0.07f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            CyberpunkNavIcon(
                type = NavIconType.TV,
                isSelected = isReady,
                color = if (isReady) GoldAccent else OnDarkSecondary.copy(alpha = 0.38f),
                modifier = Modifier.size(24.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "2× BOOST",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                color = if (isReady) OnDark else OnDarkSecondary.copy(alpha = 0.45f)
            )
            Text(
                when (state) {
                    is RewardedAdState.Ready        -> "Watch a short ad · 30 min boost"
                    is RewardedAdState.Loading      -> "Loading ad…"
                    is RewardedAdState.Cooldown     -> "Available in ${cooldownSec / 60}m ${cooldownSec % 60}s"
                    is RewardedAdState.NotAvailable -> "Ad not available"
                    is RewardedAdState.Showing      -> "Watching…"
                },
                fontSize = 11.sp,
                color = OnDarkSecondary.copy(alpha = 0.40f)
            )
        }

        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = OnDarkSecondary.copy(alpha = 0.30f)
            )
            isReady -> Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.verticalGradient(listOf(GoldAccent, NeonPink.copy(alpha = 0.8f))))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("WATCH", fontSize = 10.sp, fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp, color = DarkBackground)
            }
            else -> Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(OnDarkSecondary.copy(alpha = 0.07f))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text("—", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = OnDarkSecondary.copy(alpha = 0.22f))
            }
        }
    }
}
