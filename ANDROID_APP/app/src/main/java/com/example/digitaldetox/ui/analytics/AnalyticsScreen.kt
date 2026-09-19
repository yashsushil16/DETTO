package com.example.digitaldetox.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.digitaldetox.data.local.datastore.AppSettings
import com.example.digitaldetox.data.repository.AppRepository
import com.example.digitaldetox.ui.components.TreeVisualizer
import com.example.digitaldetox.ui.home.HomeViewModel
import com.example.digitaldetox.ui.home.HomeViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigate: (String) -> Unit,
    appRepository: AppRepository? = null,
    appSettings: AppSettings? = null
) {
    val context = LocalContext.current

    val viewModel: HomeViewModel = if (appRepository != null && appSettings != null) {
        viewModel(factory = HomeViewModelFactory(appRepository, appSettings, context))
    } else {
        viewModel(factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                throw IllegalStateException("AppRepository/AppSettings not available for AnalyticsScreen")
            }
        })
    }

    val totalScreenTime by viewModel.totalScreenTimeMs.collectAsState()
    val distractiveTime by viewModel.distractiveTimeMs.collectAsState()
    val distractionFreeTime by viewModel.distractionFreeTimeMs.collectAsState()
    val treeStage by viewModel.treeStage.collectAsState()
    val healthRatio by viewModel.healthRatio.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshUsageStats()
    }

    fun formatTime(ms: Long): String {
        val minutes = (ms / (1000 * 60)) % 60
        val hours = (ms / (1000 * 60 * 60)) % 24
        return "${hours}h ${minutes}m"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Analytics", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tree
            Box(modifier = Modifier.size(220.dp)) {
                TreeVisualizer(stage = treeStage, healthRatio = healthRatio)
            }
            val healthPercent = (healthRatio * 100).toInt().coerceAtMost(150)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Stage: $treeStage", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black))
            Text(
                "Tree Health: $healthPercent%",
                style = MaterialTheme.typography.bodyLarge,
                color = if (healthPercent >= 100) Color.Gray else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text("Usage Breakdown", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatRow("Distraction-Free Time", formatTime(distractionFreeTime), highlight = true)
                    HorizontalDivider()
                    StatRow("Time on Distracting Apps", formatTime(distractiveTime))
                    HorizontalDivider()
                    StatRow("Total Screen Time", formatTime(totalScreenTime))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Improvement Tips", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))

            val tips = if (healthPercent >= 100) listOf(
                "✦ Great work! You're well under your limits today.",
                "◈ Consider lowering your limits even further to challenge yourself.",
                "◈ Use the Detox games whenever you feel the urge to scroll."
            ) else listOf(
                "✦ Move your phone to another room while working.",
                "◈ Try lowering per-app limits by 15 minutes in the Apps tab.",
                "◈ When you open a tracked app, play a Detox game first to break the habit loop.",
                "◈ Turn off push notifications from distracting apps in system settings."
            )

            tips.forEach { tip ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(tip, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onNavigate("home") }, modifier = Modifier.fillMaxWidth()) {
                Text("Return to Home")
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, highlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = if (highlight) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
    }
}
