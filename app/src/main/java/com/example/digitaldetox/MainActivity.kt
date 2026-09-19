package com.example.digitaldetox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.Manifest
import android.os.Build
import androidx.lifecycle.viewmodel.compose.viewModel
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
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }

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
