package com.pokedex.utils;

import com.pokedex.pokemon.PokemonData;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * Utilità per creare visualizzazioni alternative delle statistiche
 */
public class StatsChartUtils {

    /**
     * Crea un grafico a barre orizzontali per le statistiche
     * @param pokemon Il Pokémon di cui visualizzare le statistiche
     * @param accentColor Il colore da usare per le barre
     * @return Un grafico a barre con le statistiche
     */
    public static BarChart<Number, String> createHorizontalBarChart(PokemonData pokemon, String accentColor) {
        // Assi del grafico
        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();

        // Creazione del grafico
        final BarChart<Number, String> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Statistiche di Base");
        barChart.setLegendVisible(false);

        // Serie di dati
        XYChart.Series<Number, String> series = new XYChart.Series<>();

        // Traduci i nomi delle statistiche
        Map<String, String> statNames = Map.of(
                "hp", "HP",
                "attack", "Attacco",
                "defense", "Difesa",
                "special-attack", "Att. Speciale",
                "special-defense", "Dif. Speciale",
                "speed", "Velocità"
        );

        // Aggiungi i dati
        for (Map.Entry<String, Integer> entry : pokemon.getStats().entrySet()) {
            String statName = statNames.getOrDefault(entry.getKey(), UIFactory.capitalize(entry.getKey()));
            int value = entry.getValue();

            series.getData().add(new XYChart.Data<>(value, statName));
        }

        // Aggiungi la serie al grafico
        barChart.getData().add(series);

        // Personalizza le barre
        for (XYChart.Data<Number, String> data : series.getData()) {
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    // Imposta il colore delle barre
                    newNode.setStyle("-fx-bar-fill: " + accentColor + ";");

                    // Aggiungi l'etichetta del valore
                    String text = String.valueOf(data.getXValue());
                    javafx.scene.control.Label label = new javafx.scene.control.Label(text);
                    label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");

                    // Posiziona l'etichetta all'interno della barra
                    data.getNode().parentProperty().addListener((obs2, old, newParent) -> {
                        if (newParent != null) {
                            javafx.scene.Group group = (javafx.scene.Group) newParent;
                            group.getChildren().add(label);

                            // Posiziona l'etichetta al centro della barra
                            label.translateXProperty().bind(
                                    data.getNode().translateXProperty().add(
                                            data.getNode().getBoundsInParent().getWidth() - label.getBoundsInParent().getWidth() - 10
                                    )
                            );
                            label.translateYProperty().bind(
                                    data.getNode().translateYProperty().add(
                                            data.getNode().getBoundsInParent().getHeight() / 2 - label.getBoundsInParent().getHeight() / 2
                                    )
                            );
                        }
                    });
                }
            });
        }

        // Ottimizza l'aspetto del grafico
        xAxis.setLabel("Valore");
        xAxis.setTickLabelRotation(0);
        xAxis.setMinorTickVisible(false);
        xAxis.setTickMarkVisible(true);
        xAxis.setTickLabelGap(10);

        yAxis.setLabel("");
        yAxis.setTickLabelGap(10);

        barChart.setHorizontalGridLinesVisible(true);
        barChart.setVerticalGridLinesVisible(false);
        barChart.setBarGap(8);
        barChart.setCategoryGap(10);
        barChart.setAnimated(true);

        return barChart;
    }

    /**
     * Crea un grafico a radar per le statistiche (simulato con segmenti di linea)
     * Nota: Questo è un esempio avanzato che richiederebbe più tempo per l'implementazione completa
     * e non è incluso nella versione attuale.
     */
    public static VBox createRadarChart(PokemonData pokemon, String accentColor) {
        // Implementazione del grafico radar (più complessa)
        // Questa funzione è un placeholder per una futura implementazione

        VBox placeholder = new VBox();
        javafx.scene.control.Label label = new javafx.scene.control.Label("Grafico radar non disponibile");
        placeholder.getChildren().add(label);

        return placeholder;
    }
}