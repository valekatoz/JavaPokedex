package com.pokedex;

import com.pokedex.pokemon.Pokemon;
import com.pokedex.utils.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.util.List;

public class PokedexApp extends Application {

    private final int BATCH_SIZE = 30;
    private FlowPane pokemonGrid;
    private Label statusLabel;
    private Button backButton;
    private PokedexController controller;
    private BorderPane root;
    private Stage primaryStage;
    private ScrollPane scrollPane;
    private boolean isSearchMode = false;
    private ProfessorAssistant professorAssistant;
    private SelettoreGenerazione selettoreGenerazione;
    private TextField searchField;
    private MusicManager musicManager = new MusicManager();

    @Override
    public void start(Stage primaryStage) {


        musicManager.play();
        this.primaryStage = primaryStage;

        PokedexCache cache = new PokedexCache();
        PokedexApi repository = new PokedexApi();
        controller = new PokedexController(repository, cache, BATCH_SIZE);

        professorAssistant = new ProfessorAssistant(ConfigUtils.OPENAI_API_KEY);

        root = new BorderPane();
        root.getStyleClass().add("root");

        HBox header = createHeader();
        root.setTop(header);

        selettoreGenerazione = new SelettoreGenerazione(this::filterByGeneration);
        HBox genSelectorContainer = UI.createGenerationSelector(selettoreGenerazione);

        pokemonGrid = new FlowPane();
        pokemonGrid.setHgap(20);
        pokemonGrid.setVgap(20);
        pokemonGrid.setPadding(new Insets(20));
        pokemonGrid.setPrefWidth(800);

        scrollPane = new ScrollPane(pokemonGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");

        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (isSearchMode) return;

            int selectedGeneration = selettoreGenerazione.getSelectedGeneration();

            if (newValue.doubleValue() > 0.8 && !controller.isLoading()) {
                if (selectedGeneration == 0 || !controller.isGenerationFullyLoaded(selectedGeneration)) {
                    loadMorePokemon();
                }
            }
        });

        VBox contentArea = new VBox(5);
        contentArea.getChildren().addAll(genSelectorContainer, scrollPane);
        contentArea.setPadding(new Insets(0, 10, 10, 10));

        root.setCenter(contentArea);

        VBox bottomArea = new VBox(10);
        bottomArea.setAlignment(Pos.CENTER);
        bottomArea.setPadding(new Insets(10));

        statusLabel = new Label("Caricamento in corso...");
        statusLabel.setVisible(false);

        bottomArea.getChildren().add(statusLabel); // Rimuovi volumeControl da qui
        root.setBottom(bottomArea);

        StackPane overlayPane = new StackPane();
        overlayPane.getChildren().add(root);

        Button professorButton = professorAssistant.createChatButton();
        StackPane.setAlignment(professorButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(professorButton, new Insets(0, 20, 20, 0));
        overlayPane.getChildren().add(professorButton);

        Scene scene = new Scene(overlayPane, 850, 650);

        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        scene.getStylesheets().add(getClass().getResource("/pokemon-styles.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/chat-styles.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("Pokédex Tommypat e Valekatoz");
        primaryStage.setScene(scene);
        primaryStage.show();

        loadMorePokemon();
    }

    private HBox createHeader() {
        backButton = new Button("Torna al Pokédex");
        backButton.setVisible(false);
        backButton.setOnAction(e -> resetAndShowAllPokemon());

        HBox header = UI.createImprovedHeader(backButton);

        searchField = (TextField) header.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .flatMap(hbox -> ((HBox) hbox).getChildren().stream())
                .filter(node -> node instanceof TextField)
                .findFirst()
                .orElse(new TextField());

        Button searchButton = (Button) header.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .flatMap(hbox -> ((HBox) hbox).getChildren().stream())
                .filter(node -> node instanceof Button && ((Button) node).getText().equals("Cerca"))
                .findFirst()
                .orElse(new Button("Cerca"));

        searchButton.setOnAction(e -> performSearch(searchField.getText()));

        // Crea controllo volume
        HBox volumeControl = createVolumeControl();

        // Aggiungi il controllo volume all'header
        // Trova lo spaziatore (spacer2) e inserisci il controllo volume prima di esso
        for (int i = 0; i < header.getChildren().size(); i++) {
            if (header.getChildren().get(i) instanceof Region && i > 0 &&
                    HBox.getHgrow(header.getChildren().get(i)) == Priority.ALWAYS &&
                    i < header.getChildren().size() - 1) {
                header.getChildren().add(i, volumeControl);
                break;
            }
        }

        return header;
    }

    private void performSearch(String searchTerm) {
        searchTerm = searchTerm.trim();
        if (searchTerm.isEmpty()) return;

        backButton.setVisible(true);
        isSearchMode = true;

        controller.searchPokemon(searchTerm,
                result -> updateUIWithSearchResults(result),
                error -> showError("Errore durante la ricerca: " + error));
    }

    private void loadMorePokemon() {
        if (controller.isLoading()) return;

        int selectedGeneration = selettoreGenerazione.getSelectedGeneration();
        statusLabel.setVisible(true);

        int placeholdersToAdd = selectedGeneration == 0 ? BATCH_SIZE : 10;
        for (int i = 0; i < placeholdersToAdd; i++) {
            pokemonGrid.getChildren().add(UI.createPlaceholder());
        }

        if (selectedGeneration == 0) {
            controller.loadNextBatch(
                    pokemonList -> {
                        pokemonGrid.getChildren().removeIf(node -> node.getStyleClass().contains("placeholder"));

                        for (Pokemon pokemon : pokemonList) {
                            VBox pokemonCard = UI.createPokemonCard(pokemon);
                            pokemonCard.setOnMouseClicked(e -> showPokemonDetails(pokemon));
                            pokemonGrid.getChildren().add(pokemonCard);
                        }

                        statusLabel.setVisible(false);
                    },
                    error -> {
                        pokemonGrid.getChildren().removeIf(node -> node.getStyleClass().contains("placeholder"));
                        showError("Errore durante il caricamento: " + error);
                    }
            );
        } else {
            controller.loadPokemonByGeneration(selectedGeneration,
                    pokemonList -> {
                        pokemonGrid.getChildren().removeIf(node -> node.getStyleClass().contains("placeholder"));

                        if (pokemonList.isEmpty()) {
                            Label noResultsLabel = new Label("Nessun Pokémon trovato per questa generazione");
                            noResultsLabel.getStyleClass().add("no-results-message");
                            pokemonGrid.getChildren().add(noResultsLabel);
                        } else {
                            for (Pokemon pokemon : pokemonList) {
                                VBox pokemonCard = UI.createPokemonCard(pokemon);
                                pokemonCard.setOnMouseClicked(e -> showPokemonDetails(pokemon));
                                pokemonGrid.getChildren().add(pokemonCard);
                            }

                            if (!controller.isGenerationFullyLoaded(selectedGeneration) &&
                                    scrollPane.getVvalue() > 0.8) {
                                loadMorePokemon();
                            }
                        }

                        statusLabel.setVisible(false);
                    },
                    error -> {
                        pokemonGrid.getChildren().removeIf(node -> node.getStyleClass().contains("placeholder"));
                        showError("Errore durante il caricamento: " + error);
                    }
            );
        }
    }

    private void updateUIWithSearchResults(List<Pokemon> results) {
        pokemonGrid.getChildren().clear();

        if (results.isEmpty()) {
            pokemonGrid.getChildren().add(UI.createNoResultsMessage());
        } else {
            for (Pokemon pokemon : results) {
                VBox pokemonCard = UI.createPokemonCard(pokemon);
                pokemonCard.setOnMouseClicked(e -> showPokemonDetails(pokemon));
                pokemonGrid.getChildren().add(pokemonCard);
            }
        }
    }

    private void resetAndShowAllPokemon() {
        pokemonGrid.getChildren().clear();
        backButton.setVisible(false);
        isSearchMode = false;
        scrollPane.setVvalue(0);

        // Non resettare l'offset
        // controller.resetOffset();

        // Usa i Pokémon già caricati
        List<Pokemon> cachedPokemon = controller.getCachedPokemon();
        if (cachedPokemon != null && !cachedPokemon.isEmpty()) {
            for (Pokemon pokemon : cachedPokemon) {
                VBox pokemonCard = UI.createPokemonCard(pokemon);
                pokemonCard.setOnMouseClicked(e -> showPokemonDetails(pokemon));
                pokemonGrid.getChildren().add(pokemonCard);
            }
        } else {
            loadMorePokemon();
        }
    }

    private void filterByGeneration(int generation) {
        if (!isSearchMode) {
            pokemonGrid.getChildren().clear();
            scrollPane.setVvalue(0);

            if (generation == 0) {
                // Per "All", mostra i Pokémon già caricati invece di caricarne di nuovi
                List<Pokemon> cachedPokemon = controller.getCachedPokemon();
                if (cachedPokemon != null && !cachedPokemon.isEmpty()) {
                    for (Pokemon pokemon : cachedPokemon) {
                        VBox pokemonCard = UI.createPokemonCard(pokemon);
                        pokemonCard.setOnMouseClicked(e -> showPokemonDetails(pokemon));
                        pokemonGrid.getChildren().add(pokemonCard);
                    }
                    // Se siamo vicini alla fine, carica altri
                    if (scrollPane.getVvalue() > 0.8) {
                        loadMorePokemon();
                    }
                } else {
                    loadMorePokemon();
                }
            } else {
                loadMorePokemon();
            }
        }
    }

    private void showPokemonDetails(Pokemon basicPokemon) {
        Stage detailStage = new Stage();
        detailStage.setTitle("Dettagli di " + UI.capitalize(basicPokemon.getName()));

        BorderPane detailRoot = new BorderPane();

        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        Label loadingLabel = new Label("Caricamento dettagli...");
        loadingBox.getChildren().add(loadingLabel);
        detailRoot.setCenter(loadingBox);

        Scene detailScene = new Scene(detailRoot, 700, 700);
        detailScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        detailScene.getStylesheets().add(getClass().getResource("/pokemon-styles.css").toExternalForm());
        detailScene.getStylesheets().add(getClass().getResource("/pokemon-detail.css").toExternalForm());
        detailStage.setScene(detailScene);
        detailStage.show();

        controller.loadPokemonDetails(basicPokemon.getId(),
                detailedPokemon -> {
                    ScrollPane detailView = DettagliPokemon.createDetailView(detailedPokemon, () -> detailStage.close());
                    detailRoot.setCenter(detailView);
                },
                error -> {
                    Label errorLabel = new Label("Errore nel caricamento dei dettagli: " + error);
                    errorLabel.getStyleClass().add("error-message");
                    detailRoot.setCenter(errorLabel);
                }
        );
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
    }

    @Override
    public void stop() {
        if (professorAssistant != null) {
            professorAssistant.shutdown();
        }
        if (musicManager != null) {
            musicManager.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private HBox createVolumeControl() {
        HBox volumeBox = new HBox(8);
        volumeBox.setAlignment(Pos.CENTER);
        volumeBox.setPadding(new Insets(0, 5, 0, 5));
        volumeBox.getStyleClass().add("volume-control-box");

        // Icona audio (usiamo testo diretto)
        Button muteButton = new Button(musicManager.isMuted() ? "🔇" : "🔊");
        muteButton.getStyleClass().addAll("btn", "volume-button");
        muteButton.setOnAction(e -> {
            musicManager.toggleMute();
            // Aggiorna testo del pulsante
            muteButton.setText(musicManager.isMuted() ? "🔇" : "🔊");
        });

        // Slider volume
        Slider volumeSlider = new Slider(0, 1, musicManager.getVolume());
        volumeSlider.setPrefWidth(80);
        volumeSlider.getStyleClass().add("volume-slider");

        // Aggiorna volume quando lo slider viene mosso
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            musicManager.setVolume(newValue.floatValue());
            // Aggiorna icona in base al volume
            if (newValue.doubleValue() == 0) {
                muteButton.setText("🔇");
            } else if (newValue.doubleValue() < 0.5) {
                muteButton.setText("🔉");
            } else {
                muteButton.setText("🔊");
            }
        });

        volumeBox.getChildren().addAll(muteButton, volumeSlider);

        return volumeBox;
    }
}