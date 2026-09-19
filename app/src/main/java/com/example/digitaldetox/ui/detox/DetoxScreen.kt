package com.example.digitaldetox.ui.detox

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
                    NavItem("More", "settings", null)
                ),
                currentRoute = "detox",
                onNavigate = onNavigate,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (activeGame == DetoxGame.NONE) {
                GameSelector(onGameSelected = { activeGame = it })
            } else {
                when (activeGame) {
                    DetoxGame.SEQUENCE -> NumberSequenceGame(onFinish = { activeGame = DetoxGame.NONE })
                    DetoxGame.BREATHING -> BreathingGame(onFinish = { activeGame = DetoxGame.NONE })
                    DetoxGame.PATTERN -> PatternMemoryGame(onFinish = { activeGame = DetoxGame.NONE })
                    else -> Unit
                }
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
        Text("Detox Zone", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Choose an activity to reset your focus", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(48.dp))

        GameCard("Number Sequence", "Tap 1–9 in order. Breaks autopilot.", "1→9") { onGameSelected(DetoxGame.SEQUENCE) }
        Spacer(modifier = Modifier.height(16.dp))
        GameCard("Breathing Circle", "4-7-8 breathing technique. Calms the mind.", "○") { onGameSelected(DetoxGame.BREATHING) }
        Spacer(modifier = Modifier.height(16.dp))
        GameCard("Pattern Memory", "Remember and repeat a sequence of tiles.", "▦") { onGameSelected(DetoxGame.PATTERN) }
    }
}

@Composable
fun GameCard(title: String, description: String, icon: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

// ----- Game 1: Number Sequence ------
@Composable
fun NumberSequenceGame(onFinish: () -> Unit) {
    var numbers by remember { mutableStateOf((1..9).shuffled()) }
    var currentExpected by remember { mutableStateOf(1) }
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
            Text("Next: $currentExpected", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(40.dp))

        if (!gameCompleted) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.size(280.dp)
            ) {
                items(numbers) { num ->
                    val isTapped = num < currentExpected
                    Button(
                        onClick = {
                            if (num == currentExpected) {
                                currentExpected++
                                if (currentExpected > 9) gameCompleted = true
                            }
                        },
                        enabled = !isTapped,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.aspectRatio(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF333333),
                            disabledContainerColor = Color(0xFF1A1A1A)
                        )
                    ) {
                        Text(
                            if (isTapped) "✓" else num.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (isTapped) Color.DarkGray else Color.White
                        )
                    }
                }
            }
        } else {
            Text("Well done.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = {
                numbers = (1..9).shuffled(); currentExpected = 1; gameCompleted = false
            }) { Text("Play Again") }
            Button(onClick = onFinish, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) { Text("Back") }
        }
    }
}

// ----- Game 2: Breathing Circle ------
@Composable
fun BreathingGame(onFinish: () -> Unit) {
    var phase by remember { mutableStateOf("Get Ready") }
    var running by remember { mutableStateOf(false) }
    var cyclesDone by remember { mutableStateOf(0) }
    val totalCycles = 3

    val scale by animateFloatAsState(
        targetValue = if (phase == "Inhale") 1.6f else if (phase == "Hold") 1.6f else 0.8f,
        animationSpec = when (phase) {
            "Inhale" -> tween(4000)
            "Hold" -> tween(7000)
            "Exhale" -> tween(8000)
            else -> tween(500)
        }, label = "breath_scale"
    )

    LaunchedEffect(running) {
        if (running) {
            repeat(totalCycles) { cycle ->
                phase = "Inhale"; delay(4000)
                phase = "Hold"; delay(7000)
                phase = "Exhale"; delay(8000)
                cyclesDone = cycle + 1
            }
            phase = "Done"; running = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("4-7-8 Breathing", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Inhale 4s · Hold 7s · Exhale 8s", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(48.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(
                        when (phase) {
                            "Inhale" -> Color(0xFF333333)
                            "Hold" -> Color(0xFF444444)
                            "Exhale" -> Color(0xFF222222)
                            else -> Color(0xFF1A1A1A)
                        }
                    )
                    .border(3.dp, Color.White, CircleShape)
            )
            Text(
                when (phase) {
                    "Inhale" -> "Inhale..."
                    "Hold" -> "Hold..."
                    "Exhale" -> "Exhale..."
                    "Done" -> "Done."
                    else -> "Ready"
                },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (running) Text("Cycle ${cyclesDone + 1} of $totalCycles", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        else if (phase == "Done") Text("Completed $totalCycles cycles", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)

        Spacer(modifier = Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!running) OutlinedButton(onClick = {
                phase = "Get Ready"; cyclesDone = 0; running = true
            }) { Text(if (phase == "Done") "Again" else "Start") }
            Button(onClick = onFinish, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) { Text("Back") }
        }
    }
}

// ----- Game 3: Pattern Memory ------
@Composable
fun PatternMemoryGame(onFinish: () -> Unit) {
    var level by remember { mutableStateOf(3) }
    var pattern by remember { mutableStateOf(List(3) { (0 until 9).random() }) }
    var playerInput by remember { mutableStateOf(listOf<Int>()) }
    var showingPattern by remember { mutableStateOf(true) }
    var highlightIndex by remember { mutableStateOf(-1) }
    var result by remember { mutableStateOf("") } // "", "correct", "wrong"
    var score by remember { mutableStateOf(0) }

    // Show the pattern to memorize
    LaunchedEffect(pattern) {
        showingPattern = true
        highlightIndex = -1
        delay(500)
        for (i in pattern.indices) {
            highlightIndex = pattern[i]
            delay(600)
            highlightIndex = -1
            delay(300)
        }
        showingPattern = false
        playerInput = emptyList()
        result = ""
    }

    fun nextRound() {
        level++
        pattern = List(level) { (0 until 9).random() }
        score++
    }

    fun resetGame() {
        level = 3
        pattern = List(3) { (0 until 9).random() }
        score = 0
        result = ""
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Pattern Memory", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            if (showingPattern) "Watch the pattern..." else if (result == "correct") "Correct!" else if (result == "wrong") "Wrong — try again." else "Tap the tiles in the same order",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Level $level · Score: $score", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(32.dp))

        // 3x3 grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.size(260.dp)
        ) {
            items((0 until 9).toList()) { index ->
                val isHighlighted = index == highlightIndex
                val isPlayerTapped = playerInput.contains(index) && !showingPattern
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isHighlighted -> Color.White
                                isPlayerTapped -> Color(0xFF555555)
                                else -> Color(0xFF222222)
                            }
                        )
                        .border(1.dp, Color(0xFF444444), RoundedCornerShape(12.dp))
                        .clickable(enabled = !showingPattern && result.isEmpty()) {
                            val newInput = playerInput + index
                            playerInput = newInput
                            // Check so far
                            val expectedSoFar = pattern.take(newInput.size)
                            if (newInput != expectedSoFar) {
                                result = "wrong"
                            } else if (newInput.size == pattern.size) {
                                result = "correct"
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // empty tile
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (result == "correct") {
                OutlinedButton(onClick = { nextRound() }) { Text("Next Level") }
            }
            if (result == "wrong") {
                OutlinedButton(onClick = { resetGame() }) { Text("Retry") }
            }
            Button(onClick = onFinish, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) { Text("Back") }
        }
    }
}
