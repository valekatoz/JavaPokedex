package com.pokedex.utils;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SelettoreGenerazione {
    private final HBox container;
    private final Map<Integer, Button> genButtons = new HashMap<>();
    private int selectedGeneration = 0;
    private Consumer<Integer> onGenerationSelected;

    public static final Map<Integer, int[]> GENERATION_LIMITS = new HashMap<>();

    static {
        GENERATION_LIMITS.put(1, new int[]{1, 151});
        GENERATION_LIMITS.put(2, new int[]{152, 251});
        GENERATION_LIMITS.put(3, new int[]{252, 386});
        GENERATION_LIMITS.put(4, new int[]{387, 493});
        GENERATION_LIMITS.put(5, new int[]{494, 649});
        GENERATION_LIMITS.put(6, new int[]{650, 721});
        GENERATION_LIMITS.put(7, new int[]{722, 809});
        GENERATION_LIMITS.put(8, new int[]{810, 898});
    }

    public SelettoreGenerazione(Consumer<Integer> onGenerationSelected) {
        this.onGenerationSelected = onGenerationSelected;

        container = new HBox(10);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("gen-selector");

        createGenerationButtons();
    }

    private void createGenerationButtons() {
        Button allGenButton = createGenButton("All", 0);
        container.getChildren().add(allGenButton);

        String[] romanNumerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII"};
        for (int i = 0; i < romanNumerals.length; i++) {
            Button genButton = createGenButton(romanNumerals[i], i + 1);
            container.getChildren().add(genButton);
        }

        selectGeneration(0);
    }

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

    public void selectGeneration(int genNumber) {
        genButtons.values().forEach(btn -> btn.getStyleClass().remove("active"));

        Button selectedButton = genButtons.get(genNumber);
        if (selectedButton != null) {
            selectedButton.getStyleClass().add("active");
            selectedGeneration = genNumber;
        }
    }

    public Node getNode() {
        return container;
    }


    public int getSelectedGeneration() {
        return selectedGeneration;
    }

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

    public static int[] getGenerationLimits(int generation) {
        if (generation == 0) {
            return new int[]{1, 898};
        }
        return GENERATION_LIMITS.getOrDefault(generation, new int[]{1, 898});
    }
}