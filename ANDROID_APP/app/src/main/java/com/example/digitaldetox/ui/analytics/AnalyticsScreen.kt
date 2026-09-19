package com.example.digitaldetox.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.digitaldetox.data.local.datastore.AppSettings
import com.example.digitaldetox.data.repository.AppRepository
import com.example.digitaldetox.ui.components.FloatingNavBar
import com.example.digitaldetox.ui.components.GlassCard
import com.example.digitaldetox.ui.components.NavItem
import com.example.digitaldetox.ui.components.TreeVisualizer
import com.example.digitaldetox.ui.home.HomeViewModel
import com.example.digitaldetox.ui.home.HomeViewModelFactory
import com.example.digitaldetox.ui.home.HourlyUsagePoint
import com.example.digitaldetox.ui.home.TrackedAppUsage

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
        viewModel()
    }

    val totalScreenTime by viewModel.totalScreenTimeMs.collectAsState()
    val distractiveTime by viewModel.distractiveTimeMs.collectAsState()
    val distractionFreeTime by viewModel.distractionFreeTimeMs.collectAsState()
    val globalLimitMs by viewModel.globalLimitMs.collectAsState()
    val budgetRemainingMs by viewModel.budgetRemainingMs.collectAsState()
    val treeStage by viewModel.treeStage.collectAsState()
    val healthRatio by viewModel.healthRatio.collectAsState()
    val trackedAppsUsage by viewModel.trackedAppUsageList.collectAsState()
    val hourlyList by viewModel.hourlyUsageList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshUsageStats()
    }

    fun formatDuration(ms: Long): String {
        val totalMinutes = ms / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
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
                currentRoute = "analytics",
                onNavigate = onNavigate,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── Screen Header ──
            Text(
                "Analytics",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "Live focus and app breakdown for today",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Primary Time Overview Card ──
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "✦ DISTRACTION-FREE TODAY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        formatDuration(distractionFreeTime),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 44.sp
                        )
                    )
                    Text(
                        "Time spent off distracting apps today",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF222222))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Screen Time", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                formatDuration(totalScreenTime),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Distraction Time", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                formatDuration(distractiveTime),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (distractiveTime > 0) MaterialTheme.colorScheme.error else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Distraction Budget Remaining
                    val budgetUsedFraction = if (globalLimitMs > 0) {
                        (distractiveTime.toFloat() / globalLimitMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Daily Distraction Budget", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(
                                if (budgetRemainingMs >= 0) "${formatDuration(budgetRemainingMs)} left" else "Exceeded by ${formatDuration(-budgetRemainingMs)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (budgetRemainingMs >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        // Clean progress track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF222222))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(budgetUsedFraction)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (budgetUsedFraction >= 1f) MaterialTheme.colorScheme.error
                                        else if (budgetUsedFraction > 0.75f) Color(0xFFFFA726)
                                        else Color(0xFF66BB6A)
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Tree Stage & Status Card ──
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A0A0A)),
                        contentAlignment = Alignment.Center
                    ) {
                        TreeVisualizer(stage = treeStage, healthRatio = healthRatio, modifier = Modifier.fillMaxSize().padding(4.dp))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "◈ $treeStage Stage",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val healthPercent = (healthRatio * 100).toInt().coerceAtMost(150)
                        Text(
                            "Vitality: $healthPercent%",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (healthPercent >= 95) Color(0xFF66BB6A) else Color(0xFFFFA726)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Stage progression hint
                        val stageHint = when (treeStage) {
                            "Seed" -> "Reach 15m to evolve into Sprout"
                            "Sprout" -> "Reach 45m to evolve into Plant"
                            "Plant" -> "Reach 2h to evolve into Tree"
                            "Tree" -> "Reach 4h to evolve into Garden"
                            "Garden" -> "Reach 6h to evolve into Ancient Forest"
                            else -> "Master Sanctuary achieved"
                        }
                        Text(
                            stageHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Hourly Activity Timeline Chart ──
            Text("Activity Timeline (Today)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Hourly screen distribution (12 AM – Now)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (hourlyList.isNotEmpty()) {
                        HourlyChartCanvas(hourlyList = hourlyList, modifier = Modifier.fillMaxWidth().height(120.dp))
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Usage distribution recording in progress...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF888888)))
                            Text("Screen Time", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                            Text("Active Focus", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Selected Monitored Apps (Till Now Today) ──
            Text("Monitored Apps Today", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))

            if (trackedAppsUsage.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No tracked apps configured yet", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { onNavigate("apps") }) {
                            Text("Configure Tracked Apps")
                        }
                    }
                }
            } else {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        trackedAppsUsage.forEachIndexed { index, app ->
                            AppUsageRow(app = app, formatDuration = ::formatDuration)
                            if (index < trackedAppsUsage.size - 1) {
                                HorizontalDivider(color = Color(0xFF1E1E1E))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Mindful Focus Insights ──
            Text("Focus Recommendations", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val isHealthy = distractiveTime <= globalLimitMs
                    if (isHealthy) {
                        InsightItem("✦ Strong Self-Discipline", "You are comfortably within your daily distraction limits. Your tree continues to flourish.")
                        InsightItem("◈ Evening Reset", "Use the Breathing Circle in the Detox tab before bed to unwind without screen glare.")
                    } else {
                        InsightItem("✦ Limit Exceeded", "Distraction time went over your budget. Play a Detox mini-game next time you open a tracked app.")
                        InsightItem("◈ Per-App Limits", "Consider setting a stricter limit on your top-used app in the Apps tab.")
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Bottom nav clearance
        }
    }
}

@Composable
fun AppUsageRow(
    app: TrackedAppUsage,
    formatDuration: (Long) -> String
) {
    val usedFraction = if (app.limitMs > 0L) {
        (app.usageMs.toFloat() / app.limitMs.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val isOverLimit = app.limitMs > 0L && app.usageMs > app.limitMs

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Monochromatic App Icon Badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF222222)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        app.appName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Column {
                    Text(
                        app.appName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Text(
                        if (app.limitMs > 0L) "Limit: ${formatDuration(app.limitMs)}" else "No limit set",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatDuration(app.usageMs),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isOverLimit) MaterialTheme.colorScheme.error else Color.White
                )
                Text(
                    if (isOverLimit) "Over limit"
                    else if (app.limitMs > 0L) "${formatDuration((app.limitMs - app.usageMs).coerceAtLeast(0L))} left"
                    else "Tracked",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOverLimit) MaterialTheme.colorScheme.error else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF1E1E1E))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(usedFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isOverLimit) MaterialTheme.colorScheme.error
                        else if (usedFraction > 0.8f) Color(0xFFFFA726)
                        else Color(0xFFB0B0B0)
                    )
            )
        }
    }
}

@Composable
fun HourlyChartCanvas(
    hourlyList: List<HourlyUsagePoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (hourlyList.isEmpty()) return@Canvas

        val maxVal = (hourlyList.maxOfOrNull { it.totalMinutes } ?: 10f).coerceAtLeast(10f)
        val barCount = hourlyList.size
        val availableWidth = size.width
        val barSpacing = 4.dp.toPx()
        val totalSpacing = barSpacing * (barCount - 1)
        val barWidth = ((availableWidth - totalSpacing) / barCount).coerceIn(4f, 24f)

        hourlyList.forEachIndexed { index, point ->
            val x = index * (barWidth + barSpacing)
            val barHeight = (point.totalMinutes / maxVal) * (size.height * 0.75f)
            val topY = size.height - barHeight - 16.dp.toPx()

            // Draw base bar
            drawRoundRect(
                color = if (point.isCurrentHour) Color(0xFFFFFFFF) else Color(0xFF333333),
                topLeft = Offset(x, topY),
                size = Size(barWidth, barHeight.coerceAtLeast(4f)),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // If distractive usage exists in this hour, overlay highlight
            if (point.distractiveMinutes > 0f) {
                val distHeight = (point.distractiveMinutes / maxVal) * (size.height * 0.75f)
                val distTopY = size.height - distHeight - 16.dp.toPx()
                drawRoundRect(
                    color = Color(0xFFE57373),
                    topLeft = Offset(x, distTopY),
                    size = Size(barWidth, distHeight.coerceAtLeast(4f)),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun InsightItem(title: String, description: String) {
    Column {
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
        Spacer(modifier = Modifier.height(2.dp))
        Text(description, style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA), lineHeight = 18.sp)
    }
}
