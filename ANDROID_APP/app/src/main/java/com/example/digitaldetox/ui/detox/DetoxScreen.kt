package com.example.digitaldetox.ui.detox

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.digitaldetox.ui.components.FloatingNavBar
import com.example.digitaldetox.ui.components.NavItem
import kotlinx.coroutines.delay

enum class DetoxGame { NONE, SEQUENCE, BREATHING, PATTERN }

@Composable
fun DetoxScreen(onNavigate: (String) -> Unit) {
    var activeGame by remember { mutableStateOf(DetoxGame.NONE) }

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
                currentRoute = "detox",
                onNavigate = onNavigate,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (activeGame) {
                DetoxGame.NONE -> GameSelector(onGameSelected = { activeGame = it })
                DetoxGame.SEQUENCE -> NumberSequenceGame(onFinish = { activeGame = DetoxGame.NONE })
                DetoxGame.BREATHING -> BreathingGame(onFinish = { activeGame = DetoxGame.NONE })
                DetoxGame.PATTERN -> PatternMemoryGame(onFinish = { activeGame = DetoxGame.NONE })
            }
        }
    }
}

@Composable
fun GameSelector(onGameSelected: (DetoxGame) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Detox Zone",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Choose an activity to reset your focus",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(48.dp))

        GameCard("Number Sequence", "Tap 1–9 in order. Breaks autopilot.", "1→9") {
            onGameSelected(DetoxGame.SEQUENCE)
        }
        Spacer(modifier = Modifier.height(16.dp))
        GameCard("Breathing Circle", "4-7-8 breathing technique. Calms the mind.", "○") {
            onGameSelected(DetoxGame.BREATHING)
        }
        Spacer(modifier = Modifier.height(16.dp))
        GameCard("Pattern Memory", "Remember and repeat a sequence of tiles.", "▦") {
            onGameSelected(DetoxGame.PATTERN)
        }
    }
}

@Composable
fun GameCard(title: String, description: String, icon: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

// ───────────── Game 1: Number Sequence ─────────────
// Layout: 3x3 grid showing numbers in random positions
// User taps them 1 to 9 in order. Key fix: use stable keys to prevent recomposition jumbling.
@Composable
fun NumberSequenceGame(onFinish: () -> Unit) {
    // Generate a fixed shuffled arrangement once
    val tileData = remember {
        // Each element: (number 1-9, gridIndex 0-8)
        val positions = (0..8).toMutableList().shuffled()
        (1..9).mapIndexed { idx, num -> Pair(num, positions[idx]) }
            .sortedBy { it.second } // sort by grid index so LazyGrid items stay stable
    }
    var nextExpected by remember { mutableStateOf(1) }
    var gameCompleted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            if (gameCompleted) "Focus Restored." else "Tap 1 to 9 in order",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (!gameCompleted) {
            Text("Next: $nextExpected", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(40.dp))

        if (!gameCompleted) {
            // Fixed 3×3 grid using simple nested Row/Column (avoids LazyGrid recomposition issues)
            val rows = tileData.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { (num, _) ->
                            val tapped = num < nextExpected
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        when {
                                            tapped -> MaterialTheme.colorScheme.surfaceVariant
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    )
                                    .clickable(enabled = !tapped && !gameCompleted) {
                                        if (num == nextExpected) {
                                            nextExpected++
                                            if (nextExpected > 9) gameCompleted = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (tapped) "✓" else num.toString(),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (tapped) Color.Gray else MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Text("Well done. 🌿", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = {
                nextExpected = 1
                gameCompleted = false
            }) { Text("Play Again") }
            Button(onClick = onFinish) { Text("Back") }
        }
    }
}

// ───────────── Game 2: Breathing Circle ─────────────
@Composable
fun BreathingGame(onFinish: () -> Unit) {
    var phase by remember { mutableStateOf("Ready") }
    var running by remember { mutableStateOf(false) }
    var cyclesDone by remember { mutableStateOf(0) }
    val totalCycles = 3

    val targetScale = when (phase) {
        "Inhale" -> 1.65f
        "Hold" -> 1.65f
        else -> 0.75f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = when (phase) {
            "Inhale" -> tween(4000, easing = LinearEasing)
            "Hold"   -> tween(100)
            "Exhale" -> tween(8000, easing = LinearEasing)
            else     -> tween(600)
        },
        label = "breath_scale"
    )

    LaunchedEffect(running) {
        if (running) {
            repeat(totalCycles) { cycle ->
                phase = "Inhale"; delay(4000)
                phase = "Hold";   delay(7000)
                phase = "Exhale"; delay(8000)
                cyclesDone = cycle + 1
            }
            phase = "Done"
            running = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "4-7-8 Breathing",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Inhale 4s · Hold 7s · Exhale 8s",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(52.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(3.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), CircleShape)
            )
            Text(
                when (phase) {
                    "Inhale" -> "Inhale..."
                    "Hold"   -> "Hold..."
                    "Exhale" -> "Exhale..."
                    "Done"   -> "Complete."
                    else     -> "Ready"
                },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            when {
                running -> "Cycle ${cyclesDone + 1} of $totalCycles"
                phase == "Done" -> "Completed $totalCycles cycles 🌿"
                else -> "Tap Start when ready"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!running) {
                OutlinedButton(onClick = {
                    cyclesDone = 0
                    phase = "Ready"
                    running = true
                }) {
                    Text(if (phase == "Done") "Again" else "Start")
                }
            }
            Button(onClick = { running = false; onFinish() }) { Text("Back") }
        }
    }
}

// ───────────── Game 3: Pattern Memory ─────────────
@Composable
fun PatternMemoryGame(onFinish: () -> Unit) {
    var level by remember { mutableStateOf(3) }
    var score by remember { mutableStateOf(0) }
    // Pattern to memorise (indices 0–8)
    var pattern by remember { mutableStateOf(generatePattern(3)) }
    var playerInput by remember { mutableStateOf(emptyList<Int>()) }
    // Phase: "showing" | "input" | "correct" | "wrong"
    var phase by remember { mutableStateOf("showing") }
    var highlightTile by remember { mutableStateOf(-1) }

    // Show the pattern whenever it changes
    LaunchedEffect(pattern) {
        phase = "showing"
        playerInput = emptyList()
        highlightTile = -1
        delay(600)
        for (idx in pattern.indices) {
            highlightTile = pattern[idx]
            delay(700)
            highlightTile = -1
            delay(300)
        }
        phase = "input"
    }

    fun onTileTap(tile: Int) {
        if (phase != "input") return
        val newInput = playerInput + tile
        playerInput = newInput
        if (newInput[newInput.lastIndex] != pattern[newInput.lastIndex]) {
            phase = "wrong"
        } else if (newInput.size == pattern.size) {
            phase = "correct"
            score++
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Pattern Memory",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            when (phase) {
                "showing" -> "Watch the pattern..."
                "input"   -> "Repeat the pattern"
                "correct" -> "Correct! ✓"
                "wrong"   -> "Wrong. Try again."
                else      -> ""
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (phase == "wrong") MaterialTheme.colorScheme.error else Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Level $level  ·  Score: $score",
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(32.dp))

        // 3×3 grid – plain Row/Column for stability
        val tileRows = (0 until 9).toList().chunked(3)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            tileRows.forEach { rowTiles ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowTiles.forEach { tile ->
                        val lit = tile == highlightTile
                        val tapped = playerInput.contains(tile) && phase == "input"
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    when {
                                        lit    -> MaterialTheme.colorScheme.onSurface
                                        tapped -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else   -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .clickable(enabled = phase == "input") { onTileTap(tile) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            when (phase) {
                "correct" -> OutlinedButton(onClick = {
                    level++
                    pattern = generatePattern(level)
                }) { Text("Next Level") }
                "wrong" -> OutlinedButton(onClick = {
                    level = 3; score = 0
                    pattern = generatePattern(3)
                }) { Text("Restart") }
            }
            Button(onClick = onFinish) { Text("Back") }
        }
    }
}

private fun generatePattern(length: Int): List<Int> = List(length) { (0 until 9).random() }
