package com.magicteamdev0.viralclicker.ui.game

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magicteamdev0.viralclicker.ads.AdManager
import com.magicteamdev0.viralclicker.data.datastore.SettingsDataStore
import com.magicteamdev0.viralclicker.data.entity.OwnedUpgradeEntity
import com.magicteamdev0.viralclicker.data.entity.PlayerEntity
import com.magicteamdev0.viralclicker.data.repository.GameRepository
import com.magicteamdev0.viralclicker.domain.catalog.MilestoneCatalog
import com.magicteamdev0.viralclicker.domain.catalog.UpgradeCatalog
import com.magicteamdev0.viralclicker.domain.model.*
import com.magicteamdev0.viralclicker.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

data class MilestoneEvent(val milestoneId: String, val name: String, val emoji: String = "🎉")

data class TapResultEvent(
    val pointsEarned: ViralPoints,
    val isCritical: Boolean,
    val comboCount: Int,
    val comboMultiplier: Double
)

data class PurchaseEvent(val upgradeId: String, val newCount: Int)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: GameRepository,
    private val settings: SettingsDataStore,
    private val adManager: AdManager,
    private val tapUseCase: TapUseCase,
    private val tickUseCase: TickUseCase,
    private val purchaseUpgradeUseCase: PurchaseUpgradeUseCase,
    private val prestigeUseCase: PrestigeUseCase,
    private val dailyStreakUseCase: DailyStreakUseCase
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _milestoneEvents = MutableSharedFlow<MilestoneEvent>(extraBufferCapacity = 10)
    val milestoneEvents: SharedFlow<MilestoneEvent> = _milestoneEvents.asSharedFlow()

    private val _tapEvents = MutableSharedFlow<TapResultEvent>(extraBufferCapacity = 20)
    val tapEvents: SharedFlow<TapResultEvent> = _tapEvents.asSharedFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseEvent>(extraBufferCapacity = 10)
    val purchaseEvents: SharedFlow<PurchaseEvent> = _purchaseEvents.asSharedFlow()

    // Ad state — delegated from AdManager
    val rewardedAdState = adManager.rewardedAdState
    val interstitialAdState = adManager.interstitialAdState

    // In-memory player state (persisted periodically)
    private var _player = PlayerEntity()
    private var _ownedMap = mutableMapOf<String, Int>()
    private var _unlockedMilestoneIds = mutableSetOf<String>()

    private var tickerJob: Job? = null
    private var autoSaveJob: Job? = null
    private var comboDecayJob: Job? = null
    private val initialized = AtomicBoolean(false)

    // Language code cached in-memory; updated via Flow collector (avoids DataStore read on every tap)
    @Volatile private var _cachedLanguageCode = "en"

    // Combo state (session-only, not persisted)
    private var _comboCount = 0
    private var _lastTapTime = 0L

    companion object {
        const val AUTO_SAVE_INTERVAL_MS = 30_000L
        const val MAX_OFFLINE_SECONDS = 8 * 3600L // 8 hours
        private const val COMBO_TIMEOUT_MS = 2000L
        private const val CRITICAL_HIT_CHANCE = 0.05f
        private const val CRITICAL_HIT_MULTIPLIER = 10.0
    }

    init {
        // Collect language code from DataStore once; cache it so emitState() never calls .first()
        viewModelScope.launch {
            settings.languageCode.collect { code -> _cachedLanguageCode = code }
        }
        viewModelScope.launch {
            loadGame()
            startTicker()
            startAutoSave()
        }
    }

    private suspend fun loadGame() {
        repository.initMilestones()
        _player = repository.getOrCreatePlayer()
        _ownedMap = repository.getOwnedUpgrades()
            .associate { it.upgradeId to it.count }
            .toMutableMap()
        _unlockedMilestoneIds = repository.getMilestones()
            .filter { it.unlockedAt != null }
            .map { it.id }
            .toMutableSet()

        // Apply offline progress
        val now = System.currentTimeMillis()
        val elapsedSeconds = ((now - _player.lastSavedAt) / 1000L).coerceAtMost(MAX_OFFLINE_SECONDS)
        var offlineEarnings: ViralPoints? = null

        if (elapsedSeconds > 5) {
            val pps = ViralPoints.fromString(_player.pointsPerSecond)
            if (pps.value.signum() > 0) {
                val earned = pps * (_player.prestigeMultiplier * _boostMultiplier(now))
                val earnedTotal = ViralPoints(earned.value.multiply(java.math.BigDecimal(elapsedSeconds)))
                val current = ViralPoints.fromString(_player.viralPoints)
                val allTime = ViralPoints.fromString(_player.allTimePoints)
                _player = _player.copy(
                    viralPoints = (current + earnedTotal).value.toPlainString(),
                    allTimePoints = (allTime + earnedTotal).value.toPlainString(),
                    lastSavedAt = now
                )
                offlineEarnings = earnedTotal
            }
        }

        // Daily streak check
        val streakResult = dailyStreakUseCase.execute(_player)
        _player = streakResult.updatedPlayer
        if (streakResult.isNewDay) {
            repository.savePlayer(_player)
        }

        val noAds = settings.noAdsPurchased.first()
        emitState(offlineEarnings, noAds, dailyBonus = streakResult.bonusAwarded)
        initialized.set(true)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (!initialized.get()) continue
                val now = System.currentTimeMillis()
                val pps = ViralPoints.fromString(_player.pointsPerSecond)
                _player = tickUseCase.execute(
                    _player,
                    pps,
                    _player.prestigeMultiplier,
                    _boostMultiplier(now)
                )
                checkMilestones(now)
                emitState()
            }
        }
    }

    private fun startAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            while (true) {
                delay(AUTO_SAVE_INTERVAL_MS)
                repository.savePlayer(_player.copy(lastSavedAt = System.currentTimeMillis()))
            }
        }
    }

    fun onTap() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()

            // Combo logic
            if (now - _lastTapTime < COMBO_TIMEOUT_MS) {
                _comboCount++
            } else {
                _comboCount = 1
            }
            _lastTapTime = now

            val comboMultiplier = when {
                _comboCount >= 30 -> 5.0
                _comboCount >= 15 -> 3.0
                _comboCount >= 5 -> 2.0
                else -> 1.0
            }

            // Critical hit roll
            val isCritical = kotlin.random.Random.nextFloat() < CRITICAL_HIT_CHANCE
            val critMultiplier = if (isCritical) CRITICAL_HIT_MULTIPLIER else 1.0

            val ppc = ViralPoints.fromString(_player.pointsPerClick)
            val effectivePpc = ppc * (comboMultiplier * critMultiplier)
            val boostMult = _boostMultiplier(now)
            _player = tapUseCase.execute(_player, effectivePpc, _player.prestigeMultiplier, boostMult)

            val totalEarned = effectivePpc * (_player.prestigeMultiplier * boostMult)
            _tapEvents.tryEmit(TapResultEvent(totalEarned, isCritical, _comboCount, comboMultiplier))

            // Schedule combo decay
            comboDecayJob?.cancel()
            comboDecayJob = viewModelScope.launch {
                delay(COMBO_TIMEOUT_MS)
                _comboCount = 0
                emitState()
            }

            checkMilestones(now)
            emitState()
        }
    }

    fun onPurchaseUpgrade(upgradeId: String) {
        viewModelScope.launch {
            val result = purchaseUpgradeUseCase.execute(upgradeId, _player, _ownedMap)
            if (result.success) {
                _player = result.updatedPlayer
                _ownedMap[upgradeId] = result.updatedOwnedUpgrade.count
                repository.saveOwnedUpgrade(result.updatedOwnedUpgrade)
                repository.savePlayer(_player)
                _purchaseEvents.tryEmit(PurchaseEvent(upgradeId, result.updatedOwnedUpgrade.count))
                checkMilestones(System.currentTimeMillis())
                emitState()
            }
        }
    }

    fun onPrestige() {
        viewModelScope.launch {
            val result = prestigeUseCase.execute(_player)
            _player = result.updatedPlayer
            _ownedMap.clear()
            emitState()
        }
    }

    fun onBoostRewarded() {
        viewModelScope.launch {
            val boostDurationMs = AdManager.BOOST_DURATION_MS
            val now = System.currentTimeMillis()
            // Extend boost rather than restart: if boost already active, add on top
            val currentEnd = _player.boostActiveUntil ?: 0L
            val newEnd = maxOf(now, currentEnd) + boostDurationMs
            _player = _player.copy(boostActiveUntil = newEnd)
            repository.savePlayer(_player)
            emitState()
        }
    }

    /** Called from UI — shows rewarded ad and triggers boost on completion. */
    fun showRewardedAd(activity: Activity) {
        adManager.showRewardedAd(activity) { onBoostRewarded() }
    }

    /** Called on pager page change — shows interstitial if conditions are met. */
    fun tryShowInterstitial(activity: Activity) {
        adManager.showInterstitialIfReady(activity, _comboCount)
    }

    fun onDismissOfflineEarnings() {
        _gameState.update { it.copy(offlineEarnings = null) }
    }

    fun onNoAdsPurchased() {
        viewModelScope.launch {
            settings.setNoAdsPurchased(true)
            _gameState.update { it.copy(noAdsPurchased = true) }
        }
    }

    private fun checkMilestones(now: Long) {
        val allTime = ViralPoints.fromString(_player.allTimePoints)
        val totalOwned = _ownedMap.values.sum()
        val prestigeCount = _player.prestigeCount.toLong()

        MilestoneCatalog.all.forEach { def ->
            if (def.id in _unlockedMilestoneIds) return@forEach
            val triggered = when (def.triggerType) {
                MilestoneTrigger.ALL_TIME_POINTS -> allTime.value >= java.math.BigDecimal(def.threshold)
                MilestoneTrigger.PRESTIGE_COUNT -> prestigeCount >= def.threshold
                MilestoneTrigger.TOTAL_UPGRADES_OWNED -> totalOwned >= def.threshold
            }
            if (triggered) {
                _unlockedMilestoneIds.add(def.id)
                viewModelScope.launch {
                    repository.unlockMilestone(def.id, now)
                    _milestoneEvents.tryEmit(MilestoneEvent(def.id, def.name))
                }
            }
        }
    }

    fun onDismissDailyBonus() {
        _gameState.update { it.copy(dailyBonus = null) }
    }

    fun onSetLanguage(code: String) {
        viewModelScope.launch {
            settings.setLanguageCode(code)
        }
    }

    private fun computeNextMilestone(): NextMilestoneProgress? {
        val allTime = ViralPoints.fromString(_player.allTimePoints)
        val totalOwned = _ownedMap.values.sum()
        val prestigeCount = _player.prestigeCount.toLong()

        val nextDef = MilestoneCatalog.all.firstOrNull { def ->
            def.id !in _unlockedMilestoneIds
        } ?: return null

        val (current, target) = when (nextDef.triggerType) {
            MilestoneTrigger.ALL_TIME_POINTS -> allTime.value.toDouble() to nextDef.threshold.toDouble()
            MilestoneTrigger.PRESTIGE_COUNT -> prestigeCount.toDouble() to nextDef.threshold.toDouble()
            MilestoneTrigger.TOTAL_UPGRADES_OWNED -> totalOwned.toDouble() to nextDef.threshold.toDouble()
        }

        val rawProgress = if (target > 0) (current / target).coerceIn(0.0, 1.0).toFloat() else 0f
        // Endowed progress: map 0..1 into 0.1..1.0 range so users feel they've already started
        val endowedProgress = 0.1f + rawProgress * 0.9f

        return NextMilestoneProgress(
            name = nextDef.name,
            progress = endowedProgress,
            currentValue = ViralPoints.fromDouble(current).toDisplayString(),
            targetValue = ViralPoints.fromDouble(target).toDisplayString()
        )
    }

    private suspend fun emitState(offlineEarnings: ViralPoints? = null, noAds: Boolean? = null, dailyBonus: ViralPoints? = null) {
        val resolvedNoAds = noAds ?: _gameState.value.noAdsPurchased
        val langCode = _cachedLanguageCode // Use in-memory cache — no DataStore I/O per tap
        val now = System.currentTimeMillis()
        val milestones = MilestoneCatalog.all.map { def ->
            val unlocked = def.id in _unlockedMilestoneIds
            MilestoneState(
                id = def.id,
                name = def.name,
                description = def.description,
                unlocked = unlocked,
                unlockedAt = null
            )
        }

        val comboMult = when {
            _comboCount >= 30 -> 5.0
            _comboCount >= 15 -> 3.0
            _comboCount >= 5 -> 2.0
            else -> 1.0
        }

        _gameState.emit(
            GameState(
                viralPoints = ViralPoints.fromString(_player.viralPoints),
                allTimePoints = ViralPoints.fromString(_player.allTimePoints),
                pointsPerClick = ViralPoints.fromString(_player.pointsPerClick) * _boostMultiplier(now),
                pointsPerSecond = ViralPoints.fromString(_player.pointsPerSecond) * _boostMultiplier(now),
                prestigeMultiplier = _player.prestigeMultiplier,
                prestigeCount = _player.prestigeCount,
                boostActiveUntil = _player.boostActiveUntil,
                ownedUpgrades = _ownedMap.toMap(),
                milestones = milestones,
                noAdsPurchased = resolvedNoAds,
                activeSkinId = _player.activeSkinId,
                offlineEarnings = offlineEarnings,
                prestigeAvailable = ViralPoints.fromString(_player.allTimePoints)
                    .isGreaterThanOrEqual(PRESTIGE_THRESHOLD),
                comboCount = _comboCount,
                comboMultiplier = comboMult,
                nextMilestone = computeNextMilestone(),
                dailyStreak = _player.currentStreak,
                dailyBonus = dailyBonus ?: _gameState.value.dailyBonus,
                languageCode = langCode
            )
        )
    }

    private fun _boostMultiplier(nowMs: Long): Double =
        if (_player.boostActiveUntil != null && nowMs < _player.boostActiveUntil!!) 2.0 else 1.0

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            repository.savePlayer(_player.copy(lastSavedAt = System.currentTimeMillis()))
        }
    }
}
