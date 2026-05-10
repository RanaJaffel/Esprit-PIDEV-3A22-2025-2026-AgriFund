package com.agrifund.controller.agriculteur;

import com.agrifund.entities.Agriculteur;
import com.agrifund.entities.Utilisateur;
import com.agrifund.entities.projectagricole;
import com.agrifund.entities.ressourceproject;
import com.agrifund.services.AgriculteurService;
import com.agrifund.services.projectagricoleCRUD;
import com.agrifund.services.ressourceprojectCRUD;
import com.agrifund.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AgriculteurRessourceController implements Initializable {

    // ============================================================================
    // FXML FIELDS - Main View
    // ============================================================================
    @FXML private FlowPane ressourcesContainer;
    @FXML private TextField tfSearchRessource;
    @FXML private ComboBox<String> cbFilterType;
    @FXML private ComboBox<String> cbFilterProjet;

    // Statistics Labels
    @FXML private Label lblTotalRessources;
    @FXML private Label lblEquipments;
    @FXML private Label lblMaterials;
    @FXML private Label lblServices;
    @FXML private Label lblCoutTotal;

    // ============================================================================
    // DATA FIELDS
    // ============================================================================
    private List<ressourceproject> allRessources = new ArrayList<>();
    private Map<String, Integer> projectNameToIdMap = new HashMap<>();
    private Map<Integer, String> projectIdToNameMap = new HashMap<>();

    private final ressourceprojectCRUD rService = new ressourceprojectCRUD();
    private final projectagricoleCRUD pService = new projectagricoleCRUD();
    private AgriculteurService agriculteurService;

    private Utilisateur currentUser;
    private Agriculteur currentAgriculteur;
    private int agriculteurId; // L'ID de l'agriculteur (pas l'ID utilisateur)

    // ============================================================================
    // INITIALIZATION
    // ============================================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("========================================");
        System.out.println("INITIALISATION - Liste des Ressources");
        System.out.println("========================================");

        try {
            agriculteurService = new AgriculteurService();

            // Récupérer l'utilisateur connecté
            currentUser = SessionManager.getInstance().getUtilisateurConnecte();

            if (currentUser == null) {
                System.err.println("[ERREUR] Aucun utilisateur connecté!");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez vous reconnecter.");
                return;
            }

            System.out.println("[OK] Utilisateur connecté:");
            System.out.println("    - utilisateur.id = " + currentUser.getId());
            System.out.println("    - Nom: " + currentUser.getNom() + " " + currentUser.getPrenom());

            // Récupérer l'agriculteur
            currentAgriculteur = agriculteurService.rechercherParUtilisateurId(currentUser.getId());

            if (currentAgriculteur == null) {
                System.err.println("[ERREUR] Aucun agriculteur trouvé!");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Profil agriculteur non trouvé.");
                return;
            }

            // IMPORTANT : Utiliser getAgriculteurId()
            agriculteurId = currentAgriculteur.getAgriculteurId();

            System.out.println("[OK] Agriculteur trouvé:");
            System.out.println("    - agriculteurId (getAgriculteurId) = " + agriculteurId);
            System.out.println("    - utilisateurId (getUtilisateurId) = " + currentAgriculteur.getUtilisateurId());

            // Setup filters
            setupFilters();

            // Load projects for filter
            loadProjectsForFilter();

            // Setup listeners
            setupSearchListener();
            setupFilterListeners();

            // Load data
            refreshDataFromDB();

            System.out.println("[OK] Initialisation terminée");
            System.out.println("========================================");

        } catch (SQLException e) {
            System.err.println("[ERREUR SQL] " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void setupFilters() {
        if (cbFilterType != null) {
            cbFilterType.getItems().clear();
            cbFilterType.getItems().addAll("Tous les types", "equipement", "materiaux", "service");
            cbFilterType.setValue("Tous les types");
        }

        if (cbFilterProjet != null) {
            cbFilterProjet.getItems().clear();
            cbFilterProjet.getItems().add("Tous les projets");
            cbFilterProjet.setValue("Tous les projets");
        }
    }

    private void loadProjectsForFilter() {
        try {
            System.out.println("[INFO] Chargement des projets pour le filtre...");
            System.out.println("    - agriculteurId = " + agriculteurId);

            List<projectagricole> projets = pService.afficherParAgriculteur(agriculteurId);

            System.out.println("    - Nombre de projets trouvés: " + projets.size());

            projectNameToIdMap.clear();
            projectIdToNameMap.clear();

            for (projectagricole p : projets) {
                projectNameToIdMap.put(p.getNomproject(), p.getIdproject());
                projectIdToNameMap.put(p.getIdproject(), p.getNomproject());

                if (cbFilterProjet != null) {
                    cbFilterProjet.getItems().add(p.getNomproject());
                }

                System.out.println("    - Projet: " + p.getNomproject() + " (ID: " + p.getIdproject() + ")");
            }

        } catch (SQLException e) {
            System.err.println("[ERREUR] Chargement projets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSearchListener() {
        if (tfSearchRessource != null) {
            tfSearchRessource.textProperty().addListener((obs, oldVal, newVal) -> updateCardsDisplay());
        }
    }

    private void setupFilterListeners() {
        if (cbFilterType != null) {
            cbFilterType.setOnAction(e -> updateCardsDisplay());
        }
        if (cbFilterProjet != null) {
            cbFilterProjet.setOnAction(e -> updateCardsDisplay());
        }
    }

    // ============================================================================
    // DATA LOADING
    // ============================================================================
    public void refreshDataFromDB() {
        try {
            System.out.println("========================================");
            System.out.println("CHARGEMENT DES RESSOURCES");
            System.out.println("    - agriculteurId = " + agriculteurId);
            System.out.println("========================================");

            // Charger les ressources de cet agriculteur
            allRessources = rService.afficherParAgriculteur(agriculteurId);

            System.out.println("[INFO] Ressources trouvées: " + allRessources.size());

            for (ressourceproject r : allRessources) {
                System.out.println("    - " + r.getNomressource() +
                        " (ID: " + r.getIdressource() +
                        ", Projet: " + r.getIdproject() + ")");
            }

            updateCardsDisplay();
            updateStatistics();

        } catch (SQLException e) {
            System.err.println("[ERREUR SQL] " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        int totalCount = allRessources.size();
        long equipmentCount = allRessources.stream()
                .filter(r -> "equipement".equals(r.getTyperessource())).count();
        long materialsCount = allRessources.stream()
                .filter(r -> "materiaux".equals(r.getTyperessource())).count();
        long servicesCount = allRessources.stream()
                .filter(r -> "service".equals(r.getTyperessource())).count();

        BigDecimal totalCost = allRessources.stream()
                .map(r -> r.getCout().multiply(new BigDecimal(r.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (lblTotalRessources != null) lblTotalRessources.setText(String.valueOf(totalCount));
        if (lblEquipments != null) lblEquipments.setText(String.valueOf(equipmentCount));
        if (lblMaterials != null) lblMaterials.setText(String.valueOf(materialsCount));
        if (lblServices != null) lblServices.setText(String.valueOf(servicesCount));
        if (lblCoutTotal != null) lblCoutTotal.setText(String.format("%.2f DT", totalCost));
    }

    // ============================================================================
    // DISPLAY
    // ============================================================================
    private void updateCardsDisplay() {
        if (ressourcesContainer == null) return;

        ressourcesContainer.getChildren().clear();

        String searchText = tfSearchRessource != null ? tfSearchRessource.getText().toLowerCase() : "";
        String filterType = cbFilterType != null ? cbFilterType.getValue() : "Tous les types";
        String filterProjet = cbFilterProjet != null ? cbFilterProjet.getValue() : "Tous les projets";

        List<ressourceproject> filteredList = allRessources.stream()
                .filter(r -> {
                    boolean matchesSearch = searchText.isEmpty()
                            || r.getNomressource().toLowerCase().contains(searchText)
                            || r.getFournisseur().toLowerCase().contains(searchText);
                    boolean matchesType = "Tous les types".equals(filterType)
                            || r.getTyperessource().equals(filterType);
                    boolean matchesProjet = "Tous les projets".equals(filterProjet)
                            || (projectNameToIdMap.containsKey(filterProjet)
                            && r.getIdproject() == projectNameToIdMap.get(filterProjet));
                    return matchesSearch && matchesType && matchesProjet;
                })
                .collect(Collectors.toList());

        System.out.println("[INFO] Affichage de " + filteredList.size() + " ressource(s)");

        if (filteredList.isEmpty()) {
            Label emptyLabel = new Label("Aucune ressource trouvée");
            emptyLabel.setStyle("-fx-text-fill: #848A86; -fx-font-size: 16px; -fx-padding: 50;");
            ressourcesContainer.getChildren().add(emptyLabel);
        } else {
            for (ressourceproject r : filteredList) {
                ressourcesContainer.getChildren().add(createResourceCard(r));
            }
        }
    }

    private VBox createResourceCard(ressourceproject resource) {
        VBox card = new VBox(12);
        card.getStyleClass().add("project-card");
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setPadding(new Insets(18));

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getTypeIcon(resource.getTyperessource()));
        icon.setStyle("-fx-font-size: 28px;");

        VBox titleBox = new VBox(4);
        Label title = new Label(resource.getNomressource());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D6A4F;");
        title.setWrapText(true);

        String projectName = projectIdToNameMap.getOrDefault(resource.getIdproject(), "Projet #" + resource.getIdproject());
        Label projectLabel = new Label("📁 " + projectName);
        projectLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #848A86;");

        titleBox.getChildren().addAll(title, projectLabel);
        header.getChildren().addAll(icon, titleBox);

        // Badges
        HBox badges = new HBox(8);
        Label typeBadge = new Label(capitalizeType(resource.getTyperessource()));
        typeBadge.getStyleClass().add(getTypeBadgeClass(resource.getTyperessource()));

        Label statusBadge = new Label(capitalizeStatus(resource.getStatut()));
        statusBadge.getStyleClass().add(getStatusBadgeClass(resource.getStatut()));
        badges.getChildren().addAll(typeBadge, statusBadge);

        // Details
        GridPane details = new GridPane();
        details.setHgap(10);
        details.setVgap(8);
        addDetailRow(details, 0, "📦 Quantité:", String.valueOf(resource.getQuantite()));
        addDetailRow(details, 1, "💰 Coût:", String.format("%.2f DT", resource.getCout()));
        addDetailRow(details, 2, "🏭 Fournisseur:", resource.getFournisseur());

        // Total
        BigDecimal total = resource.getCout().multiply(new BigDecimal(resource.getQuantite()));
        addDetailRow(details, 3, "💵 Total:", String.format("%.2f DT", total));

        // Actions
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10, 0, 0, 0));

        Button btnView = new Button("👁");
        btnView.getStyleClass().add("btn-view");
        btnView.setOnAction(e -> showResourceDetails(resource));
        btnView.setTooltip(new Tooltip("Voir détails"));

        Button btnEdit = new Button("✏");
        btnEdit.getStyleClass().add("btn-edit");
        btnEdit.setOnAction(e -> openModifyDialog(resource));
        btnEdit.setTooltip(new Tooltip("Modifier"));

        Button btnDelete = new Button("🗑");
        btnDelete.getStyleClass().add("btn-delete");
        btnDelete.setOnAction(e -> handleDelete(resource));
        btnDelete.setTooltip(new Tooltip("Supprimer"));

        actions.getChildren().addAll(btnView, btnEdit, btnDelete);

        card.getChildren().addAll(header, badges, new Separator(), details, actions);
        return card;
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #848A86;");
        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2D6A4F;");
        lblValue.setWrapText(true);
        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }

    // ============================================================================
    // CRUD OPERATIONS
    // ============================================================================
    @FXML
    void openAddResourceForm(ActionEvent event) {
        openAddDialog();
    }

    private void openAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/agrifund/fxml/agriculteur/agriculteur-ressource-form.fxml"));
            Parent root = loader.load();

            AgriculteurRessourceFormController formController = loader.getController();
            formController.setDialogMode("add");
            formController.setMainController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nouvelle Ressource");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("[ERREUR] " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire.");
        }
    }

    private void openModifyDialog(ressourceproject resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/agrifund/fxml/agriculteur/agriculteur-ressource-form.fxml"));
            Parent root = loader.load();

            AgriculteurRessourceFormController formController = loader.getController();
            formController.setDialogMode("modify");
            formController.setRessource(resource);
            formController.setMainController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier Ressource");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("[ERREUR] " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire.");
        }
    }

    private void handleDelete(ressourceproject resource) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer: " + resource.getNomressource());
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette ressource?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    rService.supprimer(resource.getIdressource());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource supprimée!");
                    refreshDataFromDB();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    // ============================================================================
    // RESOURCE DETAILS
    // ============================================================================
    private void showResourceDetails(ressourceproject resource) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la Ressource");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        content.setPrefWidth(400);

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(getTypeIcon(resource.getTyperessource()));
        icon.setStyle("-fx-font-size: 40px;");

        VBox titleBox = new VBox(5);
        Label title = new Label(resource.getNomressource());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2D6A4F;");

        String projectName = projectIdToNameMap.getOrDefault(resource.getIdproject(), "Projet #" + resource.getIdproject());
        Label projectLabel = new Label("📁 " + projectName);
        projectLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        titleBox.getChildren().addAll(title, projectLabel);
        header.getChildren().addAll(icon, titleBox);

        // Details Grid
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(15, 0, 15, 0));

        addDetailRow(grid, 0, "Type:", capitalizeType(resource.getTyperessource()));
        addDetailRow(grid, 1, "Quantité:", String.valueOf(resource.getQuantite()));
        addDetailRow(grid, 2, "Coût unitaire:", String.format("%.2f DT", resource.getCout()));

        BigDecimal total = resource.getCout().multiply(new BigDecimal(resource.getQuantite()));
        addDetailRow(grid, 3, "Coût total:", String.format("%.2f DT", total));

        addDetailRow(grid, 4, "Fournisseur:", resource.getFournisseur());
        addDetailRow(grid, 5, "Statut:", capitalizeStatus(resource.getStatut()));
        addDetailRow(grid, 6, "Date d'ajout:", resource.getDateajout().toString());

        content.getChildren().addAll(header, new Separator(), grid);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // ============================================================================
    // UTILITY METHODS
    // ============================================================================
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
