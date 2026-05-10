package com.agrifund.controller.banque;

import com.agrifund.controller.ChatController;
import com.agrifund.entities.Banque;
import com.agrifund.entities.Utilisateur;
import com.agrifund.services.BanqueService;
import com.agrifund.entities.EvaluationRisque;           // ✅ IMPORT MANQUANT AJOUTÉ
import com.agrifund.entities.EvaluationRisque;

import com.agrifund.services.PdfExportService;
import com.agrifund.services.ServiceEvaluationRisque;
import com.agrifund.services.ServiceProjectAgricoleChedy;
import com.agrifund.util.SessionManager;
import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    // CHAT - Variables
    // ══════════════════════════════════════════════════════════════
    private Stage chatStage;
    private ChatController chatController;

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

        ScaleTransition pulse = new ScaleTransition(Duration.millis(850), btnChat);
        pulse.setFromX(1.0);
        pulse.setToX(1.10);
        pulse.setFromY(1.0);
        pulse.setToY(1.10);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    // ══════════════════════════════════════════════════════════════
    // CONFIGURATION TABLE
    // ══════════════════════════════════════════════════════════════

    private void setupTableColumns() {
        // Colonne ID
        colId.setCellValueFactory(new PropertyValueFactory<>("idEvaluation"));
        colId.setStyle("-fx-alignment: CENTER;");

        // Colonne Nom du Projet
        colNomProjet.setCellValueFactory(cellData -> {
            try {
                String nomProjet = serviceProjet.getNameById(cellData.getValue().getIdProjet());
                return new SimpleStringProperty(nomProjet != null ? nomProjet : "N/A");
            } catch (SQLException e) {
                return new SimpleStringProperty("Erreur");
            }
        });

        // Colonne Score Global
        colScoreGlobal.setCellValueFactory(new PropertyValueFactory<>("scoreGlobal"));
        colScoreGlobal.setStyle("-fx-alignment: CENTER;");

        // Colonne Niveau Risque avec couleurs
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
                        case "Faible":
                            setStyle("-fx-background-color: #B2D944; -fx-text-fill: #133D03; " +
                                    "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        case "Moyen":
                            setStyle("-fx-background-color: #E1B323; -fx-text-fill: #133D03; " +
                                    "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        case "Élevé":
                            setStyle("-fx-background-color: #E17D23; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        case "Critique":
                            setStyle("-fx-background-color: #D94444; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Colonne Fiabilité Données
        colFiabiliteDonnees.setCellValueFactory(new PropertyValueFactory<>("fiabiliteDonnees"));
        colFiabiliteDonnees.setStyle("-fx-alignment: CENTER;");

        // Colonne Facteur Principal avec Tooltip
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
                    Tooltip tooltip = new Tooltip(item);
                    tooltip.setWrapText(true);
                    tooltip.setMaxWidth(400);
                    setTooltip(tooltip);
                }
            }
        });

        // Colonne Recommandation — PropertyValueFactory utilise getRecommendation() (alias anglais)
        colRecommandation.setCellValueFactory(new PropertyValueFactory<>("recommendation"));
        colRecommandation.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);

                    switch (item.toLowerCase()) {
                        case "recommandé":
                        case "recommande":
                            setStyle("-fx-background-color: #B2D944; -fx-text-fill: #133D03; " +
                                    "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        case "avec surveillance":
                        case "surveillance":
                            setStyle("-fx-background-color: #E1B323; -fx-text-fill: #133D03; " +
                                    "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        case "non recommandé":
                        case "non recommande":
                            setStyle("-fx-background-color: #D94444; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        default:
                            setStyle("-fx-alignment: CENTER;");
                    }
                }
            }
        });

        // Colonne Date Évaluation
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

        // Colonne Actions
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button btnEdit   = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox   hbox      = new HBox(5, btnEdit, btnDelete);

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
        lblFaible.setText(String.valueOf(
                evaluationsData.stream().filter(e -> "Faible".equals(e.getNiveauRisque())).count()));
        lblMoyen.setText(String.valueOf(
                evaluationsData.stream().filter(e -> "Moyen".equals(e.getNiveauRisque())).count()));
        lblEleve.setText(String.valueOf(
                evaluationsData.stream().filter(e -> "Élevé".equals(e.getNiveauRisque())).count()));
        if (lblCritique != null) {
            lblCritique.setText(String.valueOf(
                    evaluationsData.stream().filter(e -> "Critique".equals(e.getNiveauRisque())).count()));
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
                    String nomProjet    = serviceProjet.getNameById(e.getIdProjet());
                    // Utilise getRecommendation() (alias anglais) — les deux pointent vers le même champ
                    String recommendation = (e.getRecommandation() != null)
                            ? e.getRecommandation().toLowerCase() : "";

                    if ((nomProjet != null && nomProjet.toLowerCase().contains(search)) ||
                            e.getNiveauRisque().toLowerCase().contains(search) ||
                            e.getFacteurPrincipal().toLowerCase().contains(search) ||
                            recommendation.contains(search) ||
                            String.valueOf(e.getScoreGlobal()).contains(search)) {
                        filteredData.add(e);
                    }
                } catch (SQLException ex) {
                    // Ignorer l'erreur et continuer
                }
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
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/fxml/banque/banque-risque.fxml"));
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
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/fxml/banque/banque-decision-list.fxml"));
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
    // EXPORT PDF
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
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));

        Stage stage = (Stage) tableEvaluations.getScene().getWindow();
        File file = fc.showSaveDialog(stage);

        if (file != null) {
            try {
                updateStatus("⏳ Génération du PDF en cours...");
                new PdfExportService().exportToPdf(new ArrayList<>(filteredData), file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Export réussi",
                        "✅ PDF généré :\n" + file.getAbsolutePath());
                updateStatus("PDF exporté : " + file.getName());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur d'export",
                        "Impossible de générer le PDF :\n" + e.getMessage());
                updateStatus("❌ Erreur lors de l'export PDF");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // EDIT & DELETE
    // ══════════════════════════════════════════════════════════════

    private void handleEdit(EvaluationRisque evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/fxml/banque/banque-risque.fxml"));
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
                serviceEvaluation.supprimerParBanque(
                        evaluation.getIdEvaluation(),
                        currentBanque.getUtilisateurId());
                loadEvaluations();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation supprimée!");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // CHAT - Méthodes
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleToggleChat() {
        if (chatStage != null && chatStage.isShowing()) {
            closeChatStage();
        } else {
            openChatStage();
        }
    }

    private void openChatStage() {
        if (chatStage == null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/agrifund/fxml/chatbot.fxml"));
                Parent chatRoot = loader.load();

                chatController = loader.getController();
                chatController.setOnCloseCallback(this::closeChatStage);

                Scene scene = new Scene(chatRoot);
                scene.setFill(Color.TRANSPARENT);

                chatStage = new Stage();
                chatStage.initStyle(StageStyle.TRANSPARENT);
                chatStage.setScene(scene);
                chatStage.setAlwaysOnTop(true);
                chatStage.setResizable(false);
                chatStage.setOnCloseRequest(e -> closeChatStage());

                System.out.println("[Chat] Stage créé avec succès");

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible d'ouvrir le chat: " + e.getMessage());
                return;
            }
        }

        // Positionner en bas à droite de la fenêtre principale
        Stage owner = (Stage) btnChat.getScene().getWindow();
        double x = owner.getX() + owner.getWidth() - 390;
        double y = owner.getY() + owner.getHeight() - 540;
        chatStage.setX(x);
        chatStage.setY(y);
        chatStage.show();

        if (btnChat != null) {
            btnChat.setText("✕");
        }

        System.out.println("[Chat] Fenêtre ouverte");
    }

    private void closeChatStage() {
        if (chatStage != null) {
            chatStage.hide();
        }
        if (btnChat != null) {
            btnChat.setText("💬");
        }
        System.out.println("[Chat] Fenêtre fermée");
    }

    // ══════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ══════════════════════════════════════════════════════════════

    private void updateStatus(String msg) {
        if (lblStatus != null) {
            lblStatus.setText(msg);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
