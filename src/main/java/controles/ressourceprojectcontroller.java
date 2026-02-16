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

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

        // Action Buttons - ENHANCED
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(12, 0, 0, 0));

        // Details Button
        Button btnDetails = new Button("ℹ️ Détails");
        btnDetails.getStyleClass().add("btn-info");
        btnDetails.setStyle(
                "-fx-min-width: 85; " +
                        "-fx-min-height: 32; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 6 12;"
        );
        btnDetails.setOnAction(e -> showResourceDetails(resource));
        btnDetails.setTooltip(new Tooltip("Voir tous les détails de la ressource"));

        // Edit Button with enhanced styling
        Button btnEdit = new Button("✎ Modifier");
        btnEdit.getStyleClass().add("btn-secondary");
        btnEdit.setStyle(
                "-fx-min-width: 95; " +
                        "-fx-min-height: 32; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 6 12;"
        );
        btnEdit.setOnAction(e -> {
            selectedRessource = resource;
            openModifyRessourceForm(null);
        });
        btnEdit.setTooltip(new Tooltip("Modifier les informations de la ressource"));

        // Delete Button with enhanced styling
        Button btnDelete = new Button("✖ Supprimer");
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setStyle(
                "-fx-min-width: 100; " +
                        "-fx-min-height: 32; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 6 12;"
        );
        btnDelete.setOnAction(e -> {
            selectedRessource = resource;
            deleteRessource(null);
        });
        btnDelete.setTooltip(new Tooltip("Supprimer cette ressource définitivement"));

        actions.getChildren().addAll(btnDetails, btnEdit, btnDelete);

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
            case "equipement": return "🚜";
            case "materiaux": return "⚙";
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
            stage.centerOnScreen();
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
            stage.getScene().setRoot(root);
            stage.setTitle("Gestion des Projets Agricoles");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la vue des projets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shows detailed information about a resource in a dialog
     */
    private void showResourceDetails(ressourceproject resource) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la Ressource");
        dialog.setResizable(true);

        // Main content container
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #F8F9FA;");
        content.setPrefWidth(500);

        // Header Section
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle(
                "-fx-background-color: linear-gradient(to right, #2D6A4F, #40916C);" +
                        "-fx-padding: 20;" +
                        "-fx-background-radius: 8;"
        );

        Label headerIcon = new Label(getResourceIcon(resource.getTyperessource()));
        headerIcon.setStyle("-fx-font-size: 36px;");

        VBox headerText = new VBox(3);
        Label resourceName = new Label(resource.getNomressource());
        resourceName.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        Label resourceId = new Label("ID: " + resource.getIdressource());
        resourceId.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.85);");

        headerText.getChildren().addAll(resourceName, resourceId);
        headerBox.getChildren().addAll(headerIcon, headerText);

        // Status Row with Badge
        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.setPadding(new Insets(15, 0, 10, 0));

        Label statusTitle = new Label("Statut actuel:");
        statusTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #6C757D;");

        Label statusBadge = new Label(capitalizeStatus(resource.getStatut()));
        statusBadge.getStyleClass().add(getStatusBadgeClass(resource.getStatut()));
        statusBadge.setStyle(statusBadge.getStyle() + "-fx-font-size: 13px; -fx-padding: 6 16;");

        statusRow.getChildren().addAll(statusTitle, statusBadge);

        // Details Grid
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(12);
        detailsGrid.setPadding(new Insets(10, 0, 10, 0));
        detailsGrid.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 20;"
        );

        // Add detail rows
        addDetailRow(detailsGrid, 0, "🔗 Projet associé", "#" + resource.getIdproject());
        addDetailRow(detailsGrid, 1, "📦 Type de ressource", capitalizeType(resource.getTyperessource()));
        addDetailRow(detailsGrid, 2, "🔢 Quantité", resource.getQuantite() + " unités");
        addDetailRow(detailsGrid, 3, "💰 Coût unitaire", String.format("%,.2f DT", resource.getCout()));
        addDetailRow(detailsGrid, 4, "💵 Coût total", String.format("%,.2f DT",
                resource.getCout().multiply(new java.math.BigDecimal(resource.getQuantite()))));
        addDetailRow(detailsGrid, 5, "🏢 Fournisseur", resource.getFournisseur());

        // Separator
        Separator sep = new Separator();
        sep.setPadding(new Insets(5, 0, 5, 0));

        // Status Explanation Box
        VBox statusExplanation = new VBox(8);
        statusExplanation.setStyle(
                "-fx-background-color: #E8F5E9;" +
                        "-fx-border-color: #81C784;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 15;"
        );

        Label noteTitle = new Label("ℹ️ Information");
        noteTitle.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #2D6A4F;"
        );

        Label noteText = new Label(getStatusExplanation(resource.getStatut()));
        noteText.setWrapText(true);
        noteText.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #133D03;"
        );

        statusExplanation.getChildren().addAll(noteTitle, noteText);

        // Add all to content
        content.getChildren().addAll(
                headerBox,
                statusRow,
                detailsGrid,
                sep,
                statusExplanation
        );

        // Set content
        dialog.getDialogPane().setContent(content);

        // Add Close button
        ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        // Style the button
        Button closeBtn = (Button) dialog.getDialogPane().lookupButton(closeButton);
        closeBtn.setStyle(
                "-fx-background-color: #076A39;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 30;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        );

        // Show dialog
        dialog.showAndWait();
    }

    /**
     * Helper method to add detail rows to grid
     */
    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-text-fill: #848A86;"
        );

        Label lblValue = new Label(value);
        lblValue.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #076A39;"
        );

        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }

    /**
     * Get status explanation text
     */
    private String getStatusExplanation(String statut) {
        switch (statut.toLowerCase()) {
            case "achete":
                return "Cette ressource a été achetée et est disponible pour utilisation dans le projet.";
            case "prevu":
                return "Cette ressource est prévue pour achat. Elle n'a pas encore été acquise.";
            default:
                return "Statut de la ressource: " + capitalizeStatus(statut);
        }
    }

    /**
     * Shows details of the currently selected resource from toolbar button
     */
    @FXML
    void showSelectedResourceDetails(ActionEvent event) {
        if (selectedRessource == null) {
            // No resource selected - show selection dialog
            showResourceSelectionDialog();
        } else {
            // Resource is already selected - show its details
            showResourceDetails(selectedRessource);
        }
    }

    /**
     * Shows a dialog to select a resource when clicking Details without selection
     */
    private void showResourceSelectionDialog() {
        if (allRessources == null || allRessources.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucune ressource",
                    "Aucune ressource disponible. Veuillez d'abord créer une ressource.");
            return;
        }

        // Create selection dialog
        Dialog<ressourceproject> dialog = new Dialog<>();
        dialog.setTitle("Sélectionner une Ressource");
        dialog.setHeaderText("Choisissez une ressource pour voir ses détails");

        // Create list view with all resources
        ListView<ressourceproject> listView = new ListView<>();
        listView.getItems().addAll(allRessources);
        listView.setPrefHeight(400);
        listView.setPrefWidth(550);

        // Custom cell factory to display resource info nicely
        listView.setCellFactory(param -> new ListCell<ressourceproject>() {
            @Override
            protected void updateItem(ressourceproject resource, boolean empty) {
                super.updateItem(resource, empty);
                if (empty || resource == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox cell = new HBox(15);
                    cell.setAlignment(Pos.CENTER_LEFT);
                    cell.setPadding(new Insets(10));

                    // Icon based on type
                    Label icon = new Label(getResourceIcon(resource.getTyperessource()));
                    icon.setStyle("-fx-font-size: 24px;");

                    // Resource info
                    VBox info = new VBox(5);
                    Label name = new Label(resource.getNomressource());
                    name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #133D03;");

                    Label details = new Label(String.format("ID: %d | Type: %s | Quantité: %d | Coût: %,.2f DT",
                            resource.getIdressource(),
                            capitalizeType(resource.getTyperessource()),
                            resource.getQuantite(),
                            resource.getCout()));
                    details.setStyle("-fx-font-size: 11px; -fx-text-fill: #848A86;");

                    info.getChildren().addAll(name, details);

                    // Status badge
                    Label badge = new Label(capitalizeStatus(resource.getStatut()));
                    badge.getStyleClass().add(getStatusBadgeClass(resource.getStatut()));
                    badge.setStyle(badge.getStyle() + "-fx-font-size: 11px; -fx-padding: 4 12;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    cell.getChildren().addAll(icon, info, spacer, badge);
                    setGraphic(cell);
                }
            }
        });

        // Set initial selection to first resource
        if (!allRessources.isEmpty()) {
            listView.getSelectionModel().select(0);
        }

        // Dialog content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label instruction = new Label("Double-cliquez sur une ressource ou sélectionnez et cliquez sur OK");
        instruction.setStyle("-fx-font-size: 12px; -fx-text-fill: #6C757D;");

        content.getChildren().addAll(instruction, listView);
        dialog.getDialogPane().setContent(content);

        // Add buttons
        ButtonType okButton = new ButtonType("Voir Détails", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        // Enable OK button only when resource is selected
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(okButton);
        okBtn.setDisable(listView.getSelectionModel().getSelectedItem() == null);
        listView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> okBtn.setDisable(newVal == null)
        );

        // Style OK button
        okBtn.setStyle(
                "-fx-background-color: #076A39;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 20;" +
                        "-fx-background-radius: 6;"
        );

        // Handle double-click
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
                ressourceproject selected = listView.getSelectionModel().getSelectedItem();
                dialog.setResult(selected);
                dialog.close();
            }
        });

        // Set result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        // Show dialog and handle result
        dialog.showAndWait().ifPresent(resource -> {
            selectedRessource = resource;
            showResourceDetails(resource);
        });
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

    @FXML
    void exportToPDF(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.setInitialFileName("Ressources_Agricoles_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );

            File file = fileChooser.showSaveDialog(ressourcesContainer.getScene().getWindow());

            if (file != null) {
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                Color headerColor = new DeviceRgb(45, 106, 79);
                Color lightGreen = new DeviceRgb(216, 243, 220);

                // Title
                Paragraph title = new Paragraph("INVENTAIRE DES RESSOURCES AGRICOLES")
                        .setFontSize(20)
                        .setBold()
                        .setFontColor(headerColor)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(10);
                document.add(title);

                // Date and Statistics
                Paragraph info = new Paragraph(
                        "Généré le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) +
                                "\nTotal de ressources: " + allRessources.size()
                ).setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(20);
                document.add(info);

                // Statistics Summary
                Paragraph stats = new Paragraph("STATISTIQUES PAR TYPE")
                        .setFontSize(14)
                        .setBold()
                        .setFontColor(headerColor)
                        .setMarginBottom(10);
                document.add(stats);

                Table statsTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                        .useAllAvailableWidth()
                        .setMarginBottom(20);

                long equipmentCount = allRessources.stream()
                        .filter(r -> "equipement".equals(r.getTyperessource())).count();
                long materialsCount = allRessources.stream()
                        .filter(r -> "materiaux".equals(r.getTyperessource())).count();
                long servicesCount = allRessources.stream()
                        .filter(r -> "service".equals(r.getTyperessource())).count();

                // ✅✅✅ FINAL FIX - Convert BigDecimal to double
                double totalCost = allRessources.stream()
                        .filter(r -> r.getCout() != null)
                        .mapToDouble(r -> r.getCout().doubleValue())
                        .sum();

                statsTable.addCell(createStatsCell("Total Ressources",
                        String.valueOf(allRessources.size()), lightGreen));
                statsTable.addCell(createStatsCell("Équipements",
                        String.valueOf(equipmentCount), new DeviceRgb(255, 229, 204)));
                statsTable.addCell(createStatsCell("Matériaux",
                        String.valueOf(materialsCount), new DeviceRgb(229, 244, 255)));
                statsTable.addCell(createStatsCell("Services",
                        String.valueOf(servicesCount), new DeviceRgb(243, 229, 245)));

                document.add(statsTable);

                // Cost Summary
                Paragraph costTitle = new Paragraph("COÛT TOTAL: " + String.format("%.2f DT", totalCost))
                        .setFontSize(12)
                        .setBold()
                        .setFontColor(headerColor)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(15);
                document.add(costTitle);

                // Resources List Title
                Paragraph listTitle = new Paragraph("LISTE DÉTAILLÉE DES RESSOURCES")
                        .setFontSize(14)
                        .setBold()
                        .setFontColor(headerColor)
                        .setMarginBottom(10);
                document.add(listTitle);

                // Resources Table
                Table table = new Table(UnitValue.createPercentArray(new float[]{8, 20, 12, 10, 13, 18, 12, 7}))
                        .useAllAvailableWidth();

                // Table Headers
                String[] headers = {"ID", "Nom", "Type", "Quantité", "Coût (DT)", "Fournisseur", "Projet", "Statut"};
                for (String header : headers) {
                    Cell headerCell = new Cell()
                            .add(new Paragraph(header).setBold().setFontSize(9))
                            .setBackgroundColor(headerColor)
                            .setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setPadding(6);
                    table.addHeaderCell(headerCell);
                }

                // Table Data
                for (ressourceproject r : allRessources) {
                    Color rowColor = ColorConstants.WHITE;
                    if ("equipement".equals(r.getTyperessource())) {
                        rowColor = new DeviceRgb(255, 245, 230);
                    } else if ("materiaux".equals(r.getTyperessource())) {
                        rowColor = new DeviceRgb(229, 244, 255);
                    } else if ("service".equals(r.getTyperessource())) {
                        rowColor = new DeviceRgb(243, 229, 245);
                    }

                    table.addCell(createDataCell(String.valueOf(r.getIdressource()), rowColor));
                    table.addCell(createDataCell(r.getNomressource(), rowColor));
                    table.addCell(createDataCell(capitalizeType(r.getTyperessource()), rowColor));
                    table.addCell(createDataCell(String.valueOf(r.getQuantite()), rowColor));

                    // Also fix the cost display in the table
                    String costStr = r.getCout() != null ? String.format("%.2f", r.getCout()) : "0.00";
                    table.addCell(createDataCell(costStr, rowColor));

                    table.addCell(createDataCell(r.getFournisseur(), rowColor));
                    table.addCell(createDataCell("#" + r.getIdproject(), rowColor));

                    String statutText = capitalizeStatus(r.getStatut());
                    Cell statusCell = createDataCell(statutText, rowColor).setBold();
                    if ("achete".equals(r.getStatut())) {
                        statusCell.setFontColor(new DeviceRgb(64, 145, 108));
                    }
                    table.addCell(statusCell);
                }

                document.add(table);

                // Footer
                Paragraph footer = new Paragraph(
                        "\n\nDocument généré automatiquement par le Système de Gestion des Ressources Agricoles"
                ).setFontSize(8)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(ColorConstants.GRAY);
                document.add(footer);

                document.close();

                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Le fichier PDF a été généré avec succès !\n\nEmplacement: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la génération du PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper methods remain the same
    private Cell createStatsCell(String label, String value, Color bgColor) {
        Paragraph content = new Paragraph()
                .add(new Paragraph(label).setFontSize(9).setMarginBottom(2))
                .add(new Paragraph(value).setBold().setFontSize(14));

        return new Cell()
                .add(content)
                .setBackgroundColor(bgColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(10);
    }

    private Cell createDataCell(String text, Color bgColor) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(8))
                .setBackgroundColor(bgColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
    }
}