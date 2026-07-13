package com.example.utilities

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import com.example.repository.GameRepository
import kotlinx.coroutines.*

class SoundManager(context: Context, private val repository: GameRepository) {
    private var toneGenerator: ToneGenerator? = null
    private var musicJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playTap() {
        if (!repository.isSoundEnabled()) return
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playInvalid() {
        if (!repository.isSoundEnabled()) return
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 200)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playWin() {
        if (!repository.isSoundEnabled()) return
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_1, 100)
                delay(120)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_3, 100)
                delay(120)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_5, 100)
                delay(120)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_9, 250)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playGameOver() {
        if (!repository.isSoundEnabled()) return
        scope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_9, 150)
                delay(180)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_5, 150)
                delay(180)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_3, 150)
                delay(180)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_1, 300)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startMusic() {
        stopMusic()
        if (!repository.isMusicEnabled()) return
        
        musicJob = scope.launch {
            // Arpeggiated sequence for gentle rhythm
            val notes = listOf(
                ToneGenerator.TONE_DTMF_1,
                ToneGenerator.TONE_DTMF_5,
                ToneGenerator.TONE_DTMF_3,
                ToneGenerator.TONE_DTMF_8
            )
            var index = 0
            while (isActive) {
                if (repository.isMusicEnabled()) {
                    try {
                        toneGenerator?.startTone(notes[index], 120)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                index = (index + 1) % notes.size
                delay(2000)
            }
        }
    }

    fun stopMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    fun release() {
        stopMusic()
        scope.cancel()
        try {
            toneGenerator?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        toneGenerator = null
    }
}
