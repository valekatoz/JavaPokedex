package com.pokedex;

import com.pokedex.pokemon.Pokemon;
import com.pokedex.utils.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

    @Override
    public void start(Stage primaryStage) {
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

        // Griglia Pokemon con scroll pane
        pokemonGrid = new FlowPane();
        pokemonGrid.setHgap(15);
        pokemonGrid.setVgap(15);
        pokemonGrid.setPadding(new Insets(15));
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

        VBox contentArea = new VBox(10);
        contentArea.getChildren().addAll(scrollPane);
        contentArea.setPadding(new Insets(10));

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
        Scene scene = new Scene(overlayPane, 800, 600);

        // Aggiungi CSS con il metodo corretto per BootstrapFX
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/chat-styles.css").toExternalForm());

        // Configura lo stage
        primaryStage.setTitle("Modern JavaFX Pokédex");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Carica il batch iniziale
        loadMorePokemon();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15));
        header.setSpacing(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header");

        // Titolo Pokédex (a sinistra)
        Label title = new Label("Pokédex");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        title.setOnMouseClicked(e -> resetAndShowAllPokemon());
        title.getStyleClass().add("clickable-title");

        // Primo spacer per posizionare il pulsante al centro
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Pulsante "Torna al Pokédex" (in mezzo)
        backButton = new Button("Torna al Pokédex");
        backButton.getStyleClass().addAll("btn", "btn-light");
        backButton.setVisible(false); // Inizialmente nascosto
        backButton.setOnAction(e -> resetAndShowAllPokemon());

        // Secondo spacer per posizionare la ricerca a destra
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        // Area di ricerca (a destra)
        HBox searchArea = new HBox(5);
        searchArea.setAlignment(Pos.CENTER_RIGHT);

        TextField searchField = new TextField();
        searchField.setPromptText("Cerca Pokémon");
        searchField.getStyleClass().add("search-field");

        Button searchButton = new Button("Cerca");
        searchButton.getStyleClass().addAll("btn", "btn-success");
        searchButton.setOnAction(e -> performSearch(searchField.getText()));

        // Aggiungi funzionalità di ricerca all'evento Enter sulla casella di testo
        searchField.setOnAction(e -> searchButton.fire());

        searchArea.getChildren().addAll(searchField, searchButton);

        header.getChildren().addAll(title, spacer1, backButton, spacer2, searchArea);

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

                    // Aggiungi le card con i dati reali
                    for (Pokemon pokemon : pokemonList) {
                        VBox pokemonCard = UIFactory.createPokemonCard(pokemon);

                        // Aggiungi un pulsante per visualizzare i dettagli del Pokémon
                        Button detailsButton = new Button("Dettagli");
                        detailsButton.getStyleClass().addAll("btn", "btn-info", "btn-sm");
                        detailsButton.setOnAction(e -> showPokemonDetails(pokemon));

                        // Aggiungi il pulsante alla card
                        ((VBox) pokemonCard).getChildren().add(detailsButton);

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
            Label noResults = new Label("Nessun risultato trovato");
            noResults.getStyleClass().add("no-results");
            pokemonGrid.getChildren().add(noResults);
        } else {
            for (Pokemon pokemon : results) {
                VBox pokemonCard = UIFactory.createPokemonCard(pokemon);

                // Aggiungi un pulsante per visualizzare i dettagli del Pokémon
                Button detailsButton = new Button("Dettagli");
                detailsButton.getStyleClass().addAll("btn", "btn-info", "btn-sm");
                detailsButton.setOnAction(e -> showPokemonDetails(pokemon));

                // Aggiungi il pulsante alla card
                ((VBox) pokemonCard).getChildren().add(detailsButton);

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
        Scene detailScene = new Scene(detailRoot, 600, 500);
        detailScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        detailScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        detailStage.setScene(detailScene);
        detailStage.show();

        // Carica i dettagli completi
        controller.loadPokemonDetails(basicPokemon.getId(),
                detailedPokemon -> {
                    // Crea la visualizzazione dettagliata
                    ScrollPane detailView = UIFactory.createPokemonDetailView(detailedPokemon, () -> detailStage.close());
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