package com.agrifund.controller;

import com.agrifund.services.ChatbotService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Controleur pour l'interface chatbot AgriFund. Gere les interactions
 * utilisateur et communique avec le service Gemini.
 */

public class ChatbotController {

    @FXML private VBox chatContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField txtMessage;
    @FXML private Button btnSend;
    @FXML private Button btnClearChat;
    @FXML private Label lblStatus;
    @FXML private HBox typingIndicator;
    @FXML private HBox suggestionsBox;

    private ChatbotService chatbotService;
    private boolean serviceAvailable = false;

    @FXML
    public void initialize() {
        try {
            chatbotService = new ChatbotService();
            serviceAvailable = true;
            lblStatus.setText("✅ En ligne - Propulse par Gemini AI");
        } catch (Exception e) {
            serviceAvailable = false;
            lblStatus.setText("⚠️ Hors ligne - Verifiez la cle API dans .env");
            System.err.println("Chatbot init error: " + e.getMessage());
        }

        // Show welcome message
        addBotMessage(serviceAvailable
                ? chatbotService.getWelcomeMessage()
                : "⚠️ Le service IA n'est pas disponible.\n\nVerifiez que GEMINI_API_KEY est configuree dans votre fichier .env");

        // Auto-scroll on new content
        chatContainer.heightProperty().addListener((obs, oldVal, newVal) ->
                scrollPane.setVvalue(1.0));
    }

    @FXML
    private void handleSendMessage() {
        String message = txtMessage.getText().trim();
        if (message.isEmpty()) return;

        sendUserMessage(message);
    }

    private void sendUserMessage(String message) {
        if (!serviceAvailable) {
            addBotMessage("⚠️ Le service IA n'est pas disponible. Verifiez votre cle API.");
            return;
        }

        // Add user message bubble
        addUserMessage(message);
        txtMessage.clear();

        // Show typing indicator
        typingIndicator.setVisible(true);
        typingIndicator.setManaged(true);
        btnSend.setDisable(true);
        lblStatus.setText("🔄 Reflexion en cours...");

        // Send to Gemini in background thread
        new Thread(() -> {
            String response = chatbotService.sendMessage(message);
            Platform.runLater(() -> {
                typingIndicator.setVisible(false);
                typingIndicator.setManaged(false);
                btnSend.setDisable(false);
                lblStatus.setText("✅ En ligne - Propulse par Gemini AI");
                addBotMessage(response);
            });
        }).start();
    }

    @FXML
    private void handleClearChat() {
        chatContainer.getChildren().clear();
        if (serviceAvailable) {
            chatbotService.clearHistory();
            addBotMessage(chatbotService.getWelcomeMessage());
        }
    }

    // ==================== SUGGESTION HANDLERS ====================

    @FXML
    private void handleSuggestionProduits() {
        txtMessage.setText("Quels sont les produits financiers disponibles pour les agriculteurs?");
        handleSendMessage();
    }

    @FXML
    private void handleSuggestionOffres() {
        txtMessage.setText("Quelles offres de financement sont actuellement disponibles?");
        handleSendMessage();
    }

    @FXML
    private void handleSuggestionConseils() {
        txtMessage.setText("Quels conseils pour un jeune agriculteur qui cherche un financement?");
        handleSendMessage();
    }

    @FXML
    private void handleSuggestionBureaux() {
        txtMessage.setText("Ou se trouvent les bureaux AgriFund en Tunisie?");
        handleSendMessage();
    }

    // ==================== MESSAGE BUBBLE BUILDERS ====================

    private void addUserMessage(String message) {
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        wrapper.setPadding(new Insets(3, 0, 3, 60));

        VBox bubble = new VBox(4);
        bubble.setMaxWidth(500);
        bubble.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #B2D944, #8fbf30);" +
                "-fx-background-radius: 15 15 3 15;" +
                "-fx-padding: 10 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);");

        Label sender = new Label("Vous");
        sender.setStyle("-fx-text-fill: #064d2e; -fx-font-size: 10; -fx-font-weight: bold;");

        Text msgText = new Text(message);
        msgText.setStyle("-fx-fill: #064d2e; -fx-font-size: 13;");
        TextFlow textFlow = new TextFlow(msgText);
        textFlow.setMaxWidth(470);

        bubble.getChildren().addAll(sender, textFlow);
        wrapper.getChildren().add(bubble);
        chatContainer.getChildren().add(wrapper);
    }

    private void addBotMessage(String message) {
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setPadding(new Insets(3, 60, 3, 0));

        VBox bubble = new VBox(4);
        bubble.setMaxWidth(500);
        bubble.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #0d6b42, #0a5d3a);" +
                "-fx-background-radius: 15 15 15 3;" +
                "-fx-padding: 10 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 2);");

        Label sender = new Label("🤖 Assistant AgriFund");
        sender.setStyle("-fx-text-fill: #B2D944; -fx-font-size: 10; -fx-font-weight: bold;");

        Text msgText = new Text(message);
        msgText.setStyle("-fx-fill: #e0e0e0; -fx-font-size: 13;");
        TextFlow textFlow = new TextFlow(msgText);
        textFlow.setMaxWidth(470);

        bubble.getChildren().addAll(sender, textFlow);
        wrapper.getChildren().add(bubble);
        chatContainer.getChildren().add(wrapper);
    }
}
