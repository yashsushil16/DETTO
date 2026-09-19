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
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startTime, endTime
            )

            var totalTime = 0L
            stats?.forEach { stat -> totalTime += stat.totalTimeInForeground }

            // Snapshot tracked apps once — don't nest a collect inside launch
            val apps = appRepository.getAllTrackedApps().first()
            val trackedApps = apps.associateBy { it.packageName }
            val globalLimit = appSettings.appTimerLimitFlow.first()

            var distractiveTime = 0L
            stats?.filter { trackedApps.containsKey(it.packageName) }?.forEach { stat ->
                distractiveTime += stat.totalTimeInForeground
            }

            _totalScreenTimeMs.value = totalTime
            _distractiveTimeMs.value = distractiveTime

            val msElapsedToday = endTime - startTime
            val freeMs = (msElapsedToday - distractiveTime).coerceAtLeast(0L)
            _distractionFreeTimeMs.value = freeMs
            val distractionFreeHours = freeMs.toFloat() / (1000f * 60f * 60f)

            // Per-app health ratio — penalty based on distractive app limits
            var totalPenalty = 0f
            var appCount = 0
            stats?.filter { trackedApps.containsKey(it.packageName) }?.forEach { stat ->
                val app = trackedApps[stat.packageName] ?: return@forEach
                val limit = if (app.dailyLimitMs > 0L) app.dailyLimitMs.toFloat() else globalLimit.toFloat()
                totalPenalty += stat.totalTimeInForeground.toFloat() / limit
                appCount++
            }
            val avgPenalty = if (appCount > 0) totalPenalty / appCount else 0f
            var health = (1f - avgPenalty * 0.25f).coerceIn(0.7f, 1.3f)
            _healthRatio.value = health

            _treeStage.value = when {
                distractionFreeHours >= 6.0f -> "Forest"
                distractionFreeHours >= 4.0f -> "Garden"
                distractionFreeHours >= 2.0f -> "Tree"
                distractionFreeHours >= 0.75f -> "Plant"
                distractionFreeHours >= 0.25f -> "Sprout"
                else -> "Seed"
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
