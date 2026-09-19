package com.example.digitaldetox.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.digitaldetox.ui.components.FloatingNavBar
import com.example.digitaldetox.ui.components.GlassCard
import com.example.digitaldetox.ui.components.NavItem
import com.example.digitaldetox.ui.components.TreeVisualizer
import com.example.digitaldetox.data.local.datastore.AppSettings
import com.example.digitaldetox.utils.PermissionHelper
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    appSettings: AppSettings? = null,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var strictMode by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf(true) }
    var showHowToUse by remember { mutableStateOf(true) }
    var previewStage by remember { mutableStateOf("Tree") }

    // Live permission states
    var usageGranted by remember { mutableStateOf(PermissionHelper.hasUsageStatsPermission(context)) }
    var overlayGranted by remember { mutableStateOf(PermissionHelper.hasOverlayPermission(context)) }
    var accessibilityGranted by remember { mutableStateOf(PermissionHelper.hasAccessibilityPermission(context)) }
    var batteryExemptGranted by remember {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mutableStateOf(pm.isIgnoringBatteryOptimizations(context.packageName))
    }

    // Refresh permissions on every resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageGranted = PermissionHelper.hasUsageStatsPermission(context)
                overlayGranted = PermissionHelper.hasOverlayPermission(context)
                accessibilityGranted = PermissionHelper.hasAccessibilityPermission(context)
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                batteryExemptGranted = pm.isIgnoringBatteryOptimizations(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val scope = rememberCoroutineScope()
    val initialLimit = appSettings?.appTimerLimitFlow?.collectAsState(initial = AppSettings.DEFAULT_LIMIT_MS)?.value ?: AppSettings.DEFAULT_LIMIT_MS
    var currentLimitMs by remember(initialLimit) { mutableStateOf(initialLimit) }
    val currentLimitHours = currentLimitMs / (1000f * 60 * 60)

    Scaffold(
        bottomBar = {
            FloatingNavBar(
                items = listOf(
                    NavItem("Home", "home", null),
                    NavItem("Detox", "detox", null),
                    NavItem("Apps", "apps", null),
                    NavItem("More", "settings", null)
                ),
                currentRoute = "settings",
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
            Text(
                "Settings",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Global Daily Limit ──
            Text("Distraction Budget", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Global App Limit", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                        Text(
                            "%.1fh".format(currentLimitHours),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        "Default max usage across all tracked apps per day",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = currentLimitHours,
                        onValueChange = { newHours ->
                            currentLimitMs = (newHours * 60 * 60 * 1000).toLong()
                        },
                        onValueChangeFinished = {
                            scope.launch {
                                appSettings?.setAppTimerLimit(currentLimitMs)
                            }
                        },
                        valueRange = 0.5f..5.0f,
                        steps = 8
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── App Behavior ──
            Text("Behavior", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsToggle(
                        title = "Strict Mode",
                        subtitle = "Always show intervention games before unlocking distracting apps",
                        checked = strictMode,
                        onCheckedChange = { strictMode = it }
                    )
                    SettingsToggle(
                        title = "Daily Evening Analytics",
                        subtitle = "Receive a 11:00 PM summary notification with tree status",
                        checked = notifications,
                        onCheckedChange = { notifications = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Required Permissions ──
            Text("Permissions & Background", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    PermissionItem(
                        title = "Usage Stats Access",
                        granted = usageGranted,
                        onClick = { context.startActivity(PermissionHelper.getUsageStatsIntent()) }
                    )
                    HorizontalDivider(color = Color(0xFF222222))
                    PermissionItem(
                        title = "Overlay Permission",
                        granted = overlayGranted,
                        onClick = { context.startActivity(PermissionHelper.getOverlayIntent(context)) }
                    )
                    HorizontalDivider(color = Color(0xFF222222))
                    PermissionItem(
                        title = "Accessibility Blocker Service",
                        granted = accessibilityGranted,
                        onClick = { context.startActivity(PermissionHelper.getAccessibilityIntent()) }
                    )
                    HorizontalDivider(color = Color(0xFF222222))
                    PermissionItem(
                        title = "Battery Optimization Exemption",
                        granted = batteryExemptGranted,
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── How to Use DETTO & Tree Evolution Guide ──
            Text("Guide & Tree Evolution", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showHowToUse = !showHowToUse }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (showHowToUse) "▼ How DETTO & The Tree Work" else "▶ How DETTO & The Tree Work",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    
                    if (showHowToUse) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Header
                        Text(
                            "✦ TREE EVOLUTION BY TIME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Accumulate distraction-free time today to evolve your tree:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stage items with top symbol and bottom name
                        val stageSpecs = listOf(
                            Triple("Seed", "●", "0m – 15m"),
                            Triple("Sprout", "▲", "15m – 45m"),
                            Triple("Plant", "⬡", "45m – 2h"),
                            Triple("Tree", "◈", "2h – 4h"),
                            Triple("Garden", "✦", "4h – 6h"),
                            Triple("Forest", "❖", "6h+")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            stageSpecs.forEach { (stage, symbol, _) ->
                                val isSelected = previewStage == stage
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                            else Color(0xFF181818)
                                        )
                                        .clickable { previewStage = stage }
                                        .padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        symbol,
                                        fontSize = 16.sp,
                                        color = if (isSelected) Color.White else Color(0xFF888888)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        stage,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 10.sp
                                        ),
                                        color = if (isSelected) Color.White else Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stage Information Header Card
                        val stageDetails = when (previewStage) {
                            "Seed" -> Triple(
                                "0m – 15m Distraction-Free",
                                "Germinating Seed Bed",
                                "Nestled into a zen soil bed with root tendrils and emerging cotyledon shoots, ready to bloom."
                            )
                            "Sprout" -> Triple(
                                "15m – 45m Distraction-Free",
                                "Young Shoot",
                                "A tender shoot with emerging leaves that sways lightly as your focus builds."
                            )
                            "Plant" -> Triple(
                                "45m – 2h Distraction-Free",
                                "Young Bonsai",
                                "Branch structures form and small cloud canopies begin sheltering the trunk."
                            )
                            "Tree" -> Triple(
                                "2h – 4h Distraction-Free",
                                "Majestic Zen Bonsai",
                                "Full Japanese cloud foliage, thick organic trunk, ambient aura, and glowing zen spores."
                            )
                            "Garden" -> Triple(
                                "4h – 6h Distraction-Free",
                                "Harmonic Garden",
                                "Your grand bonsai is joined by blooming companion sprouts and plants in harmony."
                            )
                            else -> Triple(
                                "6h+ Distraction-Free",
                                "Ancient Forest Sanctuary",
                                "Multiple flourishing trees and dense crowns celebrating your master discipline."
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF141414))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stageDetails.second,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF222222))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        stageDetails.first,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Tree Preview Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0A0A0A)),
                                contentAlignment = Alignment.Center
                            ) {
                                TreeVisualizer(
                                    stage = previewStage,
                                    healthRatio = 1f,
                                    modifier = Modifier.fillMaxSize().padding(6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                stageDetails.third,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFAAAAAA),
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color(0xFF222222))
                        Spacer(modifier = Modifier.height(16.dp))

                        // App Features Breakdown with clean spacing
                        Text(
                            "✦ CORE FEATURES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            HelpItem("◈ Focus to Grow", "The tree progresses through these 6 stages based on your daily distraction-free hours. Usage on tracked apps stunts its health.")
                            HelpItem("◈ App Selection", "Go to the Apps tab to choose which apps DETTO tracks. Enabled apps are automatically pinned to the top.")
                            HelpItem("◈ Per-App Timers", "Tap the timer icon on any app card to give it an individual daily limit.")
                            HelpItem("◈ Intervention Overlay", "When opening a tracked app, DETTO provides a calming mini-game to break the dopamine scroll loop.")
                            HelpItem("◈ Detox Tab", "Play Number Order, Breathing Circle (4-7-8), or Pattern Memory anytime for mindful relaxation.")
                            HelpItem("◈ 11 PM Analytics", "Receive your evening breakdown notification to reflect on today's tree status and digital wellness.")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Bottom nav clearance
        }
    }
}

@Composable
fun SettingsToggle(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun PermissionItem(title: String, granted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )
        if (granted) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1B3820))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "✓ Granted",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF66BB6A)
                )
            }
        } else {
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text("Grant", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun HelpItem(title: String, description: String) {
    Column {
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
        Spacer(modifier = Modifier.height(2.dp))
        Text(description, style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA), lineHeight = 18.sp)
    }
}
