package com.example.focusflow
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.LaunchedEffect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.focusflow.data.model.Categories
import com.example.focusflow.data.model.SessionType
import com.example.focusflow.data.model.TaskEntity
import com.example.focusflow.sound.AmbientSoundManager
import com.example.focusflow.ui.CatGardenScreen
import com.example.focusflow.ui.FocusModeScreen
import com.example.focusflow.ui.GlassNavigationBar
import com.example.focusflow.ui.OnboardingScreen
import com.example.focusflow.ui.PremiumScreen
import com.example.focusflow.ui.SettingsScreen
import com.example.focusflow.ui.StatsScreen
import com.example.focusflow.ui.TaskPickerSheet
import com.example.focusflow.ui.theme.AppColors
import com.example.focusflow.ui.theme.AppGradientBackground
import com.example.focusflow.ui.theme.AppTheme
import com.example.focusflow.viewmodel.SettingsViewModel
import com.example.focusflow.viewmodel.TimerState
import com.example.focusflow.viewmodel.TimerViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.focusflow.ui.RatingPrompt
import com.example.focusflow.ui.DevToolsScreen
import com.example.focusflow.BuildConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val settingsState by settingsViewModel.settingsState.collectAsState()
            val onboardingDone by settingsViewModel.onboardingCompleted.collectAsState()
            val appColors = AppTheme.getThemeColors(settingsState.theme)

            MaterialTheme(colorScheme = AppTheme.getColorScheme(settingsState.theme)) {
                AppGradientBackground(appColors) {
                    when (onboardingDone) {
                        null -> { /* splash */ }
                        false -> OnboardingScreen(
                            onFinish = { settingsViewModel.completeOnboarding() },
                            appColors = appColors
                        )
                        true -> MainScreen(appColors)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(appColors: AppColors, viewModel: TimerViewModel = viewModel()) {
    val timerState by viewModel.timerState.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val taskCounts by viewModel.taskCounts.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var showTaskPicker by remember { mutableStateOf(false) }
    var showPremium by remember { mutableStateOf(false) }
    var showFocusMode by remember { mutableStateOf(false) }
    var showCatGarden by remember { mutableStateOf(false) }
    var showDevTools by remember { mutableStateOf(false) }

    val onOpenTaskPicker: () -> Unit = remember { { showTaskPicker = true } }
    val onOpenPremium: () -> Unit = remember { { showPremium = true } }
    val onOpenFocusMode: () -> Unit = remember { { showFocusMode = true } }
    val onOpenCatGarden: () -> Unit = remember { { showCatGarden = true } }
    val onFocusTask: (TaskEntity) -> Unit = remember {
        { task: TaskEntity ->
            viewModel.focusOnTask(task)
            selectedTab = 0
        }
    }

    DisposableEffect(Unit) {
        onDispose { AmbientSoundManager.stop() }
    }

    BackHandler(enabled = showCatGarden || showFocusMode || showPremium || selectedTab != 0) {
        when {
            showCatGarden -> showCatGarden = false
            showFocusMode -> {
                if (timerState.strictMode && timerState.isRunning &&
                    timerState.sessionType == SessionType.WORK) {
                    viewModel.burnPomodoroOnEmergencyExit()
                }
                showFocusMode = false
            }
            showPremium -> showPremium = false
            else -> selectedTab = 0
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(timerState.snackbarMessage) {
        timerState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    if (showDevTools) {
        DevToolsScreen(onBack = { showDevTools = false }, appColors = appColors)
        return
    }
    if (showCatGarden) {
        CatGardenScreen(onBack = { showCatGarden = false }, appColors = appColors)
        return
    }

    if (showFocusMode) {
        FocusModeScreen(viewModel = viewModel, onExit = { showFocusMode = false }, appColors = appColors)
        return
    }

    if (showPremium) {
        PremiumScreen(onBack = { showPremium = false }, appColors = appColors)
        return
    }

    if (showTaskPicker) {
        TaskPickerSheet(
            tasks = tasks.filter { !it.isDone },
            selectedTaskId = timerState.currentTaskId,
            onTaskSelected = { task ->
                viewModel.focusOnTask(task)
                showTaskPicker = false
            },
            onDismiss = { showTaskPicker = false },
            appColors = appColors,
            taskCounts = taskCounts
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                GlassNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    appColors = appColors
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (selectedTab) {
                    0 -> TimerScreen(
                        viewModel = viewModel,
                        state = timerState,
                        tasks = tasks,
                        appColors = appColors,
                        onOpenTaskPicker = onOpenTaskPicker,
                        onOpenFocusMode = onOpenFocusMode
                    )
                    1 -> TasksScreen(
                        viewModel = viewModel,
                        tasks = tasks,
                        taskCounts = taskCounts,
                        appColors = appColors,
                        snackbarHostState = snackbarHostState,
                        scope = scope,
                        onFocusTask = onFocusTask
                    )
                    2 -> StatsScreen(
                        appColors = appColors,
                        onOpenCatGarden = onOpenCatGarden
                    )
                    3 -> SettingsScreen(
                    onBackClick = { selectedTab = 0 },
                    onOpenPremium = onOpenPremium
                )
                }
            }
        }

        TopSnackbar(
            hostState = snackbarHostState,
            appColors = appColors,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (BuildConfig.DEBUG) {
        FloatingActionButton(
            onClick = { showDevTools = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 96.dp),
            containerColor = appColors.surface2,
            contentColor = appColors.text
        ) { Text("🛠") }
    }
    RatingPrompt(
            completedPomodoros = timerState.completedPomodoros,
            appColors = appColors
        )
    }
}

@Composable
fun TimerScreen(
    viewModel: TimerViewModel,
    state: TimerState,
    tasks: List<TaskEntity>,
    appColors: AppColors,
    onOpenTaskPicker: () -> Unit,
    onOpenFocusMode: () -> Unit
) {
    val sessionsUntilLong by viewModel.sessionsUntilLong.collectAsState()

    val currentTask by remember(state.currentTaskId, tasks) {
        derivedStateOf { tasks.find { it.id == state.currentTaskId } }
    }
    val categoryColors = remember(appColors) {
        mapOf(
            Categories.WORK to appColors.catWork,
            Categories.REST to appColors.catRest,
            Categories.HOBBY to appColors.catHobby,
            Categories.STUDY to appColors.catStudy
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onOpenFocusMode) {
                Icon(
                    Icons.Outlined.Fullscreen,
                    contentDescription = "Фокус-режим",
                    tint = appColors.textSecondary
                )
            }
        }

        if (state.isAutoStartCounting) {
            Card(colors = CardDefaults.cardColors(containerColor = appColors.surface)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Старт через ${state.autoStartCountdown}...",
                        color = appColors.text,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    OutlinedButton(onClick = { viewModel.cancelAutoStartCountdown() }) {
                        Text("Отмена")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        SessionTypeChip(state.sessionType, appColors)

        if (!state.isRunning) {
            Spacer(modifier = Modifier.height(8.dp))
            SessionTypeSwitcher(
                current = state.sessionType,
                onWork = { viewModel.switchToWork() },
                onShort = { viewModel.switchToBreak() },
                onLong = { viewModel.switchToLongBreak() },
                appColors = appColors
            )
        }

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(contentAlignment = Alignment.Center) {
                val ringSize = minOf(maxWidth, maxHeight, 260.dp)
                TimerRing(
                    remaining = state.timeRemaining,
                    total = state.totalTime,
                    isRunning = state.isRunning,
                    completedPomodoros = state.completedPomodoros,
                    sessionsUntilLong = sessionsUntilLong,
                    sessionType = state.sessionType,
                    ringSize = ringSize,
                    formatTime = viewModel::formatTime,
                    appColors = appColors
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        DailyGoalCard(state.dailyProgress, state.dailyGoal, appColors)

        Spacer(modifier = Modifier.height(8.dp))

        CurrentTaskCard(currentTask, categoryColors, onOpenTaskPicker, appColors)

        Spacer(modifier = Modifier.height(12.dp))

        TimerControls(
            isRunning = state.isRunning,
            sessionType = state.sessionType,
            onPause = { viewModel.pauseTimer() },
            onStart = { viewModel.startTimer() },
            onSkip = { viewModel.skipSession() },
            onReset = { viewModel.resetTimer() },
            appColors = appColors
        )
    }
}

@Composable
private fun SessionTypeChip(sessionType: SessionType, appColors: AppColors) {
    val color = remember(sessionType) {
        when (sessionType) {
            SessionType.WORK -> appColors.work
            SessionType.SHORT_BREAK -> appColors.rest
            SessionType.LONG_BREAK -> appColors.longBreak
        }
    }
    val emoji = remember(sessionType) {
        when (sessionType) {
            SessionType.WORK -> "🍅"
            SessionType.SHORT_BREAK -> "☕"
            SessionType.LONG_BREAK -> "🌙"
        }
    }
    val label = remember(sessionType) {
        when (sessionType) {
            SessionType.WORK -> "Фокус"
            SessionType.SHORT_BREAK -> "Короткий перерыв"
            SessionType.LONG_BREAK -> "Длинный перерыв"
        }
    }
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.15f)) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(emoji, fontSize = 18.sp)
            Text(label, style = MaterialTheme.typography.titleMedium, color = color,
                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SessionTypeSwitcher(
    current: SessionType,
    onWork: () -> Unit, onShort: () -> Unit, onLong: () -> Unit,
    appColors: AppColors
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        FilterChip(
            selected = current == SessionType.WORK, onClick = onWork,
            label = { Text("Работа", maxLines = 1) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = appColors.work, selectedLabelColor = appColors.bg),
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = current == SessionType.SHORT_BREAK, onClick = onShort,
            label = { Text("Перерыв", maxLines = 1) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = appColors.rest, selectedLabelColor = appColors.bg),
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = current == SessionType.LONG_BREAK, onClick = onLong,
            label = { Text("Длинный", maxLines = 1) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = appColors.longBreak, selectedLabelColor = appColors.bg),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TimerRing(
    remaining: Int, total: Int, isRunning: Boolean,
    completedPomodoros: Int, sessionsUntilLong: Int,
    sessionType: SessionType, ringSize: Dp,
    formatTime: (Int) -> String,
    appColors: AppColors
) {
    val sessionColor = remember(sessionType) {
        when (sessionType) {
            SessionType.WORK -> appColors.work
            SessionType.SHORT_BREAK -> appColors.rest
            SessionType.LONG_BREAK -> appColors.longBreak
        }
    }
    val progress = if (total > 0) (total - remaining).toFloat() / total else 0f

    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "pulseAlpha"
    )
    val timeColor = if (isRunning && remaining <= 10 && remaining > 0) {
        sessionColor.copy(alpha = pulseAlpha)
    } else {
        appColors.text
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(ringSize)) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = sessionColor,
            strokeWidth = 10.dp,
            strokeCap = StrokeCap.Round,
            trackColor = sessionColor.copy(alpha = 0.12f)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                formatTime(remaining),
                fontSize = 52.sp,
                style = MaterialTheme.typography.displayLarge,
                color = timeColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${completedPomodoros % sessionsUntilLong}/$sessionsUntilLong до длинного",
                style = MaterialTheme.typography.bodySmall,
                color = appColors.textSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DailyGoalCard(progress: Int, goal: Int, appColors: AppColors) {
    val achieved = progress >= goal
    Card(colors = CardDefaults.cardColors(containerColor = appColors.surface)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Цель дня",
                    style = MaterialTheme.typography.bodyMedium, color = appColors.text,
                    fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "$progress/$goal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (achieved) appColors.success else appColors.textSecondary,
                    fontWeight = FontWeight.Bold, maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (goal in 1..12) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(goal) { i ->
                        Text(
                            if (i < progress) "🍅" else "⬜",
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LinearProgressIndicator(
                    progress = { (progress.toFloat() / goal).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = if (achieved) appColors.success else appColors.primary,
                    trackColor = appColors.surface2
                )
            }

            Text(
                text = if (achieved) "Цель достигнута! Кот сыт 😻" else " ",
                style = MaterialTheme.typography.labelSmall,
                color = if (achieved) appColors.success else appColors.textSecondary,
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp).height(16.dp)
            )
        }
    }
}

@Composable
private fun CurrentTaskCard(
    task: TaskEntity?,
    categoryColors: Map<String, Color>,
    onClick: () -> Unit,
    appColors: AppColors
) {
    val categoryColor = remember(task) {
        task?.let { categoryColors[it.category] ?: appColors.primary } ?: appColors.primary
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (task != null) appColors.surface else appColors.surface2)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (task != null) {
                Box(
                    modifier = Modifier.size(4.dp, 36.dp)
                        .clip(RoundedCornerShape(2.dp)).background(categoryColor)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.bodyLarge, color = appColors.text,
                        fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        Categories.LABELS[task.category] ?: "",
                        style = MaterialTheme.typography.labelMedium, color = categoryColor,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Icon(Icons.Default.Add, contentDescription = null,
                    tint = appColors.primary, modifier = Modifier.size(28.dp))
                Text(
                    "Выбрать задачу",
                    style = MaterialTheme.typography.bodyLarge, color = appColors.primary,
                    fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TimerControls(
    isRunning: Boolean, sessionType: SessionType,
    onPause: () -> Unit, onStart: () -> Unit, onSkip: () -> Unit, onReset: () -> Unit,
    appColors: AppColors
) {
    val sessionColor = remember(sessionType) {
        when (sessionType) {
            SessionType.WORK -> appColors.work
            SessionType.SHORT_BREAK -> appColors.rest
            SessionType.LONG_BREAK -> appColors.longBreak
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        if (isRunning) {
            Button(
                onClick = onPause,
                colors = ButtonDefaults.buttonColors(containerColor = appColors.pauseWork),
                modifier = Modifier.weight(0.9f),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Outlined.Pause, contentDescription = null,
                    tint = appColors.bg, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Пауза", color = appColors.bg, fontSize = 14.sp, maxLines = 1)
            }
            Button(
                onClick = onSkip,
                colors = ButtonDefaults.buttonColors(containerColor = appColors.skip),
                modifier = Modifier.weight(1.4f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 14.dp)
            ) {
                Icon(Icons.Outlined.SkipNext, contentDescription = null,
                    tint = appColors.bg, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Пропустить", color = appColors.bg, fontSize = 14.sp, maxLines = 1)
            }
        } else {
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = sessionColor),
                modifier = Modifier.weight(2f),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = null,
                    tint = appColors.bg, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Старт",
                    fontSize = 17.sp, color = appColors.bg, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
        OutlinedButton(
            onClick = onReset,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.text),
            modifier = Modifier.weight(0.7f),
            contentPadding = PaddingValues(vertical = if (isRunning) 14.dp else 16.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Сброс", modifier = Modifier.size(22.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TimerViewModel,
    tasks: List<TaskEntity>,
    taskCounts: Map<Long, Int>,
    appColors: AppColors,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    onFocusTask: (TaskEntity) -> Unit
) {
    var newTaskTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Categories.WORK) }
    var showDoneTasks by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf("new") }
    var showSortMenu by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showCategoryPicker by remember { mutableStateOf<TaskEntity?>(null) }

    val categoryColors = remember(appColors) {
        mapOf(
            Categories.WORK to appColors.catWork,
            Categories.REST to appColors.catRest,
            Categories.HOBBY to appColors.catHobby,
            Categories.STUDY to appColors.catStudy
        )
    }

    editingTask?.let { task ->
        AlertDialog(
            onDismissRequest = { editingTask = null },
            title = { Text("Редактировать задачу", color = appColors.text) },
            text = {
                OutlinedTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    label = { Text("Название") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.text, unfocusedTextColor = appColors.text,
                        focusedBorderColor = appColors.primary)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        viewModel.updateTask(task.copy(title = newTaskTitle))
                        newTaskTitle = ""; editingTask = null
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { newTaskTitle = ""; editingTask = null }) { Text("Отмена") }
            },
            containerColor = appColors.surface
        )
    }

    showCategoryPicker?.let { task ->
        ModalBottomSheet(
            onDismissRequest = { showCategoryPicker = null },
            containerColor = appColors.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
                Text("Категория", style = MaterialTheme.typography.titleLarge,
                    color = appColors.text, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Categories.ALL.forEach { category ->
                    val color = categoryColors[category] ?: appColors.primary
                    val isSelected = task.category == category
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                            viewModel.updateTask(task.copy(category = category))
                            showCategoryPicker = null
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) color.copy(alpha = 0.2f) else appColors.surface2)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
                            Text(Categories.LABELS[category] ?: category, color = appColors.text,
                                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) Text("✓", color = appColors.primary, fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newTaskTitle,
                onValueChange = { newTaskTitle = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Новая задача", color = appColors.textSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = appColors.text, unfocusedTextColor = appColors.text,
                    focusedBorderColor = appColors.primary, unfocusedBorderColor = appColors.resetBorder)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        viewModel.addTask(newTaskTitle, selectedCategory)
                        newTaskTitle = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = appColors.bg)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Categories.ALL.forEach { category ->
                val color = categoryColors[category] ?: appColors.primary
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(Categories.LABELS[category] ?: category,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color, selectedLabelColor = appColors.bg)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Поиск задач", color = appColors.textSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = appColors.textSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = appColors.text, unfocusedTextColor = appColors.text,
                focusedBorderColor = appColors.primary, unfocusedBorderColor = appColors.resetBorder)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Показывать выполненные",
                color = appColors.textSecondary,
                style = MaterialTheme.typography.bodyMedium, maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Switch(checked = showDoneTasks, onCheckedChange = { showDoneTasks = it })
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Сортировка",
                        tint = appColors.textSecondary)
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(appColors.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Сначала новые", color = if (sortMode == "new") appColors.primary else appColors.text) },
                        onClick = { sortMode = "new"; showSortMenu = false })
                    DropdownMenuItem(
                        text = { Text("По имени", color = if (sortMode == "name") appColors.primary else appColors.text) },
                        onClick = { sortMode = "name"; showSortMenu = false })
                    DropdownMenuItem(
                        text = { Text("По помидорам", color = if (sortMode == "pomodoro") appColors.primary else appColors.text) },
                        onClick = { sortMode = "pomodoro"; showSortMenu = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val sortedTasks = remember(tasks, selectedCategory, showDoneTasks, searchQuery, sortMode, taskCounts) {
            val filtered = tasks.filter { task ->
                (task.category == selectedCategory) &&
                    (showDoneTasks || !task.isDone) &&
                    (searchQuery.isBlank() || task.title.lowercase().contains(searchQuery.lowercase()))
            }
            when (sortMode) {
                "name" -> filtered.sortedBy { it.title.lowercase() }
                "pomodoro" -> filtered.sortedByDescending { taskCounts[it.id] ?: 0 }
                else -> filtered.sortedByDescending { it.createdAt }
            }
        }

        if (sortedTasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Пока нет задач", style = MaterialTheme.typography.bodyLarge, color = appColors.text)
                    Text("Создайте первую задачу сверху",
                        style = MaterialTheme.typography.bodySmall, color = appColors.textSecondary)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(sortedTasks, key = { it.id }) { task ->
                    TaskItemRow(
                        task = task,
                        pomodoroCount = taskCounts[task.id] ?: 0,
                        onToggleDone = { viewModel.setTaskDone(task.id, !task.isDone) },
                        onEdit = { newTaskTitle = task.title; editingTask = task },
                        onChangeCategory = { showCategoryPicker = task },
                        onFocus = { onFocusTask(task) },
                        onDelete = {
                            val t = task
                            viewModel.deleteTask(task)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Удалено: ${task.title}", actionLabel = "Отменить")
                                if (result == SnackbarResult.ActionPerformed) viewModel.restoreTask(t)
                            }
                        },
                        appColors = appColors,
                        categoryColors = categoryColors
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskItemRow(
    task: TaskEntity,
    pomodoroCount: Int,
    onToggleDone: () -> Unit,
    onEdit: () -> Unit,
    onChangeCategory: () -> Unit,
    onFocus: () -> Unit,
    onDelete: () -> Unit,
    appColors: AppColors,
    categoryColors: Map<String, Color>
) {
    var showMenu by remember { mutableStateOf(false) }
    val categoryColor = remember(task.category) { categoryColors[task.category] ?: appColors.primary }

    Card(colors = CardDefaults.cardColors(containerColor = appColors.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onToggleDone() },
                colors = CheckboxDefaults.colors(checkedColor = categoryColor)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onFocus),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(4.dp, 24.dp)
                        .clip(RoundedCornerShape(2.dp)).background(categoryColor))
                    Text(
                        task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (task.isDone) appColors.textSecondary else appColors.text,
                        fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(shape = RoundedCornerShape(4.dp), color = categoryColor.copy(alpha = 0.2f)) {
                        Text(
                            Categories.LABELS[task.category] ?: task.category,
                            style = MaterialTheme.typography.labelSmall, color = categoryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), maxLines = 1)
                    }
                    if (pomodoroCount > 0) {
                        Text("🍅 $pomodoroCount", style = MaterialTheme.typography.labelSmall,
                            color = appColors.textSecondary, maxLines = 1)
                    }
                    if (task.isDone) {
                        Text("Выполнена", style = MaterialTheme.typography.labelSmall,
                            color = appColors.success, maxLines = 1)
                    }
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Меню", tint = appColors.textSecondary)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(appColors.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Начать фокус", color = appColors.text) },
                        onClick = { onFocus(); showMenu = false })
                    DropdownMenuItem(
                        text = { Text("Редактировать", color = appColors.text) },
                        onClick = { onEdit(); showMenu = false })
                    DropdownMenuItem(
                        text = { Text("Категория", color = appColors.text) },
                        onClick = { onChangeCategory(); showMenu = false })
                    HorizontalDivider(color = appColors.surface2)
                    DropdownMenuItem(
                        text = { Text("Удалить", color = appColors.error) },
                        onClick = { onDelete(); showMenu = false })
                }
            }
        }
    }
}

@Composable
fun TopSnackbar(
    hostState: SnackbarHostState,
    appColors: AppColors,
    modifier: Modifier = Modifier
) {
    val data = hostState.currentSnackbarData

    LaunchedEffect(data) {
        if (data != null) {
            delay(4000)
            data.dismiss()
        }
    }

    if (data != null) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = appColors.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.visuals.message,
                    color = appColors.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { data.dismiss() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Закрыть",
                        tint = appColors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}