package com.example.viralclicker.ui.prestige

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.viralclicker.ui.theme.NeonPink

@Composable
fun PrestigeDialog(
    currentMultiplier: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🔥 Go Viral!", fontWeight = FontWeight.Black) },
        text = {
            Text(
                "Reset ALL progress to earn a permanent +10% multiplier!\n\n" +
                "Current multiplier: ×${String.format("%.1f", currentMultiplier)}\n" +
                "After prestige: ×${String.format("%.1f", currentMultiplier * 1.1)}\n\n" +
                "This cannot be undone."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
            ) { Text("Go Viral! 🚀", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Not yet") }
        }
    )
}
