package com.pokedex.utils;

import com.pokedex.pokemon.Pokemon;
import com.pokedex.pokemon.PokemonData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.Map;

/**
 * Factory aggiornata per la creazione di componenti UI con il nuovo design
 */
public class UIFactory {

    /**
     * Crea una card per un Pokémon con il nuovo design
     */
    public static VBox createPokemonCard(Pokemon pokemon) {
        VBox card = new VBox(5);
        card.getStyleClass().add("pokemon-card");
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(160, 220);

        // Numero Pokémon
        Label numberLabel = new Label("#" + String.format("%03d", pokemon.getId()));
        numberLabel.getStyleClass().add("pokemon-number");
        HBox numberBox = new HBox(numberLabel);
        numberBox.setAlignment(Pos.CENTER_RIGHT);
        numberBox.setPrefWidth(Double.MAX_VALUE);

        // Immagine con effetto fade-in
        ImageView imageView = new ImageView(new Image(pokemon.getImageUrl(), true));
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("fade-in");

        // Nome Pokémon
        Label nameLabel = new Label(capitalize(pokemon.getName()));
        nameLabel.getStyleClass().add("pokemon-name");

        // Tipi con nuovo design
        HBox typesBox = new HBox(6);
        typesBox.setAlignment(Pos.CENTER);
        typesBox.getStyleClass().add("type-box");

        for (String type : pokemon.getTypes()) {
            Label typeLabel = new Label(capitalize(type));
            typeLabel.getStyleClass().addAll("type-label", TypeStyleUtils.getTypeClass(type));
            typesBox.getChildren().add(typeLabel);
        }

        // Pulsante dettagli più discreto
        Button detailsButton = new Button("Dettagli");
        detailsButton.getStyleClass().addAll("btn", "btn-light", "btn-sm");
        detailsButton.setPrefWidth(100);

        card.getChildren().addAll(numberBox, imageView, nameLabel, typesBox, detailsButton);

        // Applica stile in base al tipo primario
        TypeStyleUtils.applyTypeStyle(card, pokemon);

        return card;
    }

    /**
     * Crea un placeholder per una card durante il caricamento
     */
    public static VBox createPlaceholder() {
        VBox placeholder = new VBox(10);
        placeholder.getStyleClass().add("placeholder");
        placeholder.setPrefSize(160, 200);
        placeholder.setPadding(new Insets(12));

        // Numero placeholder
        Label numberLabel = new Label("#000");
        numberLabel.getStyleClass().add("pokemon-number");
        HBox numberBox = new HBox(numberLabel);
        numberBox.setAlignment(Pos.CENTER_RIGHT);
        numberBox.setPrefWidth(Double.MAX_VALUE);

        // Immagine placeholder
        Region imageArea = new Region();
        imageArea.setPrefSize(120, 120);
        imageArea.getStyleClass().add("placeholder-image");

        // Nome placeholder
        Region textLine = new Region();
        textLine.setPrefSize(100, 16);
        textLine.getStyleClass().add("placeholder-text");

        // Tipi placeholder
        HBox typesBox = new HBox(10);
        typesBox.setAlignment(Pos.CENTER);

        Region type1 = new Region();
        type1.setPrefSize(50, 20);
        type1.getStyleClass().add("placeholder-text");

        Region type2 = new Region();
        type2.setPrefSize(50, 20);
        type2.getStyleClass().add("placeholder-text");

        typesBox.getChildren().addAll(type1, type2);

        placeholder.setAlignment(Pos.CENTER);
        placeholder.getChildren().addAll(numberBox, imageArea, textLine, typesBox);

        return placeholder;
    }

    /**
     * Metodo utility per capitalizzare la prima lettera
     */
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Crea il container di generazioni
     */
    public static HBox createGenerationSelector(GenerationSelector selector) {
        HBox container = new HBox();
        container.getStyleClass().add("generation-container");
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(5, 0, 10, 0));

        Label titleLabel = new Label("Select Gen:");
        titleLabel.getStyleClass().add("gen-title");

        container.getChildren().addAll(titleLabel, selector.getNode());

        return container;
    }

    /**
     * Crea un messaggio per nessun risultato
     */
    public static VBox createNoResultsMessage() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50, 20, 50, 20));

        ImageView imageView = new ImageView(new Image(UIFactory.class.getResourceAsStream("/images/not-found.png")));
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        Label messageLabel = new Label("Nessun Pokémon trovato");
        messageLabel.getStyleClass().add("no-results-message");

        Label subMessageLabel = new Label("Prova a cercare con un nome o numero diverso");
        subMessageLabel.getStyleClass().add("no-results-submessage");

        container.getChildren().addAll(imageView, messageLabel, subMessageLabel);

        return container;
    }

    /**
     * Crea la barra del header migliorata
     */
    public static HBox createImprovedHeader(Button backButton) {
        HBox header = new HBox();
        header.setPadding(new Insets(15));
        header.setSpacing(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header");

        // Logo Pokédex
        // Titolo Pokédex (invece del logo, se l'immagine non è disponibile)
        Label title = new Label("Pokédex");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.getStyleClass().add("clickable-title");

        // Primo spacer per posizionare il pulsante al centro
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Pulsante "Torna al Pokédex" (in mezzo)
        if (backButton != null) {
            backButton.getStyleClass().addAll("btn", "btn-light");
        }

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

        // Aggiungi funzionalità di ricerca all'evento Enter sulla casella di testo
        searchField.setOnAction(e -> searchButton.fire());

        searchArea.getChildren().addAll(searchField, searchButton);

        // Aggiungi componenti al header
        if (backButton != null) {
            header.getChildren().addAll(title, spacer1, backButton, spacer2, searchArea);
        } else {
            header.getChildren().addAll(title, spacer1, spacer2, searchArea);
        }

        return header;
    }

    /**
     * Crea una vista dettagliata completa per un Pokémon col nuovo design
     */
    public static ScrollPane createImprovedPokemonDetailView(PokemonData pokemon, Runnable onClose) {
        // Container principale
        VBox detailContainer = new VBox(20);
        detailContainer.setPadding(new Insets(20));
        detailContainer.getStyleClass().addAll("pokemon-detail-container", pokemon.getTypes().get(0).toLowerCase());

        // Header con pulsante di chiusura
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("#" + String.format("%03d", pokemon.getId()) + " " + capitalize(pokemon.getName()));
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.getStyleClass().add("detail-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("Chiudi");
        closeButton.getStyleClass().addAll("btn", "btn-danger");
        closeButton.setOnAction(e -> onClose.run());

        header.getChildren().addAll(titleLabel, spacer, closeButton);

        // Immagine e informazioni di base
        HBox basicInfo = new HBox(30);
        basicInfo.setAlignment(Pos.CENTER_LEFT);

        // Immagine grande
        VBox imageContainer = new VBox(10);
        imageContainer.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView(new Image(pokemon.getImageUrl(), true));
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        // Tipi
        HBox typesBox = new HBox(10);
        typesBox.setAlignment(Pos.CENTER);

        for (String type : pokemon.getTypes()) {
            Label typeLabel = new Label(capitalize(type));
            typeLabel.getStyleClass().addAll("type-label", TypeStyleUtils.getTypeClass(type));
            typesBox.getChildren().add(typeLabel);
        }

        imageContainer.getChildren().addAll(imageView, typesBox);

        // Informazioni di base
        VBox infoBox = new VBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(10));
        infoBox.getStyleClass().add("info-box");

        // Altezza e peso
        Label heightLabel = new Label("Altezza: " + pokemon.getHeightInMeters() + " m");
        Label weightLabel = new Label("Peso: " + pokemon.getWeightInKg() + " kg");

        // Specie
        Label speciesLabel = new Label("Specie: " + capitalize(pokemon.getSpecies()));

        // Abilità
        VBox abilitiesBox = new VBox(5);
        Label abilitiesTitle = new Label("Abilità:");
        abilitiesTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        abilitiesBox.getChildren().add(abilitiesTitle);

        for (String ability : pokemon.getAbilities()) {
            Label abilityLabel = new Label("• " + capitalize(ability));
            abilitiesBox.getChildren().add(abilityLabel);
        }

        infoBox.getChildren().addAll(heightLabel, weightLabel, speciesLabel, abilitiesBox);

        basicInfo.getChildren().addAll(imageContainer, infoBox);

        // Separatore
        Separator separator1 = new Separator();

        // Statistiche
        VBox statsContainer = new VBox(10);
        Label statsTitle = new Label("Statistiche di base");
        statsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Grafico per le statistiche
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> statsChart = new BarChart<>(xAxis, yAxis);
        statsChart.setTitle("");
        statsChart.setLegendVisible(false);
        statsChart.setPrefHeight(300);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Traduci i nomi delle statistiche
        Map<String, String> statNames = Map.of(
                "hp", "HP",
                "attack", "Attacco",
                "defense", "Difesa",
                "special-attack", "Att. Sp.",
                "special-defense", "Dif. Sp.",
                "speed", "Velocità"
        );

        for (Map.Entry<String, Integer> stat : pokemon.getStats().entrySet()) {
            String statName = statNames.getOrDefault(stat.getKey(), capitalize(stat.getKey()));
            series.getData().add(new XYChart.Data<>(statName, stat.getValue()));
        }

        statsChart.getData().add(series);

        // Colora le barre in base ai valori e al tipo del Pokémon
        String primaryType = pokemon.getTypes().get(0).toLowerCase();
        String barColor = TypeStyleUtils.CARD_BORDER_COLORS.getOrDefault(primaryType, "#5DA0FF");

        for (XYChart.Data<String, Number> data : series.getData()) {
            int value = data.getYValue().intValue();

            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: " + barColor + ";");
                }
            });
        }

        statsContainer.getChildren().addAll(statsTitle, statsChart);

        // Separatore
        Separator separator2 = new Separator();

        // Mosse
        VBox movesContainer = new VBox(10);
        Label movesTitle = new Label("Mosse (prime 10)");
        movesTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

        FlowPane movesGrid = new FlowPane();
        movesGrid.setHgap(10);
        movesGrid.setVgap(10);

        for (String move : pokemon.getMoves()) {
            Label moveLabel = new Label(capitalize(move));
            moveLabel.getStyleClass().add("move-label");
            moveLabel.setPadding(new Insets(5, 10, 5, 10));
            movesGrid.getChildren().add(moveLabel);
        }

        movesContainer.getChildren().addAll(movesTitle, movesGrid);

        // Aggiunta di tutti i componenti al container principale
        detailContainer.getChildren().addAll(
                header,
                basicInfo,
                separator1,
                statsContainer,
                separator2,
                movesContainer
        );

        // Crea e configura ScrollPane
        ScrollPane scrollPane = new ScrollPane(detailContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("detail-scroll-pane");

        return scrollPane;
    }
}