package com.example.focusflow.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ThemePicker(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Оптимистичная подсветка: мгновенный отклик, DataStore догоняет в фоне
    var optimistic by remember { mutableStateOf<String?>(null) }
    val effective = optimistic ?: selectedTheme
    LaunchedEffect(selectedTheme) { optimistic = null }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        val themes = AppTheme.THEMES.keys.toList()
        val rows = themes.chunked(2)

        rows.forEach { rowThemes ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowThemes.forEach { themeName ->
                    Box(modifier = Modifier.weight(1f)) {
                        ThemeCard(
                            themeName = themeName,
                            isSelected = themeName == effective,
                            onClick = {
                                optimistic = themeName
                                onThemeSelected(themeName)
                            }
                        )
                    }
                }
                if (rowThemes.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ThemeCard(
    themeName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = AppTheme.getThemeColors(themeName)
    val displayName = AppTheme.THEME_DISPLAY[themeName] ?: themeName
    val isPremium = AppTheme.PREMIUM_THEMES.contains(themeName)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.surface2,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ColorCircle(colors.bg)
            ColorCircle(colors.primary)
            ColorCircle(colors.work)
            ColorCircle(colors.rest)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.text
        )

        if (isPremium) {
            Text(
                text = "PREMIUM",
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary
            )
        }
    }
}

@Composable
fun ColorCircle(color: Color, size: Int = 24) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
    )
}