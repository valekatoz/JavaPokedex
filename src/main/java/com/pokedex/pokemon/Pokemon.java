package com.pokedex.pokemon;

import java.util.List;

/**
 * Classe immutabile che rappresenta i dati di un singolo Pokémon
 */
public class Pokemon {
    private final int id;
    private final String name;
    private final String imageUrl;
    private final List<String> types;

    public Pokemon(int id, String name, String imageUrl, List<String> types) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.types = List.copyOf(types); // Crea una copia immutabile
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<String> getTypes() {
        return types;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pokemon that = (Pokemon) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}