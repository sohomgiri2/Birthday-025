package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Offline synthesized SFX & Music engine.
 * Plays soft crystalline tones for UI/games via AudioTrack and supports local MP3/WAV playback via MediaPlayer.
 */
class SoundEngine(private val context: Context) {

    enum class SfxType {
        TAP,
        ENVELOPE,
        GIFT,
        SLICE,
        STAR,
        ACHIEVEMENT,
        CELEBRATION,
        UNLOCK,
        ERROR,
        NAVIGATE
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var mediaPlayer: MediaPlayer? = null
    private var ambientJob: Job? = null

    @Volatile
    var sfxEnabled: Boolean = true

    @Volatile
    var bgmEnabled: Boolean = false

    @Volatile
    var volume: Float = 0.75f

    fun playSfx(type: SfxType) {
        if (!sfxEnabled || volume <= 0.01f) return
        scope.launch {
            when (type) {
                SfxType.TAP -> playTone(660.0, 55, 0.22f)
                SfxType.NAVIGATE -> playTone(520.0, 70, 0.18f)
                SfxType.ENVELOPE -> {
                    playTone(440.0, 90, 0.22f)
                    delay(50)
                    playTone(587.33, 120, 0.24f)
                }
                SfxType.GIFT -> {
                    playChord(listOf(523.25, 659.25, 783.99), 260, 0.26f)
                }
                SfxType.SLICE -> playSweep(920.0, 320.0, 95, 0.24f)
                SfxType.STAR -> {
                    playTone(783.99, 75, 0.25f)
                    delay(45)
                    playTone(1046.5, 110, 0.25f)
                }
                SfxType.ACHIEVEMENT -> {
                    listOf(523.25, 659.25, 783.99, 1046.5).forEach { freq ->
                        playTone(freq, 95, 0.24f)
                        delay(60)
                    }
                }
                SfxType.CELEBRATION -> {
                    listOf(523.25, 659.25, 783.99, 1046.5, 1318.5).forEach { freq ->
                        playTone(freq, 130, 0.26f)
                        delay(70)
                    }
                }
                SfxType.UNLOCK -> {
                    playTone(587.33, 100, 0.25f)
                    delay(70)
                    playTone(880.0, 200, 0.28f)
                }
                SfxType.ERROR -> {
                    playTone(210.0, 120, 0.26f)
                    delay(80)
                    playTone(175.0, 140, 0.26f)
                }
            }
        }
    }

    fun playLocalTrack(sourceUri: String, onCompletion: () -> Unit) {
        stopPlayback()
        try {
            if (sourceUri.startsWith("synth:")) {
                startAmbientSynth(sourceUri.removePrefix("synth:").toIntOrNull() ?: 0)
                return
            }
            val player = MediaPlayer()
            if (sourceUri.startsWith("asset:")) {
                val assetPath = sourceUri.removePrefix("asset:")
                val afd = context.assets.openFd(assetPath)
                player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
            } else if (sourceUri.startsWith("file:")) {
                val file = File(sourceUri.removePrefix("file:"))
                if (!file.exists()) return
                player.setDataSource(file.absolutePath)
            }
            player.setVolume(volume, volume)
            player.setOnCompletionListener { onCompletion() }
            player.prepare()
            player.start()
            mediaPlayer = player
        } catch (_: Exception) {
            stopPlayback()
        }
    }

    fun updateVolume(newVolume: Float) {
        volume = newVolume.coerceIn(0f, 1f)
        try {
            mediaPlayer?.setVolume(volume, volume)
        } catch (_: Exception) { }
    }

    fun pausePlayback() {
        ambientJob?.cancel()
        ambientJob = null
        try {
            if (mediaPlayer?.isPlaying == true) mediaPlayer?.pause()
        } catch (_: Exception) { }
    }

    fun resumePlayback() {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) it.start()
            }
        } catch (_: Exception) { }
    }

    fun getPlaybackProgress(): Pair<Int, Int> {
        return try {
            val mp = mediaPlayer ?: return 0 to 0
            mp.currentPosition to mp.duration.coerceAtLeast(1)
        } catch (_: Exception) {
            0 to 0
        }
    }

    fun seekToFraction(fraction: Float) {
        try {
            val mp = mediaPlayer ?: return
            val target = (mp.duration * fraction.coerceIn(0f, 1f)).toInt()
            mp.seekTo(target)
        } catch (_: Exception) { }
    }

    fun stopPlayback() {
        ambientJob?.cancel()
        ambientJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) { }
        mediaPlayer = null
    }

    private fun startAmbientSynth(preset: Int) {
        val progressions = listOf(
            listOf(261.63, 329.63, 392.00, 493.88), // Cmaj7
            listOf(220.00, 261.63, 329.63, 392.00), // Am7
            listOf(174.61, 220.00, 261.63, 329.63)  // Fmaj7
        )
        val notes = progressions[preset % progressions.size]
        ambientJob = scope.launch {
            var idx = 0
            while (isActive) {
                playTone(notes[idx % notes.size], 900, 0.14f)
                idx++
                delay(950)
            }
        }
    }

    private fun playTone(freqHz: Double, durationMs: Int, baseGain: Float) {
        val sampleRate = 22050
        val count = (sampleRate * durationMs / 1000).coerceAtLeast(1)
        val samples = ShortArray(count)
        val gain = baseGain * volume
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val env = sin(PI * i / count) * exp(-2.2 * i / count)
            val wave = sin(2.0 * PI * freqHz * t)
            samples[i] = (wave * env * gain * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        writePcm(samples, sampleRate)
    }

    private fun playSweep(startFreq: Double, endFreq: Double, durationMs: Int, baseGain: Float) {
        val sampleRate = 22050
        val count = (sampleRate * durationMs / 1000).coerceAtLeast(1)
        val samples = ShortArray(count)
        val gain = baseGain * volume
        var phase = 0.0
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val freq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * PI * freq / sampleRate
            val env = sin(PI * progress)
            samples[i] = (sin(phase) * env * gain * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        writePcm(samples, sampleRate)
    }

    private fun playChord(freqs: List<Double>, durationMs: Int, baseGain: Float) {
        val sampleRate = 22050
        val count = (sampleRate * durationMs / 1000).coerceAtLeast(1)
        val samples = ShortArray(count)
        val gain = (baseGain * volume) / freqs.size.coerceAtLeast(1)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val env = sin(PI * i / count) * exp(-1.8 * i / count)
            var sum = 0.0
            for (f in freqs) {
                sum += sin(2.0 * PI * f * t)
            }
            samples[i] = (sum * env * gain * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        writePcm(samples, sampleRate)
    }

    private fun writePcm(samples: ShortArray, sampleRate: Int) {
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBuf, samples.size * 2)
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(bufferSize)
                .build()

            track.write(samples, 0, samples.size)
            track.play()
            Thread.sleep((samples.size * 1000L / sampleRate) + 40L)
            track.stop()
            track.release()
        } catch (_: Exception) {
            // Safe fallback if audio hardware is busy
        }
    }
}
