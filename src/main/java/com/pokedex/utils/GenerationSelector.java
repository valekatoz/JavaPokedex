package com.pokedex.utils;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Classe che implementa un selettore di generazioni Pokémon
 */
public class GenerationSelector {
    private final HBox container;
    private final Map<Integer, Button> genButtons = new HashMap<>();
    private int selectedGeneration = 0; // 0 means all generations
    private Consumer<Integer> onGenerationSelected;

    /**
     * Map che associa ogni generazione ai suoi limiti di Pokémon
     */
    private static final Map<Integer, int[]> GENERATION_LIMITS = new HashMap<>();

    static {
        // Format: [startId, endId]
        GENERATION_LIMITS.put(1, new int[]{1, 151});      // Gen I: Kanto
        GENERATION_LIMITS.put(2, new int[]{152, 251});    // Gen II: Johto
        GENERATION_LIMITS.put(3, new int[]{252, 386});    // Gen III: Hoenn
        GENERATION_LIMITS.put(4, new int[]{387, 493});    // Gen IV: Sinnoh
        GENERATION_LIMITS.put(5, new int[]{494, 649});    // Gen V: Unova
        GENERATION_LIMITS.put(6, new int[]{650, 721});    // Gen VI: Kalos
        GENERATION_LIMITS.put(7, new int[]{722, 809});    // Gen VII: Alola
        GENERATION_LIMITS.put(8, new int[]{810, 898});    // Gen VIII: Galar
    }

    /**
     * Crea un nuovo selettore di generazioni
     */
    public GenerationSelector(Consumer<Integer> onGenerationSelected) {
        this.onGenerationSelected = onGenerationSelected;

        container = new HBox(10);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("gen-selector");

        createGenerationButtons();
    }

    /**
     * Crea i bottoni per ogni generazione
     */
    private void createGenerationButtons() {
        // Tutte le generazioni
        Button allGenButton = createGenButton("All", 0);
        container.getChildren().add(allGenButton);

        // Generazioni da I a VIII con numeri romani
        String[] romanNumerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII"};
        for (int i = 0; i < romanNumerals.length; i++) {
            Button genButton = createGenButton(romanNumerals[i], i + 1);
            container.getChildren().add(genButton);
        }

        // Seleziona la prima opzione (tutte le generazioni) per impostazione predefinita
        selectGeneration(0);
    }

    /**
     * Crea un singolo bottone per la generazione
     */
    private Button createGenButton(String label, int genNumber) {
        Button button = new Button(label);
        button.getStyleClass().add("gen-button");

        String tooltipText;
        if (genNumber == 0) {
            tooltipText = "Mostra tutte le generazioni";
        } else {
            int[] limits = GENERATION_LIMITS.get(genNumber);
            String regionName = getRegionName(genNumber);
            tooltipText = String.format("Generazione %s: %s (#%d-#%d)",
                    label, regionName, limits[0], limits[1]);
        }

        button.setTooltip(new Tooltip(tooltipText));

        button.setOnAction(e -> {
            selectGeneration(genNumber);
            if (onGenerationSelected != null) {
                onGenerationSelected.accept(genNumber);
            }
        });

        genButtons.put(genNumber, button);

        return button;
    }

    /**
     * Seleziona visivamente una generazione
     */
    public void selectGeneration(int genNumber) {
        // Rimuovi la classe active da tutti i bottoni
        genButtons.values().forEach(btn -> btn.getStyleClass().remove("active"));

        // Aggiungi la classe active al bottone selezionato
        Button selectedButton = genButtons.get(genNumber);
        if (selectedButton != null) {
            selectedButton.getStyleClass().add("active");
            selectedGeneration = genNumber;
        }
    }

    /**
     * Ottiene il container del selettore di generazioni
     */
    public Node getNode() {
        return container;
    }

    /**
     * Controlla se un Pokémon appartiene alla generazione selezionata
     */
    public boolean isInSelectedGeneration(int pokemonId) {
        if (selectedGeneration == 0) {
            return true; // Tutte le generazioni
        }

        int[] limits = GENERATION_LIMITS.get(selectedGeneration);
        return pokemonId >= limits[0] && pokemonId <= limits[1];
    }

    /**
     * Ottiene la generazione selezionata
     */
    public int getSelectedGeneration() {
        return selectedGeneration;
    }

    /**
     * Restituisce il nome della regione per una generazione
     */
    private String getRegionName(int genNumber) {
        switch (genNumber) {
            case 1: return "Kanto";
            case 2: return "Johto";
            case 3: return "Hoenn";
            case 4: return "Sinnoh";
            case 5: return "Unova";
            case 6: return "Kalos";
            case 7: return "Alola";
            case 8: return "Galar";
            default: return "Regione sconosciuta";
        }
    }
}