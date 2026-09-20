package com.example.digitaldetox.ui.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.digitaldetox.ui.components.TreeVisualizer
import kotlinx.coroutines.delay

private val TREE_STAGES = listOf("Seed", "Sprout", "Plant", "Tree", "Garden", "Forest")

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    // Stage cycling — one stage every ~350ms so growth is visible
    var currentStageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        // Cycle through all stages smoothly
        for (i in TREE_STAGES.indices) {
            currentStageIndex = i
            delay(350L) // increased time per stage
        }
        // Linger on Forest a moment before finishing
        delay(400L)
        onFinished()
    }

    // Overall screen fade-in
    val screenAlpha by produceState(initialValue = 0f) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        ) { value, _ -> this.value = value }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha)
            .background(Color(0xFF080808)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Smaller tree placed just above the text with smooth crossfade
            AnimatedContent(
                targetState = TREE_STAGES[currentStageIndex],
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "tree_growth"
            ) { stage ->
                Box(
                    modifier = Modifier.size(180.dp), // much smaller tree size
                    contentAlignment = Alignment.Center
                ) {
                    TreeVisualizer(
                        stage = stage,
                        healthRatio = 1f,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App name + tagline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DETTO",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 10.sp,
                        color = Color(0xFFEEEEEE)
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "your digital detox garden",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        letterSpacing = 3.sp,
                        color = Color(0xFF666666)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
