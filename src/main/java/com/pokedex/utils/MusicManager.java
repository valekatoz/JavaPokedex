package com.pokedex.utils;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * La classe per gestisce l'uso dell'audio all'interno del gioco.
 */
public class MusicManager {
    private static final String AUDIO_PATH = "/audio/track.wav";
    private static Clip clip;
    private static FloatControl volumeControl;
    private static boolean muted = false;
    private static float currentVolume = 0.5f; // Valore predefinito 50%

    /**
     * Gestisce l'audio nel gioco
     */
    public MusicManager() {
        try {
            // Verifica che la risorsa esista
            InputStream is = getClass().getResourceAsStream(AUDIO_PATH);
            if (is == null) {
                System.err.println("ERRORE: File audio non trovato: " + AUDIO_PATH);
                return;
            }

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            // Verifica che il controllo del volume sia disponibile
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(currentVolume); // Imposta il volume iniziale
                System.out.println("Controllo volume inizializzato: " + currentVolume);
            } else {
                System.err.println("AVVISO: Controllo del volume non supportato");
            }

            System.out.println("MusicManager inizializzato con successo");
        } catch (UnsupportedAudioFileException e) {
            System.err.println("ERRORE: Formato audio non supportato: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("ERRORE I/O durante il caricamento dell'audio: " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.err.println("ERRORE: Linea audio non disponibile: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERRORE imprevisto durante l'inizializzazione audio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fa partire la musica
     */
    public void play() {
        if (clip == null) {
            System.err.println("ERRORE: Clip audio non inizializzato");
            return;
        }

        if (!muted) {
            try {
                clip.setFramePosition(0);
                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                System.out.println("Riproduzione audio avviata");
            } catch (Exception e) {
                System.err.println("ERRORE durante la riproduzione audio: " + e.getMessage());
            }
        } else {
            System.out.println("Audio disattivato, non verrà riprodotto");
        }
    }

    /**
     * Imposta il volume da 0.0 a 1.0
     * @param volume valore da 0.0 (muto) a 1.0 (massimo)
     */
    public void setVolume(float volume) {
        if (volumeControl == null) return;

        currentVolume = volume;

        // Utilizziamo una formula più semplice per il volume
        float min = volumeControl.getMinimum();
        float range = volumeControl.getMaximum() - min;
        float gain = min + (range * volume);

        volumeControl.setValue(gain);
        System.out.println("Volume impostato a: " + volume + " (" + gain + " dB)");
    }

    /**
     * Ottiene il volume corrente (0.0-1.0)
     * @return volume corrente
     */
    public float getVolume() {
        return currentVolume;
    }

    /**
     * Attiva/disattiva l'audio
     */
    public void toggleMute() {
        if (clip == null) return;

        muted = !muted;
        System.out.println("Audio " + (muted ? "disattivato" : "attivato"));

        if (muted) {
            clip.stop();
        } else {
            clip.start();
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * Verifica se l'audio è disattivato
     * @return true se l'audio è disattivato
     */
    public boolean isMuted() {
        return muted;
    }

    /**
     * Ferma completamente la riproduzione e rilascia le risorse
     */
    public void shutdown() {
        if (clip != null) {
            clip.stop();
            clip.close();
            System.out.println("MusicManager arrestato");
        }
    }
}