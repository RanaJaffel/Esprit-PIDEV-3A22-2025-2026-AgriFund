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
import javafx.stage.Modality;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
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

public class ressourceprojectcontroller implements Initializable {

    // ============================================================================
    // MAIN VIEW FIELDS (List View)
    // ============================================================================
    @FXML private FlowPane ressourcesContainer;
    @FXML private TextField tfSearchRessource;
    @FXML private ComboBox<String> cbFilterTypeList;

    // Statistics Labels
    @FXML private Label lblTotalRessources;
    @FXML private Label lblEquipments;
    @FXML private Label lblMaterials;
    @FXML private Label lblServices;

    // ============================================================================
    // ADD/MODIFY DIALOG FIELDS
    // ============================================================================
    @FXML private ComboBox<Integer> cbIdProjectDialog;
    @FXML private TextField tfNomRessourceDialog;
    @FXML private ComboBox<String> cbTypeRessourceDialog;
    @FXML private TextField tfQuantiteDialog;
    @FXML private TextField tfCoutDialog;
    @FXML private TextField tfFournisseurDialog;
    @FXML private ComboBox<String> cbStatutRessourceDialog;
    @FXML private DatePicker dpDateAjoutDialog;
    @FXML private Button btnSave;

    // ============================================================================
    // SHARED FIELDS
    // ============================================================================
    private List<ressourceproject> allRessources = new ArrayList<>();
    private ressourceproject selectedRessource = null;
    private ressourceproject currentRessource = null; // For modify operation
    private final ressourceprojectCRUD rService = new ressourceprojectCRUD();
    private final projectagricoleCRUD pService = new projectagricoleCRUD();

    // Reference to main controller for dialog callbacks
    private ressourceprojectcontroller mainController;

    // Mode: "list", "add" or "modify"
    private String dialogMode = "list";

    // ============================================================================
    // INITIALIZATION
    // ============================================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize filter combo for main view
        if (cbFilterTypeList != null) {
            cbFilterTypeList.getItems().addAll("Tous les types", "equipement", "materiaux", "service");
            cbFilterTypeList.setValue("Tous les types");
        }

        // Setup for main list view
        if (ressourcesContainer != null) {
            setupSearchListener();
            setupFilterListener();
            refreshDataFromDB();
        }

        // Setup for add/modify dialog
        if (cbTypeRessourceDialog != null && cbStatutRessourceDialog != null) {
            setupDialog();
        }
    }

    /**
     * Setup for Add/Modify Dialog
     */
    private void setupDialog() {
        // Initialize combo boxes for dialog
        if (cbTypeRessourceDialog != null) {
            cbTypeRessourceDialog.getItems().addAll("equipement", "materiaux", "service");
        }
        if (cbStatutRessourceDialog != null) {
            cbStatutRessourceDialog.getItems().addAll("prevu", "achete");
            cbStatutRessourceDialog.setValue("prevu"); // Default value for add mode
        }

        // Load project IDs
        try {
            if (cbIdProjectDialog != null) {
                for (projectagricole p : pService.afficher()) {
                    cbIdProjectDialog.getItems().add(p.getIdproject());
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les ID des projets.");
        }
    }

    /**
     * Setup for Modify Dialog
     */
    public void setRessource(ressourceproject ressource) {
        this.currentRessource = ressource;
        this.dialogMode = "modify";
        populateFields();
    }

    public void setMainController(ressourceprojectcontroller mainController) {
        this.mainController = mainController;
    }

    private void populateFields() {
        if (currentRessource != null && cbIdProjectDialog != null) {
            cbIdProjectDialog.setValue(currentRessource.getIdproject());
            tfNomRessourceDialog.setText(currentRessource.getNomressource());
            cbTypeRessourceDialog.setValue(currentRessource.getTyperessource());
            tfQuantiteDialog.setText(String.valueOf(currentRessource.getQuantite()));
            tfCoutDialog.setText(currentRessource.getCout().toString());
            tfFournisseurDialog.setText(currentRessource.getFournisseur());
            cbStatutRessourceDialog.setValue(currentRessource.getStatut());
            dpDateAjoutDialog.setValue(currentRessource.getDateajout().toLocalDate());
        }
    }

    // ============================================================================
    // ADD/MODIFY DIALOG HANDLERS
    // ============================================================================

    /**
     * Handle Save button for both Add and Modify modes
     */
    @FXML
    void handleSave(ActionEvent event) {
        if (!validateDialogInputs()) return;

        try {
            if ("modify".equals(dialogMode)) {
                // MODIFY MODE
                currentRessource.setIdproject(cbIdProjectDialog.getValue());
                currentRessource.setNomressource(tfNomRessourceDialog.getText().trim());
                currentRessource.setTyperessource(cbTypeRessourceDialog.getValue());
                currentRessource.setQuantite(Integer.parseInt(tfQuantiteDialog.getText().trim()));
                currentRessource.setCout(new BigDecimal(tfCoutDialog.getText().trim()));
                currentRessource.setFournisseur(tfFournisseurDialog.getText().trim());
                currentRessource.setStatut(cbStatutRessourceDialog.getValue());
                currentRessource.setDateajout(Date.valueOf(dpDateAjoutDialog.getValue()));

                rService.modifier(currentRessource);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource modifiée avec succès!");
            } else {
                // ADD MODE
                ressourceproject r = new ressourceproject(
                        tfNomRessourceDialog.getText().trim(),
                        cbTypeRessourceDialog.getValue(),
                        Integer.parseInt(tfQuantiteDialog.getText().trim()),
                        new BigDecimal(tfCoutDialog.getText().trim()),
                        tfFournisseurDialog.getText().trim(),
                        cbStatutRessourceDialog.getValue(),
                        Date.valueOf(dpDateAjoutDialog.getValue()),
                        cbIdProjectDialog.getValue()
                );

                rService.ajouter(r);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource ajoutée avec succès!");
            }

            // Refresh main controller if available
            if (mainController != null) {
                mainController.refreshDataFromDB();
            }

            closeDialogWindow();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL",
                    "Erreur lors de l'opération: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeDialogWindow();
    }

    private void closeDialogWindow() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    private boolean validateDialogInputs() {
        // Check for empty fields
        if (tfNomRessourceDialog.getText().trim().isEmpty() ||
                tfQuantiteDialog.getText().trim().isEmpty() ||
                tfCoutDialog.getText().trim().isEmpty() ||
                tfFournisseurDialog.getText().trim().isEmpty() ||
                cbIdProjectDialog.getValue() == null ||
                cbTypeRessourceDialog.getValue() == null ||
                cbStatutRessourceDialog.getValue() == null ||
                dpDateAjoutDialog.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir tous les champs!");
            return false;
        }

        // Validate resource name length
        if (tfNomRessourceDialog.getText().trim().length() < 2) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Le nom de la ressource doit contenir au moins 2 caractères!");
            return false;
        }

        // Validate supplier name length
        if (tfFournisseurDialog.getText().trim().length() < 2) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Le nom du fournisseur doit contenir au moins 2 caractères!");
            return false;
        }

        // Validate date based on mode
        if ("add".equals(dialogMode)) {
            // Add mode: only today's date allowed
            if (!dpDateAjoutDialog.getValue().isEqual(java.time.LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Erreur de date",
                        "La date d'ajout doit être la date d'aujourd'hui uniquement!\n" +
                                "Date actuelle: " + java.time.LocalDate.now());
                return false;
            }
        } else if ("modify".equals(dialogMode)) {
            // Modify mode: date cannot be in the past
            if (dpDateAjoutDialog.getValue().isBefore(java.time.LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                        "La date d'ajout ne peut pas être dans le passé!");
                return false;
            }
        }

        // Validate numeric fields
        try {
            int quantite = Integer.parseInt(tfQuantiteDialog.getText().trim());
            if (quantite <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                        "La quantité doit être un nombre positif!");
                return false;
            }

            BigDecimal cout = new BigDecimal(tfCoutDialog.getText().trim());
            if (cout.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                        "Le coût doit être un nombre positif!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format",
                    "La quantité et le coût doivent être des nombres valides!");
            return false;
        }

        return true;
    }

    // ============================================================================
    // MAIN VIEW - LIST OPERATIONS
    // ============================================================================

    private void setupSearchListener() {
        if (tfSearchRessource != null) {
            tfSearchRessource.textProperty().addListener((observable, oldValue, newValue) -> {
                updateCardsDisplay();
            });
        }
    }

    private void setupFilterListener() {
        if (cbFilterTypeList != null) {
            cbFilterTypeList.setOnAction(event -> {
                updateCardsDisplay();
            });
        }
    }

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

    private void updateCardsDisplay() {
        if (ressourcesContainer == null) return;

        ressourcesContainer.getChildren().clear();

        String searchText = tfSearchRessource != null ? tfSearchRessource.getText().toLowerCase() : "";
        String filterType = cbFilterTypeList != null ? cbFilterTypeList.getValue() : "Tous les types";

        List<ressourceproject> filteredList = allRessources.stream()
                .filter(r -> {
                    boolean matchesSearch = searchText.isEmpty()
                            || r.getNomressource().toLowerCase().contains(searchText);
                    boolean matchesType = filterType.equals("Tous les types")
                            || r.getTyperessource().equals(filterType);
                    return matchesSearch && matchesType;
                })
                .collect(Collectors.toList());

        for (ressourceproject r : filteredList) {
            ressourcesContainer.getChildren().add(createEnhancedResourceCard(r));
        }
    }

    private VBox createEnhancedResourceCard(ressourceproject resource) {
        VBox card = new VBox(12);
        card.getStyleClass().add("project-card");
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setPadding(new Insets(18));

        // Card Header with Icon and Resource Name
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getTypeIcon(resource.getTyperessource()));
        icon.setStyle("-fx-font-size: 28px;");

        VBox titleBox = new VBox(4);
        Label title = new Label(resource.getNomressource());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D6A4F;");
        title.setWrapText(true);

        Label id = new Label("ID: " + resource.getIdressource());
        id.setStyle("-fx-font-size: 10px; -fx-text-fill: #848A86;");

        titleBox.getChildren().addAll(title, id);
        header.getChildren().addAll(icon, titleBox);

        // Type Badge
        Label typeBadge = new Label(capitalizeType(resource.getTyperessource()));
        typeBadge.getStyleClass().add(getTypeBadgeClass(resource.getTyperessource()));

        // Status Badge
        Label statusBadge = new Label(capitalizeStatus(resource.getStatut()));
        statusBadge.getStyleClass().add(getStatusBadgeClass(resource.getStatut()));

        HBox badges = new HBox(8);
        badges.getChildren().addAll(typeBadge, statusBadge);

        // Details Grid
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(8);

        addCardDetailRow(detailsGrid, 0, "📦 Quantité:", String.valueOf(resource.getQuantite()));
        addCardDetailRow(detailsGrid, 1, "💰 Coût:", String.format("%.2f DT", resource.getCout()));
        addCardDetailRow(detailsGrid, 2, "🏭 Fournisseur:", resource.getFournisseur());
        addCardDetailRow(detailsGrid, 3, "🔗 Projet:", "#" + resource.getIdproject());

        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnView = new Button("👁 Voir");
        btnView.getStyleClass().add("btn-view");
        btnView.setOnAction(e -> showResourceDetails(resource));

        Button btnEdit = new Button("✏ Modifier");
        btnEdit.getStyleClass().add("btn-edit");
        btnEdit.setOnAction(e -> openModifyDialog(resource));

        Button btnDelete = new Button("🗑 Supprimer");
        btnDelete.getStyleClass().add("btn-delete");
        btnDelete.setOnAction(e -> handleDelete(resource));

        actions.getChildren().addAll(btnView, btnEdit, btnDelete);

        // Assemble card
        card.getChildren().addAll(header, badges, new Separator(), detailsGrid, actions);

        return card;
    }

    private void addCardDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #848A86;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2D6A4F;");
        lblValue.setWrapText(true);

        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }

    private String getTypeIcon(String type) {
        switch (type.toLowerCase()) {
            case "equipement": return "🚜";
            case "materiaux": return "⚙";
            case "service": return "🔧";
            default: return "📦";
        }
    }

    private String capitalizeType(String type) {
        switch (type.toLowerCase()) {
            case "equipement": return "Équipement";
            case "materiaux": return "Matériaux";
            case "service": return "Service";
            default: return type;
        }
    }

    private String capitalizeStatus(String statut) {
        switch (statut.toLowerCase()) {
            case "prevu": return "Prévu";
            case "achete": return "Acheté";
            default: return statut;
        }
    }

    private String getTypeBadgeClass(String type) {
        switch (type.toLowerCase()) {
            case "equipement": return "badge-equipment";
            case "materiaux": return "badge-materials";
            case "service": return "badge-service";
            default: return "badge-default";
        }
    }

    private String getStatusBadgeClass(String statut) {
        switch (statut.toLowerCase()) {
            case "achete": return "status-accepted";
            case "prevu": return "status-progress";
            default: return "status-progress";
        }
    }

    // ============================================================================
    // CRUD OPERATIONS FROM MAIN VIEW
    // ============================================================================

    @FXML
    void handleAdd(ActionEvent event) {
        openAddDialog();
    }

    @FXML
    void handleModify(ActionEvent event) {
        if (selectedRessource == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection",
                    "Veuillez sélectionner une ressource à modifier!");
            return;
        }
        openModifyDialog(selectedRessource);
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (selectedRessource == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection",
                    "Veuillez sélectionner une ressource à supprimer!");
            return;
        }
        handleDelete(selectedRessource);
    }

    private void handleDelete(ressourceproject resource) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer la ressource: " + resource.getNomressource());
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette ressource?\nCette action est irréversible.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    rService.supprimer(resource.getIdressource());
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "Ressource supprimée avec succès!");
                    refreshDataFromDB();
                    selectedRessource = null;
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur SQL",
                            "Erreur lors de la suppression: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // ============================================================================
    // DIALOG OPENERS
    // ============================================================================

    @FXML
    void openAddResourceForm(ActionEvent event) {
        openAddDialog();
    }

    private void openAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ressourceagricoleadd.fxml"));
            Parent root = loader.load();

            ressourceprojectcontroller controller = loader.getController();
            controller.dialogMode = "add";
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nouvelle Ressource");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    private void openModifyDialog(ressourceproject resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ressourceagricoleadd.fxml"));
            Parent root = loader.load();

            ressourceprojectcontroller controller = loader.getController();
            controller.dialogMode = "modify";
            controller.setRessource(resource);
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier Ressource");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }

    // ============================================================================
    // RESOURCE DETAILS DISPLAY
    // ============================================================================

    private void showResourceDetails(ressourceproject resource) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la Ressource");
        dialog.setHeaderText(null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white;");

        // Header
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label iconLarge = new Label(getTypeIcon(resource.getTyperessource()));
        iconLarge.setStyle("-fx-font-size: 48px;");

        VBox titleBox = new VBox(5);
        Label titleLabel = new Label(resource.getNomressource());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2D6A4F;");

        Label idLabel = new Label("Ressource #" + resource.getIdressource());
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #848A86;");

        titleBox.getChildren().addAll(titleLabel, idLabel);
        headerBox.getChildren().addAll(iconLarge, titleBox);

        // Badges row
        HBox badgesRow = new HBox(10);
        badgesRow.setAlignment(Pos.CENTER_LEFT);

        Label typeBadge = new Label(capitalizeType(resource.getTyperessource()));
        typeBadge.getStyleClass().add(getTypeBadgeClass(resource.getTyperessource()));

        Label statusBadge = new Label(capitalizeStatus(resource.getStatut()));
        statusBadge.getStyleClass().add(getStatusBadgeClass(resource.getStatut()));

        badgesRow.getChildren().addAll(typeBadge, statusBadge);

        // Details grid
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(15);
        detailsGrid.setPadding(new Insets(10, 0, 10, 0));

        addDetailRow(detailsGrid, 0, "📦 Quantité:", String.valueOf(resource.getQuantite()));
        addDetailRow(detailsGrid, 1, "💰 Coût unitaire:", String.format("%.2f DT", resource.getCout()));
        addDetailRow(detailsGrid, 2, "🏭 Fournisseur:", resource.getFournisseur());
        addDetailRow(detailsGrid, 3, "🔗 Projet associé:", "Projet #" + resource.getIdproject());
        addDetailRow(detailsGrid, 4, "📅 Date d'ajout:", resource.getDateajout().toString());

        Separator sep = new Separator();

        // Total cost calculation
        BigDecimal totalCost = resource.getCout().multiply(new BigDecimal(resource.getQuantite()));
        VBox costBox = new VBox(8);
        Label costTitle = new Label("💵 Coût Total");
        costTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D6A4F;");

        Label costValue = new Label(String.format("%.2f DT", totalCost));
        costValue.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #40916C;");

        costBox.getChildren().addAll(costTitle, costValue);

        content.getChildren().addAll(headerBox, badgesRow, detailsGrid, sep, costBox);
        dialog.getDialogPane().setContent(content);

        ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        Button closeBtn = (Button) dialog.getDialogPane().lookupButton(closeButton);
        closeBtn.setStyle(
                "-fx-background-color: #2D6A4F;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 30;" +
                        "-fx-background-radius: 6;"
        );

        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #848A86;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D6A4F;");

        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }

    // ============================================================================
    // NAVIGATION METHODS
    // ============================================================================

    @FXML
    void goToProjects(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectagricole.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Gestion des Projets Agricoles");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible de charger la page Projets: " + e.getMessage());
        }
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        refreshDataFromDB();
        showAlert(Alert.AlertType.INFORMATION, "Actualisation",
                "Les données ont été actualisées!");
    }

    // ============================================================================
    // PDF EXPORT
    // ============================================================================

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

    // ============================================================================
    // UTILITY METHODS
    // ============================================================================

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}