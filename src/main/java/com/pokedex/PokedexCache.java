package com.pokedex;

import com.pokedex.pokemon.Pokemon;
import com.pokedex.pokemon.PokemonData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sistema di cache per i dati dei Pokémon per evitare di ricaricarli
 * ad ogni accesso e migliorare le prestazioni dell'applicazione
 */
public class PokedexCache {
    // Cache per i dati completi dei singoli Pokémon (id -> PokemonData)
    private final Map<Integer, Pokemon> pokemonCache = new ConcurrentHashMap<>();

    // Cache per i dati dettagliati dei Pokémon (id -> PokemonDetailData)
    private final Map<Integer, PokemonData> detailedPokemonCache = new ConcurrentHashMap<>();

    // Cache per i risultati di paginazione (offset -> List<PokemonData>)
    private final Map<Integer, List<Pokemon>> batchCache = new ConcurrentHashMap<>();

    // Cache per i risultati di ricerca
    private final Map<String, List<Pokemon>> searchCache = new ConcurrentHashMap<>();

    /**
     * Ottiene un Pokémon dalla cache se presente
     */
    public Pokemon getPokemon(int id) {
        return pokemonCache.get(id);
    }

    /**
     * Salva un Pokémon nella cache
     */
    public void cachePokemon(Pokemon pokemon) {
        pokemonCache.put(pokemon.getId(), pokemon);
    }

    /**
     * Verifica se un Pokémon è presente nella cache
     */
    public boolean hasPokemon(int id) {
        return pokemonCache.containsKey(id);
    }

    /**
     * Ottiene un Pokémon dettagliato dalla cache se presente
     */
    public PokemonData getDetailedPokemon(int id) {
        return detailedPokemonCache.get(id);
    }

    /**
     * Salva un Pokémon dettagliato nella cache
     */
    public void cacheDetailedPokemon(PokemonData pokemon) {
        detailedPokemonCache.put(pokemon.getId(), pokemon);
        // Aggiorna anche la cache base
        pokemonCache.put(pokemon.getId(), pokemon);
    }

    /**
     * Verifica se un Pokémon dettagliato è presente nella cache
     */
    public boolean hasDetailedPokemon(int id) {
        return detailedPokemonCache.containsKey(id);
    }

    /**
     * Ottiene un batch di Pokémon dalla cache se presente
     */
    public List<Pokemon> getBatch(int offset, int limit) {
        return batchCache.get(offset);
    }

    /**
     * Salva un batch di Pokémon nella cache
     */
    public void cacheBatch(int offset, List<Pokemon> batch) {
        batchCache.put(offset, new ArrayList<>(batch));
    }

    /**
     * Verifica se un batch è presente nella cache
     */
    public boolean hasBatch(int offset) {
        return batchCache.containsKey(offset);
    }

    /**
     * Ottiene i risultati di una ricerca dalla cache se presenti
     */
    public List<Pokemon> getSearchResults(String query) {
        return searchCache.get(query.toLowerCase());
    }

    /**
     * Salva i risultati di una ricerca nella cache
     */
    public void cacheSearchResults(String query, List<Pokemon> results) {
        searchCache.put(query.toLowerCase(), new ArrayList<>(results));
    }

    /**
     * Verifica se i risultati di una ricerca sono presenti nella cache
     */
    public boolean hasSearchResults(String query) {
        return searchCache.containsKey(query.toLowerCase());
    }

    /**
     * Pulisce tutta la cache
     */
    public void clearCache() {
        pokemonCache.clear();
        detailedPokemonCache.clear();
        batchCache.clear();
        searchCache.clear();
    }

    /**
     * Dimensione attuale della cache dei Pokémon
     */
    public int size() {
        return pokemonCache.size();
    }

    /**
     * Ottiene tutti i Pokémon presenti nella cache come Collection
     * Metodo aggiunto per supportare la ricerca nella cache
     */
    public Collection<Pokemon> getPokemonMapValues() {
        return pokemonCache.values();
    }
}