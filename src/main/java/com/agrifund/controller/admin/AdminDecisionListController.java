package com.agrifund.controller.admin;

import com.agrifund.entities.DecisionFinanciere;
import com.agrifund.services.BanqueService;
import com.agrifund.services.ServiceDecisionFinanciere;
import com.agrifund.services.ServiceEvaluationRisque;
import com.agrifund.services.ServiceProjectAgricoleChedy;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import com.agrifund.entities.EvaluationRisque;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminDecisionListController {

    @FXML private TableView<DecisionFinanciere> tableDecisions;
    @FXML private TableColumn<DecisionFinanciere, Integer> colId;
    @FXML private TableColumn<DecisionFinanciere, String> colStatut;
    @FXML private TableColumn<DecisionFinanciere, String> colJustification;
    @FXML private TableColumn<DecisionFinanciere, Date> colDateDecision;
    @FXML private TableColumn<DecisionFinanciere, String> colProjet;
    @FXML private TableColumn<DecisionFinanciere, String> colBanque;
    @FXML private TableColumn<DecisionFinanciere, Integer> colIdEvaluation;

    @FXML private TextField tfSearch;
    @FXML private Label lblTotal;
    @FXML private Label lblApprouves;
    @FXML private Label lblEnAttente;
    @FXML private Label lblRejetes;
    @FXML private Label lblStatus;

    private ServiceDecisionFinanciere serviceDecision;
    private ServiceEvaluationRisque serviceEvaluation;
    private ServiceProjectAgricoleChedy serviceProjet;
    private BanqueService banqueService;

    private ObservableList<DecisionFinanciere> decisionsData;
    private ObservableList<DecisionFinanciere> filteredData;

    // Couleurs AgriFund
    private static final String PRIMARY_GREEN = "#089647";
    private static final String LIGHT_GREEN = "#B2D944";
    private static final String YELLOW = "#E1B323";
    private static final String OLIVE = "#476C1A";

    @FXML
    public void initialize() {
        serviceDecision = new ServiceDecisionFinanciere();
        serviceEvaluation = new ServiceEvaluationRisque();
        serviceProjet = new ServiceProjectAgricoleChedy();
        decisionsData = FXCollections.observableArrayList();
        filteredData = FXCollections.observableArrayList();

        try {
            banqueService = new BanqueService();
            setupTableColumns();
            loadDecisions();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idDecision"));
        colId.setStyle("-fx-alignment: CENTER;");

        // Colonne Statut avec couleurs AgriFund
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(column -> new TableCell<>() {
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
                        case "Approuvé":
                            setStyle("-fx-background-color: " + LIGHT_GREEN + "; -fx-text-fill: #133D03; " +
                                    "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 8;");
                            break;
                        case "En attente":
                            setStyle("-fx-background-color: " + YELLOW + "; -fx-text-fill: #133D03; " +
                                    "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 8;");
                            break;
                        case "Rejeté":
                            setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 8;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Colonne Justification avec Tooltip
        colJustification.setCellValueFactory(new PropertyValueFactory<>("justification"));
        colJustification.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item.length() > 50 ? item.substring(0, 50) + "..." : item);
                    Tooltip tooltip = new Tooltip(item);
                    tooltip.setWrapText(true);
                    tooltip.setMaxWidth(400);
                    setTooltip(tooltip);
                }
            }
        });

        // Colonne Date
        colDateDecision.setCellValueFactory(new PropertyValueFactory<>("dateDecision"));
        colDateDecision.setCellFactory(column -> new TableCell<>() {
            private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt.format(item));
                setAlignment(Pos.CENTER);
            }
        });

        // Colonne Projet
        colProjet.setCellValueFactory(cellData -> {
            try {
                EvaluationRisque eval = serviceEvaluation.getById(cellData.getValue().getIdEvaluation());
                if (eval != null) {
                    String nomProjet = serviceProjet.getNameById(eval.getIdProjet());
                    return new javafx.beans.property.SimpleStringProperty(nomProjet != null ? nomProjet : "N/A");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        // Colonne Banque
        colBanque.setCellValueFactory(cellData -> {
            try {
                var banque = banqueService.rechercherParUtilisateurId(cellData.getValue().getBanqueId());
                return new javafx.beans.property.SimpleStringProperty(
                        banque != null ? "🏦 " + banque.getNom() : "N/A"
                );
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Erreur");
            }
        });

        colIdEvaluation.setCellValueFactory(new PropertyValueFactory<>("idEvaluation"));
        colIdEvaluation.setStyle("-fx-alignment: CENTER;");
    }

    private void loadDecisions() {
        try {
            decisionsData.clear();
            decisionsData.addAll(serviceDecision.afficher());
            filteredData.setAll(decisionsData);
            tableDecisions.setItems(filteredData);
            updateStatistics();
            updateStatus("✅ " + decisionsData.size() + " décision(s) chargée(s) — Mode consultation");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void updateStatistics() {
        int total = decisionsData.size();
        int approuves = (int) decisionsData.stream().filter(d -> "Approuvé".equals(d.getStatut())).count();
        int enAttente = (int) decisionsData.stream().filter(d -> "En attente".equals(d.getStatut())).count();
        int rejetes = (int) decisionsData.stream().filter(d -> "Rejeté".equals(d.getStatut())).count();

        lblTotal.setText(String.valueOf(total));
        lblApprouves.setText(String.valueOf(approuves));
        lblEnAttente.setText(String.valueOf(enAttente));
        lblRejetes.setText(String.valueOf(rejetes));
    }

    @FXML
    private void handleSearch() {
        String search = tfSearch.getText().toLowerCase().trim();

        if (search.isEmpty()) {
            filteredData.setAll(decisionsData);
        } else {
            filteredData.clear();
            for (DecisionFinanciere d : decisionsData) {
                try {
                    if (d.getStatut().toLowerCase().contains(search)) {
                        filteredData.add(d);
                        continue;
                    }

                    if (d.getJustification().toLowerCase().contains(search)) {
                        filteredData.add(d);
                        continue;
                    }

                    var banque = banqueService.rechercherParUtilisateurId(d.getBanqueId());
                    if (banque != null && banque.getNom().toLowerCase().contains(search)) {
                        filteredData.add(d);
                        continue;
                    }

                    EvaluationRisque eval = serviceEvaluation.getById(d.getIdEvaluation());
                    if (eval != null) {
                        String nomProjet = serviceProjet.getNameById(eval.getIdProjet());
                        if (nomProjet != null && nomProjet.toLowerCase().contains(search)) {
                            filteredData.add(d);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        tableDecisions.setItems(filteredData);
        updateStatus("🔍 " + filteredData.size() + " résultat(s) trouvé(s)");
    }

    @FXML
    private void handleRefresh() {
        tfSearch.clear();
        loadDecisions();
    }

    @FXML
    private void handleViewEvaluations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/fxml/admin/admin-risque-list.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("📊 Toutes les Évaluations de Risque — AgriFund");
            stage.setScene(new Scene(root));
            stage.setMinWidth(1500);
            stage.setMinHeight(850);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleExportStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("══════════════════════════════════════════\n");
        stats.append("     🌾 RAPPORT DES DÉCISIONS - AGRIFUND\n");
        stats.append("══════════════════════════════════════════\n\n");

        stats.append("📊 STATISTIQUES GLOBALES\n");
        stats.append("────────────────────────────────────────────\n");
        stats.append(String.format("📋 Total des décisions : %s\n", lblTotal.getText()));
        stats.append(String.format("✅ Approuvées : %s\n", lblApprouves.getText()));
        stats.append(String.format("⏳ En attente : %s\n", lblEnAttente.getText()));
        stats.append(String.format("❌ Rejetées : %s\n", lblRejetes.getText()));

        if (!decisionsData.isEmpty()) {
            double tauxApprobation = (Double.parseDouble(lblApprouves.getText()) / decisionsData.size()) * 100;
            stats.append(String.format("\n📈 Taux d'approbation : %.1f%%\n", tauxApprobation));
        }

        stats.append("\n══════════════════════════════════════════\n");
        stats.append("🕐 Généré le : " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()) + "\n");
        stats.append("🏢 AgriFund - Plateforme de financement agricole");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("📊 Statistiques des Décisions");
        alert.setHeaderText(null);

        TextArea textArea = new TextArea(stats.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 13px;");
        textArea.setPrefWidth(480);
        textArea.setPrefHeight(380);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
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
