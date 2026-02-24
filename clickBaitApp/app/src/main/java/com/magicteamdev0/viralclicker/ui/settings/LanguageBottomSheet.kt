package com.magicteamdev0.viralclicker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magicteamdev0.viralclicker.R
import com.magicteamdev0.viralclicker.ui.theme.*
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════
//  Supported Languages Data
// ═══════════════════════════════════════════════

data class SupportedLanguage(
    val code: String,        // BCP 47 (stored in DataStore)
    val flag: String,        // Flag emoji
    val nativeName: String   // Language name in its own script
)

val supportedLanguages = listOf(
    SupportedLanguage("en",    "🇬🇧", "English"),
    SupportedLanguage("zh-CN", "🇨🇳", "中文（简体）"),
    SupportedLanguage("hi",    "🇮🇳", "हिंदी"),
    SupportedLanguage("es",    "🇪🇸", "Español"),
    SupportedLanguage("fr",    "🇫🇷", "Français"),
    SupportedLanguage("ar",    "🇸🇦", "العربية"),
    SupportedLanguage("bn",    "🇧🇩", "বাংলা"),
    SupportedLanguage("ru",    "🇷🇺", "Русский"),
    SupportedLanguage("pt",    "🇧🇷", "Português")
)

// ═══════════════════════════════════════════════
//  Language Bottom Sheet
// ═══════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageBottomSheet(
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = stringResource(R.string.settings_language),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(4.dp))

            HorizontalDivider(
                color = NeonCyan.copy(alpha = 0.15f),
                thickness = 1.dp
            )

            Spacer(Modifier.height(12.dp))

            // Language list
            supportedLanguages.forEach { lang ->
                val isSelected = lang.code == currentLanguageCode
                LanguageRow(
                    language = lang,
                    isSelected = isSelected,
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            onLanguageSelected(lang.code)
                        }
                    }
                )
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Single language row
// ─────────────────────────────────────────────

@Composable
private fun LanguageRow(
    language: SupportedLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected)
                    Modifier.border(1.5.dp, NeonCyan, RoundedCornerShape(12.dp))
                else
                    Modifier
            )
            .background(
                if (isSelected) NeonCyan.copy(alpha = 0.08f)
                else DarkSurfaceVariant.copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Flag emoji
        Text(
            text = language.flag,
            fontSize = 24.sp
        )

        // Native language name
        Text(
            text = language.nativeName,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) NeonCyan else OnDark,
            modifier = Modifier.weight(1f)
        )

        // Selection checkmark
        if (isSelected) {
            Text(
                text = "✓",
                color = NeonCyan,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }
    }
}
