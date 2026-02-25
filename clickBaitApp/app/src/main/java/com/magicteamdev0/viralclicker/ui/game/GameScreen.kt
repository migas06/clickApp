package com.magicteamdev0.viralclicker.ui.game

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.magicteamdev0.viralclicker.R
import com.magicteamdev0.viralclicker.ads.RewardedAdState
import com.magicteamdev0.viralclicker.ui.game.components.*
import com.magicteamdev0.viralclicker.ui.prestige.PrestigeDialog
import com.magicteamdev0.viralclicker.ui.settings.LanguageBottomSheet
import com.magicteamdev0.viralclicker.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel = hiltViewModel()) {
    val state by viewModel.gameState.collectAsState()
    val rewardedAdState by viewModel.rewardedAdState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    var showPrestigeDialog by remember { mutableStateOf(false) }
    var celebratingMilestone by remember { mutableStateOf<MilestoneEvent?>(null) }
    var showLanguageSheet by remember { mutableStateOf(false) }

    // Milestone celebrations
    LaunchedEffect(Unit) {
        viewModel.milestoneEvents.collectLatest { event ->
            celebratingMilestone = event
        }
    }

    // Offline earnings dialog
    if (state.offlineEarnings != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissOfflineEarnings,
            title = { Text("👋 ${stringResource(R.string.offline_earnings_title)}") },
            text = { Text(stringResource(R.string.offline_earnings_message, state.offlineEarnings!!.toDisplayString())) },
            confirmButton = {
                TextButton(onClick = viewModel::onDismissOfflineEarnings) { Text(stringResource(R.string.offline_confirm)) }
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
                        "🔥 ${stringResource(R.string.streak_title, state.dailyStreak)}",
                        fontWeight = FontWeight.Black
                    )
                }
            },
            text = {
                Text(stringResource(R.string.streak_message, state.dailyBonus!!.toDisplayString()))
            },
            confirmButton = {
                Button(
                    onClick = viewModel::onDismissDailyBonus,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text(stringResource(R.string.streak_collect), fontWeight = FontWeight.Bold, color = DarkBackground)
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

        // Trigger interstitial on page change
        LaunchedEffect(pagerState.currentPage) {
            if (pagerState.currentPage != 0) {
                activity?.let { viewModel.tryShowInterstitial(it) }
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // ── Layer 0: Cyberpunk city background ──
            CyberpunkBackground(modifier = Modifier.fillMaxSize())

            // ── Layer 1: Main content ──
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Content pages (weight 1f = fills available space)
                HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                    when (page) {
                        0 -> GameTab(state, viewModel, onPrestige = { showPrestigeDialog = true })
                        1 -> UpgradeShop(
                            viralPoints = state.viralPoints,
                            allTimePoints = state.allTimePoints,
                            ownedUpgrades = state.ownedUpgrades,
                            onPurchase = viewModel::onPurchaseUpgrade,
                            purchaseEvents = viewModel.purchaseEvents,
                            rewardedAdState = rewardedAdState,
                            onWatchAd = { activity?.let { viewModel.showRewardedAd(it) } }
                        )
                        2 -> MilestonePanel(
                            milestones = state.milestones,
                            allTimePoints = state.allTimePoints,
                            prestigeCount = state.prestigeCount,
                            totalUpgradesOwned = state.ownedUpgrades.values.sum()
                        )
                    }
                }

                // ── Cyberpunk Nav Bar (bottom) ──
                CyberpunkNavBar(
                    pagerState = pagerState,
                    onPageSelected = { scope.launch { pagerState.animateScrollToPage(it) } },
                    onSettingsClick = { showLanguageSheet = true }
                )
            }

            // ── Layer 2: Milestone celebration overlay ──
            celebratingMilestone?.let { event ->
                MilestoneCelebration(
                    milestoneName = event.name,
                    onDismiss = { celebratingMilestone = null }
                )
            }
        }
    }

    if (showLanguageSheet) {
        LanguageBottomSheet(
            currentLanguageCode = state.languageCode,
            onLanguageSelected = { code ->
                scope.launch {
                    viewModel.onSetLanguage(code)
                    kotlinx.coroutines.delay(100)
                    (context as android.app.Activity).recreate()
                }
            },
            onDismiss = { showLanguageSheet = false }
        )
    }
}

@Composable
private fun GameTab(
    state: com.magicteamdev0.viralclicker.domain.model.GameState,
    viewModel: GameViewModel,
    onPrestige: () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val floatingTexts = remember { mutableStateListOf<FloatingTextData>() }

    val flashAlpha = remember { Animatable(0f) }
    val shakeOffsetX = remember { Animatable(0f) }

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
            if (floatingTexts.size > 15) floatingTexts.removeFirst()

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
            val alpha = ((state.comboMultiplier - 1.0) / 4.0 * 0.22).toFloat()
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
                .offset(x = shakeOffsetX.value.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Stats bar at top
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

            ComboMeter(
                comboCount = state.comboCount,
                comboMultiplier = state.comboMultiplier
            )

            // Clicker with floating text and particles
            Box(contentAlignment = Alignment.Center) {
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

                FloatingTextOverlay(
                    floatingTexts = floatingTexts,
                    onRemove = { id -> floatingTexts.removeAll { it.id == id } }
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                if (state.prestigeAvailable) {
                    Button(
                        onClick = onPrestige,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text("🔥 ${stringResource(R.string.prestige_button)}", fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = { activity?.let { viewModel.showRewardedAd(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    enabled = state.boostActiveUntil == null ||
                            System.currentTimeMillis() >= (state.boostActiveUntil ?: 0L)
                ) {
                    Text("📺 ${stringResource(R.string.watch_ad_button)}")
                }
            }
        }

        // Critical hit screen flash
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
//  CYBERPUNK NAV BAR — Bottom, icon + label
// ═══════════════════════════════════════════════

private data class NavTab(val iconType: NavIconType, val label: String, val color: Color)

@Composable
private fun CyberpunkNavBar(
    pagerState: PagerState,
    onPageSelected: (Int) -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val selectedPage = pagerState.currentPage
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    // Reserve space for the settings icon (40dp button, 32dp Row end-padding used)
    val settingsPadPx = with(density) { 32.dp.toPx() }

    val navTabs = listOf(
        NavTab(NavIconType.GAME,  stringResource(R.string.nav_game),  NeonCyan),
        NavTab(NavIconType.SHOP,  stringResource(R.string.nav_shop),  NeonPurple),
        NavTab(NavIconType.VAULT, stringResource(R.string.nav_vault), GoldAccent)
    )

    val animatedTabOffset by animateFloatAsState(
        targetValue = selectedPage.toFloat(),
        animationSpec = spring(dampingRatio = 0.70f, stiffness = 380f),
        label = "indicator_pos"
    )

    val indicatorColor by animateColorAsState(
        targetValue = navTabs[selectedPage].color,
        animationSpec = tween(280),
        label = "indicator_color"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface.copy(alpha = 0.95f))
    ) {
        // ── Neon top indicator line (active tab highlight) ──
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            // Full symmetric width without right-padding deduction
            val tabAreaW = size.width
            val tabW = tabAreaW / 3f
            val barW = tabW * 0.50f
            val rtlOffset = if (layoutDirection == LayoutDirection.Rtl) 2f - animatedTabOffset else animatedTabOffset
            val x = rtlOffset * tabW + (tabW - barW) / 2f

            // Soft glow halo
            drawRoundRect(
                color = indicatorColor.copy(alpha = 0.22f),
                topLeft = Offset(x - 10f, -8f),
                size = Size(barW + 20f, 12f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
            )
            // Solid bar
            drawRoundRect(
                color = indicatorColor,
                topLeft = Offset(x, 0f),
                size = Size(barW, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f)
            )
        }

        // Hairline separator below indicator
        Box(
            Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(OnDarkSecondary.copy(alpha = 0.08f))
        )

        // ── Tabs row ──
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                navTabs.forEachIndexed { index, tab ->
                    val isSelected = selectedPage == index

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.18f else 0.88f,
                        animationSpec = spring(dampingRatio = 0.52f, stiffness = 500f),
                        label = "scale_$index"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onPageSelected(index) }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Cyberpunk canvas icon with neon glow
                        CyberpunkNavIcon(
                            type = tab.iconType,
                            isSelected = isSelected,
                            color = tab.color,
                            modifier = Modifier
                                .size(26.dp)
                                .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                        )
                        Text(
                            text = tab.label,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            letterSpacing = 1.5.sp,
                            color = if (isSelected) tab.color
                            else OnDarkSecondary.copy(alpha = 0.32f)
                        )
                    }
                }
            }

            // Settings icon (end of row, overlapping right-most tab gracefully)
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(40.dp)
                    .padding(end = 4.dp, top = 2.dp)
            ) {
                CyberpunkNavIcon(
                    type = NavIconType.GEAR,
                    isSelected = true,
                    color = OnDarkSecondary.copy(alpha = 0.45f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
