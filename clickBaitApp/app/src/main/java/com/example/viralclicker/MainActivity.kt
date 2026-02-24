package com.example.viralclicker

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.example.viralclicker.data.datastore.SettingsDataStore
import com.example.viralclicker.data.datastore.dataStore
import com.example.viralclicker.ui.game.GameScreen
import com.example.viralclicker.ui.theme.ViralClickerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
    }
}
