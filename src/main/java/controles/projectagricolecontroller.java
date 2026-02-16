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


import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatter;

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
    private Timeline autoRefreshTimeline;
    private static final int REFRESH_INTERVAL_SECONDS = 5;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (cbStatut != null) {
            cbStatut.getItems().addAll("en cours", "accepte", "refuse");
        }
        if (cbFilterStatutList != null) {
            cbFilterStatutList.getItems().addAll("Tous les statuts", "en cours", "accepte", "refuse");
            cbFilterStatutList.setValue("Tous les statuts");
        }
        setupSearchListener();
        setupFilterListener();
        if (projectsContainer != null) {
            refreshDataFromDB();
            startAutoRefresh();
        }
    }
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
            updateCardsDisplay();
            updateStatistics();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Base de données",
                    "Impossible de charger les projets : " + e.getMessage());
            e.printStackTrace();
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

        // Calculate total budget
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

        // Filter projects using streams
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
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setPadding(new Insets(18));


        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getProjectIcon(project.getStatut()));
        icon.setStyle("-fx-font-size: 24px;");

        Label title = new Label(project.getNomproject());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);
        title.setMaxWidth(220);

        header.getChildren().addAll(icon, title);


        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));


        VBox details = new VBox(8);


        HBox idRow = createInfoRow("🔖", "ID Projet", "#" + project.getIdproject());


        HBox surfaceBox = createInfoRow("🌍", "Surface",
                String.format("%.2f Ha", project.getSurface()));


        HBox budgetBox = createInfoRow("💰", "Budget",
                String.format("%,.2f DT", project.getBudgetdemande()));


        HBox dateBox = createInfoRow("📅", "Date",
                project.getDatesoumission().toLocalDate().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                ));

        details.getChildren().addAll(idRow, surfaceBox, budgetBox, dateBox);


        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label("Statut:");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6C757D; -fx-font-weight: 600;");

        Label badge = new Label(capitalizeStatus(project.getStatut()));
        badge.getStyleClass().add(getStatusBadgeClass(project.getStatut()));

        statusBox.getChildren().addAll(statusLabel, badge);


        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(12, 0, 0, 0));


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
        btnDetails.setOnAction(e -> showProjectDetails(project));
        btnDetails.setTooltip(new Tooltip("Voir tous les détails du projet"));


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

        actions.getChildren().addAll(btnDetails, btnEdit, btnDelete);


        card.getChildren().addAll(header, separator, details, statusBox, actions);

        return card;
    }


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


    private String getProjectIcon(String status) {
        switch (status.toLowerCase()) {
            case "accepte": return "✅";
            case "en cours": return "⏳";
            case "refuse": return "❌";
            default: return "📁";
        }
    }


    private String getStatusBadgeClass(String status) {
        switch (status.toLowerCase()) {
            case "accepte": return "status-badge-accepte";
            case "en cours": return "status-badge-encours";
            case "refuse": return "status-badge-refuse";
            default: return "status-badge-encours";
        }
    }


    private String capitalizeStatus(String status) {
        switch (status.toLowerCase()) {
            case "accepte": return "Accepté";
            case "en cours": return "En Cours";
            case "refuse": return "Refusé";
            default: return status;
        }
    }


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


            refreshDataFromDB();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }


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


            ProjectAgricoleModifyController modifyController = loader.getController();
            modifyController.setProject(selectedProject);
            modifyController.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier le Projet");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();


            refreshDataFromDB();
            selectedProject = null;
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
            e.printStackTrace();
        }
    }


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
            stage.getScene().setRoot(root);
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

    @FXML
    void exportToPDF(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.setInitialFileName("Projets_Agricoles_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );

            File file = fileChooser.showSaveDialog(projectsContainer.getScene().getWindow());

            if (file != null) {
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // CORRECTED: Use Color type instead of DeviceRgb
                Color headerColor = new DeviceRgb(45, 106, 79);
                Color lightGreen = new DeviceRgb(216, 243, 220);

                // Title
                Paragraph title = new Paragraph("RAPPORT DES PROJETS AGRICOLES")
                        .setFontSize(20)
                        .setBold()
                        .setFontColor(headerColor)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(10);
                document.add(title);

                // Date and Statistics
                Paragraph info = new Paragraph(
                        "Généré le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) +
                                "\nTotal de projets: " + allProjects.size()
                ).setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(20);
                document.add(info);

                // Statistics Summary
                Paragraph stats = new Paragraph("STATISTIQUES")
                        .setFontSize(14)
                        .setBold()
                        .setFontColor(headerColor)
                        .setMarginBottom(10);
                document.add(stats);

                Table statsTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                        .useAllAvailableWidth()
                        .setMarginBottom(20);

                long acceptedCount = allProjects.stream().filter(p -> "accepte".equals(p.getStatut())).count();
                long inProgressCount = allProjects.stream().filter(p -> "en cours".equals(p.getStatut())).count();
                long refusedCount = allProjects.stream().filter(p -> "refuse".equals(p.getStatut())).count();

                BigDecimal totalBudget = allProjects.stream()
                        .map(projectagricole::getBudgetdemande)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                statsTable.addCell(createStatsCell("Acceptés", String.valueOf(acceptedCount),
                        new DeviceRgb(213, 244, 230)));
                statsTable.addCell(createStatsCell("En Cours", String.valueOf(inProgressCount),
                        new DeviceRgb(255, 229, 204)));
                statsTable.addCell(createStatsCell("Refusés", String.valueOf(refusedCount),
                        new DeviceRgb(255, 229, 229)));
                statsTable.addCell(createStatsCell("Budget Total", String.format("%.2f DT", totalBudget),
                        lightGreen));

                document.add(statsTable);

                // Projects List Title
                Paragraph listTitle = new Paragraph("LISTE DÉTAILLÉE DES PROJETS")
                        .setFontSize(14)
                        .setBold()
                        .setFontColor(headerColor)
                        .setMarginBottom(10);
                document.add(listTitle);

                // Projects Table
                Table table = new Table(UnitValue.createPercentArray(new float[]{8, 22, 15, 18, 18, 19}))
                        .useAllAvailableWidth();

                // Table Headers
                String[] headers = {"ID", "Nom du Projet", "Surface (Ha)", "Budget (DT)", "Date Soumission", "Statut"};
                for (String header : headers) {
                    Cell headerCell = new Cell()
                            .add(new Paragraph(header).setBold().setFontSize(10))
                            .setBackgroundColor(headerColor)
                            .setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setPadding(8);
                    table.addHeaderCell(headerCell);
                }

                // Table Data
                for (projectagricole p : allProjects) {
                    Color rowColor = ColorConstants.WHITE;
                    if ("accepte".equals(p.getStatut())) {
                        rowColor = new DeviceRgb(213, 244, 230);
                    } else if ("refuse".equals(p.getStatut())) {
                        rowColor = new DeviceRgb(255, 235, 235);
                    } else if ("en cours".equals(p.getStatut())) {
                        rowColor = new DeviceRgb(255, 245, 230);
                    }

                    table.addCell(createDataCell(String.valueOf(p.getIdproject()), rowColor));
                    table.addCell(createDataCell(p.getNomproject(), rowColor));
                    table.addCell(createDataCell(String.format("%.2f", p.getSurface()), rowColor));
                    table.addCell(createDataCell(String.format("%,.2f", p.getBudgetdemande()), rowColor));
                    table.addCell(createDataCell(
                            p.getDatesoumission().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            rowColor
                    ));

                    String statutText = capitalizeStatus(p.getStatut());
                    table.addCell(createDataCell(statutText, rowColor).setBold());
                }

                document.add(table);

                // Footer
                Paragraph footer = new Paragraph(
                        "\n\nDocument généré automatiquement par le Système de Gestion des Projets Agricoles"
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

    // CORRECTED HELPER METHODS - Use Color parameter type
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
                .add(new Paragraph(text).setFontSize(9))
                .setBackgroundColor(bgColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6);
    }
    private void startAutoRefresh() {
        // Create a timeline that refreshes every X seconds
        autoRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(REFRESH_INTERVAL_SECONDS), event -> {
                    refreshDataFromDBSilently();
                })
        );
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE); // Run forever
        autoRefreshTimeline.play(); // Start the timer
    }

    /**
     * ✅ NEW: Stops automatic refresh (call this when closing the window)
     */
    private void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
    }

    private void refreshDataFromDBSilently() {
        try {
            // Get fresh data from database
            List<projectagricole> newData = service.afficher();

            // Check if any status has changed
            boolean statusChanged = hasStatusChanged(allProjects, newData);

            // Update the data
            allProjects = newData;
            updateCardsDisplay();
            updateStatistics();

            // Optional: Show notification if status changed
            if (statusChanged) {
                updateLastRefreshTime();
            }

        } catch (SQLException e) {
            // Silent error - don't show alert during auto-refresh
            System.err.println("Auto-refresh error: " + e.getMessage());
        }
    }
    private boolean hasStatusChanged(List<projectagricole> oldList, List<projectagricole> newList) {
        if (oldList.size() != newList.size()) return true;

        for (int i = 0; i < oldList.size(); i++) {
            projectagricole oldProject = oldList.get(i);
            projectagricole newProject = newList.stream()
                    .filter(p -> p.getIdproject() == oldProject.getIdproject())
                    .findFirst()
                    .orElse(null);

            if (newProject != null && !oldProject.getStatut().equals(newProject.getStatut())) {
                return true; // Status changed!
            }
        }
        return false;
    }

    /**
     * ✅ NEW: Updates the last refresh timestamp
     */
    private void updateLastRefreshTime() {
        if (lblLastUpdate != null) {
            lblLastUpdate.setText("Mis à jour: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            );
        }
    }

    /**
     * Shows detailed information dialog for a project
     */
    private void showProjectDetails(projectagricole project) {
        // Create custom dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails du Projet");
        dialog.setHeaderText(null);

        // Create dialog content
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white;");
        content.setPrefWidth(550);

        // Header with project name and icon
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle(
                "-fx-background-color: linear-gradient(to right, #076A39, #095032);" +
                        "-fx-padding: 20;" +
                        "-fx-background-radius: 8;"
        );

        Label headerIcon = new Label(getProjectIcon(project.getStatut()));
        headerIcon.setStyle("-fx-font-size: 36px;");

        VBox headerText = new VBox(5);
        Label projectName = new Label(project.getNomproject());
        projectName.setStyle(
                "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        Label projectId = new Label("Projet #" + project.getIdproject());
        projectId.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: rgba(255,255,255,0.8);"
        );

        headerText.getChildren().addAll(projectName, projectId);
        headerBox.getChildren().addAll(headerIcon, headerText);

        // Status Badge
        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.setPadding(new Insets(10, 0, 0, 0));

        Label statusTitleLabel = new Label("Statut:");
        statusTitleLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-text-fill: #133D03;"
        );

        Label statusBadge = new Label(capitalizeStatus(project.getStatut()));
        statusBadge.getStyleClass().add(getStatusBadgeClass(project.getStatut()));
        statusBadge.setStyle(
                statusBadge.getStyle() +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 6 16;"
        );

        statusRow.getChildren().addAll(statusTitleLabel, statusBadge);

        // Details Grid
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(18);
        detailsGrid.setPadding(new Insets(15, 0, 0, 0));

        // Surface
        addDetailRow(detailsGrid, 0, "🌍 Surface",
                String.format("%.2f Ha", project.getSurface()));

        // Budget
        addDetailRow(detailsGrid, 1, "💰 Budget Demandé",
                String.format("%,.2f DT", project.getBudgetdemande()));

        // Date de Soumission
        addDetailRow(detailsGrid, 2, "📅 Date de Soumission",
                project.getDatesoumission().toLocalDate().format(
                        DateTimeFormatter.ofPattern("dd MMMM yyyy")
                ));

        // Days since submission
        long daysSince = java.time.temporal.ChronoUnit.DAYS.between(
                project.getDatesoumission().toLocalDate(),
                java.time.LocalDate.now()
        );
        addDetailRow(detailsGrid, 3, "⏱️ Soumis depuis",
                daysSince + " jour(s)");

        // Status explanation
        Separator sep = new Separator();
        sep.setPadding(new Insets(10, 0, 10, 0));

        VBox statusExplanation = new VBox(10);
        statusExplanation.setPadding(new Insets(15));
        statusExplanation.setStyle(
                "-fx-background-color: #F5F7F6;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #E0E4E2;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;"
        );

        Label noteTitle = new Label("ℹ️ Information");
        noteTitle.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #076A39;"
        );

        Label noteText = new Label(getStatusExplanation(project.getStatut()));
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

    /**
     * Shows details of the currently selected project from toolbar button
     */
    @FXML
    void showSelectedProjectDetails(ActionEvent event) {
        if (selectedProject == null) {
            // No project selected - show selection dialog
            showProjectSelectionDialog();
        } else {
            // Project is already selected - show its details
            showProjectDetails(selectedProject);
        }
    }

    /**
     * Shows a dialog to select a project when clicking Details without selection
     */
    private void showProjectSelectionDialog() {
        if (allProjects == null || allProjects.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucun projet",
                    "Aucun projet disponible. Veuillez d'abord créer un projet.");
            return;
        }

        // Create selection dialog
        Dialog<projectagricole> dialog = new Dialog<>();
        dialog.setTitle("Sélectionner un Projet");
        dialog.setHeaderText("Choisissez un projet pour voir ses détails");

        // Create list view with all projects
        ListView<projectagricole> listView = new ListView<>();
        listView.getItems().addAll(allProjects);
        listView.setPrefHeight(400);
        listView.setPrefWidth(500);

        // Custom cell factory to display project names nicely
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

                    // Icon based on status
                    Label icon = new Label(getProjectIcon(project.getStatut()));
                    icon.setStyle("-fx-font-size: 24px;");

                    // Project info
                    VBox info = new VBox(5);
                    Label name = new Label(project.getNomproject());
                    name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #133D03;");

                    Label details = new Label(String.format("ID: %d | Surface: %.2f Ha | Budget: %,.2f DT",
                            project.getIdproject(), project.getSurface(), project.getBudgetdemande()));
                    details.setStyle("-fx-font-size: 11px; -fx-text-fill: #848A86;");

                    info.getChildren().addAll(name, details);

                    // Status badge
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

        // Set initial selection to first project
        if (!allProjects.isEmpty()) {
            listView.getSelectionModel().select(0);
        }

        // Dialog content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label instruction = new Label("Double-cliquez sur un projet ou sélectionnez et cliquez sur OK");
        instruction.setStyle("-fx-font-size: 12px; -fx-text-fill: #6C757D;");

        content.getChildren().addAll(instruction, listView);
        dialog.getDialogPane().setContent(content);

        // Add buttons
        ButtonType okButton = new ButtonType("Voir Détails", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelButton);

        // Enable OK button only when project is selected
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
                projectagricole selected = listView.getSelectionModel().getSelectedItem();
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
        dialog.showAndWait().ifPresent(project -> {
            selectedProject = project;
            showProjectDetails(project);
        });
    }
}