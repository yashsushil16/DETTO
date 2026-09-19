package com.example.digitaldetox.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// High-contrast, elegant monochrome palette for dark OLED theme
private val TRUNK_BASE = Color(0xFF505050)
private val TRUNK_MAIN = Color(0xFF7A7A7A)
private val TRUNK_HIGHLIGHT = Color(0xFFA0A0A0)

private val SOIL_BASE = Color(0xFF1E1E1E)
private val SOIL_MID = Color(0xFF2C2C2C)
private val SOIL_SURFACE = Color(0xFF424242)
private val SOIL_HIGHLIGHT = Color(0xFF6E6E6E)

private val CANOPY_SHADOW = Color(0xFF383838)
private val CANOPY_DEEP = Color(0xFF656565)
private val CANOPY_MID = Color(0xFF909090)
private val CANOPY_LIGHT = Color(0xFFCCCCCC)
private val CANOPY_WHITE = Color(0xFFFFFFFF)
private val GLOW_WHITE = Color(0x33FFFFFF)

@Composable
fun TreeVisualizer(
    stage: String,
    healthRatio: Float = 1f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tree_anim")
    
    // Gentle organic swaying
    val swayAngle by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    // Breathing glow phase
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Subtle seed pulse
    val seedBreath by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "seed_breath"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        if (size.width <= 0 || size.height <= 0) return@Canvas

        val rootX = size.width * 0.5f
        val hr = healthRatio.coerceIn(0.6f, 1.3f)

        if (stage == "Seed") {
            // For Seed stage, center the soil bed nicely in the middle
            val seedY = size.height * 0.62f
            val baseDimension = min(size.width * 0.85f, size.height * 0.7f)
            val s = (baseDimension / 240f) * hr
            drawDetailedSeedWithGround(rootX, seedY, s, glowPulse, seedBreath)
        } else {
            val rootY = size.height * 0.84f
            val baseDimension = min(size.width * 0.85f, size.height * 0.75f)
            val s = (baseDimension / 260f) * hr

            // Draw soft ground mound / pedestal
            drawGroundMound(rootX, rootY, 110f * s, s)

            when (stage) {
                "Sprout" -> drawDetailedSprout(rootX, rootY, swayAngle, s, glowPulse)
                "Plant" -> drawDetailedPlant(rootX, rootY, swayAngle, s, glowPulse)
                "Garden" -> drawDetailedGarden(rootX, rootY, swayAngle, s, glowPulse)
                "Forest" -> drawDetailedForest(rootX, rootY, swayAngle, s, glowPulse)
                else -> drawDetailedTree(rootX, rootY, swayAngle, s, glowPulse, 1f) // "Tree"
            }
        }
    }
}

// ── Ground / Soil Layer Helper ──
private fun DrawScope.drawGroundMound(x: Float, y: Float, radius: Float, s: Float) {
    // Deep base glow
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x30FFFFFF), Color(0x00000000)),
            center = Offset(x, y + 4f * s),
            radius = radius * 1.3f
        ),
        topLeft = Offset(x - radius * 1.3f, y - 10f * s),
        size = Size(radius * 2.6f, 30f * s)
    )

    // Soil mound curve
    val moundPath = Path().apply {
        moveTo(x - radius, y + 6f * s)
        cubicTo(x - radius * 0.6f, y - 8f * s, x + radius * 0.6f, y - 8f * s, x + radius, y + 6f * s)
        cubicTo(x + radius * 0.5f, y + 14f * s, x - radius * 0.5f, y + 14f * s, x - radius, y + 6f * s)
        close()
    }
    drawPath(moundPath, SOIL_BASE, style = Fill)
    drawPath(moundPath, SOIL_SURFACE, style = Stroke(width = 1.8f * s))

    // Zen pebbles / soil highlights
    drawCircle(SOIL_HIGHLIGHT, 2.5f * s, Offset(x - radius * 0.45f, y + 2f * s))
    drawCircle(SOIL_HIGHLIGHT, 3.2f * s, Offset(x + radius * 0.35f, y + 1f * s))
    drawCircle(SOIL_SURFACE, 2f * s, Offset(x - radius * 0.15f, y + 5f * s))
    drawCircle(SOIL_HIGHLIGHT, 2.2f * s, Offset(x + radius * 0.6f, y + 4f * s))
}

// ── Stage 1: Seed in Ground Bed ──
private fun DrawScope.drawDetailedSeedWithGround(
    x: Float,
    y: Float,
    s: Float,
    glow: Float,
    breath: Float
) {
    val soilWidth = 140f * s
    val soilHeight = 36f * s

    // 1. Ambient soft aura around soil and seed
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x28FFFFFF), Color(0x08FFFFFF), Color(0x00000000)),
            center = Offset(x, y - 10f * s),
            radius = 160f * s * glow
        ),
        topLeft = Offset(x - 160f * s, y - 100f * s),
        size = Size(320f * s, 200f * s)
    )

    // 2. Soil Bed (Layered Zen Planter / Ground Mound)
    // Soil Bed Base Shadow
    drawOval(
        color = Color(0x60000000),
        topLeft = Offset(x - soilWidth * 1.05f, y + 8f * s),
        size = Size(soilWidth * 2.1f, soilHeight * 0.9f)
    )

    // Soil Mound Path
    val soilPath = Path().apply {
        moveTo(x - soilWidth, y + 12f * s)
        cubicTo(x - soilWidth * 0.6f, y - 14f * s, x + soilWidth * 0.6f, y - 14f * s, x + soilWidth, y + 12f * s)
        cubicTo(x + soilWidth * 0.7f, y + 24f * s, x - soilWidth * 0.7f, y + 24f * s, x - soilWidth, y + 12f * s)
        close()
    }
    drawPath(soilPath, SOIL_MID, style = Fill)

    // Soil Rim / Surface Line
    val rimPath = Path().apply {
        moveTo(x - soilWidth * 0.95f, y + 10f * s)
        cubicTo(x - soilWidth * 0.55f, y - 12f * s, x + soilWidth * 0.55f, y - 12f * s, x + soilWidth * 0.95f, y + 10f * s)
    }
    drawPath(rimPath, SOIL_HIGHLIGHT, style = Stroke(width = 2.2f * s, cap = StrokeCap.Round))

    // Decorative Zen stones in the soil bed
    drawCircle(SOIL_HIGHLIGHT, 4.5f * s, Offset(x - soilWidth * 0.5f, y + 2f * s))
    drawCircle(SOIL_SURFACE, 3.5f * s, Offset(x - soilWidth * 0.35f, y + 8f * s))
    drawCircle(SOIL_HIGHLIGHT, 5f * s, Offset(x + soilWidth * 0.45f, y + 4f * s))
    drawCircle(SOIL_SURFACE, 3f * s, Offset(x + soilWidth * 0.65f, y + 6f * s))

    // 3. Root tendrils growing into the soil
    val root1 = Path().apply {
        moveTo(x, y + 2f * s)
        cubicTo(x - 8f * s, y + 14f * s, x - 18f * s, y + 20f * s, x - 26f * s, y + 24f * s)
    }
    drawPath(root1, TRUNK_HIGHLIGHT, style = Stroke(width = 2f * s, cap = StrokeCap.Round))

    val root2 = Path().apply {
        moveTo(x + 2f * s, y + 3f * s)
        cubicTo(x + 6f * s, y + 12f * s, x + 15f * s, y + 18f * s, x + 24f * s, y + 22f * s)
    }
    drawPath(root2, TRUNK_HIGHLIGHT, style = Stroke(width = 1.8f * s, cap = StrokeCap.Round))

    // 4. The Seed (Nestled in the soil center, pulsating with life)
    val seedScale = s * 1.5f * breath
    val seedCenterX = x
    val seedCenterY = y - 4f * s

    // Seed back glow
    drawCircle(Color(0x35FFFFFF), 35f * seedScale * glow, Offset(seedCenterX, seedCenterY))

    // Seed Husk / Shell Path
    val seedHusk = Path().apply {
        moveTo(seedCenterX, seedCenterY - 18f * seedScale)
        cubicTo(
            seedCenterX + 16f * seedScale, seedCenterY - 14f * seedScale,
            seedCenterX + 16f * seedScale, seedCenterY + 12f * seedScale,
            seedCenterX, seedCenterY + 20f * seedScale
        )
        cubicTo(
            seedCenterX - 16f * seedScale, seedCenterY + 12f * seedScale,
            seedCenterX - 16f * seedScale, seedCenterY - 14f * seedScale,
            seedCenterX, seedCenterY - 18f * seedScale
        )
        close()
    }
    drawPath(seedHusk, TRUNK_MAIN, style = Fill)
    drawPath(seedHusk, CANOPY_LIGHT, style = Stroke(width = 2f * seedScale))

    // Inner seed body (silver-white core)
    val innerSeed = Path().apply {
        moveTo(seedCenterX, seedCenterY - 12f * seedScale)
        cubicTo(
            seedCenterX + 9f * seedScale, seedCenterY - 8f * seedScale,
            seedCenterX + 9f * seedScale, seedCenterY + 8f * seedScale,
            seedCenterX, seedCenterY + 13f * seedScale
        )
        cubicTo(
            seedCenterX - 9f * seedScale, seedCenterY + 8f * seedScale,
            seedCenterX - 9f * seedScale, seedCenterY - 8f * seedScale,
            seedCenterX, seedCenterY - 12f * seedScale
        )
        close()
    }
    drawPath(innerSeed, CANOPY_WHITE, style = Fill)

    // 5. Emerging Sprout Shoot & Tender Leaves
    val shootTipY = seedCenterY - 45f * seedScale
    val shootPath = Path().apply {
        moveTo(seedCenterX, seedCenterY - 14f * seedScale)
        cubicTo(
            seedCenterX - 4f * seedScale, seedCenterY - 26f * seedScale,
            seedCenterX + 6f * seedScale, seedCenterY - 36f * seedScale,
            seedCenterX + 2f * seedScale, shootTipY
        )
    }
    drawPath(shootPath, CANOPY_WHITE, style = Stroke(width = 4f * seedScale, cap = StrokeCap.Round))

    // Little Twin Seed Leaves (Cotyledons)
    drawLeaf(seedCenterX - 10f * seedScale, shootTipY + 4f * seedScale, -40f, 18f * seedScale, CANOPY_WHITE)
    drawLeaf(seedCenterX + 12f * seedScale, shootTipY + 2f * seedScale, 38f, 20f * seedScale, CANOPY_LIGHT)

    // Glowing core particle
    drawCircle(CANOPY_WHITE, 4f * seedScale * glow, Offset(seedCenterX, seedCenterY))
    drawCircle(Color(0x50FFFFFF), 7f * seedScale * glow, Offset(seedCenterX, seedCenterY))
}

// ── Stage 2: Sprout ──
private fun DrawScope.drawDetailedSprout(x: Float, y: Float, sway: Float, s: Float, glow: Float) {
    val sproutScale = s * 1.4f
    val tipX = x + sway * 2f
    val tipY = y - 90f * sproutScale

    // Organic stem
    val stemPath = Path().apply {
        moveTo(x, y)
        cubicTo(x - 8f * sproutScale, y - 30f * sproutScale, tipX - 4f * sproutScale, y - 60f * sproutScale, tipX, tipY)
    }
    drawPath(stemPath, TRUNK_MAIN, style = Stroke(width = 6f * sproutScale, cap = StrokeCap.Round))

    // Lower leaves
    drawLeaf(x - 22f * sproutScale, y - 45f * sproutScale, -50f, 22f * sproutScale, CANOPY_MID)
    drawLeaf(x + 24f * sproutScale, y - 55f * sproutScale, 45f, 24f * sproutScale, CANOPY_LIGHT)
    
    // Upper canopy cluster
    drawCircle(CANOPY_DEEP, 24f * sproutScale, Offset(tipX - 10f * sproutScale, tipY + 8f * sproutScale))
    drawCircle(CANOPY_MID, 26f * sproutScale, Offset(tipX + 8f * sproutScale, tipY + 4f * sproutScale))
    drawCircle(CANOPY_LIGHT, 22f * sproutScale, Offset(tipX, tipY - 10f * sproutScale))
    drawCircle(CANOPY_WHITE, 16f * sproutScale, Offset(tipX - 2f * sproutScale, tipY - 16f * sproutScale))
    
    drawCircle(GLOW_WHITE, 40f * sproutScale * glow, Offset(tipX, tipY))
}

// ── Stage 3: Plant / Young Bonsai ──
private fun DrawScope.drawDetailedPlant(x: Float, y: Float, sway: Float, s: Float, glow: Float) {
    val plantScale = s * 1.25f
    val trunkTopY = y - 130f * plantScale
    val swayX = x + sway * 3f

    // Trunk
    drawTaperedTrunk(x, y, swayX, trunkTopY, 14f * plantScale, 7f * plantScale)

    // Branches
    val b1x = x + (swayX - x) * 0.5f
    val b1y = y - 65f * plantScale
    drawLine(TRUNK_MAIN, Offset(b1x, b1y), Offset(swayX - 45f * plantScale, trunkTopY + 30f * plantScale), strokeWidth = 6.5f * plantScale, cap = StrokeCap.Round)
    drawLine(TRUNK_MAIN, Offset(b1x + 2f, b1y + 10f * plantScale), Offset(swayX + 42f * plantScale, trunkTopY + 25f * plantScale), strokeWidth = 6f * plantScale, cap = StrokeCap.Round)

    // Foliage Clouds
    drawCanopyCloud(swayX - 45f * plantScale, trunkTopY + 25f * plantScale, 32f * plantScale)
    drawCanopyCloud(swayX + 42f * plantScale, trunkTopY + 20f * plantScale, 30f * plantScale)
    drawCanopyCloud(swayX, trunkTopY - 15f * plantScale, 42f * plantScale)
    
    // Top highlight
    drawCircle(CANOPY_WHITE, 20f * plantScale, Offset(swayX + 4f * plantScale, trunkTopY - 30f * plantScale))
}

// ── Stage 4: Majestic Full Tree (Synced with DETTO) ──
private fun DrawScope.drawDetailedTree(x: Float, y: Float, sway: Float, s: Float, glow: Float, extraScale: Float) {
    val treeScale = s * extraScale
    val swayX = x + sway * 3.5f
    val trunkTopY = y - 160f * treeScale

    // 1. Root flare
    val rootL = Path().apply {
        moveTo(x - 22f * treeScale, y)
        cubicTo(x - 30f * treeScale, y + 2f * treeScale, x - 42f * treeScale, y + 8f * treeScale, x - 48f * treeScale, y + 10f * treeScale)
    }
    drawPath(rootL, TRUNK_BASE, style = Stroke(width = 8f * treeScale, cap = StrokeCap.Round))
    
    val rootR = Path().apply {
        moveTo(x + 22f * treeScale, y)
        cubicTo(x + 30f * treeScale, y + 2f * treeScale, x + 40f * treeScale, y + 7f * treeScale, x + 46f * treeScale, y + 9f * treeScale)
    }
    drawPath(rootR, TRUNK_BASE, style = Stroke(width = 7.5f * treeScale, cap = StrokeCap.Round))

    // 2. Thick organic curved trunk
    drawTaperedTrunk(x, y, swayX, trunkTopY, 22f * treeScale, 10f * treeScale)

    // 3. Branches
    val midY = y - 75f * treeScale
    val midX = x + (swayX - x) * 0.45f
    
    // Low left branch
    val branchL1 = Offset(swayX - 70f * treeScale, trunkTopY + 45f * treeScale)
    drawLine(TRUNK_MAIN, Offset(midX, midY), branchL1, strokeWidth = 11f * treeScale, cap = StrokeCap.Round)
    drawLine(TRUNK_HIGHLIGHT, branchL1, Offset(swayX - 95f * treeScale, trunkTopY + 30f * treeScale), strokeWidth = 6f * treeScale, cap = StrokeCap.Round)

    // Low right branch
    val branchR1 = Offset(swayX + 68f * treeScale, trunkTopY + 40f * treeScale)
    drawLine(TRUNK_MAIN, Offset(midX + 2f, midY + 12f * treeScale), branchR1, strokeWidth = 10f * treeScale, cap = StrokeCap.Round)
    drawLine(TRUNK_HIGHLIGHT, branchR1, Offset(swayX + 90f * treeScale, trunkTopY + 22f * treeScale), strokeWidth = 5.5f * treeScale, cap = StrokeCap.Round)

    // Upper branches
    val upperY = y - 115f * treeScale
    val upperX = x + (swayX - x) * 0.7f
    drawLine(TRUNK_MAIN, Offset(upperX, upperY), Offset(swayX - 50f * treeScale, trunkTopY + 5f * treeScale), strokeWidth = 8f * treeScale, cap = StrokeCap.Round)
    drawLine(TRUNK_MAIN, Offset(upperX, upperY), Offset(swayX + 48f * treeScale, trunkTopY - 5f * treeScale), strokeWidth = 7.5f * treeScale, cap = StrokeCap.Round)
    drawLine(TRUNK_MAIN, Offset(upperX, upperY), Offset(swayX, trunkTopY - 20f * treeScale), strokeWidth = 9f * treeScale, cap = StrokeCap.Round)

    // 4. Multi-layered Cloud Canopy
    drawCanopyCloud(swayX - 82f * treeScale, trunkTopY + 35f * treeScale, 38f * treeScale)
    drawCanopyCloud(swayX + 80f * treeScale, trunkTopY + 28f * treeScale, 36f * treeScale)
    drawCanopyCloud(swayX - 45f * treeScale, trunkTopY + 2f * treeScale, 44f * treeScale)
    drawCanopyCloud(swayX + 42f * treeScale, trunkTopY - 6f * treeScale, 42f * treeScale)
    drawCanopyCloud(swayX, trunkTopY - 32f * treeScale, 56f * treeScale)
    
    // Top Highlights & Zen Leaf Nodes
    drawCircle(CANOPY_WHITE, 26f * treeScale, Offset(swayX + 10f * treeScale, trunkTopY - 55f * treeScale))
    drawCircle(CANOPY_WHITE, 20f * treeScale, Offset(swayX - 18f * treeScale, trunkTopY - 50f * treeScale))
    drawCircle(CANOPY_LIGHT, 16f * treeScale, Offset(swayX + 32f * treeScale, trunkTopY - 40f * treeScale))

    // Glowing Spores / Zen Sparkles
    val auraRadius = 110f * treeScale * glow
    drawCircle(Color(0x15FFFFFF), auraRadius, Offset(swayX, trunkTopY - 15f * treeScale))
    drawCircle(Color(0x28FFFFFF), 4f * treeScale, Offset(swayX - 60f * treeScale, trunkTopY - 40f * treeScale))
    drawCircle(Color(0x28FFFFFF), 3.5f * treeScale, Offset(swayX + 70f * treeScale, trunkTopY - 60f * treeScale))
    drawCircle(Color(0x35FFFFFF), 5f * treeScale, Offset(swayX + 15f * treeScale, trunkTopY - 80f * treeScale))
}

// ── Stage 5: Garden ──
private fun DrawScope.drawDetailedGarden(x: Float, y: Float, sway: Float, s: Float, glow: Float) {
    drawDetailedPlant(x - 90f * s, y + 5f, sway * 0.7f, s * 0.65f, glow)
    drawDetailedSprout(x + 92f * s, y + 8f, sway * -0.6f, s * 0.7f, glow)
    drawDetailedTree(x, y, sway, s, glow, 1.05f)
}

// ── Stage 6: Flourishing Forest ──
private fun DrawScope.drawDetailedForest(x: Float, y: Float, sway: Float, s: Float, glow: Float) {
    drawDetailedPlant(x - 110f * s, y + 5f, sway * 0.5f, s * 0.55f, glow)
    drawDetailedPlant(x + 115f * s, y + 6f, sway * -0.7f, s * 0.58f, glow)
    drawDetailedSprout(x - 55f * s, y + 10f, sway * 0.8f, s * 0.5f, glow)
    drawDetailedSprout(x + 60f * s, y + 12f, sway * -0.5f, s * 0.52f, glow)
    drawDetailedTree(x, y, sway, s, glow, 1.15f)
}

// ── Helper: Draw tapered organic trunk ──
private fun DrawScope.drawTaperedTrunk(
    baseX: Float, baseY: Float,
    topX: Float, topY: Float,
    baseWidth: Float, topWidth: Float
) {
    val trunkPath = Path().apply {
        moveTo(baseX - baseWidth, baseY)
        cubicTo(
            baseX - baseWidth * 0.8f, baseY - (baseY - topY) * 0.4f,
            topX - topWidth * 1.2f, topY + (baseY - topY) * 0.3f,
            topX - topWidth, topY
        )
        lineTo(topX + topWidth, topY)
        cubicTo(
            topX + topWidth * 1.2f, topY + (baseY - topY) * 0.3f,
            baseX + baseWidth * 0.8f, baseY - (baseY - topY) * 0.4f,
            baseX + baseWidth, baseY
        )
        close()
    }
    drawPath(trunkPath, TRUNK_BASE, style = Fill)

    val barkPath = Path().apply {
        moveTo(baseX - baseWidth * 0.35f, baseY)
        cubicTo(
            baseX - baseWidth * 0.25f, baseY - (baseY - topY) * 0.4f,
            topX - topWidth * 0.4f, topY + (baseY - topY) * 0.3f,
            topX - topWidth * 0.3f, topY
        )
        lineTo(topX + topWidth * 0.3f, topY)
        cubicTo(
            topX + topWidth * 0.4f, topY + (baseY - topY) * 0.3f,
            baseX + baseWidth * 0.25f, baseY - (baseY - topY) * 0.4f,
            baseX + baseWidth * 0.35f, baseY
        )
        close()
    }
    drawPath(barkPath, TRUNK_MAIN, style = Fill)
}

// ── Helper: Draw cloud foliage cluster ──
private fun DrawScope.drawCanopyCloud(cx: Float, cy: Float, radius: Float) {
    drawCircle(CANOPY_SHADOW, radius, Offset(cx, cy + radius * 0.15f))
    drawCircle(CANOPY_DEEP, radius * 0.95f, Offset(cx - radius * 0.3f, cy + radius * 0.1f))
    drawCircle(CANOPY_DEEP, radius * 0.9f, Offset(cx + radius * 0.35f, cy + radius * 0.05f))
    drawCircle(CANOPY_MID, radius * 0.85f, Offset(cx - radius * 0.15f, cy - radius * 0.1f))
    drawCircle(CANOPY_MID, radius * 0.8f, Offset(cx + radius * 0.2f, cy - radius * 0.15f))
    drawCircle(CANOPY_LIGHT, radius * 0.65f, Offset(cx, cy - radius * 0.25f))
    drawCircle(CANOPY_WHITE, radius * 0.4f, Offset(cx + radius * 0.1f, cy - radius * 0.35f))
}

// ── Helper: Draw organic leaf ──
private fun DrawScope.drawLeaf(x: Float, y: Float, angleDeg: Float, size: Float, color: Color) {
    val leaf = Path().apply {
        moveTo(x, y)
        cubicTo(x - size * 0.5f, y - size * 0.7f, x - size * 0.2f, y - size, x, y - size * 1.2f)
        cubicTo(x + size * 0.2f, y - size, x + size * 0.5f, y - size * 0.7f, x, y)
        close()
    }
    drawPath(leaf, color, style = Fill)
}
