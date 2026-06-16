package com.sysadmindoc.guitartuner.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.io.Closeable
import java.util.Locale

class SpokenFeedbackController(context: Context) : Closeable {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var lastSpokenText: String? = null
    private var lastSpeakTimeMs = 0L

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                tts?.language = Locale.getDefault()
            }
        }
    }

    fun speak(text: String) {
        if (!ready || text.isBlank()) return
        val now = System.currentTimeMillis()
        if (text == lastSpokenText && now - lastSpeakTimeMs < MinIntervalMs) return
        lastSpokenText = text
        lastSpeakTimeMs = now
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tuner_feedback")
    }

    fun stop() {
        tts?.stop()
        lastSpokenText = null
    }

    override fun close() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }

    private companion object {
        const val MinIntervalMs = 2000L
    }
}
