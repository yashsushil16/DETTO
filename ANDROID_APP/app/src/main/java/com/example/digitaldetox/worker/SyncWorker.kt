package com.example.digitaldetox.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.digitaldetox.data.repository.AppRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        // UPDATE THIS after deploying backend to Render
        const val BACKEND_URL = "https://detto-backend.onrender.com/api/sync/push"
    }

    override suspend fun doWork(): Result {
        val database = com.example.digitaldetox.data.local.database.AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(
            database.trackedAppDao(),
            database.usageDao(),
            database.interventionDao(),
            database.gardenDao(),
            database.scheduleDao()
        )

        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                // Not logged in. Do nothing, but signify success so it reschedules normally.
                return Result.success()
            }

            val uid = user.uid
            val email = user.email ?: ""
            val phone = user.phoneNumber ?: ""

            // Gather local analytics dynamically
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = dateFormat.format(Date())
            
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            val usageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
            val stats = usageStatsManager.queryUsageStats(
                android.app.usage.UsageStatsManager.INTERVAL_DAILY, startTime, endTime
            )

            var totalTime = 0L
            stats?.forEach { stat -> totalTime += stat.totalTimeInForeground }

            val apps = repository.getAllTrackedApps().first()
            val trackedApps = apps.associateBy { it.packageName }
            
            var distractiveTime = 0L
            val appUsageMap = mutableMapOf<String, Long>()
            stats?.forEach { stat ->
                if (trackedApps.containsKey(stat.packageName)) {
                    val current = appUsageMap[stat.packageName] ?: 0L
                    appUsageMap[stat.packageName] = current + stat.totalTimeInForeground
                    distractiveTime += stat.totalTimeInForeground
                }
            }

            val totalScreenTimeMs = totalTime
            val distractiveTimeMs = distractiveTime
            val msElapsedToday = endTime - startTime
            val distractionFreeTimeMs = (msElapsedToday - distractiveTime).coerceAtLeast(0L)

            // Determine stage based on free hours (matching ViewModel logic)
            val freeHours = distractionFreeTimeMs.toFloat() / (1000f * 60f * 60f)
            val calculatedStage = when {
                freeHours < 1f -> "Seed"
                freeHours < 3f -> "Sprout"
                freeHours < 6f -> "Plant"
                freeHours < 10f -> "Tree"
                freeHours < 14f -> "Garden"
                else -> "Forest"
            }

            val pm = applicationContext.packageManager
            val appUsageList = apps.map { app ->
                val usage = appUsageMap[app.packageName] ?: 0L
                val appLabel = try {
                    val appInfo = pm.getApplicationInfo(app.packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    app.displayName.ifBlank { app.packageName }
                }
                AppUsageData(
                    packageName = app.packageName,
                    appName = appLabel,
                    usageMs = usage,
                    limitMs = app.dailyLimitMs
                )
            }

            val payload = SyncPayload(
                uid = uid,
                email = email,
                phone = phone,
                date = dateStr,
                totalScreenTimeMs = totalScreenTimeMs,
                distractiveTimeMs = distractiveTimeMs,
                distractionFreeTimeMs = distractionFreeTimeMs,
                appUsage = appUsageList,
                treeStage = calculatedStage,
                healthRatio = 1.0
            )

            val json = Gson().toJson(payload)
            val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(BACKEND_URL)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d("SyncWorker", "Data synced successfully!")
                return Result.success()
            } else {
                Log.e("SyncWorker", "Failed to sync: ${response.code}")
                return Result.retry()
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error syncing data", e)
            return Result.retry()
        }
    }

    data class AppUsageData(
        val packageName: String,
        val appName: String,
        val usageMs: Long,
        val limitMs: Long
    )

    data class SyncPayload(
        val uid: String,
        val email: String,
        val phone: String,
        val date: String,
        val totalScreenTimeMs: Long,
        val distractiveTimeMs: Long,
        val distractionFreeTimeMs: Long,
        val appUsage: List<AppUsageData>,
        val treeStage: String,
        val healthRatio: Double
    )
}
