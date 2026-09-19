package com.example.digitaldetox.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.digitaldetox.data.local.database.AppDatabase
import com.example.digitaldetox.data.local.database.TrackedAppEntity
import com.example.digitaldetox.ui.intervention.InterventionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AccessibilityBlockerService : AccessibilityService() {
    
    companion object {
        private const val TAG = "BlockerService"
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    // Map of packageName -> TrackedAppEntity (for display name + limits)
    private var trackedAppsMap = mapOf<String, TrackedAppEntity>()
    private var lastInterceptedPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility Service Connected")
        val dao = AppDatabase.getDatabase(applicationContext).trackedAppDao()
        serviceScope.launch {
            dao.getAllTrackedApps().collectLatest { apps ->
                trackedAppsMap = apps.associateBy { it.packageName }
                Log.d(TAG, "Updated tracked apps: ${trackedAppsMap.keys}")
            }
        }
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Ignore our own package and system UI
            if (packageName == this.packageName) return
            if (packageName == "com.android.systemui") return
            if (packageName == "com.android.launcher") return
            
            // If the user just completed the game for this app, don't re-intercept
            if (packageName == lastInterceptedPackage) {
                lastInterceptedPackage = null
                return
            }
            
            val trackedApp = trackedAppsMap[packageName]
            if (trackedApp != null) {
                Log.d(TAG, "Intercepting ${trackedApp.displayName} ($packageName)! Launching Detox.")
                lastInterceptedPackage = packageName
                val intent = Intent(this, InterventionActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra("BLOCKED_APP_PACKAGE", packageName)
                    putExtra("BLOCKED_APP_NAME", trackedApp.displayName)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
