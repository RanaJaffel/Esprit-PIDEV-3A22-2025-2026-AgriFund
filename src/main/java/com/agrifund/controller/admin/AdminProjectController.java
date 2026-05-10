package com.agrifund.controller.admin;

import com.agrifund.entities.ProjectWithAgriculteur;
import com.agrifund.entities.projectagricole;
import com.agrifund.services.projectagricoleCRUD;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

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

import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

public class AdminProjectController implements Initializable {

    // ============================================================================
    // FXML FIELDS
    // ============================================================================
    @FXML private FlowPane projectsContainer;
    @FXML private TextField tfSearchProject;
    @FXML private ComboBox<String> cbFilterStatut;
    @FXML private ComboBox<String> cbFilterAgriculteur;

    @FXML private Label lblTotalProjects;
    @FXML private Label lblAcceptedProjects;
    @FXML private Label lblInProgressProjects;
    @FXML private Label lblRefusedProjects;

    @FXML private Label lblTotalBudget;
    @FXML private Label lblTotalSurface;
    @FXML private Label lblLastUpdate;

    // ============================================================================
    // SHARED FIELDS
    // ============================================================================
    private List<ProjectWithAgriculteur> allProjects = new ArrayList<>();
    private projectagricoleCRUD service;
    private Timeline autoRefreshTimeline;
    private static final int REFRESH_INTERVAL_SECONDS = 30;

    // Couleurs AgriFund
    private static final String PRIMARY_GREEN = "#089647";
    private static final String DARK_GREEN = "#076A39";
    private static final String FOREST_GREEN = "#095032";
    private static final String OLIVE_DARK = "#133D03";
    private static final String OLIVE = "#476C1A";
    private static final String YELLOW = "#E1B323";
    private static final String LIGHT_GREEN = "#B2D944";
    private static final String GRAY = "#848A86";

    // ============================================================================
    // INITIALIZATION
    // ============================================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        service = new projectagricoleCRUD();

        if (cbFilterStatut != null) {
            cbFilterStatut.getItems().addAll("Tous les statuts", "en cours", "accepte", "refuse");
            cbFilterStatut.setValue("Tous les statuts");
            cbFilterStatut.setOnAction(e -> updateCardsDisplay());
        }

        if (cbFilterAgriculteur != null) {
            cbFilterAgriculteur.getItems().add("Tous les agriculteurs");
            cbFilterAgriculteur.setValue("Tous les agriculteurs");
            cbFilterAgriculteur.setOnAction(e -> updateCardsDisplay());
        }

        if (tfSearchProject != null) {
            tfSearchProject.textProperty().addListener((obs, old, newVal) -> updateCardsDisplay());
        }

        if (projectsContainer != null) {
            refreshDataFromDB();
            startAutoRefresh();
        }
    }

    // ============================================================================
    // DATA LOADING
    // ============================================================================

    public void refreshDataFromDB() {
        try {
            allProjects = service.afficherTousAvecAgriculteur();
            if (allProjects == null) {
                allProjects = new ArrayList<>();
            }
            populateAgriculteurFilter();
            updateCardsDisplay();
            updateStatistics();
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            allProjects = new ArrayList<>();
            updateCardsDisplay();
            updateStatistics();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les projets: " + e.getMessage());
        }
    }

    private void populateAgriculteurFilter() {
        if (cbFilterAgriculteur == null) return;

        String currentSelection = cbFilterAgriculteur.getValue();
        cbFilterAgriculteur.getItems().clear();
        cbFilterAgriculteur.getItems().add("Tous les agriculteurs");

        List<String> agriculteurs = allProjects.stream()
                .map(ProjectWithAgriculteur::getAgriculteurNomComplet)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        cbFilterAgriculteur.getItems().addAll(agriculteurs);

        if (currentSelection != null && cbFilterAgriculteur.getItems().contains(currentSelection)) {
            cbFilterAgriculteur.setValue(currentSelection);
        } else {
            cbFilterAgriculteur.setValue("Tous les agriculteurs");
        }
    }

    // ============================================================================
    // STATISTICS
    // ============================================================================

    private void updateStatistics() {
        int totalCount = allProjects.size();
        long acceptedCount = allProjects.stream().filter(p -> "accepte".equals(p.getStatut())).count();
        long inProgressCount = allProjects.stream().filter(p -> "en cours".equals(p.getStatut())).count();
        long refusedCount = allProjects.stream().filter(p -> "refuse".equals(p.getStatut())).count();

        if (lblTotalProjects != null) lblTotalProjects.setText(String.valueOf(totalCount));
        if (lblAcceptedProjects != null) lblAcceptedProjects.setText(String.valueOf(acceptedCount));
        if (lblInProgressProjects != null) lblInProgressProjects.setText(String.valueOf(inProgressCount));
        if (lblRefusedProjects != null) lblRefusedProjects.setText(String.valueOf(refusedCount));

        updateFooterStats();
    }

    private void updateFooterStats() {
        if (allProjects.isEmpty()) {
            if (lblTotalBudget != null) lblTotalBudget.setText("0.00 DT");
            if (lblTotalSurface != null) lblTotalSurface.setText("0.00 Ha");
            if (lblLastUpdate != null) lblLastUpdate.setText("--");
            return;
        }

        BigDecimal totalBudget = allProjects.stream()
                .map(ProjectWithAgriculteur::getBudgetdemande)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double totalSurface = allProjects.stream()
                .mapToDouble(ProjectWithAgriculteur::getSurface)
                .sum();

        if (lblTotalBudget != null) lblTotalBudget.setText(String.format("%,.2f DT", totalBudget));
        if (lblTotalSurface != null) lblTotalSurface.setText(String.format("%.2f Ha", totalSurface));

        if (lblLastUpdate != null) {
            lblLastUpdate.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
    }

    // ============================================================================
    // DISPLAY
    // ============================================================================

    private void updateCardsDisplay() {
        if (projectsContainer == null) return;

        projectsContainer.getChildren().clear();

        String searchText = tfSearchProject != null ? tfSearchProject.getText().toLowerCase() : "";
        String filterStatut = cbFilterStatut != null ? cbFilterStatut.getValue() : "Tous les statuts";
        String filterAgriculteur = cbFilterAgriculteur != null ? cbFilterAgriculteur.getValue() : "Tous les agriculteurs";

        List<ProjectWithAgriculteur> filteredList = allProjects.stream()
                .filter(p -> {
                    boolean matchesSearch = searchText.isEmpty()
                            || p.getNomproject().toLowerCase().contains(searchText)
                            || p.getAgriculteurNomComplet().toLowerCase().contains(searchText);
                    boolean matchesStatus = filterStatut.equals("Tous les statuts")
                            || p.getStatut().equals(filterStatut);
                    boolean matchesAgriculteur = filterAgriculteur.equals("Tous les agriculteurs")
                            || p.getAgriculteurNomComplet().equals(filterAgriculteur);
                    return matchesSearch && matchesStatus && matchesAgriculteur;
                })
                .collect(Collectors.toList());

        if (filteredList.isEmpty()) {
            VBox emptyState = new VBox(15);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(60));
            emptyState.setStyle("-fx-background-color: white; -fx-background-radius: 16;");

            Label emptyIcon = new Label("📋");
            emptyIcon.setStyle("-fx-font-size: 48px; -fx-opacity: 0.5;");

            Label emptyLabel = new Label("Aucun projet trouvé");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: " + GRAY + "; -fx-font-weight: bold;");

            Label emptySubtitle = new Label("Modifiez vos critères de recherche");
            emptySubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #a0a5a0;");

            emptyState.getChildren().addAll(emptyIcon, emptyLabel, emptySubtitle);
            projectsContainer.getChildren().add(emptyState);
        } else {
            for (ProjectWithAgriculteur p : filteredList) {
                projectsContainer.getChildren().add(createProjectCard(p));
            }
        }
    }

    private VBox createProjectCard(ProjectWithAgriculteur project) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setMaxWidth(420);
        card.setPrefWidth(420);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e0e5e0; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: " + PRIMARY_GREEN + "; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(8, 150, 71, 0.15), 15, 0, 0, 5);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e0e5e0; -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);"));

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getProjectIcon(project.getStatut()));
        icon.setStyle("-fx-font-size: 32px;");

        VBox titleBox = new VBox(3);
        Label title = new Label(project.getNomproject());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + OLIVE_DARK + ";");
        title.setWrapText(true);

        Label idLabel = new Label("ID: #" + project.getIdproject());
        idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GRAY + ";");

        titleBox.getChildren().addAll(title, idLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        header.getChildren().addAll(icon, titleBox);

        // Agriculteur info
        HBox agriculteurBox = new HBox(10);
        agriculteurBox.setAlignment(Pos.CENTER_LEFT);
        agriculteurBox.setStyle("-fx-background-color: rgba(8, 150, 71, 0.08); -fx-padding: 12 15; -fx-background-radius: 10;");

        Label farmerIcon = new Label("👨‍🌾");
        farmerIcon.setStyle("-fx-font-size: 20px;");

        VBox farmerInfo = new VBox(3);
        HBox.setHgrow(farmerInfo, Priority.ALWAYS);

        Label farmerName = new Label(project.getAgriculteurNomComplet());
        farmerName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + PRIMARY_GREEN + ";");

        Label farmerEmail = new Label(project.getAgriculteurEmail());
        farmerEmail.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GRAY + ";");

        farmerInfo.getChildren().addAll(farmerName, farmerEmail);
        agriculteurBox.getChildren().addAll(farmerIcon, farmerInfo);

        if (project.isCompteVerifie()) {
            Label verifiedBadge = new Label("✓ Vérifié");
            verifiedBadge.setStyle("-fx-background-color: " + PRIMARY_GREEN + "; -fx-text-fill: white; " +
                    "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;");
            agriculteurBox.getChildren().add(verifiedBadge);
        }

        // Status badge
        Label statusBadge = new Label(capitalizeStatus(project.getStatut()));
        statusBadge.setStyle(getStatusBadgeStyle(project.getStatut()));

        // Détails du projet
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(8);
        detailsGrid.setStyle("-fx-padding: 10 0;");

        addCardDetailRow(detailsGrid, 0, "🌾 Surface:", String.format("%.2f Ha", project.getSurface()));
        addCardDetailRow(detailsGrid, 1, "💰 Budget:", String.format("%,.2f DT", project.getBudgetdemande()));
        addCardDetailRow(detailsGrid, 2, "📅 Date:", project.getDatesoumission().toString());
        addCardDetailRow(detailsGrid, 3, "🌱 Culture:", project.getTypeCulture() != null ? project.getTypeCulture() : "N/A");

        // Boutons d'action
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10, 0, 0, 0));

        Button btnView = new Button("👁 Détails");
        btnView.setStyle("-fx-background-color: " + PRIMARY_GREEN + "; -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        btnView.setOnAction(e -> showProjectDetails(project));
        btnView.setOnMouseEntered(e -> btnView.setStyle("-fx-background-color: " + DARK_GREEN + "; -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;"));
        btnView.setOnMouseExited(e -> btnView.setStyle("-fx-background-color: " + PRIMARY_GREEN + "; -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;"));

        Button btnDelete = new Button("🗑 Supprimer");
        btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        btnDelete.setOnAction(e -> handleDelete(project));
        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-background-color: #c82333; -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;"));
        btnDelete.setOnMouseExited(e -> btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;"));

        actions.getChildren().addAll(btnView, btnDelete);

        card.getChildren().addAll(header, agriculteurBox, statusBadge, new Separator(), detailsGrid, actions);

        return card;
    }

    private void addCardDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + GRAY + ";");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + OLIVE_DARK + ";");

        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }

    // ============================================================================
    // DETAILS DIALOG
    // ============================================================================

    private void showProjectDetails(ProjectWithAgriculteur project) {
        Stage detailStage = new Stage();
        detailStage.initModality(Modality.APPLICATION_MODAL);
        detailStage.setTitle("Détails du Projet — " + project.getNomproject());

        VBox content = new VBox(18);
        content.setPadding(new Insets(0));
        content.setStyle("-fx-background-color: #f8faf8;");

        // Header
        HBox headerBox = new HBox(18);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle("-fx-background-color: linear-gradient(to right, " + PRIMARY_GREEN + ", " + DARK_GREEN + "); " +
                "-fx-padding: 25;");

        Label iconLarge = new Label(getProjectIcon(project.getStatut()));
        iconLarge.setStyle("-fx-font-size: 42px;");

        VBox titleBox = new VBox(5);
        Label titleLabel = new Label(project.getNomproject());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label statusLabel = new Label(capitalizeStatus(project.getStatut()));
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + LIGHT_GREEN + "; -fx-font-weight: bold;");

        titleBox.getChildren().addAll(titleLabel, statusLabel);
        headerBox.getChildren().addAll(iconLarge, titleBox);

        // Content sections
        VBox sectionsBox = new VBox(15);
        sectionsBox.setPadding(new Insets(20));

        // Section Agriculteur
        VBox agriculteurSection = new VBox(10);
        agriculteurSection.setStyle("-fx-background-color: white; -fx-padding: 18; -fx-background-radius: 12; " +
                "-fx-border-color: #e0e5e0; -fx-border-radius: 12;");

        Label sectionTitle1 = new Label("👨‍🌾 Informations Agriculteur");
        sectionTitle1.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + PRIMARY_GREEN + ";");

        GridPane agriculteurGrid = new GridPane();
        agriculteurGrid.setHgap(20);
        agriculteurGrid.setVgap(10);

        addDetailRow(agriculteurGrid, 0, "Nom complet:", project.getAgriculteurNomComplet());
        addDetailRow(agriculteurGrid, 1, "Email:", project.getAgriculteurEmail());
        addDetailRow(agriculteurGrid, 2, "Téléphone:", project.getAgriculteurTel() != null ? project.getAgriculteurTel() : "N/A");
        addDetailRow(agriculteurGrid, 3, "Adresse ferme:", project.getAdresseFerme() != null ? project.getAdresseFerme() : "N/A");
        addDetailRow(agriculteurGrid, 4, "Type culture:", project.getTypeCulture() != null ? project.getTypeCulture() : "N/A");
        addDetailRow(agriculteurGrid, 5, "Superficie ferme:",
                project.getSuperficieFerme() != null ? project.getSuperficieFerme() + " Ha" : "N/A");
        addDetailRow(agriculteurGrid, 6, "Compte vérifié:", project.isCompteVerifie() ? "✓ Oui" : "✗ Non");

        agriculteurSection.getChildren().addAll(sectionTitle1, agriculteurGrid);

        // Section Projet
        VBox projetSection = new VBox(10);
        projetSection.setStyle("-fx-background-color: white; -fx-padding: 18; -fx-background-radius: 12; " +
                "-fx-border-color: #e0e5e0; -fx-border-radius: 12;");

        Label sectionTitle2 = new Label("📋 Détails du Projet");
        sectionTitle2.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + OLIVE + ";");

        GridPane projetGrid = new GridPane();
        projetGrid.setHgap(20);
        projetGrid.setVgap(10);

        addDetailRow(projetGrid, 0, "ID Projet:", String.valueOf(project.getIdproject()));
        addDetailRow(projetGrid, 1, "Surface:", String.format("%.2f Hectares", project.getSurface()));
        addDetailRow(projetGrid, 2, "Budget demandé:", String.format("%,.2f DT", project.getBudgetdemande()));
        addDetailRow(projetGrid, 3, "Date soumission:", project.getDatesoumission().toString());
        addDetailRow(projetGrid, 4, "Statut:", capitalizeStatus(project.getStatut()));

        if (project.hasLocation()) {
            addDetailRow(projetGrid, 5, "Latitude:", String.format("%.6f", project.getLatitude()));
            addDetailRow(projetGrid, 6, "Longitude:", String.format("%.6f", project.getLongitude()));
        }

        projetSection.getChildren().addAll(sectionTitle2, projetGrid);

        sectionsBox.getChildren().addAll(agriculteurSection, projetSection);

        // Bouton fermer
        Button closeBtn = new Button("✖  Fermer");
        closeBtn.setStyle("-fx-background-color: " + PRIMARY_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 12 35; -fx-background-radius: 10; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> detailStage.close());

        HBox btnRow = new HBox(closeBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(0, 20, 20, 20));

        content.getChildren().addAll(headerBox, sectionsBox, btnRow);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8faf8;");

        detailStage.setScene(new Scene(scrollPane, 580, 650));
        detailStage.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + GRAY + ";");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + OLIVE_DARK + ";");

        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }

    // ============================================================================
    // DELETE
    // ============================================================================

    private void handleDelete(ProjectWithAgriculteur project) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer le projet: " + project.getNomproject());
        confirmAlert.setContentText("Agriculteur: " + project.getAgriculteurNomComplet() +
                "\n\nÊtes-vous sûr de vouloir supprimer ce projet?\nCette action est irréversible.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.supprimer(project.getIdproject());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "✓ Projet supprimé avec succès!");
                    refreshDataFromDB();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    // ============================================================================
    // EXPORT PDF
    // ============================================================================

    @FXML
    void exportToPDF(ActionEvent event) {
        if (allProjects == null || allProjects.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucune donnée", "Aucun projet à exporter!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le rapport PDF");
        fileChooser.setInitialFileName("rapport_projets_admin_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(projectsContainer.getScene().getWindow());
        if (file != null) {
            try {
                generatePDFReport(file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "✓ Rapport PDF généré!\n\n📁 " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur PDF: " + e.getMessage());
            }
        }
    }

    private void generatePDFReport(String filePath) throws Exception {
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // AgriFund Green
        Color agrifundGreen = new DeviceRgb(8, 150, 71);

        document.add(new Paragraph("🌾 Rapport des Projets Agricoles - AgriFund")
                .setFontSize(20).setBold().setTextAlignment(TextAlignment.CENTER)
                .setFontColor(agrifundGreen));
        document.add(new Paragraph("Généré le: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFontSize(11).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("📊 Statistiques Globales").setFontSize(14).setBold()
                .setFontColor(agrifundGreen));
        document.add(new Paragraph("• Total: " + allProjects.size() + " projets"));
        document.add(new Paragraph("• Acceptés: " + allProjects.stream().filter(p -> "accepte".equals(p.getStatut())).count()));
        document.add(new Paragraph("• En cours: " + allProjects.stream().filter(p -> "en cours".equals(p.getStatut())).count()));
        document.add(new Paragraph("• Refusés: " + allProjects.stream().filter(p -> "refuse".equals(p.getStatut())).count()));
        document.add(new Paragraph("\n"));

        float[] columnWidths = {1, 2.5f, 2.5f, 1.5f, 1.5f, 1.5f, 1.5f};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        String[] headers = {"ID", "Projet", "Agriculteur", "Surface", "Budget", "Statut", "Date"};

        for (String header : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(header).setFontSize(9))
                    .setBackgroundColor(agrifundGreen)
                    .setFontColor(ColorConstants.WHITE)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
        }

        for (ProjectWithAgriculteur p : allProjects) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(p.getIdproject())).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(p.getNomproject()).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(p.getAgriculteurNomComplet()).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f Ha", p.getSurface())).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(String.format("%,.0f DT", p.getBudgetdemande())).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(capitalizeStatus(p.getStatut())).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(p.getDatesoumission().toString()).setFontSize(8)));
        }

        document.add(table);
        document.close();
    }

    // ============================================================================
    // MAP
    // ============================================================================

    @FXML
    void handleShowAllProjectsMap(ActionEvent event) {
        if (allProjects == null || allProjects.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucun projet", "Aucun projet à afficher sur la carte.");
            return;
        }

        java.net.URL mapUrl = getClass().getResource("/com/agrifund/fxml/projects_map.html");
        if (mapUrl == null) {
            showAlert(Alert.AlertType.ERROR, "Fichier manquant", "projects_map.html introuvable");
            return;
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < allProjects.size(); i++) {
            ProjectWithAgriculteur p = allProjects.get(i);
            if (i > 0) json.append(",");
            json.append("{");
            json.append("\"nom\":\"").append(escapeJson(p.getNomproject())).append("\",");
            json.append("\"agriculteur\":\"").append(escapeJson(p.getAgriculteurNomComplet())).append("\",");
            json.append("\"statut\":\"").append(p.getStatut()).append("\",");
            json.append("\"surface\":").append(p.getSurface()).append(",");
            json.append("\"budget\":\"").append(String.format("%,.2f", p.getBudgetdemande())).append("\",");
            json.append("\"date\":\"").append(p.getDatesoumission()).append("\",");
            if (p.hasLocation()) {
                json.append("\"lat\":").append(p.getLatitude()).append(",");
                json.append("\"lng\":").append(p.getLongitude());
            } else {
                json.append("\"lat\":null,\"lng\":null");
            }
            json.append("}");
        }
        json.append("]");

        Stage mapStage = new Stage();
        mapStage.initModality(Modality.APPLICATION_MODAL);
        mapStage.setTitle("🗺 Tous les Projets sur la Carte — AgriFund");
        mapStage.setWidth(1100);
        mapStage.setHeight(720);

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);

        String projectsJson = json.toString();
        engine.getLoadWorker().stateProperty().addListener((obs, oldS, newS) -> {
            if (newS == javafx.concurrent.Worker.State.SUCCEEDED) {
                engine.executeScript("loadProjects(" + projectsJson + ")");
            }
        });

        engine.load(mapUrl.toExternalForm());
        mapStage.setScene(new Scene(new StackPane(webView)));
        mapStage.show();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // ============================================================================
    // AUTO-REFRESH
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
        showAlert(Alert.AlertType.INFORMATION, "Actualisation", "✓ Les projets ont été actualisés!");
    }

    // ============================================================================
    // UTILITY METHODS
    // ============================================================================

    private String getProjectIcon(String statut) {
        switch (statut.toLowerCase()) {
            case "accepte": return "✅";
            case "refuse": return "❌";
            default: return "⏳";
        }
    }

    private String capitalizeStatus(String statut) {
        switch (statut.toLowerCase()) {
            case "accepte": return "Accepté";
            case "refuse": return "Refusé";
            default: return "En cours";
        }
    }

    private String getStatusBadgeStyle(String statut) {
        String baseStyle = "-fx-padding: 6 14; -fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;";
        switch (statut.toLowerCase()) {
            case "accepte":
                return baseStyle + "-fx-background-color: rgba(178, 217, 68, 0.2); -fx-text-fill: " + OLIVE + "; " +
                        "-fx-border-color: " + LIGHT_GREEN + "; -fx-border-radius: 15;";
            case "refuse":
                return baseStyle + "-fx-background-color: rgba(220, 53, 69, 0.1); -fx-text-fill: #dc3545; " +
                        "-fx-border-color: #dc3545; -fx-border-radius: 15;";
            default:
                return baseStyle + "-fx-background-color: rgba(225, 179, 35, 0.15); -fx-text-fill: #9A951F; " +
                        "-fx-border-color: " + YELLOW + "; -fx-border-radius: 15;";
        }
    }

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
