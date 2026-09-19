package com.example.digitaldetox.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.digitaldetox.ui.components.FloatingNavBar
import com.example.digitaldetox.ui.components.GlassCard
import com.example.digitaldetox.ui.components.NavItem
import com.example.digitaldetox.data.local.datastore.AppSettings
import com.example.digitaldetox.utils.PermissionHelper
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    appSettings: AppSettings? = null,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    var strictMode by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf(true) }
    var showHowToUse by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val initialLimit = appSettings?.appTimerLimitFlow?.collectAsState(initial = AppSettings.DEFAULT_LIMIT_MS)?.value ?: AppSettings.DEFAULT_LIMIT_MS
    var currentLimitMs by remember(initialLimit) { mutableStateOf(initialLimit) }
    val currentLimitHours = currentLimitMs / (1000f * 60 * 60)

    val isBatteryOptimized = remember {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoring = pm.isIgnoringBatteryOptimizations(context.packageName)
        !isIgnoring
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
                currentRoute = "settings",
                onNavigate = onNavigate,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Settings", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black))
                Spacer(modifier = Modifier.height(24.dp))

                // ── Main Settings ──
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingsToggle("Strict Mode", "Prevents bypassing detox restrictions", strictMode) { strictMode = it }
                        HorizontalDivider()
                        SettingsToggle("Notifications", "Get alerts when approaching limits", notifications) { notifications = it }
                        HorizontalDivider()
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Distractive Apps Limit", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                            Text(String.format("%.1f Hours / day", currentLimitHours), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Slider(
                                value = currentLimitHours,
                                onValueChange = { currentLimitMs = (it * 60 * 60 * 1000).toLong() },
                                onValueChangeFinished = {
                                    scope.launch { appSettings?.setAppTimerLimit(currentLimitMs) }
                                },
                                valueRange = 0.5f..12f,
                                steps = 23
                            )
                            Text("Global limit for all tracked apps. Per-app limits override this.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Permissions & Background ──
                Text("Permissions & Background", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PermissionItem("Usage Access", PermissionHelper.hasUsageStatsPermission(context)) {
                            context.startActivity(PermissionHelper.getUsageStatsIntent())
                        }
                        HorizontalDivider()
                        PermissionItem("Overlay Permission", PermissionHelper.hasOverlayPermission(context)) {
                            context.startActivity(PermissionHelper.getOverlayIntent(context))
                        }
                        HorizontalDivider()
                        PermissionItem("Accessibility Service", PermissionHelper.hasAccessibilityPermission(context)) {
                            context.startActivity(PermissionHelper.getAccessibilityIntent())
                        }
                        HorizontalDivider()
                        PermissionItem("Battery Unrestricted", !isBatteryOptimized) {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── How to Use ──
                Text("Help", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        TextButton(
                            onClick = { showHowToUse = !showHowToUse },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (showHowToUse) "▼ How to Use DETTO" else "▶ How to Use DETTO",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        if (showHowToUse) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                HelpItem("🌳 The Tree", "Your home screen shows a tree that grows when you stay off distracting apps. The more you resist, the bigger it gets. Overuse shrinks it.")
                                HelpItem("📱 App Selection", "Go to the Apps tab to toggle which apps DETTO monitors. Popular apps like Instagram, TikTok, and Reddit appear at the top.")
                                HelpItem("⏱ Timers", "Tap the clock icon next to any app to set a per-app daily limit. Or use the global limit slider in Settings.")
                                HelpItem("🛑 Overlay", "When you open a tracked app, DETTO shows an intervention screen. Complete a mini-game to proceed or go back home.")
                                HelpItem("🎮 Detox Games", "In the Detox tab, play Number Sequence, Breathing Circle, or Pattern Memory anytime to calm your mind.")
                                HelpItem("📊 Analytics", "At 11 PM, you'll get a notification with your daily stats. Tap it to view your tree growth and usage breakdown.")
                                HelpItem("⚙ Background", "Grant Battery Unrestricted permission so DETTO can run reliably in the background without being killed by the system.")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(80.dp)) // Bottom nav clearance
            }
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
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
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
        Text(title, style = MaterialTheme.typography.bodyMedium)
        if (granted) {
            Text("✓", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.Gray)
        } else {
            TextButton(onClick = onClick) { Text("Grant") }
        }
    }
}

@Composable
fun HelpItem(title: String, description: String) {
    Column {
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(2.dp))
        Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}
