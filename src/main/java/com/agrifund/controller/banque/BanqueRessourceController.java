package com.agrifund.controller.banque;

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

public class BanqueRessourceController implements Initializable {

    // ============================================================================
    // FXML FIELDS
    // ============================================================================
    @FXML private FlowPane ressourcesContainer;
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbFilterType;
    @FXML private ComboBox<String> cbFilterStatutProjet;
    @FXML private ComboBox<String> cbFilterStatutRessource;

    // Statistics
    @FXML private Label lblTotalRessources;
    @FXML private Label lblTotalAgriculteurs;
    @FXML private Label lblTotalProjets;
    @FXML private Label lblCoutTotal;

    // ============================================================================
    // DATA
    // ============================================================================
    private List<RessourceDetailDTO> allRessources = new ArrayList<>();
    private final ressourceprojectCRUD rService = new ressourceprojectCRUD();

    // ============================================================================
    // INITIALIZATION
    // ============================================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("========================================");
        System.out.println("BANQUE - Consultation des Ressources");
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
            // La banque s'intéresse principalement aux projets approuvés
            cbFilterStatutProjet.getItems().addAll("Tous les statuts", "approuve", "en_cours", "termine", "en_attente");
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
            System.out.println("[INFO] Chargement des ressources pour la banque...");

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
        if (lblCoutTotal != null) lblCoutTotal.setText(String.format("%.2f DT", totalCost));
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
                            || r.getNomCompletAgriculteur().toLowerCase().contains(searchText);

                    boolean matchesType = "Tous les types".equals(filterType)
                            || r.getTyperessource().equals(filterType);

                    boolean matchesStatutProjet = "Tous les statuts".equals(filterStatutProjet)
                            || (r.getStatutProjet() != null && r.getStatutProjet().equals(filterStatutProjet));

                    boolean matchesStatutRessource = "Tous".equals(filterStatutRessource)
                            || r.getStatut().equals(filterStatutRessource);

                    return matchesSearch && matchesType && matchesStatutProjet && matchesStatutRessource;
                })
                .collect(Collectors.toList());

        if (filteredList.isEmpty()) {
            Label emptyLabel = new Label("Aucune ressource trouvée");
            emptyLabel.setStyle("-fx-text-fill: #848A86; -fx-font-size: 16px; -fx-padding: 50;");
            ressourcesContainer.getChildren().add(emptyLabel);
        } else {
            for (RessourceDetailDTO r : filteredList) {
                ressourcesContainer.getChildren().add(createBanqueCard(r));
            }
        }
    }

    private VBox createBanqueCard(RessourceDetailDTO dto) {
        VBox card = new VBox(10);
        card.getStyleClass().add("project-card");
        card.setPrefWidth(340);
        card.setMaxWidth(340);
        card.setPadding(new Insets(15));

        // Header avec montant mis en évidence (important pour la banque)
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getTypeIcon(dto.getTyperessource()));
        icon.setStyle("-fx-font-size: 24px;");

        VBox titleBox = new VBox(2);
        Label title = new Label(dto.getNomressource());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D6A4F;");
        title.setWrapText(true);

        titleBox.getChildren().add(title);
        header.getChildren().addAll(icon, titleBox);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Montant total (mis en évidence pour la banque)
        VBox montantBox = new VBox(2);
        montantBox.setAlignment(Pos.CENTER_RIGHT);

        Label montantLabel = new Label("Montant Total");
        montantLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");

        Label montantValue = new Label(String.format("%.2f DT", dto.getCoutTotal()));
        montantValue.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #E65100;");

        montantBox.getChildren().addAll(montantLabel, montantValue);
        header.getChildren().add(montantBox);

        // Badges
        HBox badges = new HBox(5);
        Label typeBadge = new Label(capitalizeType(dto.getTyperessource()));
        typeBadge.getStyleClass().add(getTypeBadgeClass(dto.getTyperessource()));

        Label statusBadge = new Label(capitalizeStatus(dto.getStatut()));
        statusBadge.getStyleClass().add(getStatusBadgeClass(dto.getStatut()));

        // Badge statut projet
        Label projetStatutBadge = new Label(dto.getStatutProjet() != null ? dto.getStatutProjet().toUpperCase() : "N/A");
        projetStatutBadge.setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; " +
                "-fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");

        badges.getChildren().addAll(typeBadge, statusBadge, projetStatutBadge);

        // Agriculteur & Projet Info (compact pour la banque)
        VBox infoSection = new VBox(8);
        infoSection.setStyle("-fx-background-color: #F5F5F5; -fx-padding: 10; -fx-background-radius: 8;");

        HBox agriculteurRow = new HBox(5);
        agriculteurRow.setAlignment(Pos.CENTER_LEFT);
        Label agriIcon = new Label("👨‍🌾");
        Label agriName = new Label(dto.getNomCompletAgriculteur());
        agriName.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        Label agriEmail = new Label("(" + (dto.getEmailAgriculteur() != null ? dto.getEmailAgriculteur() : "N/A") + ")");
        agriEmail.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        agriculteurRow.getChildren().addAll(agriIcon, agriName, agriEmail);

        HBox projetRow = new HBox(5);
        projetRow.setAlignment(Pos.CENTER_LEFT);
        Label projetIcon = new Label("📁");
        Label projetName = new Label(dto.getNomproject());
        projetName.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        Label projetBudget = new Label("Budget: " +
                (dto.getBudgetdemande() != null ? String.format("%.2f DT", dto.getBudgetdemande()) : "N/A"));
        projetBudget.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        projetRow.getChildren().addAll(projetIcon, projetName, projetBudget);

        infoSection.getChildren().addAll(agriculteurRow, projetRow);

        // Détails financiers (important pour la banque)
        GridPane financeGrid = new GridPane();
        financeGrid.setHgap(15);
        financeGrid.setVgap(5);
        financeGrid.setStyle("-fx-background-color: #FFF3E0; -fx-padding: 10; -fx-background-radius: 8;");

        addFinanceRow(financeGrid, 0, "Quantité:", String.valueOf(dto.getQuantite()));
        addFinanceRow(financeGrid, 1, "Prix unitaire:", String.format("%.2f DT", dto.getCout()));
        addFinanceRow(financeGrid, 2, "Fournisseur:", dto.getFournisseur() != null ? dto.getFournisseur() : "N/A");

        // Bouton voir détails
        Button btnDetails = new Button("📋 Voir fiche complète");
        btnDetails.getStyleClass().add("btn-info");
        btnDetails.setMaxWidth(Double.MAX_VALUE);
        btnDetails.setOnAction(e -> showBanqueDetails(dto));

        card.getChildren().addAll(header, badges, new Separator(), infoSection, financeGrid, btnDetails);
        return card;
    }

    private void addFinanceRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #E65100;");

        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }

    // ============================================================================
    // DETAILS DIALOG (Vue Banque)
    // ============================================================================
    private void showBanqueDetails(RessourceDetailDTO dto) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Fiche Ressource - Banque");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        content.setPrefWidth(550);

        // En-tête avec montant
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setStyle("-fx-background-color: #2D6A4F; -fx-padding: 15; -fx-background-radius: 8;");

        VBox leftHeader = new VBox(5);
        Label resourceTitle = new Label(dto.getNomressource());
        resourceTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label typeLabel = new Label(capitalizeType(dto.getTyperessource()) + " | " + capitalizeStatus(dto.getStatut()));
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.8);");

        leftHeader.getChildren().addAll(resourceTitle, typeLabel);
        HBox.setHgrow(leftHeader, Priority.ALWAYS);

        VBox rightHeader = new VBox(2);
        rightHeader.setAlignment(Pos.CENTER_RIGHT);

        Label totalLabel = new Label("MONTANT TOTAL");
        totalLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.7);");

        Label totalValue = new Label(String.format("%.2f DT", dto.getCoutTotal()));
        totalValue.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #FFD54F;");

        rightHeader.getChildren().addAll(totalLabel, totalValue);
        headerBox.getChildren().addAll(leftHeader, rightHeader);

        // Section Financière
        VBox financeSection = new VBox(10);
        financeSection.setStyle("-fx-background-color: #FFF8E1; -fx-padding: 15; -fx-background-radius: 8;");

        Label financeTitle = new Label("💰 DÉTAILS FINANCIERS");
        financeTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #E65100;");

        GridPane financeGrid = new GridPane();
        financeGrid.setHgap(30);
        financeGrid.setVgap(10);

        addDetailRowBanque(financeGrid, 0, "Quantité:", String.valueOf(dto.getQuantite()));
        addDetailRowBanque(financeGrid, 1, "Coût unitaire:", String.format("%.2f DT", dto.getCout()));
        addDetailRowBanque(financeGrid, 2, "Coût total:", String.format("%.2f DT", dto.getCoutTotal()));
        addDetailRowBanque(financeGrid, 3, "Fournisseur:", dto.getFournisseur() != null ? dto.getFournisseur() : "N/A");
        addDetailRowBanque(financeGrid, 4, "Date d'ajout:", dto.getDateajout() != null ? dto.getDateajout().toString() : "N/A");

        financeSection.getChildren().addAll(financeTitle, financeGrid);

        // Section Agriculteur
        VBox agriSection = new VBox(10);
        agriSection.setStyle("-fx-background-color: #E8F5E9; -fx-padding: 15; -fx-background-radius: 8;");

        Label agriTitle = new Label("👨‍🌾 AGRICULTEUR");
        agriTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2D6A4F;");

        GridPane agriGrid = new GridPane();
        agriGrid.setHgap(30);
        agriGrid.setVgap(10);

        addDetailRowBanque(agriGrid, 0, "Nom:", dto.getNomCompletAgriculteur());
        addDetailRowBanque(agriGrid, 1, "Email:", dto.getEmailAgriculteur() != null ? dto.getEmailAgriculteur() : "N/A");
        addDetailRowBanque(agriGrid, 2, "Téléphone:", dto.getTelAgriculteur() != null ? dto.getTelAgriculteur() : "N/A");
        addDetailRowBanque(agriGrid, 3, "Adresse:", dto.getAdresseFerme() != null ? dto.getAdresseFerme() : "N/A");
        addDetailRowBanque(agriGrid, 4, "Culture:", dto.getTypeCulture() != null ? dto.getTypeCulture() : "N/A");

        agriSection.getChildren().addAll(agriTitle, agriGrid);

        // Section Projet
        VBox projetSection = new VBox(10);
        projetSection.setStyle("-fx-background-color: #E3F2FD; -fx-padding: 15; -fx-background-radius: 8;");

        Label projetTitle = new Label("📁 PROJET ASSOCIÉ");
        projetTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1565C0;");

        GridPane projetGrid = new GridPane();
        projetGrid.setHgap(30);
        projetGrid.setVgap(10);

        addDetailRowBanque(projetGrid, 0, "Nom du projet:", dto.getNomproject());
        addDetailRowBanque(projetGrid, 1, "Surface:", dto.getSurface() + " ha");
        addDetailRowBanque(projetGrid, 2, "Budget demandé:",
                dto.getBudgetdemande() != null ? String.format("%.2f DT", dto.getBudgetdemande()) : "N/A");
        addDetailRowBanque(projetGrid, 3, "Statut:", dto.getStatutProjet() != null ? dto.getStatutProjet() : "N/A");

        projetSection.getChildren().addAll(projetTitle, projetGrid);

        content.getChildren().addAll(headerBox, financeSection, agriSection, projetSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private void addDetailRowBanque(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
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
        showAlert(Alert.AlertType.INFORMATION, "Actualisation", "Données actualisées!");
    }

    // ============================================================================
    // UTILITY METHODS
    // ============================================================================
    private String getTypeIcon(String type) {
        if (type == null) return "📦";
        switch (type.toLowerCase()) {
            case "equipement": return "🚜";
            case "materiaux": return "⚙";
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

    private String getTypeBadgeClass(String type) {
        if (type == null) return "badge-default";
        switch (type.toLowerCase()) {
            case "equipement": return "badge-equipment";
            case "materiaux": return "badge-materials";
            case "service": return "badge-service";
            default: return "badge-default";
        }
    }

    private String getStatusBadgeClass(String statut) {
        if (statut == null) return "status-progress";
        switch (statut.toLowerCase()) {
            case "achete": return "status-accepted";
            case "prevu": return "status-progress";
            default: return "status-progress";
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