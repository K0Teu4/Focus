package com.example.focusflow.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer

/**
 * Фоновые эмбиенты для фокуса.
 * Генерируемые: white / pink / brown (синтез без файлов)
 * Файловые: rain / cafe и т.п. — подхватываются из res/raw автоматически
 */
object AmbientSoundManager {

    private var context: Context? = null
    private var audioTrack: AudioTrack? = null
    private var mediaPlayer: MediaPlayer? = null
    private var noiseThread: Thread? = null

    @Volatile private var playing = false
    private var volume = 0.5f
    private var currentType = ""

    fun init(ctx: Context) {
        if (context == null) context = ctx.applicationContext
    }

    val generatedSounds = listOf("white", "pink", "brown")

    val fileSounds: List<String>
        get() = listOf("rain", "cafe").filter {
            context?.resources?.getIdentifier(it, "raw", context!!.packageName) != 0
        }

    fun displayName(type: String): String = when (type) {
        "white" -> "Белый шум"
        "pink" -> "Розовый шум"
        "brown" -> "Глубокий (дождь)"
        "rain" -> "Дождь (запись)"
        "cafe" -> "Кафе"
        else -> type
    }

    fun setVolume(v: Float) {
        volume = v.coerceIn(0f, 1f)
        try {
            mediaPlayer?.setVolume(volume, volume)
            audioTrack?.setVolume(volume)
        } catch (_: Exception) { }
    }

    fun isPlaying(): Boolean = playing
    fun currentType(): String = currentType

    fun start(type: String) {
        val ctx = context ?: return
        if (type == currentType && playing) return // уже играет этот тип
        stop()
        if (type == "off") return
        playing = true
        currentType = type

        val resId = ctx.resources.getIdentifier(type, "raw", ctx.packageName)
        if (type !in generatedSounds && resId != 0) {
            startFile(ctx, resId)
        } else {
            startNoise(type)
        }
    }

    /** Полная остановка, вызывается при выборе "off" и при завершении/паузе сессии */
    fun stop() {
        playing = false
        currentType = ""
        noiseThread = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) { }
        audioTrack = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) { }
        mediaPlayer = null
    }

    private fun startFile(ctx: Context, resId: Int) {
        mediaPlayer = MediaPlayer.create(ctx, resId)?.apply {
            isLooping = true
            setVolume(volume, volume)
            start()
        }
    }

    private fun startNoise(type: String) {
        val sampleRate = 44100
        val bufSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufSize * 4)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        audioTrack = track
        track.setVolume(volume)
        track.play()

        noiseThread = Thread({
            val buffer = ShortArray(bufSize)
            val random = java.util.Random(42)
            var lastBrown = 0.0
            var b0 = 0.0; var b1 = 0.0; var b2 = 0.0; var b3 = 0.0
            var b4 = 0.0; var b5 = 0.0; var b6 = 0.0

            while (playing) {
                for (i in buffer.indices) {
                    val white = random.nextDouble() * 2 - 1
                    val sample = when (type) {
                        "white" -> white * 0.25
                        "pink" -> {
                            b0 = 0.99886 * b0 + white * 0.0555179
                            b1 = 0.99332 * b1 + white * 0.0750759
                            b2 = 0.96900 * b2 + white * 0.1538520
                            b3 = 0.86650 * b3 + white * 0.3104856
                            b4 = 0.55000 * b4 + white * 0.5329522
                            b5 = -0.7616 * b5 - white * 0.0168980
                            val pink = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362
                            b6 = white * 0.115926
                            pink * 0.11
                        }
                        else -> { // brown
                            lastBrown = (lastBrown + 0.02 * white) / 1.02
                            lastBrown * 3.5
                        }
                    }.coerceIn(-1.0, 1.0)
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }
                if (playing) track.write(buffer, 0, buffer.size)
            }
        }, "ambient-noise").apply {
            priority = Thread.MIN_PRIORITY
            start()
        }
    }
}