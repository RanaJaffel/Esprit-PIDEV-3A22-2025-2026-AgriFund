package controllers;

import entities.EvaluationRisque;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import services.PdfExportService;
import services.ServiceEvaluationRisque;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import controllers.ChatController;

public class RisqueListController {

    // ── FXML - Table & colonnes ───────────────────────────────────────────────
    @FXML private TableView<EvaluationRisque> tableEvaluations;
    @FXML private TableColumn<EvaluationRisque, Integer> colId;
    @FXML private TableColumn<EvaluationRisque, Integer> colIdProjet;
    @FXML private TableColumn<EvaluationRisque, Integer> colScoreGlobal;
    @FXML private TableColumn<EvaluationRisque, String>  colNiveauRisque;
    @FXML private TableColumn<EvaluationRisque, String>  colFiabiliteDonnees;
    @FXML private TableColumn<EvaluationRisque, String>  colFacteurPrincipal;
    @FXML private TableColumn<EvaluationRisque, String>  colRecommandation;
    @FXML private TableColumn<EvaluationRisque, Date>    colDateEvaluation;
    @FXML private TableColumn<EvaluationRisque, Void>    colActions;

    // ── FXML - Autres contrôles ───────────────────────────────────────────────
    @FXML private TextField tfSearch;
    @FXML private Label     lblTotal;
    @FXML private Label     lblFaible;
    @FXML private Label     lblMoyen;
    @FXML private Label     lblEleve;
    @FXML private Label     lblCritique;
    @FXML private Label     lblStatus;
    @FXML private Button    btnChat;

    // ── Services & données ────────────────────────────────────────────────────
    private ServiceEvaluationRisque          service;
    private ObservableList<EvaluationRisque> evaluationsData;
    private ObservableList<EvaluationRisque> filteredData;

    // ── Chat - Config API ─────────────────────────────────────────────────────
    private static final String HF_API_KEY = "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";
    private static final String HF_API_URL = "https://router.huggingface.co/v1/chat/completions";

    // ✅ Format correct : "model-id:provider"
    // cerebras = gratuit et très rapide
    // auto = choisit automatiquement le meilleur provider disponible
    private static final String[] HF_MODELS = {
            "meta-llama/Llama-3.1-8B-Instruct:cerebras",
            "meta-llama/Llama-3.2-3B-Instruct:cerebras",
            "HuggingFaceTB/SmolLM3-3B:hf-inference",
            "Qwen/Qwen2.5-7B-Instruct:auto"
    };

    private static final String SYSTEM_CONTEXT =
            "Tu es un assistant expert en agriculture et gestion des risques agricoles. " +
                    "Réponds TOUJOURS en français, de manière concise et pratique, en 3 à 5 phrases maximum. " +
                    "Contexte : Dans un projet agricole, la température, l'humidité et le pH du sol sont " +
                    "trois facteurs essentiels. La température contrôle la photosynthèse. " +
                    "L'humidité du sol permet l'absorption des nutriments. " +
                    "Le pH est souvent le facteur le plus critique car il détermine la capacité d'absorption " +
                    "des éléments nutritifs. Tu aides les utilisateurs à interpréter et améliorer leurs " +
                    "évaluations de risque agricole.";

    // ── Chat - État UI ────────────────────────────────────────────────────────
    private Stage      chatStage;
    private VBox       chatMessagesContainer;
    private ScrollPane chatScrollPane;
    private TextField  chatTfMessage;
    private Button     chatBtnSend;
    private Label      chatLblStatus;

    // Historique UNIQUEMENT des échanges réussis (pas les erreurs)
    private final List<String[]>    conversationHistory = new ArrayList<>();
    private final DateTimeFormatter timeFmt             = DateTimeFormatter.ofPattern("HH:mm");

    // ═════════════════════════════════════════════════════════════════════════
    //  INITIALISATION
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        service         = new ServiceEvaluationRisque();
        evaluationsData = FXCollections.observableArrayList();
        filteredData    = FXCollections.observableArrayList();
        setupTableColumns();
        loadEvaluations();
        animateFabButton();
    }

    private void animateFabButton() {
        if (btnChat == null) return;
        javafx.animation.ScaleTransition pulse =
                new javafx.animation.ScaleTransition(javafx.util.Duration.millis(850), btnChat);
        pulse.setFromX(1.0); pulse.setToX(1.10);
        pulse.setFromY(1.0); pulse.setToY(1.10);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
        pulse.play();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAT - Construction du Stage
    // ═════════════════════════════════════════════════════════════════════════

    private void buildChatStageIfNeeded() {

        if (chatStage != null) return;

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chatbot.fxml"));
            VBox chatRoot = loader.load();

            // 🔥 ON UTILISE LE CONTROLLER (PLUS DE lookup)
            ChatController chatController = loader.getController();

            chatScrollPane        = chatController.scrollPane;
            chatMessagesContainer = chatController.messagesContainer;
            chatTfMessage         = chatController.tfChatMessage;
            chatBtnSend           = chatController.btnChatSend;
            chatLblStatus         = chatController.lblStatusChat;
            Button btnClose       = chatController.btnCloseChat;

            chatBtnSend.setOnAction(e -> handleChatSend());
            chatTfMessage.setOnAction(e -> handleChatSend());
            btnClose.setOnAction(e -> closeChatStage());

            Scene chatScene = new Scene(chatRoot);
            chatStage = new Stage();
            chatStage.setScene(chatScene);
            chatStage.setAlwaysOnTop(true);
            chatStage.setResizable(false);

            addBotBubble(
                    "👋 Bonjour ! Je suis votre assistant agricole.\n\n" +
                            "Posez-moi votre question."
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAT - Ouvrir / Fermer
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleToggleChat() {
        if (chatStage != null && chatStage.isShowing()) closeChatStage();
        else openChatStage();
    }

    private void openChatStage() {
        buildChatStageIfNeeded();
        if (chatStage == null) return;
        Stage owner = (Stage) btnChat.getScene().getWindow();
        double x = owner.getX() + owner.getWidth()  - 370 - 40;
        double y = owner.getY() + owner.getHeight() - 500 - 100;
        chatStage.setX(x);
        chatStage.setY(y);
        chatStage.show();
        btnChat.setText("✕");
        Platform.runLater(() -> { if (chatTfMessage != null) chatTfMessage.requestFocus(); });
    }

    private void closeChatStage() {
        if (chatStage != null) chatStage.hide();
        if (btnChat != null) btnChat.setText("💬");
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CHAT - Envoi
    // ═════════════════════════════════════════════════════════════════════════

    private void handleChatSend() {
        if (chatTfMessage == null) return;
        String userText = chatTfMessage.getText().trim();
        if (userText.isEmpty()) return;

        addUserBubble(userText);
        chatTfMessage.clear();
        chatBtnSend.setDisable(true);
        setStatusText("● En train d'écrire...");

        // Sauvegarder AVANT l'appel
        conversationHistory.add(new String[]{"user", userText});

        Thread thread = new Thread(() -> {
            String response = callAPIWithFallback(userText);
            boolean success = response != null && !response.startsWith("⚠️");

            // N'ajouter à l'historique que si c'est une vraie réponse
            if (success) {
                conversationHistory.add(new String[]{"assistant", response});
            } else {
                // Retirer le dernier message user de l'historique si échec
                if (!conversationHistory.isEmpty()) {
                    conversationHistory.remove(conversationHistory.size() - 1);
                }
            }

            final String finalResponse = response != null ? response
                    : "⚠️ Aucun modèle disponible. Vérifiez votre connexion.";

            Platform.runLater(() -> {
                addBotBubble(finalResponse);
                chatBtnSend.setDisable(false);
                setStatusText("● En ligne");
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ── Cascade de modèles ────────────────────────────────────────────────────

    private String callAPIWithFallback(String userMessage) {
        for (String model : HF_MODELS) {
            try {
                System.out.println("[Chat] Essai : " + model);
                String result = callChatCompletionsAPI(userMessage, model);
                // ✅ Succès uniquement si on a du texte ET pas d'erreur API
                if (result != null && !result.isBlank() && !result.startsWith("ERR:")) {
                    System.out.println("[Chat] ✅ OK avec : " + model);
                    return result;
                }
                System.out.println("[Chat] ❌ Échec : " + model + " → " + result);
            } catch (Exception ex) {
                System.err.println("[Chat] Exception [" + model + "] : " + ex.getMessage());
            }
        }
        return "⚠️ Aucun modèle disponible actuellement. Réessayez dans quelques secondes.";
    }

    private String callChatCompletionsAPI(String userMessage, String model) throws IOException {
        URL url = new URL(HF_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + HF_API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(40000);

        // Construire le tableau messages
        StringBuilder msgs = new StringBuilder("[");
        msgs.append("{\"role\":\"system\",\"content\":").append(escapeJson(SYSTEM_CONTEXT)).append("}");

        // Historique (6 derniers échanges max, seulement les réussis)
        int start = Math.max(0, conversationHistory.size() - 7);
        for (int i = start; i < conversationHistory.size() - 1; i++) {
            String[] t = conversationHistory.get(i);
            msgs.append(",{\"role\":\"").append(t[0]).append("\",\"content\":").append(escapeJson(t[1])).append("}");
        }
        msgs.append(",{\"role\":\"user\",\"content\":").append(escapeJson(userMessage)).append("}]");

        String body = "{\"model\":" + escapeJson(model)
                + ",\"messages\":" + msgs
                + ",\"max_tokens\":300,\"temperature\":0.7}";

        System.out.println("[Chat] Body : " + body);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        System.out.println("[Chat] HTTP : " + status);

        InputStream is = (status == 200) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }

        String raw = sb.toString();
        System.out.println("[Chat] Brut : " + raw);

        // ✅ Retourner "ERR:..." si erreur API (pour ne pas le confondre avec une réponse)
        if (status != 200) return "ERR:" + raw;
        return parseChatResponse(raw);
    }

    /**
     * Parse : {"choices":[{"message":{"role":"assistant","content":"..."}},...]}
     */
    private String parseChatResponse(String json) {
        try {
            if (json.contains("\"choices\"") && json.contains("\"content\"")) {
                // Trouver le content dans choices[0].message.content
                int choicesIdx = json.indexOf("\"choices\"");
                int contentIdx = json.indexOf("\"content\"", choicesIdx);
                if (contentIdx != -1) {
                    // Sauter : "content":
                    int colon      = json.indexOf(":", contentIdx + 9);
                    int valueStart = json.indexOf("\"", colon) + 1;
                    int valueEnd   = findJsonStringEnd(json, valueStart);
                    String text = json.substring(valueStart, valueEnd)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\")
                            .trim();
                    return text.isEmpty() ? null : text;
                }
            }
            if (json.contains("\"error\"")) {
                System.err.println("[Chat] Erreur API dans JSON : " + json);
            }
        } catch (Exception e) {
            System.err.println("[Chat] Parsing exception : " + e.getMessage());
        }
        return null;
    }

    private int findJsonStringEnd(String json, int start) {
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\') { i++; continue; }
            if (c == '"')  return i;
        }
        return json.length();
    }

    private String escapeJson(String text) {
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                + "\"";
    }

    // ── Bulles ────────────────────────────────────────────────────────────────

    private void addUserBubble(String text) {
        if (chatMessagesContainer == null) return;
        Label bubble = new Label(text);
        bubble.getStyleClass().add("bubble-user");
        bubble.setWrapText(true); bubble.setMaxWidth(250);
        Label time = new Label(LocalTime.now().format(timeFmt));
        time.getStyleClass().add("bubble-time");
        VBox wrapper = new VBox(3, bubble, time);
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        HBox row = new HBox(wrapper);
        row.setAlignment(Pos.CENTER_RIGHT);
        chatMessagesContainer.getChildren().add(row);
        scrollChatToBottom();
    }

    private void addBotBubble(String text) {
        if (chatMessagesContainer == null) return;
        Label avatar = new Label("🌿");
        avatar.setStyle("-fx-font-size: 17px;");
        Label bubble = new Label(text);
        bubble.getStyleClass().add("bubble-bot");
        bubble.setWrapText(true); bubble.setMaxWidth(275);
        Label time = new Label(LocalTime.now().format(timeFmt));
        time.getStyleClass().add("bubble-time");
        VBox textWrapper = new VBox(3, bubble, time);
        textWrapper.setAlignment(Pos.CENTER_LEFT);
        HBox row = new HBox(8, avatar, textWrapper);
        row.setAlignment(Pos.TOP_LEFT);
        chatMessagesContainer.getChildren().add(row);
        scrollChatToBottom();
    }

    private void scrollChatToBottom() {
        Platform.runLater(() -> { if (chatScrollPane != null) chatScrollPane.setVvalue(1.0); });
    }
    private void setStatusText(String text) {
        if (chatLblStatus != null) chatLblStatus.setText(text);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CONFIGURATION DES COLONNES
    // ═════════════════════════════════════════════════════════════════════════

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idEvaluation"));
        colId.setStyle("-fx-alignment: CENTER;");
        colIdProjet.setCellValueFactory(new PropertyValueFactory<>("idProjet"));
        colIdProjet.setStyle("-fx-alignment: CENTER;");
        colScoreGlobal.setCellValueFactory(new PropertyValueFactory<>("scoreGlobal"));
        colScoreGlobal.setStyle("-fx-alignment: CENTER;");

        colNiveauRisque.setCellValueFactory(new PropertyValueFactory<>("niveauRisque"));
        colNiveauRisque.setCellFactory(column -> new TableCell<EvaluationRisque, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item); setAlignment(Pos.CENTER);
                switch (item) {
                    case "Faible":   setStyle("-fx-background-color: #B2D944; -fx-text-fill: #133D03; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;"); break;
                    case "Moyen":    setStyle("-fx-background-color: #E1B323; -fx-text-fill: #133D03; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;"); break;
                    case "Élevé":   setStyle("-fx-background-color: #E17D23; -fx-text-fill: white;   -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;"); break;
                    case "Critique": setStyle("-fx-background-color: #D94444; -fx-text-fill: white;   -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;"); break;
                    default: setStyle("");
                }
            }
        });

        colFiabiliteDonnees.setCellValueFactory(new PropertyValueFactory<>("fiabiliteDonnees"));
        colFiabiliteDonnees.setStyle("-fx-alignment: CENTER;");

        colFacteurPrincipal.setCellValueFactory(new PropertyValueFactory<>("facteurPrincipal"));
        colFacteurPrincipal.setCellFactory(column -> new TableCell<EvaluationRisque, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setTooltip(null); return; }
                setText(item.length() > 50 ? item.substring(0, 50) + "..." : item);
                Tooltip t = new Tooltip(item); t.setWrapText(true); t.setMaxWidth(400); setTooltip(t);
            }
        });

        colRecommandation.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        getRecommandationString(cellData.getValue().getRecommandation())));
        colRecommandation.setStyle("-fx-alignment: CENTER;");

        colDateEvaluation.setCellValueFactory(new PropertyValueFactory<>("dateEvaluation"));
        colDateEvaluation.setCellFactory(column -> new TableCell<EvaluationRisque, Date>() {
            private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(fmt.format(item)); setAlignment(Pos.CENTER);
            }
        });

        colActions.setCellFactory(column -> new TableCell<EvaluationRisque, Void>() {
            private final Button btnEdit   = new Button("✏️ Modifier");
            private final Button btnDelete = new Button("🗑️ Supprimer");
            private final HBox   hbox      = new HBox(10, btnEdit, btnDelete);
            {
                btnEdit.getStyleClass().add("button-primary");
                btnEdit.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
                btnDelete.getStyleClass().add("button-secondary");
                btnDelete.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");
                hbox.setAlignment(Pos.CENTER);
                btnEdit.setOnAction(e   -> handleEdit(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty ? null : hbox);
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CHARGEMENT & STATISTIQUES
    // ═════════════════════════════════════════════════════════════════════════

    private void loadEvaluations() {
        try {
            evaluationsData.clear();
            evaluationsData.addAll(service.afficher());
            filteredData.setAll(evaluationsData);
            tableEvaluations.setItems(filteredData);
            updateStatistics();
            updateStatus("Données chargées - " + evaluationsData.size() + " évaluation(s)");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les évaluations: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        lblTotal.setText(String.valueOf(evaluationsData.size()));
        lblFaible.setText(String.valueOf(evaluationsData.stream().filter(d -> "Faible".equals(d.getNiveauRisque())).count()));
        lblMoyen.setText(String.valueOf(evaluationsData.stream().filter(d -> "Moyen".equals(d.getNiveauRisque())).count()));
        lblEleve.setText(String.valueOf(evaluationsData.stream().filter(d -> "Élevé".equals(d.getNiveauRisque())).count()));
        lblCritique.setText(String.valueOf(evaluationsData.stream().filter(d -> "Critique".equals(d.getNiveauRisque())).count()));
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HANDLERS FXML
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleSearch() {
        String s = tfSearch.getText().toLowerCase().trim();
        if (s.isEmpty()) { filteredData.setAll(evaluationsData); }
        else {
            filteredData.clear();
            for (EvaluationRisque e : evaluationsData) {
                if (String.valueOf(e.getIdEvaluation()).contains(s)  ||
                        String.valueOf(e.getIdProjet()).contains(s)       ||
                        String.valueOf(e.getScoreGlobal()).contains(s)    ||
                        e.getNiveauRisque().toLowerCase().contains(s)     ||
                        e.getFiabiliteDonnees().toLowerCase().contains(s) ||
                        e.getFacteurPrincipal().toLowerCase().contains(s) ||
                        getRecommandationString(e.getRecommandation()).toLowerCase().contains(s))
                    filteredData.add(e);
            }
        }
        tableEvaluations.setItems(filteredData);
        updateStatus(filteredData.size() + " résultat(s) trouvé(s)");
    }

    @FXML private void handleRefresh() { loadEvaluations(); }

    @FXML
    private void handleNewEvaluation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Risque.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Nouvelle Évaluation");
            stage.setScene(new Scene(root));
            stage.setMinWidth(800); stage.setMinHeight(600);
            stage.setOnHidden(e -> loadEvaluations());
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewDecisionList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DecisionList.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Liste des Décisions Financières");
            stage.setScene(new Scene(root));
            stage.setMinWidth(1400); stage.setMinHeight(800);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportPdf() {
        if (filteredData.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Export PDF", "Aucune donnée à exporter."); return; }
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le rapport PDF");
        fc.setInitialFileName("rapport_risques_" + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));
        Stage stage = (Stage) tableEvaluations.getScene().getWindow();
        File file = fc.showSaveDialog(stage);
        if (file != null) {
            try {
                updateStatus("⏳ Génération du PDF en cours...");
                new PdfExportService().exportToPdf(new ArrayList<>(filteredData), file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Export réussi", "✅ PDF généré :\n" + file.getAbsolutePath());
                updateStatus("PDF exporté : " + file.getName());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur d'export", "Impossible de générer le PDF :\n" + e.getMessage());
                updateStatus("❌ Erreur lors de l'export PDF");
            }
        }
    }

    private void handleEdit(EvaluationRisque evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Risque.fxml"));
            Parent root = loader.load();
            RisqueController controller = loader.getController();
            controller.loadEvaluation(evaluation.getIdEvaluation());
            Stage stage = new Stage();
            stage.setTitle("Modifier Évaluation #" + evaluation.getIdEvaluation());
            stage.setScene(new Scene(root));
            stage.setMinWidth(800); stage.setMinHeight(600);
            stage.setOnHidden(e -> loadEvaluations());
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void handleDelete(EvaluationRisque evaluation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'évaluation #" + evaluation.getIdEvaluation() + " ?");
        alert.setContentText("Cette action est irréversible !");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(evaluation.getIdEvaluation());
                evaluationsData.remove(evaluation);
                filteredData.remove(evaluation);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation supprimée avec succès !");
                updateStatistics();
                updateStatus("Évaluation supprimée");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }


    private String getRecommandationString(int r) {
        switch (r) {
            case 0: return "Aucune";
            case 1: return "Surveillance";
            case 2: return "Action immédiate";
            default: return "Inconnu";
        }
    }
    private void updateStatus(String message) { if (lblStatus != null) lblStatus.setText(message); }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
}