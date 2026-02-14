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

public class projectagricolecontroller implements Initializable {

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

    // Footer Statistics (Optional)
    @FXML private Label lblTotalBudget;
    @FXML private Label lblTotalSurface;
    @FXML private Label lblLastUpdate;

    private List<projectagricole> allProjects = new ArrayList<>();
    private projectagricole selectedProject = null;
    private final projectagricoleCRUD service = new projectagricoleCRUD();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize combo boxes if they exist
        if (cbStatut != null) {
            cbStatut.getItems().addAll("en cours", "accepte", "refuse");
        }

        // Initialize filter combo
        if (cbFilterStatutList != null) {
            cbFilterStatutList.getItems().addAll("Tous les statuts", "en cours", "accepte", "refuse");
            cbFilterStatutList.setValue("Tous les statuts");
        }

        // Setup listeners
        setupSearchListener();
        setupFilterListener();

        // Load data if container exists
        if (projectsContainer != null) {
            refreshDataFromDB();
        }
    }

    /**
     * Sets up real-time search functionality
     */
    private void setupSearchListener() {
        if (tfSearchProject != null) {
            tfSearchProject.textProperty().addListener((observable, oldValue, newValue) -> {
                updateCardsDisplay();
            });
        }
    }

    /**
     * Sets up filter dropdown functionality
     */
    private void setupFilterListener() {
        if (cbFilterStatutList != null) {
            cbFilterStatutList.setOnAction(event -> {
                updateCardsDisplay();
            });
        }
    }

    /**
     * Refreshes data from database and updates all displays
     */
    public void refreshDataFromDB() {
        try {
            allProjects = service.afficher();
            updateCardsDisplay();
            updateStatistics();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Base de données",
                    "Impossible de charger les projets : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates all statistics displayed in the dashboard
     */
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

            // Update stat labels if they exist
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

            // Update footer stats
            updateFooterStats();

        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates the footer statistics (total budget, surface, etc.)
     */
    private void updateFooterStats() {
        if (allProjects.isEmpty()) return;

        // Calculate total budget
        BigDecimal totalBudget = allProjects.stream()
                .map(projectagricole::getBudgetdemande)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total surface
        double totalSurface = allProjects.stream()
                .mapToDouble(projectagricole::getSurface)
                .sum();

        // Update footer labels if they exist
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

    /**
     * Filters and displays projects based on search text and status filter
     */
    private void updateCardsDisplay() {
        if (projectsContainer == null) return;

        projectsContainer.getChildren().clear();

        String searchText = tfSearchProject != null ? tfSearchProject.getText().toLowerCase() : "";
        String filterStatut = cbFilterStatutList != null ? cbFilterStatutList.getValue() : "Tous les statuts";

        // Filter projects using streams
        List<projectagricole> filteredList = allProjects.stream()
                .filter(p -> {
                    // Search filter
                    boolean matchesSearch = searchText.isEmpty()
                            || p.getNomproject().toLowerCase().contains(searchText);

                    // Status filter
                    boolean matchesStatus = filterStatut.equals("Tous les statuts")
                            || p.getStatut().equals(filterStatut);

                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toList());

        // Create and display cards
        for (projectagricole p : filteredList) {
            projectsContainer.getChildren().add(createEnhancedProjectCard(p));
        }
    }

    /**
     * Creates an enhanced project card with professional styling
     */
    private VBox createEnhancedProjectCard(projectagricole project) {
        VBox card = new VBox(12);
        card.getStyleClass().add("project-card");
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setPadding(new Insets(18));

        // Card Header with Icon and Project Name
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getProjectIcon(project.getStatut()));
        icon.setStyle("-fx-font-size: 24px;");

        Label title = new Label(project.getNomproject());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);
        title.setMaxWidth(220);

        header.getChildren().addAll(icon, title);

        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));

        // Project Details
        VBox details = new VBox(8);

        // ID Row
        HBox idRow = createInfoRow("🔖", "ID Projet", "#" + project.getIdproject());

        // Surface
        HBox surfaceBox = createInfoRow("🌍", "Surface",
                String.format("%.2f Ha", project.getSurface()));

        // Budget
        HBox budgetBox = createInfoRow("💰", "Budget",
                String.format("%,.2f DT", project.getBudgetdemande()));

        // Date
        HBox dateBox = createInfoRow("📅", "Date",
                project.getDatesoumission().toLocalDate().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                ));

        details.getChildren().addAll(idRow, surfaceBox, budgetBox, dateBox);

        // Status Badge
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label("Statut:");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6C757D; -fx-font-weight: 600;");

        Label badge = new Label(capitalizeStatus(project.getStatut()));
        badge.getStyleClass().add(getStatusBadgeClass(project.getStatut()));

        statusBox.getChildren().addAll(statusLabel, badge);

        // Action Buttons - ENHANCED
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(12, 0, 0, 0));

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
            selectedProject = project;
            openModifyProjectForm(null);
        });
        btnEdit.setTooltip(new Tooltip("Modifier les informations du projet"));

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
            selectedProject = project;
            deleteProject(null);
        });
        btnDelete.setTooltip(new Tooltip("Supprimer ce projet définitivement"));

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
     * Returns appropriate icon based on project status
     */
    private String getProjectIcon(String status) {
        switch (status.toLowerCase()) {
            case "accepte": return "✅";
            case "en cours": return "⏳";
            case "refuse": return "❌";
            default: return "📁";
        }
    }

    /**
     * Returns CSS class for status badge
     */
    private String getStatusBadgeClass(String status) {
        switch (status.toLowerCase()) {
            case "accepte": return "status-badge-accepte";
            case "en cours": return "status-badge-encours";
            case "refuse": return "status-badge-refuse";
            default: return "status-badge-encours";
        }
    }

    /**
     * Capitalizes status text for display
     */
    private String capitalizeStatus(String status) {
        switch (status.toLowerCase()) {
            case "accepte": return "Accepté";
            case "en cours": return "En Cours";
            case "refuse": return "Refusé";
            default: return status;
        }
    }

    /**
     * Opens the Add Project form
     */
    @FXML
    public void openAddProjectForm(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectagricoleadd.fxml"));
            Parent root = loader.load();

            // Get the controller of the add form
            ProjectAgricoleAddController addController = loader.getController();
            addController.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Nouveau Projet");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            // Refresh the list after closing the add window
            refreshDataFromDB();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Opens the Modify Project form
     */
    @FXML
    void openModifyProjectForm(ActionEvent event) {
        if (selectedProject == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner un projet à modifier.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectagricolemodify.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the selected project
            ProjectAgricoleModifyController modifyController = loader.getController();
            modifyController.setProject(selectedProject);
            modifyController.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier le Projet");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            // Refresh the list after closing
            refreshDataFromDB();
            selectedProject = null;
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Deletes the selected project
     */
    @FXML
    void deleteProject(ActionEvent event) {
        if (selectedProject == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner un projet à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer le projet");
        confirm.setContentText("Voulez-vous vraiment supprimer le projet \"" +
                selectedProject.getNomproject() + "\" ?\n\nCette action est irréversible.");

        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.OK) {
            try {
                service.supprimer(selectedProject.getIdproject());
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Projet supprimé avec succès !");
                selectedProject = null;
                refreshDataFromDB();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur SQL",
                        "Impossible de supprimer : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Navigates to Resources view
     */
    @FXML
    void goToRessources(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/ressourceproject.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Gestion des Ressources");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la vue des ressources: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates form inputs
     */
    private boolean validateInputs() {
        if (tfNomProject == null || tfSurface == null || tfBudget == null ||
                cbStatut == null || dpDateSoumission == null) {
            return false;
        }

        if (tfNomProject.getText().trim().isEmpty() ||
                tfSurface.getText().trim().isEmpty() ||
                tfBudget.getText().trim().isEmpty() ||
                cbStatut.getValue() == null ||
                dpDateSoumission.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Veuillez remplir tous les champs !");
            return false;
        }

        try {
            float surface = Float.parseFloat(tfSurface.getText().trim());
            if (surface <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                        "La surface doit être un nombre positif !");
                return false;
            }

            BigDecimal budget = new BigDecimal(tfBudget.getText().trim());
            if (budget.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                        "Le budget doit être un nombre positif !");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format",
                    "Surface et Budget doivent être des nombres valides !");
            return false;
        }

        return true;
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

    /**
     * Clears all form fields
     */
    @FXML
    void clearFields(ActionEvent event) {
        if (tfNomProject == null) return;

        selectedProject = null;
        tfNomProject.clear();
        tfSurface.clear();
        tfBudget.clear();
        cbStatut.getSelectionModel().clearSelection();
        dpDateSoumission.setValue(null);
    }
}