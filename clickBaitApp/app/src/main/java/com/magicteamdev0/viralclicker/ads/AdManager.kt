package com.magicteamdev0.viralclicker.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.magicteamdev0.viralclicker.BuildConfig
import com.magicteamdev0.viralclicker.data.datastore.SettingsDataStore
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

// ── Ad State models ──────────────────────────────────────────────

sealed class RewardedAdState {
    object Loading : RewardedAdState()
    object Ready : RewardedAdState()
    object Showing : RewardedAdState()
    object NotAvailable : RewardedAdState()
    data class Cooldown(val remainingMs: Long) : RewardedAdState()
}

sealed class InterstitialAdState {
    object Loading : InterstitialAdState()
    object Ready : InterstitialAdState()
    object NotAvailable : InterstitialAdState()
    object Disabled : InterstitialAdState()
}

// ── AdManager ────────────────────────────────────────────────────

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsDataStore
) {
    companion object {
        private const val TAG = "ViralClicker.AdManager"
        private const val REWARDED_AD_UNIT_ID   = BuildConfig.REWARDED_AD_UNIT_ID
        private const val INTERSTITIAL_AD_UNIT_ID = BuildConfig.INTERSTITIAL_AD_UNIT_ID

        const val INTERSTITIAL_COOLDOWN_MS = 60L * 60 * 1000       // 60 minutes
        const val REWARDED_COOLDOWN_MS     = 3L  * 60 * 1000       // 3 minutes
        const val BOOST_DURATION_MS        = 30L * 60 * 1000       // 30 minutes
        private const val RETRY_DELAY_MS   = 30_000L
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ── Rewarded Ad ──
    private var rewardedAd: RewardedAd? = null
    private val _rewardedAdState = MutableStateFlow<RewardedAdState>(RewardedAdState.Loading)
    val rewardedAdState: StateFlow<RewardedAdState> = _rewardedAdState.asStateFlow()

    // ── Interstitial Ad ──
    private var interstitialAd: InterstitialAd? = null
    private val _interstitialAdState = MutableStateFlow<InterstitialAdState>(InterstitialAdState.Loading)
    val interstitialAdState: StateFlow<InterstitialAdState> = _interstitialAdState.asStateFlow()

    // Persisted timestamps (in-memory cache, updated from DataStore)
    @Volatile private var lastRewardedElapsed  = 0L   // SystemClock.elapsedRealtime()
    @Volatile private var lastInterstitialMs   = 0L   // System.currentTimeMillis()
    @Volatile private var noAdsPurchased       = false

    init {
        scope.launch {
            // Seed cached timestamps from DataStore
            settings.lastRewardedTimestamp.first().let { lastRewardedElapsed = it }
            settings.lastInterstitialTimestamp.first().let { lastInterstitialMs = it }
            settings.noAdsPurchased.collect { noAdsPurchased = it }
        }
    }

    /** Called from MainActivity only after UMP consent is handled and MobileAds is initialized */
    fun startPreload() {
        Log.d(TAG, "Starting ad pre-load after consent/init")
        loadRewardedAd()
        loadInterstitialAd()
    }

    // ─── Rewarded Ad ─────────────────────────────────────────────

    private fun loadRewardedAd() {
        _rewardedAdState.value = RewardedAdState.Loading
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    refreshRewardedState()
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            rewardedAd = null
                            _rewardedAdState.value = RewardedAdState.Loading
                            loadRewardedAd()
                        }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e(TAG, "Rewarded ad failed to show: ${error.message} (code ${error.code})")
                            rewardedAd = null
                            _rewardedAdState.value = RewardedAdState.NotAvailable
                            scheduleRewardedRetry()
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Rewarded ad failed to load: ${error.message} (code ${error.code})")
                    rewardedAd = null
                    _rewardedAdState.value = RewardedAdState.NotAvailable
                    scheduleRewardedRetry()
                }
            }
        )
    }

    /** Show rewarded ad. Calls [onReward] only if the user earns the reward. */
    fun showRewardedAd(activity: Activity, onReward: () -> Unit) {
        val ad = rewardedAd ?: return
        _rewardedAdState.value = RewardedAdState.Showing
        ad.show(activity) {
            // onUserEarnedReward — only reached on full completion
            val now = SystemClock.elapsedRealtime()
            lastRewardedElapsed = now
            scope.launch { settings.setLastRewardedTimestamp(now) }
            onReward()
            refreshRewardedState()
        }
    }

    /** True if rewarded ad is ready AND cooldown has passed. */
    fun isRewardedReady(): Boolean {
        val cooldownPassed = (SystemClock.elapsedRealtime() - lastRewardedElapsed) >= REWARDED_COOLDOWN_MS
        return rewardedAd != null && cooldownPassed
    }

    private fun refreshRewardedState() {
        val elapsed = SystemClock.elapsedRealtime() - lastRewardedElapsed
        _rewardedAdState.value = when {
            rewardedAd == null              -> RewardedAdState.NotAvailable
            elapsed < REWARDED_COOLDOWN_MS  -> RewardedAdState.Cooldown(REWARDED_COOLDOWN_MS - elapsed)
            else                            -> RewardedAdState.Ready
        }
        if (_rewardedAdState.value is RewardedAdState.Cooldown) {
            // Tick the cooldown every second
            scope.launch {
                delay(1_000L)
                refreshRewardedState()
            }
        }
    }

    private fun scheduleRewardedRetry() {
        scope.launch {
            delay(RETRY_DELAY_MS)
            loadRewardedAd()
        }
    }

    // ─── Interstitial Ad ─────────────────────────────────────────

    private fun loadInterstitialAd() {
        if (noAdsPurchased) {
            _interstitialAdState.value = InterstitialAdState.Disabled
            return
        }
        _interstitialAdState.value = InterstitialAdState.Loading
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    _interstitialAdState.value = InterstitialAdState.Ready
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            loadInterstitialAd()          // pre-cache next
                        }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e(TAG, "Interstitial ad failed to show: ${error.message} (code ${error.code})")
                            interstitialAd = null
                            _interstitialAdState.value = InterstitialAdState.NotAvailable
                            scheduleInterstitialRetry()
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${error.message} (code ${error.code})")
                    interstitialAd = null
                    _interstitialAdState.value = InterstitialAdState.NotAvailable
                    scheduleInterstitialRetry()
                }
            }
        )
    }

    /**
     * Show interstitial if:
     * - User has not purchased "No Ads"
     * - Cooldown of 60 min has passed (wall-clock)
     * - [comboCount] == 0 (never interrupt an active combo)
     * - An ad is loaded and ready
     */
    fun showInterstitialIfReady(activity: Activity, comboCount: Int) {
        if (noAdsPurchased) return
        if (comboCount > 0) return
        val now = System.currentTimeMillis()
        if ((now - lastInterstitialMs) < INTERSTITIAL_COOLDOWN_MS) return
        val ad = interstitialAd ?: return

        lastInterstitialMs = now
        scope.launch { settings.setLastInterstitialTimestamp(now) }
        ad.show(activity)
    }

    private fun scheduleInterstitialRetry() {
        scope.launch {
            delay(RETRY_DELAY_MS)
            loadInterstitialAd()
        }
    }

    fun destroy() {
        scope.cancel()
    }
}
