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

// ── Monochrome Palette ──
private val TRUNK_BASE      = Color(0xFF404040)
private val TRUNK_MAIN      = Color(0xFF6A6A6A)
private val TRUNK_MID       = Color(0xFF8A8A8A)
private val TRUNK_HIGHLIGHT = Color(0xFFB0B0B0)

private val SOIL_BASE       = Color(0xFF1A1A1A)
private val SOIL_MID        = Color(0xFF2C2C2C)
private val SOIL_SURFACE    = Color(0xFF424242)
private val SOIL_HIGHLIGHT  = Color(0xFF6E6E6E)

private val CANOPY_DARKEST  = Color(0xFF2E2E2E)
private val CANOPY_SHADOW   = Color(0xFF3E3E3E)
private val CANOPY_DARK     = Color(0xFF555555)
private val CANOPY_MID      = Color(0xFF888888)
private val CANOPY_LIGHT    = Color(0xFFBBBBBB)
private val CANOPY_HILIGHT  = Color(0xFFDDDDDD)
private val CANOPY_WHITE    = Color(0xFFFFFFFF)

@Composable
fun TreeVisualizer(
    stage: String,
    healthRatio: Float = 1f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tree_anim")

    val swayAngle by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val particleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle"
    )

    val seedBreath by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "seed_breath"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        if (size.width <= 0 || size.height <= 0) return@Canvas
        val rootX = size.width * 0.5f
        val hr = healthRatio.coerceIn(0.5f, 1.3f)

        if (stage == "Seed") {
            val seedY = size.height * 0.62f
            val baseDim = min(size.width * 0.85f, size.height * 0.7f)
            val s = (baseDim / 240f) * hr
            drawDetailedSeedWithGround(rootX, seedY, s, glowPulse, seedBreath, particleAngle)
        } else {
            val rootY = size.height * 0.84f
            val baseDim = min(size.width * 0.85f, size.height * 0.75f)
            val s = (baseDim / 260f) * hr

            drawGroundMound(rootX, rootY, 110f * s, s)

            when (stage) {
                "Sprout" -> drawDetailedSprout(rootX, rootY, swayAngle, s, glowPulse, particleAngle)
                "Plant"  -> drawDetailedPlant(rootX, rootY, swayAngle, s, glowPulse, particleAngle)
                "Garden" -> drawDetailedGarden(rootX, rootY, swayAngle, s, glowPulse, particleAngle)
                "Forest" -> drawDetailedForest(rootX, rootY, swayAngle, s, glowPulse, particleAngle)
                else     -> drawDetailedTree(rootX, rootY, swayAngle, s, glowPulse, particleAngle, 1f)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// GLOW ORB — drawn BEHIND the tree at a given center
// ═══════════════════════════════════════════════════════
private fun DrawScope.drawGlowOrb(
    cx: Float, cy: Float,
    baseRadius: Float,
    pulse: Float,
    particleAngle: Float,
    particleCount: Int = 6
) {
    val r = baseRadius * pulse

    // Very subtle concentric halos — calming, not blinding
    drawCircle(Color(0x04FFFFFF), r * 2.6f, Offset(cx, cy))
    drawCircle(Color(0x06FFFFFF), r * 2.0f, Offset(cx, cy))
    drawCircle(Color(0x09FFFFFF), r * 1.5f, Offset(cx, cy))
    drawCircle(Color(0x0EFFFFFF), r * 1.15f, Offset(cx, cy))
    drawCircle(Color(0x16FFFFFF), r * 0.72f, Offset(cx, cy))
    drawCircle(Color(0x22FFFFFF), r * 0.38f, Offset(cx, cy))

    // Very faint ring outlines
    drawCircle(Color(0x07FFFFFF), r * 1.75f, Offset(cx, cy),
        style = Stroke(width = r * 0.025f))
    drawCircle(Color(0x09FFFFFF), r * 1.3f, Offset(cx, cy),
        style = Stroke(width = r * 0.018f))

    // Soft orbiting motes — barely visible
    for (i in 0 until particleCount) {
        val angle = Math.toRadians((particleAngle + (360f / particleCount) * i).toDouble())
        val orbitR = r * 1.5f
        val px = cx + orbitR * cos(angle).toFloat()
        val py = cy + orbitR * sin(angle).toFloat()
        val pSize = r * 0.055f * (0.6f + 0.4f * ((i % 3).toFloat() / 2f))
        drawCircle(Color(0x20FFFFFF), pSize, Offset(px, py))
        drawCircle(Color(0x10FFFFFF), pSize * 1.5f, Offset(px, py))
    }

    // Counter-rotating inner motes
    for (i in 0 until (particleCount / 2)) {
        val angle = Math.toRadians((-particleAngle * 0.6f + (360f / (particleCount / 2)) * i).toDouble())
        val orbitR = r * 1.1f
        val px = cx + orbitR * cos(angle).toFloat()
        val py = cy + orbitR * sin(angle).toFloat()
        drawCircle(Color(0x14FFFFFF), r * 0.038f, Offset(px, py))
    }
}

// ═══════════════════════════════════════════════════════
// FOLIAGE SPHERE — reference-image quality 3D shading
// Multiple layers: shadow base → mid tones → bright top-left highlight → specular
// ═══════════════════════════════════════════════════════
private fun DrawScope.drawFoliageSphere(cx: Float, cy: Float, r: Float) {
    // Subtle drop shadow
    drawOval(
        color = Color(0x25000000),
        topLeft = Offset(cx - r * 0.9f, cy + r * 0.7f),
        size = Size(r * 1.8f, r * 0.4f)
    )

    // Base fill — darkest
    drawCircle(CANOPY_DARKEST, r, Offset(cx, cy))

    // Lower hemisphere shadow gradient (gentle)
    drawCircle(
        brush = Brush.radialGradient(
            0f to Color(0x00000000),
            0.5f to Color(0x12000000),
            1f to Color(0x40000000),
            center = Offset(cx + r * 0.2f, cy + r * 0.2f),
            radius = r
        ),
        radius = r,
        center = Offset(cx, cy)
    )

    drawCircle(CANOPY_DARK, r * 0.88f, Offset(cx - r * 0.05f, cy - r * 0.02f))
    drawCircle(CANOPY_MID,  r * 0.72f, Offset(cx - r * 0.08f, cy - r * 0.06f))

    // Subtle leaf veins
    val veins = listOf(
        Pair(Offset(cx - r * 0.3f, cy - r * 0.35f), Offset(cx + r * 0.25f, cy + r * 0.15f)),
        Pair(Offset(cx - r * 0.1f, cy - r * 0.4f),  Offset(cx + r * 0.35f, cy + r * 0.3f)),
        Pair(Offset(cx - r * 0.4f, cy - r * 0.1f),  Offset(cx + r * 0.1f,  cy + r * 0.4f))
    )
    for ((a, b) in veins) {
        val veinPath = Path().apply {
            moveTo(a.x, a.y)
            quadraticTo(cx, cy, b.x, b.y)
        }
        drawPath(veinPath, Color(0x10000000), style = Stroke(width = r * 0.035f, cap = StrokeCap.Round))
        drawPath(veinPath, Color(0x08FFFFFF), style = Stroke(width = r * 0.02f, cap = StrokeCap.Round))
    }

    // Soft top-left highlight (gentle, not harsh)
    drawCircle(CANOPY_LIGHT,  r * 0.38f, Offset(cx - r * 0.22f, cy - r * 0.26f))
    drawCircle(CANOPY_HILIGHT, r * 0.20f, Offset(cx - r * 0.28f, cy - r * 0.32f))
    // Soft specular — smaller and more translucent
    drawCircle(Color(0xCCFFFFFF),  r * 0.09f, Offset(cx - r * 0.32f, cy - r * 0.38f))

    // Subtle rim edge
    drawCircle(CANOPY_DARK, r, Offset(cx, cy), style = Stroke(width = r * 0.04f))
}

// ═══════════════════════════════════════════════════════
// TRUNK — thicker, organic 3D trunk with bark seam highlight
// ═══════════════════════════════════════════════════════
private fun DrawScope.drawRichTrunk(
    baseX: Float, baseY: Float,
    topX: Float, topY: Float,
    baseW: Float, topW: Float
) {
    // Outer shadow trunk silhouette
    val shadowPath = Path().apply {
        moveTo(baseX - baseW - 3f, baseY + 4f)
        cubicTo(baseX - baseW * 0.9f, baseY - (baseY - topY) * 0.4f,
            topX - topW * 1.3f, topY + (baseY - topY) * 0.28f,
            topX - topW - 2f, topY + 2f)
        lineTo(topX + topW + 2f, topY + 2f)
        cubicTo(topX + topW * 1.3f, topY + (baseY - topY) * 0.28f,
            baseX + baseW * 0.9f, baseY - (baseY - topY) * 0.4f,
            baseX + baseW + 3f, baseY + 4f)
        close()
    }
    drawPath(shadowPath, Color(0x60000000), style = Fill)

    // Main trunk body
    val trunkPath = Path().apply {
        moveTo(baseX - baseW, baseY)
        cubicTo(baseX - baseW * 0.82f, baseY - (baseY - topY) * 0.38f,
            topX - topW * 1.15f, topY + (baseY - topY) * 0.3f,
            topX - topW, topY)
        lineTo(topX + topW, topY)
        cubicTo(topX + topW * 1.15f, topY + (baseY - topY) * 0.3f,
            baseX + baseW * 0.82f, baseY - (baseY - topY) * 0.38f,
            baseX + baseW, baseY)
        close()
    }
    drawPath(trunkPath, TRUNK_BASE, style = Fill)

    // Mid-tone volume layer
    val midPath = Path().apply {
        moveTo(baseX - baseW * 0.55f, baseY)
        cubicTo(baseX - baseW * 0.42f, baseY - (baseY - topY) * 0.38f,
            topX - topW * 0.55f, topY + (baseY - topY) * 0.3f,
            topX - topW * 0.4f, topY)
        lineTo(topX + topW * 0.4f, topY)
        cubicTo(topX + topW * 0.55f, topY + (baseY - topY) * 0.3f,
            baseX + baseW * 0.42f, baseY - (baseY - topY) * 0.38f,
            baseX + baseW * 0.55f, baseY)
        close()
    }
    drawPath(midPath, TRUNK_MAIN, style = Fill)

    // Central highlight seam (gives the 3D rounded trunk feel)
    val highlightPath = Path().apply {
        moveTo(baseX - baseW * 0.12f, baseY)
        cubicTo(baseX - baseW * 0.08f, baseY - (baseY - topY) * 0.38f,
            topX - topW * 0.15f, topY + (baseY - topY) * 0.3f,
            topX, topY)
    }
    drawPath(highlightPath, TRUNK_MID, style = Stroke(width = baseW * 0.28f, cap = StrokeCap.Round))
    drawPath(highlightPath, TRUNK_HIGHLIGHT, style = Stroke(width = baseW * 0.10f, cap = StrokeCap.Round))

    // Bark texture lines (2 subtle vertical lines)
    val bark1 = Path().apply {
        moveTo(baseX - baseW * 0.35f, baseY)
        cubicTo(baseX - baseW * 0.25f, baseY - (baseY - topY) * 0.3f,
            topX - topW * 0.6f, topY + (baseY - topY) * 0.2f,
            topX - topW * 0.55f, topY + 4f)
    }
    drawPath(bark1, Color(0x30FFFFFF), style = Stroke(width = baseW * 0.06f, cap = StrokeCap.Round))

    val bark2 = Path().apply {
        moveTo(baseX + baseW * 0.4f, baseY)
        cubicTo(baseX + baseW * 0.3f, baseY - (baseY - topY) * 0.25f,
            topX + topW * 0.55f, topY + (baseY - topY) * 0.15f,
            topX + topW * 0.5f, topY + 6f)
    }
    drawPath(bark2, Color(0x20FFFFFF), style = Stroke(width = baseW * 0.05f, cap = StrokeCap.Round))
}

// ═══════════════════════════════════════════════════════
// ROOT FLARES
// ═══════════════════════════════════════════════════════
private fun DrawScope.drawRootFlares(x: Float, y: Float, s: Float) {
    val roots = listOf(
        Triple(-28f, 4f, -52f to 14f),
        Triple(28f, 4f, 50f to 13f),
        Triple(-14f, 2f, -34f to 20f),
        Triple(14f, 2f, 32f to 20f)
    )
    for ((ox, oy, end) in roots) {
        val rootPath = Path().apply {
            moveTo(x + ox * s, y + oy * s)
            cubicTo(
                x + ox * s * 0.6f, y + oy * s + 6f * s,
                x + end.first * s * 0.7f, y + end.second * s * 0.7f,
                x + end.first * s, y + end.second * s
            )
        }
        val w = (6f - kotlin.math.abs(ox) * 0.06f).coerceAtLeast(3f) * s
        drawPath(rootPath, TRUNK_BASE, style = Stroke(width = w, cap = StrokeCap.Round))
        drawPath(rootPath, TRUNK_MAIN, style = Stroke(width = w * 0.5f, cap = StrokeCap.Round))
    }
}

// ═══════════════════════════════════════════════════════
// BRANCH helper
// ═══════════════════════════════════════════════════════
private fun DrawScope.drawBranch(
    fromX: Float, fromY: Float,
    toX: Float, toY: Float,
    width: Float
) {
    drawLine(TRUNK_BASE, Offset(fromX, fromY), Offset(toX, toY), strokeWidth = width * 1.1f, cap = StrokeCap.Round)
    drawLine(TRUNK_MAIN, Offset(fromX, fromY), Offset(toX, toY), strokeWidth = width * 0.8f, cap = StrokeCap.Round)
    drawLine(TRUNK_MID,  Offset(fromX, fromY), Offset(toX, toY), strokeWidth = width * 0.3f, cap = StrokeCap.Round)
}

// ═══════════════════════════════════════════════════════
// GROUND MOUND
// ═══════════════════════════════════════════════════════
private fun DrawScope.drawGroundMound(x: Float, y: Float, radius: Float, s: Float) {
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x20FFFFFF), Color(0x00000000)),
            center = Offset(x, y + 4f * s),
            radius = radius * 1.4f
        ),
        topLeft = Offset(x - radius * 1.4f, y - 10f * s),
        size = Size(radius * 2.8f, 32f * s)
    )
    val moundPath = Path().apply {
        moveTo(x - radius, y + 6f * s)
        cubicTo(x - radius * 0.6f, y - 10f * s, x + radius * 0.6f, y - 10f * s, x + radius, y + 6f * s)
        cubicTo(x + radius * 0.5f, y + 16f * s, x - radius * 0.5f, y + 16f * s, x - radius, y + 6f * s)
        close()
    }
    drawPath(moundPath, SOIL_BASE,    style = Fill)
    drawPath(moundPath, SOIL_SURFACE, style = Stroke(width = 1.8f * s))
    // pebbles
    for ((ox, oy, r) in listOf(
        Triple(-0.45f, 2f, 2.8f), Triple(0.35f, 1f, 3.4f),
        Triple(-0.12f, 5f, 2.0f), Triple(0.6f, 4f, 2.3f)
    )) {
        drawCircle(SOIL_HIGHLIGHT, r * s, Offset(x + radius * ox, y + oy * s))
    }
}

// ═══════════════════════════════════════════════════════
// STAGE 1: SEED
// ═══════════════════════════════════════════════════════
private fun DrawScope.drawDetailedSeedWithGround(
    x: Float, y: Float, s: Float,
    glow: Float, breath: Float, particleAngle: Float
) {
    val soilW = 140f * s
    val seedScale = s * 1.5f * breath
    val seedX = x
    val seedY = y - 4f * s

    // Glow orb behind seed
    drawGlowOrb(seedX, seedY - 10f * s, 50f * s, glow, particleAngle, 5)

    // Soil bed
    drawOval(color = Color(0x50000000),
        topLeft = Offset(x - soilW * 1.05f, y + 8f * s),
        size = Size(soilW * 2.1f, soilW * 0.38f))
    val soilPath = Path().apply {
        moveTo(x - soilW, y + 12f * s)
        cubicTo(x - soilW * 0.6f, y - 14f * s, x + soilW * 0.6f, y - 14f * s, x + soilW, y + 12f * s)
        cubicTo(x + soilW * 0.7f, y + 26f * s, x - soilW * 0.7f, y + 26f * s, x - soilW, y + 12f * s)
        close()
    }
    drawPath(soilPath, SOIL_MID, style = Fill)
    val rimPath = Path().apply {
        moveTo(x - soilW * 0.95f, y + 10f * s)
        cubicTo(x - soilW * 0.55f, y - 12f * s, x + soilW * 0.55f, y - 12f * s, x + soilW * 0.95f, y + 10f * s)
    }
    drawPath(rimPath, SOIL_HIGHLIGHT, style = Stroke(width = 2.2f * s, cap = StrokeCap.Round))
    for ((ox, oy, r) in listOf(Triple(-0.5f, 2f, 4.5f), Triple(-0.35f, 8f, 3.5f),
            Triple(0.45f, 4f, 5f), Triple(0.65f, 6f, 3f))) {
        drawCircle(SOIL_HIGHLIGHT, r * s, Offset(x + soilW * ox, y + oy * s))
    }

    // Root tendrils
    for ((cp1, cp2, ep) in listOf(
        Triple(Offset(x - 8f*s, y+14f*s), Offset(x-18f*s, y+20f*s), Offset(x-26f*s, y+24f*s)),
        Triple(Offset(x + 6f*s, y+12f*s), Offset(x+15f*s, y+18f*s), Offset(x+24f*s, y+22f*s))
    )) {
        val rp = Path().apply { moveTo(x, y+2f*s); cubicTo(cp1.x,cp1.y,cp2.x,cp2.y,ep.x,ep.y) }
        drawPath(rp, TRUNK_MID, style = Stroke(width = 2f*s, cap = StrokeCap.Round))
        drawPath(rp, TRUNK_HIGHLIGHT, style = Stroke(width = 0.8f*s, cap = StrokeCap.Round))
    }

    // Seed husk
    val seedHusk = Path().apply {
        moveTo(seedX, seedY - 18f*seedScale)
        cubicTo(seedX+16f*seedScale,seedY-14f*seedScale, seedX+16f*seedScale,seedY+12f*seedScale, seedX,seedY+20f*seedScale)
        cubicTo(seedX-16f*seedScale,seedY+12f*seedScale, seedX-16f*seedScale,seedY-14f*seedScale, seedX,seedY-18f*seedScale)
        close()
    }
    drawPath(seedHusk, TRUNK_MAIN, style = Fill)
    drawPath(seedHusk, TRUNK_MID, style = Stroke(width = 2f*seedScale))
    val innerSeed = Path().apply {
        moveTo(seedX, seedY-12f*seedScale)
        cubicTo(seedX+9f*seedScale,seedY-8f*seedScale, seedX+9f*seedScale,seedY+8f*seedScale, seedX,seedY+13f*seedScale)
        cubicTo(seedX-9f*seedScale,seedY+8f*seedScale, seedX-9f*seedScale,seedY-8f*seedScale, seedX,seedY-12f*seedScale)
        close()
    }
    drawPath(innerSeed, CANOPY_WHITE, style = Fill)

    // Shoot
    val shootTipY = seedY - 45f*seedScale
    val shootPath = Path().apply {
        moveTo(seedX, seedY-14f*seedScale)
        cubicTo(seedX-4f*seedScale,seedY-26f*seedScale, seedX+6f*seedScale,seedY-36f*seedScale, seedX+2f*seedScale,shootTipY)
    }
    drawPath(shootPath, TRUNK_MID, style = Stroke(width=5f*seedScale, cap=StrokeCap.Round))
    drawPath(shootPath, CANOPY_WHITE, style = Stroke(width=2f*seedScale, cap=StrokeCap.Round))

    drawLeaf(seedX-10f*seedScale, shootTipY+4f*seedScale, -40f, 18f*seedScale, CANOPY_HILIGHT)
    drawLeaf(seedX+12f*seedScale, shootTipY+2f*seedScale,  38f, 20f*seedScale, CANOPY_WHITE)
    drawCircle(CANOPY_WHITE, 4f*seedScale*glow, Offset(seedX, seedY))
    drawCircle(Color(0x50FFFFFF), 8f*seedScale*glow, Offset(seedX, seedY))
}

// ═══════════════════════════════════════════════════════
// STAGE 2: SPROUT
// ═══════════════════════════════════════════════════════
private fun DrawScope.drawDetailedSprout(
    x: Float, y: Float, sway: Float, s: Float,
    glow: Float, particleAngle: Float
) {
    val sc = s * 1.4f
    val tipX = x + sway * 2f
    val tipY = y - 90f * sc

    // Glow orb behind canopy
    drawGlowOrb(tipX, tipY - 10f*sc, 38f*sc, glow, particleAngle, 4)

    // Organic stem
    val stemPath = Path().apply {
        moveTo(x, y)
        cubicTo(x - 8f*sc, y - 30f*sc, tipX - 4f*sc, y - 60f*sc, tipX, tipY)
    }
    drawPath(stemPath, TRUNK_BASE,      style = Stroke(width=7f*sc, cap=StrokeCap.Round))
    drawPath(stemPath, TRUNK_MAIN,      style = Stroke(width=5f*sc, cap=StrokeCap.Round))
    drawPath(stemPath, TRUNK_HIGHLIGHT, style = Stroke(width=2f*sc, cap=StrokeCap.Round))

    // Side leaves
    drawLeaf(x - 22f*sc, y - 45f*sc, -50f, 22f*sc, CANOPY_MID)
    drawLeaf(x + 24f*sc, y - 55f*sc,  45f, 24f*sc, CANOPY_LIGHT)

    // Foliage spheres (detailed)
    drawFoliageSphere(tipX - 11f*sc, tipY + 8f*sc,  26f*sc)
    drawFoliageSphere(tipX + 9f*sc,  tipY + 4f*sc,  24f*sc)
    drawFoliageSphere(tipX,          tipY - 12f*sc,  28f*sc)
}

// ═══════════════════════════════════════════════════════
// STAGE 3: PLANT / YOUNG BONSAI
// ═══════════════════════════════════════════════════════
private fun DrawScope.drawDetailedPlant(
    x: Float, y: Float, sway: Float, s: Float,
    glow: Float, particleAngle: Float
) {
    val sc = s * 1.22f
    val trunkTopY = y - 130f*sc
    val swayX = x + sway * 3f

    // Glow orb
    drawGlowOrb(swayX, trunkTopY - 30f*sc, 58f*sc, glow, particleAngle, 5)

    // Trunk
    drawRichTrunk(x, y, swayX, trunkTopY, 14f*sc, 7f*sc)

    // Branches
    val midY = y - 60f*sc
    val midX = x + (swayX - x) * 0.5f
    drawBranch(midX, midY, swayX - 46f*sc, trunkTopY + 30f*sc, 7f*sc)
    drawBranch(midX, midY, swayX + 44f*sc, trunkTopY + 25f*sc, 6.5f*sc)
    drawBranch(midX, midY, swayX,           trunkTopY - 10f*sc, 8f*sc)

    // Foliage
    drawFoliageSphere(swayX - 46f*sc, trunkTopY + 14f*sc,  34f*sc)
    drawFoliageSphere(swayX + 44f*sc, trunkTopY + 10f*sc,  32f*sc)
    drawFoliageSphere(swayX,           trunkTopY - 30f*sc,  42f*sc)
}

// ═══════════════════════════════════════════════════════
// STAGE 4: MAJESTIC TREE
// ═══════════════════════════════════════════════════════
private fun DrawScope.drawDetailedTree(
    x: Float, y: Float, sway: Float, s: Float,
    glow: Float, particleAngle: Float, extraScale: Float
) {
    val sc = s * extraScale
    val swayX = x + sway * 3.5f
    val trunkTopY = y - 160f * sc

    // Glow orb — drawn first so tree is on top
    drawGlowOrb(swayX, trunkTopY - 38f*sc, 88f*sc, glow, particleAngle, 7)

    // Root flares
    drawRootFlares(x, y, sc)

    // Trunk
    drawRichTrunk(x, y, swayX, trunkTopY, 24f*sc, 11f*sc)

    // Branches
    val midY = y - 72f*sc
    val midX = x + (swayX - x) * 0.45f
    val upperY = y - 118f*sc
    val upperX = x + (swayX - x) * 0.7f

    drawBranch(midX, midY, swayX - 72f*sc, trunkTopY + 45f*sc, 12f*sc)
    drawBranch(midX, midY, swayX + 70f*sc, trunkTopY + 40f*sc, 11f*sc)
    drawBranch(upperX, upperY, swayX - 52f*sc, trunkTopY + 8f*sc,  8.5f*sc)
    drawBranch(upperX, upperY, swayX + 50f*sc, trunkTopY + 2f*sc,  8f*sc)
    drawBranch(upperX, upperY, swayX,           trunkTopY - 18f*sc, 9f*sc)

    // Sub-branches
    drawBranch(swayX - 72f*sc, trunkTopY + 45f*sc, swayX - 96f*sc, trunkTopY + 28f*sc, 5.5f*sc)
    drawBranch(swayX + 70f*sc, trunkTopY + 40f*sc, swayX + 92f*sc, trunkTopY + 22f*sc, 5f*sc)

    // Foliage cloud — 5 main spheres matching reference
    drawFoliageSphere(swayX - 84f*sc, trunkTopY + 30f*sc,  40f*sc)
    drawFoliageSphere(swayX + 82f*sc, trunkTopY + 24f*sc,  38f*sc)
    drawFoliageSphere(swayX - 46f*sc, trunkTopY - 2f*sc,   46f*sc)
    drawFoliageSphere(swayX + 44f*sc, trunkTopY - 8f*sc,   44f*sc)
    drawFoliageSphere(swayX,           trunkTopY - 36f*sc,  58f*sc)
}

// ═══════════════════════════════════════════════════════
// STAGE 5: GARDEN
// ═══════════════════════════════════════════════════════
private fun DrawScope.drawDetailedGarden(
    x: Float, y: Float, sway: Float, s: Float,
    glow: Float, particleAngle: Float
) {
    drawDetailedPlant(x - 88f*s, y + 5f, sway * 0.7f, s * 0.64f, glow, particleAngle)
    drawDetailedSprout(x + 90f*s, y + 8f, sway * -0.6f, s * 0.68f, glow, particleAngle)
    drawDetailedTree(x, y, sway, s, glow, particleAngle, 1.05f)
}

// ═══════════════════════════════════════════════════════
// STAGE 6: FOREST
// ═══════════════════════════════════════════════════════
private fun DrawScope.drawDetailedForest(
    x: Float, y: Float, sway: Float, s: Float,
    glow: Float, particleAngle: Float
) {
    drawDetailedPlant(x - 108f*s, y + 5f, sway * 0.5f, s * 0.54f, glow, particleAngle)
    drawDetailedPlant(x + 112f*s, y + 6f, sway * -0.7f, s * 0.56f, glow, particleAngle)
    drawDetailedSprout(x - 52f*s, y + 10f, sway * 0.8f,  s * 0.5f, glow, particleAngle)
    drawDetailedSprout(x + 58f*s, y + 12f, sway * -0.5f, s * 0.52f, glow, particleAngle)
    drawDetailedTree(x, y, sway, s, glow, particleAngle, 1.15f)
}

// ═══════════════════════════════════════════════════════
// HELPER: Organic leaf shape
// ═══════════════════════════════════════════════════════
private fun DrawScope.drawLeaf(x: Float, y: Float, angleDeg: Float, size: Float, color: Color) {
    val leaf = Path().apply {
        moveTo(x, y)
        cubicTo(x - size*0.5f, y - size*0.7f, x - size*0.2f, y - size, x, y - size*1.2f)
        cubicTo(x + size*0.2f, y - size, x + size*0.5f, y - size*0.7f, x, y)
        close()
    }
    drawPath(leaf, color, style = Fill)
    // Leaf vein
    val vein = Path().apply {
        moveTo(x, y)
        lineTo(x, y - size * 1.1f)
    }
    drawPath(vein, Color(0x30000000), style = Stroke(width = size * 0.06f, cap = StrokeCap.Round))
}
