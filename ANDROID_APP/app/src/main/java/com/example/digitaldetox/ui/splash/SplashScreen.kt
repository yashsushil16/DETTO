package com.example.digitaldetox.ui.splash

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
    // Stage cycling — one stage every ~220ms so all 6 cycle in ~1.3s
    var currentStageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        // Cycle through all stages rapidly
        for (i in TREE_STAGES.indices) {
            currentStageIndex = i
            delay(220L)
        }
        // Linger on Forest a moment before finishing
        delay(300L)
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
            // Tree cycling zone — takes up upper 55% of screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.58f),
                contentAlignment = Alignment.Center
            ) {
                TreeVisualizer(
                    stage = TREE_STAGES[currentStageIndex],
                    healthRatio = 1f,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // App name + tagline — lower portion
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.42f),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 8.dp)
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
                        text = "your digital garden",
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
}
