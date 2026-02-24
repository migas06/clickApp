package com.magicteamdev0.viralclicker.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "viral_clicker_settings")

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        val NO_ADS_PURCHASED = booleanPreferencesKey("no_ads_purchased")
        val ACTIVE_SKIN_ID = stringPreferencesKey("active_skin_id")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val LAST_INTERSTITIAL_TIMESTAMP = longPreferencesKey("last_interstitial_timestamp")
        val LAST_REWARDED_TIMESTAMP = longPreferencesKey("last_rewarded_timestamp")
    }

    val noAdsPurchased: Flow<Boolean> = context.dataStore.data.map { it[NO_ADS_PURCHASED] ?: false }
    val activeSkinId: Flow<String> = context.dataStore.data.map { it[ACTIVE_SKIN_ID] ?: "default" }
    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { it[SOUND_ENABLED] ?: true }
    val languageCode: Flow<String> = context.dataStore.data.map { it[LANGUAGE_CODE] ?: "en" }
    val lastInterstitialTimestamp: Flow<Long> = context.dataStore.data.map { it[LAST_INTERSTITIAL_TIMESTAMP] ?: 0L }
    val lastRewardedTimestamp: Flow<Long> = context.dataStore.data.map { it[LAST_REWARDED_TIMESTAMP] ?: 0L }

    suspend fun setNoAdsPurchased(value: Boolean) {
        context.dataStore.edit { it[NO_ADS_PURCHASED] = value }
    }

    suspend fun setActiveSkinId(id: String) {
        context.dataStore.edit { it[ACTIVE_SKIN_ID] = id }
    }

    suspend fun setSoundEnabled(value: Boolean) {
        context.dataStore.edit { it[SOUND_ENABLED] = value }
    }

    suspend fun setLanguageCode(code: String) {
        context.dataStore.edit { it[LANGUAGE_CODE] = code }
    }

    suspend fun setLastInterstitialTimestamp(value: Long) {
        context.dataStore.edit { it[LAST_INTERSTITIAL_TIMESTAMP] = value }
    }

    suspend fun setLastRewardedTimestamp(value: Long) {
        context.dataStore.edit { it[LAST_REWARDED_TIMESTAMP] = value }
    }
}
