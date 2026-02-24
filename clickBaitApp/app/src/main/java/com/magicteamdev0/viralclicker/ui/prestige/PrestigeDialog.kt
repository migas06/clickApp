package com.magicteamdev0.viralclicker.ui.prestige

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.magicteamdev0.viralclicker.R
import com.magicteamdev0.viralclicker.ui.theme.NeonPink

@Composable
fun PrestigeDialog(
    currentMultiplier: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentStr = String.format("%.1f", currentMultiplier)
    val afterStr = String.format("%.1f", currentMultiplier * 1.1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🔥 ${stringResource(R.string.prestige_dialog_title)}", fontWeight = FontWeight.Black) },
        text = {
            Text(stringResource(R.string.prestige_dialog_body, currentStr, afterStr))
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
            ) { Text(stringResource(R.string.prestige_confirm), fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.prestige_dismiss)) }
        }
    )
}
