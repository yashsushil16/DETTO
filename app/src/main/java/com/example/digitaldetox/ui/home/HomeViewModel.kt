package com.example.digitaldetox.ui.home

import android.app.usage.UsageStatsManager
import android.app.usage.UsageStats
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitaldetox.data.repository.AppRepository
import com.example.digitaldetox.data.local.datastore.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val appRepository: AppRepository,
    private val appSettings: AppSettings,
    private val context: Context
) : ViewModel() {

    private val _totalScreenTimeMs = MutableStateFlow(0L)
    val totalScreenTimeMs: StateFlow<Long> = _totalScreenTimeMs.asStateFlow()

    private val _distractiveTimeMs = MutableStateFlow(0L)
    val distractiveTimeMs: StateFlow<Long> = _distractiveTimeMs.asStateFlow()

    private val _distractionFreeTimeMs = MutableStateFlow(0L)
    val distractionFreeTimeMs: StateFlow<Long> = _distractionFreeTimeMs.asStateFlow()

    private val _healthRatio = MutableStateFlow(1f)
    val healthRatio: StateFlow<Float> = _healthRatio.asStateFlow()

    private val _treeStage = MutableStateFlow("Tree")
    val treeStage: StateFlow<String> = _treeStage.asStateFlow()

    init {
        refreshUsageStats()
    }

    fun refreshUsageStats() {
        viewModelScope.launch {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            
            var totalTime = 0L
            if (stats != null) {
                for (stat in stats) {
                    totalTime += stat.totalTimeInForeground
                }
            }
            
            appRepository.getAllTrackedApps().collect { apps ->
                var distractiveTime = 0L
                val trackedApps = apps.associateBy { it.packageName }
                val globalLimit = appSettings.appTimerLimitFlow.first()

                if (stats != null) {
                    distractiveTime = stats
                        .filter { stat: UsageStats -> trackedApps.containsKey(stat.packageName) }
                        .sumOf { stat: UsageStats -> stat.totalTimeInForeground }
                }

                _totalScreenTimeMs.value = totalTime
                _distractiveTimeMs.value = distractiveTime

                // Distraction-free = hours elapsed today minus distractive app time
                val msElapsedToday = endTime - startTime
                val freeTime = (msElapsedToday - distractiveTime).coerceAtLeast(0L)
                _distractionFreeTimeMs.value = freeTime

                // Health ratio: for each tracked app, check its individual limit or fall back to global
                // Sum weighted penalty across all tracked apps
                var totalPenalty = 0f
                var appCount = 0
                if (stats != null) {
                    for (stat in stats.filter { trackedApps.containsKey(it.packageName) }) {
                        val app = trackedApps[stat.packageName] ?: continue
                        val limitForApp = if (app.dailyLimitMs > 0L) app.dailyLimitMs.toFloat() else globalLimit.toFloat()
                        val penalty = stat.totalTimeInForeground.toFloat() / limitForApp
                        totalPenalty += penalty
                        appCount++
                    }
                }
                val avgPenalty = if (appCount > 0) totalPenalty / appCount else 0f

                var health = 1f - (avgPenalty * 0.8f)
                if (health < 0.2f) health = 0.2f
                if (health > 1.5f) health = 1.5f

                _healthRatio.value = health

                _treeStage.value = when {
                    health >= 1.2f -> "Forest"
                    health >= 1.0f -> "Garden"
                    health >= 0.8f -> "Tree"
                    health >= 0.5f -> "Plant"
                    health >= 0.3f -> "Sprout"
                    else -> "Seed"
                }
            }
        }
    }
}

class HomeViewModelFactory(
    private val appRepository: AppRepository,
    private val appSettings: AppSettings,
    private val context: Context
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(appRepository, appSettings, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
