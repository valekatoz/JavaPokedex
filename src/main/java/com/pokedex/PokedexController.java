package com.pokedex;

import com.pokedex.pokemon.Pokemon;
import com.pokedex.pokemon.PokemonData;
import com.pokedex.utils.SelettoreGenerazione;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    public void loadNextBatch(Consumer<List<Pokemon>> onSuccess, Consumer<String> onError) {
        if (isLoading) return;

        isLoading = true;

        if (cache.hasBatch(currentOffset)) {
            List<Pokemon> batch = cache.getBatch(currentOffset, batchSize);
            Platform.runLater(() -> {
                onSuccess.accept(batch);
                currentOffset += batchSize;
                isLoading = false;
            });
            return;
        }

        repository.fetchPokemonBatch(currentOffset, batchSize)
                .thenAccept(pokemonList -> {
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

    public void loadPokemonDetails(int pokemonId, Consumer<PokemonData> onSuccess, Consumer<String> onError) {
        if (cache.hasDetailedPokemon(pokemonId)) {
            PokemonData detailedPokemon = cache.getDetailedPokemon(pokemonId);
            Platform.runLater(() -> onSuccess.accept(detailedPokemon));
            return;
        }

        repository.fetchPokemonDetailsComplete(pokemonId)
                .thenAccept(detailedPokemon -> {
                    cache.cacheDetailedPokemon(detailedPokemon);

                    Platform.runLater(() -> onSuccess.accept(detailedPokemon));
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> onError.accept(e.getMessage()));
                    return null;
                });
    }

    public void searchPokemon(String query, Consumer<List<Pokemon>> onSuccess, Consumer<String> onError) {
        if (isLoading) return;

        isLoading = true;

        if (cache.hasSearchResults(query)) {
            List<Pokemon> results = cache.getSearchResults(query);
            Platform.runLater(() -> {
                onSuccess.accept(results);
                isLoading = false;
            });
            return;
        }

        repository.searchPokemonByNameOrId(query)
                .thenAccept(pokemon -> {
                    cache.cachePokemon(pokemon);
                    List<Pokemon> results = List.of(pokemon);
                    cache.cacheSearchResults(query, results);

                    Platform.runLater(() -> {
                        onSuccess.accept(results);
                        isLoading = false;
                    });
                })
                .exceptionally(e -> {
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

    private List<Pokemon> searchInCache(String query) {
        String lowercaseQuery = query.toLowerCase();
        return cache.getPokemonMapValues().stream()
                .filter(p -> p.getName().toLowerCase().contains(lowercaseQuery) ||
                        String.valueOf(p.getId()).equals(lowercaseQuery))
                .collect(Collectors.toList());
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void resetOffset() {
        currentOffset = 0;
    }

    public void loadPokemonByGeneration(int generation, Consumer<List<Pokemon>> onSuccess, Consumer<String> onError) {
        if (isLoading) return;

        isLoading = true;

        int[] limits = SelettoreGenerazione.getGenerationLimits(generation);
        int startId = limits[0];
        int endId = limits[1];

        if (generation == 0) {
            loadNextBatch(onSuccess, onError);
            return;
        }

        List<Pokemon> cachedGenerationPokemon = getCachedPokemonByGeneration(generation);
        if (!cachedGenerationPokemon.isEmpty() && cachedGenerationPokemon.size() >= (endId - startId + 1)) {
            Platform.runLater(() -> {
                onSuccess.accept(cachedGenerationPokemon);
                isLoading = false;
            });
            return;
        }

        List<CompletableFuture<Pokemon>> futures = new ArrayList<>();

        for (int id = startId; id <= endId; id++) {
            final int pokemonId = id;

            if (cache.hasPokemon(pokemonId)) {
                Pokemon pokemon = cache.getPokemon(pokemonId);
                futures.add(CompletableFuture.completedFuture(pokemon));
            } else {
                String url = "https://pokeapi.co/api/v2/pokemon/" + pokemonId;
                futures.add(repository.fetchPokemonDetailsBasic(url));
            }

            if (futures.size() >= 10) {
                break;
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> {
                    List<Pokemon> result = new ArrayList<>();
                    for (CompletableFuture<Pokemon> future : futures) {
                        try {
                            Pokemon pokemon = future.get();
                            if (pokemon != null) {
                                cache.cachePokemon(pokemon);
                                result.add(pokemon);
                            }
                        } catch (Exception e) {
                            System.err.println("Errore nel recupero del Pokémon: " + e.getMessage());
                        }
                    }

                    Platform.runLater(() -> {
                        onSuccess.accept(result);
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

    private List<Pokemon> getCachedPokemonByGeneration(int generation) {
        if (generation == 0) {
            return new ArrayList<>(cache.getPokemonMapValues());
        }

        int[] limits = SelettoreGenerazione.getGenerationLimits(generation);
        int startId = limits[0];
        int endId = limits[1];

        return cache.getPokemonMapValues().stream()
                .filter(p -> p.getId() >= startId && p.getId() <= endId)
                .sorted((p1, p2) -> Integer.compare(p1.getId(), p2.getId()))
                .collect(Collectors.toList());
    }

    public boolean isGenerationFullyLoaded(int generation) {
        if (generation == 0) {
            return false;
        }

        int[] limits = SelettoreGenerazione.getGenerationLimits(generation);
        int startId = limits[0];
        int endId = limits[1];
        int expectedSize = endId - startId + 1;

        List<Pokemon> generationPokemon = getCachedPokemonByGeneration(generation);
        return generationPokemon.size() >= expectedSize;
    }

    public List<Pokemon> getCachedPokemon() {
        return cache.getPokemonMapValues().stream()
                .sorted((p1, p2) -> Integer.compare(p1.getId(), p2.getId()))
                .collect(Collectors.toList());
    }
}