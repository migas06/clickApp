package com.example.viralclicker.ui.game.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.withFrameMillis
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    var radius: Float,
    var color: Color,
    var life: Float
)

// ─────────────────────────────────────────────────────────────────────────────
//  ParticleEffect — Performance-optimised
//
//  Previous version used mutableStateListOf, so every particles.add() and
//  removeAll() inside the draw scope triggered a full recomposition (~20/sec
//  during combos, plus extra from draw-phase mutations).
//
//  New approach:
//  • particles: regular mutableListOf — never observed by composition
//  • Spawning & physics updates run via withFrameMillis (main thread, outside draw)
//  • drawTick: plain Int state read only inside the Canvas lambda (draw scope)
//    → only invalidates the draw node, zero recompositions
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ParticleEffect(
    isActive: Boolean,
    particleColor: Color,
    secondaryColor: Color = particleColor,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    // Plain list — NOT Compose state, so add/remove never triggers recomposition
    val particles = remember { mutableListOf<Particle>() }

    // Spawn particles on a slower timer (20 fps equivalent)
    LaunchedEffect(isActive) {
        while (isActive) {
            delay(50L)
            repeat(Random.nextInt(2, 4)) {
                val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
                val spawnRadius = 250f
                val color = if (Random.nextBoolean()) particleColor else secondaryColor
                particles.add(
                    Particle(
                        x = (cos(angle) * spawnRadius) + spawnRadius,
                        y = (sin(angle) * spawnRadius) + spawnRadius,
                        vx = (Random.nextFloat() - 0.5f) * 3f,
                        vy = -Random.nextFloat() * 5f - 1f,
                        alpha = 1f,
                        radius = Random.nextFloat() * 3f + 1.5f,
                        color = color,
                        life = 1f
                    )
                )
            }
        }
    }

    // Update physics per vsync frame and signal canvas to redraw (draw-scope only)
    var drawTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(isActive) {
        while (isActive) {
            withFrameMillis {
                val iter = particles.iterator()
                while (iter.hasNext()) {
                    val p = iter.next()
                    p.x += p.vx
                    p.y += p.vy
                    p.life -= 0.025f
                    p.alpha = p.life.coerceIn(0f, 1f)
                    if (p.life <= 0f) iter.remove()
                }
                drawTick++ // draw-scope state: only invalidates Canvas draw node
            }
        }
    }

    Canvas(modifier = modifier) {
        drawTick.let {} // Draw-scope read — triggers redraw only, NOT recomposition
        particles.forEach { p ->
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.radius * p.life,
                center = Offset(p.x, p.y)
            )
        }
    }
}
