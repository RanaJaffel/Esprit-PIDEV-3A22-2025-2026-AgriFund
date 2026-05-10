package com.agrifund.controller.banque;

import com.agrifund.entities.Banque;
import com.agrifund.entities.DecisionFinanciere;
import com.agrifund.entities.Utilisateur;
import com.agrifund.services.BanqueService;
import com.agrifund.services.ServiceDecisionFinanciere;
import com.agrifund.services.ServiceEvaluationRisque;
import com.agrifund.services.ServiceProjectAgricoleChedy;
import com.agrifund.util.SessionManager;
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
import javafx.stage.Stage;
import com.agrifund.entities.EvaluationRisque;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class BanqueDecisionListController {

    @FXML private TableView<DecisionFinanciere> tableDecisions;
    @FXML private TableColumn<DecisionFinanciere, Integer> colId;
    @FXML private TableColumn<DecisionFinanciere, String> colProjet;
    @FXML private TableColumn<DecisionFinanciere, String> colStatut;
    @FXML private TableColumn<DecisionFinanciere, String> colJustification;
    @FXML private TableColumn<DecisionFinanciere, Date> colDateDecision;
    @FXML private TableColumn<DecisionFinanciere, Integer> colIdEvaluation;
    @FXML private TableColumn<DecisionFinanciere, Void> colActions;

    @FXML private TextField tfSearch;
    @FXML private Label lblTotal;
    @FXML private Label lblApprouves;
    @FXML private Label lblEnAttente;
    @FXML private Label lblRejetes;
    @FXML private Label lblStatus;
    @FXML private Label lblBanqueInfo;

    private ServiceDecisionFinanciere serviceDecision;
    private ServiceEvaluationRisque serviceEvaluation;
    private ServiceProjectAgricoleChedy serviceProjet;
    private BanqueService banqueService;

    private Banque currentBanque;
    private ObservableList<DecisionFinanciere> decisionsData;
    private ObservableList<DecisionFinanciere> filteredData;

    @FXML
    public void initialize() {
        serviceDecision = new ServiceDecisionFinanciere();
        serviceEvaluation = new ServiceEvaluationRisque();
        serviceProjet = new ServiceProjectAgricoleChedy();
        decisionsData = FXCollections.observableArrayList();
        filteredData = FXCollections.observableArrayList();

        try {
            banqueService = new BanqueService();
            loadCurrentBanque();
            setupTableColumns();
            loadDecisions();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void loadCurrentBanque() throws SQLException {
        Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
        currentBanque = banqueService.rechercherParUtilisateurId(user.getId());

        if (currentBanque != null && lblBanqueInfo != null) {
            lblBanqueInfo.setText("🏦 " + currentBanque.getNom() + " - Mes Décisions");
        }
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idDecision"));
        colId.setStyle("-fx-alignment: CENTER;");

        // Colonne Projet (via évaluation)
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

        // Colonne Statut avec couleurs
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
                            setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        case "En attente":
                            setStyle("-fx-background-color: #ffc107; -fx-text-fill: #333; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        case "Rejeté":
                            setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Colonne Justification
        colJustification.setCellValueFactory(new PropertyValueFactory<>("justification"));
        colJustification.setCellFactory(column -> new TableCell<>() {
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

        // Colonne Date
        colDateDecision.setCellValueFactory(new PropertyValueFactory<>("dateDecision"));
        colDateDecision.setCellFactory(column -> new TableCell<>() {
            private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt.format(item));
                setAlignment(Pos.CENTER);
            }
        });

        colIdEvaluation.setCellValueFactory(new PropertyValueFactory<>("idEvaluation"));
        colIdEvaluation.setStyle("-fx-alignment: CENTER;");

        // Colonne Actions
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final Button btnResend = new Button("📧");
            private final HBox hbox = new HBox(5, btnEdit, btnDelete, btnResend);
            {
                btnEdit.setStyle("-fx-font-size: 12px; -fx-cursor: hand;");
                btnEdit.setTooltip(new Tooltip("Modifier"));

                btnDelete.setStyle("-fx-font-size: 12px; -fx-cursor: hand;");
                btnDelete.setTooltip(new Tooltip("Supprimer"));

                btnResend.setStyle("-fx-font-size: 12px; -fx-cursor: hand;");
                btnResend.setTooltip(new Tooltip("Renvoyer l'email"));

                hbox.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                btnResend.setOnAction(e -> handleResendEmail(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    private void loadDecisions() {
        try {
            decisionsData.clear();
            // Charger uniquement les décisions de cette banque
            decisionsData.addAll(serviceDecision.afficherParBanque(currentBanque.getUtilisateurId()));
            filteredData.setAll(decisionsData);
            tableDecisions.setItems(filteredData);
            updateStatistics();
            updateStatus("✅ " + decisionsData.size() + " décision(s) chargée(s)");
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
                    // Recherche dans statut
                    if (d.getStatut().toLowerCase().contains(search)) {
                        filteredData.add(d);
                        continue;
                    }

                    // Recherche dans justification
                    if (d.getJustification().toLowerCase().contains(search)) {
                        filteredData.add(d);
                        continue;
                    }

                    // Recherche dans nom projet
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
        updateStatus(filteredData.size() + " résultat(s)");
    }

    @FXML
    private void handleNewDecision() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/fxml/banque/banque-decision.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Décision Financière");
            stage.setScene(new Scene(root));
            stage.setMinWidth(800);
            stage.setMinHeight(650);
            stage.setOnHidden(e -> loadDecisions());
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleViewEvaluations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/fxml/banque/banque-risque-list.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Mes Évaluations de Risque");
            stage.setScene(new Scene(root));
            stage.setMinWidth(1400);
            stage.setMinHeight(850);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void handleEdit(DecisionFinanciere decision) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/fxml/banque/banque-decision.fxml"));
            Parent root = loader.load();

            BanqueDecisionController controller = loader.getController();
            controller.loadDecision(decision.getIdDecision());

            Stage stage = new Stage();
            stage.setTitle("Modifier Décision #" + decision.getIdDecision());
            stage.setScene(new Scene(root));
            stage.setMinWidth(800);
            stage.setMinHeight(650);
            stage.setOnHidden(e -> loadDecisions());
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void handleDelete(DecisionFinanciere decision) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer cette décision ?");
        confirm.setContentText("Cette action est irréversible!\nL'agriculteur ne sera pas notifié de la suppression.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceDecision.supprimerParBanque(decision.getIdDecision(), currentBanque.getUtilisateurId());
                loadDecisions();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Décision supprimée!");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    private void handleResendEmail(DecisionFinanciere decision) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Renvoyer l'email");
        confirm.setHeaderText("Renvoyer la notification à l'agriculteur ?");
        confirm.setContentText("Un nouvel email sera envoyé avec les informations de cette décision.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Utiliser le service pour renvoyer l'email
                // On peut créer une méthode spécifique ou réutiliser la logique
                serviceDecision.modifier(decision); // Cela renverra l'email
                showAlert(Alert.AlertType.INFORMATION, "Succès", "📧 Email renvoyé avec succès!");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        tfSearch.clear();
        loadDecisions();
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
