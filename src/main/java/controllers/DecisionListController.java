package controllers;

import entities.DecisionFinanciere;
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
import services.ServiceDecisionFinanciere;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class DecisionListController {

    @FXML private TableView<DecisionFinanciere> tableDecisions;
    @FXML private TableColumn<DecisionFinanciere, Integer> colId;
    @FXML private TableColumn<DecisionFinanciere, Integer> colIdProjet;
    @FXML private TableColumn<DecisionFinanciere, String> colStatut;
    @FXML private TableColumn<DecisionFinanciere, Date> colDate;
    @FXML private TableColumn<DecisionFinanciere, String> colJustification;
    @FXML private TableColumn<DecisionFinanciere, Void> colActions;

    @FXML private TextField tfSearch;
    @FXML private Label lblTotal;
    @FXML private Label lblApprouves;
    @FXML private Label lblEnAttente;
    @FXML private Label lblStatus;

    private ServiceDecisionFinanciere service;
    private ObservableList<DecisionFinanciere> decisionsData;
    private ObservableList<DecisionFinanciere> filteredData;

    @FXML
    public void initialize() {
        service = new ServiceDecisionFinanciere();
        decisionsData = FXCollections.observableArrayList();
        filteredData = FXCollections.observableArrayList();

        setupTableColumns();
        loadDecisions();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idDecision"));
        colId.setStyle("-fx-alignment: CENTER;");

        colIdProjet.setCellValueFactory(new PropertyValueFactory<>("idProjet"));
        colIdProjet.setStyle("-fx-alignment: CENTER;");

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(column -> new TableCell<DecisionFinanciere, String>() {
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
                            setStyle("-fx-background-color: #B2D944; -fx-text-fill: #133D03; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        case "En attente":
                            setStyle("-fx-background-color: #E1B323; -fx-text-fill: #133D03; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        case "Rejeté":
                            setStyle("-fx-background-color: #848A86; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDecision"));
        colDate.setCellFactory(column -> new TableCell<DecisionFinanciere, Date>() {
            private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                    setAlignment(Pos.CENTER);
                }
            }
        });

        colJustification.setCellValueFactory(new PropertyValueFactory<>("justification"));
        colJustification.setCellFactory(column -> new TableCell<DecisionFinanciere, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    String displayText = item.length() > 50 ? item.substring(0, 50) + "..." : item;
                    setText(displayText);

                    Tooltip tooltip = new Tooltip(item);
                    tooltip.setWrapText(true);
                    tooltip.setMaxWidth(400);
                    setTooltip(tooltip);
                }
            }
        });

        colActions.setCellFactory(column -> new TableCell<DecisionFinanciere, Void>() {
            private final Button btnEdit = new Button("✏️ Modifier");
            private final Button btnDelete = new Button("🗑️ Supprimer");
            private final HBox hbox = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("button-primary");
                btnEdit.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");

                btnDelete.getStyleClass().add("button-secondary");
                btnDelete.setStyle("-fx-font-size: 11px; -fx-padding: 5 10;");

                hbox.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(event -> {
                    DecisionFinanciere decision = getTableView().getItems().get(getIndex());
                    handleEdit(decision);
                });

                btnDelete.setOnAction(event -> {
                    DecisionFinanciere decision = getTableView().getItems().get(getIndex());
                    handleDelete(decision);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
    }

    private void loadDecisions() {
        try {
            decisionsData.clear();
            decisionsData.addAll(service.afficher());
            filteredData.setAll(decisionsData);
            tableDecisions.setItems(filteredData);

            updateStatistics();
            updateStatus("Données chargées - " + decisionsData.size() + " décision(s)");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les décisions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStatistics() {
        int total = decisionsData.size();
        int approuves = (int) decisionsData.stream().filter(d -> "Approuvé".equals(d.getStatut())).count();
        int enAttente = (int) decisionsData.stream().filter(d -> "En attente".equals(d.getStatut())).count();

        lblTotal.setText(String.valueOf(total));
        lblApprouves.setText(String.valueOf(approuves));
        lblEnAttente.setText(String.valueOf(enAttente));
    }

    @FXML
    private void handleSearch() {
        String searchText = tfSearch.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            filteredData.setAll(decisionsData);
        } else {
            filteredData.clear();
            for (DecisionFinanciere d : decisionsData) {
                if (String.valueOf(d.getIdDecision()).contains(searchText) ||
                        String.valueOf(d.getIdProjet()).contains(searchText) ||
                        d.getStatut().toLowerCase().contains(searchText) ||
                        d.getJustification().toLowerCase().contains(searchText)) {
                    filteredData.add(d);
                }
            }
        }

        tableDecisions.setItems(filteredData);
        updateStatus(filteredData.size() + " résultat(s) trouvé(s)");
    }

    @FXML
    private void handleNewDecision() {
        openDecisionForm(null);
    }

    private void handleEdit(DecisionFinanciere decision) {
        openDecisionForm(decision);
    }

    private void handleDelete(DecisionFinanciere decision) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la décision #" + decision.getIdDecision() + " ?");
        alert.setContentText("Cette action est irréversible!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(decision.getIdDecision());
                decisionsData.remove(decision);
                filteredData.remove(decision);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Décision supprimée avec succès!");
                updateStatistics();
                updateStatus("Décision supprimée");

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void openDecisionForm(DecisionFinanciere decision) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Decesion.fxml"));
            Parent root = loader.load();

            DecesionController controller = loader.getController();

            if (decision != null) {
                controller.loadDecision(decision.getIdDecision());
            }

            Stage stage = new Stage();
            stage.setTitle(decision == null ? "Nouvelle Décision" : "Modifier Décision #" + decision.getIdDecision());
            stage.setScene(new Scene(root));
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            stage.setOnHidden(e -> loadDecisions()); // Recharge les données après la fermeture de la fenêtre

            stage.showAndWait(); // Utilisez showAndWait() pour attendre la fermeture de la fenêtre

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadDecisions();
    }

    @FXML
    private void handleStats() {
        String stats = String.format(
                "📊 STATISTIQUES DES DÉCISIONS\n\n" +
                        "Total: %d\n" +
                        "Approuvés: %d (%.1f%%)\n" +
                        "En Attente: %d (%.1f%%)",
                decisionsData.size(),
                Integer.parseInt(lblApprouves.getText()),
                decisionsData.isEmpty() ? 0 : (Integer.parseInt(lblApprouves.getText()) * 100.0 / decisionsData.size()),
                Integer.parseInt(lblEnAttente.getText()),
                decisionsData.isEmpty() ? 0 : (Integer.parseInt(lblEnAttente.getText()) * 100.0 / decisionsData.size())
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistiques");
        alert.setHeaderText(null);
        alert.setContentText(stats);
        alert.showAndWait();
    }

    @FXML
    private void handleViewRisqueList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RisqueList.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Liste des Évaluations de Risque");
            stage.setScene(new Scene(root));
            stage.setMinWidth(1400);
            stage.setMinHeight(800);
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la liste des évaluations de risque: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStatus(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
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
