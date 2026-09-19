package com.example.digitaldetox.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode") // "Dark", "Light", "System"
        val STRICTNESS_LEVEL = stringPreferencesKey("strictness_level")
        val USAGE_ACCESS_GRANTED = booleanPreferencesKey("usage_access_granted")
        val ACCESSIBILITY_GRANTED = booleanPreferencesKey("accessibility_granted")
    }

    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }
    val themeMode: Flow<String> = dataStore.data.map { it[THEME_MODE] ?: "Dark" }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }
    
    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[THEME_MODE] = mode }
    }
}
