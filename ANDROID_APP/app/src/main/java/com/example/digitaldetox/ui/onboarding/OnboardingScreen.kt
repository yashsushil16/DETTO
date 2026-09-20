package com.example.digitaldetox.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.digitaldetox.data.preferences.UserPreferencesRepository
import com.example.digitaldetox.utils.PermissionHelper
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    repository: UserPreferencesRepository?,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasUsageStats by remember { mutableStateOf(PermissionHelper.hasUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(PermissionHelper.hasOverlayPermission(context)) }
    var hasAccessibility by remember { mutableStateOf(PermissionHelper.hasAccessibilityPermission(context)) }
    
    // 0..3 = tutorial pages, 4 = permissions, 5 = done
    var currentPage by remember { mutableStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageStats = PermissionHelper.hasUsageStatsPermission(context)
                hasOverlay = PermissionHelper.hasOverlayPermission(context)
                hasAccessibility = PermissionHelper.hasAccessibilityPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val allPermissionsGranted = hasUsageStats && hasOverlay && hasAccessibility

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            when (currentPage) {
                0 -> TutorialPage(
                    title = "Welcome to DETTO",
                    subtitle = "Your digital detox companion",
                    items = listOf(
                        "✿ A tree grows on your home screen — it thrives when you stay off distracting apps.",
                        "↟ The more you use tracked apps, the smaller the tree gets. Stay focused and watch it flourish.",
                        "↟ Your main timer shows distraction-free time: hours in the day you haven't spent on tracked apps."
                    )
                )
                1 -> TutorialPage(
                    title = "App Selection & Timers",
                    subtitle = "Customize what to limit",
                    items = listOf(
                        "✿ In the Apps tab, toggle any app on/off to track it.",
                        "↟ Tap the clock icon on any app to set a per-app daily limit (e.g. 30 min for Instagram).",
                        "↟ Use the search bar to find apps quickly.",
                        "↟ In Settings → More, set a global daily limit for all tracked apps combined."
                    )
                )
                2 -> TutorialPage(
                    title = "Overlay & Games",
                    subtitle = "When you open a tracked app...",
                    items = listOf(
                        "✿ DETTO pops up an intervention screen before you can use the app.",
                        "↟ You must complete a mini-game (like tapping numbers 1–9 in order) to proceed.",
                        "↟ In the Detox tab, play calming games anytime: Breathing Circle, Number Sequence, Pattern Memory.",
                        "↟ After completing the game, you can choose to continue to the app or go home."
                    )
                )
                3 -> TutorialPage(
                    title = "Analytics & Growth",
                    subtitle = "Track your progress",
                    items = listOf(
                        "✿ At 11 PM each night, you'll receive a notification with your daily analytics.",
                        "↟ Your tree progresses through stages: Seed → Sprout → Plant → Tree → Garden → Forest.",
                        "↟ The Analytics screen shows total screen time, distraction-free time, and improvement tips.",
                        "↟ The less you use distracting apps, the healthier and larger your tree becomes!"
                    )
                )
                4 -> {
                    // Permissions page
                    Text(
                        "Permissions Required",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "DETTO needs these permissions to work properly.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    PermissionRow("Usage Access", "Know which apps you use", hasUsageStats) {
                        context.startActivity(PermissionHelper.getUsageStatsIntent())
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    PermissionRow("Overlay Permission", "Show intervention over apps", hasOverlay) {
                        context.startActivity(PermissionHelper.getOverlayIntent(context))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    PermissionRow("Accessibility Service", "Detect when tracked apps open", hasAccessibility) {
                        context.startActivity(PermissionHelper.getAccessibilityIntent())
                    }

                    if (!hasAccessibility) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "For Accessibility: find \"DETTO\" or \"Digital Detox\" in the list and toggle it ON.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }
                5 -> {
                    Text(
                        "You're All Set!",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Let's build healthier digital habits.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Navigation buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (currentPage > 0) {
                    OutlinedButton(onClick = { currentPage-- }) {
                        Text("Back")
                    }
                }
                if (currentPage < 4) {
                    Button(onClick = { currentPage++ }) {
                        Text("Next")
                    }
                } else if (currentPage == 4 && allPermissionsGranted) {
                    Button(onClick = { currentPage = 5 }) {
                        Text("Continue")
                    }
                } else if (currentPage == 5) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                repository?.setOnboardingCompleted(true)
                                onFinish()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enter DETTO")
                    }
                }
            }

            // Page indicator
            Spacer(modifier = Modifier.height(16.dp))
            Text("${currentPage + 1} / 6", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        }
    }
}

@Composable
fun TutorialPage(title: String, subtitle: String, items: List<String>) {
    Text(
        title,
        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        subtitle,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = Color.Gray
    )
    Spacer(modifier = Modifier.height(32.dp))
    items.forEach { item ->
        Text(
            item,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
    }
}

@Composable
fun PermissionRow(title: String, description: String, granted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        if (granted) {
            Text("✓ Granted", color = Color.Gray, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
        } else {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
            ) {
                Text("Grant")
            }
        }
    }
}
