package com.agrifund.controller.banque;

import com.agrifund.entities.Banque;
import com.agrifund.entities.EvaluationRisque;
import com.agrifund.entities.Utilisateur;
import com.agrifund.services.BanqueService;
import com.agrifund.services.PdfExportService1;
import com.agrifund.services.ServiceEvaluationRisque;
import com.agrifund.services.ServiceProjectAgricoleChedy;
import com.agrifund.util.SessionManager;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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

public class BanqueRisqueListController {

    // ══════════════════════════════════════════════════════════════
    // FXML - Table & colonnes
    // ══════════════════════════════════════════════════════════════
    @FXML private TableView<EvaluationRisque> tableEvaluations;
    @FXML private TableColumn<EvaluationRisque, Integer> colId;
    @FXML private TableColumn<EvaluationRisque, String> colNomProjet;
    @FXML private TableColumn<EvaluationRisque, Integer> colScoreGlobal;
    @FXML private TableColumn<EvaluationRisque, String> colNiveauRisque;
    @FXML private TableColumn<EvaluationRisque, String> colFiabiliteDonnees;
    @FXML private TableColumn<EvaluationRisque, String> colFacteurPrincipal;
    @FXML private TableColumn<EvaluationRisque, String> colRecommandation;
    @FXML private TableColumn<EvaluationRisque, Date> colDateEvaluation;
    @FXML private TableColumn<EvaluationRisque, Void> colActions;

    // ══════════════════════════════════════════════════════════════
    // FXML - Autres contrôles
    // ══════════════════════════════════════════════════════════════
    @FXML private TextField tfSearch;
    @FXML private Label lblTotal;
    @FXML private Label lblFaible;
    @FXML private Label lblMoyen;
    @FXML private Label lblEleve;
    @FXML private Label lblCritique;
    @FXML private Label lblStatus;
    @FXML private Label lblBanqueInfo;
    @FXML private Button btnChat;

    // ══════════════════════════════════════════════════════════════
    // Services & données
    // ══════════════════════════════════════════════════════════════
    private ServiceEvaluationRisque serviceEvaluation;
    private ServiceProjectAgricoleChedy serviceProjet;
    private BanqueService banqueService;

    private Banque currentBanque;
    private ObservableList<EvaluationRisque> evaluationsData;
    private ObservableList<EvaluationRisque> filteredData;

    // ══════════════════════════════════════════════════════════════
    // Chat - Config API
    // ══════════════════════════════════════════════════════════════
    private static final String HF_API_KEY = "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";
    private static final String HF_API_URL = "https://router.huggingface.co/v1/chat/completions";

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
                    "trois facteurs essentiels. Tu aides les banques à interpréter et évaluer les risques " +
                    "des projets agricoles pour le financement.";

    // ══════════════════════════════════════════════════════════════
    // Chat - État UI
    // ══════════════════════════════════════════════════════════════
    private Stage chatStage;
    private VBox chatMessagesContainer;
    private ScrollPane chatScrollPane;
    private TextField chatTfMessage;
    private Button chatBtnSend;
    private Label chatLblStatus;

    private final List<String[]> conversationHistory = new ArrayList<>();
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    // ══════════════════════════════════════════════════════════════
    // INITIALISATION
    // ══════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        serviceEvaluation = new ServiceEvaluationRisque();
        serviceProjet = new ServiceProjectAgricoleChedy();
        evaluationsData = FXCollections.observableArrayList();
        filteredData = FXCollections.observableArrayList();

        try {
            banqueService = new BanqueService();
            loadCurrentBanque();
            setupTableColumns();
            loadEvaluations();
            animateFabButton();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void loadCurrentBanque() throws SQLException {
        Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
        currentBanque = banqueService.rechercherParUtilisateurId(user.getId());

        if (currentBanque != null && lblBanqueInfo != null) {
            lblBanqueInfo.setText("🏦 " + currentBanque.getNom() + " - Mes Évaluations");
        }
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

    // ══════════════════════════════════════════════════════════════
    // CONFIGURATION TABLE
    // ══════════════════════════════════════════════════════════════

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idEvaluation"));
        colId.setStyle("-fx-alignment: CENTER;");

        // Colonne Nom du Projet
        colNomProjet.setCellValueFactory(cellData -> {
            try {
                String nomProjet = serviceProjet.getNameById(cellData.getValue().getIdProjet());
                return new javafx.beans.property.SimpleStringProperty(nomProjet != null ? nomProjet : "N/A");
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Erreur");
            }
        });

        colScoreGlobal.setCellValueFactory(new PropertyValueFactory<>("scoreGlobal"));
        colScoreGlobal.setStyle("-fx-alignment: CENTER;");

        // Niveau Risque avec couleurs
        colNiveauRisque.setCellValueFactory(new PropertyValueFactory<>("niveauRisque"));
        colNiveauRisque.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
                    switch (item) {
                        case "Faible": setStyle("-fx-background-color: #B2D944; -fx-text-fill: #133D03; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;"); break;
                        case "Moyen": setStyle("-fx-background-color: #E1B323; -fx-text-fill: #133D03; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;"); break;
                        case "Élevé": setStyle("-fx-background-color: #E17D23; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;"); break;
                        case "Critique": setStyle("-fx-background-color: #D94444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;"); break;
                        default: setStyle("");
                    }
                }
            }
        });

        colFiabiliteDonnees.setCellValueFactory(new PropertyValueFactory<>("fiabiliteDonnees"));
        colFiabiliteDonnees.setStyle("-fx-alignment: CENTER;");

        colFacteurPrincipal.setCellValueFactory(new PropertyValueFactory<>("facteurPrincipal"));
        colFacteurPrincipal.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item.length() > 40 ? item.substring(0, 40) + "..." : item);
                    Tooltip t = new Tooltip(item);
                    t.setWrapText(true);
                    t.setMaxWidth(400);
                    setTooltip(t);
                }
            }
        });

        colRecommandation.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        getRecommandationString(cellData.getValue().getRecommandation())
                )
        );
        colRecommandation.setStyle("-fx-alignment: CENTER;");

        colDateEvaluation.setCellValueFactory(new PropertyValueFactory<>("dateEvaluation"));
        colDateEvaluation.setCellFactory(column -> new TableCell<>() {
            private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt.format(item));
                setAlignment(Pos.CENTER);
            }
        });

        // Actions
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox hbox = new HBox(5, btnEdit, btnDelete);
            {
                btnEdit.setStyle("-fx-font-size: 12px; -fx-cursor: hand;");
                btnEdit.setTooltip(new Tooltip("Modifier"));
                btnDelete.setStyle("-fx-font-size: 12px; -fx-cursor: hand;");
                btnDelete.setTooltip(new Tooltip("Supprimer"));
                hbox.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    // CHARGEMENT DONNÉES
    // ══════════════════════════════════════════════════════════════

    private void loadEvaluations() {
        try {
            evaluationsData.clear();
            // ✅ Charger UNIQUEMENT les évaluations de cette banque
            evaluationsData.addAll(serviceEvaluation.afficherParBanque(currentBanque.getUtilisateurId()));
            filteredData.setAll(evaluationsData);
            tableEvaluations.setItems(filteredData);
            updateStatistics();
            updateStatus("✅ " + evaluationsData.size() + " évaluation(s) chargée(s)");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void updateStatistics() {
        lblTotal.setText(String.valueOf(evaluationsData.size()));
        lblFaible.setText(String.valueOf(evaluationsData.stream().filter(e -> "Faible".equals(e.getNiveauRisque())).count()));
        lblMoyen.setText(String.valueOf(evaluationsData.stream().filter(e -> "Moyen".equals(e.getNiveauRisque())).count()));
        lblEleve.setText(String.valueOf(evaluationsData.stream().filter(e -> "Élevé".equals(e.getNiveauRisque())).count()));
        if (lblCritique != null) {
            lblCritique.setText(String.valueOf(evaluationsData.stream().filter(e -> "Critique".equals(e.getNiveauRisque())).count()));
        }
    }

    // ══════════════════════════════════════════════════════════════
    // HANDLERS FXML
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleSearch() {
        String search = tfSearch.getText().toLowerCase().trim();
        if (search.isEmpty()) {
            filteredData.setAll(evaluationsData);
        } else {
            filteredData.clear();
            for (EvaluationRisque e : evaluationsData) {
                try {
                    String nomProjet = serviceProjet.getNameById(e.getIdProjet());
                    if ((nomProjet != null && nomProjet.toLowerCase().contains(search)) ||
                            e.getNiveauRisque().toLowerCase().contains(search) ||
                            e.getFacteurPrincipal().toLowerCase().contains(search) ||
                            String.valueOf(e.getScoreGlobal()).contains(search)) {
                        filteredData.add(e);
                    }
                } catch (SQLException ex) {}
            }
        }
        tableEvaluations.setItems(filteredData);
        updateStatus(filteredData.size() + " résultat(s)");
    }

    @FXML
    private void handleRefresh() {
        tfSearch.clear();
        loadEvaluations();
    }

    @FXML
    private void handleNewEvaluation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/fxml/banque/banque-risque.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Évaluation de Risque");
            stage.setScene(new Scene(root));
            stage.setMinWidth(900);
            stage.setMinHeight(850);
            stage.setOnHidden(e -> loadEvaluations());
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleViewDecisions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/fxml/banque/banque-decision-list.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Mes Décisions Financières");
            stage.setScene(new Scene(root));
            stage.setMinWidth(1400);
            stage.setMinHeight(850);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // ✅ EXPORT PDF
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleExportPdf() {
        if (filteredData.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Export PDF", "Aucune donnée à exporter.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le rapport PDF");
        fc.setInitialFileName("rapport_risques_" + currentBanque.getCodeBanque() + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));

        Stage stage = (Stage) tableEvaluations.getScene().getWindow();
        File file = fc.showSaveDialog(stage);

        if (file != null) {
            try {
                updateStatus("⏳ Génération du PDF en cours...");
                new PdfExportService1().exportToPdf(new ArrayList<>(filteredData), file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Export réussi", "✅ PDF généré :\n" + file.getAbsolutePath());
                updateStatus("PDF exporté : " + file.getName());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur d'export", "Impossible de générer le PDF :\n" + e.getMessage());
                updateStatus("❌ Erreur lors de l'export PDF");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // EDIT & DELETE
    // ══════════════════════════════════════════════════════════════

    private void handleEdit(EvaluationRisque evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/fxml/banque/banque-risque.fxml"));
            Parent root = loader.load();

            BanqueRisqueController controller = loader.getController();
            controller.loadEvaluation(evaluation.getIdEvaluation());

            Stage stage = new Stage();
            stage.setTitle("Modifier Évaluation #" + evaluation.getIdEvaluation());
            stage.setScene(new Scene(root));
            stage.setMinWidth(900);
            stage.setMinHeight(850);
            stage.setOnHidden(e -> loadEvaluations());
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void handleDelete(EvaluationRisque evaluation) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer cette évaluation ?");
        confirm.setContentText("Cette action est irréversible!");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceEvaluation.supprimerParBanque(evaluation.getIdEvaluation(), currentBanque.getUtilisateurId());
                loadEvaluations();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation supprimée!");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // ✅ CHATBOT
    // ══════════════════════════════════════════════════════════════

    private void buildChatStageIfNeeded() {
        if (chatStage != null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/fxml/chatbot.fxml"));
            VBox chatRoot = loader.load();

            // Récupérer les éléments du chat
            chatScrollPane = (ScrollPane) chatRoot.lookup("#scrollPane");
            chatMessagesContainer = (VBox) chatRoot.lookup("#messagesContainer");
            chatTfMessage = (TextField) chatRoot.lookup("#tfChatMessage");
            chatBtnSend = (Button) chatRoot.lookup("#btnChatSend");
            chatLblStatus = (Label) chatRoot.lookup("#lblStatusChat");
            Button btnClose = (Button) chatRoot.lookup("#btnCloseChat");

            if (chatBtnSend != null) chatBtnSend.setOnAction(e -> handleChatSend());
            if (chatTfMessage != null) chatTfMessage.setOnAction(e -> handleChatSend());
            if (btnClose != null) btnClose.setOnAction(e -> closeChatStage());

            Scene chatScene = new Scene(chatRoot);
            chatStage = new Stage();
            chatStage.setScene(chatScene);
            chatStage.setAlwaysOnTop(true);
            chatStage.setResizable(false);

            addBotBubble("👋 Bonjour ! Je suis votre assistant agricole.\n\nPosez-moi votre question sur l'évaluation des risques.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleToggleChat() {
        if (chatStage != null && chatStage.isShowing()) {
            closeChatStage();
        } else {
            openChatStage();
        }
    }

    private void openChatStage() {
        buildChatStageIfNeeded();
        if (chatStage == null) return;

        Stage owner = (Stage) btnChat.getScene().getWindow();
        double x = owner.getX() + owner.getWidth() - 370 - 40;
        double y = owner.getY() + owner.getHeight() - 500 - 100;
        chatStage.setX(x);
        chatStage.setY(y);
        chatStage.show();
        btnChat.setText("✕");

        Platform.runLater(() -> {
            if (chatTfMessage != null) chatTfMessage.requestFocus();
        });
    }

    private void closeChatStage() {
        if (chatStage != null) chatStage.hide();
        if (btnChat != null) btnChat.setText("💬");
    }

    private void handleChatSend() {
        if (chatTfMessage == null) return;
        String userText = chatTfMessage.getText().trim();
        if (userText.isEmpty()) return;

        addUserBubble(userText);
        chatTfMessage.clear();
        chatBtnSend.setDisable(true);
        setStatusText("● En train d'écrire...");

        conversationHistory.add(new String[]{"user", userText});

        Thread thread = new Thread(() -> {
            String response = callAPIWithFallback(userText);
            boolean success = response != null && !response.startsWith("⚠️");

            if (success) {
                conversationHistory.add(new String[]{"assistant", response});
            } else {
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

    private String callAPIWithFallback(String userMessage) {
        for (String model : HF_MODELS) {
            try {
                String result = callChatCompletionsAPI(userMessage, model);
                if (result != null && !result.isBlank() && !result.startsWith("ERR:")) {
                    return result;
                }
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

        StringBuilder msgs = new StringBuilder("[");
        msgs.append("{\"role\":\"system\",\"content\":").append(escapeJson(SYSTEM_CONTEXT)).append("}");

        int start = Math.max(0, conversationHistory.size() - 7);
        for (int i = start; i < conversationHistory.size() - 1; i++) {
            String[] t = conversationHistory.get(i);
            msgs.append(",{\"role\":\"").append(t[0]).append("\",\"content\":").append(escapeJson(t[1])).append("}");
        }
        msgs.append(",{\"role\":\"user\",\"content\":").append(escapeJson(userMessage)).append("}]");

        String body = "{\"model\":" + escapeJson(model)
                + ",\"messages\":" + msgs
                + ",\"max_tokens\":300,\"temperature\":0.7}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        InputStream is = (status == 200) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }

        String raw = sb.toString();
        if (status != 200) return "ERR:" + raw;
        return parseChatResponse(raw);
    }

    private String parseChatResponse(String json) {
        try {
            if (json.contains("\"choices\"") && json.contains("\"content\"")) {
                int choicesIdx = json.indexOf("\"choices\"");
                int contentIdx = json.indexOf("\"content\"", choicesIdx);
                if (contentIdx != -1) {
                    int colon = json.indexOf(":", contentIdx + 9);
                    int valueStart = json.indexOf("\"", colon) + 1;
                    int valueEnd = findJsonStringEnd(json, valueStart);
                    String text = json.substring(valueStart, valueEnd)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\")
                            .trim();
                    return text.isEmpty() ? null : text;
                }
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
            if (c == '"') return i;
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

    private void addUserBubble(String text) {
        if (chatMessagesContainer == null) return;
        Label bubble = new Label(text);
        bubble.getStyleClass().add("bubble-user");
        bubble.setWrapText(true);
        bubble.setMaxWidth(250);
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
        bubble.setWrapText(true);
        bubble.setMaxWidth(275);
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
        Platform.runLater(() -> {
            if (chatScrollPane != null) chatScrollPane.setVvalue(1.0);
        });
    }

    private void setStatusText(String text) {
        if (chatLblStatus != null) chatLblStatus.setText(text);
    }

    // ══════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ══════════════════════════════════════════════════════════════

    private String getRecommandationString(int r) {
        switch (r) {
            case 0: return "Recommandé";
            case 1: return "Avec surveillance";
            case 2: return "Non recommandé";
            default: return "Aucune";
        }
    }

    private void updateStatus(String msg) {
        if (lblStatus != null) lblStatus.setText(msg);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}