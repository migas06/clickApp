package com.magicteamdev0.viralclicker.ui.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magicteamdev0.viralclicker.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ClickerElement(
    skinId: String,
    onTap: () -> Unit,
    comboMultiplier: Double = 1.0,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    val emoji = when (skinId) {
        "galaxy_meme" -> "🌌"
        "fire_meme"   -> "🔥"
        else          -> "😂"
    }

    // Dynamic glow intensity based on combo multiplier
    val glowAlpha by animateFloatAsState(
        targetValue = (0.2f + ((comboMultiplier - 1.0) * 0.15f).toFloat()).coerceAtMost(0.8f),
        animationSpec = tween(300),
        label = "glow_alpha"
    )

    val glowColor = when {
        comboMultiplier >= 5.0 -> NeonPink
        comboMultiplier >= 3.0 -> GoldAccent
        comboMultiplier >= 2.0 -> NeonCyan
        else -> NeonPurple
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(200.dp)
            .scale(scale.value)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = glowAlpha),
                        NeonCyan.copy(alpha = 0.2f)
                    )
                )
            )
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    coroutineScope.launch {
                        scale.animateTo(0.88f, animationSpec = tween(60))
                        scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                    onTap()
                    tryAwaitRelease()
                })
            }
    ) {
        Text(text = emoji, fontSize = 80.sp, textAlign = TextAlign.Center)
    }
}
