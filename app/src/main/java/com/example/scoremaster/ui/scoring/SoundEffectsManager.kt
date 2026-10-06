package com.example.scoremaster.ui.scoring

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoundEffectsManager {

    private var toneGenerator: ToneGenerator? = null
    var isSoundEnabled: Boolean = true
        private set

    init {
        runCatching {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        }
    }

    fun toggleSound(): Boolean {
        isSoundEnabled = !isSoundEnabled
        return isSoundEnabled
    }

    fun playRunSound(runs: Int) {
        if (!isSoundEnabled) return
        when (runs) {
            4 -> playFourSound()
            6 -> playSixSound()
            else -> playSingleSound()
        }
    }

    fun playFourSound() {
        if (!isSoundEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
                delay(150)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
            }
        }
    }

    fun playSixSound() {
        if (!isSoundEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150)
                delay(180)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
                delay(220)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 250)
            }
        }
    }

    fun playWicketSound() {
        if (!isSoundEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 300)
            }
        }
    }

    fun playSingleSound() {
        if (!isSoundEnabled) return
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
            }
        }
    }

    fun release() {
        runCatching {
            toneGenerator?.release()
            toneGenerator = null
        }
    }
}
