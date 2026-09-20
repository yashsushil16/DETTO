package com.example.digitaldetox.ui.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitaldetox.data.local.database.TrackedAppEntity
import com.example.digitaldetox.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppItem(
    val packageName: String,
    val appName: String,
    /** True = this app is in the user's pinned/selected list */
    val isTracked: Boolean,
    /** True = monitoring/blocking is active for this app */
    val isMonitored: Boolean,
    val dailyLimitMs: Long = 0L
)

class AppsViewModel(
    private val appRepository: AppRepository,
    private val context: Context
) : ViewModel() {

    // Full installed apps list — loaded lazily only when Add Apps dialog opens
    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    // Only apps in the user's pinned list (in DB) — shown in the main Apps screen
    private val _trackedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val trackedApps: StateFlow<List<AppItem>> = _trackedApps.asStateFlow()

    init {
        viewModelScope.launch {
            appRepository.getAllTrackedApps().collect { trackedList ->
                _trackedApps.value = trackedList.map { entity ->
                    AppItem(
                        packageName = entity.packageName,
                        appName = entity.displayName,
                        isTracked = true,
                        isMonitored = entity.isMonitored,
                        dailyLimitMs = entity.dailyLimitMs
                    )
                }.sortedBy { it.appName }

                // Keep the full app list in sync if it's already loaded
                if (_installedApps.value.isNotEmpty()) {
                    val trackedMap = trackedList.associateBy { it.packageName }
                    _installedApps.value = _installedApps.value.map { app ->
                        app.copy(
                            isTracked = trackedMap.containsKey(app.packageName),
                            isMonitored = trackedMap[app.packageName]?.isMonitored ?: false,
                            dailyLimitMs = trackedMap[app.packageName]?.dailyLimitMs ?: 0L
                        )
                    }
                }
            }
        }
    }

    /** Load all installed apps — called lazily when the Add Apps dialog opens */
    fun loadAllApps() {
        if (_installedApps.value.isNotEmpty()) return

        viewModelScope.launch {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val trackedMap = _trackedApps.value.associateBy { it.packageName }

            val knownDistractive = listOf(
                "instagram", "facebook", "reddit", "tiktok",
                "twitter", "snapchat", "youtube"
            )

            val userApps = packages.filter {
                (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                    knownDistractive.any { name -> it.packageName.contains(name) }
            }.map { appInfo ->
                AppItem(
                    packageName = appInfo.packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    isTracked = trackedMap.containsKey(appInfo.packageName),
                    isMonitored = trackedMap[appInfo.packageName]?.isMonitored ?: false,
                    dailyLimitMs = trackedMap[appInfo.packageName]?.dailyLimitMs ?: 0L
                )
            }.sortedWith(
                compareByDescending<AppItem> { app ->
                    knownDistractive.any { name -> app.packageName.contains(name) }
                }.thenBy { it.appName }
            )

            _installedApps.value = userApps
        }
    }

    /**
     * Used in the ADD APPS dialog.
     * Adds the app to the pinned list (DB), or removes it entirely.
     */
    fun toggleAppInList(appItem: AppItem) {
        viewModelScope.launch {
            if (appItem.isTracked) {
                // Remove from DB entirely
                appRepository.deleteTrackedApp(
                    TrackedAppEntity(
                        packageName = appItem.packageName,
                        displayName = appItem.appName,
                        isMonitored = appItem.isMonitored,
                        isRestricted = true,
                        dailyLimitMs = appItem.dailyLimitMs
                    )
                )
            } else {
                // Add to DB with monitoring enabled by default
                appRepository.addTrackedApp(
                    TrackedAppEntity(
                        packageName = appItem.packageName,
                        displayName = appItem.appName,
                        isMonitored = true,
                        isRestricted = true,
                        dailyLimitMs = appItem.dailyLimitMs
                    )
                )
            }
        }
    }

    /**
     * Used in the MAIN Apps screen.
     * Keeps the app in the pinned list but flips its monitoring on/off.
     */
    fun toggleMonitoring(appItem: AppItem) {
        viewModelScope.launch {
            appRepository.updateTrackedApp(
                TrackedAppEntity(
                    packageName = appItem.packageName,
                    displayName = appItem.appName,
                    isMonitored = !appItem.isMonitored,
                    isRestricted = true,
                    dailyLimitMs = appItem.dailyLimitMs
                )
            )
        }
    }

    fun setAppDailyLimit(appItem: AppItem, limitMs: Long) {
        viewModelScope.launch {
            appRepository.addTrackedApp(
                TrackedAppEntity(
                    packageName = appItem.packageName,
                    displayName = appItem.appName,
                    isMonitored = appItem.isMonitored,
                    isRestricted = true,
                    dailyLimitMs = limitMs
                )
            )
        }
    }
}
