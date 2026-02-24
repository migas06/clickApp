package com.example.viralclicker.ui.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viralclicker.ui.theme.GoldAccent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class FloatingTextData(
    val id: Long,
    val text: String,
    val isCritical: Boolean,
    val offsetX: Float // horizontal spread in dp
)

@Composable
fun FloatingTextOverlay(
    floatingTexts: List<FloatingTextData>,
    onRemove: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        floatingTexts.forEach { data ->
            key(data.id) {
                FloatingTextItem(data = data, onComplete = { onRemove(data.id) })
            }
        }
    }
}

@Composable
private fun FloatingTextItem(
    data: FloatingTextData,
    onComplete: () -> Unit
) {
    val alpha = remember { Animatable(1f) }
    val offsetY = remember { Animatable(0f) }
    val scale = remember { Animatable(if (data.isCritical) 1.5f else 1f) }

    LaunchedEffect(data.id) {
        coroutineScope {
            launch { offsetY.animateTo(-120f, tween(800, easing = FastOutSlowInEasing)) }
            launch { alpha.animateTo(0f, tween(800, easing = LinearEasing)) }
            launch {
                if (data.isCritical) {
                    scale.animateTo(2.2f, spring(dampingRatio = 0.3f, stiffness = 600f))
                    scale.animateTo(1.5f, tween(200))
                }
            }
        }
        onComplete()
    }

    Text(
        text = data.text,
        modifier = Modifier
            .offset(x = data.offsetX.dp, y = offsetY.value.dp)
            .graphicsLayer(
                alpha = alpha.value,
                scaleX = scale.value,
                scaleY = scale.value
            ),
        color = if (data.isCritical) GoldAccent else Color.White,
        fontSize = if (data.isCritical) 28.sp else 18.sp,
        fontWeight = FontWeight.Black
    )
}
