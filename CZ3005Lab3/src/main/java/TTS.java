import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;

public class TTS {
    private MaryInterface marytts;
    private Clip clip;

    public TTS() {
        try {
            marytts = new LocalMaryInterface();
        } catch (MaryConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void dictate(String text) {
        AudioInputStream audio;

        try {
            audio = marytts.generateAudio(text);
        } catch (SynthesisException e) {
            e.printStackTrace();
            return;
        }

        try {
            clip = AudioSystem.getClip();
            clip.open(audio);
            clip.loop(0);
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void cut() {
        if (clip != null) {
            clip.stop();
        }
    }
}
