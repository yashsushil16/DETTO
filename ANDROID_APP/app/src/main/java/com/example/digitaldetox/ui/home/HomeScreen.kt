package com.example.digitaldetox.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.digitaldetox.ui.components.FloatingNavBar
import com.example.digitaldetox.ui.components.NavItem
import com.example.digitaldetox.ui.components.TreeVisualizer

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
        val totalMinutes = ms / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}h ${minutes}m"
    }

    Scaffold(
        bottomBar = {
            FloatingNavBar(
                items = listOf(
                    NavItem("Home", "home"),
                    NavItem("Detox", "detox"),
                    NavItem("Apps", "apps"),
                    NavItem("Stats", "analytics"),
                    NavItem("More", "settings")
                ),
                currentRoute = "home",
                onNavigate = onNavigate,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ── Distraction-Free Header ──
            Text(
                "TODAY",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Distraction-free counter
            val totalMinutes = distractionFreeTime / (1000 * 60)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            Text(
                "${hours}h ${minutes}m",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 54.sp
                )
            )
            Text(
                "distraction-free",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Sub stats row (Screen time, Distraction, Stage) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        formatTime(totalScreenTime),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "screen time",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        formatTime(distractiveTime),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (distractiveTime > 0) MaterialTheme.colorScheme.error else Color.Gray
                    )
                    Text(
                        "distracted",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        treeStage,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        "stage",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Zen Tree: Prominent, frameless, directly on screen ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                TreeVisualizer(
                    stage = treeStage,
                    healthRatio = healthRatio,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
