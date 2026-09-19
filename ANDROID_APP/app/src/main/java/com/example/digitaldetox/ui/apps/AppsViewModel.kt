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

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            
            // Get all currently tracked apps from DB
            appRepository.getAllTrackedApps().collect { trackedList ->
                val trackedMap = trackedList.associateBy { it.packageName }
                
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
