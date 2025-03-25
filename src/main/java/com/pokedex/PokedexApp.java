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
import java.util.stream.Collectors;

/**
 * Applicazione Pokédex con UI migliorata
 */
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
    private GenerationSelector generationSelector;
    private TextField searchField;

    @Override
    public void start(Stage primaryStage) {
        MusicManager musicManager = new MusicManager();
        musicManager.play();
        this.primaryStage = primaryStage;

        // Inizializza il controller con cache e repository
        PokedexCache cache = new PokedexCache();
        PokedexApi repository = new PokedexApi();
        controller = new PokedexController(repository, cache, BATCH_SIZE);

        // Inizializza l'assistente del professore
        professorAssistant = new ProfessorAssistant(ConfigUtils.OPENAI_API_KEY);

        // Container principale
        root = new BorderPane();
        root.getStyleClass().add("root");

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Selettore generazioni
        generationSelector = new GenerationSelector(this::filterByGeneration);
        HBox genSelectorContainer = UIFactory.createGenerationSelector(generationSelector);

        // Griglia Pokemon con scroll pane
        pokemonGrid = new FlowPane();
        pokemonGrid.setHgap(20);
        pokemonGrid.setVgap(20);
        pokemonGrid.setPadding(new Insets(20));
        pokemonGrid.setPrefWidth(800);

        scrollPane = new ScrollPane(pokemonGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");

        // Implementazione dello scroll infinito
        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            // Se siamo in modalità ricerca, non caricare automaticamente
            if (isSearchMode) return;

            // Se l'utente è vicino al fondo e non stiamo già caricando
            if (newValue.doubleValue() > 0.9 && !controller.isLoading()) {
                loadMorePokemon();
            }
        });

        VBox contentArea = new VBox(5);
        contentArea.getChildren().addAll(genSelectorContainer, scrollPane);
        contentArea.setPadding(new Insets(0, 10, 10, 10));

        root.setCenter(contentArea);

        // Area stato
        VBox bottomArea = new VBox(10);
        bottomArea.setAlignment(Pos.CENTER);
        bottomArea.setPadding(new Insets(10));

        statusLabel = new Label("Caricamento in corso...");
        statusLabel.setVisible(false);

        bottomArea.getChildren().add(statusLabel);
        root.setBottom(bottomArea);

        // Aggiungi il pulsante flottante del professore
        StackPane overlayPane = new StackPane();
        overlayPane.getChildren().add(root);

        Button professorButton = professorAssistant.createChatButton();
        StackPane.setAlignment(professorButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(professorButton, new Insets(0, 20, 20, 0));
        overlayPane.getChildren().add(professorButton);

        // Crea la scena
        Scene scene = new Scene(overlayPane, 850, 650);

        // Aggiungi CSS con il metodo corretto per BootstrapFX
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        scene.getStylesheets().add(getClass().getResource("/pokemon-styles.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/chat-styles.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Configura lo stage
        primaryStage.setTitle("Modern Pokédex");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Carica il batch iniziale
        loadMorePokemon();
    }

    private HBox createHeader() {
        backButton = new Button("Torna al Pokédex");
        backButton.setVisible(false); // Inizialmente nascosto
        backButton.setOnAction(e -> resetAndShowAllPokemon());

        HBox header = UIFactory.createImprovedHeader(backButton);

        // Aggiungi funzionalità di ricerca
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

        return header;
    }

    private void performSearch(String searchTerm) {
        searchTerm = searchTerm.trim();
        if (searchTerm.isEmpty()) return;

        // Mostra il pulsante per tornare alla lista completa
        backButton.setVisible(true);
        isSearchMode = true;

        controller.searchPokemon(searchTerm,
                result -> updateUIWithSearchResults(result),
                error -> showError("Errore durante la ricerca: " + error));
    }

    private void loadMorePokemon() {
        if (controller.isLoading()) return;

        statusLabel.setVisible(true);

        // Aggiungi placeholders per il prossimo batch
        for (int i = 0; i < BATCH_SIZE; i++) {
            pokemonGrid.getChildren().add(UIFactory.createPlaceholder());
        }

        controller.loadNextBatch(
                pokemonList -> {
                    // Rimuovi i placeholders
                    pokemonGrid.getChildren().removeIf(node -> node.getStyleClass().contains("placeholder"));

                    // Filtra per generazione selezionata
                    List<Pokemon> filteredList = pokemonList.stream()
                            .filter(pokemon -> generationSelector.isInSelectedGeneration(pokemon.getId()))
                            .collect(Collectors.toList());

                    // Aggiungi le card con i dati reali
                    for (Pokemon pokemon : filteredList) {
                        VBox pokemonCard = UIFactory.createPokemonCard(pokemon);

                        // Aggiungi un listener per visualizzare i dettagli del Pokémon
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
    }

    private void updateUIWithSearchResults(List<Pokemon> results) {
        pokemonGrid.getChildren().clear();

        if (results.isEmpty()) {
            pokemonGrid.getChildren().add(UIFactory.createNoResultsMessage());
        } else {
            for (Pokemon pokemon : results) {
                VBox pokemonCard = UIFactory.createPokemonCard(pokemon);
                pokemonCard.setOnMouseClicked(e -> showPokemonDetails(pokemon));
                pokemonGrid.getChildren().add(pokemonCard);
            }
        }
    }

    private void resetAndShowAllPokemon() {
        // Pulisci la griglia
        pokemonGrid.getChildren().clear();

        // Resetta il controller
        controller.resetOffset();

        // Nascondi il pulsante indietro
        backButton.setVisible(false);

        // Disattiva la modalità ricerca
        isSearchMode = false;

        // Reimposta la posizione di scroll a inizio pagina
        scrollPane.setVvalue(0);

        // Carica nuovamente il primo batch
        loadMorePokemon();
    }

    private void filterByGeneration(int generation) {
        // Solo se non siamo in modalità ricerca
        if (!isSearchMode) {
            pokemonGrid.getChildren().clear();
            controller.resetOffset();
            scrollPane.setVvalue(0);
            loadMorePokemon();
        }
    }

    private void showPokemonDetails(Pokemon basicPokemon) {
        // Crea una nuova finestra per i dettagli
        Stage detailStage = new Stage();
        detailStage.setTitle("Dettagli di " + UIFactory.capitalize(basicPokemon.getName()));

        // Crea un contenitore con indicatore di caricamento
        BorderPane detailRoot = new BorderPane();

        // Indicatore di caricamento temporaneo
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        Label loadingLabel = new Label("Caricamento dettagli...");
        loadingBox.getChildren().add(loadingLabel);
        detailRoot.setCenter(loadingBox);

        // Mostra la finestra con l'indicatore di caricamento
        Scene detailScene = new Scene(detailRoot, 700, 700);
        detailScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        detailScene.getStylesheets().add(getClass().getResource("/pokemon-styles.css").toExternalForm());
        detailScene.getStylesheets().add(getClass().getResource("/pokemon-detail.css").toExternalForm());
        detailStage.setScene(detailScene);
        detailStage.show();

        // Carica i dettagli completi
        controller.loadPokemonDetails(basicPokemon.getId(),
                detailedPokemon -> {
                    // Crea la visualizzazione dettagliata con il nuovo design
                    ScrollPane detailView = DetailViewFactory.createDetailView(detailedPokemon, () -> detailStage.close());
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
        // Chiudi correttamente le risorse
        if (professorAssistant != null) {
            professorAssistant.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}