package com.example.viralclicker.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "viral_clicker_settings")

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        val NO_ADS_PURCHASED = booleanPreferencesKey("no_ads_purchased")
        val ACTIVE_SKIN_ID = stringPreferencesKey("active_skin_id")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    }

    val noAdsPurchased: Flow<Boolean> = context.dataStore.data.map { it[NO_ADS_PURCHASED] ?: false }
    val activeSkinId: Flow<String> = context.dataStore.data.map { it[ACTIVE_SKIN_ID] ?: "default" }
    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { it[SOUND_ENABLED] ?: true }

    suspend fun setNoAdsPurchased(value: Boolean) {
        context.dataStore.edit { it[NO_ADS_PURCHASED] = value }
    }

    suspend fun setActiveSkinId(id: String) {
        context.dataStore.edit { it[ACTIVE_SKIN_ID] = id }
    }

    suspend fun setSoundEnabled(value: Boolean) {
        context.dataStore.edit { it[SOUND_ENABLED] = value }
    }
}
