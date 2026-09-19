package com.example.digitaldetox.ui.home

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
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

data class TrackedAppUsage(
    val packageName: String,
    val appName: String,
    val usageMs: Long,
    val limitMs: Long
)

data class HourlyUsagePoint(
    val hour: Int,
    val hourLabel: String,
    val totalMinutes: Float,
    val distractiveMinutes: Float,
    val isCurrentHour: Boolean
)

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

    private val _globalLimitMs = MutableStateFlow(AppSettings.DEFAULT_LIMIT_MS)
    val globalLimitMs: StateFlow<Long> = _globalLimitMs.asStateFlow()

    private val _budgetRemainingMs = MutableStateFlow(0L)
    val budgetRemainingMs: StateFlow<Long> = _budgetRemainingMs.asStateFlow()

    private val _healthRatio = MutableStateFlow(1f)
    val healthRatio: StateFlow<Float> = _healthRatio.asStateFlow()

    private val _treeStage = MutableStateFlow("Tree")
    val treeStage: StateFlow<String> = _treeStage.asStateFlow()

    private val _trackedAppUsageList = MutableStateFlow<List<TrackedAppUsage>>(emptyList())
    val trackedAppUsageList: StateFlow<List<TrackedAppUsage>> = _trackedAppUsageList.asStateFlow()

    private val _hourlyUsageList = MutableStateFlow<List<HourlyUsagePoint>>(emptyList())
    val hourlyUsageList: StateFlow<List<HourlyUsagePoint>> = _hourlyUsageList.asStateFlow()

    init {
        refreshUsageStats()
    }

    fun refreshUsageStats() {
        viewModelScope.launch {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            
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

            val apps = appRepository.getAllTrackedApps().first()
            val trackedApps = apps.associateBy { it.packageName }
            val globalLimit = appSettings.appTimerLimitFlow.first()
            _globalLimitMs.value = globalLimit

            var distractiveTime = 0L
            val appUsageMap = mutableMapOf<String, Long>()
            stats?.forEach { stat ->
                if (trackedApps.containsKey(stat.packageName)) {
                    val current = appUsageMap[stat.packageName] ?: 0L
                    appUsageMap[stat.packageName] = current + stat.totalTimeInForeground
                    distractiveTime += stat.totalTimeInForeground
                }
            }

            _totalScreenTimeMs.value = totalTime
            _distractiveTimeMs.value = distractiveTime

            val msElapsedToday = endTime - startTime
            val freeMs = (msElapsedToday - distractiveTime).coerceAtLeast(0L)
            _distractionFreeTimeMs.value = freeMs
            val distractionFreeHours = freeMs.toFloat() / (1000f * 60f * 60f)

            // Remaining budget
            _budgetRemainingMs.value = (globalLimit - distractiveTime)

            // Per-app health ratio
            var totalPenalty = 0f
            var appCount = 0
            val appUsageDetailedList = mutableListOf<TrackedAppUsage>()
            val pm = context.packageManager

            apps.forEach { app ->
                val usage = appUsageMap[app.packageName] ?: 0L
                val limit = if (app.dailyLimitMs > 0L) app.dailyLimitMs else globalLimit
                if (limit > 0L) {
                    totalPenalty += usage.toFloat() / limit.toFloat()
                    appCount++
                }
                
                val appLabel = try {
                    val appInfo = pm.getApplicationInfo(app.packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    app.displayName.ifBlank { app.packageName }
                }

                appUsageDetailedList.add(
                    TrackedAppUsage(
                        packageName = app.packageName,
                        appName = appLabel,
                        usageMs = usage,
                        limitMs = limit
                    )
                )
            }

            // Sort tracked apps: most used today at top
            _trackedAppUsageList.value = appUsageDetailedList.sortedByDescending { it.usageMs }

            val avgPenalty = if (appCount > 0) totalPenalty / appCount else 0f
            val health = (1f - avgPenalty * 0.25f).coerceIn(0.7f, 1.3f)
            _healthRatio.value = health

            _treeStage.value = when {
                distractionFreeHours >= 6.0f -> "Forest"
                distractionFreeHours >= 4.0f -> "Garden"
                distractionFreeHours >= 2.0f -> "Tree"
                distractionFreeHours >= 0.75f -> "Plant"
                distractionFreeHours >= 0.25f -> "Sprout"
                else -> "Seed"
            }

            // Generate Hourly Usage Timeline points for today (12 AM to current hour)
            val hourlyPoints = mutableListOf<HourlyUsagePoint>()
            for (h in 0..currentHour.coerceAtLeast(12)) {
                val hourLabel = when {
                    h == 0 -> "12a"
                    h < 12 -> "${h}a"
                    h == 12 -> "12p"
                    else -> "${h - 12}p"
                }

                // Approximate distribution based on day's usage
                val factor = if (h in 8..currentHour) 1f else 0.2f
                val totalMinForHour = if (currentHour > 0) (totalTime / 1000f / 60f / (currentHour + 1)) * factor else 0f
                val distMinForHour = if (currentHour > 0) (distractiveTime / 1000f / 60f / (currentHour + 1)) * factor else 0f

                hourlyPoints.add(
                    HourlyUsagePoint(
                        hour = h,
                        hourLabel = hourLabel,
                        totalMinutes = totalMinForHour.coerceAtLeast(0f),
                        distractiveMinutes = distMinForHour.coerceAtLeast(0f),
                        isCurrentHour = (h == currentHour)
                    )
                )
            }
            _hourlyUsageList.value = hourlyPoints
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
