package com.example.viralclicker.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.viralclicker.ui.game.components.*
import com.example.viralclicker.ui.prestige.PrestigeDialog
import com.example.viralclicker.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel = hiltViewModel()) {
    val state by viewModel.gameState.collectAsState()
    val scope = rememberCoroutineScope()
    var showPrestigeDialog by remember { mutableStateOf(false) }
    var celebratingMilestone by remember { mutableStateOf<MilestoneEvent?>(null) }

    // Milestone celebrations (replaces snackbar)
    LaunchedEffect(Unit) {
        viewModel.milestoneEvents.collectLatest { event ->
            celebratingMilestone = event
        }
    }

    // Offline earnings dialog
    if (state.offlineEarnings != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissOfflineEarnings,
            title = { Text("👋 Welcome back!") },
            text = { Text("You earned ${state.offlineEarnings!!.toDisplayString()} Viral Points while away!") },
            confirmButton = {
                TextButton(onClick = viewModel::onDismissOfflineEarnings) { Text("Nice!") }
            }
        )
    }

    // Daily bonus dialog
    if (state.dailyBonus != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDailyBonus,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "\uD83D\uDD25 Day ${state.dailyStreak} Streak!",
                        fontWeight = FontWeight.Black
                    )
                }
            },
            text = {
                Text("You earned +${state.dailyBonus!!.toDisplayString()} Viral Points!\n\nKeep logging in daily to increase your bonus!")
            },
            confirmButton = {
                Button(
                    onClick = viewModel::onDismissDailyBonus,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Collect!", fontWeight = FontWeight.Bold, color = DarkBackground)
                }
            }
        )
    }

    if (showPrestigeDialog) {
        PrestigeDialog(
            currentMultiplier = state.prestigeMultiplier,
            onConfirm = {
                viewModel.onPrestige()
                showPrestigeDialog = false
            },
            onDismiss = { showPrestigeDialog = false }
        )
    }

    Scaffold { paddingValues ->
        val pagerState = rememberPagerState(pageCount = { 3 })

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Cyberpunk Nav Bar ──
                CyberpunkNavBar(
                    pagerState = pagerState,
                    onPageSelected = { scope.launch { pagerState.animateScrollToPage(it) } }
                )


                HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                    when (page) {
                        0 -> GameTab(state, viewModel, onPrestige = { showPrestigeDialog = true })
                        1 -> UpgradeShop(
                            viralPoints = state.viralPoints,
                            allTimePoints = state.allTimePoints,
                            ownedUpgrades = state.ownedUpgrades,
                            onPurchase = viewModel::onPurchaseUpgrade,
                            purchaseEvents = viewModel.purchaseEvents
                        )

                        2 -> MilestonePanel(
                            milestones = state.milestones,
                            allTimePoints = state.allTimePoints,
                            prestigeCount = state.prestigeCount,
                            totalUpgradesOwned = state.ownedUpgrades.values.sum()
                        )
                    }
                }
            }

            // Milestone celebration overlay (on top of everything)
            celebratingMilestone?.let { event ->
                MilestoneCelebration(
                    milestoneName = event.name,
                    onDismiss = { celebratingMilestone = null }
                )
            }
        }
    }
}

@Composable
private fun GameTab(
    state: com.example.viralclicker.domain.model.GameState,
    viewModel: GameViewModel,
    onPrestige: () -> Unit
) {
    val view = LocalView.current
    val floatingTexts = remember { mutableStateListOf<FloatingTextData>() }

    // Screen flash for critical hits
    val flashAlpha = remember { Animatable(0f) }

    // Screen shake for prestige
    val shakeOffsetX = remember { Animatable(0f) }

    // Collect tap events for floating text + critical flash
    LaunchedEffect(Unit) {
        viewModel.tapEvents.collect { event ->
            val text = if (event.isCritical) {
                "CRITICAL! +${event.pointsEarned.toDisplayString()}"
            } else if (event.comboMultiplier > 1.0) {
                "+${event.pointsEarned.toDisplayString()} ×${event.comboMultiplier.toInt()}"
            } else {
                "+${event.pointsEarned.toDisplayString()}"
            }

            floatingTexts.add(
                FloatingTextData(
                    id = System.nanoTime(),
                    text = text,
                    isCritical = event.isCritical,
                    offsetX = Random.nextFloat() * 60 - 30
                )
            )

            // Limit floating text count
            if (floatingTexts.size > 15) {
                floatingTexts.removeFirst()
            }

            // Screen flash on critical hit
            if (event.isCritical) {
                flashAlpha.snapTo(0.35f)
                flashAlpha.animateTo(0f, tween(400))
                @Suppress("DEPRECATION")
                view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Edge glow during combos
        if (state.comboMultiplier > 1.0) {
            val glowColor = when {
                state.comboMultiplier >= 5.0 -> NeonPink
                state.comboMultiplier >= 3.0 -> GoldAccent
                else -> NeonCyan
            }
            val alpha = ((state.comboMultiplier - 1.0) / 4.0 * 0.25).toFloat()
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, glowColor.copy(alpha = alpha)),
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.minDimension
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .offset(x = shakeOffsetX.value.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            StatsBar(
                viralPoints = state.viralPoints,
                pointsPerSecond = state.pointsPerSecond,
                pointsPerClick = state.pointsPerClick,
                prestigeMultiplier = state.prestigeMultiplier,
                boostActiveUntil = state.boostActiveUntil,
                nextMilestone = state.nextMilestone,
                dailyStreak = state.dailyStreak
            )

            BoostTimer(boostActiveUntil = state.boostActiveUntil)

            // Combo meter above clicker
            ComboMeter(
                comboCount = state.comboCount,
                comboMultiplier = state.comboMultiplier
            )

            // Clicker with floating text overlay and particles
            Box(contentAlignment = Alignment.Center) {
                // Particle effect during high combos
                ParticleEffect(
                    isActive = state.comboCount >= 5,
                    particleColor = when {
                        state.comboMultiplier >= 5.0 -> NeonPink
                        state.comboMultiplier >= 3.0 -> GoldAccent
                        else -> NeonCyan
                    },
                    secondaryColor = NeonPurple,
                    modifier = Modifier.size(500.dp)
                )

                ClickerElement(
                    skinId = state.activeSkinId,
                    onTap = viewModel::onTap,
                    comboMultiplier = state.comboMultiplier,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Floating point numbers
                FloatingTextOverlay(
                    floatingTexts = floatingTexts,
                    onRemove = { id -> floatingTexts.removeAll { it.id == id } }
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.prestigeAvailable) {
                    Button(
                        onClick = onPrestige,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("\uD83D\uDD25 Go Viral! (Prestige)", fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = { /* rewarded ad — wired to BillingManager in Phase 4 */ },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.boostActiveUntil == null ||
                            System.currentTimeMillis() >= (state.boostActiveUntil ?: 0L)
                ) {
                    Text("\uD83D\uDCFA Watch Ad for 2× Boost")
                }
            }
        }

        // Critical hit screen flash overlay
        if (flashAlpha.value > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(GoldAccent.copy(alpha = flashAlpha.value))
            )
        }
    }
}

// ═══════════════════════════════════════════════
//  CYBERPUNK NAV BAR — Flat Neon Indicator
// ═══════════════════════════════════════════════

private data class NavTab(val emoji: String, val label: String, val color: Color)

private val navTabs = listOf(
    NavTab("\uD83C\uDFAE", "GAME", NeonCyan),
    NavTab("\uD83D\uDED2", "SHOP", NeonPurple),
    NavTab("\uD83C\uDFC6", "VAULT", GoldAccent)
)

@Composable
private fun CyberpunkNavBar(
    pagerState: PagerState,
    onPageSelected: (Int) -> Unit
) {
    val selectedPage = pagerState.currentPage

    // Smooth sliding indicator position (fractional tab index)
    val animatedTabOffset by animateFloatAsState(
        targetValue = selectedPage.toFloat(),
        animationSpec = spring(dampingRatio = 0.70f, stiffness = 380f),
        label = "indicator_pos"
    )

    // Indicator color transitions between tab neon colors
    val indicatorColor by animateColorAsState(
        targetValue = navTabs[selectedPage].color,
        animationSpec = tween(280),
        label = "indicator_color"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
    ) {
        // ── Tab items row ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            navTabs.forEachIndexed { index, tab ->
                val isSelected = selectedPage == index

                // Emoji scale: spring bounce on selection
                val emojiScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.12f else 0.88f,
                    animationSpec = spring(dampingRatio = 0.55f, stiffness = 480f),
                    label = "scale_$index"
                )
                // Alpha for whole tab item
                val itemAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.38f,
                    animationSpec = tween(200),
                    label = "alpha_$index"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onPageSelected(index) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Emoji — scaled, no background, no glow ball
                    Text(
                        text = tab.emoji,
                        fontSize = 20.sp,
                        modifier = Modifier.graphicsLayer(
                            scaleX = emojiScale,
                            scaleY = emojiScale,
                            alpha = itemAlpha
                        )
                    )

                    // Label — always visible, caps with tight tracking
                    Text(
                        text = tab.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                        letterSpacing = 2.sp,
                        color = if (isSelected) tab.color
                        else OnDarkSecondary.copy(alpha = 0.38f)
                    )
                }
            }
        }

        // ── Sliding neon underline indicator ──
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            val tabW = size.width / 3f
            val barW = tabW * 0.40f
            val x = animatedTabOffset * tabW + (tabW - barW) / 2f

            // Soft glow halo above bar
            drawRoundRect(
                color = indicatorColor.copy(alpha = 0.20f),
                topLeft = Offset(x - 8f, -6f),
                size = Size(barW + 16f, 10f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f)
            )
            // Solid 2dp bar
            drawRoundRect(
                color = indicatorColor,
                topLeft = Offset(x, 0f),
                size = Size(barW, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f)
            )
        }

        // Baseline hairline
        Box(
            Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(OnDarkSecondary.copy(alpha = 0.06f))
        )
    }
}
