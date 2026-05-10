package com.agrifund.controller.admin;

import com.agrifund.entities.RessourceDetailDTO;
import com.agrifund.services.ressourceprojectCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminRessourceController implements Initializable {

    // ============================================================================
    // FXML FIELDS
    // ============================================================================
    @FXML private FlowPane ressourcesContainer;
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbFilterType;
    @FXML private ComboBox<String> cbFilterStatutProjet;
    @FXML private ComboBox<String> cbFilterStatutRessource;

    @FXML private Label lblTotalRessources;
    @FXML private Label lblTotalAgriculteurs;
    @FXML private Label lblTotalProjets;
    @FXML private Label lblCoutTotal;

    // ============================================================================
    // DATA
    // ============================================================================
    private List<RessourceDetailDTO> allRessources = new ArrayList<>();
    private final ressourceprojectCRUD rService = new ressourceprojectCRUD();

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
        System.out.println("========================================");
        System.out.println("ADMIN - Gestion des Ressources AgriFund");
        System.out.println("========================================");

        setupFilters();
        setupSearchListener();
        refreshDataFromDB();
    }

    private void setupFilters() {
        if (cbFilterType != null) {
            cbFilterType.getItems().clear();
            cbFilterType.getItems().addAll("Tous les types", "equipement", "materiaux", "service");
            cbFilterType.setValue("Tous les types");
            cbFilterType.setOnAction(e -> updateCardsDisplay());
        }

        if (cbFilterStatutProjet != null) {
            cbFilterStatutProjet.getItems().clear();
            cbFilterStatutProjet.getItems().addAll("Tous les statuts", "en_attente", "approuve", "rejete", "en_cours", "termine");
            cbFilterStatutProjet.setValue("Tous les statuts");
            cbFilterStatutProjet.setOnAction(e -> updateCardsDisplay());
        }

        if (cbFilterStatutRessource != null) {
            cbFilterStatutRessource.getItems().clear();
            cbFilterStatutRessource.getItems().addAll("Tous", "prevu", "achete");
            cbFilterStatutRessource.setValue("Tous");
            cbFilterStatutRessource.setOnAction(e -> updateCardsDisplay());
        }
    }

    private void setupSearchListener() {
        if (tfSearch != null) {
            tfSearch.textProperty().addListener((obs, oldVal, newVal) -> updateCardsDisplay());
        }
    }

    // ============================================================================
    // DATA LOADING
    // ============================================================================
    public void refreshDataFromDB() {
        try {
            System.out.println("[INFO] Chargement de toutes les ressources...");
            allRessources = rService.afficherAvecDetails();
            System.out.println("[OK] " + allRessources.size() + " ressource(s) chargée(s)");

            updateCardsDisplay();
            updateStatistics();

        } catch (SQLException e) {
            System.err.println("[ERREUR SQL] " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        int totalRessources = allRessources.size();

        long uniqueAgriculteurs = allRessources.stream()
                .map(RessourceDetailDTO::getAgriculteurId)
                .distinct().count();

        long uniqueProjets = allRessources.stream()
                .map(RessourceDetailDTO::getIdproject)
                .distinct().count();

        BigDecimal totalCost = allRessources.stream()
                .map(RessourceDetailDTO::getCoutTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (lblTotalRessources != null) lblTotalRessources.setText(String.valueOf(totalRessources));
        if (lblTotalAgriculteurs != null) lblTotalAgriculteurs.setText(String.valueOf(uniqueAgriculteurs));
        if (lblTotalProjets != null) lblTotalProjets.setText(String.valueOf(uniqueProjets));
        if (lblCoutTotal != null) lblCoutTotal.setText(String.format("%,.2f DT", totalCost));
    }

    // ============================================================================
    // DISPLAY
    // ============================================================================
    private void updateCardsDisplay() {
        if (ressourcesContainer == null) return;

        ressourcesContainer.getChildren().clear();

        String searchText = tfSearch != null ? tfSearch.getText().toLowerCase() : "";
        String filterType = cbFilterType != null ? cbFilterType.getValue() : "Tous les types";
        String filterStatutProjet = cbFilterStatutProjet != null ? cbFilterStatutProjet.getValue() : "Tous les statuts";
        String filterStatutRessource = cbFilterStatutRessource != null ? cbFilterStatutRessource.getValue() : "Tous";

        List<RessourceDetailDTO> filteredList = allRessources.stream()
                .filter(r -> {
                    boolean matchesSearch = searchText.isEmpty()
                            || r.getNomressource().toLowerCase().contains(searchText)
                            || r.getNomproject().toLowerCase().contains(searchText)
                            || r.getNomCompletAgriculteur().toLowerCase().contains(searchText)
                            || (r.getFournisseur() != null && r.getFournisseur().toLowerCase().contains(searchText));

                    boolean matchesType = "Tous les types".equals(filterType)
                            || r.getTyperessource().equals(filterType);

                    boolean matchesStatutProjet = "Tous les statuts".equals(filterStatutProjet)
                            || (r.getStatutProjet() != null && r.getStatutProjet().equals(filterStatutProjet));

                    boolean matchesStatutRessource = "Tous".equals(filterStatutRessource)
                            || r.getStatut().equals(filterStatutRessource);

                    return matchesSearch && matchesType && matchesStatutProjet && matchesStatutRessource;
                })
                .collect(Collectors.toList());

        System.out.println("[INFO] Affichage de " + filteredList.size() + " ressource(s)");

        if (filteredList.isEmpty()) {
            VBox emptyState = new VBox(15);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(60));
            emptyState.setStyle("-fx-background-color: white; -fx-background-radius: 16;");

            Label emptyIcon = new Label("📦");
            emptyIcon.setStyle("-fx-font-size: 48px; -fx-opacity: 0.5;");

            Label emptyLabel = new Label("Aucune ressource trouvée");
            emptyLabel.setStyle("-fx-text-fill: " + GRAY + "; -fx-font-size: 18px; -fx-font-weight: bold;");

            Label emptySubtitle = new Label("Modifiez vos critères de recherche");
            emptySubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #a0a5a0;");

            emptyState.getChildren().addAll(emptyIcon, emptyLabel, emptySubtitle);
            ressourcesContainer.getChildren().add(emptyState);
        } else {
            for (RessourceDetailDTO r : filteredList) {
                ressourcesContainer.getChildren().add(createDetailCard(r));
            }
        }
    }

    private VBox createDetailCard(RessourceDetailDTO dto) {
        VBox card = new VBox(12);
        card.setPrefWidth(340);
        card.setMaxWidth(340);
        card.setPadding(new Insets(18));
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

        Label icon = new Label(getTypeIcon(dto.getTyperessource()));
        icon.setStyle("-fx-font-size: 28px;");

        VBox titleBox = new VBox(3);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label title = new Label(dto.getNomressource());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + OLIVE_DARK + ";");
        title.setWrapText(true);

        Label idLabel = new Label("ID: #" + dto.getIdressource());
        idLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + GRAY + ";");

        titleBox.getChildren().addAll(title, idLabel);
        header.getChildren().addAll(icon, titleBox);

        // Badges
        HBox badges = new HBox(8);

        Label typeBadge = new Label(capitalizeType(dto.getTyperessource()));
        typeBadge.setStyle(getTypeBadgeStyle(dto.getTyperessource()));

        Label statusBadge = new Label(capitalizeStatus(dto.getStatut()));
        statusBadge.setStyle(getStatusBadgeStyle(dto.getStatut()));

        badges.getChildren().addAll(typeBadge, statusBadge);

        // Agriculteur Section
        VBox agriculteurSection = new VBox(6);
        agriculteurSection.setStyle("-fx-background-color: rgba(8, 150, 71, 0.08); -fx-padding: 12; -fx-background-radius: 10;");

        Label agriculteurTitle = new Label("👨‍🌾 Agriculteur");
        agriculteurTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: " + PRIMARY_GREEN + "; -fx-font-size: 12px;");

        Label agriculteurName = new Label(dto.getNomCompletAgriculteur());
        agriculteurName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + OLIVE_DARK + ";");

        Label agriculteurEmail = new Label("📧 " + (dto.getEmailAgriculteur() != null ? dto.getEmailAgriculteur() : "N/A"));
        agriculteurEmail.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GRAY + ";");

        Label agriculteurTel = new Label("📱 " + (dto.getTelAgriculteur() != null ? dto.getTelAgriculteur() : "N/A"));
        agriculteurTel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GRAY + ";");

        agriculteurSection.getChildren().addAll(agriculteurTitle, agriculteurName, agriculteurEmail, agriculteurTel);

        // Project Section
        VBox projetSection = new VBox(6);
        projetSection.setStyle("-fx-background-color: rgba(71, 108, 26, 0.08); -fx-padding: 12; -fx-background-radius: 10;");

        Label projetTitle = new Label("📁 Projet");
        projetTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: " + OLIVE + "; -fx-font-size: 12px;");

        Label projetName = new Label(dto.getNomproject());
        projetName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + OLIVE_DARK + ";");

        Label projetBudget = new Label("💰 Budget: " +
                (dto.getBudgetdemande() != null ? String.format("%,.2f DT", dto.getBudgetdemande()) : "N/A"));
        projetBudget.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GRAY + ";");

        Label projetStatut = new Label("📊 Statut: " + (dto.getStatutProjet() != null ? dto.getStatutProjet() : "N/A"));
        projetStatut.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GRAY + ";");

        projetSection.getChildren().addAll(projetTitle, projetName, projetBudget, projetStatut);

        // Resource Details
        GridPane details = new GridPane();
        details.setHgap(12);
        details.setVgap(6);
        details.setStyle("-fx-padding: 8 0;");

        addDetailRow(details, 0, "📦 Quantité:", String.valueOf(dto.getQuantite()));
        addDetailRow(details, 1, "💵 Coût unit.:", String.format("%,.2f DT", dto.getCout()));
        addDetailRow(details, 2, "💰 Total:", String.format("%,.2f DT", dto.getCoutTotal()));
        addDetailRow(details, 3, "🏭 Fournisseur:", dto.getFournisseur() != null ? dto.getFournisseur() : "N/A");

        // View Details Button
        Button btnDetails = new Button("👁 Voir détails complets");
        btnDetails.setMaxWidth(Double.MAX_VALUE);
        btnDetails.setStyle("-fx-background-color: " + PRIMARY_GREEN + "; -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        btnDetails.setOnAction(e -> showFullDetails(dto));
        btnDetails.setOnMouseEntered(e -> btnDetails.setStyle("-fx-background-color: " + DARK_GREEN + "; -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;"));
        btnDetails.setOnMouseExited(e -> btnDetails.setStyle("-fx-background-color: " + PRIMARY_GREEN + "; -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;"));

        card.getChildren().addAll(header, badges, new Separator(),
                agriculteurSection, projetSection,
                new Separator(), details, btnDetails);
        return card;
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + GRAY + ";");
        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + OLIVE_DARK + ";");
        lblValue.setWrapText(true);
        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }

    // ============================================================================
    // DETAILS DIALOG
    // ============================================================================
    private void showFullDetails(RessourceDetailDTO dto) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails Complets — " + dto.getNomressource());

        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: #f8faf8;");
        content.setPrefWidth(520);

        // Header
        HBox headerBox = new HBox(18);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle("-fx-background-color: linear-gradient(to right, " + PRIMARY_GREEN + ", " + DARK_GREEN + "); " +
                "-fx-padding: 25;");

        Label iconLarge = new Label(getTypeIcon(dto.getTyperessource()));
        iconLarge.setStyle("-fx-font-size: 42px;");

        VBox titleBox = new VBox(5);
        Label resourceTitle = new Label(dto.getNomressource());
        resourceTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label idLabel = new Label("Ressource #" + dto.getIdressource());
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + LIGHT_GREEN + ";");

        titleBox.getChildren().addAll(resourceTitle, idLabel);
        headerBox.getChildren().addAll(iconLarge, titleBox);

        // Content sections
        VBox sectionsBox = new VBox(15);
        sectionsBox.setPadding(new Insets(20));

        // Resource Section
        TitledPane resourcePane = new TitledPane();
        resourcePane.setText("📦 Informations Ressource");
        resourcePane.setExpanded(true);
        resourcePane.setCollapsible(false);
        resourcePane.setStyle("-fx-font-weight: bold;");

        GridPane resourceGrid = new GridPane();
        resourceGrid.setHgap(20);
        resourceGrid.setVgap(12);
        resourceGrid.setPadding(new Insets(12));

        addDetailRowDialog(resourceGrid, 0, "Type:", capitalizeType(dto.getTyperessource()));
        addDetailRowDialog(resourceGrid, 1, "Quantité:", String.valueOf(dto.getQuantite()));
        addDetailRowDialog(resourceGrid, 2, "Coût unitaire:", String.format("%,.2f DT", dto.getCout()));
        addDetailRowDialog(resourceGrid, 3, "Coût total:", String.format("%,.2f DT", dto.getCoutTotal()));
        addDetailRowDialog(resourceGrid, 4, "Fournisseur:", dto.getFournisseur() != null ? dto.getFournisseur() : "N/A");
        addDetailRowDialog(resourceGrid, 5, "Statut:", capitalizeStatus(dto.getStatut()));
        addDetailRowDialog(resourceGrid, 6, "Date d'ajout:", dto.getDateajout() != null ? dto.getDateajout().toString() : "N/A");

        resourcePane.setContent(resourceGrid);

        // Agriculteur Section
        TitledPane agriculteurPane = new TitledPane();
        agriculteurPane.setText("👨‍🌾 Informations Agriculteur");
        agriculteurPane.setExpanded(true);
        agriculteurPane.setCollapsible(false);
        agriculteurPane.setStyle("-fx-font-weight: bold;");

        GridPane agriculteurGrid = new GridPane();
        agriculteurGrid.setHgap(20);
        agriculteurGrid.setVgap(12);
        agriculteurGrid.setPadding(new Insets(12));

        addDetailRowDialog(agriculteurGrid, 0, "Nom complet:", dto.getNomCompletAgriculteur());
        addDetailRowDialog(agriculteurGrid, 1, "Email:", dto.getEmailAgriculteur() != null ? dto.getEmailAgriculteur() : "N/A");
        addDetailRowDialog(agriculteurGrid, 2, "Téléphone:", dto.getTelAgriculteur() != null ? dto.getTelAgriculteur() : "N/A");
        addDetailRowDialog(agriculteurGrid, 3, "Adresse ferme:", dto.getAdresseFerme() != null ? dto.getAdresseFerme() : "N/A");
        addDetailRowDialog(agriculteurGrid, 4, "Type culture:", dto.getTypeCulture() != null ? dto.getTypeCulture() : "N/A");

        agriculteurPane.setContent(agriculteurGrid);

        // Project Section
        TitledPane projetPane = new TitledPane();
        projetPane.setText("📁 Informations Projet");
        projetPane.setExpanded(true);
        projetPane.setCollapsible(false);
        projetPane.setStyle("-fx-font-weight: bold;");

        GridPane projetGrid = new GridPane();
        projetGrid.setHgap(20);
        projetGrid.setVgap(12);
        projetGrid.setPadding(new Insets(12));

        addDetailRowDialog(projetGrid, 0, "ID Projet:", String.valueOf(dto.getIdproject()));
        addDetailRowDialog(projetGrid, 1, "Nom:", dto.getNomproject());
        addDetailRowDialog(projetGrid, 2, "Surface:", dto.getSurface() + " hectares");
        addDetailRowDialog(projetGrid, 3, "Budget demandé:",
                dto.getBudgetdemande() != null ? String.format("%,.2f DT", dto.getBudgetdemande()) : "N/A");
        addDetailRowDialog(projetGrid, 4, "Statut:", dto.getStatutProjet() != null ? dto.getStatutProjet() : "N/A");

        projetPane.setContent(projetGrid);

        sectionsBox.getChildren().addAll(resourcePane, agriculteurPane, projetPane);

        content.getChildren().addAll(headerBox, sectionsBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(550);
        scrollPane.setStyle("-fx-background-color: #f8faf8;");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private void addDetailRowDialog(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + GRAY + "; -fx-font-weight: bold;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 13px; -fx-text-fill: " + OLIVE_DARK + ";");
        lblValue.setWrapText(true);

        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }

    // ============================================================================
    // ACTIONS
    // ============================================================================
    @FXML
    void handleRefresh(ActionEvent event) {
        refreshDataFromDB();
        showAlert(Alert.AlertType.INFORMATION, "Actualisation", "✓ Données actualisées!");
    }

    // ============================================================================
    // UTILITY METHODS
    // ============================================================================
    private String getTypeIcon(String type) {
        if (type == null) return "📦";
        switch (type.toLowerCase()) {
            case "equipement": return "🚜";
            case "materiaux": return "⚙️";
            case "service": return "🔧";
            default: return "📦";
        }
    }

    private String capitalizeType(String type) {
        if (type == null) return "N/A";
        switch (type.toLowerCase()) {
            case "equipement": return "Équipement";
            case "materiaux": return "Matériaux";
            case "service": return "Service";
            default: return type;
        }
    }

    private String capitalizeStatus(String statut) {
        if (statut == null) return "N/A";
        switch (statut.toLowerCase()) {
            case "prevu": return "Prévu";
            case "achete": return "Acheté";
            default: return statut;
        }
    }

    private String getTypeBadgeStyle(String type) {
        String baseStyle = "-fx-padding: 5 12; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;";
        if (type == null) return baseStyle + "-fx-background-color: #e0e5e0; -fx-text-fill: " + GRAY + ";";

        switch (type.toLowerCase()) {
            case "equipement":
                return baseStyle + "-fx-background-color: rgba(8, 150, 71, 0.15); -fx-text-fill: " + PRIMARY_GREEN + "; " +
                        "-fx-border-color: " + PRIMARY_GREEN + "; -fx-border-radius: 12;";
            case "materiaux":
                return baseStyle + "-fx-background-color: rgba(71, 108, 26, 0.15); -fx-text-fill: " + OLIVE + "; " +
                        "-fx-border-color: " + OLIVE + "; -fx-border-radius: 12;";
            case "service":
                return baseStyle + "-fx-background-color: rgba(225, 179, 35, 0.15); -fx-text-fill: #9A951F; " +
                        "-fx-border-color: " + YELLOW + "; -fx-border-radius: 12;";
            default:
                return baseStyle + "-fx-background-color: #e0e5e0; -fx-text-fill: " + GRAY + ";";
        }
    }

    private String getStatusBadgeStyle(String statut) {
        String baseStyle = "-fx-padding: 5 12; -fx-background-radius: 12; -fx-font-size: 10px; -fx-font-weight: bold;";
        if (statut == null) return baseStyle + "-fx-background-color: #e0e5e0; -fx-text-fill: " + GRAY + ";";

        switch (statut.toLowerCase()) {
            case "achete":
                return baseStyle + "-fx-background-color: rgba(178, 217, 68, 0.2); -fx-text-fill: " + OLIVE + "; " +
                        "-fx-border-color: " + LIGHT_GREEN + "; -fx-border-radius: 12;";
            case "prevu":
                return baseStyle + "-fx-background-color: rgba(225, 179, 35, 0.15); -fx-text-fill: #9A951F; " +
                        "-fx-border-color: " + YELLOW + "; -fx-border-radius: 12;";
            default:
                return baseStyle + "-fx-background-color: #e0e5e0; -fx-text-fill: " + GRAY + ";";
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
