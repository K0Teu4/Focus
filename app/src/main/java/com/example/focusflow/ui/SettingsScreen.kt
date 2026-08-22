package com.example.focusflow.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusflow.sound.AmbientSoundManager
import com.example.focusflow.sound.SoundManager
import com.example.focusflow.ui.theme.AppColors
import com.example.focusflow.ui.theme.AppTheme
import com.example.focusflow.ui.theme.ThemePicker
import com.example.focusflow.viewmodel.PremiumViewModel
import com.example.focusflow.viewmodel.SettingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onOpenPremium: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(),
    premiumViewModel: PremiumViewModel = viewModel()
) {
    val state by viewModel.settingsState.collectAsState()
    val isPremium by premiumViewModel.isPremium.collectAsState()
    val appColors = AppTheme.getThemeColors(state.theme)

    // Оптимистичные значения для мгновенного отклика карточек
    var optimisticSound by remember { mutableStateOf<String?>(null) }
    var optimisticAmbient by remember { mutableStateOf<String?>(null) }
    val effectiveSound = optimisticSound ?: state.soundType
    val effectiveAmbient = optimisticAmbient ?: state.ambientType
    LaunchedEffect(state.soundType) { optimisticSound = null }
    LaunchedEffect(state.ambientType) { optimisticAmbient = null }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopAmbientPreview() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки", color = appColors.text) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SectionHeader("Таймер", appColors)

            DurationSetting(
                "Работа", state.workDuration, state.workSeconds,
                { viewModel.updateWorkDuration(it) }, { viewModel.updateWorkSeconds(it) }, appColors
            )
            DurationSetting(
                "Короткий перерыв", state.shortBreakDuration, state.shortBreakSeconds,
                { viewModel.updateShortBreakDuration(it) }, { viewModel.updateShortBreakSeconds(it) }, appColors
            )
            DurationSetting(
                "Длинный перерыв", state.longBreakDuration, state.longBreakSeconds,
                { viewModel.updateLongBreakDuration(it) }, { viewModel.updateLongBreakSeconds(it) }, appColors
            )

            SwitchRow(
                title = "Автозапуск",
                subtitle = "Автоматически начинать следующую сессию",
                checked = state.autoStart,
                onCheckedChange = { viewModel.updateAutoStart(it) },
                appColors = appColors
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Сессий до длинного перерыва", color = appColors.text,
                        style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium
                    )
                    Text(
                        "После N помидоров — длинный перерыв",
                        style = MaterialTheme.typography.bodySmall, color = appColors.textSecondary
                    )
                }
                NumberField(
                    value = state.sessionsUntilLongBreak,
                    onCommit = { viewModel.updateSessionsUntilLongBreak(it) },
                    label = "2-8", range = 2..8,
                    appColors = appColors, modifier = Modifier.width(90.dp)
                )
            }

            SwitchRow(
                title = "🔥 Строгий режим",
                subtitle = "Пауза/пропуск/сброс/выход сжигает помидор",
                checked = state.strictMode,
                onCheckedChange = { viewModel.updateStrictMode(it) },
                appColors = appColors
            )

            HorizontalDivider(color = appColors.surface2)

            SectionHeader("Цель дня", appColors)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Помидоров в день", color = appColors.text,
                    style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                NumberField(
                    value = state.dailyGoal,
                    onCommit = { viewModel.updateDailyGoal(it) },
                    label = "1-30", range = 1..30,
                    appColors = appColors, modifier = Modifier.width(90.dp)
                )
            }

            HorizontalDivider(color = appColors.surface2)

            SectionHeader("Звуки и вибрация", appColors)

            SwitchRow(
                title = "Звуковые уведомления",
                subtitle = null,
                checked = state.soundEnabled,
                onCheckedChange = { viewModel.updateSoundEnabled(it) },
                appColors = appColors
            )

            if (state.soundEnabled) {
                VolumeControl(
                    initial = state.soundVolume,
                    onSave = { viewModel.updateSoundVolume(it) },
                    appColors = appColors
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Звук уведомления", color = appColors.text,
                    style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                val sounds = SoundManager.SOUND_DISPLAY.keys.toList()
                sounds.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { s ->
                            SoundCard(
                                type = s,
                                selected = effectiveSound == s,
                                onSelect = {
                                    optimisticSound = s
                                    viewModel.updateSoundType(s)
                                },
                                onPreview = { viewModel.previewSound(s) },
                                label = SoundManager.SOUND_DISPLAY[s] ?: s,
                                appColors = appColors,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            SwitchRow(
                title = "Вибрация",
                subtitle = "Вибрировать при завершении сессии",
                checked = state.vibrationEnabled,
                onCheckedChange = { viewModel.updateVibrationEnabled(it) },
                appColors = appColors
            )

            HorizontalDivider(color = appColors.surface2)

            SectionHeader("Фоновые звуки", appColors)

            if (!isPremium) {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenPremium),
                    colors = CardDefaults.cardColors(containerColor = appColors.primary.copy(alpha = 0.15f)),
                    border = BorderStroke(1.5.dp, appColors.primary.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Lock, contentDescription = null,
                            tint = appColors.primary, modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Доступно в Premium", color = appColors.text,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Белый/розовый шум, дождь и кафе во время фокуса",
                                color = appColors.textSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                AmbientVolumeControl(
                    initial = state.ambientVolume,
                    onVolumeChange = { AmbientSoundManager.setVolume(it) },
                    onSave = { viewModel.updateAmbientVolume(it) },
                    appColors = appColors
                )

                val ambientOptions = remember {
                    listOf("off") + AmbientSoundManager.generatedSounds + AmbientSoundManager.fileSounds
                }
                ambientOptions.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { s ->
                            AmbientCard(
                                type = s,
                                selected = effectiveAmbient == s,
                                onSelect = {
                                    optimisticAmbient = s
                                    viewModel.updateAmbientType(s)
                                    if (s == "off") {
                                        AmbientSoundManager.stop()
                                    } else {
                                        AmbientSoundManager.start(s)
                                    }
                                },
                                appColors = appColors,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (AmbientSoundManager.isPlaying()) {
                    OutlinedButton(
                        onClick = {
                            AmbientSoundManager.stop()
                            viewModel.updateAmbientType("off")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.error)
                    ) {
                        Text("Остановить фоновый шум")
                    }
                }
            }

            HorizontalDivider(color = appColors.surface2)

            SectionHeader("Тема", appColors)

            ThemePicker(
                selectedTheme = state.theme,
                onThemeSelected = { theme ->
                    if (AppTheme.PREMIUM_THEMES.contains(theme) && !isPremium) {
                        onOpenPremium()
                    } else {
                        viewModel.updateTheme(theme)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = appColors.surface2)

            if (isPremium) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenPremium),
                    colors = CardDefaults.cardColors(
                        containerColor = appColors.success.copy(alpha = 0.12f)
                    ),
                    border = BorderStroke(1.5.dp, appColors.success.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("★", color = appColors.success, fontSize = 26.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Premium активен",
                                color = appColors.success,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Бессрочно · подробнее",
                                color = appColors.textSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenPremium),
                    colors = CardDefaults.cardColors(
                        containerColor = appColors.primary.copy(alpha = 0.15f)
                    ),
                    border = BorderStroke(1.5.dp, appColors.primary.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("★", color = appColors.primary, fontSize = 26.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "FocusFlow Premium",
                                color = appColors.text,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Эксклюзивные темы и поддержка разработчика",
                                color = appColors.textSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    appColors: AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title, color = appColors.text,
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = appColors.textSecondary)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SectionHeader(title: String, appColors: AppColors) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = appColors.primary,
        fontSize = 20.sp
    )
}

@Composable
fun VolumeControl(
    initial: Float,
    onSave: (Float) -> Unit,
    appColors: AppColors
) {
    var local by remember { mutableFloatStateOf(initial) }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "Громкость", color = appColors.text,
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium
            )
            Text(
                "${(local * 100).toInt()}%", color = appColors.textSecondary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Slider(
            value = local,
            onValueChange = { local = it },
            onValueChangeFinished = { onSave(local) },
            valueRange = 0f..1f
        )
    }
}

@Composable
fun AmbientVolumeControl(
    initial: Float,
    onVolumeChange: (Float) -> Unit,
    onSave: (Float) -> Unit,
    appColors: AppColors
) {
    var local by remember { mutableFloatStateOf(initial) }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "Громкость фона", color = appColors.text,
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium
            )
            Text(
                "${(local * 100).toInt()}%", color = appColors.textSecondary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Slider(
            value = local,
            onValueChange = {
                local = it
                onVolumeChange(it)
            },
            onValueChangeFinished = { onSave(local) },
            valueRange = 0f..1f
        )
    }
}

@Composable
fun SoundCard(
    type: String, selected: Boolean,
    onSelect: () -> Unit, onPreview: () -> Unit,
    label: String,
    appColors: AppColors, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) appColors.primary.copy(alpha = 0.2f) else appColors.surface2
        ),
        border = if (selected) BorderStroke(2.dp, appColors.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                label, color = appColors.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onPreview, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.PlayArrow, contentDescription = "Прослушать",
                    tint = appColors.primary, modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AmbientCard(
    type: String, selected: Boolean,
    onSelect: () -> Unit,
    appColors: AppColors, modifier: Modifier = Modifier
) {
    val label = if (type == "off") "⏹ Выкл" else AmbientSoundManager.displayName(type)

    Card(
        modifier = modifier.clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) appColors.primary.copy(alpha = 0.2f) else appColors.surface2
        ),
        border = if (selected) BorderStroke(2.dp, appColors.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label, color = appColors.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun NumberField(
    value: Int, onCommit: (Int) -> Unit,
    label: String, range: IntRange,
    appColors: AppColors, modifier: Modifier = Modifier
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            if (newText.length <= 3) {
                text = newText
                val parsed = newText.toIntOrNull()
                if (parsed != null && parsed in range) onCommit(parsed)
            }
        },
        modifier = modifier,
        label = { Text(label, color = appColors.textSecondary) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = appColors.text, unfocusedTextColor = appColors.text,
            focusedBorderColor = appColors.primary, unfocusedBorderColor = appColors.resetBorder
        )
    )
}

@Composable
fun DurationSetting(
    title: String, minutes: Int, seconds: Int,
    onMinutesChange: (Int) -> Unit, onSecondsChange: (Int) -> Unit,
    appColors: AppColors
) {
    Column {
        Text(
            title, style = MaterialTheme.typography.bodyLarge,
            color = appColors.text, fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberField(minutes, onMinutesChange, "Мин", 0..120, appColors, Modifier.weight(1f))
            NumberField(seconds, onSecondsChange, "Сек", 0..59, appColors, Modifier.weight(1f))
        }
        val totalSec = minutes * 60 + seconds
        if (totalSec in 1..9) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Внимание: очень короткий таймер",
                style = MaterialTheme.typography.labelSmall, color = appColors.error
            )
        }
    }
}