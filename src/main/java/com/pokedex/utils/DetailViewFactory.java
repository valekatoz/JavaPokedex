package com.pokedex.utils;

import com.pokedex.pokemon.PokemonData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Map;

/**
 * Classe per la creazione di una vista dettagliata migliorata dei Pokémon
 */
public class DetailViewFactory {

    /**
     * Crea una vista dettagliata avanzata per un Pokémon
     * @param pokemon Il Pokémon di cui mostrare i dettagli
     * @param onClose Callback da eseguire alla chiusura
     * @return Una ScrollPane contenente la vista dettagliata
     */
    public static ScrollPane createDetailView(PokemonData pokemon, Runnable onClose) {
        // Ottieni il tipo primario per lo styling
        String primaryType = pokemon.getTypes().get(0).toLowerCase();
        String accentColor = TypeStyleUtils.CARD_BORDER_COLORS.getOrDefault(primaryType, "#5DA0FF");

        // Container principale con sfondo basato sul tipo
        VBox detailContainer = new VBox(20);
        detailContainer.setPadding(new Insets(0, 0, 30, 0));
        detailContainer.getStyleClass().add("pokemon-detail-container");

        // Header con immagine di sfondo sfumata
        StackPane headerBackground = new StackPane();
        headerBackground.getStyleClass().add("detail-header-background");
        headerBackground.setStyle("-fx-background-color: linear-gradient(to bottom, " + accentColor + "30, transparent);");
        headerBackground.setMinHeight(200);

        // Contenuto dell'header
        VBox headerContent = new VBox(20);
        headerContent.setPadding(new Insets(20));
        headerContent.setAlignment(Pos.TOP_CENTER);

        // Numero del Pokémon
        Label idLabel = new Label("#" + String.format("%03d", pokemon.getId()));
        idLabel.getStyleClass().add("detail-id");

        // Nome del Pokémon
        Label nameLabel = new Label(UIFactory.capitalize(pokemon.getName()));
        nameLabel.getStyleClass().add("detail-name");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 32));

        // Tipi del Pokémon
        HBox typesBox = new HBox(10);
        typesBox.setAlignment(Pos.CENTER);

        for (String type : pokemon.getTypes()) {
            Label typeLabel = new Label(UIFactory.capitalize(type));
            typeLabel.getStyleClass().addAll("type-label-large", TypeStyleUtils.getTypeClass(type));
            typesBox.getChildren().add(typeLabel);
        }

        headerContent.getChildren().addAll(idLabel, nameLabel, typesBox);
        headerBackground.getChildren().add(headerContent);

        // Immagine del Pokémon
        StackPane imageContainer = new StackPane();
        imageContainer.setMinHeight(230);
        imageContainer.setMaxHeight(230);

        ImageView pokemonImage = new ImageView(new Image(pokemon.getImageUrl(), true));
        pokemonImage.setFitHeight(200);
        pokemonImage.setPreserveRatio(true);
        pokemonImage.getStyleClass().add("pokemon-detail-image");

        // Effetto ombra circolare sotto l'immagine
        StackPane imageShadow = new StackPane();
        imageShadow.getStyleClass().add("image-shadow");
        imageShadow.setPrefWidth(180);
        imageShadow.setPrefHeight(40);
        imageShadow.setStyle("-fx-background-color: " + accentColor + "40;");

        // Posiziona ombra e immagine
        StackPane.setAlignment(imageShadow, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(pokemonImage, Pos.CENTER);
        imageContainer.getChildren().addAll(imageShadow, pokemonImage);

        // Sezione caratteristiche fisiche
        HBox physicalAttributes = new HBox(30);
        physicalAttributes.setAlignment(Pos.CENTER);
        physicalAttributes.setPadding(new Insets(20, 15, 30, 15));

        // Altezza
        VBox heightBox = new VBox(5);
        heightBox.setAlignment(Pos.CENTER);
        Label heightValue = new Label(String.format("%.1f m", pokemon.getHeightInMeters()));
        heightValue.getStyleClass().add("attribute-value");
        Label heightTitle = new Label("Altezza");
        heightTitle.getStyleClass().add("attribute-title");
        heightBox.getChildren().addAll(heightValue, heightTitle);

        // Peso
        VBox weightBox = new VBox(5);
        weightBox.setAlignment(Pos.CENTER);
        Label weightValue = new Label(String.format("%.1f kg", pokemon.getWeightInKg()));
        weightValue.getStyleClass().add("attribute-value");
        Label weightTitle = new Label("Peso");
        weightTitle.getStyleClass().add("attribute-title");
        weightBox.getChildren().addAll(weightValue, weightTitle);

        // Categoria
        VBox categoryBox = new VBox(5);
        categoryBox.setAlignment(Pos.CENTER);
        Label categoryValue = new Label(UIFactory.capitalize(pokemon.getSpecies()));
        categoryValue.getStyleClass().add("attribute-value");
        Label categoryTitle = new Label("Specie");
        categoryTitle.getStyleClass().add("attribute-title");
        categoryBox.getChildren().addAll(categoryValue, categoryTitle);

        physicalAttributes.getChildren().addAll(heightBox, weightBox, categoryBox);

        // Contenitore per le schede informative
        VBox infoTabs = new VBox(30);
        infoTabs.setPadding(new Insets(0, 15, 15, 15));

        // Scheda statistiche
        VBox statsCard = createStatsCard(pokemon, accentColor);

        // Scheda abilità
        VBox abilitiesCard = createAbilitiesCard(pokemon, accentColor);

        // Scheda mosse
        VBox movesCard = createMovesCard(pokemon, accentColor);

        // Aggiunta schede al contenitore
        infoTabs.getChildren().addAll(statsCard, abilitiesCard, movesCard);

        // Bottone per chiudere
        HBox buttonBar = new HBox();
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(20, 0, 0, 0));

        Button closeButton = new Button("Chiudi");
        closeButton.getStyleClass().addAll("btn", "btn-detail-close");
        closeButton.setStyle("-fx-background-color: " + accentColor + ";");
        closeButton.setPrefWidth(150);
        closeButton.setOnAction(e -> onClose.run());

        buttonBar.getChildren().add(closeButton);

        // Assemblaggio di tutti gli elementi
        detailContainer.getChildren().addAll(
                headerBackground,
                imageContainer,
                physicalAttributes,
                infoTabs,
                buttonBar
        );

        // Creazione ScrollPane
        ScrollPane scrollPane = new ScrollPane(detailContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("detail-scroll-pane");

        return scrollPane;
    }

    /**
     * Crea la scheda informativa delle statistiche
     */
    private static VBox createStatsCard(PokemonData pokemon, String accentColor) {
        VBox card = new VBox(15);
        card.getStyleClass().add("info-card");
        card.setPadding(new Insets(20));

        // Titolo sezione
        Label title = new Label("Statistiche di Base");
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-text-fill: " + accentColor + ";");

        // Statistiche
        VBox statsContainer = new VBox(15);

        // Traduci i nomi delle statistiche
        Map<String, String> statNames = Map.of(
                "hp", "HP",
                "attack", "Attacco",
                "defense", "Difesa",
                "special-attack", "Att. Sp.",
                "special-defense", "Dif. Sp.",
                "speed", "Velocità"
        );

        // Valore massimo per le statistiche (per scalare le barre)
        int maxStatValue = 255; // Valore massimo possibile per una statistica in Pokémon

        // Crea barre per ogni statistica
        for (Map.Entry<String, Integer> entry : pokemon.getStats().entrySet()) {
            String statName = statNames.getOrDefault(entry.getKey(), UIFactory.capitalize(entry.getKey()));
            int value = entry.getValue();

            // Contenitore per la singola statistica
            HBox statRow = new HBox(15);
            statRow.setAlignment(Pos.CENTER_LEFT);

            // Nome statistica
            Label nameLabel = new Label(statName);
            nameLabel.getStyleClass().add("stat-name");
            nameLabel.setPrefWidth(100);

            // Valore numerico
            Label valueLabel = new Label(String.valueOf(value));
            valueLabel.getStyleClass().add("stat-value");
            valueLabel.setPrefWidth(40);
            valueLabel.setAlignment(Pos.CENTER_RIGHT);

            // Barra della statistica
            HBox barContainer = new HBox();
            barContainer.setPrefHeight(16);
            barContainer.setPrefWidth(300);
            barContainer.setMaxWidth(300);
            barContainer.getStyleClass().add("stat-bar-container");

            // Calcola la dimensione della barra in base al valore della statistica
            // Utilizziamo una scala di 0-255 (valore max delle statistiche Pokémon)
            double percentage = (double) value / maxStatValue;
            double barWidth = percentage * 300; // 300px è la larghezza massima della barra

            Region statBar = new Region();
            statBar.getStyleClass().add("stat-bar");
            statBar.setStyle("-fx-background-color: " + accentColor + ";");
            statBar.setPrefWidth(barWidth);
            statBar.setPrefHeight(16);

            barContainer.getChildren().add(statBar);

            statRow.getChildren().addAll(nameLabel, valueLabel, barContainer);
            statsContainer.getChildren().add(statRow);
        }

        card.getChildren().addAll(title, statsContainer);
        return card;
    }

    /**
     * Crea la scheda informativa delle abilità
     */
    private static VBox createAbilitiesCard(PokemonData pokemon, String accentColor) {
        VBox card = new VBox(15);
        card.getStyleClass().add("info-card");
        card.setPadding(new Insets(20));

        // Titolo sezione
        Label title = new Label("Abilità");
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-text-fill: " + accentColor + ";");

        // Elenco abilità
        VBox abilitiesList = new VBox(10);

        for (String ability : pokemon.getAbilities()) {
            Label abilityLabel = new Label(UIFactory.capitalize(ability));
            abilityLabel.getStyleClass().add("ability-item");

            if (ability.contains("nascosta")) {
                abilityLabel.getStyleClass().add("hidden-ability");

                // Icona per abilità nascosta
                HBox abilityRow = new HBox(10);
                abilityRow.setAlignment(Pos.CENTER_LEFT);

                Label hiddenIcon = new Label("★");
                hiddenIcon.getStyleClass().add("hidden-icon");
                hiddenIcon.setStyle("-fx-text-fill: " + accentColor + ";");

                abilityRow.getChildren().addAll(hiddenIcon, abilityLabel);
                abilitiesList.getChildren().add(abilityRow);
            } else {
                abilitiesList.getChildren().add(abilityLabel);
            }
        }

        card.getChildren().addAll(title, abilitiesList);
        return card;
    }

    /**
     * Crea la scheda informativa delle mosse
     */
    private static VBox createMovesCard(PokemonData pokemon, String accentColor) {
        VBox card = new VBox(15);
        card.getStyleClass().add("info-card");
        card.setPadding(new Insets(20));

        // Titolo sezione
        Label title = new Label("Mosse Principali");
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-text-fill: " + accentColor + ";");

        // Flow per le mosse
        FlowPane movesFlow = new FlowPane();
        movesFlow.setHgap(10);
        movesFlow.setVgap(10);

        for (String move : pokemon.getMoves()) {
            Label moveLabel = new Label(UIFactory.capitalize(move));
            moveLabel.getStyleClass().add("move-chip");
            moveLabel.setStyle("-fx-border-color: " + accentColor + "80;");

            // Aggiungi un effetto hover
            moveLabel.setOnMouseEntered(e ->
                    moveLabel.setStyle("-fx-border-color: " + accentColor + "; -fx-background-color: " + accentColor + "10;"));
            moveLabel.setOnMouseExited(e ->
                    moveLabel.setStyle("-fx-border-color: " + accentColor + "80;"));

            movesFlow.getChildren().add(moveLabel);
        }

        card.getChildren().addAll(title, movesFlow);
        return card;
    }
}