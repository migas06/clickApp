package com.example.viralclicker.ui.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viralclicker.R
import com.example.viralclicker.ui.theme.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun MilestoneCelebration(
    milestoneName: String,
    onDismiss: () -> Unit
) {
    // Auto-dismiss after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000L)
        onDismiss()
    }

    // Screen flash
    val flashAlpha = remember { Animatable(0.5f) }
    LaunchedEffect(Unit) {
        flashAlpha.animateTo(0f, tween(600))
    }

    // Card slide-up animation
    val slideOffset = remember { Animatable(300f) }
    val cardAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        coroutineScope {
            launch { slideOffset.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 300f)) }
            launch { cardAlpha.animateTo(1f, tween(300)) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        // Confetti layer
        ConfettiOverlay()

        // Flash overlay
        Box(
            Modifier
                .fillMaxSize()
                .background(GoldAccent.copy(alpha = flashAlpha.value))
        )

        // Achievement card
        Card(
            modifier = Modifier
                .offset(y = slideOffset.value.dp)
                .graphicsLayer(alpha = cardAlpha.value)
                .padding(32.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(2.dp, GoldAccent)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text("🏆", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.milestone_unlocked_banner),
                    color = GoldAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    milestoneName,
                    color = OnDark,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ConfettiOverlay — Performance-optimised
//
//  Positions are computed deterministically from elapsed time (no mutable
//  particle state and no draw-phase side-effects).  The animation clock is
//  read inside the Canvas lambda (drawBehind scope), so Compose only
//  invalidates the draw node — NOT the composition tree — producing 0
//  recompositions while confetti is visible.
// ─────────────────────────────────────────────────────────────────────────────

private data class ConfettoInit(
    val x0: Float, val y0: Float,
    val vx: Float, val vy0: Float,
    val rotationOffset: Float,
    val color: Color, val size: Float
)

@Composable
private fun ConfettiOverlay() {
    val colors = remember { listOf(NeonPurple, NeonCyan, NeonPink, GoldAccent, NeonGreen) }

    // Immutable initial state — never mutated
    val confetti = remember {
        List(60) {
            ConfettoInit(
                x0 = Random.nextFloat() * 1200f,
                y0 = -Random.nextFloat() * 600f,
                vx = (Random.nextFloat() - 0.5f) * 5f,
                vy0 = Random.nextFloat() * 6f + 3f,
                rotationOffset = Random.nextFloat() * 360f,
                color = colors.random(),
                size = Random.nextFloat() * 7f + 3f
            )
        }
    }

    val startTimeMs = remember { System.currentTimeMillis() }

    // Clock value read only inside Canvas (draw scope) → only draw is invalidated
    val infiniteTransition = rememberInfiniteTransition(label = "confetti_clock")
    val clockState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "confetti_t"
    ) // NOT destructured with 'by' — we need the State<Float> object

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw-scope read: only invalidates draw, does NOT recompose parent
        @Suppress("UNUSED_VARIABLE")
        val clock = clockState.value

        val t = (System.currentTimeMillis() - startTimeMs) / 16.67f // frames at ~60 fps

        confetti.forEach { c ->
            val x = c.x0 + c.vx * t
            val y = c.y0 + c.vy0 * t + 0.1f * t * t  // y₀ + v·t + ½g·t²
            val rotation = (c.rotationOffset + 3f * t) % 360f

            rotate(rotation, pivot = Offset(x, y)) {
                drawRect(
                    color = c.color,
                    topLeft = Offset(x, y),
                    size = Size(c.size, c.size * 2.5f)
                )
            }
        }
    }
}
