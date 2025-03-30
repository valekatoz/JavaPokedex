package com.pokedex.utils;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * La classe per gestisce l'uso dell'audio all'interno del gioco.
 */
public class MusicManager {
    private InputStream is = getClass().getResourceAsStream("/audio/track.wav");
    private static Clip clip;
    private static FloatControl volumeControl;
    private static boolean muted;

    /**
     * Gestisce l'audio nel gioco
     */
    public MusicManager() {
        this.muted = false;
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fa partire la musica
     */
    public void play() {
        if (!muted) {
            clip.setFramePosition(0);
            clip.start();
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }
}
