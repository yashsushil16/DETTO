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
    val isTracked: Boolean,
    val dailyLimitMs: Long = 0L // 0 = use global limit
)

class AppsViewModel(
    private val appRepository: AppRepository,
    private val context: Context
) : ViewModel() {

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

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
                        dailyLimitMs = entity.dailyLimitMs
                    )
                }.sortedBy { it.appName }
                
                // If installedApps is already loaded, update their tracked status too
                if (_installedApps.value.isNotEmpty()) {
                    val trackedMap = trackedList.associateBy { it.packageName }
                    _installedApps.value = _installedApps.value.map { app ->
                        app.copy(
                            isTracked = trackedMap.containsKey(app.packageName),
                            dailyLimitMs = trackedMap[app.packageName]?.dailyLimitMs ?: 0L
                        )
                    }
                }
            }
        }
    }

    fun loadAllApps() {
        if (_installedApps.value.isNotEmpty()) return // Already loaded

        viewModelScope.launch {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val trackedMap = _trackedApps.value.associateBy { it.packageName }
            
            val knownDistractive = listOf("instagram", "facebook", "reddit", "tiktok", "twitter", "snapchat", "youtube")
            
            val userApps = packages.filter { 
                (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || 
                knownDistractive.any { name -> it.packageName.contains(name) }
            }.map { appInfo ->
                AppItem(
                    packageName = appInfo.packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    isTracked = trackedMap.containsKey(appInfo.packageName),
                    dailyLimitMs = trackedMap[appInfo.packageName]?.dailyLimitMs ?: 0L
                )
            }.sortedWith(compareByDescending<AppItem> { app -> 
                knownDistractive.any { name -> app.packageName.contains(name) }
            }.thenBy { it.appName })

            _installedApps.value = userApps
        }
    }

    fun toggleAppTracking(appItem: AppItem) {
        viewModelScope.launch {
            if (appItem.isTracked) {
                appRepository.deleteTrackedApp(
                    TrackedAppEntity(
                        packageName = appItem.packageName,
                        displayName = appItem.appName,
                        isMonitored = true,
                        isRestricted = true,
                        dailyLimitMs = appItem.dailyLimitMs
                    )
                )
            } else {
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

    fun setAppDailyLimit(appItem: AppItem, limitMs: Long) {
        viewModelScope.launch {
            // Ensure the app is tracked before setting a limit; insert if needed
            appRepository.addTrackedApp(
                TrackedAppEntity(
                    packageName = appItem.packageName,
                    displayName = appItem.appName,
                    isMonitored = true,
                    isRestricted = true,
                    dailyLimitMs = limitMs
                )
            )
        }
    }
}
