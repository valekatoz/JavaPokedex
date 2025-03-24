package com.pokedex.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe di configurazione che legge le chiavi API da un file .env
 * per evitare di salvare dati sensibili nel codice sorgente.
 */
public class ConfigUtils {
    // IMPORTANTE: L'ordine di inizializzazione è importante!
    // Mappa che contiene le variabili d'ambiente caricate dal file .env
    // Deve essere inizializzato PRIMA di OPENAI_API_KEY
    private static final Map<String, String> ENV_VARS = loadEnvFile();

    // Chiave API per ChatGPT - inizializzato DOPO ENV_VARS
    public static final String OPENAI_API_KEY = loadEnvVar("OPENAI_API_KEY", "");

    /**
     * Carica le variabili dal file .env cercando in diverse posizioni
     */
    private static Map<String, String> loadEnvFile() {
        Map<String, String> envVars = new HashMap<>();

        // Posizioni da controllare per il file .env
        File[] possibleLocations = {
                new File(".env"),                         // Directory di lavoro corrente
                new File(System.getProperty("user.dir") + "/.env"),  // Directory del progetto
                new File("../env"),                       // Una directory sopra
                new File(System.getProperty("user.home") + "/pokedex.env") // Home dell'utente
        };

        File envFile = null;
        for (File location : possibleLocations) {
            if (location.exists() && location.isFile()) {
                envFile = location;
                System.out.println("File .env trovato in: " + location.getAbsolutePath());
                break;
            }
        }

        if (envFile == null) {
            System.err.println(
                    "ATTENZIONE: File .env non trovato!\n" +
                            "Per utilizzare l'assistente del professore, crea un file .env nella directory principale del progetto\n" +
                            "con il contenuto: OPENAI_API_KEY=sk-tuaChiaveAPI"
            );
            return envVars;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Ignora commenti e righe vuote
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                // Split sulla prima occorrenza di =
                int separatorPos = line.indexOf('=');
                if (separatorPos > 0) {
                    String key = line.substring(0, separatorPos).trim();
                    String value = line.substring(separatorPos + 1).trim();

                    // Rimuovi eventuali virgolette attorno al valore
                    if (value.startsWith("\"") && value.endsWith("\"") ||
                            value.startsWith("'") && value.endsWith("'")) {
                        value = value.substring(1, value.length() - 1);
                    }

                    envVars.put(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("Errore durante la lettura del file .env: " + e.getMessage());
        }

        return envVars;
    }

    /**
     * Ottiene una variabile d'ambiente dal file .env o restituisce il valore predefinito
     */
    private static String loadEnvVar(String key, String defaultValue) {
        // Prima controlla nel file .env
        String value = ENV_VARS.get(key);

        // Se non trovato, prova nelle variabili d'ambiente del sistema
        if (value == null || value.isEmpty()) {
            value = System.getenv(key);
        }

        // Se ancora non trovato, usa il valore predefinito
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Verifica se la chiave API di OpenAI è configurata correttamente
     */
    public static boolean isOpenAiApiKeyConfigured() {
        return OPENAI_API_KEY != null && !OPENAI_API_KEY.isEmpty();
    }
}