package com.example.digitaldetox.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.digitaldetox.ui.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tree Growth Status", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Stage: $treeStage", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black))
            val healthPercent = (healthRatio * 100).toInt()
            Text("Health: $healthPercent%", style = MaterialTheme.typography.bodyLarge, color = if (healthPercent >= 100) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text("Usage Analytics", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Screen Time: ${formatTime(totalScreenTime)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Distraction-Free: ${formatTime(distractionFreeTime)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Time on Distractions: ${formatTime(distractiveTime)}", color = MaterialTheme.colorScheme.error)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text("Improvement Methods", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                if (healthPercent >= 100) 
                    "Great job! You stayed under your limits today. Keep up the good work and maintain this healthy habit." 
                else 
                    "You exceeded your distractive app limit today. Try moving your phone to another room or lower the limits to restrict yourself more strictly.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { onNavigate("home") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Return to Home")
            }
        }
    }
}
