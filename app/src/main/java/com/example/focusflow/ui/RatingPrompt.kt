package com.example.focusflow.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.focusflow.ui.theme.AppColors

private const val RUSTORE_URL = "https://www.rustore.ru/catalog/app/com.example.focusflow"

@Composable
fun RatingPrompt(completedPomodoros: Int, appColors: AppColors) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    // Проверяем только при изменении completedPomodoros
    LaunchedEffect(completedPomodoros) {
        val prefs = context.getSharedPreferences("focusflow_rating", Context.MODE_PRIVATE)
        val lastChecked = prefs.getInt("last_checked", 0)
        val asked = prefs.getBoolean("asked", false)
        
        // Показываем если: ещё не спрашивали И завершено >= 5 И это новый помидор
        if (!asked && completedPomodoros >= 5 && completedPomodoros > lastChecked) {
            showDialog = true
            prefs.edit().putInt("last_checked", completedPomodoros).apply()
        } else if (completedPomodoros > lastChecked) {
            prefs.edit().putInt("last_checked", completedPomodoros).apply()
        }
    }

    if (showDialog) {
        val prefs = context.getSharedPreferences("focusflow_rating", Context.MODE_PRIVATE)
        val dismiss = {
            prefs.edit().putBoolean("asked", true).apply()
            showDialog = false
        }
        AlertDialog(
            onDismissRequest = { dismiss() },
            icon = { Text("⭐", fontSize = 40.sp) },
            title = {
                Text(
                    "Нравится FocusFlow?",
                    color = appColors.text,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    "Оцените приложение в RuStore — это очень помогает развитию кото-сада! 🍅",
                    color = appColors.textSecondary,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    dismiss()
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(RUSTORE_URL)))
                    } catch (_: Exception) { }
                }) { Text("Оценить", color = appColors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { dismiss() }) {
                    Text("Позже", color = appColors.textSecondary)
                }
            },
            containerColor = appColors.surface
        )
    }
}