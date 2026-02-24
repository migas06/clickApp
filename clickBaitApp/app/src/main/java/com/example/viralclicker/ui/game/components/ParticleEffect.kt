package com.example.viralclicker.ui.game.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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

@Composable
fun ParticleEffect(
    isActive: Boolean,
    particleColor: Color,
    secondaryColor: Color = particleColor,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val particles = remember { mutableStateListOf<Particle>() }

    LaunchedEffect(isActive) {
        while (isActive) {
            // Spawn 2-3 particles per frame around a circle
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
            delay(50L)
        }
    }

    // Clear particles when deactivated
    LaunchedEffect(isActive) {
        if (!isActive) particles.clear()
    }

    Canvas(modifier = modifier) {
        val toRemove = mutableListOf<Particle>()
        particles.forEach { p ->
            p.x += p.vx
            p.y += p.vy
            p.life -= 0.025f
            p.alpha = p.life.coerceIn(0f, 1f)
            if (p.life <= 0f) {
                toRemove.add(p)
            } else {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.radius * p.life,
                    center = Offset(p.x, p.y)
                )
            }
        }
        particles.removeAll(toRemove.toSet())
    }
}
