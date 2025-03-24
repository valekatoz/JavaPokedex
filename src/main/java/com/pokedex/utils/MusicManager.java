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

    /**
     * Mette il muto
     */
    public static void mute() {
        clip.stop();
        muted = true;
    }

    /**
     * Toglie il muto
     */
    public static void unmute() {
        muted = false;
        clip.start();
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     * Ritorna true se c'è il muto,false se non c'è
     * @return
     */
    public static boolean isMuted() {
        return muted;
    }

    /**
     * Seleziona il livello del volume
     * @param value il valore del volume
     */
    public static void setVolume(double value) {
        if (volumeControl != null) {
            // Scala il valore dello slider (0-1) in decibel accettati da volumeControl
            float dB = (float) (20 * Math.log10(Math.max(value, 0.01))); // Evita valore 0 per evitare eccezioni
            volumeControl.setValue(dB);
        }
    }

    /**
     * Ritorna il livello del volume
     * @return il livello del volume
     */
    public static double getVolume() {
        if (volumeControl != null) {
            // Ottieni il volume attuale in decibel e converti in scala lineare (0-1)
            float dB = volumeControl.getValue();
            return Math.pow(10, dB / 20.0);
        }
        return 1.0; // Volume massimo di default
    }
}
