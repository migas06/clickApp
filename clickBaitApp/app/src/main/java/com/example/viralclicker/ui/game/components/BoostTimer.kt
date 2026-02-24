package com.example.viralclicker.ui.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.viralclicker.R
import com.example.viralclicker.ui.theme.NeonPink
import java.util.concurrent.TimeUnit

@Composable
fun BoostTimer(boostActiveUntil: Long?, modifier: Modifier = Modifier) {
    if (boostActiveUntil == null) return

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(boostActiveUntil) {
        while (System.currentTimeMillis() < boostActiveUntil) {
            kotlinx.coroutines.delay(1000L)
            now = System.currentTimeMillis()
        }
    }

    val remaining = (boostActiveUntil - now).coerceAtLeast(0L)
    if (remaining <= 0L) return

    val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60
    val boostLabel = stringResource(R.string.boost_timer_label)

    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("$boostLabel %02d:%02d".format(minutes, seconds),
            color = NeonPink,
            style = MaterialTheme.typography.labelSmall)
    }
}
