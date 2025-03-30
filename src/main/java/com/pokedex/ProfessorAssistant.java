package com.pokedex;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfessorAssistant {

    private final String apiKey;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Stage chatStage;
    private final TextArea chatArea;
    private final TextField inputField;
    private final VBox messagesContainer;
    private final ScrollPane scrollPane;
    private final List<String> funFacts;
    private final Random random = new Random();
    private boolean isProcessing = false;

    private final String professorName = "Professor Lavandonia";
    private final String professorImagePath = "/images/professor.png";
    private final String professorPrompt = "Sei il Professor Lavandonia, esperto di Pokémon e famoso ricercatore. " +
            "Rispondi come se fossi un professore Pokémon entusiasta che aiuta gli allenatori con consigli e informazioni. " +
            "Parla in modo educato ma amichevole, con termini da mondo Pokémon. " +
            "Se non conosci la risposta a una domanda specifica sui Pokémon, indirizza l'allenatore al Pokédex " +
            "per ulteriori informazioni. Occasionalmente, menziona la tua ricerca sui Pokémon leggendari.";

    public ProfessorAssistant(String apiKey) {
        this.apiKey = apiKey;
        this.funFacts = initializeFunFacts();

        chatStage = new Stage();
        chatStage.setTitle("Chat con " + professorName);
        chatStage.initModality(Modality.NONE);
        chatStage.initStyle(StageStyle.DECORATED);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("chat-root");

        HBox header = createHeader();
        root.setTop(header);

        messagesContainer = new VBox(10);
        messagesContainer.setPadding(new Insets(15));
        messagesContainer.getStyleClass().add("messages-container");

        scrollPane = new ScrollPane(messagesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("chat-scroll-pane");

        HBox inputArea = new HBox(10);
        inputArea.setPadding(new Insets(10));
        inputArea.setAlignment(Pos.CENTER);
        inputArea.getStyleClass().add("chat-input-area");

        inputField = new TextField();
        inputField.setPromptText("Fai una domanda al " + professorName + "...");
        inputField.getStyleClass().add("chat-input");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        Button sendButton = new Button("Invia");
        sendButton.getStyleClass().addAll("btn", "btn-primary", "chat-send-button");
        sendButton.disableProperty().bind(
                Bindings.isEmpty(inputField.textProperty())
                        .or(Bindings.createBooleanBinding(() -> isProcessing))
        );

        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendButton.fire());

        inputArea.getChildren().addAll(inputField, sendButton);

        HBox extrasArea = createExtrasButtons();

        VBox bottomArea = new VBox(10);
        bottomArea.getChildren().addAll(extrasArea, inputArea);
        bottomArea.getStyleClass().add("chat-bottom-area");

        root.setCenter(scrollPane);
        root.setBottom(bottomArea);

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.getStyleClass().add("chat-display");
        chatArea.setVisible(false);

        Scene scene = new Scene(root, 420, 600);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        scene.getStylesheets().add(ProfessorAssistant.class.getResource("/chat-styles.css").toExternalForm());

        chatStage.setScene(scene);

        addAssistantMessage("Ciao! Sono il " + professorName + ", esperto di Pokémon e ricercatore. Come posso aiutarti oggi nel tuo viaggio nel mondo dei Pokémon?");
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(10));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("chat-header");

        ImageView professorImageView = new ImageView(new Image(
                getClass().getResourceAsStream(professorImagePath)));
        professorImageView.setFitHeight(50);
        professorImageView.setFitWidth(50);
        professorImageView.setPreserveRatio(true);
        professorImageView.getStyleClass().add("professor-image");

        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(professorName);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.getStyleClass().add("professor-name");

        Label statusLabel = new Label("Esperto di Pokémon");
        statusLabel.setFont(Font.font("System", 12));
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.getStyleClass().add("professor-status");

        nameBox.getChildren().addAll(nameLabel, statusLabel);

        header.getChildren().addAll(professorImageView, nameBox);

        return header;
    }

    private HBox createExtrasButtons() {
        HBox extrasBox = new HBox(10);
        extrasBox.setPadding(new Insets(0, 10, 0, 10));
        extrasBox.setAlignment(Pos.CENTER);
        extrasBox.getStyleClass().add("extras-box");

        Button funFactButton = new Button("Fatto Divertente");
        funFactButton.getStyleClass().addAll("btn", "btn-info", "btn-sm", "extras-button");
        funFactButton.setOnAction(e -> showRandomFunFact());

        Button pokemonOfDayButton = new Button("Pokémon del Giorno");
        pokemonOfDayButton.getStyleClass().addAll("btn", "btn-success", "btn-sm", "extras-button");
        pokemonOfDayButton.setOnAction(e -> showPokemonOfTheDay());

        Button tipButton = new Button("Consiglio per Allenatori");
        tipButton.getStyleClass().addAll("btn", "btn-warning", "btn-sm", "extras-button");
        tipButton.setOnAction(e -> showTrainerTip());

        extrasBox.getChildren().addAll(funFactButton, pokemonOfDayButton, tipButton);

        return extrasBox;
    }

    public void show() {
        chatStage.show();
        chatStage.setX((Screen.getPrimary().getVisualBounds().getWidth() - chatStage.getWidth()) / 2);
        chatStage.setY((Screen.getPrimary().getVisualBounds().getHeight() - chatStage.getHeight()) / 2);
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty() || isProcessing) return;

        isProcessing = true;

        addUserMessage(message);

        inputField.clear();

        CompletableFuture.supplyAsync(() -> {
            try {
                return callChatGptApi(message);
            } catch (Exception e) {
                return "Mi dispiace, sto avendo problemi con il mio Rotom Phone. Puoi riprovare tra poco? (Errore: " + e.getMessage() + ")";
            }
        }, executorService).thenAccept(response -> {
            Platform.runLater(() -> {
                addAssistantMessage(response);
                isProcessing = false;
            });
        });
    }

    private void addUserMessage(String message) {
        chatArea.appendText("Tu: " + message + "\n\n");

        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));
        messageBox.getStyleClass().add("user-message-box");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(250);
        messageLabel.setPadding(new Insets(10));
        messageLabel.getStyleClass().add("user-message");

        messageBox.getChildren().add(messageLabel);

        messagesContainer.getChildren().add(messageBox);

        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    private void addAssistantMessage(String message) {
        chatArea.appendText(professorName + ": " + message + "\n\n");

        HBox messageBox = new HBox(10);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));
        messageBox.getStyleClass().add("assistant-message-box");

        ImageView miniProfessorView = new ImageView(new Image(
                getClass().getResourceAsStream(professorImagePath)));
        miniProfessorView.setFitHeight(30);
        miniProfessorView.setFitWidth(30);
        miniProfessorView.setPreserveRatio(true);
        miniProfessorView.getStyleClass().add("mini-professor-image");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(250);
        messageLabel.setPadding(new Insets(10));
        messageLabel.getStyleClass().add("assistant-message");

        messageBox.getChildren().addAll(miniProfessorView, messageLabel);

        messagesContainer.getChildren().add(messageBox);

        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    private String callChatGptApi(String message) throws IOException {
        URL url = new URL("https://api.openai.com/v1/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);

        JSONObject payload = new JSONObject();
        payload.put("model", "gpt-3.5-turbo");

        JSONArray messages = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", professorPrompt);
        messages.put(systemMessage);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messages.put(userMessage);

        payload.put("messages", messages);
        payload.put("max_tokens", 300);
        payload.put("temperature", 0.7);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject choiceObj = choices.getJSONObject(0);
                JSONObject messageObj = choiceObj.getJSONObject("message");
                return messageObj.getString("content").trim();
            }
        }

        return "Mi dispiace, non sono riuscito a comprendere la tua domanda. Puoi riprovare?";
    }

    private void showRandomFunFact() {
        String funFact = funFacts.get(random.nextInt(funFacts.size()));
        addAssistantMessage("📚 Fatto divertente: " + funFact);
    }

    private void showPokemonOfTheDay() {
        int pokemonId = 1 + random.nextInt(898);

        addAssistantMessage("✨ Il Pokémon del giorno è il #" + pokemonId + "! " +
                "Dai un'occhiata nel Pokédex per scoprire di più su questo fantastico Pokémon!");
    }

    private void showTrainerTip() {
        String[] tips = {
                "Ricorda di portare sempre con te delle Pozioni! La salute dei tuoi Pokémon è la priorità.",
                "I Pokémon di tipo Acqua sono efficaci contro i tipi Fuoco, Terra e Roccia.",
                "Catturare Pokémon con diversi tipi ti darà vantaggi strategici nelle battaglie.",
                "Non dimenticare di visitare regolarmente i Centri Pokémon per curare la tua squadra.",
                "I tipi Elettro sono immuni ai danni Elettro e sono efficaci contro tipi Acqua e Volante.",
                "Gli oggetti tenuti dai Pokémon possono influenzare notevolmente le loro prestazioni in battaglia.",
                "Assicurati di esplorare tutte le aree: alcuni Pokémon rari si trovano solo in luoghi specifici.",
                "Allenare Pokémon con personalità diverse può essere vantaggioso per battaglie diverse.",
                "Le bacche possono essere molto utili durante le battaglie difficili.",
                "Ricorda che i legami che crei con i tuoi Pokémon sono importanti quanto la loro forza!"
        };

        String tip = tips[random.nextInt(tips.length)];
        addAssistantMessage("💡 Consiglio per allenatori: " + tip);
    }

    private List<String> initializeFunFacts() {
        List<String> facts = new ArrayList<>();

        facts.add("Rhydon è stato il primo Pokémon mai creato!");
        facts.add("Il nome 'Pokémon' è l'abbreviazione di 'Pocket Monsters'.");
        facts.add("Pikachu prende il nome dalla combinazione di 'pika' (scintilla) e 'chu' (squittio).");
        facts.add("Arbok al contrario è 'kobra', un riferimento al cobra.");
        facts.add("Il Pokémon più pesante è Cosmoem, che pesa 999,9 kg!");
        facts.add("Magikarp può saltare montagne, secondo il Pokédex.");
        facts.add("Arceus è considerato il creatore dell'universo Pokémon.");
        facts.add("Slowpoke può rigenerare la sua coda se viene tagliata.");
        facts.add("Wobbuffet mantiene la maggior parte del suo corpo nascosto; solo la sua coda blu è visibile.");
        facts.add("La temperatura del corpo di Charizard può raggiungere i 1.000 gradi Celsius.");
        facts.add("Cubone indossa il teschio della sua madre defunta.");
        facts.add("Ditto è l'unico Pokémon che può accoppiarsi con quasi tutti gli altri Pokémon.");
        facts.add("Meowth è stato il primo Pokémon a parlare nell'anime.");
        facts.add("Psyduck ha costantemente il mal di testa a causa dei suoi poteri psichici.");
        facts.add("Eevee ha il maggior numero di evoluzioni diverse di qualsiasi Pokémon.");

        return facts;
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public Button createChatButton() {
        Button chatButton = new Button();
        chatButton.getStyleClass().add("professor-chat-button");

        ImageView buttonImage = new ImageView(new Image(
                getClass().getResourceAsStream(professorImagePath)));
        buttonImage.setFitHeight(40);
        buttonImage.setFitWidth(40);
        buttonImage.setPreserveRatio(true);

        chatButton.setGraphic(buttonImage);

        Tooltip tooltip = new Tooltip("Chiedi al " + professorName);
        Tooltip.install(chatButton, tooltip);

        chatButton.setOnAction(e -> show());

        return chatButton;
    }

    // Aggiunto import mancante per Screen
    private static class Screen {
        public static Screen getPrimary() {
            return new Screen();
        }

        public javafx.geometry.Rectangle2D getVisualBounds() {
            return javafx.stage.Screen.getPrimary().getVisualBounds();
        }
    }
}