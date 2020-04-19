package tools

import marytts.LocalMaryInterface
import marytts.MaryInterface
import marytts.exceptions.MaryConfigurationException
import marytts.exceptions.SynthesisException
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

/**
 * Text-To-Speech synthesiser using MaryTTS.
 */
class TTS {
    // Apparently Clip has better quality than built-in MaryTTS audio player.
    private lateinit var clip: Clip
    private lateinit var maryInterface: MaryInterface

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

    /**
     * Synthesises text and reads it out.
     * @param input Text to be read.
     */
    fun nag(input: String) {
        if (!::maryInterface.isInitialized) return

        try {
            val audio: AudioInputStream = maryInterface.generateAudio(input)
            clip = AudioSystem.getClip()
            clip.open(audio)
            clip.loop(0) // means no loop
        } catch (e: SynthesisException) {
            e.printStackTrace()
            println("========== EXCEPTION ==========")
            println("TTS failed to speak.")
            println("===============================")
        }
    }

    /**
     * Stops any ongoing reading of text.
     */
    fun stfu() {
        // Different from clip == null! Kotlin is null-safe unless the variable type has a question mark: ex. Clip? instead of Clip.
        if (::clip.isInitialized) {
            clip.stop()
        }
    }
}