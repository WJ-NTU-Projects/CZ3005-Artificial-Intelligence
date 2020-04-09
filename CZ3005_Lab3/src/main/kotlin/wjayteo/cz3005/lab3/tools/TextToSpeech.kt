package wjayteo.cz3005.lab3.tools

import marytts.LocalMaryInterface
import marytts.MaryInterface
import marytts.exceptions.MaryConfigurationException
import marytts.exceptions.SynthesisException
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

class TextToSpeech() {
    private lateinit var maryInterface: MaryInterface
    private lateinit var clip: Clip

    init {
        try {
            maryInterface = LocalMaryInterface()
        } catch (e: MaryConfigurationException) {
            e.printStackTrace()
            println("========== EXCEPTION ==========")
            println("Failed to initialise TTS.")
            println("===============================")
        }
    }

    fun speak(input: String) {
        if (!::maryInterface.isInitialized) return

        try {
            val audio: AudioInputStream = maryInterface.generateAudio(input)
            clip = AudioSystem.getClip()
            clip.open(audio)
            clip.loop(0)
        } catch (e: SynthesisException) {
            e.printStackTrace()
            println("========== EXCEPTION ==========")
            println("TTS failed to speak.")
            println("===============================")
        }
    }

    fun stop() {
        if (::clip.isInitialized) {
            clip.stop()
        }
    }
}