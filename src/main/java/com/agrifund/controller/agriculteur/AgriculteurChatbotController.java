package com.agrifund.controller.agriculteur;

import com.agrifund.Main;
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
 * Contrôleur Chatbot pour l'espace Agriculteur
 * Réutilise le ChatbotService existant
 */
public class AgriculteurChatbotController {

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
            lblStatus.setText("✅ En ligne - Propulsé par Gemini AI");
            lblStatus.setStyle("-fx-text-fill: #B2D944;");
        } catch (Exception e) {
            serviceAvailable = false;
            lblStatus.setText("⚠️ Hors ligne - Vérifiez la clé API");
            lblStatus.setStyle("-fx-text-fill: #ffb74d;");
            System.err.println("Chatbot init error: " + e.getMessage());
        }

        // Message de bienvenue
        addBotMessage(serviceAvailable
                ? chatbotService.getWelcomeMessage()
                : "⚠️ Le service IA n'est pas disponible.\n\nVérifiez que GEMINI_API_KEY est configurée dans votre fichier .env");

        // Auto-scroll
        chatContainer.heightProperty().addListener((obs, oldVal, newVal) ->
                scrollPane.setVvalue(1.0));

        // Enter pour envoyer
        txtMessage.setOnAction(e -> handleSendMessage());
    }

    @FXML
    private void handleSendMessage() {
        String message = txtMessage.getText().trim();
        if (message.isEmpty()) return;

        sendUserMessage(message);
    }

    private void sendUserMessage(String message) {
        if (!serviceAvailable) {
            addBotMessage("⚠️ Le service IA n'est pas disponible. Vérifiez votre clé API.");
            return;
        }

        // Ajouter le message utilisateur
        addUserMessage(message);
        txtMessage.clear();

        // Indicateur de typing
        typingIndicator.setVisible(true);
        typingIndicator.setManaged(true);
        btnSend.setDisable(true);
        lblStatus.setText("🔄 Réflexion en cours...");

        // Appel API en arrière-plan
        new Thread(() -> {
            String response = chatbotService.sendMessage(message);
            Platform.runLater(() -> {
                typingIndicator.setVisible(false);
                typingIndicator.setManaged(false);
                btnSend.setDisable(false);
                lblStatus.setText("✅ En ligne - Propulsé par Gemini AI");
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

    // ==================== SUGGESTIONS ====================

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
    private void handleSuggestionDemande() {
        txtMessage.setText("Comment faire une demande de financement sur AgriFund?");
        handleSendMessage();
    }

    @FXML
    private void handleSuggestionTaux() {
        txtMessage.setText("Quels sont les meilleurs taux d'intérêt disponibles?");
        handleSendMessage();
    }

    @FXML
    private void handleSuggestionConditions() {
        txtMessage.setText("Quelles sont les conditions pour obtenir un prêt agricole?");
        handleSendMessage();
    }

    // ==================== MESSAGE BUBBLES ====================

    private void addUserMessage(String message) {
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        wrapper.setPadding(new Insets(4, 0, 4, 80));

        VBox bubble = new VBox(4);
        bubble.setMaxWidth(480);
        bubble.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #B2D944, #8fbf30);" +
                        "-fx-background-radius: 18 18 4 18;" +
                        "-fx-padding: 12 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 2);");

        Label sender = new Label("Vous");
        sender.setStyle("-fx-text-fill: #064d2e; -fx-font-size: 10; -fx-font-weight: bold;");

        Text msgText = new Text(message);
        msgText.setStyle("-fx-fill: #064d2e; -fx-font-size: 13;");
        TextFlow textFlow = new TextFlow(msgText);
        textFlow.setMaxWidth(450);

        bubble.getChildren().addAll(sender, textFlow);
        wrapper.getChildren().add(bubble);
        chatContainer.getChildren().add(wrapper);
    }

    private void addBotMessage(String message) {
        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_LEFT);
        wrapper.setPadding(new Insets(4, 80, 4, 0));

        VBox bubble = new VBox(4);
        bubble.setMaxWidth(480);
        bubble.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #0d6b42, #0a5d3a);" +
                        "-fx-background-radius: 18 18 18 4;" +
                        "-fx-padding: 12 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);");

        Label sender = new Label("🤖 Assistant AgriFund");
        sender.setStyle("-fx-text-fill: #B2D944; -fx-font-size: 10; -fx-font-weight: bold;");

        Text msgText = new Text(message);
        msgText.setStyle("-fx-fill: #e8e8e8; -fx-font-size: 13;");
        TextFlow textFlow = new TextFlow(msgText);
        textFlow.setMaxWidth(450);

        bubble.getChildren().addAll(sender, textFlow);
        wrapper.getChildren().add(bubble);
        chatContainer.getChildren().add(wrapper);
    }

    // ==================== NAVIGATION ====================

    @FXML
    private void handleRetour() {
        Main.navigateTo("/com/agrifund/fxml/agriculteur/agriculteur-dashboard.fxml");
    }

    @FXML
    private void goToProduits() {
        Main.navigateTo("/com/agrifund/fxml/agriculteur/AgriculteurProduitView.fxml");
    }

    @FXML
    private void goToOffres() {
        Main.navigateTo("/com/agrifund/fxml/agriculteur/AgriculteurOffreView.fxml");
    }
}