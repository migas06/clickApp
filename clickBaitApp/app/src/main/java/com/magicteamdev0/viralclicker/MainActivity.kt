package com.magicteamdev0.viralclicker

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import android.util.Log
import com.magicteamdev0.viralclicker.ads.AdManager
import com.magicteamdev0.viralclicker.data.datastore.SettingsDataStore
import com.magicteamdev0.viralclicker.data.datastore.dataStore
import com.magicteamdev0.viralclicker.ui.game.GameScreen
import com.magicteamdev0.viralclicker.ui.theme.ViralClickerTheme
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var adManager: AdManager

    companion object {
        private const val TAG = "ViralClicker.Main"
    }

    override fun attachBaseContext(newBase: Context) {
        // Read the persisted language code synchronously (DataStore is file-backed, < 5ms)
        val langCode = runBlocking {
            newBase.dataStore.data
                .map { prefs -> prefs[SettingsDataStore.LANGUAGE_CODE] ?: "en" }
                .first()
        }
        val locale = Locale.forLanguageTag(langCode)
        val config = Configuration(newBase.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale) // Enables RTL automatically for Arabic etc.
        }
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Force white status bar icons on dark background
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false
        setContent {
            ViralClickerTheme {
                GameScreen()
            }
        }

        initConsent()
    }

    private fun initConsent() {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInfo = UserMessagingPlatform.getConsentInformation(this)

        consentInfo.requestConsentInfoUpdate(
            this,
            params,
            {
                // The consent information state was updated.
                // You are now ready to check if a form is available.
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this
                ) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        Log.w(TAG, "Consent form load/show failed: ${loadAndShowError.message}")
                    }
                    // Consent has been gathered (or not required). Initialize Mobile Ads SDK.
                    if (consentInfo.canRequestAds()) {
                        initMobileAds()
                    }
                }
            },
            { requestConsentError ->
                // Consent gathering failed.
                Log.w(TAG, "Consent info update failed: ${requestConsentError.message}")
                if (consentInfo.canRequestAds()) {
                    initMobileAds()
                }
            }
        )

        // Check if we can initialize ads on startup (if consent is already available from previous run)
        if (consentInfo.canRequestAds()) {
            initMobileAds()
        }
    }

    private var isMobileAdsInitializeCalled = false

    private fun initMobileAds() {
        if (isMobileAdsInitializeCalled) return
        isMobileAdsInitializeCalled = true

        Thread {
            MobileAds.initialize(this) { status ->
                Log.d(TAG, "MobileAds initialized: ${status.adapterStatusMap.keys}")
                // Delegate ad loading to the AdManager singleton
                adManager.startPreload()
            }
        }.start()
    }
}
