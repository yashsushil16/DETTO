package com.example.digitaldetox.service

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.digitaldetox.data.local.database.AppDatabase
import com.example.digitaldetox.data.local.database.TrackedAppEntity
import com.example.digitaldetox.data.local.datastore.AppSettings
import com.example.digitaldetox.ui.intervention.InterventionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class AccessibilityBlockerService : AccessibilityService() {

    companion object {
        private const val TAG = "BlockerService"
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // Map of packageName -> TrackedAppEntity (for display name + per-app limits)
    private var trackedAppsMap = mapOf<String, TrackedAppEntity>()

    // Cached global limit (refreshed from DataStore whenever tracked apps update)
    private var globalLimitMs: Long = AppSettings.DEFAULT_LIMIT_MS

    // After the user completes the game, we let them back in once without re-intercepting
    private var lastInterceptedPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility Service Connected")
        val dao = AppDatabase.getDatabase(applicationContext).trackedAppDao()
        val appSettings = AppSettings(applicationContext)

        serviceScope.launch {
            // Keep global limit in sync
            appSettings.appTimerLimitFlow.collectLatest { limit ->
                globalLimitMs = limit
            }
        }

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
            if (packageName.startsWith("com.android.launcher")) return

            // If the user just completed the game/breathing for this app, let them back in once
            if (packageName == lastInterceptedPackage) {
                lastInterceptedPackage = null
                return
            }

            val trackedApp = trackedAppsMap[packageName] ?: return

            // ── TIME LIMIT CHECK ──────────────────────────────────────────────
            // Only show the overlay if the app (or global) time limit has been reached.
            val todayUsageMs = getTodayUsageMs(packageName)
            val appLimit = if (trackedApp.dailyLimitMs > 0L) trackedApp.dailyLimitMs else globalLimitMs

            val limitReached = appLimit > 0L && todayUsageMs >= appLimit

            Log.d(
                TAG,
                "App=${trackedApp.displayName} usageMs=$todayUsageMs limitMs=$appLimit limitReached=$limitReached"
            )

            if (!limitReached) return
            // ─────────────────────────────────────────────────────────────────

            Log.d(TAG, "Limit reached for ${trackedApp.displayName}. Launching intervention.")
            lastInterceptedPackage = packageName
            val intent = Intent(this, InterventionActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("BLOCKED_APP_PACKAGE", packageName)
                putExtra("BLOCKED_APP_NAME", trackedApp.displayName)
            }
            startActivity(intent)
        }
    }

    /**
     * Returns today's foreground usage in milliseconds for the given package,
     * using UsageStatsManager (requires PACKAGE_USAGE_STATS permission).
     */
    private fun getTodayUsageMs(packageName: String): Long {
        return try {
            val usageStatsManager =
                applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis
            val now = System.currentTimeMillis()

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startOfDay, now
            )

            stats?.filter { it.packageName == packageName }
                ?.sumOf { it.totalTimeInForeground }
                ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query usage stats for $packageName", e)
            0L
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
