package com.example.focusflow.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.focusflow.ui.theme.AppColors

@Composable
fun GlassNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    appColors: AppColors
) {
    val barShape = RoundedCornerShape(50)
    val isDark = appColors.mode == "dark"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .height(76.dp)
            .clip(barShape)
            .background(appColors.surface.copy(alpha = if (isDark) 0.6f else 0.75f))
            .border(
                width = 1.5.dp,
                color = if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.7f),
                shape = barShape
            )
    ) {
        // "Блик" сверху — эффект стекла
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (isDark) 0.12f else 0.35f),
                            Color.White.copy(alpha = 0.02f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassNavItem(
                icon = Icons.Outlined.Timer,
                contentDescription = "Таймер",
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                appColors = appColors,
                modifier = Modifier.weight(1f)
            )
            GlassNavItem(
                icon = Icons.Outlined.TaskAlt,
                contentDescription = "Задачи",
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                appColors = appColors,
                modifier = Modifier.weight(1f)
            )
            GlassNavItem(
                icon = Icons.Outlined.BarChart,
                contentDescription = "Статистика",
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                appColors = appColors,
                modifier = Modifier.weight(1f)
            )

            // Тонкий разделитель
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(appColors.text.copy(alpha = 0.15f))
            )

            // Настройки — 4-я вкладка
            GlassNavItem(
                icon = Icons.Outlined.Settings,
                contentDescription = "Настройки",
                selected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                appColors = appColors,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun GlassNavItem(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
    appColors: AppColors,
    modifier: Modifier = Modifier
) {
    val isDark = appColors.mode == "dark"

    val circleAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(250),
        label = "circleAlpha"
    )
    val circleScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.6f,
        animationSpec = tween(250),
        label = "circleScale"
    )

    val targetIconColor = if (selected) {
        if (isDark) appColors.bg else Color.White
    } else {
        appColors.textSecondary
    }
    val iconColor by animateColorAsState(targetIconColor, tween(250), label = "iconColor")

    Box(
        modifier = modifier
            .height(76.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .graphicsLayer {
                    scaleX = circleScale
                    scaleY = circleScale
                }
                .clip(CircleShape)
                .background(appColors.primary.copy(alpha = circleAlpha))
        )

        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}