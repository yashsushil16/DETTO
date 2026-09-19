package com.example.digitaldetox

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.digitaldetox.data.preferences.UserPreferencesRepository
import com.example.digitaldetox.data.preferences.dataStore
import com.example.digitaldetox.data.local.datastore.AppSettings
import com.example.digitaldetox.ui.navigation.DettoNavGraph
import com.example.digitaldetox.ui.theme.DettoTheme
import com.example.digitaldetox.worker.DailyAnalyticsWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        // Request battery optimization exemption so the accessibility service
        // and WorkManager tasks survive system battery killers
        requestBatteryOptimizationExemption()

        scheduleAnalyticsNotification()

        val userPreferencesRepository = UserPreferencesRepository(applicationContext.dataStore)
        val database = com.example.digitaldetox.data.local.database.AppDatabase.getDatabase(applicationContext)
        val appRepository = com.example.digitaldetox.data.repository.AppRepository(
            database.trackedAppDao(),
            database.usageDao(),
            database.interventionDao(),
            database.gardenDao(),
            database.scheduleDao()
        )
        val appSettings = AppSettings(applicationContext)

        setContent {
            DettoTheme {
                val onboardingCompleted by userPreferencesRepository.onboardingCompleted.collectAsState(initial = false)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DettoNavGraph(
                        userPreferencesRepository = userPreferencesRepository,
                        appRepository = appRepository,
                        appSettings = appSettings,
                        startDestination = if (onboardingCompleted) "home" else "onboarding"
                    )
                }
            }
        }
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fallback: open general battery settings
                    try {
                        startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    } catch (ignored: Exception) { }
                }
            }
        }
    }

    private fun scheduleAnalyticsNotification() {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyAnalyticsWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyAnalyticsWorker",
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }
}
