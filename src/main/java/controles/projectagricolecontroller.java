package controles;

import entities.projectagricole;
import services.projectagricoleCRUD;
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
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

public class projectagricolecontroller implements Initializable {

    // ============================================================================
    // MAIN VIEW FIELDS (List View)
    // ============================================================================
    @FXML private TextField tfNomProject;
    @FXML private TextField tfSurface;
    @FXML private TextField tfBudget;
    @FXML private ComboBox<String> cbStatut;
    @FXML private DatePicker dpDateSoumission;
    @FXML private FlowPane projectsContainer;

    // Search and Filter Elements
    @FXML private TextField tfSearchProject;
    @FXML private ComboBox<String> cbFilterStatutList;

    // Statistics Labels
    @FXML private Label lblTotalProjects;
    @FXML private Label lblAcceptedProjects;
    @FXML private Label lblInProgressProjects;
    @FXML private Label lblRefusedProjects;

    // Footer Statistics
    @FXML private Label lblTotalBudget;
    @FXML private Label lblTotalSurface;
    @FXML private Label lblLastUpdate;

    // ============================================================================
    // ADD/MODIFY DIALOG FIELDS
    // ============================================================================
    @FXML private TextField tfNomProjectDialog;
    @FXML private TextField tfSurfaceDialog;
    @FXML private TextField tfBudgetDialog;
    @FXML private DatePicker dpDateSoumissionDialog;
    @FXML private Button btnSave;
    @FXML private Label lblStatusBadge;

    // ============================================================================
    // SHARED FIELDS
    // ============================================================================
    private List<projectagricole> allProjects = new ArrayList<>();
    private projectagricole selectedProject = null;
    private projectagricole currentProject = null; // For modify operation
    private final projectagricoleCRUD service = new projectagricoleCRUD();
    private Timeline autoRefreshTimeline;
    private static final int REFRESH_INTERVAL_SECONDS = 5;

    // Reference to main controller for dialog callbacks
    private projectagricolecontroller mainController;

    // Mode: "add" or "modify"
    private String dialogMode = "list";

    // ============================================================================
    // INITIALIZATION
    // ============================================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize based on which view is loaded
        if (cbStatut != null) {
            cbStatut.getItems().addAll("en cours", "accepte", "refuse");
        }
        if (cbFilterStatutList != null) {
            cbFilterStatutList.getItems().addAll("Tous les statuts", "en cours", "accepte", "refuse");
            cbFilterStatutList.setValue("Tous les statuts");
        }

        // Setup for main list view
        if (projectsContainer != null) {
            setupSearchListener();
            setupFilterListener();

            // Load data with comprehensive error handling
            try {
                refreshDataFromDB();
                startAutoRefresh();
            } catch (Exception e) {
                System.err.println("Error loading initial data: " + e.getMessage());
                e.printStackTrace();

                // Initialize with empty data to prevent crashes
                allProjects = new ArrayList<>();
                updateCardsDisplay();
                updateStatistics();

                // Show user-friendly error
                showAlert(Alert.AlertType.ERROR, "Erreur de connexion",
                        "Impossible de se connecter à la base de données.\n\n" +
                                "Vérifiez que :\n" +
                                "1. Le serveur de base de données est démarré\n" +
                                "2. Les identifiants de connexion sont corrects\n" +
                                "3. La base de données existe\n\n" +
                                "Détails: " + e.getMessage());
            }
        }

        // Setup for add dialog
        if (lblStatusBadge != null && btnSave != null && dpDateSoumissionDialog != null) {
            setupAddDialog();
        }
    }

    /**
     * Setup for Add Dialog
     */
    private void setupAddDialog() {
        if (lblStatusBadge != null) {
            lblStatusBadge.setText("📋 En cours");
            lblStatusBadge.setStyle(
                    "-fx-background-color: linear-gradient(to right, #E1B323, #9A951F);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;" +
                            "-fx-padding: 10 24;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-radius: 20;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(225, 179, 35, 0.4), 8, 0, 0, 2);" +
                            "-fx-cursor: hand;"
            );

            Tooltip tooltip = new Tooltip(
                    "Le statut initial est toujours 'En cours'.\n" +
                            "Il sera modifié automatiquement par les décisions financières."
            );
            lblStatusBadge.setTooltip(tooltip);
        }
    }

    /**
     * Setup for Modify Dialog
     */
    public void setProject(projectagricole project) {
        this.currentProject = project;
        this.dialogMode = "modify";
        populateFields();
    }

    public void setMainController(projectagricolecontroller mainController) {
        this.mainController = mainController;
    }

    private void populateFields() {
        if (currentProject != null) {
            // Use dialog fields
            if (tfNomProjectDialog != null) {
                tfNomProjectDialog.setText(currentProject.getNomproject());
                tfSurfaceDialog.setText(String.valueOf(currentProject.getSurface()));
                tfBudgetDialog.setText(currentProject.getBudgetdemande().toString());
                dpDateSoumissionDialog.setValue(currentProject.getDatesoumission().toLocalDate());
                updateStatusBadge(currentProject.getStatut());
            }
        }
    }

    /**
     * Update status badge with appropriate colors based on status
     */
    private void updateStatusBadge(String statut) {
        if (lblStatusBadge == null) return;

        String displayText;
        String backgroundColor;
        String textColor = "white";
        String tooltipText;
        String icon;

        switch (statut.toLowerCase()) {
            case "accepte":
                icon = "✓";
                displayText = icon + " Accepté";
                backgroundColor = "linear-gradient(to right, #089647, #076A39)";
                tooltipText = "Projet accepté par la décision financière.\nStatut géré automatiquement.";
                break;

            case "refuse":
                icon = "✗";
                displayText = icon + " Refusé";
                backgroundColor = "linear-gradient(to right, #D32F2F, #B71C1C)";
                tooltipText = "Projet refusé par la décision financière.\nStatut géré automatiquement.";
                break;

            case "en cours":
            default:
                icon = "📋";
                displayText = icon + " En cours";
                backgroundColor = "linear-gradient(to right, #E1B323, #9A951F)";
                tooltipText = "Projet en attente de décision financière.\nStatut géré automatiquement.";
                break;
        }

        lblStatusBadge.setText(displayText);
        lblStatusBadge.setStyle(
                "-fx-background-color: " + backgroundColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10 24;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 8, 0, 0, 2);" +
                        "-fx-cursor: hand;"
        );

        Tooltip tooltip = new Tooltip(tooltipText);
        lblStatusBadge.setTooltip(tooltip);
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
                currentProject.setNomproject(getDialogFieldValue(tfNomProjectDialog));
                currentProject.setSurface(Float.parseFloat(getDialogFieldValue(tfSurfaceDialog)));
                currentProject.setBudgetdemande(new BigDecimal(getDialogFieldValue(tfBudgetDialog)));
                currentProject.setDatesoumission(Date.valueOf(dpDateSoumissionDialog.getValue()));
                // Do NOT update status - it's managed by financial decisions

                service.modifier(currentProject);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Projet modifié avec succès!\n\n" +
                                "Note: Le statut reste inchangé (" +
                                getStatusDisplayName(currentProject.getStatut()) + ")");
            } else {
                // ADD MODE
                projectagricole p = new projectagricole(
                        getDialogFieldValue(tfNomProjectDialog),
                        Float.parseFloat(getDialogFieldValue(tfSurfaceDialog)),
                        new BigDecimal(getDialogFieldValue(tfBudgetDialog)),
                        "en cours",
                        Date.valueOf(dpDateSoumissionDialog.getValue())
                );

                service.ajouter(p);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Projet ajouté avec succès!\n\n" +
                                "Statut: En cours (en attente de décision financière)");
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

    private String getDialogFieldValue(TextField field) {
        return field != null ? field.getText().trim() : "";
    }

    private boolean validateDialogInputs() {
        // Check for empty fields
        if (getDialogFieldValue(tfNomProjectDialog).isEmpty() ||
                getDialogFieldValue(tfSurfaceDialog).isEmpty() ||
                getDialogFieldValue(tfBudgetDialog).isEmpty() ||
                dpDateSoumissionDialog.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Veuillez remplir tous les champs!");
            return false;
        }

        // Validate project name length
        if (getDialogFieldValue(tfNomProjectDialog).length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Le nom du projet doit contenir au moins 3 caractères!");
            return false;
        }

        // Validate date based on mode
        if ("add".equals(dialogMode)) {
            // Add mode: only today's date allowed
            if (!dpDateSoumissionDialog.getValue().isEqual(java.time.LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Erreur de date",
                        "La date de soumission doit être la date d'aujourd'hui uniquement!\n" +
                                "Date actuelle: " + java.time.LocalDate.now());
                return false;
            }
        } else if ("modify".equals(dialogMode)) {
            // Modify mode: no future dates allowed
            if (dpDateSoumissionDialog.getValue().isAfter(java.time.LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Erreur de date",
                        "La date de soumission ne peut pas être dans le futur!\n" +
                                "Veuillez sélectionner la date d'aujourd'hui ou une date passée.");
                return false;
            }
        }

        // Validate numeric fields
        try {
            float surface = Float.parseFloat(getDialogFieldValue(tfSurfaceDialog));
            if (surface <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                        "La surface doit être un nombre positif!");
                return false;
            }

            BigDecimal budget = new BigDecimal(getDialogFieldValue(tfBudgetDialog));
            if (budget.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                        "Le budget doit être un nombre positif!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format",
                    "Surface et Budget doivent être des nombres valides!");
            return false;
        }

        return true;
    }

    private String getStatusDisplayName(String statut) {
        switch (statut.toLowerCase()) {
            case "accepte": return "Accepté";
            case "refuse": return "Refusé";
            case "en cours": return "En cours";
            default: return statut;
        }
    }

    // ============================================================================
    // MAIN VIEW - LIST OPERATIONS
    // ============================================================================

    private void setupSearchListener() {
        if (tfSearchProject != null) {
            tfSearchProject.textProperty().addListener((observable, oldValue, newValue) -> {
                updateCardsDisplay();
            });
        }
    }

    private void setupFilterListener() {
        if (cbFilterStatutList != null) {
            cbFilterStatutList.setOnAction(event -> {
                updateCardsDisplay();
            });
        }
    }

    public void refreshDataFromDB() {
        try {
            allProjects = service.afficher();
            if (allProjects == null) {
                allProjects = new ArrayList<>();
            }
            updateCardsDisplay();
            updateStatistics();
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();

            // Initialize with empty list to prevent crashes
            if (allProjects == null) {
                allProjects = new ArrayList<>();
            }

            showAlert(Alert.AlertType.ERROR, "Erreur Base de données",
                    "Impossible de charger les projets.\n" +
                            "Vérifiez votre connexion à la base de données.\n\n" +
                            "Détails: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();

            if (allProjects == null) {
                allProjects = new ArrayList<>();
            }

            showAlert(Alert.AlertType.ERROR, "Erreur inattendue",
                    "Une erreur inattendue s'est produite.\n\n" +
                            "Détails: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        try {
            int totalCount = allProjects.size();
            long acceptedCount = allProjects.stream()
                    .filter(p -> "accepte".equals(p.getStatut()))
                    .count();
            long inProgressCount = allProjects.stream()
                    .filter(p -> "en cours".equals(p.getStatut()))
                    .count();
            long refusedCount = allProjects.stream()
                    .filter(p -> "refuse".equals(p.getStatut()))
                    .count();

            if (lblTotalProjects != null) {
                lblTotalProjects.setText(String.valueOf(totalCount));
            }
            if (lblAcceptedProjects != null) {
                lblAcceptedProjects.setText(String.valueOf(acceptedCount));
            }
            if (lblInProgressProjects != null) {
                lblInProgressProjects.setText(String.valueOf(inProgressCount));
            }
            if (lblRefusedProjects != null) {
                lblRefusedProjects.setText(String.valueOf(refusedCount));
            }

            updateFooterStats();

        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateFooterStats() {
        if (allProjects.isEmpty()) return;

        BigDecimal totalBudget = allProjects.stream()
                .map(projectagricole::getBudgetdemande)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double totalSurface = allProjects.stream()
                .mapToDouble(projectagricole::getSurface)
                .sum();

        if (lblTotalBudget != null) {
            lblTotalBudget.setText(String.format("%,.2f DT", totalBudget));
        }
        if (lblTotalSurface != null) {
            lblTotalSurface.setText(String.format("%.2f Ha", totalSurface));
        }
        if (lblLastUpdate != null) {
            lblLastUpdate.setText(java.time.LocalDate.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            ));
        }
    }

    private void updateCardsDisplay() {
        if (projectsContainer == null) return;

        projectsContainer.getChildren().clear();

        String searchText = tfSearchProject != null ? tfSearchProject.getText().toLowerCase() : "";
        String filterStatut = cbFilterStatutList != null ? cbFilterStatutList.getValue() : "Tous les statuts";

        List<projectagricole> filteredList = allProjects.stream()
                .filter(p -> {
                    boolean matchesSearch = searchText.isEmpty()
                            || p.getNomproject().toLowerCase().contains(searchText);
                    boolean matchesStatus = filterStatut.equals("Tous les statuts")
                            || p.getStatut().equals(filterStatut);
                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toList());

        for (projectagricole p : filteredList) {
            projectsContainer.getChildren().add(createEnhancedProjectCard(p));
        }
    }

    private VBox createEnhancedProjectCard(projectagricole project) {
        VBox card = new VBox(12);
        card.getStyleClass().add("project-card");
        card.setPadding(new Insets(20));
        card.setMaxWidth(380);
        card.setPrefWidth(380);

        // Header with icon and title
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getProjectIcon(project.getStatut()));
        icon.setStyle("-fx-font-size: 32px;");

        VBox titleBox = new VBox(4);
        Label title = new Label(project.getNomproject());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #133D03;");
        title.setWrapText(true);

        Label id = new Label("ID: " + project.getIdproject());
        id.setStyle("-fx-font-size: 11px; -fx-text-fill: #848A86;");

        titleBox.getChildren().addAll(title, id);
        header.getChildren().addAll(icon, titleBox);

        // Status badge
        Label statusBadge = new Label(capitalizeStatus(project.getStatut()));
        statusBadge.getStyleClass().add(getStatusBadgeClass(project.getStatut()));

        // Details grid
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(8);

        addCardDetailRow(detailsGrid, 0, "🌾 Surface:", String.format("%.2f Ha", project.getSurface()));
        addCardDetailRow(detailsGrid, 1, "💰 Budget:", String.format("%,.2f DT", project.getBudgetdemande()));
        addCardDetailRow(detailsGrid, 2, "📅 Date:", project.getDatesoumission().toString());

        // Action buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(5, 0, 0, 0));

        Button btnView = new Button("👁");
        btnView.getStyleClass().add("btn-view");
        btnView.setOnAction(e -> showProjectDetails(project));
        btnView.setTooltip(new Tooltip("Voir les détails"));
        btnView.setMinWidth(40);
        btnView.setPrefWidth(40);

        Button btnEdit = new Button("✏");
        btnEdit.getStyleClass().add("btn-edit");
        btnEdit.setOnAction(e -> openModifyDialog(project));
        btnEdit.setTooltip(new Tooltip("Modifier"));
        btnEdit.setMinWidth(40);
        btnEdit.setPrefWidth(40);

        Button btnDelete = new Button("🗑");
        btnDelete.getStyleClass().add("btn-delete");
        btnDelete.setOnAction(e -> handleDelete(project));
        btnDelete.setTooltip(new Tooltip("Supprimer"));
        btnDelete.setMinWidth(40);
        btnDelete.setPrefWidth(40);

        actions.getChildren().addAll(btnView, btnEdit, btnDelete);
        HBox.setHgrow(actions, Priority.ALWAYS);

        // Assemble card
        card.getChildren().addAll(header, statusBadge, new Separator(), detailsGrid, actions);

        return card;
    }

    private void addCardDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #848A86;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #076A39;");

        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }

    private String getProjectIcon(String statut) {
        switch (statut.toLowerCase()) {
            case "accepte": return "✅";
            case "refuse": return "❌";
            case "en cours": return "⏳";
            default: return "📋";
        }
    }

    private String capitalizeStatus(String statut) {
        switch (statut.toLowerCase()) {
            case "accepte": return "Accepté";
            case "refuse": return "Refusé";
            case "en cours": return "En cours";
            default: return statut;
        }
    }

    private String getStatusBadgeClass(String statut) {
        switch (statut.toLowerCase()) {
            case "accepte": return "status-accepted";
            case "refuse": return "status-refused";
            case "en cours": return "status-progress";
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
        if (selectedProject == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection",
                    "Veuillez sélectionner un projet à modifier!");
            return;
        }
        openModifyDialog(selectedProject);
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (selectedProject == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection",
                    "Veuillez sélectionner un projet à supprimer!");
            return;
        }
        handleDelete(selectedProject);
    }

    private void handleDelete(projectagricole project) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer le projet: " + project.getNomproject());
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce projet?\nCette action est irréversible.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.supprimer(project.getIdproject());
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "Projet supprimé avec succès!");
                    refreshDataFromDB();
                    selectedProject = null;
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

    private void openAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectagricoleadd.fxml"));
            Parent root = loader.load();

            projectagricolecontroller controller = loader.getController();
            controller.dialogMode = "add";
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nouveau Projet Agricole");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    private void openModifyDialog(projectagricole project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectagricoleadd.fxml"));
            Parent root = loader.load();

            projectagricolecontroller controller = loader.getController();
            controller.dialogMode = "modify";
            controller.setProject(project);
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier Projet Agricole");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }

    // ============================================================================
    // AUTO-REFRESH & UTILITY METHODS
    // ============================================================================

    private void startAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }

        autoRefreshTimeline = new Timeline(new KeyFrame(
                Duration.seconds(REFRESH_INTERVAL_SECONDS),
                event -> refreshDataFromDB()
        ));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        refreshDataFromDB();
        showAlert(Alert.AlertType.INFORMATION, "Actualisation",
                "Les données ont été actualisées!");
    }

    // ============================================================================
    // PROJECT DETAILS DISPLAY
    // ============================================================================

    private void showProjectDetails(projectagricole project) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails du Projet");
        dialog.setHeaderText(null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white;");

        // Header with icon and title
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label iconLarge = new Label(getProjectIcon(project.getStatut()));
        iconLarge.setStyle("-fx-font-size: 48px;");

        VBox titleBox = new VBox(5);
        Label titleLabel = new Label(project.getNomproject());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #133D03;");

        Label idLabel = new Label("Projet #" + project.getIdproject());
        idLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #848A86;");

        titleBox.getChildren().addAll(titleLabel, idLabel);
        headerBox.getChildren().addAll(iconLarge, titleBox);

        // Status row
        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label("Statut:");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #848A86;");

        Label statusBadge = new Label(capitalizeStatus(project.getStatut()));
        statusBadge.getStyleClass().add(getStatusBadgeClass(project.getStatut()));
        statusBadge.setStyle(statusBadge.getStyle() + "-fx-font-size: 14px; -fx-padding: 8 20;");

        statusRow.getChildren().addAll(statusLabel, statusBadge);

        // Details grid
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(15);
        detailsGrid.setPadding(new Insets(10, 0, 10, 0));

        addDetailRow(detailsGrid, 0, "🌾 Surface:", String.format("%.2f Hectares", project.getSurface()));
        addDetailRow(detailsGrid, 1, "💰 Budget demandé:", String.format("%,.2f DT", project.getBudgetdemande()));
        addDetailRow(detailsGrid, 2, "📅 Date de soumission:", project.getDatesoumission().toString());

        Separator sep = new Separator();

        // Status explanation
        VBox statusExplanation = new VBox(8);
        Label explanationTitle = new Label("ℹ️ À propos du statut");
        explanationTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #133D03;");

        Label explanationText = new Label(getStatusExplanation(project.getStatut()));
        explanationText.setWrapText(true);
        explanationText.setMaxWidth(450);
        explanationText.setStyle("-fx-font-size: 12px; -fx-text-fill: #6C757D; -fx-line-spacing: 2px;");

        statusExplanation.getChildren().addAll(explanationTitle, explanationText);

        // Add all to content
        content.getChildren().addAll(
                headerBox,
                statusRow,
                detailsGrid,
                sep,
                statusExplanation
        );

        dialog.getDialogPane().setContent(content);

        ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        Button closeBtn = (Button) dialog.getDialogPane().lookupButton(closeButton);
        closeBtn.setStyle(
                "-fx-background-color: #076A39;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 30;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        );

        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #848A86;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #076A39;");

        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }

    private String getStatusExplanation(String statut) {
        switch (statut.toLowerCase()) {
            case "accepte":
                return "Ce projet a été accepté par la décision financière. " +
                        "Le financement a été approuvé et le projet peut démarrer.";
            case "refuse":
                return "Ce projet a été refusé par la décision financière. " +
                        "Le financement n'a pas été approuvé.";
            case "en cours":
                return "Ce projet est en attente d'une décision financière. " +
                        "Le statut sera mis à jour automatiquement une fois la décision prise.";
            default:
                return "Statut du projet: " + capitalizeStatus(statut);
        }
    }

    @FXML
    void showSelectedProjectDetails(ActionEvent event) {
        if (selectedProject == null) {
            showProjectSelectionDialog();
        } else {
            showProjectDetails(selectedProject);
        }
    }

    private void showProjectSelectionDialog() {
        if (allProjects == null || allProjects.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucun projet",
                    "Aucun projet disponible. Veuillez d'abord créer un projet.");
            return;
        }

        Dialog<projectagricole> dialog = new Dialog<>();
        dialog.setTitle("Sélectionner un Projet");
        dialog.setHeaderText("Choisissez un projet pour voir ses détails");

        ListView<projectagricole> listView = new ListView<>();
        listView.getItems().addAll(allProjects);
        listView.setPrefHeight(400);
        listView.setPrefWidth(500);

        listView.setCellFactory(param -> new ListCell<projectagricole>() {
            @Override
            protected void updateItem(projectagricole project, boolean empty) {
                super.updateItem(project, empty);
                if (empty || project == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox cell = new HBox(15);
                    cell.setAlignment(Pos.CENTER_LEFT);
                    cell.setPadding(new Insets(10));

                    Label icon = new Label(getProjectIcon(project.getStatut()));
                    icon.setStyle("-fx-font-size: 24px;");

                    VBox info = new VBox(5);
                    Label name = new Label(project.getNomproject());
                    name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #133D03;");

                    Label details = new Label(String.format("ID: %d | Surface: %.2f Ha | Budget: %,.2f DT",
                            project.getIdproject(), project.getSurface(), project.getBudgetdemande()));
                    details.setStyle("-fx-font-size: 11px; -fx-text-fill: #848A86;");

                    info.getChildren().addAll(name, details);

                    Label badge = new Label(capitalizeStatus(project.getStatut()));
                    badge.getStyleClass().add(getStatusBadgeClass(project.getStatut()));
                    badge.setStyle(badge.getStyle() + "-fx-font-size: 11px; -fx-padding: 4 12;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    cell.getChildren().addAll(icon, info, spacer, badge);
                    setGraphic(cell);
                }
            }
        });

        if (!allProjects.isEmpty()) {
            listView.getSelectionModel().select(0);
        }

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label instruction = new Label("Double-cliquez sur un projet ou sélectionnez et cliquez sur OK");
        instruction.setStyle("-fx-font-size: 12px; -fx-text-fill: #6C757D;");

        content.getChildren().addAll(instruction, listView);
        dialog.getDialogPane().setContent(content);

        ButtonType okButton = new ButtonType("Voir Détails", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(okButton);
        okBtn.setDisable(listView.getSelectionModel().getSelectedItem() == null);
        listView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> okBtn.setDisable(newVal == null)
        );

        okBtn.setStyle(
                "-fx-background-color: #076A39;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 20;" +
                        "-fx-background-radius: 6;"
        );

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
                projectagricole selected = listView.getSelectionModel().getSelectedItem();
                dialog.setResult(selected);
                dialog.close();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(project -> {
            selectedProject = project;
            showProjectDetails(project);
        });
    }

    // ============================================================================
    // NAVIGATION METHODS
    // ============================================================================

    /**
     * Navigate to Ressources view (called from FXML)
     */
    @FXML
    void goToRessources(ActionEvent event) {
        try {
            // Load the ressources FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ressourceproject.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.getScene().setRoot(root);
            stage.setTitle("Gestion des Ressources");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible de charger la page Ressources: " + e.getMessage());
        }
    }

    /**
     * Open Add Project Form (called from FXML toolbar button)
     */
    @FXML
    void openAddProjectForm(ActionEvent event) {
        openAddDialog();
    }

    // ============================================================================
    // EXPORT OPERATIONS (PDF, etc.)
    // ============================================================================

    /**
     * Export to PDF (called from FXML)
     */
    @FXML
    void exportToPDF(ActionEvent event) {
        handleExportPDF(event);
    }

    @FXML
    void handleExportPDF(ActionEvent event) {
        if (allProjects == null || allProjects.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucune donnée",
                    "Aucun projet à exporter!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le rapport PDF");
        fileChooser.setInitialFileName("rapport_projets_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(projectsContainer.getScene().getWindow());
        if (file != null) {
            try {
                generatePDFReport(file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Rapport PDF généré avec succès!\n" + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de la génération du PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void generatePDFReport(String filePath) throws Exception {
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Title
        Paragraph title = new Paragraph("Rapport des Projets Agricoles")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        Paragraph date = new Paragraph("Généré le: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(date);

        document.add(new Paragraph("\n"));

        // Statistics summary
        document.add(new Paragraph("Statistiques Générales").setFontSize(14).setBold());
        document.add(new Paragraph("Total de projets: " + allProjects.size()));
        document.add(new Paragraph("Projets acceptés: " +
                allProjects.stream().filter(p -> "accepte".equals(p.getStatut())).count()));
        document.add(new Paragraph("Projets en cours: " +
                allProjects.stream().filter(p -> "en cours".equals(p.getStatut())).count()));
        document.add(new Paragraph("Projets refusés: " +
                allProjects.stream().filter(p -> "refuse".equals(p.getStatut())).count()));

        document.add(new Paragraph("\n"));

        // Table
        float[] columnWidths = {1, 3, 2, 2, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Header
        Color headerColor = new DeviceRgb(7, 106, 57);
        String[] headers = {"ID", "Nom", "Surface", "Budget", "Statut", "Date"};

        for (String header : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(header))
                    .setBackgroundColor(headerColor)
                    .setFontColor(ColorConstants.WHITE)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
        }

        // Data rows
        for (projectagricole p : allProjects) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(p.getIdproject()))));
            table.addCell(new Cell().add(new Paragraph(p.getNomproject())));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f Ha", p.getSurface()))));
            table.addCell(new Cell().add(new Paragraph(String.format("%,.2f DT", p.getBudgetdemande()))));
            table.addCell(new Cell().add(new Paragraph(capitalizeStatus(p.getStatut()))));
            table.addCell(new Cell().add(new Paragraph(p.getDatesoumission().toString())));
        }

        document.add(table);
        document.close();
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

    public void cleanup() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
    }
}