package com.example.focusflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusflow.ui.theme.AppColors
import com.example.focusflow.utils.CatAchievement
import com.example.focusflow.utils.CatGarden
import com.example.focusflow.utils.GardenStats
import com.example.focusflow.viewmodel.StatsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatGardenScreen(
    onBack: () -> Unit,
    appColors: AppColors,
    statsViewModel: StatsViewModel = viewModel()
) {
    val state by statsViewModel.state.collectAsState()
    var selected by remember { mutableStateOf<CatAchievement?>(null) }

    val stats = GardenStats(
        pomodoros = state.completedPomodoros,
        streak = state.streak,
        focusMinutes = state.focusMinutes,
        tasksDone = state.tasksDone
    )
    val unlocked = CatGarden.unlockedCount(stats)
    val total = CatGarden.achievements.size
    val nearest = remember(stats) { CatGarden.nearest(stats) }

    // Диалог с деталями достижения
    selected?.let { a ->
        val isUnlocked = a.isUnlocked(stats)
        AlertDialog(
            onDismissRequest = { selected = null },
            icon = { Text(a.emoji, fontSize = 48.sp) },
            title = { Text(a.name, color = appColors.text, textAlign = TextAlign.Center) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(a.desc, color = appColors.textSecondary, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (isUnlocked) "✓ Открыто!"
                        else "Прогресс: ${a.progress(stats)}/${a.threshold} ${a.unitLabel()}",
                        color = if (isUnlocked) appColors.success else appColors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selected = null }) { Text("Понятно") }
            },
            containerColor = appColors.surface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🐱 Кото-сад", color = appColors.text) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = appColors.text
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appColors.surface)
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Прогресс сада
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = appColors.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Прогресс сада",
                            style = MaterialTheme.typography.titleMedium,
                            color = appColors.text,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$unlocked/$total 🐾",
                            color = appColors.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatPill("🍅", stats.pomodoros.toString(), "Помидоров", Modifier.weight(1f), appColors)
                        StatPill("🔥", stats.streak.toString(), "Дней подряд", Modifier.weight(1f), appColors)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatPill("⏰", formatHours(stats.focusMinutes), "Фокуса", Modifier.weight(1f), appColors)
                        StatPill("✅", stats.tasksDone.toString(), "Задач", Modifier.weight(1f), appColors)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (nearest != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Ближайший: ${nearest.emoji} ${nearest.name}",
                                color = appColors.text,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "+${nearest.threshold - nearest.progress(stats)} ${nearest.unitLabel()}",
                                color = appColors.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = {
                                (nearest.progress(stats).toFloat() / nearest.threshold).coerceIn(0f, 1f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = appColors.primary,
                            trackColor = appColors.surface2
                        )
                    } else {
                        Text(
                            "🏆 Все коты собраны! Сад полон счастья.",
                            color = appColors.success,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            GardenSection(
                title = "🍅 За помидоры",
                achievements = CatGarden.achievements.filter { it.kind == CatAchievement.Kind.POMODOROS },
                stats = stats,
                onAchievementClick = { selected = it },
                appColors = appColors
            )

            GardenSection(
                title = "🔥 За серии",
                achievements = CatGarden.achievements.filter { it.kind == CatAchievement.Kind.STREAK },
                stats = stats,
                onAchievementClick = { selected = it },
                appColors = appColors
            )

            GardenSection(
                title = "⏰ За время фокуса",
                achievements = CatGarden.achievements.filter { it.kind == CatAchievement.Kind.FOCUS_MINUTES },
                stats = stats,
                onAchievementClick = { selected = it },
                appColors = appColors
            )

            GardenSection(
                title = "✅ За задачи",
                achievements = CatGarden.achievements.filter { it.kind == CatAchievement.Kind.TASKS },
                stats = stats,
                onAchievementClick = { selected = it },
                appColors = appColors
            )

            Text(
                "🐱 Каждый помидор кормит кота, каждая серия растит сад. Тапни по коту — узнай его историю!",
                color = appColors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatHours(minutes: Int): String =
    if (minutes >= 60) "${minutes / 60} ч ${minutes % 60} м" else "$minutes мин"

@Composable
private fun StatPill(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier,
    appColors: AppColors
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = appColors.surface2
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(emoji, fontSize = 24.sp)
            Column {
                Text(value, style = MaterialTheme.typography.titleMedium, color = appColors.text,
                    fontWeight = FontWeight.Bold, maxLines = 1)
                Text(label, style = MaterialTheme.typography.bodySmall, color = appColors.textSecondary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun GardenSection(
    title: String,
    achievements: List<CatAchievement>,
    stats: GardenStats,
    onAchievementClick: (CatAchievement) -> Unit,
    appColors: AppColors
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = appColors.text,
            fontWeight = FontWeight.Bold
        )
        Card(colors = CardDefaults.cardColors(containerColor = appColors.surface)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                achievements.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { a ->
                            Box(modifier = Modifier.weight(1f)) {
                                AchievementBig(a, stats, onAchievementClick, appColors)
                            }
                        }
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementBig(
    a: CatAchievement,
    stats: GardenStats,
    onClick: (CatAchievement) -> Unit,
    appColors: AppColors
) {
    val unlocked = a.isUnlocked(stats)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(a) }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (unlocked) appColors.primary.copy(alpha = 0.2f)
                    else appColors.surface2
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                a.emoji,
                fontSize = 32.sp,
                modifier = if (unlocked) Modifier else Modifier.alpha(0.3f)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            a.name,
            fontSize = 11.sp,
            color = if (unlocked) appColors.text else appColors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            if (unlocked) "✓" else "${a.progress(stats)}/${a.threshold}",
            fontSize = 10.sp,
            color = if (unlocked) appColors.success else appColors.textSecondary,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}