package com.example.digitaldetox.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.digitaldetox.data.preferences.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppSettings(private val context: Context) {
    companion object {
        val APP_TIMER_LIMIT_MS = longPreferencesKey("app_timer_limit_ms")
        const val DEFAULT_LIMIT_MS = 2L * 60L * 60L * 1000L // 2 hours
    }

    val appTimerLimitFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[APP_TIMER_LIMIT_MS] ?: DEFAULT_LIMIT_MS
        }

    suspend fun setAppTimerLimit(limitMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[APP_TIMER_LIMIT_MS] = limitMs
        }
    }
}
