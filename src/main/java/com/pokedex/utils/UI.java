package com.pokedex.utils;

import com.pokedex.pokemon.Pokemon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class UI {
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
            typeLabel.getStyleClass().addAll("type-label", StilePerTipoPokemon.getTypeClass(type));
            typesBox.getChildren().add(typeLabel);
        }

        // Pulsante dettagli più discreto
        Button detailsButton = new Button("Dettagli");
        detailsButton.getStyleClass().addAll("btn", "btn-light", "btn-sm");
        detailsButton.setPrefWidth(100);

        card.getChildren().addAll(numberBox, imageView, nameLabel, typesBox, detailsButton);

        // Applica stile in base al tipo primario
        StilePerTipoPokemon.applyTypeStyle(card, pokemon);

        return card;
    }

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

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static HBox createGenerationSelector(SelettoreGenerazione selector) {
        HBox container = new HBox();
        container.getStyleClass().add("generation-container");
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(5, 0, 10, 0));

        Label titleLabel = new Label("Select Gen:");
        titleLabel.getStyleClass().add("gen-title");

        container.getChildren().addAll(titleLabel, selector.getNode());

        return container;
    }

    public static VBox createNoResultsMessage() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50, 20, 50, 20));

        ImageView imageView = new ImageView(new Image(UI.class.getResourceAsStream("/images/not-found.png")));
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

    
    public static HBox createImprovedHeader(Button backButton) {
        HBox header = new HBox();
        header.setPadding(new Insets(15));
        header.setSpacing(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header");

        Label title = new Label("Pokédex");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.getStyleClass().add("clickable-title");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        if (backButton != null) {
            backButton.getStyleClass().addAll("btn", "btn-light");
        }

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        HBox searchArea = new HBox(5);
        searchArea.setAlignment(Pos.CENTER_RIGHT);

        TextField searchField = new TextField();
        searchField.setPromptText("Cerca Pokémon");
        searchField.getStyleClass().add("search-field");

        Button searchButton = new Button("Cerca");
        searchButton.getStyleClass().addAll("btn", "btn-success");

        searchField.setOnAction(e -> searchButton.fire());

        searchArea.getChildren().addAll(searchField, searchButton);

        if (backButton != null) {
            header.getChildren().addAll(title, spacer1, backButton, spacer2, searchArea);
        } else {
            header.getChildren().addAll(title, spacer1, spacer2, searchArea);
        }

        return header;
    }

    
}