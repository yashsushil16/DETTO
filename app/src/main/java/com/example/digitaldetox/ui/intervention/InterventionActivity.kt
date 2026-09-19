package com.example.digitaldetox.ui.intervention

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.digitaldetox.ui.components.TreeVisualizer
import com.example.digitaldetox.ui.theme.DettoTheme

class InterventionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val blockedPackage = intent.getStringExtra("BLOCKED_APP_PACKAGE") ?: "App"
        val blockedAppName = intent.getStringExtra("BLOCKED_APP_NAME") ?: blockedPackage

        setContent {
            DettoTheme {
                var gameCompleted by remember { mutableStateOf(false) }
                val numbers = remember { (1..9).shuffled() }
                var currentExpected by remember { mutableStateOf(1) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.98f)), 
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Interrupt Autopilot.",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (gameCompleted) "Focus restored." else "Tap 1–9 in order to unlock $blockedAppName",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        if (!gameCompleted) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.size(240.dp)
                            ) {
                                items(numbers) { num ->
                                    val isTapped = num < currentExpected
                                    Button(
                                        onClick = { 
                                            if (num == currentExpected) {
                                                currentExpected++
                                                if (currentExpected > 9) {
                                                    gameCompleted = true
                                                }
                                            }
                                        },
                                        enabled = !isTapped,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.aspectRatio(1f)
                                    ) {
                                        Text(
                                            text = if (isTapped) "✓" else num.toString(), 
                                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.size(200.dp)) {
                                TreeVisualizer(stage = "Tree")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        if (gameCompleted) {
                            Button(
                                onClick = { finish() }, 
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text("Continue to $blockedAppName")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        Button(
                            onClick = {
                                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_HOME)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                startActivity(homeIntent)
                                finish()
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(if (gameCompleted) "Close App Instead" else "Nevermind, Close App")
                        }
                    }
                }
            }
        }
    }
}
