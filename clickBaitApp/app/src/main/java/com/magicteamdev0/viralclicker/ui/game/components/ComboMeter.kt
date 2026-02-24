package com.magicteamdev0.viralclicker.ui.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magicteamdev0.viralclicker.R
import com.magicteamdev0.viralclicker.ui.theme.GoldAccent
import com.magicteamdev0.viralclicker.ui.theme.NeonCyan
import com.magicteamdev0.viralclicker.ui.theme.NeonPink

@Composable
fun ComboMeter(
    comboCount: Int,
    comboMultiplier: Double,
    modifier: Modifier = Modifier
) {
    if (comboCount < 2) return

    val tierColor = when {
        comboMultiplier >= 5.0 -> NeonPink
        comboMultiplier >= 3.0 -> GoldAccent
        comboMultiplier >= 2.0 -> NeonCyan
        else -> NeonCyan
    }

    val infiniteTransition = rememberInfiniteTransition(label = "combo_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f + (comboMultiplier.toFloat() * 0.04f),
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "combo_scale"
    )

    val progress = comboProgressToNextTier(comboCount)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
    ) {
        Text(
            text = stringResource(R.string.combo_meter, comboMultiplier.toInt()),
            color = tierColor,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp
        )
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            color = tierColor,
            trackColor = tierColor.copy(alpha = 0.2f),
            modifier = Modifier
                .width(120.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}

private fun comboProgressToNextTier(comboCount: Int): Float {
    return when {
        comboCount >= 30 -> 1f // Max tier
        comboCount >= 15 -> (comboCount - 15f) / 15f
        comboCount >= 5 -> (comboCount - 5f) / 10f
        else -> comboCount / 5f
    }
}
