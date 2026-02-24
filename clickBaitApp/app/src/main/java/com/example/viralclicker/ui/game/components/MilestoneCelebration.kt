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

@Composable
private fun ConfettiOverlay() {
    val colors = listOf(NeonPurple, NeonCyan, NeonPink, GoldAccent, NeonGreen)
    val confetti = remember {
        List(60) {
            ConfettoState(
                x = Random.nextFloat() * 1200f,
                y = -Random.nextFloat() * 600f,
                vx = (Random.nextFloat() - 0.5f) * 5f,
                vy = Random.nextFloat() * 6f + 3f,
                rotation = Random.nextFloat() * 360f,
                color = colors.random(),
                size = Random.nextFloat() * 7f + 3f
            )
        }
    }

    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L) // ~60 FPS
            tick++
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Reference tick to trigger recomposition
        tick.let { }
        confetti.forEach { c ->
            c.x += c.vx
            c.y += c.vy
            c.vy += 0.2f // gravity
            c.rotation += 3f
            rotate(c.rotation, pivot = Offset(c.x, c.y)) {
                drawRect(
                    c.color,
                    topLeft = Offset(c.x, c.y),
                    size = Size(c.size, c.size * 2.5f)
                )
            }
        }
    }
}

private data class ConfettoState(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var color: Color,
    var size: Float
)
