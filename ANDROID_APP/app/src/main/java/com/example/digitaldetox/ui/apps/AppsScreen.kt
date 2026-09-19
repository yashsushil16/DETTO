package com.example.digitaldetox.ui.apps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.digitaldetox.data.repository.AppRepository
import com.example.digitaldetox.ui.components.FloatingNavBar
import com.example.digitaldetox.ui.components.GlassCard
import com.example.digitaldetox.ui.components.NavItem

@Composable
fun AppsScreen(
    repository: AppRepository?,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    
    val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppsViewModel(repository!!, context.applicationContext) as T
        }
    }
    
    val viewModel: AppsViewModel = if (repository != null) {
        viewModel(factory = factory)
    } else {
        viewModel()
    }
    
    val apps by viewModel.installedApps.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedApp by remember { mutableStateOf<AppItem?>(null) }
    var showTimerDialog by remember { mutableStateOf(false) }

    // Sort: tracked apps first, then alphabetically within each group
    val sortedApps = apps.sortedWith(compareByDescending<AppItem> { it.isTracked }.thenBy { it.appName })
    val filteredApps = if (searchQuery.isBlank()) sortedApps
    else sortedApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }

    // Timer Dialog
    if (showTimerDialog && selectedApp != null) {
        AppTimerDialog(
            appItem = selectedApp!!,
            onDismiss = { showTimerDialog = false },
            onConfirm = { limitMs ->
                viewModel.setAppDailyLimit(selectedApp!!, limitMs)
                // Also ensure tracked
                if (!selectedApp!!.isTracked) {
                    viewModel.toggleAppTracking(selectedApp!!)
                }
                showTimerDialog = false
            }
        )
    }

    Scaffold(
        bottomBar = {
            FloatingNavBar(
                items = listOf(
                    NavItem("Home", "home", null),
                    NavItem("Detox", "detox", null),
                    NavItem("Apps", "apps", null),
                    NavItem("Stats", "analytics", null),
                    NavItem("More", "settings", null)
                ),
                currentRoute = "apps",
                onNavigate = onNavigate,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Monitored Apps", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Toggle tracking. Tap ⏱ to set a custom daily limit.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredApps, key = { it.packageName }) { appItem ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(appItem.appName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                    if (appItem.isTracked && appItem.dailyLimitMs > 0L) {
                                        val h = appItem.dailyLimitMs / (1000 * 60 * 60)
                                        val m = (appItem.dailyLimitMs / (1000 * 60)) % 60
                                        Text(
                                            "Limit: ${if (h > 0) "${h}h " else ""}${m}m",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else if (appItem.isTracked) {
                                        Text(
                                            "Using global limit",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Timer button
                                TextButton(
                                    onClick = {
                                        selectedApp = appItem
                                        showTimerDialog = true
                                    }
                                ) {
                                    Text(
                                        "⏱",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = if (appItem.dailyLimitMs > 0L && appItem.isTracked)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Switch(
                                    checked = appItem.isTracked,
                                    onCheckedChange = { viewModel.toggleAppTracking(appItem) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppTimerDialog(
    appItem: AppItem,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var hours by remember { mutableStateOf(if (appItem.dailyLimitMs > 0) (appItem.dailyLimitMs / 3_600_000).toString() else "0") }
    var minutes by remember { mutableStateOf(if (appItem.dailyLimitMs > 0) ((appItem.dailyLimitMs / 60_000) % 60).toString() else "30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Daily Limit", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("App: ${appItem.appName}", style = MaterialTheme.typography.bodyMedium)
                Text("Set the maximum daily usage time. If exceeded, the tree will shrink.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { if (it.length <= 2) hours = it.filter { c -> c.isDigit() } },
                        label = { Text("Hours") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Text(":", style = MaterialTheme.typography.headlineSmall)
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { if (it.length <= 2) minutes = it.filter { c -> c.isDigit() } },
                        label = { Text("Minutes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val h = hours.toLongOrNull() ?: 0L
                val m = minutes.toLongOrNull() ?: 0L
                val limitMs = (h * 3_600_000L) + (m * 60_000L)
                onConfirm(limitMs)
            }) {
                Text("Set Limit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
