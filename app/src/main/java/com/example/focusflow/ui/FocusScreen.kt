package com.example.focusflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusflow.data.model.SessionType
import com.example.focusflow.ui.theme.AppColors
import com.example.focusflow.viewmodel.TimerViewModel

@Composable
fun FocusModeScreen(
    viewModel: TimerViewModel,
    onExit: () -> Unit,
    appColors: AppColors
) {
    val state by viewModel.timerState.collectAsState()
    val sessionsUntilLong by viewModel.sessionsUntilLong.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val view = LocalView.current

    DisposableEffect(state.isRunning) {
        view.keepScreenOn = state.isRunning
        onDispose { view.keepScreenOn = false }
    }

    val sessionColor = remember(state.sessionType) {
        when (state.sessionType) {
            SessionType.WORK -> appColors.work
            SessionType.SHORT_BREAK -> appColors.rest
            SessionType.LONG_BREAK -> appColors.longBreak
        }
    }

    val currentTask = remember(state.currentTaskId, tasks) {
        tasks.find { it.id == state.currentTaskId }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(sessionColor.copy(alpha = 0.22f), appColors.bg)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // Кнопка выхода: в строгом режиме сжигает помидор
            IconButton(onClick = {
                if (state.strictMode && state.isRunning && state.sessionType == SessionType.WORK) {
                    viewModel.burnPomodoroOnEmergencyExit()
                }
                onExit()
            }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Выйти из фокус-режима",
                    tint = appColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.weight(0.6f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(300.dp).align(Alignment.CenterHorizontally)
            ) {
                val progress = if (state.totalTime > 0) {
                    (state.totalTime - state.timeRemaining).toFloat() / state.totalTime
                } else 0f

                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = sessionColor,
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round,
                    trackColor = sessionColor.copy(alpha = 0.12f)
                )

                Text(
                    text = viewModel.formatTime(state.timeRemaining),
                    fontSize = 64.sp,
                    color = appColors.text,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = when (state.sessionType) {
                    SessionType.WORK -> "Фокус"
                    SessionType.SHORT_BREAK -> "Короткий перерыв"
                    SessionType.LONG_BREAK -> "Длинный перерыв"
                },
                style = MaterialTheme.typography.titleLarge,
                color = sessionColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = currentTask?.title ?: "Без задачи",
                color = appColors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Сессия ${state.completedPomodoros % sessionsUntilLong} из $sessionsUntilLong" +
                    if (state.strictMode && state.sessionType == SessionType.WORK) " · 🔥 строгий" else "",
                color = appColors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.resetTimer() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Сброс",
                        tint = appColors.textSecondary, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(24.dp))

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(sessionColor)
                        .clickable {
                            if (state.isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = if (state.isRunning) "Пауза" else "Старт",
                        tint = appColors.bg,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                IconButton(onClick = { viewModel.skipSession() }) {
                    Icon(Icons.Outlined.SkipNext, contentDescription = "Пропустить",
                        tint = appColors.textSecondary, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}