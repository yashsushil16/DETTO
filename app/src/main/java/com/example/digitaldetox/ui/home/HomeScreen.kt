package com.example.digitaldetox.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.example.digitaldetox.ui.components.FloatingNavBar
import com.example.digitaldetox.ui.components.NavItem
import com.example.digitaldetox.ui.components.TreeVisualizer
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
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
        bottomBar = {
            FloatingNavBar(
                items = listOf(
                    NavItem("Home", "home", null),
                    NavItem("Detox", "detox", null),
                    NavItem("Apps", "apps", null),
                    NavItem("More", "settings", null)
                ),
                currentRoute = "home",
                onNavigate = onNavigate,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            TreeVisualizer(stage = treeStage, healthRatio = healthRatio, modifier = Modifier.fillMaxSize())
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Total Screen Time: ${formatTime(totalScreenTime)}", 
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                Text("TODAY", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    formatTime(distractionFreeTime), 
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Black)
                )
                Text("distraction-free", style = MaterialTheme.typography.bodyLarge)
                
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Time on Distractions: ${formatTime(distractiveTime)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
