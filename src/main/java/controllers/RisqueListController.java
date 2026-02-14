package controllers;

import entities.EvaluationRisque;
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
import services.ServiceEvaluationRisque;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class RisqueListController {

    @FXML private TableView<EvaluationRisque> tableEvaluations;
    @FXML private TableColumn<EvaluationRisque, Integer> colId;
    @FXML private TableColumn<EvaluationRisque, Integer> colIdProjet;
    @FXML private TableColumn<EvaluationRisque, Integer> colScoreGlobal;
    @FXML private TableColumn<EvaluationRisque, String> colNiveauRisque;
    @FXML private TableColumn<EvaluationRisque, String> colFiabiliteDonnees;
    @FXML private TableColumn<EvaluationRisque, String> colFacteurPrincipal;
    @FXML private TableColumn<EvaluationRisque, String> colRecommandation;
    @FXML private TableColumn<EvaluationRisque, Date> colDateEvaluation;
    @FXML private TableColumn<EvaluationRisque, Void> colActions;

    @FXML private TextField tfSearch;
    @FXML private Label lblTotal;
    @FXML private Label lblFaible;
    @FXML private Label lblMoyen;
    @FXML private Label lblEleve;
    @FXML private Label lblCritique;
    @FXML private Label lblStatus;

    private ServiceEvaluationRisque service;
    private ObservableList<EvaluationRisque> evaluationsData;
    private ObservableList<EvaluationRisque> filteredData;

    @FXML
    public void initialize() {
        service = new ServiceEvaluationRisque();
        evaluationsData = FXCollections.observableArrayList();
        filteredData = FXCollections.observableArrayList();

        setupTableColumns();
        loadEvaluations();
    }

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
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);

                    switch (item) {
                        case "Faible":
                            setStyle("-fx-background-color: #B2D944; -fx-text-fill: #133D03; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        case "Moyen":
                            setStyle("-fx-background-color: #E1B323; -fx-text-fill: #133D03; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        case "Élevé":
                            setStyle("-fx-background-color: #E17D23; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        case "Critique":
                            setStyle("-fx-background-color: #D94444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                            break;
                        default:
                            setStyle("");
                    }
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

        colRecommandation.setCellValueFactory(cellData -> {
            int recommandation = cellData.getValue().getRecommandation();
            String recommandationString = getRecommandationString(recommandation);
            return new javafx.beans.property.SimpleStringProperty(recommandationString);
        });
        colRecommandation.setStyle("-fx-alignment: CENTER;");

        colDateEvaluation.setCellValueFactory(new PropertyValueFactory<>("dateEvaluation"));
        colDateEvaluation.setCellFactory(column -> new TableCell<EvaluationRisque, Date>() {
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

        colActions.setCellFactory(column -> new TableCell<EvaluationRisque, Void>() {
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
                    EvaluationRisque evaluation = getTableView().getItems().get(getIndex());
                    handleEdit(evaluation);
                });

                btnDelete.setOnAction(event -> {
                    EvaluationRisque evaluation = getTableView().getItems().get(getIndex());
                    handleDelete(evaluation);
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

    private String getRecommandationString(int recommandation) {
        switch (recommandation) {
            case 0: return "Aucune";
            case 1: return "Surveillance";
            case 2: return "Action immédiate";
            default: return "Inconnu";
        }
    }

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
            e.printStackTrace();
        }
    }

    private void updateStatistics() {
        int total = evaluationsData.size();
        int faible = (int) evaluationsData.stream().filter(d -> "Faible".equals(d.getNiveauRisque())).count();
        int moyen = (int) evaluationsData.stream().filter(d -> "Moyen".equals(d.getNiveauRisque())).count();
        int eleve = (int) evaluationsData.stream().filter(d -> "Élevé".equals(d.getNiveauRisque())).count();
        int critique = (int) evaluationsData.stream().filter(d -> "Critique".equals(d.getNiveauRisque())).count();

        lblTotal.setText(String.valueOf(total));
        lblFaible.setText(String.valueOf(faible));
        lblMoyen.setText(String.valueOf(moyen));
        lblEleve.setText(String.valueOf(eleve));
        lblCritique.setText(String.valueOf(critique));
    }

    @FXML
    private void handleSearch() {
        String searchText = tfSearch.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            filteredData.setAll(evaluationsData);
        } else {
            filteredData.clear();
            for (EvaluationRisque e : evaluationsData) {
                if (String.valueOf(e.getIdEvaluation()).contains(searchText) ||
                        String.valueOf(e.getIdProjet()).contains(searchText) ||
                        String.valueOf(e.getScoreGlobal()).contains(searchText) ||
                        e.getNiveauRisque().toLowerCase().contains(searchText) ||
                        e.getFiabiliteDonnees().toLowerCase().contains(searchText) ||
                        e.getFacteurPrincipal().toLowerCase().contains(searchText) ||
                        getRecommandationString(e.getRecommandation()).toLowerCase().contains(searchText)) {
                    filteredData.add(e);
                }
            }
        }

        tableEvaluations.setItems(filteredData);
        updateStatus(filteredData.size() + " résultat(s) trouvé(s)");
    }

    @FXML
    private void handleNewEvaluation() {
        openEvaluationForm(null);
    }

    private void handleEdit(EvaluationRisque evaluation) {
        openEvaluationForm(evaluation);
    }

    private void handleDelete(EvaluationRisque evaluation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'évaluation #" + evaluation.getIdEvaluation() + " ?");
        alert.setContentText("Cette action est irréversible!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(evaluation.getIdEvaluation());
                evaluationsData.remove(evaluation);
                filteredData.remove(evaluation);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation supprimée avec succès!");
                updateStatistics();
                updateStatus("Évaluation supprimée");

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void openEvaluationForm(EvaluationRisque evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Risque.fxml"));
            Parent root = loader.load();

            RisqueController controller = loader.getController();

            if (evaluation != null) {
                controller.loadEvaluation(evaluation.getIdEvaluation());
            }

            Stage stage = new Stage();
            stage.setTitle(evaluation == null ? "Nouvelle Évaluation" : "Modifier Évaluation #" + evaluation.getIdEvaluation());
            stage.setScene(new Scene(root));
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            stage.setOnHidden(e -> loadEvaluations()); // Recharge les données après la fermeture de la fenêtre

            stage.showAndWait(); // Utilisez showAndWait() pour attendre la fermeture de la fenêtre

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadEvaluations();
    }

    @FXML
    private void handleViewDecisionList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DecisionList.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Liste des Décisions Financières");
            stage.setScene(new Scene(root));
            stage.setMinWidth(1400);
            stage.setMinHeight(800);
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la liste des décisions financières: " + e.getMessage());
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
