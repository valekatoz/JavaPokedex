package com.pokedex.pokemon;

import java.util.List;
import java.util.Map;

/**
 * Classe che contiene i dati dettagliati di un Pokémon
 */
public class PokemonData extends Pokemon {

    private final int height;
    private final int weight;
    private final List<String> abilities;
    private final Map<String, Integer> stats;
    private final List<String> moves;
    private final String species;
    private final List<String> gameIndices;

    public PokemonData(
            int id,
            String name,
            String imageUrl,
            List<String> types,
            int height,
            int weight,
            List<String> abilities,
            Map<String, Integer> stats,
            List<String> moves,
            String species,
            List<String> gameIndices) {
        super(id, name, imageUrl, types);
        this.height = height;
        this.weight = weight;
        this.abilities = List.copyOf(abilities);
        this.stats = Map.copyOf(stats);
        this.moves = List.copyOf(moves);
        this.species = species;
        this.gameIndices = List.copyOf(gameIndices);
    }

    public int getHeight() {
        return height;
    }

    public int getWeight() {
        return weight;
    }

    public List<String> getAbilities() {
        return abilities;
    }

    public Map<String, Integer> getStats() {
        return stats;
    }

    public List<String> getMoves() {
        return moves;
    }

    public String getSpecies() {
        return species;
    }

    public List<String> getGameIndices() {
        return gameIndices;
    }

    /**
     * Calcola l'altezza in metri
     */
    public double getHeightInMeters() {
        return height / 10.0;
    }

    /**
     * Calcola il peso in kg
     */
    public double getWeightInKg() {
        return weight / 10.0;
    }
}