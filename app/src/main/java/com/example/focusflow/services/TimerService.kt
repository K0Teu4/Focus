package com.example.focusflow.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.focusflow.MainActivity
import com.example.focusflow.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Общий "мост" между ViewModel и сервисом (один процесс).
 */
object TimerNotificationHub {
    val remainingSeconds = MutableStateFlow(0)
    val totalSeconds = MutableStateFlow(1)
    val isRunning = MutableStateFlow(false)
    val sessionLabel = MutableStateFlow("Фокус")

    var actionListener: ((String) -> Unit)? = null
}

class TimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()

        val initial = buildNotification(
            remaining = TimerNotificationHub.remainingSeconds.value,
            total = TimerNotificationHub.totalSeconds.value,
            running = TimerNotificationHub.isRunning.value,
            label = TimerNotificationHub.sessionLabel.value
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                initial,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, initial)
        }

        // Обновляем уведомление на каждое изменение состояния
        scope.launch {
            combine(
                TimerNotificationHub.remainingSeconds,
                TimerNotificationHub.totalSeconds,
                TimerNotificationHub.isRunning,
                TimerNotificationHub.sessionLabel
            ) { remaining, total, running, label ->
                buildNotification(remaining, total, running, label)
            }.collect { notification ->
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                    .notify(NOTIFICATION_ID, notification)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE -> TimerNotificationHub.actionListener?.invoke("toggle")
            ACTION_SKIP -> TimerNotificationHub.actionListener?.invoke("skip")
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Таймер FocusFlow",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Обратный отсчёт текущей сессии"
                setShowBadge(false)
            }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        remaining: Int,
        total: Int,
        running: Boolean,
        label: String
    ): android.app.Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val toggleIntent = PendingIntent.getService(
            this, 1,
            Intent(this, TimerService::class.java).apply { action = ACTION_TOGGLE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val skipIntent = PendingIntent.getService(
            this, 2,
            Intent(this, TimerService::class.java).apply { action = ACTION_SKIP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val safeTotal = total.coerceAtLeast(1)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_timer)
            .setContentTitle("FocusFlow — $label")
            .setContentText(formatTime(remaining))
            .setProgress(safeTotal, safeTotal - remaining.coerceIn(0, safeTotal), false)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openIntent)
            .addAction(0, if (running) "Пауза" else "Продолжить", toggleIntent)
            .addAction(0, "Пропустить", skipIntent)
            .build()
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }

    companion object {
        private const val CHANNEL_ID = "focusflow_timer"
        private const val NOTIFICATION_ID = 101
        private const val ACTION_TOGGLE = "com.example.focusflow.action.TOGGLE"
        private const val ACTION_SKIP = "com.example.focusflow.action.SKIP"
    }
}