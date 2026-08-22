package com.example.focusflow.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusflow.data.model.SessionEntity
import com.example.focusflow.ui.theme.AppColors
import com.example.focusflow.utils.CatGarden
import com.example.focusflow.utils.GardenStats
import com.example.focusflow.viewmodel.PremiumViewModel
import com.example.focusflow.viewmodel.StatsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    appColors: AppColors,
    onOpenCatGarden: () -> Unit,
    viewModel: StatsViewModel = viewModel(),
    premiumViewModel: PremiumViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val isPremium by premiumViewModel.isPremium.collectAsState()
    var sessionFilter by remember { mutableStateOf("all") }

    val filteredSessions = remember(state.recentSessions, sessionFilter) {
        when (sessionFilter) {
            "work" -> state.recentSessions.filter { it.type == "work" }
            "short" -> state.recentSessions.filter { it.type == "short_break" }
            "long" -> state.recentSessions.filter { it.type == "long_break" }
            else -> state.recentSessions
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "📊 Статистика",
                style = MaterialTheme.typography.headlineMedium,
                color = appColors.text,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = appColors.surface)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        state.streak.toString(),
                        fontSize = 36.sp,
                        color = appColors.work,
                        fontWeight = FontWeight.Bold
                    )
                    Column {
                        Text(
                            "дней подряд",
                            style = MaterialTheme.typography.titleMedium,
                            color = appColors.text,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Ваша текущая серия",
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.textSecondary
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard(state.bestDay, "Лучший день", appColors.work, Modifier.weight(1f), appColors)
                MetricCard(state.avgPerDay, "Среднее / день", appColors.primary, Modifier.weight(1f), appColors)
                MetricCard(state.totalTime, "Всего", appColors.longBreak, Modifier.weight(1f), appColors)
            }
        }

        item {
            CatGardenTeaser(
                stats = GardenStats(
                    pomodoros = state.completedPomodoros,
                    streak = state.streak,
                    focusMinutes = state.focusMinutes,
                    tasksDone = state.tasksDone
                ),
                onClick = onOpenCatGarden,
                appColors = appColors
            )
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = appColors.surface)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Активность за 7 дней (мин)",
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.text,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val last7 = state.weekActivity.takeLast(7)
                    val maxMinutes = last7.maxOfOrNull { it.workMinutes } ?: 0f

                    if (maxMinutes <= 0f) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Пока нет данных.\nЗапустите первую сессию.",
                                color = appColors.textSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            last7.forEach { day ->
                                val barHeight = if (day.workMinutes > 0f) {
                                    (100 * day.workMinutes / maxMinutes).coerceIn(14f, 100f)
                                } else 4f

                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.height(16.dp), contentAlignment = Alignment.Center) {
                                        if (day.workMinutes > 0f) {
                                            Text(
                                                viewModel.formatMinutes(day.workMinutes),
                                                fontSize = 10.sp,
                                                color = appColors.work,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(110.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(22.dp)
                                                .height(barHeight.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (day.workMinutes > 0f) appColors.work
                                                    else appColors.textSecondary.copy(alpha = 0.2f)
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(day.dayLabel, fontSize = 11.sp, color = appColors.textSecondary, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isPremium) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = appColors.surface)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Сравнение периодов",
                            style = MaterialTheme.typography.titleMedium,
                            color = appColors.text, fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        CompareRow("Эта неделя / прошлая", state.weekDelta, appColors)
                        Spacer(modifier = Modifier.height(10.dp))
                        CompareRow("Сессий за неделю", state.sessionsDelta, appColors)
                        Spacer(modifier = Modifier.height(10.dp))
                        CompareRow("Этот месяц / прошлый", state.monthDelta, appColors)
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = appColors.surface)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Активность за 90 дней",
                            style = MaterialTheme.typography.titleMedium,
                            color = appColors.text,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HeatmapGrid(activities = state.weekActivity, appColors = appColors)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Меньше", fontSize = 10.sp, color = appColors.textSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp))
                                .background(appColors.textSecondary.copy(alpha = 0.1f)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp))
                                .background(appColors.work.copy(alpha = 0.3f)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp))
                                .background(appColors.work.copy(alpha = 0.6f)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp))
                                .background(appColors.work))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Больше", fontSize = 10.sp, color = appColors.textSecondary)
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Последние сессии",
                style = MaterialTheme.typography.titleMedium,
                color = appColors.text,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SessionFilterChip("all", "Все", sessionFilter, { sessionFilter = it }, appColors)
                SessionFilterChip("work", "Работа", sessionFilter, { sessionFilter = it }, appColors)
                SessionFilterChip("short", "Короткий", sessionFilter, { sessionFilter = it }, appColors)
                SessionFilterChip("long", "Длинный", sessionFilter, { sessionFilter = it }, appColors)
            }
        }

        if (filteredSessions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Нет сессий в этом фильтре", color = appColors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(filteredSessions.take(20), key = { it.id }) { session ->
                SessionItem(
                    session = session,
                    taskTitle = state.taskTitles[session.taskId],
                    appColors = appColors,
                    formatDuration = viewModel::formatDuration
                )
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun CatGardenTeaser(
    stats: GardenStats,
    onClick: () -> Unit,
    appColors: AppColors
) {
    val unlocked = CatGarden.unlockedCount(stats)
    val total = CatGarden.achievements.size
    val nearest = CatGarden.nearest(stats)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = appColors.primary.copy(alpha = 0.12f)),
        border = BorderStroke(1.5.dp, appColors.primary.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🐱", fontSize = 28.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Кото-сад · $unlocked/$total котов",
                    color = appColors.text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (nearest != null) "Ближайший: ${nearest.emoji} ${nearest.name} (+${nearest.threshold - nearest.progress(stats)} ${nearest.unitLabel()})"
                    else "Все коты собраны! 🏆",
                    color = appColors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Outlined.ArrowForward,
                contentDescription = "Открыть",
                tint = appColors.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun CompareRow(label: String, deltaPct: Int?, appColors: AppColors) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = appColors.text, style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (deltaPct == null) {
            Text("—", color = appColors.textSecondary, fontWeight = FontWeight.Bold)
        } else {
            val color = when {
                deltaPct > 0 -> appColors.success
                deltaPct < 0 -> appColors.error
                else -> appColors.textSecondary
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (deltaPct != 0) {
                    Icon(
                        imageVector = if (deltaPct > 0) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
                Text(
                    "${abs(deltaPct)}%",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun SessionFilterChip(key: String, label: String, current: String, onSelect: (String) -> Unit, appColors: AppColors) {
    FilterChip(
        selected = current == key,
        onClick = { onSelect(key) },
        label = { Text(label, maxLines = 1, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = appColors.primary,
            selectedLabelColor = appColors.bg
        )
    )
}

@Composable
fun MetricCard(value: String, label: String, color: Color, modifier: Modifier = Modifier, appColors: AppColors) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = appColors.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 18.sp, color = color, fontWeight = FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, color = appColors.textSecondary,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun SessionItem(session: SessionEntity, taskTitle: String?, appColors: AppColors, formatDuration: (Int) -> String) {
    val (icon, name, color) = when (session.type) {
        "work" -> Triple("🍅", "Работа", appColors.work)
        "short_break" -> Triple("☕", "Короткий перерыв", appColors.rest)
        "long_break" -> Triple("🌙", "Длинный перерыв", appColors.longBreak)
        else -> Triple("•", session.type, appColors.text)
    }
    val dateFormat = remember { SimpleDateFormat("dd.MM HH:mm", Locale("ru")) }
    val dateStr = remember(session.startedAt) { dateFormat.format(Date(session.startedAt)) }
    val title = taskTitle ?: "Без задачи"

    Card(modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(icon, fontSize = 16.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyMedium, color = appColors.text,
                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(title, style = MaterialTheme.typography.bodySmall, color = appColors.textSecondary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatDuration(session.durationSec), style = MaterialTheme.typography.bodyMedium,
                    color = color, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = appColors.textSecondary, maxLines = 1)
            }
        }
    }
}