package controles;

import entities.projectagricole;
import entities.ressourceproject;
import services.projectagricoleCRUD;
import services.ressourceprojectCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ressourceprojectcontroller implements Initializable {

    @FXML private FlowPane ressourcesContainer;
    @FXML private TextField tfSearchRessource;
    @FXML private ComboBox<String> cbFilterTypeList;

    // Statistics Labels
    @FXML private Label lblTotalRessources;
    @FXML private Label lblEquipments;
    @FXML private Label lblMaterials;
    @FXML private Label lblServices;

    private List<ressourceproject> allRessources = new ArrayList<>();
    private ressourceproject selectedRessource = null;
    private final ressourceprojectCRUD rService = new ressourceprojectCRUD();
    private final projectagricoleCRUD pService = new projectagricoleCRUD();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize filter combo if it exists
        if (cbFilterTypeList != null) {
            cbFilterTypeList.getItems().addAll("Tous les types", "equipement", "materiaux", "service");
            cbFilterTypeList.setValue("Tous les types");
        }

        // Setup listeners
        setupSearchListener();
        setupFilterListener();

        // Load data if container exists
        if (ressourcesContainer != null) {
            refreshDataFromDB();
        }
    }

    /**
     * Sets up real-time search functionality
     */
    private void setupSearchListener() {
        if (tfSearchRessource != null) {
            tfSearchRessource.textProperty().addListener((observable, oldValue, newValue) -> {
                updateCardsDisplay();
            });
        }
    }

    /**
     * Sets up filter dropdown functionality
     */
    private void setupFilterListener() {
        if (cbFilterTypeList != null) {
            cbFilterTypeList.setOnAction(event -> {
                updateCardsDisplay();
            });
        }
    }

    /**
     * Load data from database and update display
     */
    public void refreshDataFromDB() {
        try {
            allRessources = rService.afficher();
            updateCardsDisplay();
            updateStatistics();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD",
                    "Erreur lors du chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates all statistics displayed in the dashboard
     */
    private void updateStatistics() {
        try {
            int totalCount = allRessources.size();
            long equipmentCount = allRessources.stream()
                    .filter(r -> "equipement".equals(r.getTyperessource()))
                    .count();
            long materialsCount = allRessources.stream()
                    .filter(r -> "materiaux".equals(r.getTyperessource()))
                    .count();
            long servicesCount = allRessources.stream()
                    .filter(r -> "service".equals(r.getTyperessource()))
                    .count();

            // Update stat labels if they exist
            if (lblTotalRessources != null) {
                lblTotalRessources.setText(String.valueOf(totalCount));
            }
            if (lblEquipments != null) {
                lblEquipments.setText(String.valueOf(equipmentCount));
            }
            if (lblMaterials != null) {
                lblMaterials.setText(String.valueOf(materialsCount));
            }
            if (lblServices != null) {
                lblServices.setText(String.valueOf(servicesCount));
            }

        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Filter and display cards
     */
    private void updateCardsDisplay() {
        if (ressourcesContainer == null) return;

        ressourcesContainer.getChildren().clear();

        String searchText = tfSearchRessource != null ? tfSearchRessource.getText().toLowerCase() : "";
        String filterType = cbFilterTypeList != null ? cbFilterTypeList.getValue() : "Tous les types";

        List<ressourceproject> filteredList = allRessources.stream()
                .filter(r -> {
                    // Search filter
                    boolean matchesSearch = searchText.isEmpty()
                            || r.getNomressource().toLowerCase().contains(searchText);

                    // Type filter
                    boolean matchesType = filterType.equals("Tous les types")
                            || r.getTyperessource().equals(filterType);

                    return matchesSearch && matchesType;
                })
                .collect(Collectors.toList());

        for (ressourceproject r : filteredList) {
            ressourcesContainer.getChildren().add(createEnhancedResourceCard(r));
        }
    }

    /**
     * Create an enhanced visual card for a resource
     */
    private VBox createEnhancedResourceCard(ressourceproject resource) {
        VBox card = new VBox(12);
        card.getStyleClass().add("project-card");
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setPadding(new Insets(18));

        // Card Header with Icon and Resource Name
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getResourceIcon(resource.getTyperessource()));
        icon.setStyle("-fx-font-size: 24px;");

        Label title = new Label(resource.getNomressource());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);
        title.setMaxWidth(220);

        header.getChildren().addAll(icon, title);

        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));

        // Resource Details
        VBox details = new VBox(8);

        // Project ID
        HBox projectRow = createInfoRow("🔗", "Projet", "#" + resource.getIdproject());

        // Type
        HBox typeBox = createInfoRow("📦", "Type", capitalizeType(resource.getTyperessource()));

        // Quantity
        HBox quantityBox = createInfoRow("🔢", "Quantité", resource.getQuantite() + " unités");

        // Cost
        HBox costBox = createInfoRow("💰", "Coût",
                String.format("%,.2f DT", resource.getCout()));

        // Supplier
        HBox supplierBox = createInfoRow("🏢", "Fournisseur", resource.getFournisseur());

        details.getChildren().addAll(projectRow, typeBox, quantityBox, costBox, supplierBox);

        // Status Badge
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label("Statut:");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6C757D; -fx-font-weight: 600;");

        Label badge = new Label(capitalizeStatus(resource.getStatut()));
        badge.getStyleClass().add(getStatusBadgeClass(resource.getStatut()));

        statusBox.getChildren().addAll(statusLabel, badge);

        // Action Buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(10, 0, 0, 0));

        Button btnEdit = new Button("✏️");
        btnEdit.getStyleClass().add("btn-secondary");
        btnEdit.setStyle("-fx-min-width: 35; -fx-min-height: 35; -fx-font-size: 14px;");
        btnEdit.setOnAction(e -> {
            selectedRessource = resource;
            openModifyRessourceForm(null);
        });
        btnEdit.setTooltip(new Tooltip("Modifier la ressource"));

        Button btnDelete = new Button("🗑️");
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setStyle("-fx-min-width: 35; -fx-min-height: 35; -fx-font-size: 14px;");
        btnDelete.setOnAction(e -> {
            selectedRessource = resource;
            deleteRessource(null);
        });
        btnDelete.setTooltip(new Tooltip("Supprimer la ressource"));

        actions.getChildren().addAll(btnEdit, btnDelete);

        // Add all elements to card
        card.getChildren().addAll(header, separator, details, statusBox, actions);

        return card;
    }

    /**
     * Creates an info row with icon, label, and value
     */
    private HBox createInfoRow(String emoji, String label, String value) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 16px;");

        Label labelText = new Label(label + ":");
        labelText.getStyleClass().add("card-info-label");

        Label valueText = new Label(value);
        valueText.getStyleClass().add("card-info-value");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(icon, labelText, spacer, valueText);

        return row;
    }

    /**
     * Returns appropriate icon based on resource type
     */
    private String getResourceIcon(String type) {
        switch (type.toLowerCase()) {
            case "equipement": return "⚙️";
            case "materiaux": return "🧱";
            case "service": return "🔧";
            default: return "📦";
        }
    }

    /**
     * Returns CSS class for status badge
     */
    private String getStatusBadgeClass(String status) {
        switch (status.toLowerCase()) {
            case "achete": return "status-badge-achete";
            case "prevu": return "status-badge-prevu";
            default: return "status-badge-prevu";
        }
    }

    /**
     * Capitalizes status text for display
     */
    private String capitalizeStatus(String status) {
        switch (status.toLowerCase()) {
            case "achete": return "Acheté";
            case "prevu": return "Prévu";
            default: return status;
        }
    }

    /**
     * Capitalizes type text for display
     */
    private String capitalizeType(String type) {
        switch (type.toLowerCase()) {
            case "equipement": return "Équipement";
            case "materiaux": return "Matériaux";
            case "service": return "Service";
            default: return type;
        }
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
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Open the Modify Resource form
     */
    @FXML
    void openModifyRessourceForm(ActionEvent event) {
        if (selectedRessource == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner une ressource à modifier.");
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
            selectedRessource = null;
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Delete a resource
     */
    @FXML
    void deleteRessource(ActionEvent event) {
        if (selectedRessource == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner une ressource à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer la ressource");
        confirm.setContentText("Voulez-vous vraiment supprimer la ressource \"" +
                selectedRessource.getNomressource() + "\" ?\n\nCette action est irréversible.");

        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.OK) {
            try {
                rService.supprimer(selectedRessource.getIdressource());
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Ressource supprimée avec succès !");
                selectedRessource = null;
                refreshDataFromDB();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur SQL",
                        "Impossible de supprimer : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Navigate to Projects view
     */
    @FXML
    void goToProjects(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/projectagricole.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Gestion des Projets Agricoles");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la vue des projets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}