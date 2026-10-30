package com.example

import com.example.audio.AudioTranscoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sin

class AudioTranscoderTest {

    @Test
    fun pcmToWav_createsValidWavHeader() {
        val sampleCount = 16000 // 1 second of 16kHz mono
        val pcm = ByteArray(sampleCount * 2)
        val wav = AudioTranscoder.pcmToWav(pcm, sampleRate = 16000, channels = 1, bitsPerSample = 16)

        assertTrue("WAV output must be recognized by isWav", AudioTranscoder.isWav(wav))
        assertEquals(44 + pcm.size, wav.size)
        assertEquals('R'.code.toByte(), wav[0])
        assertEquals('I'.code.toByte(), wav[1])
        assertEquals('F'.code.toByte(), wav[2])
        assertEquals('F'.code.toByte(), wav[3])
        assertEquals('W'.code.toByte(), wav[8])
        assertEquals('A'.code.toByte(), wav[9])
        assertEquals('V'.code.toByte(), wav[10])
        assertEquals('E'.code.toByte(), wav[11])
    }

    @Test
    fun isWav_detectsNonWavBytes() {
        assertFalse(AudioTranscoder.isWav(ByteArray(0)))
        assertFalse(AudioTranscoder.isWav(ByteArray(20)))
        assertFalse(AudioTranscoder.isWav("fake audio data content".toByteArray()))
    }

    @Test
    fun hasAudibleSpeech_detectsPureSilence() {
        // 1 second of digital silence
        val silentPcm = ByteArray(16000 * 2)
        val silentWav = AudioTranscoder.pcmToWav(silentPcm)

        assertFalse("Silent WAV must not be flagged as audible speech", AudioTranscoder.hasAudibleSpeech(silentWav))
    }

    @Test
    fun hasAudibleSpeech_detectsToneSpeechEnergy() {
        // 1 second of loud sine wave (amplitude 8000)
        val sampleRate = 16000
        val pcm = ByteArray(sampleRate * 2)
        for (i in 0 until sampleRate) {
            val sample = (8000.0 * sin(2.0 * Math.PI * 440.0 * i / sampleRate)).toInt().toShort()
            pcm[i * 2] = (sample.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }
        val wav = AudioTranscoder.pcmToWav(pcm)

        assertTrue("Tone audio with amplitude 8000 must be detected as audible speech", AudioTranscoder.hasAudibleSpeech(wav))
    }
}
