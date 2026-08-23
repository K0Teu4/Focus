package com.example.focusflow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.focusflow.data.AppDatabase
import com.example.focusflow.ui.theme.AppColors
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusflow.viewmodel.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevToolsScreen(
    onBack: () -> Unit,
    appColors: AppColors,
    viewModel: TimerViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🛠 Консоль разработчика", color = appColors.text) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = appColors.text)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Данные для скриншотов. Используй debug-сборку.",
                color = appColors.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = {
                    scope.launch {
                        viewModel.devSeedPomodoros(50)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
            ) {
                Text("🍅 Добавить 50 помидоров (размазано по неделе)")
            }

            Button(
                onClick = {
                    scope.launch {
                        viewModel.devSeedStreak(7)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
            ) {
                Text("🔥 Симулировать серию 7 дней")
            }

            Button(
                onClick = {
                    scope.launch {
                        viewModel.devSeedTasks(20)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
            ) {
                Text("✅ Добавить 20 задач")
            }

            HorizontalDivider(color = appColors.surface2)

            OutlinedButton(
                onClick = {
                    scope.launch {
                        viewModel.devResetStats()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.error)
            ) {
                Text("🗑 Сбросить всю статистику")
            }
        }
    }
}