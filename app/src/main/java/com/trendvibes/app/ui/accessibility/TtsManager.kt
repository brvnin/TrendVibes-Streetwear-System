package com.trendvibes.app.ui.accessibility

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import java.util.Locale

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingText: String? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("pt", "BR"))
                if ((result == TextToSpeech.LANG_MISSING_DATA) || (result == TextToSpeech.LANG_NOT_SUPPORTED)) {
                    tts?.language = Locale.getDefault()
                }
                isInitialized = true
                pendingText?.let { text ->
                    speak(text)
                    pendingText = null
                }
            } else {
                Log.e("TrendVibesTTS", "Falha ao inicializar TextToSpeech: status $status")
            }
        }
    }

    fun speak(text: String) {
        if (!isInitialized) {
            pendingText = text
            return
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tv_tts_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
    }
}

val LocalTtsManager = compositionLocalOf<TtsManager?> { null }
