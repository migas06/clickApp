package com.magicteamdev0.viralclicker.ui.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.magicteamdev0.viralclicker.ui.theme.*
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════
//  CLICKER ELEMENT — Neon Triangle Button
// ═══════════════════════════════════════════════

@Composable
fun ClickerElement(
    skinId: String,
    onTap: () -> Unit,
    comboMultiplier: Double = 1.0,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    // Glow color changes with combo tier
    val glowColor = when {
        comboMultiplier >= 5.0 -> NeonPink
        comboMultiplier >= 3.0 -> GoldAccent
        else -> NeonCyan
    }
    val ringColor = when {
        comboMultiplier >= 5.0 -> NeonPurple
        comboMultiplier >= 3.0 -> NeonPink
        else -> NeonPurple
    }

    // Dynamic glow intensity from combo
    val glowAlpha by animateFloatAsState(
        targetValue = (0.30f + ((comboMultiplier - 1.0) * 0.10f).toFloat()).coerceAtMost(0.90f),
        animationSpec = tween(300),
        label = "glow_alpha"
    )

    // Infinite pulse for outer rings
    val infiniteTransition = rememberInfiniteTransition(label = "clicker_pulse")
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.52f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(220.dp)
            .scale(scale.value)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    coroutineScope.launch {
                        scale.animateTo(0.88f, animationSpec = tween(60))
                        scale.animateTo(
                            1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        )
                    }
                    onTap()
                    tryAwaitRelease()
                })
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxR = size.minDimension / 2f

            // ── Outer ambient glow rings (concentric, fading) ──
            drawCircle(
                color = glowColor.copy(alpha = ringPulse * 0.06f),
                radius = maxR * 0.98f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = ringColor.copy(alpha = ringPulse * 0.10f),
                radius = maxR * 0.82f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = glowColor.copy(alpha = ringPulse * 0.14f),
                radius = maxR * 0.66f,
                center = Offset(cx, cy)
            )

            // ── Center radial fill ──
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ringColor.copy(alpha = glowAlpha * 0.18f),
                        glowColor.copy(alpha = glowAlpha * 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = maxR * 0.60f
                ),
                radius = maxR * 0.60f,
                center = Offset(cx, cy)
            )

            // ── Triangle path (pointing upward) ──
            val triSize = maxR * 0.64f
            val triCY = cy - triSize * 0.06f   // slightly above center

            val trianglePath = Path().apply {
                // Top vertex
                moveTo(cx, triCY - triSize * 0.78f)
                // Bottom-right vertex
                lineTo(cx + triSize * 0.68f, triCY + triSize * 0.46f)
                // Bottom-left vertex
                lineTo(cx - triSize * 0.68f, triCY + triSize * 0.46f)
                close()
            }

            // Triangle inner fill (subtle)
            drawPath(
                path = trianglePath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        ringColor.copy(alpha = 0.20f),
                        glowColor.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(cx, triCY),
                    radius = triSize
                )
            )

            // Triangle glow stroke (multi-pass for bloom effect)
            drawPath(
                path = trianglePath,
                color = glowColor.copy(alpha = glowAlpha * 0.25f),
                style = Stroke(width = 18f)
            )
            drawPath(
                path = trianglePath,
                color = glowColor.copy(alpha = glowAlpha * 0.40f),
                style = Stroke(width = 10f)
            )
            drawPath(
                path = trianglePath,
                color = glowColor.copy(alpha = glowAlpha * 0.60f),
                style = Stroke(width = 5f)
            )
            // Sharp neon edge
            drawPath(
                path = trianglePath,
                color = glowColor,
                style = Stroke(width = 2.5f)
            )
            // Inner highlight
            drawPath(
                path = trianglePath,
                color = Color.White.copy(alpha = 0.25f),
                style = Stroke(width = 1f)
            )

            // ── Apex spark (top of triangle) ──
            val apexY = triCY - triSize * 0.78f
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = 2.5f,
                center = Offset(cx, apexY)
            )
            drawCircle(
                color = glowColor.copy(alpha = 0.60f),
                radius = 6f,
                center = Offset(cx, apexY)
            )

            // ── Corner sparks (bottom two vertices) ──
            val bRightX = cx + triSize * 0.68f
            val bLeftX = cx - triSize * 0.68f
            val bY = triCY + triSize * 0.46f
            val sparkRadius = 3f

            drawCircle(
                color = ringColor.copy(alpha = 0.7f),
                radius = sparkRadius,
                center = Offset(bRightX, bY)
            )
            drawCircle(
                color = ringColor.copy(alpha = 0.7f),
                radius = sparkRadius,
                center = Offset(bLeftX, bY)
            )

            // ── Platform ellipse at base ──
            val platY = triCY + triSize * 0.52f
            val platW = triSize * 1.80f
            val platH = triSize * 0.22f

            // Outer glow
            drawOval(
                color = glowColor.copy(alpha = glowAlpha * 0.22f),
                topLeft = Offset(cx - platW * 0.65f, platY - platH * 2f),
                size = Size(platW * 1.30f, platH * 4f)
            )
            // Primary platform ring
            drawOval(
                color = glowColor.copy(alpha = 0.75f),
                topLeft = Offset(cx - platW / 2f, platY - platH / 2f),
                size = Size(platW, platH),
                style = Stroke(width = 2f)
            )
            // Inner platform ring
            drawOval(
                color = ringColor.copy(alpha = 0.45f),
                topLeft = Offset(cx - platW * 0.40f, platY - platH * 0.38f),
                size = Size(platW * 0.80f, platH * 0.76f),
                style = Stroke(width = 1.2f)
            )
            // Platform fill gradient (ground shadow)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(cx, platY),
                    radius = platW / 2f
                ),
                topLeft = Offset(cx - platW / 2f, platY - platH),
                size = Size(platW, platH * 2f)
            )
        }
    }
}
