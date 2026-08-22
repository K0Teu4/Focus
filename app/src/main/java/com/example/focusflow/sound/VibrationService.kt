package com.example.focusflow.sound

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class VibrationService(private val context: Context) {
    
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun vibrate(pattern: VibrationPattern = VibrationPattern.COMPLETION) {
        vibrator?.let { v ->
            when (pattern) {
                VibrationPattern.COMPLETION -> {
                    // Вибрация при завершении сессии: короткая-длинная
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 400), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(longArrayOf(0, 200, 100, 400), -1)
                    }
                }
                VibrationPattern.BUTTON -> {
                    // Лёгкая вибрация при нажатии
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(30)
                    }
                }
                VibrationPattern.ERROR -> {
                    // Вибрация при ошибке
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100, 50, 100), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(longArrayOf(0, 100, 50, 100, 50, 100), -1)
                    }
                }
            }
        }
    }

    enum class VibrationPattern {
        COMPLETION,
        BUTTON,
        ERROR
    }
}