package controles;

import entities.projectagricole;
import entities.ressourceproject;
import services.projectagricoleCRUD;
import services.ressourceprojectCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ressourceprojectcontroller implements Initializable {

    @FXML private FlowPane ressourcesContainer;
    @FXML private TextField tfSearchRessource;
    @FXML private ComboBox<String> cbFilterTypeList;

    private List<ressourceproject> allRessources = new ArrayList<>();
    private ressourceproject selectedRessource = null;
    private final ressourceprojectCRUD rService = new ressourceprojectCRUD();
    private final projectagricoleCRUD pService = new projectagricoleCRUD();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize filter combo if it exists
        if (cbFilterTypeList != null) {
            cbFilterTypeList.getItems().addAll("Tous", "equipement", "materiaux", "service");
            cbFilterTypeList.setValue("Tous");
        }

        // Listeners for search (only if search field exists)
        if (tfSearchRessource != null) {
            tfSearchRessource.textProperty().addListener((observable, oldValue, newValue) -> updateCardsDisplay());
        }

        if (cbFilterTypeList != null) {
            cbFilterTypeList.setOnAction(event -> updateCardsDisplay());
        }

        // Load data if container exists
        if (ressourcesContainer != null) {
            refreshDataFromDB();
        }
    }

    /**
     * Load data from database
     */
    public void refreshDataFromDB() {
        try {
            allRessources = rService.afficher();
            updateCardsDisplay();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Erreur lors du chargement : " + e.getMessage());
        }
    }

    /**
     * Filter and display cards
     */
    private void updateCardsDisplay() {
        if (ressourcesContainer == null) return;

        ressourcesContainer.getChildren().clear();

        String searchText = tfSearchRessource != null ? tfSearchRessource.getText().toLowerCase() : "";
        String filterType = cbFilterTypeList != null ? cbFilterTypeList.getValue() : "Tous";

        List<ressourceproject> filteredList = allRessources.stream()
                .filter(r -> r.getNomressource().toLowerCase().contains(searchText))
                .filter(r -> filterType.equals("Tous") || r.getTyperessource().equals(filterType))
                .collect(Collectors.toList());

        for (ressourceproject r : filteredList) {
            ressourcesContainer.getChildren().add(createCard(r));
        }
    }

    /**
     * Create a visual card for a resource
     */
    private VBox createCard(ressourceproject r) {
        VBox card = new VBox(8);
        card.getStyleClass().add("project-card");
        card.setPrefWidth(260);

        Label lblNom = new Label(r.getNomressource() + " (Proj #" + r.getIdproject() + ")");
        lblNom.getStyleClass().add("card-title");

        Label lblStatut = new Label(r.getStatut().toUpperCase());
        if (r.getStatut().equals("achete")) lblStatut.getStyleClass().add("status-badge-accepte");
        else lblStatut.getStyleClass().add("status-badge-encours");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topRow = new HBox(lblNom, spacer, lblStatut);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblType = new Label("📦 Type: " + r.getTyperessource());
        lblType.getStyleClass().add("card-info");

        Label lblQte = new Label("🔢 Quantité: " + r.getQuantite());
        lblQte.getStyleClass().add("card-info");

        Label lblCout = new Label("💲 Coût: " + r.getCout() + " DT");
        lblCout.getStyleClass().add("card-info");

        Label lblFournisseur = new Label("🏢 Fournisseur: " + r.getFournisseur());
        lblFournisseur.getStyleClass().add("card-info");

        // Action buttons
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.getStyleClass().add("btn-secondary");
        btnModifier.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
        btnModifier.setOnAction(e -> {
            selectedRessource = r;
            openModifyRessourceForm(null);
        });

        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.getStyleClass().add("btn-danger");
        btnSupprimer.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
        btnSupprimer.setOnAction(e -> {
            selectedRessource = r;
            deleteRessource(null);
        });

        actionBox.getChildren().addAll(btnModifier, btnSupprimer);

        card.getChildren().addAll(topRow, new Separator(), lblType, lblQte, lblCout, lblFournisseur, actionBox);

        return card;
    }

    /**
     * Open the Add Resource form
     */
    @FXML
    public void openAddRessourceForm(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ressourceagricoleadd.fxml"));
            Parent root = loader.load();

            ressourceprojectaddcontroller addController = loader.getController();
            addController.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Nouvelle Ressource");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            refreshDataFromDB();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Open the Modify Resource form
     */
    @FXML
    void openModifyRessourceForm(ActionEvent event) {
        if (selectedRessource == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une ressource à modifier.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ressourceagricolemodify.fxml"));
            Parent root = loader.load();

            ressourceprojectmodifycontroller modifyController = loader.getController();
            modifyController.setRessource(selectedRessource);
            modifyController.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier la Ressource");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            refreshDataFromDB();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Delete a resource
     */
    @FXML
    void deleteRessource(ActionEvent event) {
        if (selectedRessource == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une ressource à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cette ressource ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                rService.supprimer(selectedRessource.getIdressource());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource supprimée !");
                selectedRessource = null;
                refreshDataFromDB();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
            }
        }
    }

    /**
     * Navigate to Projects view
     */
    @FXML
    void goToProjects(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/projectagricole.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
        } catch (IOException e) {
            e.printStackTrace();
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