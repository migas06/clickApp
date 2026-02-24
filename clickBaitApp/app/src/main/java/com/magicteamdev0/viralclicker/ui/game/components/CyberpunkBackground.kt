package com.magicteamdev0.viralclicker.ui.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════
//  CYBERPUNK BACKGROUND — Animated city skyline
// ═══════════════════════════════════════════════

@Composable
fun CyberpunkBackground(modifier: Modifier = Modifier) {
    // Slow pulsing glow for neon city lights
    val infiniteTransition = rememberInfiniteTransition(label = "city_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.40f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "city_glow_alpha"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val horizonY = h * 0.56f

        // ── Gradient sky background ──
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF08080E),
                    Color(0xFF0C1020),
                    Color(0xFF0A1428)
                ),
                startY = 0f,
                endY = h
            )
        )

        // ── Ambient purple haze near top ──
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1A0835).copy(alpha = 0.30f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.12f),
                radius = w * 0.75f
            )
        )

        // ── Ambient cyan glow on horizon (center) ──
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF003355).copy(alpha = 0.40f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, horizonY),
                radius = w * 0.60f
            )
        )

        // ── City silhouette and buildings ──
        drawCityscape(horizonY = horizonY, glowAlpha = glowAlpha)

        // ── Ground darkening ──
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF08080E).copy(alpha = 0.85f)
                ),
                startY = horizonY,
                endY = h
            ),
            topLeft = Offset(0f, horizonY),
            size = Size(w, h - horizonY)
        )

        // ── Horizon neon glow line ──
        val glowW = w
        // Wide soft glow band
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF00E5FF).copy(alpha = glowAlpha * 0.25f),
                    Color(0xFF9B59FF).copy(alpha = glowAlpha * 0.35f),
                    Color(0xFF00E5FF).copy(alpha = glowAlpha * 0.25f),
                    Color.Transparent
                ),
                startX = 0f,
                endX = glowW
            ),
            topLeft = Offset(0f, horizonY - 12f),
            size = Size(w, 24f)
        )
        // Sharp horizon line
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF00E5FF).copy(alpha = glowAlpha * 0.7f),
                    Color(0xFFFFFFFF).copy(alpha = glowAlpha * 0.4f),
                    Color(0xFF00E5FF).copy(alpha = glowAlpha * 0.7f),
                    Color.Transparent
                ),
                startX = 0f,
                endX = w
            ),
            start = Offset(0f, horizonY),
            end = Offset(w, horizonY),
            strokeWidth = 1.5f
        )

        // ── Scan lines (very subtle) ──
        val lineSpacing = 8f
        var lineY = 0f
        while (lineY < h) {
            drawLine(
                color = Color.White.copy(alpha = 0.018f),
                start = Offset(0f, lineY),
                end = Offset(w, lineY),
                strokeWidth = 0.5f
            )
            lineY += lineSpacing
        }
    }
}

// ─────────────────────────────────────────────
//  City silhouette drawing
// ─────────────────────────────────────────────

private fun DrawScope.drawCityscape(horizonY: Float, glowAlpha: Float) {
    val w = size.width

    // Deterministic building layout (xFrac, widthFrac, heightFrac-above-horizon)
    val buildings = listOf(
        Triple(0.00f, 0.055f, 0.20f),
        Triple(0.055f, 0.040f, 0.14f),
        Triple(0.095f, 0.050f, 0.26f),
        Triple(0.145f, 0.038f, 0.17f),
        Triple(0.183f, 0.060f, 0.34f),
        Triple(0.243f, 0.038f, 0.19f),
        Triple(0.281f, 0.068f, 0.44f),   // tall left-center
        Triple(0.349f, 0.030f, 0.23f),
        Triple(0.379f, 0.052f, 0.30f),
        Triple(0.431f, 0.038f, 0.16f),
        Triple(0.469f, 0.062f, 0.48f),   // tallest (center)
        Triple(0.531f, 0.038f, 0.28f),
        Triple(0.569f, 0.068f, 0.41f),   // tall right-center
        Triple(0.637f, 0.038f, 0.21f),
        Triple(0.675f, 0.052f, 0.32f),
        Triple(0.727f, 0.038f, 0.15f),
        Triple(0.765f, 0.060f, 0.37f),
        Triple(0.825f, 0.038f, 0.20f),
        Triple(0.863f, 0.050f, 0.25f),
        Triple(0.913f, 0.038f, 0.13f),
        Triple(0.951f, 0.049f, 0.17f),
    )

    // Draw from back to front: first pass fills all building silhouettes
    for ((xFrac, wFrac, hFrac) in buildings) {
        val bx = xFrac * w
        val bw = wFrac * w
        val bh = hFrac * horizonY
        val by = horizonY - bh

        // Building body
        drawRect(
            color = Color(0xFF0C1828),
            topLeft = Offset(bx, by),
            size = Size(bw, bh)
        )

        // Subtle left-edge rim light (neon reflection from city)
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF00E5FF).copy(alpha = 0.06f),
                    Color.Transparent
                ),
                startX = bx,
                endX = bx + (bw * 0.25f).coerceAtLeast(2f)
            ),
            topLeft = Offset(bx, by),
            size = Size(bw, bh)
        )

        // Draw windows
        drawBuildingWindows(bx, by, bw, bh, glowAlpha)
    }

    // Draw spires on tallest buildings
    val tallBuildings = buildings.filter { it.third >= 0.38f }
    for ((xFrac, wFrac, hFrac) in tallBuildings) {
        val cx = (xFrac + wFrac / 2f) * w
        val topY = horizonY - hFrac * horizonY
        drawAntenna(cx, topY, glowAlpha)
    }
}

private fun DrawScope.drawBuildingWindows(
    bx: Float, by: Float, bw: Float, bh: Float,
    glowAlpha: Float
) {
    val winW = (bw * 0.20f).coerceAtLeast(3f)
    val winH = (bh * 0.065f).coerceAtLeast(2f)
    val colCount = ((bw / (winW * 2.8f)).roundToInt()).coerceIn(1, 4)
    val rowCount = ((bh / (winH * 3.2f)).roundToInt()).coerceIn(1, 9)

    val windowColors = listOf(
        Color(0xFF00E5FF),   // cyan
        Color(0xFF9B59FF),   // purple
        Color(0xFFFFD700),   // gold (rare)
        Color(0xFF00E5FF),   // cyan (weighted more)
        Color(0xFF9B59FF),   // purple (weighted more)
    )

    for (row in 0 until rowCount) {
        for (col in 0 until colCount) {
            val seed = (row * 7 + col * 13 + (bx / 3).toInt() + (bh / 2).toInt()) and 0x7FFFFFFF
            val lit = (seed % 3) != 0   // ~66% lit
            if (!lit) continue

            val wx = bx + (col + 0.5f) * (bw / (colCount.toFloat() + 1f))
            val wy = by + (row + 1f) * (bh / (rowCount.toFloat() + 2f))
            val wColor = windowColors[seed % windowColors.size]
            val wAlpha = 0.25f + glowAlpha * 0.20f

            // Glow halo
            drawRect(
                color = wColor.copy(alpha = glowAlpha * 0.10f),
                topLeft = Offset(wx - winW * 0.9f, wy - winH * 0.9f),
                size = Size(winW * 1.8f, winH * 1.8f)
            )
            // Window fill
            drawRect(
                color = wColor.copy(alpha = wAlpha),
                topLeft = Offset(wx - winW / 2f, wy - winH / 2f),
                size = Size(winW, winH)
            )
        }
    }
}

private fun DrawScope.drawAntenna(cx: Float, topY: Float, glowAlpha: Float) {
    val spireH = 28f

    // Antenna line
    drawLine(
        color = Color(0xFF00E5FF).copy(alpha = 0.45f + glowAlpha * 0.25f),
        start = Offset(cx, topY),
        end = Offset(cx, topY - spireH),
        strokeWidth = 1.2f
    )

    // Small horizontal crossbar
    drawLine(
        color = Color(0xFF00E5FF).copy(alpha = 0.30f),
        start = Offset(cx - 5f, topY - spireH * 0.6f),
        end = Offset(cx + 5f, topY - spireH * 0.6f),
        strokeWidth = 1f
    )

    // Pulsing beacon tip
    drawCircle(
        color = Color(0xFFFF2D78).copy(alpha = 0.55f + glowAlpha * 0.45f),
        radius = 2.2f,
        center = Offset(cx, topY - spireH)
    )

    // Beacon outer glow
    drawCircle(
        color = Color(0xFFFF2D78).copy(alpha = glowAlpha * 0.18f),
        radius = 6f,
        center = Offset(cx, topY - spireH)
    )
}
