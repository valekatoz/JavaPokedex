package com.pokedex.utils;

import com.pokedex.pokemon.Pokemon;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class StilePerTipoPokemon {

    private static final Map<String, String> CARD_BACKGROUND_COLORS = new HashMap<>();

    public static final Map<String, String> CARD_BORDER_COLORS = new HashMap<>();

    private static final Map<String, String> TYPE_CLASSES = new HashMap<>();

    static {
        CARD_BACKGROUND_COLORS.put("normal", "#F5F5F5");
        CARD_BACKGROUND_COLORS.put("fire", "#FFF1F0");
        CARD_BACKGROUND_COLORS.put("water", "#F0F9FF");
        CARD_BACKGROUND_COLORS.put("grass", "#F1FFEB");
        CARD_BACKGROUND_COLORS.put("electric", "#FFFEF0");
        CARD_BACKGROUND_COLORS.put("ice", "#F0FFFF");
        CARD_BACKGROUND_COLORS.put("fighting", "#FFF0F5");
        CARD_BACKGROUND_COLORS.put("poison", "#FDF0FF");
        CARD_BACKGROUND_COLORS.put("ground", "#FFF5E6");
        CARD_BACKGROUND_COLORS.put("flying", "#F5F5FF");
        CARD_BACKGROUND_COLORS.put("psychic", "#FFF0F7");
        CARD_BACKGROUND_COLORS.put("bug", "#F5FFEB");
        CARD_BACKGROUND_COLORS.put("rock", "#F5F5F0");
        CARD_BACKGROUND_COLORS.put("ghost", "#F2F2FF");
        CARD_BACKGROUND_COLORS.put("dragon", "#F0F5FF");
        CARD_BACKGROUND_COLORS.put("dark", "#F0F0F0");
        CARD_BACKGROUND_COLORS.put("steel", "#F5F5F8");
        CARD_BACKGROUND_COLORS.put("fairy", "#FFF0F5");

        // colori del bordo
        CARD_BORDER_COLORS.put("normal", "#AAAAAA");
        CARD_BORDER_COLORS.put("fire", "#FF6B70");
        CARD_BORDER_COLORS.put("water", "#5DA0FF");
        CARD_BORDER_COLORS.put("grass", "#70ED57");
        CARD_BORDER_COLORS.put("electric", "#FFD34E");
        CARD_BORDER_COLORS.put("ice", "#7DEEEE");
        CARD_BORDER_COLORS.put("fighting", "#D36063");
        CARD_BORDER_COLORS.put("poison", "#BD77BD");
        CARD_BORDER_COLORS.put("ground", "#E1BC6D");
        CARD_BORDER_COLORS.put("flying", "#9E85FF");
        CARD_BORDER_COLORS.put("psychic", "#FF6EB3");
        CARD_BORDER_COLORS.put("bug", "#A5C83B");
        CARD_BORDER_COLORS.put("rock", "#C5B67C");
        CARD_BORDER_COLORS.put("ghost", "#7878C8");
        CARD_BORDER_COLORS.put("dragon", "#7D6BF6");
        CARD_BORDER_COLORS.put("dark", "#775747");
        CARD_BORDER_COLORS.put("steel", "#B2B2C8");
        CARD_BORDER_COLORS.put("fairy", "#F5B0D0");

        for (String type : CARD_BACKGROUND_COLORS.keySet()) {
            TYPE_CLASSES.put(type, "type-" + type);
        }
    }

    public static void applyTypeStyle(VBox card, Pokemon pokemon) {
        if (pokemon == null || pokemon.getTypes().isEmpty()) {
            return;
        }

        String primaryType = pokemon.getTypes().get(0).toLowerCase();

        card.getStyleClass().add(primaryType);

        String backgroundColor = CARD_BACKGROUND_COLORS.getOrDefault(primaryType, "#FFFFFF");
        card.setStyle("-fx-background-color: " + backgroundColor + ";");

        // Imposta colore del bordo
        String borderColor = CARD_BORDER_COLORS.getOrDefault(primaryType, "#DDDDDD");
        card.setStyle(card.getStyle() + " -fx-border-color: " + borderColor + "; -fx-border-width: 2; -fx-border-radius: 20;");
    }
    public static String getTypeClass(String type) {
        return TYPE_CLASSES.getOrDefault(type.toLowerCase(), "");
    }
}