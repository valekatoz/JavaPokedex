package com.pokedex;

import com.pokedex.pokemon.Pokemon;
import com.pokedex.pokemon.PokemonData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository responsabile dell'interazione con le PokeAPI
 */
public class PokedexApi {
    private static final String BASE_URL = "https://pokeapi.co/api/v2";
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * Ottiene un batch di Pokémon dalla API
     */
    public CompletableFuture<List<Pokemon>> fetchPokemonBatch(int offset, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<Pokemon> pokemonList = new ArrayList<>();
            try {
                URL url = new URL(BASE_URL + "/pokemon?offset=" + offset + "&limit=" + limit);
                String response = makeHttpRequest(url);

                JSONObject json = new JSONObject(response);
                JSONArray results = json.getJSONArray("results");

                List<CompletableFuture<Pokemon>> futures = new ArrayList<>();

                for (int i = 0; i < results.length(); i++) {
                    JSONObject pokemonJson = results.getJSONObject(i);
                    String pokemonUrl = pokemonJson.getString("url");

                    CompletableFuture<Pokemon> future = fetchPokemonDetailsBasic(pokemonUrl);
                    futures.add(future);
                }

                for (CompletableFuture<Pokemon> future : futures) {
                    try {
                        Pokemon pokemon = future.get();
                        if (pokemon != null) {
                            pokemonList.add(pokemon);
                        }
                    } catch (Exception e) {
                        System.err.println("Errore nel recupero dei dettagli del Pokémon: " + e.getMessage());
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException("Errore nel recupero dei Pokémon: " + e.getMessage(), e);
            }

            return pokemonList;
        }, executorService);
    }

    /**
     * Ottiene i dettagli di base di un Pokémon dalla API
     */
    public CompletableFuture<Pokemon> fetchPokemonDetailsBasic(String pokemonUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(pokemonUrl);
                String response = makeHttpRequest(url);

                JSONObject pokemonJson = new JSONObject(response);

                int id = pokemonJson.getInt("id");
                String name = pokemonJson.getString("name");
                String imageUrl = pokemonJson.getJSONObject("sprites").getJSONObject("other")
                        .getJSONObject("official-artwork").getString("front_default");

                // Ottieni i tipi
                List<String> types = new ArrayList<>();
                JSONArray typesArray = pokemonJson.getJSONArray("types");
                for (int i = 0; i < typesArray.length(); i++) {
                    String type = typesArray.getJSONObject(i).getJSONObject("type").getString("name");
                    types.add(type);
                }

                return new Pokemon(id, name, imageUrl, types);
            } catch (Exception e) {
                throw new RuntimeException("Errore nel recupero dei dettagli del Pokémon: " + e.getMessage(), e);
            }
        }, executorService);
    }

    /**
     * Ottiene i dettagli completi di un Pokémon dalla API
     */
    public CompletableFuture<PokemonData> fetchPokemonDetailsComplete(int pokemonId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(BASE_URL + "/pokemon/" + pokemonId);
                String response = makeHttpRequest(url);

                JSONObject pokemonJson = new JSONObject(response);

                int id = pokemonJson.getInt("id");
                String name = pokemonJson.getString("name");
                String imageUrl = pokemonJson.getJSONObject("sprites").getJSONObject("other")
                        .getJSONObject("official-artwork").getString("front_default");

                // Altezza e peso
                int height = pokemonJson.getInt("height");
                int weight = pokemonJson.getInt("weight");

                // Tipi
                List<String> types = new ArrayList<>();
                JSONArray typesArray = pokemonJson.getJSONArray("types");
                for (int i = 0; i < typesArray.length(); i++) {
                    String type = typesArray.getJSONObject(i).getJSONObject("type").getString("name");
                    types.add(type);
                }

                // Abilità
                List<String> abilities = new ArrayList<>();
                JSONArray abilitiesArray = pokemonJson.getJSONArray("abilities");
                for (int i = 0; i < abilitiesArray.length(); i++) {
                    JSONObject abilityObj = abilitiesArray.getJSONObject(i);
                    String ability = abilityObj.getJSONObject("ability").getString("name");
                    boolean isHidden = abilityObj.getBoolean("is_hidden");
                    abilities.add(ability + (isHidden ? " (nascosta)" : ""));
                }

                // Statistiche
                Map<String, Integer> stats = new HashMap<>();
                JSONArray statsArray = pokemonJson.getJSONArray("stats");
                for (int i = 0; i < statsArray.length(); i++) {
                    JSONObject statObj = statsArray.getJSONObject(i);
                    String statName = statObj.getJSONObject("stat").getString("name");
                    int statValue = statObj.getInt("base_stat");
                    stats.put(statName, statValue);
                }

                // Mosse (limitate alle prime 10 per brevità)
                List<String> moves = new ArrayList<>();
                JSONArray movesArray = pokemonJson.getJSONArray("moves");
                for (int i = 0; i < Math.min(movesArray.length(), 10); i++) {
                    String move = movesArray.getJSONObject(i).getJSONObject("move").getString("name");
                    moves.add(move);
                }

                // Specie
                String species = pokemonJson.getJSONObject("species").getString("name");

                // Indici nei giochi
                List<String> gameIndices = new ArrayList<>();
                JSONArray gamesArray = pokemonJson.getJSONArray("game_indices");
                for (int i = 0; i < gamesArray.length(); i++) {
                    String game = gamesArray.getJSONObject(i).getJSONObject("version").getString("name");
                    gameIndices.add(game);
                }

                return new PokemonData(id, name, imageUrl, types, height, weight,
                        abilities, stats, moves, species, gameIndices);

            } catch (Exception e) {
                throw new RuntimeException("Errore nel recupero dei dettagli completi del Pokémon: " + e.getMessage(), e);
            }
        }, executorService);
    }

    /**
     * Cerca un Pokémon per nome o ID
     */
    public CompletableFuture<Pokemon> searchPokemonByNameOrId(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(BASE_URL + "/pokemon/" + query.toLowerCase());
                String response = makeHttpRequest(url);

                JSONObject pokemonJson = new JSONObject(response);

                int id = pokemonJson.getInt("id");
                String name = pokemonJson.getString("name");
                String imageUrl = pokemonJson.getJSONObject("sprites").getJSONObject("other")
                        .getJSONObject("official-artwork").getString("front_default");

                // Ottieni i tipi
                List<String> types = new ArrayList<>();
                JSONArray typesArray = pokemonJson.getJSONArray("types");
                for (int i = 0; i < typesArray.length(); i++) {
                    String type = typesArray.getJSONObject(i).getJSONObject("type").getString("name");
                    types.add(type);
                }

                return new Pokemon(id, name, imageUrl, types);
            } catch (Exception e) {
                throw new RuntimeException("Pokémon non trovato: " + e.getMessage(), e);
            }
        }, executorService);
    }

    /**
     * Metodo helper per effettuare richieste HTTP
     */
    private String makeHttpRequest(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    /**
     * Chiude il thread pool al termine dell'applicazione
     */
    public void shutdown() {
        executorService.shutdown();
    }
}