package com.example.digitaldetox.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.digitaldetox.ui.theme.MediumGray
import com.example.digitaldetox.ui.theme.White

@Composable
fun CircularProgressMeter(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(160.dp)) {
        val strokeWidth = 12.dp.toPx()
        val radius = size.minDimension / 2 - strokeWidth
        
        // Background track
        drawCircle(
            color = MediumGray.copy(alpha = 0.3f),
            radius = radius,
            center = Offset(size.width / 2, size.height / 2),
            style = Stroke(width = strokeWidth)
        )
        
        // Progress arc
        drawArc(
            color = White,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(strokeWidth, strokeWidth),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
