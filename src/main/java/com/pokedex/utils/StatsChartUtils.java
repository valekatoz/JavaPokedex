package com.pokedex.utils;

import com.pokedex.pokemon.PokemonData;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.Map;

public class StatsChartUtils {
    public static BarChart<Number, String> createHorizontalBarChart(PokemonData pokemon, String accentColor) {
        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();

        final BarChart<Number, String> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Statistiche di Base");
        barChart.setLegendVisible(false);
        XYChart.Series<Number, String> series = new XYChart.Series<>();

        Map<String, String> statNames = Map.of(
                "hp", "HP",
                "attack", "Attacco",
                "defense", "Difesa",
                "special-attack", "Att. Speciale",
                "special-defense", "Dif. Speciale",
                "speed", "Velocità"
        );

        for (Map.Entry<String, Integer> entry : pokemon.getStats().entrySet()) {
            String statName = statNames.getOrDefault(entry.getKey(), UI.capitalize(entry.getKey()));
            int value = entry.getValue();

            series.getData().add(new XYChart.Data<>(value, statName));
        }

        barChart.getData().add(series);
        for (XYChart.Data<Number, String> data : series.getData()) {
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: " + accentColor + ";");

                    String text = String.valueOf(data.getXValue());
                    javafx.scene.control.Label label = new javafx.scene.control.Label(text);
                    label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");

                    data.getNode().parentProperty().addListener((obs2, old, newParent) -> {
                        if (newParent != null) {
                            javafx.scene.Group group = (javafx.scene.Group) newParent;
                            group.getChildren().add(label);

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
}