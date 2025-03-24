module com.valekatoz.pokedex {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires org.json;

    opens com.pokedex to javafx.fxml;
    exports com.pokedex;
    exports com.pokedex.utils;
    opens com.pokedex.utils to javafx.fxml;
    exports com.pokedex.pokemon;
    opens com.pokedex.pokemon to javafx.fxml;
}