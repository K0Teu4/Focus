package com.example.focusflow.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.focusflow.MainActivity
import com.example.focusflow.R

class TimerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        TimerWidgetUpdater.updateIdle(context)
    }
}

object TimerWidgetUpdater {
    private const val ACTION_TOGGLE = "com.example.focusflow.action.TOGGLE"
    private const val ACTION_SKIP = "com.example.focusflow.action.SKIP"

    fun update(context: Context, remaining: Int, total: Int, running: Boolean, label: String) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, TimerWidgetProvider::class.java))
        if (ids.isEmpty()) return
        manager.updateAppWidget(ids, buildRunning(context, remaining, total, running, label))
    }

    fun updateIdle(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, TimerWidgetProvider::class.java))
        if (ids.isEmpty()) return
        val views = base(context)
        views.setViewVisibility(R.id.widgetTime, View.GONE)
        views.setViewVisibility(R.id.widgetSession, View.GONE)
        views.setViewVisibility(R.id.widgetProgress, View.GONE)
        views.setViewVisibility(R.id.widgetButtons, View.GONE)
        views.setViewVisibility(R.id.widgetIdleText, View.VISIBLE)
        views.setOnClickPendingIntent(R.id.widgetRoot, openApp(context))
        manager.updateAppWidget(ids, views)
    }

    private fun base(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_timer)
        views.setTextViewText(R.id.widgetLabel, "🍅 FocusFlow")
        return views
    }

    private fun openApp(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun buildRunning(
        context: Context, remaining: Int, total: Int, running: Boolean, label: String
    ): RemoteViews {
        val views = base(context)
        views.setViewVisibility(R.id.widgetTime, View.VISIBLE)
        views.setViewVisibility(R.id.widgetSession, View.VISIBLE)
        views.setViewVisibility(R.id.widgetProgress, View.VISIBLE)
        views.setViewVisibility(R.id.widgetButtons, View.VISIBLE)
        views.setViewVisibility(R.id.widgetIdleText, View.GONE)

        views.setTextViewText(R.id.widgetTime, format(remaining))
        views.setTextViewText(
            R.id.widgetSession,
            if (running) label else "$label · пауза"
        )
        val safeTotal = total.coerceAtLeast(1)
        views.setProgressBar(
            R.id.widgetProgress,
            safeTotal,
            safeTotal - remaining.coerceIn(0, safeTotal),
            false
        )
        views.setTextViewText(R.id.widgetToggle, if (running) "Пауза" else "Дальше")
        views.setTextViewText(R.id.widgetSkip, "Пропустить")

        val toggle = PendingIntent.getService(
            context, 1,
            Intent(context, com.example.focusflow.services.TimerService::class.java).apply { action = ACTION_TOGGLE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val skip = PendingIntent.getService(
            context, 2,
            Intent(context, com.example.focusflow.services.TimerService::class.java).apply { action = ACTION_SKIP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widgetToggle, toggle)
        views.setOnClickPendingIntent(R.id.widgetSkip, skip)
        views.setOnClickPendingIntent(R.id.widgetRoot, openApp(context))
        return views
    }

    private fun format(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }
}