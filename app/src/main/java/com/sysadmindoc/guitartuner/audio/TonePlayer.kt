package com.sysadmindoc.guitartuner.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

internal fun isPlayableReferenceToneFrequency(frequencyHz: Double): Boolean =
    frequencyHz.isFinite() && frequencyHz in 20.0..5_000.0

class TonePlayer {
    @Volatile
    private var track: AudioTrack? = null

    val isPlaying: Boolean
        get() = track?.playState == AudioTrack.PLAYSTATE_PLAYING

    fun play(frequencyHz: Double) {
        stop()
        if (!isPlayableReferenceToneFrequency(frequencyHz)) return

        val samples = generateTone(frequencyHz)
        val bufferSize = samples.size * Short.SIZE_BYTES
        val audioTrack = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
        } catch (_: RuntimeException) {
            return
        }

        if (audioTrack.state != AudioTrack.STATE_INITIALIZED) {
            try { audioTrack.release() } catch (_: RuntimeException) {}
            return
        }

        audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(t: AudioTrack) {
                try { t.stop() } catch (_: IllegalStateException) {}
                try { t.release() } catch (_: RuntimeException) {}
                if (track === t) track = null
            }
            override fun onPeriodicNotification(t: AudioTrack) {}
        })
        track = audioTrack
        try {
            val written = audioTrack.write(samples, 0, samples.size)
            if (written != samples.size) {
                track = null
                try { audioTrack.release() } catch (_: RuntimeException) {}
                return
            }
            audioTrack.setNotificationMarkerPosition(samples.size)
            audioTrack.play()
        } catch (_: RuntimeException) {
            track = null
            try { audioTrack.release() } catch (_: RuntimeException) {}
        }
    }

    fun stop() {
        val t = track
        track = null
        if (t != null) {
            try {
                if (t.playState == AudioTrack.PLAYSTATE_PLAYING) t.stop()
            } catch (_: IllegalStateException) {}
            try { t.release() } catch (_: RuntimeException) {}
        }
    }

    private fun generateTone(frequencyHz: Double): ShortArray {
        val totalSamples = SampleRate * DurationMs / 1000
        val fadeInSamples = SampleRate * FadeMs / 1000
        val fadeOutSamples = SampleRate * FadeMs / 1000
        val samples = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val time = i.toDouble() / SampleRate
            val sine = sin(2.0 * PI * frequencyHz * time)
            val envelope = when {
                i < fadeInSamples -> i.toDouble() / fadeInSamples
                i > totalSamples - fadeOutSamples -> (totalSamples - i).toDouble() / fadeOutSamples
                else -> 1.0
            }
            val decayEnvelope = exp(-time / DecaySeconds)
            samples[i] = (sine * envelope * decayEnvelope * Amplitude * Short.MAX_VALUE).toInt().toShort()
        }
        return samples
    }

    private companion object {
        const val SampleRate = 44_100
        const val DurationMs = 2_000
        const val FadeMs = 50
        const val DecaySeconds = 1.5
        const val Amplitude = 0.4
    }
}
