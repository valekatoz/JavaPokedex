package com.pokedex.utils;

import com.pokedex.pokemon.Pokemon;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Factory per la creazione di componenti UI riutilizzabili
 */
public class UIFactory {
    // Mappa dei colori per i tipi di Pokémon
    private static final Map<String, String> TYPE_COLORS = new HashMap<>();

    static {
        // Inizializza i colori per i tipi
        TYPE_COLORS.put("normal", "#A8A878");
        TYPE_COLORS.put("fire", "#F08030");
        TYPE_COLORS.put("water", "#6890F0");
        TYPE_COLORS.put("grass", "#78C850");
        TYPE_COLORS.put("electric", "#F8D030");
        TYPE_COLORS.put("ice", "#98D8D8");
        TYPE_COLORS.put("fighting", "#C03028");
        TYPE_COLORS.put("poison", "#A040A0");
        TYPE_COLORS.put("ground", "#E0C068");
        TYPE_COLORS.put("flying", "#A890F0");
        TYPE_COLORS.put("psychic", "#F85888");
        TYPE_COLORS.put("bug", "#A8B820");
        TYPE_COLORS.put("rock", "#B8A038");
        TYPE_COLORS.put("ghost", "#705898");
        TYPE_COLORS.put("dragon", "#7038F8");
        TYPE_COLORS.put("dark", "#705848");
        TYPE_COLORS.put("steel", "#B8B8D0");
        TYPE_COLORS.put("fairy", "#EE99AC");
    }

    /**
     * Crea una card per un Pokémon
     */
    public static VBox createPokemonCard(Pokemon pokemon) {
        VBox card = new VBox(10);
        card.getStyleClass().add("pokemon-card");
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(160, 220);

        // Immagine
        ImageView imageView = new ImageView(new Image(pokemon.getImageUrl(),
                true /* supporto per caricamento asincrono */));
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        // Placeholder immagine mentre carica
        /*imageView.setOnError(e -> {
            // Sostituisci con un'immagine di fallback o un placeholder
            imageView.setImage(new Image(UIFactory.class.getResourceAsStream("/images/placeholder.png")));
        });*/

        // Nome Pokémon
        Label nameLabel = new Label("#" + pokemon.getId() + " " + capitalize(pokemon.getName()));
        nameLabel.getStyleClass().add("pokemon-name");

        // Tipi
        HBox typesBox = new HBox(5);
        typesBox.setAlignment(Pos.CENTER);

        for (String type : pokemon.getTypes()) {
            Label typeLabel = new Label(capitalize(type));
            typeLabel.getStyleClass().add("type-label");
            typeLabel.setStyle("-fx-background-color: " + TYPE_COLORS.getOrDefault(type, "#AAAAAA") + ";");
            typesBox.getChildren().add(typeLabel);
        }

        card.getChildren().addAll(imageView, nameLabel, typesBox);

        return card;
    }

    /**
     * Crea una vista dettagliata completa per un Pokémon
     */
    public static ScrollPane createPokemonDetailView(PokemonData pokemon, Runnable onClose) {
        // Container principale
        VBox detailContainer = new VBox(20);
        detailContainer.setPadding(new Insets(20));
        detailContainer.getStyleClass().add("pokemon-detail-container");

        // Header con pulsante di chiusura
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("#" + pokemon.getId() + " " + capitalize(pokemon.getName()));
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
        ImageView imageView = new ImageView(new Image(pokemon.getImageUrl(), true));
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        // Informazioni di base
        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        // Tipi
        HBox typesBox = new HBox(10);
        typesBox.setAlignment(Pos.CENTER_LEFT);

        Label typesLabel = new Label("Tipi:");
        typesLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        typesBox.getChildren().add(typesLabel);

        for (String type : pokemon.getTypes()) {
            Label typeLabel = new Label(capitalize(type));
            typeLabel.getStyleClass().add("type-label");
            typeLabel.setStyle("-fx-background-color: " + TYPE_COLORS.getOrDefault(type, "#AAAAAA") + ";");
            typesBox.getChildren().add(typeLabel);
        }

        // Altezza e peso
        Label heightLabel = new Label("Altezza: " + pokemon.getHeightInMeters() + " m");
        Label weightLabel = new Label("Peso: " + pokemon.getWeightInKg() + " kg");

        // Specie
        Label speciesLabel = new Label("Specie: " + capitalize(pokemon.getSpecies()));

        infoBox.getChildren().addAll(typesBox, heightLabel, weightLabel, speciesLabel);

        basicInfo.getChildren().addAll(imageView, infoBox);

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
        statsChart.setTitle("Statistiche");
        statsChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (Map.Entry<String, Integer> stat : pokemon.getStats().entrySet()) {
            series.getData().add(new XYChart.Data<>(capitalize(stat.getKey()), stat.getValue()));
        }

        statsChart.getData().add(series);

        // Colora le barre in base ai valori
        for (XYChart.Data<String, Number> data : series.getData()) {
            int value = data.getYValue().intValue();
            String color;

            if (value < 50) {
                color = "#FF7675"; // Rosso chiaro
            } else if (value < 80) {
                color = "#FDCB6E"; // Giallo
            } else if (value < 100) {
                color = "#74B9FF"; // Blu chiaro
            } else {
                color = "#55EFC4"; // Verde acqua
            }

            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: " + color + ";");
                }
            });
        }

        statsContainer.getChildren().addAll(statsTitle, statsChart);

        // Separatore
        Separator separator2 = new Separator();

        // Abilità
        VBox abilitiesContainer = new VBox(10);
        Label abilitiesTitle = new Label("Abilità");
        abilitiesTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

        GridPane abilitiesGrid = new GridPane();
        abilitiesGrid.setHgap(10);
        abilitiesGrid.setVgap(5);

        for (int i = 0; i < pokemon.getAbilities().size(); i++) {
            Label abilityLabel = new Label(capitalize(pokemon.getAbilities().get(i)));
            abilityLabel.getStyleClass().add("ability-label");
            abilitiesGrid.add(abilityLabel, 0, i);
        }

        abilitiesContainer.getChildren().addAll(abilitiesTitle, abilitiesGrid);

        // Separatore
        Separator separator3 = new Separator();

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
            moveLabel.setStyle("-fx-background-color: #E0E0E0; -fx-background-radius: 15;");
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
                abilitiesContainer,
                separator3,
                movesContainer
        );

        // Crea e configura ScrollPane
        ScrollPane scrollPane = new ScrollPane(detailContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("detail-scroll-pane");

        return scrollPane;
    }

    /**
     * Crea un placeholder per una card durante il caricamento
     */
    public static VBox createPlaceholder() {
        VBox placeholder = new VBox(10);
        placeholder.getStyleClass().add("placeholder");
        placeholder.setPrefSize(160, 200);

        Region imageArea = new Region();
        imageArea.setPrefSize(120, 120);
        imageArea.getStyleClass().add("placeholder-image");

        Region textLine1 = new Region();
        textLine1.setPrefSize(100, 15);
        textLine1.getStyleClass().add("placeholder-text");

        Region textLine2 = new Region();
        textLine2.setPrefSize(80, 15);
        textLine2.getStyleClass().add("placeholder-text");

        placeholder.setAlignment(Pos.CENTER);
        placeholder.getChildren().addAll(imageArea, textLine1, textLine2);

        return placeholder;
    }

    /**
     * Crea un messaggio di errore
     */
    public static Label createErrorMessage(String message) {
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("error-message");
        return errorLabel;
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
     * Restituisce il colore per un tipo di Pokémon
     */
    public static String getTypeColor(String type) {
        return TYPE_COLORS.getOrDefault(type.toLowerCase(), "#AAAAAA");
    }
}