package com.agrifund.controller.admin;

import com.agrifund.entities.EvaluationRisque;
import com.agrifund.services.BanqueService;
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

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminRisqueListController {

    @FXML private TableView<EvaluationRisque> tableEvaluations;
    @FXML private TableColumn<EvaluationRisque, Integer> colId;
    @FXML private TableColumn<EvaluationRisque, String> colNomProjet;
    @FXML private TableColumn<EvaluationRisque, String> colBanque;
    @FXML private TableColumn<EvaluationRisque, Integer> colScoreGlobal;
    @FXML private TableColumn<EvaluationRisque, String> colNiveauRisque;
    @FXML private TableColumn<EvaluationRisque, String> colFiabiliteDonnees;
    @FXML private TableColumn<EvaluationRisque, String> colFacteurPrincipal;
    @FXML private TableColumn<EvaluationRisque, String> colRecommandation;
    @FXML private TableColumn<EvaluationRisque, Date> colDateEvaluation;

    @FXML private TextField tfSearch;
    @FXML private Label lblTotal;
    @FXML private Label lblFaible;
    @FXML private Label lblMoyen;
    @FXML private Label lblEleve;
    @FXML private Label lblCritique;
    @FXML private Label lblStatus;

    private ServiceEvaluationRisque serviceEvaluation;
    private ServiceProjectAgricoleChedy serviceProjet;
    private BanqueService banqueService;

    private ObservableList<EvaluationRisque> evaluationsData;
    private ObservableList<EvaluationRisque> filteredData;

    @FXML
    public void initialize() {
        serviceEvaluation = new ServiceEvaluationRisque();
        serviceProjet = new ServiceProjectAgricoleChedy();
        evaluationsData = FXCollections.observableArrayList();
        filteredData = FXCollections.observableArrayList();

        try {
            banqueService = new BanqueService();
            setupTableColumns();
            loadEvaluations();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

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

        // ✅ Colonne Banque (pour l'admin)
        colBanque.setCellValueFactory(cellData -> {
            try {
                var banque = banqueService.rechercherParUtilisateurId(cellData.getValue().getBanqueId());
                return new javafx.beans.property.SimpleStringProperty(
                        banque != null ? banque.getNom() : "N/A"
                );
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Erreur");
            }
        });

        colScoreGlobal.setCellValueFactory(new PropertyValueFactory<>("scoreGlobal"));
        colScoreGlobal.setStyle("-fx-alignment: CENTER;");

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
    }

    private void loadEvaluations() {
        try {
            evaluationsData.clear();
            // ✅ Admin voit TOUTES les évaluations
            evaluationsData.addAll(serviceEvaluation.afficher());
            filteredData.setAll(evaluationsData);
            tableEvaluations.setItems(filteredData);
            updateStatistics();
            updateStatus("✅ " + evaluationsData.size() + " évaluation(s) - Mode consultation");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void updateStatistics() {
        lblTotal.setText(String.valueOf(evaluationsData.size()));
        lblFaible.setText(String.valueOf(evaluationsData.stream().filter(e -> "Faible".equals(e.getNiveauRisque())).count()));
        lblMoyen.setText(String.valueOf(evaluationsData.stream().filter(e -> "Moyen".equals(e.getNiveauRisque())).count()));
        lblEleve.setText(String.valueOf(evaluationsData.stream().filter(e -> "Élevé".equals(e.getNiveauRisque())).count()));
        lblCritique.setText(String.valueOf(evaluationsData.stream().filter(e -> "Critique".equals(e.getNiveauRisque())).count()));
    }

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
                    var banque = banqueService.rechercherParUtilisateurId(e.getBanqueId());
                    String nomBanque = banque != null ? banque.getNom() : "";

                    if ((nomProjet != null && nomProjet.toLowerCase().contains(search)) ||
                            nomBanque.toLowerCase().contains(search) ||
                            e.getNiveauRisque().toLowerCase().contains(search) ||
                            e.getFacteurPrincipal().toLowerCase().contains(search)) {
                        filteredData.add(e);
                    }
                } catch (SQLException ex) {}
            }
        }
        updateStatus(filteredData.size() + " résultat(s)");
    }

    @FXML
    private void handleRefresh() {
        loadEvaluations();
    }

    @FXML
    private void handleViewDecisions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/fxml/admin/admin-decision-list.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("📊 Toutes les Décisions Financières");
            stage.setScene(new Scene(root));
            stage.setMinWidth(1400);
            stage.setMinHeight(800);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private String getRecommandationString(int r) {
        switch (r) {
            case 0: return "Recommandé";
            case 1: return "Surveillance";
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