package com.pokedex;

import com.pokedex.pokemon.Pokemon;
import com.pokedex.pokemon.PokemonData;
import javafx.application.Platform;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Controller che gestisce la logica dell'applicazione e coordina
 * il repository API con la cache
 */
public class PokedexController {
    private final PokedexApi repository;
    private final PokedexCache cache;
    private final int batchSize;
    private int currentOffset = 0;
    private boolean isLoading = false;

    public PokedexController(PokedexApi repository, PokedexCache cache, int batchSize) {
        this.repository = repository;
        this.cache = cache;
        this.batchSize = batchSize;
    }

    /**
     * Carica il prossimo batch di Pokémon, utilizzando la cache quando possibile
     */
    public void loadNextBatch(Consumer<List<Pokemon>> onSuccess, Consumer<String> onError) {
        if (isLoading) return;

        isLoading = true;

        // Controlla se il batch è già in cache
        if (cache.hasBatch(currentOffset)) {
            List<Pokemon> batch = cache.getBatch(currentOffset, batchSize);
            Platform.runLater(() -> {
                onSuccess.accept(batch);
                currentOffset += batchSize;
                isLoading = false;
            });
            return;
        }

        // Recupera il batch dalle API
        repository.fetchPokemonBatch(currentOffset, batchSize)
                .thenAccept(pokemonList -> {
                    // Salva nella cache ogni Pokémon e l'intero batch
                    for (Pokemon pokemon : pokemonList) {
                        cache.cachePokemon(pokemon);
                    }
                    cache.cacheBatch(currentOffset, pokemonList);

                    Platform.runLater(() -> {
                        onSuccess.accept(pokemonList);
                        currentOffset += batchSize;
                        isLoading = false;
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        onError.accept(e.getMessage());
                        isLoading = false;
                    });
                    return null;
                });
    }

    /**
     * Carica i dettagli completi di un singolo Pokémon
     */
    public void loadPokemonDetails(int pokemonId, Consumer<PokemonData> onSuccess, Consumer<String> onError) {
        // Verifica se i dettagli estesi sono già in cache
        if (cache.hasDetailedPokemon(pokemonId)) {
            PokemonData detailedPokemon = cache.getDetailedPokemon(pokemonId);
            Platform.runLater(() -> onSuccess.accept(detailedPokemon));
            return;
        }

        // Altrimenti recupera i dettagli completi dall'API
        repository.fetchPokemonDetailsComplete(pokemonId)
                .thenAccept(detailedPokemon -> {
                    // Salva i dettagli completi in cache
                    cache.cacheDetailedPokemon(detailedPokemon);

                    Platform.runLater(() -> onSuccess.accept(detailedPokemon));
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> onError.accept(e.getMessage()));
                    return null;
                });
    }

    /**
     * Cerca Pokémon per nome o ID
     */
    public void searchPokemon(String query, Consumer<List<Pokemon>> onSuccess, Consumer<String> onError) {
        if (isLoading) return;

        isLoading = true;

        // Controlla se la ricerca è già in cache
        if (cache.hasSearchResults(query)) {
            List<Pokemon> results = cache.getSearchResults(query);
            Platform.runLater(() -> {
                onSuccess.accept(results);
                isLoading = false;
            });
            return;
        }

        // Esegui la ricerca attraverso l'API
        repository.searchPokemonByNameOrId(query)
                .thenAccept(pokemon -> {
                    // Salva il risultato in cache
                    cache.cachePokemon(pokemon);
                    List<Pokemon> results = List.of(pokemon);
                    cache.cacheSearchResults(query, results);

                    Platform.runLater(() -> {
                        onSuccess.accept(results);
                        isLoading = false;
                    });
                })
                .exceptionally(e -> {
                    // Cerca nei Pokémon già in cache
                    List<Pokemon> cachedResults = searchInCache(query);
                    if (!cachedResults.isEmpty()) {
                        cache.cacheSearchResults(query, cachedResults);
                        Platform.runLater(() -> {
                            onSuccess.accept(cachedResults);
                            isLoading = false;
                        });
                    } else {
                        Platform.runLater(() -> {
                            onError.accept("Nessun risultato trovato per: " + query);
                            isLoading = false;
                        });
                    }
                    return null;
                });
    }

    /**
     * Ricerca nei Pokémon già presenti in cache
     */
    private List<Pokemon> searchInCache(String query) {
        String lowercaseQuery = query.toLowerCase();
        return cache.getPokemonMapValues().stream()
                .filter(p -> p.getName().toLowerCase().contains(lowercaseQuery) ||
                        String.valueOf(p.getId()).equals(lowercaseQuery))
                .collect(Collectors.toList());
    }

    /**
     * Restituisce lo stato del caricamento
     */
    public boolean isLoading() {
        return isLoading;
    }

    /**
     * Resetta l'offset per ricominciare da capo
     */
    public void resetOffset() {
        currentOffset = 0;
    }
}