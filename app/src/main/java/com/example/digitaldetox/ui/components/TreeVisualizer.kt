package com.example.digitaldetox.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import kotlin.math.cos
import kotlin.math.sin

// Greyscale palette matching the reference image
private val TRUNK_DARK = Color(0xFF6B6B6B)
private val TRUNK_MID = Color(0xFF808080)
private val TRUNK_LIGHT = Color(0xFF999999)
private val CANOPY_DARKEST = Color(0xFF8A8A8A)
private val CANOPY_DARK = Color(0xFF9E9E9E)
private val CANOPY_MID = Color(0xFFB0B0B0)
private val CANOPY_LIGHT = Color(0xFFC8C8C8)
private val CANOPY_LIGHTEST = Color(0xFFDCDCDC)
private val LEAF_HIGHLIGHT = Color(0xFFE8E8E8)

@Composable
fun TreeVisualizer(
    stage: String,
    healthRatio: Float = 1f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sway")
    val swayAngle by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "sway_anim"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val rootX = size.width / 2
        val rootY = size.height * 0.88f
        val hr = healthRatio.coerceIn(0.2f, 1.5f)

        when (stage) {
            "Seed" -> drawDetailedSeed(rootX, rootY, hr)
            "Sprout" -> drawDetailedSprout(rootX, rootY, swayAngle, hr)
            "Plant" -> drawDetailedPlant(rootX, rootY, swayAngle, hr)
            "Tree" -> drawDetailedTree(rootX, rootY, swayAngle, hr, 1f)
            "Garden" -> drawDetailedGarden(rootX, rootY, swayAngle, hr)
            "Forest" -> drawDetailedForest(rootX, rootY, swayAngle, hr)
            else -> drawDetailedSeed(rootX, rootY, hr)
        }
    }
}

// ── Stage 1: Seed ──
private fun DrawScope.drawDetailedSeed(x: Float, y: Float, hr: Float) {
    val s = hr * 1.2f
    // Tiny stem
    drawLine(TRUNK_DARK, Offset(x, y), Offset(x, y - 18f * s), strokeWidth = 4f * s, cap = StrokeCap.Round)
    // Two tiny seed leaves (cotyledons)
    val leafPath1 = Path().apply {
        moveTo(x, y - 16f * s)
        cubicTo(x - 14f * s, y - 24f * s, x - 16f * s, y - 14f * s, x, y - 16f * s)
    }
    drawPath(leafPath1, CANOPY_MID, style = Fill)
    val leafPath2 = Path().apply {
        moveTo(x, y - 16f * s)
        cubicTo(x + 14f * s, y - 24f * s, x + 16f * s, y - 14f * s, x, y - 16f * s)
    }
    drawPath(leafPath2, CANOPY_LIGHT, style = Fill)
}

// ── Stage 2: Sprout ──
private fun DrawScope.drawDetailedSprout(x: Float, y: Float, sway: Float, hr: Float) {
    val s = hr * 1.2f
    val swayX = x + sway * 0.5f
    // Stem
    drawLine(TRUNK_DARK, Offset(x, y), Offset(swayX, y - 40f * s), strokeWidth = 5f * s, cap = StrokeCap.Round)
    // Small branch left
    drawLine(TRUNK_MID, Offset(swayX - 2f, y - 25f * s), Offset(swayX - 16f * s, y - 34f * s), strokeWidth = 3f * s, cap = StrokeCap.Round)
    // Leaves
    drawCanopyCluster(swayX, y - 42f * s, 14f * s, CANOPY_DARK, CANOPY_MID, CANOPY_LIGHT)
    drawCircle(CANOPY_LIGHT, 8f * s, Offset(swayX - 14f * s, y - 36f * s))
    drawCircle(CANOPY_MID, 7f * s, Offset(swayX + 10f * s, y - 38f * s))
}

// ── Stage 3: Plant (small tree) ──
private fun DrawScope.drawDetailedPlant(x: Float, y: Float, sway: Float, hr: Float) {
    val s = hr * 1.1f
    val swayX = x + sway
    // Trunk
    drawTrunk(x, y, swayX, y - 65f * s, 9f * s)
    // Branches
    val midY = y - 40f * s
    val midX = x + (swayX - x) * 0.6f
    drawLine(TRUNK_MID, Offset(midX, midY), Offset(swayX - 28f * s, y - 58f * s), strokeWidth = 5f * s, cap = StrokeCap.Round)
    drawLine(TRUNK_MID, Offset(midX + 2f, midY + 5f * s), Offset(swayX + 24f * s, y - 52f * s), strokeWidth = 4.5f * s, cap = StrokeCap.Round)
    // Canopy
    drawCanopyCluster(swayX, y - 68f * s, 22f * s, CANOPY_DARKEST, CANOPY_DARK, CANOPY_MID)
    drawCanopyCluster(swayX - 20f * s, y - 58f * s, 16f * s, CANOPY_DARK, CANOPY_MID, CANOPY_LIGHT)
    drawCanopyCluster(swayX + 18f * s, y - 56f * s, 15f * s, CANOPY_DARK, CANOPY_MID, CANOPY_LIGHT)
    // Top highlight
    drawCircle(CANOPY_LIGHTEST, 12f * s, Offset(swayX + 4f * s, y - 78f * s))
}

// ── Stage 4: Tree (full-sized) ──
private fun DrawScope.drawDetailedTree(x: Float, y: Float, sway: Float, hr: Float, extraScale: Float) {
    val s = hr * extraScale
    val swayX = x + sway * 1.5f
    val trunkTop = y - 120f * s

    // Thick trunk with taper
    drawTrunk(x, y, swayX, trunkTop, 16f * s)
    // Root flare
    drawLine(TRUNK_DARK, Offset(x - 12f * s, y), Offset(x - 20f * s, y + 4f * s), strokeWidth = 8f * s, cap = StrokeCap.Round)
    drawLine(TRUNK_DARK, Offset(x + 12f * s, y), Offset(x + 18f * s, y + 3f * s), strokeWidth = 7f * s, cap = StrokeCap.Round)

    // Main branches
    val b1x = x + (swayX - x) * 0.45f
    val b1y = y - 60f * s
    val b2x = x + (swayX - x) * 0.6f
    val b2y = y - 80f * s
    val b3x = x + (swayX - x) * 0.75f
    val b3y = y - 100f * s

    // Branch left low
    drawLine(TRUNK_MID, Offset(b1x, b1y), Offset(swayX - 55f * s, trunkTop + 40f * s), strokeWidth = 10f * s, cap = StrokeCap.Round)
    // Sub-branch
    drawLine(TRUNK_LIGHT, Offset(swayX - 40f * s, trunkTop + 48f * s), Offset(swayX - 65f * s, trunkTop + 25f * s), strokeWidth = 5f * s, cap = StrokeCap.Round)
    // Branch right low
    drawLine(TRUNK_MID, Offset(b1x + 4f, b1y + 8f * s), Offset(swayX + 50f * s, trunkTop + 35f * s), strokeWidth = 9f * s, cap = StrokeCap.Round)
    drawLine(TRUNK_LIGHT, Offset(swayX + 36f * s, trunkTop + 42f * s), Offset(swayX + 60f * s, trunkTop + 20f * s), strokeWidth = 4.5f * s, cap = StrokeCap.Round)
    // Branch left high
    drawLine(TRUNK_MID, Offset(b2x, b2y), Offset(swayX - 40f * s, trunkTop + 10f * s), strokeWidth = 8f * s, cap = StrokeCap.Round)
    // Branch right high
    drawLine(TRUNK_MID, Offset(b3x, b3y), Offset(swayX + 35f * s, trunkTop - 5f * s), strokeWidth = 7f * s, cap = StrokeCap.Round)
    // Top continuation
    drawLine(TRUNK_MID, Offset(b3x, b3y), Offset(swayX, trunkTop - 15f * s), strokeWidth = 9f * s, cap = StrokeCap.Round)

    // ── Multi-layered canopy ──
    val cc = Offset(swayX, trunkTop - 20f * s)
    // Back shadow layer
    drawCircle(CANOPY_DARKEST, 52f * s, Offset(cc.x - 48f * s, cc.y + 30f * s))
    drawCircle(CANOPY_DARKEST, 55f * s, Offset(cc.x + 50f * s, cc.y + 25f * s))
    drawCircle(CANOPY_DARKEST, 48f * s, Offset(cc.x - 20f * s, cc.y + 40f * s))
    drawCircle(CANOPY_DARKEST, 50f * s, Offset(cc.x + 15f * s, cc.y + 38f * s))
    // Mid layer
    drawCircle(CANOPY_DARK, 50f * s, Offset(cc.x - 38f * s, cc.y + 15f * s))
    drawCircle(CANOPY_DARK, 55f * s, Offset(cc.x + 40f * s, cc.y + 10f * s))
    drawCircle(CANOPY_DARK, 60f * s, Offset(cc.x, cc.y + 5f * s))
    drawCircle(CANOPY_DARK, 45f * s, Offset(cc.x - 55f * s, cc.y + 5f * s))
    drawCircle(CANOPY_DARK, 42f * s, Offset(cc.x + 55f * s, cc.y))
    // Front layer
    drawCircle(CANOPY_MID, 48f * s, Offset(cc.x - 30f * s, cc.y))
    drawCircle(CANOPY_MID, 50f * s, Offset(cc.x + 25f * s, cc.y - 5f * s))
    drawCircle(CANOPY_MID, 55f * s, Offset(cc.x, cc.y - 15f * s))
    drawCircle(CANOPY_MID, 40f * s, Offset(cc.x - 45f * s, cc.y - 10f * s))
    drawCircle(CANOPY_MID, 38f * s, Offset(cc.x + 48f * s, cc.y - 12f * s))
    // Highlight layer
    drawCircle(CANOPY_LIGHT, 42f * s, Offset(cc.x - 15f * s, cc.y - 25f * s))
    drawCircle(CANOPY_LIGHT, 38f * s, Offset(cc.x + 20f * s, cc.y - 28f * s))
    drawCircle(CANOPY_LIGHT, 35f * s, Offset(cc.x, cc.y - 35f * s))
    // Top highlights
    drawCircle(CANOPY_LIGHTEST, 28f * s, Offset(cc.x + 8f * s, cc.y - 42f * s))
    drawCircle(LEAF_HIGHLIGHT, 20f * s, Offset(cc.x - 5f * s, cc.y - 48f * s))
}

// ── Stage 5: Garden (Tree + side plants) ──
private fun DrawScope.drawDetailedGarden(x: Float, y: Float, sway: Float, hr: Float) {
    drawDetailedPlant(x - 95f * hr, y + 8f, sway * 0.7f, hr * 0.55f)
    drawDetailedTree(x, y, sway, hr, 1.05f)
    drawDetailedSprout(x + 90f * hr, y + 12f, sway * -0.5f, hr * 0.5f)
}

// ── Stage 6: Forest (multiple trees) ──
private fun DrawScope.drawDetailedForest(x: Float, y: Float, sway: Float, hr: Float) {
    // Background small trees
    drawDetailedPlant(x - 110f * hr, y + 5f, sway * 0.6f, hr * 0.5f)
    drawDetailedPlant(x + 105f * hr, y + 8f, sway * -0.8f, hr * 0.48f)
    drawDetailedSprout(x - 60f * hr, y + 15f, sway * 0.4f, hr * 0.4f)
    // Main tree (larger)
    drawDetailedTree(x, y, sway, hr, 1.15f)
}

// ── Helper: Trunk with taper ──
private fun DrawScope.drawTrunk(baseX: Float, baseY: Float, topX: Float, topY: Float, width: Float) {
    // Draw tapered trunk as a path
    val halfBase = width
    val halfTop = width * 0.55f
    val path = Path().apply {
        moveTo(baseX - halfBase, baseY)
        lineTo(topX - halfTop, topY)
        lineTo(topX + halfTop, topY)
        lineTo(baseX + halfBase, baseY)
        close()
    }
    drawPath(path, TRUNK_DARK, style = Fill)
    // Center highlight strip for bark effect
    val highlight = Path().apply {
        moveTo(baseX - halfBase * 0.3f, baseY)
        lineTo(topX - halfTop * 0.3f, topY)
        lineTo(topX + halfTop * 0.2f, topY)
        lineTo(baseX + halfBase * 0.2f, baseY)
        close()
    }
    drawPath(highlight, TRUNK_MID, style = Fill)
}

// ── Helper: Canopy cluster (3-circle set) ──
private fun DrawScope.drawCanopyCluster(cx: Float, cy: Float, radius: Float, c1: Color, c2: Color, c3: Color) {
    drawCircle(c1, radius, Offset(cx - radius * 0.5f, cy + radius * 0.3f))
    drawCircle(c2, radius * 0.9f, Offset(cx + radius * 0.4f, cy + radius * 0.2f))
    drawCircle(c3, radius * 0.85f, Offset(cx, cy - radius * 0.35f))
}
