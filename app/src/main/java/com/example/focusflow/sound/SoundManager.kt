package com.example.focusflow.sound

import android.content.Context
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoundManager(context: Context) {
    private val soundPool = SoundPool.Builder().setMaxStreams(2).build()
    private val soundIds = mutableMapOf<String, Int>()
    private var volume = 1.0f

    val availableSounds = listOf("bell", "chime", "digital", "soft")

    init {
        for (name in availableSounds) {
            val resId = context.resources.getIdentifier(name, "raw", context.packageName)
            if (resId != 0) {
                soundIds[name] = soundPool.load(context, resId, 1)
            }
        }
    }

    fun setVolume(v: Float) {
        volume = v.coerceIn(0f, 1f)
    }

    fun play(type: String) {
        val id = soundIds[type] ?: soundIds["bell"] ?: soundIds.values.firstOrNull() ?: return
        soundPool.play(id, volume, volume, 1, 0, 1.0f)
    }

    fun playMultiple(type: String, times: Int = 3, delayMs: Long = 1500) {
        CoroutineScope(Dispatchers.Main).launch {
            repeat(times) { i ->
                play(type)
                if (i < times - 1) delay(delayMs)
            }
        }
    }

    fun release() {
        soundPool.release()
    }

    companion object {
        val SOUND_DISPLAY = mapOf(
            "bell" to "Колокольчик",
            "chime" to "Перезвон",
            "digital" to "Импульс",
            "soft" to "Нежный"
        )
    }
}