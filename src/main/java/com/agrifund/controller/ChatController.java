package com.agrifund.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatController {

    @FXML
    ScrollPane scrollPane;
    @FXML
    VBox messagesContainer;
    @FXML
    TextField tfChatMessage;
    @FXML
    Button btnChatSend;
    @FXML
    Button btnCloseChat;
    @FXML
    Label lblStatusChat;

    // ══════════════════════════════════════════════════════════════
    // ✅ NOUVELLE API HUGGINGFACE ROUTER
    // ══════════════════════════════════════════════════════════════

    private static final String HF_API_KEY = System.getenv("HF_API_KEY") != null
           ? System.getenv("HF_API_KEY")
            : "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";

    // ✅ Nouvelle URL du router
    private static final String HF_API_URL =
            "https://router.huggingface.co/v1/chat/completions";

    // ✅ Modèles compatibles avec le router (format OpenAI)
    private static final String[] HF_MODELS = {
            "meta-llama/Llama-3.3-70B-Instruct",
            "meta-llama/Llama-3.2-3B-Instruct",
            "Qwen/Qwen2.5-72B-Instruct",
            "mistralai/Mistral-7B-Instruct-v0.3",
            "microsoft/Phi-3.5-mini-instruct"
    };

    private static final String SYSTEM_CONTEXT =
            "Tu es un assistant expert en agriculture et gestion des risques agricoles. " +
                    "Réponds TOUJOURS en français, de manière concise (3-5 phrases maximum). " +
                    "Tu aides les banques à évaluer les risques des projets agricoles. " +
                    "Facteurs clés: température, humidité, pH du sol.";

    private final List<String[]> conversationHistory = new ArrayList<>();
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
    private Runnable onCloseCallback;

    // ══════════════════════════════════════════════════════════════
    // Initialisation
    // ══════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        System.out.println("[ChatController] Initialisation...");

        if (messagesContainer == null || scrollPane == null) {
            System.err.println("[ChatController] ❌ Éléments FXML non chargés!");
            return;
        }

        if (btnChatSend != null) btnChatSend.setOnAction(e -> handleSendMessage());
        if (tfChatMessage != null) tfChatMessage.setOnAction(e -> handleSendMessage());
        if (btnCloseChat != null) btnCloseChat.setOnAction(e -> handleClose());

        Platform.runLater(() -> {
            addBotMessage("👋 Bonjour ! Je suis votre assistant agricole.\n\n" +
                    "Posez-moi vos questions sur l'évaluation des risques " +
                    "agricoles pour le financement bancaire.");
            if (tfChatMessage != null) tfChatMessage.requestFocus();
        });

        System.out.println("[ChatController] ✅ Initialisé");
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    // ══════════════════════════════════════════════════════════════
    // Gestion des messages
    // ══════════════════════════════════════════════════════════════

    private void handleSendMessage() {
        if (tfChatMessage == null) return;

        String userText = tfChatMessage.getText().trim();
        if (userText.isEmpty()) return;

        addUserMessage(userText);
        tfChatMessage.clear();

        setInputEnabled(false);
        setStatus("⏳ En train d'écrire...", "#FFA726");

        conversationHistory.add(new String[]{"user", userText});

        Thread apiThread = new Thread(() -> {
            String response = callAPIWithFallback(userText);

            boolean success = response != null && !response.startsWith("⚠️");

            if (success) {
                conversationHistory.add(new String[]{"assistant", response});
            } else {
                if (!conversationHistory.isEmpty()) {
                    conversationHistory.remove(conversationHistory.size() - 1);
                }
                if (response == null) {
                    response = "⚠️ Impossible de contacter le serveur. Vérifiez votre connexion.";
                }
            }

            final String finalResponse = response;
            Platform.runLater(() -> {
                addBotMessage(finalResponse);
                setInputEnabled(true);
                setStatus("● En ligne", "#4CAF50");
                if (tfChatMessage != null) tfChatMessage.requestFocus();
            });
        });
        apiThread.setDaemon(true);
        apiThread.start();
    }

    private void handleClose() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        } else if (btnCloseChat != null && btnCloseChat.getScene() != null) {
            Stage stage = (Stage) btnCloseChat.getScene().getWindow();
            if (stage != null) stage.hide();
        }
    }

    // ══════════════════════════════════════════════════════════════
    // ✅ APPEL API AVEC NOUVEAU FORMAT OPENAI-COMPATIBLE
    // ══════════════════════════════════════════════════════════════

    private String callAPIWithFallback(String userMessage) {
        for (String model : HF_MODELS) {
            System.out.println("[Chat] Tentative avec: " + model);
            try {
                String result = callChatCompletionsAPI(userMessage, model);
                if (result != null && !result.isBlank()) {
                    System.out.println("[Chat] ✅ Réponse de: " + model);
                    return result;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "⚠️ Requête interrompue.";
            } catch (Exception ex) {
                System.err.println("[Chat] ❌ Échec [" + model + "]: " + ex.getMessage());
            }
        }
        return "⚠️ Tous les modèles sont indisponibles. Réessayez dans quelques secondes.";
    }

    /**
     * ✅ Nouvelle méthode utilisant le format OpenAI Chat Completions
     */
    private String callChatCompletionsAPI(String userMessage, String model)
            throws IOException, InterruptedException {

        URL url = new URL(HF_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + HF_API_KEY);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setDoOutput(true);
        conn.setConnectTimeout(20_000);
        conn.setReadTimeout(40_000);

        // ✅ Construire les messages au format OpenAI
        StringBuilder messages = new StringBuilder();
        messages.append("[");

        // Message système
        messages.append("{\"role\":\"system\",\"content\":")
                .append(toJsonString(SYSTEM_CONTEXT)).append("}");

        // Historique récent (6 derniers messages max)
        int start = Math.max(0, conversationHistory.size() - 6);
        for (int i = start; i < conversationHistory.size(); i++) {
            String[] entry = conversationHistory.get(i);
            messages.append(",{\"role\":\"").append(entry[0])
                    .append("\",\"content\":").append(toJsonString(entry[1])).append("}");
        }

        messages.append("]");

        // ✅ Corps de la requête au format OpenAI
        String body = "{"
                + "\"model\":" + toJsonString(model) + ","
                + "\"messages\":" + messages + ","
                + "\"max_tokens\":300,"
                + "\"temperature\":0.7,"
                + "\"stream\":false"
                + "}";

        System.out.println("[Chat] Envoi vers " + model);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int statusCode = conn.getResponseCode();
        System.out.println("[Chat] HTTP " + statusCode);

        InputStream is = (statusCode >= 200 && statusCode < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        String responseBody = readStream(is);

        if (statusCode == 503) {
            System.out.println("[Chat] Modèle en chargement...");
            Thread.sleep(2000);
            return null;
        }

        if (statusCode == 401 || statusCode == 403) {
            System.err.println("[Chat] Authentification échouée");
            return "⚠️ Clé API invalide. Vérifiez votre configuration.";
        }

        if (statusCode == 429) {
            System.out.println("[Chat] Rate limit, pause...");
            Thread.sleep(3000);
            return null;
        }

        if (statusCode != 200) {
            System.err.println("[Chat] Erreur " + statusCode + ": "
                    + responseBody.substring(0, Math.min(500, responseBody.length())));
            return null;
        }

        return extractContentFromChatResponse(responseBody);
    }

    /**
     * ✅ Extraction du contenu depuis la réponse OpenAI format
     * Format: {"choices":[{"message":{"role":"assistant","content":"..."}}]}
     */
    private String extractContentFromChatResponse(String json) {
        try {
            // Chercher "choices" puis "content"
            int choicesIdx = json.indexOf("\"choices\"");
            if (choicesIdx == -1) {
                System.err.println("[Chat] Pas de 'choices' dans la réponse");
                return null;
            }

            int contentIdx = json.indexOf("\"content\"", choicesIdx);
            if (contentIdx == -1) {
                System.err.println("[Chat] Pas de 'content' dans la réponse");
                return null;
            }

            // Trouver la valeur du content
            int colonIdx = json.indexOf(':', contentIdx + 9);
            if (colonIdx == -1) return null;

            int i = colonIdx + 1;
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;

            if (i >= json.length() || json.charAt(i) != '"') {
                System.err.println("[Chat] Format inattendu");
                return null;
            }

            // Parser la chaîne JSON
            i++;
            StringBuilder content = new StringBuilder();
            boolean escaped = false;

            while (i < json.length()) {
                char c = json.charAt(i);
                if (escaped) {
                    switch (c) {
                        case 'n': content.append('\n'); break;
                        case 't': content.append('\t'); break;
                        case 'r': content.append('\r'); break;
                        case '"': content.append('"'); break;
                        case '\\': content.append('\\'); break;
                        default: content.append(c);
                    }
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    break;
                } else {
                    content.append(c);
                }
                i++;
            }

            String result = content.toString().trim();

            if (result.isEmpty()) {
                System.err.println("[Chat] Réponse vide");
                return null;
            }

            System.out.println("[Chat] Réponse extraite (" + result.length() + " chars)");
            return result;

        } catch (Exception e) {
            System.err.println("[Chat] Erreur parsing: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private String toJsonString(String text) {
        if (text == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    // ══════════════════════════════════════════════════════════════
    // UI - Bulles de message
    // ══════════════════════════════════════════════════════════════

    private void addUserMessage(String text) {
        if (messagesContainer == null) return;

        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(230);
        bubble.getStyleClass().add("bubble-user");

        Label time = new Label(LocalTime.now().format(timeFmt));
        time.getStyleClass().add("bubble-time");

        VBox wrapper = new VBox(3, bubble, time);
        wrapper.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(wrapper);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setStyle("-fx-padding: 0 0 0 50;");

        messagesContainer.getChildren().add(row);
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        if (messagesContainer == null) return;

        Label avatar = new Label("🌿");
        avatar.setStyle("-fx-font-size: 18px; -fx-padding: 3 0 0 0;");

        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(250);
        bubble.getStyleClass().add("bubble-bot");

        Label time = new Label(LocalTime.now().format(timeFmt));
        time.getStyleClass().add("bubble-time");

        VBox textWrapper = new VBox(3, bubble, time);
        textWrapper.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(8, avatar, textWrapper);
        row.setAlignment(Pos.TOP_LEFT);
        row.setStyle("-fx-padding: 0 50 0 0;");

        messagesContainer.getChildren().add(row);
        scrollToBottom();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (scrollPane != null) {
                scrollPane.applyCss();
                scrollPane.layout();
                scrollPane.setVvalue(1.0);
            }
        });
    }

    private void setInputEnabled(boolean enabled) {
        if (btnChatSend != null) btnChatSend.setDisable(!enabled);
        if (tfChatMessage != null) tfChatMessage.setDisable(!enabled);
    }

    private void setStatus(String text, String color) {
        if (lblStatusChat != null) {
            lblStatusChat.setText(text);
            lblStatusChat.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px;");
        }
    }
}