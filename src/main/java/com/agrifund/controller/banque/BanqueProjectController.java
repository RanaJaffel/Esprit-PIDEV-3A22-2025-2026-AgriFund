package com.agrifund.controller.banque;

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
import java.time.LocalDateTime;

import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

public class BanqueProjectController implements Initializable {

    // ============================================================================
    // FXML FIELDS
    // ============================================================================
    @FXML
    private FlowPane projectsContainer;

    @FXML
    private TextField tfSearchProject;
    @FXML
    private ComboBox<String> cbFilterStatut;

    @FXML
    private Label lblTotalProjects;
    @FXML
    private Label lblPendingProjects;
    @FXML
    private Label lblTotalBudget;
    @FXML
    private Label lblPendingBudget;

    // ============================================================================
    // SHARED FIELDS
    // ============================================================================
    private List<ProjectWithAgriculteur> allProjects = new ArrayList<>();
    private projectagricoleCRUD service;
    private Timeline autoRefreshTimeline;
    private static final int REFRESH_INTERVAL_SECONDS = 5;

    // ============================================================================
    // INITIALIZATION
    // ============================================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        service = new projectagricoleCRUD();

        // Initialize filter ComboBox
        if (cbFilterStatut != null) {
            cbFilterStatut.getItems().addAll("Tous les statuts", "en cours", "accepte", "refuse");
            cbFilterStatut.setValue("Tous les statuts");
            cbFilterStatut.setOnAction(e -> updateCardsDisplay());
        }

        // Setup search listener
        if (tfSearchProject != null) {
            tfSearchProject.textProperty().addListener((obs, old, newVal) -> updateCardsDisplay());
        }

        // Load data
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

            updateCardsDisplay();
            updateStatistics();

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            allProjects = new ArrayList<>();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les projets: " + e.getMessage());
        }
    }

    // ============================================================================
    // STATISTICS
    // ============================================================================

    private void updateStatistics() {
        int totalCount = allProjects.size();
        long pendingCount = allProjects.stream().filter(p -> "en cours".equals(p.getStatut())).count();

        BigDecimal totalBudget = allProjects.stream()
                .map(ProjectWithAgriculteur::getBudgetdemande)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingBudget = allProjects.stream()
                .filter(p -> "en cours".equals(p.getStatut()))
                .map(ProjectWithAgriculteur::getBudgetdemande)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (lblTotalProjects != null) lblTotalProjects.setText(String.valueOf(totalCount));
        if (lblPendingProjects != null) lblPendingProjects.setText(String.valueOf(pendingCount));
        if (lblTotalBudget != null) lblTotalBudget.setText(String.format("%,.2f DT", totalBudget));
        if (lblPendingBudget != null) lblPendingBudget.setText(String.format("%,.2f DT", pendingBudget));
    }

    // ============================================================================
    // DISPLAY
    // ============================================================================

    private void updateCardsDisplay() {
        if (projectsContainer == null) return;

        projectsContainer.getChildren().clear();

        String searchText = tfSearchProject != null ? tfSearchProject.getText().toLowerCase() : "";
        String filterStatut = cbFilterStatut != null ? cbFilterStatut.getValue() : "Tous les statuts";

        List<ProjectWithAgriculteur> filteredList = allProjects.stream()
                .filter(p -> {
                    boolean matchesSearch = searchText.isEmpty()
                            || p.getNomproject().toLowerCase().contains(searchText)
                            || p.getAgriculteurNomComplet().toLowerCase().contains(searchText);
                    boolean matchesStatus = filterStatut.equals("Tous les statuts")
                            || p.getStatut().equals(filterStatut);
                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toList());

        if (filteredList.isEmpty()) {
            Label emptyLabel = new Label("🏦 Aucun projet trouvé.");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #848A86; -fx-padding: 50;");
            projectsContainer.getChildren().add(emptyLabel);
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
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F8FFF8);" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: rgba(178,217,68,0.4);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 16;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(7,106,57,0.12), 10, 0, 0, 3);" +
                "-fx-cursor: hand;");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Icon circle
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(48, 48);
        iconCircle.setMaxSize(48, 48);
        String iconBgColor = switch (project.getStatut().toLowerCase()) {
            case "accepte" -> "rgba(8,150,71,0.15)";
            case "refuse" -> "rgba(198,40,40,0.1)";
            default -> "rgba(225,179,35,0.15)";
        };
        iconCircle.setStyle("-fx-background-color: " + iconBgColor + "; -fx-background-radius: 12;");

        Label icon = new Label(getProjectIcon(project.getStatut()));
        icon.setStyle("-fx-font-size: 26px;");
        iconCircle.getChildren().add(icon);

        VBox titleBox = new VBox(4);
        Label title = new Label(project.getNomproject());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #076A39;");
        title.setWrapText(true);
        title.setMaxWidth(220);

        Label budgetLabel = new Label(String.format("💰 %,.2f DT", project.getBudgetdemande()));
        budgetLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #089647;");

        titleBox.getChildren().addAll(title, budgetLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Status badge
        Label statusBadge = new Label(capitalizeStatus(project.getStatut()));
        statusBadge.setStyle(getStatusBadgeStyle(project.getStatut()));

        header.getChildren().addAll(iconCircle, titleBox, statusBadge);

        // Agriculteur info
        HBox agriculteurBox = new HBox(10);
        agriculteurBox.setAlignment(Pos.CENTER_LEFT);
        agriculteurBox.setStyle("-fx-background-color: rgba(178,217,68,0.1); -fx-padding: 12 14; -fx-background-radius: 10;");

        Label farmerIcon = new Label("👨‍🌾");
        farmerIcon.setStyle("-fx-font-size: 20px;");

        VBox farmerInfo = new VBox(3);
        Label farmerName = new Label(project.getAgriculteurNomComplet());
        farmerName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #133D03;");

        HBox contactRow = new HBox(15);
        Label emailLabel = new Label("📧 " + project.getAgriculteurEmail());
        emailLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #848A86;");

        String tel = project.getAgriculteurTel() != null ? project.getAgriculteurTel() : "N/A";
        Label telLabel = new Label("📱 " + tel);
        telLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #848A86;");

        contactRow.getChildren().addAll(emailLabel, telLabel);
        farmerInfo.getChildren().addAll(farmerName, contactRow);

        // Badge vérifié
        VBox badgeBox = new VBox();
        badgeBox.setAlignment(Pos.CENTER_RIGHT);
        if (project.isCompteVerifie()) {
            Label verifiedBadge = new Label("✓ Vérifié");
            verifiedBadge.setStyle("-fx-background-color: #089647; -fx-text-fill: white; " +
                    "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;");
            badgeBox.getChildren().add(verifiedBadge);
        } else {
            Label unverifiedBadge = new Label("⚠ Non vérifié");
            unverifiedBadge.setStyle("-fx-background-color: #E1B323; -fx-text-fill: #133D03; " +
                    "-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;");
            badgeBox.getChildren().add(unverifiedBadge);
        }

        HBox.setHgrow(farmerInfo, Priority.ALWAYS);
        agriculteurBox.getChildren().addAll(farmerIcon, farmerInfo, badgeBox);

        // Détails
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(8);

        addCardDetailRow(detailsGrid, 0, "🌾 Surface:", String.format("%.2f Ha", project.getSurface()));
        addCardDetailRow(detailsGrid, 1, "📅 Soumission:", project.getDatesoumission().toString());
        addCardDetailRow(detailsGrid, 2, "🌱 Culture:", project.getTypeCulture() != null ? project.getTypeCulture() : "N/A");
        addCardDetailRow(detailsGrid, 3, "🏠 Ferme:",
                project.getSuperficieFerme() != null ? project.getSuperficieFerme() + " Ha" : "N/A");

        // Boutons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10, 0, 0, 0));

        Button btnDetails = new Button("📋 Voir Dossier");
        btnDetails.setStyle("-fx-background-color: linear-gradient(to right, #076A39, #089647); -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 12px;");
        btnDetails.setOnAction(e -> showProjectDetails(project));

        // Hover effect
        btnDetails.setOnMouseEntered(e ->
                btnDetails.setStyle("-fx-background-color: linear-gradient(to right, #089647, #B2D944); -fx-text-fill: #133D03; " +
                        "-fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 12px;")
        );
        btnDetails.setOnMouseExited(e ->
                btnDetails.setStyle("-fx-background-color: linear-gradient(to right, #076A39, #089647); -fx-text-fill: white; " +
                        "-fx-padding: 10 20; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 12px;")
        );

        actions.getChildren().add(btnDetails);

        // Boutons décision si en cours
        if ("en cours".equals(project.getStatut())) {
            Button btnAccept = new Button("✓ Accepter");
            btnAccept.setStyle("-fx-background-color: rgba(8,150,71,0.12); -fx-text-fill: #089647; " +
                    "-fx-padding: 10 16; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold;" +
                    "-fx-border-color: #089647; -fx-border-radius: 10; -fx-border-width: 1.5;");
            btnAccept.setOnAction(e -> handleDecision(project, "accepte"));

            btnAccept.setOnMouseEntered(e ->
                    btnAccept.setStyle("-fx-background-color: #089647; -fx-text-fill: white; " +
                            "-fx-padding: 10 16; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold;" +
                            "-fx-border-color: #089647; -fx-border-radius: 10; -fx-border-width: 1.5;")
            );
            btnAccept.setOnMouseExited(e ->
                    btnAccept.setStyle("-fx-background-color: rgba(8,150,71,0.12); -fx-text-fill: #089647; " +
                            "-fx-padding: 10 16; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold;" +
                            "-fx-border-color: #089647; -fx-border-radius: 10; -fx-border-width: 1.5;")
            );

            Button btnRefuse = new Button("✗ Refuser");
            btnRefuse.setStyle("-fx-background-color: rgba(198,40,40,0.1); -fx-text-fill: #C62828; " +
                    "-fx-padding: 10 16; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold;" +
                    "-fx-border-color: #C62828; -fx-border-radius: 10; -fx-border-width: 1.5;");
            btnRefuse.setOnAction(e -> handleDecision(project, "refuse"));

            btnRefuse.setOnMouseEntered(e ->
                    btnRefuse.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; " +
                            "-fx-padding: 10 16; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold;" +
                            "-fx-border-color: #C62828; -fx-border-radius: 10; -fx-border-width: 1.5;")
            );
            btnRefuse.setOnMouseExited(e ->
                    btnRefuse.setStyle("-fx-background-color: rgba(198,40,40,0.1); -fx-text-fill: #C62828; " +
                            "-fx-padding: 10 16; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold;" +
                            "-fx-border-color: #C62828; -fx-border-radius: 10; -fx-border-width: 1.5;")
            );

            actions.getChildren().addAll(btnAccept, btnRefuse);
        }

        // Separator avec style
        Separator sep = new Separator();
        sep.setStyle("-fx-padding: 5 0;");

        card.getChildren().addAll(header, agriculteurBox, sep, detailsGrid, actions);

        // Hover effect pour la carte
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F0FFF0);" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #089647;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 16;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(8,150,71,0.25), 16, 0, 0, 4);" +
                        "-fx-cursor: hand;")
        );
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: linear-gradient(to bottom right, #FFFFFF, #F8FFF8);" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: rgba(178,217,68,0.4);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 16;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(7,106,57,0.12), 10, 0, 0, 3);" +
                        "-fx-cursor: hand;")
        );

        return card;
    }

    private void addCardDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #848A86;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #076A39;");

        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }


    // ============================================================================
    // DECISION HANDLING
    // ============================================================================

    private void handleDecision(ProjectWithAgriculteur project, String decision) {
        String actionText = "accepte".equals(decision) ? "accepter" : "refuser";
        String resultText = "accepte".equals(decision) ? "accepté" : "refusé";

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de décision");
        confirmAlert.setHeaderText("Voulez-vous " + actionText + " ce projet?");
        confirmAlert.setContentText("Projet: " + project.getNomproject() +
                "\nAgriculteur: " + project.getAgriculteurNomComplet() +
                "\nBudget: " + String.format("%,.2f DT", project.getBudgetdemande()));

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Créer un objet projectagricole pour la mise à jour
                    projectagricole p = new projectagricole();
                    p.setIdproject(project.getIdproject());
                    p.setAgriculteurId(project.getAgriculteurId());
                    p.setNomproject(project.getNomproject());
                    p.setSurface(project.getSurface());
                    p.setBudgetdemande(project.getBudgetdemande());
                    p.setStatut(decision);
                    p.setDatesoumission(project.getDatesoumission());
                    p.setLatitude(project.getLatitude());
                    p.setLongitude(project.getLongitude());

                    service.modifier(p);
                    showAlert(Alert.AlertType.INFORMATION, "Décision enregistrée",
                            "Le projet a été " + resultText + " avec succès!");
                    refreshDataFromDB();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Erreur lors de l'enregistrement de la décision: " + e.getMessage());
                }
            }
        });
    }

    // ============================================================================
    // DETAILS DIALOG
    // ============================================================================

    private void showProjectDetails(ProjectWithAgriculteur project) {
        Stage detailStage = new Stage();
        detailStage.initModality(Modality.APPLICATION_MODAL);
        detailStage.setTitle("Dossier de Demande — " + project.getNomproject());

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: #FAFAFA;");

        // Header
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle("-fx-background-color: linear-gradient(to right, #1565C0, #1976D2); " +
                "-fx-padding: 18 24; -fx-background-radius: 10;");

        Label iconLarge = new Label("🏦");
        iconLarge.setStyle("-fx-font-size: 40px;");

        VBox titleBox = new VBox(4);
        Label titleLabel = new Label("Demande de Financement");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label(project.getNomproject());
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9);");

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        headerBox.getChildren().addAll(iconLarge, titleBox);

        // Montant demandé (highlight)
        HBox amountBox = new HBox();
        amountBox.setAlignment(Pos.CENTER);
        amountBox.setStyle("-fx-background-color: #E8F5E9; -fx-padding: 20; -fx-background-radius: 10;");

        VBox amountContent = new VBox(5);
        amountContent.setAlignment(Pos.CENTER);
        Label amountTitle = new Label("💰 Montant Demandé");
        amountTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        Label amountValue = new Label(String.format("%,.2f DT", project.getBudgetdemande()));
        amountValue.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        amountContent.getChildren().addAll(amountTitle, amountValue);
        amountBox.getChildren().add(amountContent);

        // Section Demandeur
        VBox demandeurSection = new VBox(10);
        demandeurSection.setStyle("-fx-background-color: white; -fx-padding: 15; " +
                "-fx-background-radius: 8; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8;");

        Label sectionTitle1 = new Label("👨‍🌾 Profil du Demandeur");
        sectionTitle1.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");

        GridPane demandeurGrid = new GridPane();
        demandeurGrid.setHgap(20);
        demandeurGrid.setVgap(8);

        addDetailRow(demandeurGrid, 0, "Nom complet:", project.getAgriculteurNomComplet());
        addDetailRow(demandeurGrid, 1, "Email:", project.getAgriculteurEmail());
        addDetailRow(demandeurGrid, 2, "Téléphone:", project.getAgriculteurTel() != null ? project.getAgriculteurTel() : "Non renseigné");
        addDetailRow(demandeurGrid, 3, "Adresse ferme:", project.getAdresseFerme() != null ? project.getAdresseFerme() : "Non renseignée");
        addDetailRow(demandeurGrid, 4, "Superficie totale:",
                project.getSuperficieFerme() != null ? project.getSuperficieFerme() + " Hectares" : "Non renseignée");
        addDetailRow(demandeurGrid, 5, "Type de culture:", project.getTypeCulture() != null ? project.getTypeCulture() : "Non renseigné");
        addDetailRow(demandeurGrid, 6, "Compte vérifié:", project.isCompteVerifie() ? "✓ Oui" : "✗ Non");

        demandeurSection.getChildren().addAll(sectionTitle1, demandeurGrid);

        // Section Projet
        VBox projetSection = new VBox(10);
        projetSection.setStyle("-fx-background-color: white; -fx-padding: 15; " +
                "-fx-background-radius: 8; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8;");

        Label sectionTitle2 = new Label("📋 Détails du Projet");
        sectionTitle2.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1565C0;");

        GridPane projetGrid = new GridPane();
        projetGrid.setHgap(20);
        projetGrid.setVgap(8);

        addDetailRow(projetGrid, 0, "N° Dossier:", "PRJ-" + project.getIdproject());
        addDetailRow(projetGrid, 1, "Surface projet:", String.format("%.2f Hectares", project.getSurface()));
        addDetailRow(projetGrid, 2, "Date soumission:", project.getDatesoumission().toString());
        addDetailRow(projetGrid, 3, "Statut actuel:", capitalizeStatus(project.getStatut()));

        if (project.hasLocation()) {
            addDetailRow(projetGrid, 4, "Coordonnées GPS:",
                    String.format("%.4f, %.4f", project.getLatitude(), project.getLongitude()));
        }

        projetSection.getChildren().addAll(sectionTitle2, projetGrid);

        // Boutons
        HBox btnRow = new HBox(15);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.setPadding(new Insets(10, 0, 0, 0));

        Button closeBtn = new Button("✖  Fermer");
        closeBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> detailStage.close());

        btnRow.getChildren().add(closeBtn);

        // Ajouter boutons de décision si en cours
        if ("en cours".equals(project.getStatut())) {
            Button acceptBtn = new Button("✓ Approuver");
            acceptBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand;");
            acceptBtn.setOnAction(e -> {
                detailStage.close();
                handleDecision(project, "accepte");
            });

            Button refuseBtn = new Button("✗ Rejeter");
            refuseBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand;");
            refuseBtn.setOnAction(e -> {
                detailStage.close();
                handleDecision(project, "refuse");
            });

            btnRow.getChildren().addAll(0, List.of(acceptBtn, refuseBtn));
        }

        content.getChildren().addAll(headerBox, amountBox, demandeurSection, projetSection, btnRow);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #FAFAFA;");

        detailStage.setScene(new Scene(scrollPane, 600, 700));
        detailStage.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333;");

        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
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
        fileChooser.setTitle("Sauvegarder le rapport");
        fileChooser.setInitialFileName("rapport_demandes_banque_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        File file = fileChooser.showSaveDialog(projectsContainer.getScene().getWindow());
        if (file != null) {
            try {
                generatePDFReport(file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Rapport généré: " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur: " + e.getMessage());
            }
        }
    }

    private void generatePDFReport(String filePath) throws Exception {
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Rapport des Demandes de Financement")
                .setFontSize(18).setBold().setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Service Bancaire - " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFontSize(10).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));

        // Stats
        long pendingCount = allProjects.stream().filter(p -> "en cours".equals(p.getStatut())).count();
        BigDecimal pendingBudget = allProjects.stream()
                .filter(p -> "en cours".equals(p.getStatut()))
                .map(ProjectWithAgriculteur::getBudgetdemande)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        document.add(new Paragraph("Résumé").setFontSize(14).setBold());
        document.add(new Paragraph("Demandes en attente: " + pendingCount));
        document.add(new Paragraph("Montant total en attente: " + String.format("%,.2f DT", pendingBudget)));
        document.add(new Paragraph("\n"));

        // Table
        float[] columnWidths = {1, 2.5f, 2.5f, 2f, 1.5f, 1.5f};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        Color headerColor = new DeviceRgb(21, 101, 192);
        String[] headers = {"N°", "Projet", "Demandeur", "Montant", "Statut", "Date"};

        for (String header : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(header).setFontSize(9))
                    .setBackgroundColor(headerColor)
                    .setFontColor(ColorConstants.WHITE)
                    .setBold());
        }

        for (ProjectWithAgriculteur p : allProjects) {
            table.addCell(new Cell().add(new Paragraph("PRJ-" + p.getIdproject()).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(p.getNomproject()).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(p.getAgriculteurNomComplet()).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(String.format("%,.0f DT", p.getBudgetdemande())).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(capitalizeStatus(p.getStatut())).setFontSize(8)));
            table.addCell(new Cell().add(new Paragraph(p.getDatesoumission().toString()).setFontSize(8)));
        }

        document.add(table);
        document.close();
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
        showAlert(Alert.AlertType.INFORMATION, "Actualisation", "Les demandes ont été actualisées!");
    }

    // ============================================================================
    // UTILITY
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
            case "accepte": return "Approuvé";
            case "refuse": return "Rejeté";
            default: return "En attente";
        }
    }

    private String getStatusBadgeStyle(String statut) {
        String baseStyle = "-fx-padding: 6 16; -fx-background-radius: 15; -fx-font-size: 11px; -fx-font-weight: bold;";
        switch (statut.toLowerCase()) {
            case "accepte":
                return baseStyle + "-fx-background-color: linear-gradient(to right, #089647, #076A39); -fx-text-fill: white;";
            case "refuse":
                return baseStyle + "-fx-background-color: linear-gradient(to right, #C62828, #B71C1C); -fx-text-fill: white;";
            default:
                return baseStyle + "-fx-background-color: linear-gradient(to right, #E1B323, #9A951F); -fx-text-fill: #133D03;";
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