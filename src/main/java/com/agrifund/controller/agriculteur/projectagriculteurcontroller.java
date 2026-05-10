package com.agrifund.controller.agriculteur;

import com.agrifund.entities.Agriculteur;
import com.agrifund.entities.Utilisateur;
import com.agrifund.entities.projectagricole;
import com.agrifund.services.AgriculteurService;
import com.agrifund.services.projectagricoleCRUD;
import com.agrifund.util.SessionManager;

import javafx.concurrent.Task;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.PauseTransition;
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
import netscape.javascript.JSObject;

import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class projectagriculteurcontroller implements Initializable {

    // ============================================================================
    // FXML FIELDS - MAIN VIEW (List View)
    // ============================================================================
    @FXML
    private TextField tfNomProject;
    @FXML
    private TextField tfSurface;
    @FXML
    private TextField tfBudget;
    @FXML
    private ComboBox<String> cbStatut;
    @FXML
    private DatePicker dpDateSoumission;
    @FXML
    private FlowPane projectsContainer;

    @FXML
    private TextField tfSearchProject;
    @FXML
    private ComboBox<String> cbFilterStatutList;

    @FXML
    private Label lblTotalProjects;
    @FXML
    private Label lblAcceptedProjects;
    @FXML
    private Label lblInProgressProjects;
    @FXML
    private Label lblRefusedProjects;

    @FXML
    private Label lblTotalBudget;
    @FXML
    private Label lblTotalSurface;
    @FXML
    private Label lblLastUpdate;

    @FXML
    private Label lblUserName;
    @FXML
    private Label lblUserEmail;

    // ============================================================================
    // FXML FIELDS - ADD/MODIFY DIALOG
    // ============================================================================
    @FXML
    private TextField tfNomProjectDialog;
    @FXML
    private TextField tfSurfaceDialog;
    @FXML
    private TextField tfBudgetDialog;
    @FXML
    private DatePicker dpDateSoumissionDialog;
    @FXML
    private Button btnSave;
    @FXML
    private Label lblStatusBadge;

    @FXML
    private TextField tfLatitudeDialog;
    @FXML
    private TextField tfLongitudeDialog;
    @FXML
    private Label lblLocationStatus;
    @FXML
    private Button btnOpenMap;

    private Double pickedLatitude = null;
    private Double pickedLongitude = null;

    // ============================================================================
    // SESSION - AGRICULTEUR CONNECTÉ
    // ============================================================================
    private Utilisateur currentUser;
    private Agriculteur currentAgriculteur;
    private AgriculteurService agriculteurService;

    // ============================================================================
    // SHARED FIELDS
    // ============================================================================
    private List<projectagricole> allProjects = new ArrayList<>();
    private projectagricole selectedProject = null;
    private projectagricole currentProject = null;
    private projectagricoleCRUD service;
    private Timeline autoRefreshTimeline;
    private static final int REFRESH_INTERVAL_SECONDS = 3;

    private Map<Integer, String> previousStatuses = new HashMap<>();

    private projectagriculteurcontroller mainController;
    private String dialogMode = "list";

    // API Key pour Agromonitoring
    private static final String AGRO_API_KEY = "15ec96f6a29c1d2c59280c84a585bb66";

    // ============================================================================
    // 🌿 ASSISTANT PLANTE MALADE — GROQ AI VISION
    // ============================================================================
    private static String GROQ_API_KEY = "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";

    private static final String[] GROQ_VISION_MODELS = {
            "meta-llama/llama-4-scout-17b-16e-instruct",
            "meta-llama/llama-4-maverick-17b-128e-instruct",
            "llava-v1.5-7b-4096-preview"
    };

    // ============================================================================
// 📰 ACTUALITÉS AGRICOLES — NewsAPI
// ============================================================================
    private static final String NEWS_API_KEY = "6263389c98ca4dbbb7ac1eec33269db2";
    // ============================================================================
    // INITIALIZATION
    // ============================================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialiser le service CRUD
        service = new projectagricoleCRUD();

        // Initialiser le service Agriculteur
        try {
            agriculteurService = new AgriculteurService();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'initialiser les services: " + e.getMessage());
            return;
        }

        // Récupérer l'utilisateur connecté depuis la session
        currentUser = SessionManager.getInstance().getUtilisateurConnecte();

        if (currentUser != null) {
            // Récupérer le profil agriculteur
            try {
                currentAgriculteur = agriculteurService.rechercherParUtilisateurId(currentUser.getId());
            } catch (SQLException e) {
                System.err.println("Erreur lors de la récupération du profil agriculteur: " + e.getMessage());
            }
        }

        // Initialize ComboBoxes
        if (cbStatut != null) {
            cbStatut.getItems().addAll("en cours", "accepte", "refuse");
        }
        if (cbFilterStatutList != null) {
            cbFilterStatutList.getItems().addAll("Tous les statuts", "en cours", "accepte", "refuse");
            cbFilterStatutList.setValue("Tous les statuts");
        }

        // Setup for main list view (page principale)
        if (projectsContainer != null) {
            setupSearchListener();
            setupFilterListener();
            loadUserInfo();

            if (currentAgriculteur != null) {
                try {
                    refreshDataFromDB();
                    for (projectagricole p : allProjects) {
                        previousStatuses.put(p.getIdproject(), p.getStatut());
                    }
                    startAutoRefresh();
                } catch (Exception e) {
                    System.err.println("Error loading initial data: " + e.getMessage());
                    allProjects = new ArrayList<>();
                    updateCardsDisplay();
                    updateStatistics();
                }
            }
        }

        // Setup for add/modify dialog
        if (lblStatusBadge != null && btnSave != null && dpDateSoumissionDialog != null) {
            setupAddDialog();
        }
    }

    /**
     * Méthode pour passer les informations de l'agriculteur au dialogue
     */
    public void setAgriculteurInfo(Utilisateur user, Agriculteur agriculteur) {
        this.currentUser = user;
        this.currentAgriculteur = agriculteur;
    }

    /**
     * Load and display user info in header
     */
    private void loadUserInfo() {
        if (lblUserName != null && currentUser != null) {
            lblUserName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        }
        if (lblUserEmail != null && currentUser != null) {
            lblUserEmail.setText(currentUser.getEmail());
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

    public void setMainController(projectagriculteurcontroller mainController) {
        this.mainController = mainController;
    }

    private void populateFields() {
        if (currentProject != null && tfNomProjectDialog != null) {
            tfNomProjectDialog.setText(currentProject.getNomproject());
            tfSurfaceDialog.setText(String.valueOf(currentProject.getSurface()));
            tfBudgetDialog.setText(currentProject.getBudgetdemande().toString());
            dpDateSoumissionDialog.setValue(currentProject.getDatesoumission().toLocalDate());
            updateStatusBadge(currentProject.getStatut());

            if (currentProject.getLatitude() != null && currentProject.getLongitude() != null) {
                pickedLatitude = currentProject.getLatitude();
                pickedLongitude = currentProject.getLongitude();
                String latStr = String.format("%.6f", pickedLatitude);
                String lngStr = String.format("%.6f", pickedLongitude);
                if (tfLatitudeDialog != null) tfLatitudeDialog.setText(latStr);
                if (tfLongitudeDialog != null) tfLongitudeDialog.setText(lngStr);
                if (lblLocationStatus != null) {
                    lblLocationStatus.setText("✅ Localisation définie : " + latStr + ", " + lngStr);
                    lblLocationStatus.setStyle("-fx-text-fill: #076A39; -fx-font-size: 11px; -fx-font-weight: bold;");
                }
            }
        }
    }

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
                tooltipText = "Projet accepté par la décision financière.";
                break;
            case "refuse":
                icon = "✗";
                displayText = icon + " Refusé";
                backgroundColor = "linear-gradient(to right, #D32F2F, #B71C1C)";
                tooltipText = "Projet refusé par la décision financière.";
                break;
            case "en cours":
            default:
                icon = "📋";
                displayText = icon + " En cours";
                backgroundColor = "linear-gradient(to right, #E1B323, #9A951F)";
                tooltipText = "Projet en attente de décision financière.";
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
    // HANDLE SAVE - AJOUT / MODIFICATION
    // ============================================================================

    @FXML
    void handleSave(ActionEvent event) {
        // Vérifier que l'agriculteur est bien défini
        if (currentAgriculteur == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur Session",
                    "Session expirée. Veuillez vous reconnecter.");
            return;
        }

        if (!validateDialogInputs()) return;

        try {
            if ("modify".equals(dialogMode)) {
                // MODIFY MODE
                currentProject.setNomproject(getDialogFieldValue(tfNomProjectDialog));
                currentProject.setSurface(Float.parseFloat(getDialogFieldValue(tfSurfaceDialog)));
                currentProject.setBudgetdemande(new BigDecimal(getDialogFieldValue(tfBudgetDialog)));
                currentProject.setDatesoumission(Date.valueOf(dpDateSoumissionDialog.getValue()));
                currentProject.setLatitude(pickedLatitude);
                currentProject.setLongitude(pickedLongitude);

                service.modifier(currentProject);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Projet modifié avec succès!\n\n" +
                                "Note: Le statut reste inchangé (" +
                                getStatusDisplayName(currentProject.getStatut()) + ")");

            } else {
                // ADD MODE - Utiliser l'ID de l'agriculteur connecté
                projectagricole p = new projectagricole(
                        currentAgriculteur.getAgriculteurId(),
                        getDialogFieldValue(tfNomProjectDialog),
                        Float.parseFloat(getDialogFieldValue(tfSurfaceDialog)),
                        new BigDecimal(getDialogFieldValue(tfBudgetDialog)),
                        "en cours",
                        Date.valueOf(dpDateSoumissionDialog.getValue())
                );
                p.setLatitude(pickedLatitude);
                p.setLongitude(pickedLongitude);

                service.ajouter(p);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Projet ajouté avec succès!\n\n" +
                                "Statut: En cours (en attente de décision financière)");
            }

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

    private String getStatusDisplayName(String statut) {
        switch (statut.toLowerCase()) {
            case "accepte":
                return "Accepté";
            case "refuse":
                return "Refusé";
            case "en cours":
                return "En cours";
            default:
                return statut;
        }
    }

    // ============================================================================
    // MAP PICKER
    // ============================================================================

    @FXML
    void handleOpenMap(ActionEvent event) {
        Stage mapStage = new Stage();
        mapStage.initModality(Modality.APPLICATION_MODAL);
        mapStage.setTitle("Choisir la localisation");
        mapStage.setWidth(920);
        mapStage.setHeight(640);

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);

        java.net.URL mapUrl = getClass().getResource("/com/agrifund/fxml/mapbox_picker.html");
        if (mapUrl == null) {
            showAlert(Alert.AlertType.ERROR, "Fichier manquant",
                    "Le fichier mapbox_picker.html est introuvable.");
            return;
        }

        engine.titleProperty().addListener((obs, oldTitle, newTitle) -> {
            if (newTitle != null && newTitle.startsWith("COORDS|")) {
                String[] parts = newTitle.substring(7).split("\\|", 2);
                if (parts.length == 2) {
                    try {
                        double lat = Double.parseDouble(parts[0].trim());
                        double lng = Double.parseDouble(parts[1].trim());
                        onLocationPicked(lat, lng, mapStage);
                    } catch (NumberFormatException ignored) {
                        System.err.println("Bad coords in title: " + newTitle);
                    }
                }
            }
        });

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                MapBridge bridge = new MapBridge(mapStage, this);
                JSObject jsWindow = (JSObject) engine.executeScript("window");
                jsWindow.setMember("javaBridge", bridge);

                if (pickedLatitude != null && pickedLongitude != null) {
                    engine.executeScript(String.format(java.util.Locale.US,
                            "setTimeout(function(){ setInitialMarker(%f, %f); }, 400);",
                            pickedLatitude, pickedLongitude));
                }
            }
        });

        engine.load(mapUrl.toExternalForm());

        Scene mapScene = new Scene(new StackPane(webView));
        mapStage.setScene(mapScene);
        mapStage.showAndWait();
    }

    public void onLocationPicked(double lat, double lng, Stage stageToClose) {
        pickedLatitude = lat;
        pickedLongitude = lng;
        String latStr = String.format(java.util.Locale.US, "%.6f", lat);
        String lngStr = String.format(java.util.Locale.US, "%.6f", lng);

        javafx.application.Platform.runLater(() -> {
            if (stageToClose != null && stageToClose.isShowing()) {
                stageToClose.close();
            }
            if (tfLatitudeDialog != null) tfLatitudeDialog.setText(latStr);
            if (tfLongitudeDialog != null) tfLongitudeDialog.setText(lngStr);
            if (lblLocationStatus != null) {
                lblLocationStatus.setText("✅ Localisation définie : " + latStr + ", " + lngStr);
                lblLocationStatus.setStyle("-fx-text-fill: #076A39; -fx-font-size: 11px; -fx-font-weight: bold;");
            }
        });
    }

    public class MapBridge {
        private final Stage mapStage;
        private final projectagriculteurcontroller controller;

        public MapBridge(Stage mapStage, projectagriculteurcontroller controller) {
            this.mapStage = mapStage;
            this.controller = controller;
        }

        public void setCoordinates(String lat, String lng) {
            try {
                double latD = Double.parseDouble(lat.trim());
                double lngD = Double.parseDouble(lng.trim());
                controller.onLocationPicked(latD, lngD, mapStage);
            } catch (NumberFormatException e) {
                System.err.println("MapBridge: invalid coords: " + lat + " / " + lng);
            }
        }
    }

    private void closeDialogWindow() {
        if (btnSave != null) {
            Stage stage = (Stage) btnSave.getScene().getWindow();
            stage.close();
        }
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

        // Check uniqueness of project name for this agriculteur
        String newName = getDialogFieldValue(tfNomProjectDialog);
        try {
            Integer excludeId = "modify".equals(dialogMode) && currentProject != null
                    ? currentProject.getIdproject() : null;

            if (currentAgriculteur != null &&
                    service.nomProjetExiste(currentAgriculteur.getAgriculteurId(), newName, excludeId)) {
                showAlert(Alert.AlertType.ERROR, "Nom déjà utilisé",
                        "Vous avez déjà un projet avec le nom \"" + newName + "\".\n" +
                                "Le nom du projet doit être unique!");
                return false;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Impossible de vérifier l'unicité du nom: " + e.getMessage());
            return false;
        }

        // Validate date
        if ("add".equals(dialogMode)) {
            if (!dpDateSoumissionDialog.getValue().isEqual(java.time.LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Erreur de date",
                        "La date de soumission doit être la date d'aujourd'hui uniquement!\n" +
                                "Date actuelle: " + java.time.LocalDate.now());
                return false;
            }
        } else if ("modify".equals(dialogMode)) {
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
        if (currentAgriculteur == null) return;

        try {
            List<projectagricole> freshProjects = service.afficherParAgriculteur(
                    currentAgriculteur.getAgriculteurId()
            );

            if (freshProjects == null) {
                freshProjects = new ArrayList<>();
            }

            // Detect status changes
            List<Integer> changedProjectIds = new ArrayList<>();
            if (!previousStatuses.isEmpty()) {
                for (projectagricole p : freshProjects) {
                    String oldStatut = previousStatuses.get(p.getIdproject());
                    if (oldStatut != null && !oldStatut.equals(p.getStatut())) {
                        changedProjectIds.add(p.getIdproject());
                        System.out.println("[Refresh] Status changed for project #"
                                + p.getIdproject() + ": " + oldStatut + " → " + p.getStatut());
                    }
                }
            }

            // Update snapshot
            previousStatuses.clear();
            for (projectagricole p : freshProjects) {
                previousStatuses.put(p.getIdproject(), p.getStatut());
            }

            allProjects = freshProjects;
            updateCardsDisplay();
            updateStatistics();

            if (!changedProjectIds.isEmpty()) {
                highlightChangedCards(changedProjectIds);
                showStatusChangeToast(changedProjectIds, freshProjects);
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            if (allProjects == null) {
                allProjects = new ArrayList<>();
            }
            showAlert(Alert.AlertType.ERROR, "Erreur Base de données",
                    "Impossible de charger vos projets: " + e.getMessage());
        }
    }

    private void highlightChangedCards(List<Integer> changedIds) {
        if (projectsContainer == null) return;
        for (javafx.scene.Node node : projectsContainer.getChildren()) {
            if (node.getUserData() instanceof Integer) {
                int cardProjectId = (Integer) node.getUserData();
                if (changedIds.contains(cardProjectId)) {
                    playCardChangedAnimation((VBox) node);
                }
            }
        }
    }

    private void playCardChangedAnimation(VBox card) {
        String originalStyle = card.getStyle();
        card.setStyle(originalStyle +
                "-fx-border-color: #E1B323; -fx-border-width: 3; -fx-border-radius: 12;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(225,179,35,0.7), 16, 0, 0, 0);");

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(180), card);
        scaleUp.setToX(1.04);
        scaleUp.setToY(1.04);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(180), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        PauseTransition hold = new PauseTransition(Duration.seconds(2.5));

        SequentialTransition seq = new SequentialTransition(scaleUp, scaleDown, hold);
        seq.setOnFinished(e -> card.setStyle(originalStyle));
        seq.play();
    }

    private void showStatusChangeToast(List<Integer> changedIds, List<projectagricole> projects) {
        StringBuilder msg = new StringBuilder("🔔 Statut mis à jour:\n");
        for (int id : changedIds) {
            projects.stream()
                    .filter(p -> p.getIdproject() == id)
                    .findFirst()
                    .ifPresent(p -> msg.append("  • ").append(p.getNomproject())
                            .append(" → ").append(capitalizeStatus(p.getStatut())).append("\n"));
        }

        showAlert(Alert.AlertType.INFORMATION, "Décision financière reçue", msg.toString().trim());
    }

    private void updateStatistics() {
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
            return;
        }

        BigDecimal totalBudget = allProjects.stream()
                .map(projectagricole::getBudgetdemande)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double totalSurface = allProjects.stream()
                .mapToDouble(projectagricole::getSurface)
                .sum();

        if (lblTotalBudget != null) lblTotalBudget.setText(String.format("%,.2f DT", totalBudget));
        if (lblTotalSurface != null) lblTotalSurface.setText(String.format("%.2f Ha", totalSurface));
        if (lblLastUpdate != null) {
            lblLastUpdate.setText(java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
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

        if (filteredList.isEmpty()) {
            Label emptyLabel = new Label("🌾 Aucun projet trouvé.\nCliquez sur '+ Nouveau Projet' pour commencer.");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #848A86; -fx-padding: 50;");
            emptyLabel.setWrapText(true);
            projectsContainer.getChildren().add(emptyLabel);
        } else {
            for (projectagricole p : filteredList) {
                projectsContainer.getChildren().add(createEnhancedProjectCard(p));
            }
        }
    }

    private VBox createEnhancedProjectCard(projectagricole project) {
        VBox card = new VBox(12);
        card.getStyleClass().add("project-card");
        card.setPadding(new Insets(20));
        card.setMaxWidth(380);
        card.setPrefWidth(380);
        card.setUserData(project.getIdproject());

        // Header
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

        // Details
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(8);

        addCardDetailRow(detailsGrid, 0, "🌾 Surface:", String.format("%.2f Ha", project.getSurface()));
        addCardDetailRow(detailsGrid, 1, "💰 Budget:", String.format("%,.2f DT", project.getBudgetdemande()));
        addCardDetailRow(detailsGrid, 2, "📅 Date:", project.getDatesoumission().toString());

        // Actions
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

        // Bouton Chat Assistant Plante Malade
        Button btnChat = new Button("💬");
        btnChat.getStyleClass().add("btn-view");
        btnChat.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #1a73e8, #0d47a1);" +
                        "-fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 6 10;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(26,115,232,0.5), 6, 0, 0, 2);");
        btnChat.setTooltip(new Tooltip("🌿 Assistant Plante Malade — Analyse IA"));
        btnChat.setMinWidth(40);
        btnChat.setPrefWidth(40);
        btnChat.setOnAction(e -> openPlantDiseaseChat(project));

        actions.getChildren().addAll(btnView, btnEdit, btnDelete, btnChat);
        HBox.setHgrow(actions, Priority.ALWAYS);

        // Bouton Conseils du projet
        Button btnConseils = new Button("🌱 Conseils du projet");
        btnConseils.setMaxWidth(Double.MAX_VALUE);
        btnConseils.setStyle(
                "-fx-background-color: linear-gradient(to right, #076A39, #089647);" +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;" +
                        "-fx-padding: 9 0; -fx-background-radius: 10; -fx-cursor: hand;");
        btnConseils.setTooltip(new Tooltip("Données Agromonitoring + conseils agricoles IA"));
        btnConseils.setOnAction(e -> handleAgroAdvice(project));

        if (project.getLatitude() == null || project.getLongitude() == null) {
            btnConseils.setDisable(true);
            btnConseils.setStyle(
                    "-fx-background-color: #ccc; -fx-text-fill: #888; -fx-font-size: 13px;" +
                            "-fx-padding: 9 0; -fx-background-radius: 10;");
            btnConseils.setTooltip(new Tooltip("Localisation non définie — modifiez le projet pour ajouter une position GPS"));
        }

        // Assemble card
        card.getChildren().addAll(header, statusBadge, new Separator(), detailsGrid, actions, btnConseils);

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
            case "accepte":
                return "✅";
            case "refuse":
                return "❌";
            case "en cours":
                return "⏳";
            default:
                return "📋";
        }
    }

    private String capitalizeStatus(String statut) {
        switch (statut.toLowerCase()) {
            case "accepte":
                return "Accepté";
            case "refuse":
                return "Refusé";
            case "en cours":
                return "En cours";
            default:
                return statut;
        }
    }

    private String getStatusBadgeClass(String statut) {
        switch (statut.toLowerCase()) {
            case "accepte":
                return "status-accepted";
            case "refuse":
                return "status-refused";
            case "en cours":
                return "status-progress";
            default:
                return "status-progress";
        }
    }

    // ============================================================================
    // CRUD OPERATIONS
    // ============================================================================

    @FXML
    void handleAdd(ActionEvent event) {
        openAddDialog();
    }

    @FXML
    void openAddProjectForm(ActionEvent event) {
        openAddDialog();
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
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Projet supprimé avec succès!");
                    refreshDataFromDB();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    // ============================================================================
    // DIALOG OPENERS
    // ============================================================================

    private void openAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/agrifund/fxml/agriculteur/agriculteur-project-add.fxml"));
            Parent root = loader.load();

            projectagriculteurcontroller controller = loader.getController();
            controller.dialogMode = "add";
            controller.setMainController(this);
            controller.setAgriculteurInfo(currentUser, currentAgriculteur);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nouveau Projet Agricole");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void openModifyDialog(projectagricole project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/agrifund/fxml/agriculteur/agriculteur-project-modify.fxml"));
            Parent root = loader.load();

            projectagriculteurcontroller controller = loader.getController();
            controller.dialogMode = "modify";
            controller.setProject(project);
            controller.setMainController(this);
            controller.setAgriculteurInfo(currentUser, currentAgriculteur);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier Projet — " + project.getNomproject());
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
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
        showAlert(Alert.AlertType.INFORMATION, "Actualisation", "Vos projets ont été actualisés!");
    }

    // ============================================================================
    // PROJECT DETAILS
    // ============================================================================

    private void showProjectDetails(projectagricole project) {
        Stage detailStage = new Stage();
        detailStage.initModality(Modality.APPLICATION_MODAL);
        detailStage.setTitle("Détails du Projet — " + project.getNomproject());

        boolean hasLocation = project.getLatitude() != null && project.getLongitude() != null;

        // Left panel: project info
        VBox infoPanel = new VBox(18);
        infoPanel.setPadding(new Insets(25));
        infoPanel.setStyle("-fx-background-color: white;");
        infoPanel.setPrefWidth(420);

        // Header
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label iconLarge = new Label(getProjectIcon(project.getStatut()));
        iconLarge.setStyle("-fx-font-size: 44px;");
        VBox titleBox = new VBox(4);
        Label titleLabel = new Label(project.getNomproject());
        titleLabel.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #133D03;");
        titleLabel.setWrapText(true);
        Label idLabel = new Label("Projet #" + project.getIdproject());
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #848A86;");
        titleBox.getChildren().addAll(titleLabel, idLabel);
        headerBox.getChildren().addAll(iconLarge, titleBox);

        // Status badge
        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label("Statut :");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #848A86;");
        Label statusBadge = new Label(capitalizeStatus(project.getStatut()));
        statusBadge.getStyleClass().add(getStatusBadgeClass(project.getStatut()));
        statusBadge.setStyle(statusBadge.getStyle() + "-fx-font-size: 13px; -fx-padding: 6 18;");
        statusRow.getChildren().addAll(statusLabel, statusBadge);

        // Details grid
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(20);
        detailsGrid.setVgap(12);
        detailsGrid.setPadding(new Insets(8, 0, 8, 0));
        addDetailRow(detailsGrid, 0, "🌾 Surface :", String.format("%.2f Hectares", project.getSurface()));
        addDetailRow(detailsGrid, 1, "💰 Budget demandé :", String.format("%,.2f DT", project.getBudgetdemande()));
        addDetailRow(detailsGrid, 2, "📅 Date de soumission :", project.getDatesoumission().toString());

        // Location info row
        if (hasLocation) {
            addDetailRow(detailsGrid, 3, "📍 Latitude :", String.format(java.util.Locale.US, "%.6f", project.getLatitude()));
            addDetailRow(detailsGrid, 4, "📍 Longitude :", String.format(java.util.Locale.US, "%.6f", project.getLongitude()));
        } else {
            Label noLoc = new Label("📍 Localisation non définie");
            noLoc.setStyle("-fx-text-fill: #848A86; -fx-font-size: 12px; -fx-font-style: italic;");
            detailsGrid.add(noLoc, 0, 3, 2, 1);
        }

        // Status explanation
        Separator sep = new Separator();
        VBox statusExplanation = new VBox(6);
        Label explanationTitle = new Label("ℹ️ À propos du statut");
        explanationTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #133D03;");
        Label explanationText = new Label(getStatusExplanation(project.getStatut()));
        explanationText.setWrapText(true);
        explanationText.setMaxWidth(380);
        explanationText.setStyle("-fx-font-size: 11px; -fx-text-fill: #6C757D;");
        statusExplanation.getChildren().addAll(explanationTitle, explanationText);

        // Close button
        Button closeBtn = new Button("✖  Fermer");
        closeBtn.setStyle(
                "-fx-background-color: #076A39; -fx-text-fill: white; -fx-font-weight: bold;" +
                        "-fx-padding: 10 28; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13px;");
        closeBtn.setOnAction(e -> detailStage.close());
        HBox btnRow = new HBox(closeBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(10, 0, 0, 0));

        infoPanel.getChildren().addAll(headerBox, statusRow, new Separator(), detailsGrid, sep, statusExplanation, btnRow);

        // Right panel: map WebView (only if location exists)
        WebView mapView = null;
        if (hasLocation) {
            mapView = new WebView();
            mapView.setPrefWidth(480);
            mapView.setPrefHeight(500);
            mapView.setMinWidth(480);

            final WebEngine engine = mapView.getEngine();
            engine.setJavaScriptEnabled(true);

            java.net.URL mapUrl = getClass().getResource("/com/agrifund/fxml/mapbox_picker.html");
            if (mapUrl != null) {
                final double lat = project.getLatitude();
                final double lng = project.getLongitude();
                engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                        engine.executeScript(String.format(java.util.Locale.US,
                                "setTimeout(function(){" +
                                        "  setInitialMarker(%f, %f);" +
                                        "  document.getElementById('bottom-bar').style.display='none';" +
                                        "  document.getElementById('hint').style.display='none';" +
                                        "  document.getElementById('map-wrap').style.cursor='default';" +
                                        "}, 400);", lat, lng));
                    }
                });
                engine.load(mapUrl.toExternalForm());
            }
        }

        // Assemble layout
        HBox root;
        if (mapView != null) {
            root = new HBox(mapView, infoPanel);
            detailStage.setWidth(920);
            detailStage.setHeight(520);
        } else {
            root = new HBox(infoPanel);
            detailStage.setWidth(450);
            detailStage.setHeight(480);
        }

        detailStage.setScene(new Scene(root));
        detailStage.showAndWait();
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

    // ============================================================================
    // MAP - SHOW ALL MY PROJECTS
    // ============================================================================

    @FXML
    void handleShowAllProjectsMap(ActionEvent event) {
        if (allProjects == null || allProjects.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucun projet", "Vous n'avez aucun projet à afficher sur la carte.");
            return;
        }

        java.net.URL mapUrl = getClass().getResource("/com/agrifund/fxml/projects_map.html");
        if (mapUrl == null) {
            showAlert(Alert.AlertType.ERROR, "Fichier manquant", "projects_map.html introuvable");
            return;
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < allProjects.size(); i++) {
            projectagricole p = allProjects.get(i);
            if (i > 0) json.append(",");
            json.append("{");
            json.append("\"nom\":").append(jsonStr(p.getNomproject())).append(",");
            json.append("\"statut\":").append(jsonStr(p.getStatut())).append(",");
            json.append("\"surface\":").append(String.format(java.util.Locale.US, "%.2f", p.getSurface())).append(",");
            json.append("\"budget\":").append(jsonStr(String.format(java.util.Locale.US, "%,.2f", p.getBudgetdemande()))).append(",");
            json.append("\"date\":").append(jsonStr(p.getDatesoumission().toString())).append(",");
            if (p.getLatitude() != null && p.getLongitude() != null) {
                json.append("\"lat\":").append(String.format(java.util.Locale.US, "%.6f", p.getLatitude())).append(",");
                json.append("\"lng\":").append(String.format(java.util.Locale.US, "%.6f", p.getLongitude()));
            } else {
                json.append("\"lat\":null,\"lng\":null");
            }
            json.append("}");
        }
        json.append("]");

        Stage mapStage = new Stage();
        mapStage.initModality(Modality.APPLICATION_MODAL);
        mapStage.setTitle("🗺 Mes Projets sur la Carte");
        mapStage.setWidth(1100);
        mapStage.setHeight(720);

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);

        final String projectsJson = json.toString();

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                engine.executeScript("loadProjects(" + projectsJson + ")");
            }
        });

        engine.load(mapUrl.toExternalForm());
        mapStage.setScene(new Scene(new StackPane(webView)));
        mapStage.show();
    }

    private String jsonStr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
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
        fileChooser.setInitialFileName("mes_projets_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(projectsContainer.getScene().getWindow());
        if (file != null) {
            try {
                generatePDFReport(file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Rapport PDF généré: " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur PDF: " + e.getMessage());
            }
        }
    }

    private void generatePDFReport(String filePath) throws Exception {
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Title
        document.add(new Paragraph("Mes Projets Agricoles")
                .setFontSize(20).setBold().setTextAlignment(TextAlignment.CENTER));

        if (currentUser != null) {
            document.add(new Paragraph("Agriculteur: " + currentUser.getPrenom() + " " + currentUser.getNom())
                    .setFontSize(12).setTextAlignment(TextAlignment.CENTER));
        }

        document.add(new Paragraph("Généré le: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFontSize(10).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));

        // Stats
        document.add(new Paragraph("Statistiques").setFontSize(14).setBold());
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
    // AGROMONITORING + AI CONSEILS
    // ============================================================================

    private void handleAgroAdvice(projectagricole project) {
        if (project.getLatitude() == null || project.getLongitude() == null) {
            showAlert(Alert.AlertType.WARNING, "Localisation manquante",
                    "Ce projet n'a pas de coordonnées GPS.\nModifiez le projet pour ajouter une localisation.");
            return;
        }

        // Loading dialog
        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setTitle("Chargement des données...");
        Label loadingLabel = new Label("🌐 Récupération des données Agromonitoring...");
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-padding: 30 40;");
        ProgressIndicator spinner = new ProgressIndicator();
        VBox loadingBox = new VBox(15, spinner, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(30));
        loadingStage.setScene(new Scene(loadingBox, 340, 160));
        loadingStage.show();

        double lat = project.getLatitude();
        double lng = project.getLongitude();

        Thread thread = new Thread(() -> {
            try {
                String weatherJson = httpGet(
                        "http://api.agromonitoring.com/agro/1.0/weather?lat=" + lat
                                + "&lon=" + lng + "&appid=" + AGRO_API_KEY);

                String soilJson = httpGet(
                        "http://api.agromonitoring.com/agro/1.0/soil?polyid=&lat=" + lat
                                + "&lon=" + lng + "&appid=" + AGRO_API_KEY);

                String uvJson = httpGet(
                        "http://api.agromonitoring.com/agro/1.0/uvi?lat=" + lat
                                + "&lon=" + lng + "&appid=" + AGRO_API_KEY);

                final String w = weatherJson;
                final String s = soilJson;
                final String u = uvJson;

                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    showAdviceDialog(project, w, s, u);
                });

            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    showAlert(Alert.AlertType.ERROR, "Erreur API",
                            "Impossible de récupérer les données Agromonitoring.\n\n" + ex.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private String httpGet(String urlStr) throws Exception {
        java.net.URL url = new java.net.URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestProperty("Accept", "application/json");

        int code = conn.getResponseCode();
        java.io.InputStream stream = (code >= 200 && code < 300)
                ? conn.getInputStream() : conn.getErrorStream();
        if (stream == null) return "{\"error\":\"" + code + "\"}";

        BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return sb.toString();
    }

    private String parseJson(String json, String key) {
        if (json == null || json.isEmpty()) return "—";
        try {
            int idx = json.indexOf("\"" + key + "\"");
            if (idx < 0) return "—";
            int colon = json.indexOf(":", idx);
            if (colon < 0) return "—";
            int start = colon + 1;
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
            if (start >= json.length()) return "—";
            char first = json.charAt(start);
            if (first == '"') {
                int end = json.indexOf('"', start + 1);
                return end > start ? json.substring(start + 1, end) : "—";
            } else {
                int end = start;
                while (end < json.length() && ",}]\n\r".indexOf(json.charAt(end)) < 0) end++;
                return json.substring(start, end).trim();
            }
        } catch (Exception e) {
            return "—";
        }
    }

    private String parseNestedJson(String json, String parent, String key) {
        if (json == null) return "—";
        try {
            int parentIdx = json.indexOf("\"" + parent + "\"");
            if (parentIdx < 0) return "—";
            int braceOpen = json.indexOf("{", parentIdx);
            if (braceOpen < 0) return "—";
            int braceClose = json.indexOf("}", braceOpen);
            String sub = json.substring(braceOpen, braceClose + 1);
            return parseJson(sub, key);
        } catch (Exception e) {
            return "—";
        }
    }

    private String kelvinToCelsius(String kelvinStr) {
        try {
            double k = Double.parseDouble(kelvinStr);
            return String.format("%.1f°C", k - 273.15);
        } catch (Exception e) {
            return kelvinStr;
        }
    }

    private void showAdviceDialog(projectagricole project,
                                  String weatherJson, String soilJson, String uvJson) {

        String tempK = parseNestedJson(weatherJson, "main", "temp");
        String humidity = parseNestedJson(weatherJson, "main", "humidity");
        String windSpeed = parseNestedJson(weatherJson, "wind", "speed");
        String pressure = parseNestedJson(weatherJson, "main", "pressure");
        String cloudiness = parseNestedJson(weatherJson, "clouds", "all");
        String weatherDesc = parseNestedJson(weatherJson, "weather", "description");
        String cityName = parseJson(weatherJson, "name");
        String tempC = kelvinToCelsius(tempK);

        String soilMoisture = parseJson(soilJson, "moisture");
        String soilTempK = parseJson(soilJson, "t0");
        String soilTempC = kelvinToCelsius(soilTempK);

        String uvIndex = parseJson(uvJson, "value");
        if ("—".equals(uvIndex)) uvIndex = parseJson(uvJson, "uvi");

        String advice = generateAgriculturalAdvice(
                project, tempK, humidity, windSpeed, soilMoisture, uvIndex, cloudiness);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("🌱 Conseils — " + project.getNomproject());
        stage.setWidth(720);
        stage.setHeight(700);

        VBox content = new VBox(18);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F8FFF9;");

        VBox headerBox = new VBox(4);
        headerBox.setStyle(
                "-fx-background-color: linear-gradient(to right, #076A39, #089647);" +
                        "-fx-padding: 18 24;" +
                        "-fx-background-radius: 12;");

        Label hTitle = new Label("🌱 Conseils Agricoles — " + project.getNomproject());
        hTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label hSub = new Label("📍 " + String.format("%.4f, %.4f", project.getLatitude(), project.getLongitude())
                + (cityName.equals("—") ? "" : "   🏙 " + cityName));
        hSub.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.85);");

        headerBox.getChildren().addAll(hTitle, hSub);

        VBox weatherCard = buildInfoCard("🌦 Météo Actuelle", new String[][]{
                {"🌡 Température", tempC},
                {"💧 Humidité", humidity + "%"},
                {"💨 Vent", windSpeed + " m/s"},
                {"🌫 Pression", pressure + " hPa"},
                {"☁ Nébulosité", cloudiness + "%"},
                {"📝 Conditions", weatherDesc}
        }, "#E8F5E9", "#2E7D32");

        VBox soilCard = buildInfoCard("🌍 Données du Sol", new String[][]{
                {"💧 Humidité sol", soilMoisture.equals("—") ? "—" : String.format("%.3f m³/m³", safeDouble(soilMoisture))},
                {"🌡 Temp. surface", soilTempC}
        }, "#FFF8E1", "#F57F17");

        String uvLevel = uvLevel(uvIndex);
        VBox uvCard = buildInfoCard("☀ Rayonnement UV", new String[][]{
                {"🔆 Indice UV", uvIndex},
                {"⚠ Niveau", uvLevel}
        }, "#E3F2FD", "#1565C0");

        VBox adviceCard = new VBox(10);
        adviceCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-padding: 16;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #076A39;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(7,106,57,0.15), 8, 0, 0, 2);");

        Label adviceTitle = new Label("🤖 Recommandations Agricoles");
        adviceTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #076A39;");

        Label adviceText = new Label(advice);
        adviceText.setWrapText(true);
        adviceText.setStyle("-fx-font-size: 13px; -fx-text-fill: #2C3E50; -fx-line-spacing: 3px;");

        adviceCard.getChildren().addAll(adviceTitle, adviceText);

        Label satNote = new Label(
                "🛰 Données satellitaires via Agromonitoring API  •  Surface: "
                        + String.format("%.2f Ha", project.getSurface())
                        + "  •  Projet: " + capitalizeStatus(project.getStatut()));
        satNote.setStyle("-fx-font-size: 11px; -fx-text-fill: #888; -fx-font-style: italic;");

        Button closeBtn = new Button("✖  Fermer");
        closeBtn.setStyle(
                "-fx-background-color: #076A39;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 30;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 13px;");
        closeBtn.setOnAction(e -> stage.close());

        HBox btnRow = new HBox(closeBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(
                headerBox,
                weatherCard,
                soilCard,
                uvCard,
                adviceCard,
                satNote,
                btnRow
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F8FFF9; -fx-border-width: 0;");

        stage.setScene(new Scene(scroll));
        stage.show();
    }

    private VBox buildInfoCard(String title, String[][] rows, String bgColor, String accentColor) {
        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-padding: 14 18;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + accentColor + "33;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;");

        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");
        card.getChildren().add(lbl);

        card.getChildren().add(new Separator());

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(6);

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(150);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setMinWidth(200);
        grid.getColumnConstraints().addAll(c1, c2);

        int row = 0;
        for (String[] pair : rows) {
            Label k = new Label(pair[0]);
            k.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

            Label v = new Label(pair.length > 1 ? pair[1] : "—");
            v.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

            grid.add(k, 0, row);
            grid.add(v, 1, row);
            row++;
        }

        card.getChildren().add(grid);
        return card;
    }

    private double safeDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private String uvLevel(String uvStr) {
        try {
            double uv = Double.parseDouble(uvStr);
            if (uv < 3) return "Faible — Travail normal";
            if (uv < 6) return "Modéré — Protection recommandée";
            if (uv < 8) return "Élevé — Éviter 11h-16h";
            if (uv < 11) return "Très élevé — Protection obligatoire";
            return "Extrême — Éviter l'exposition";
        } catch (Exception e) {
            return "—";
        }
    }

    private String generateAgriculturalAdvice(projectagricole project,
                                              String tempK, String humidity, String windSpeed,
                                              String soilMoisture, String uvIndex, String cloudiness) {

        StringBuilder advice = new StringBuilder();
        double tempC = 0, hum = 0, wind = 0, soil = 0, uv = 0, cloud = 0;

        try {
            tempC = Double.parseDouble(tempK) - 273.15;
        } catch (Exception ignored) {
        }
        try {
            hum = Double.parseDouble(humidity);
        } catch (Exception ignored) {
        }
        try {
            wind = Double.parseDouble(windSpeed);
        } catch (Exception ignored) {
        }
        try {
            soil = Double.parseDouble(soilMoisture);
        } catch (Exception ignored) {
        }
        try {
            uv = Double.parseDouble(uvIndex);
        } catch (Exception ignored) {
        }
        try {
            cloud = Double.parseDouble(cloudiness);
        } catch (Exception ignored) {
        }

        if (tempC < 5) {
            advice.append("❄ Température très froide (").append(String.format("%.1f", tempC))
                    .append("°C) — Risque de gel. Protégez les cultures fragiles et évitez les semis.\n\n");
        } else if (tempC < 15) {
            advice.append("🌤 Température fraîche (").append(String.format("%.1f", tempC))
                    .append("°C) — Conditions favorables pour blé, orge et légumes d'hiver.\n\n");
        } else if (tempC <= 30) {
            advice.append("☀ Température optimale (").append(String.format("%.1f", tempC))
                    .append("°C) — Idéal pour la croissance active des cultures.\n\n");
        } else {
            advice.append("🌡 Chaleur élevée (").append(String.format("%.1f", tempC))
                    .append("°C) — Augmentez l'irrigation. Préférez les travaux tôt le matin.\n\n");
        }

        if (hum < 30) {
            advice.append("💧 Humidité très basse (").append((int) hum)
                    .append("%) — Irrigation urgente recommandée. Risque de stress hydrique.\n\n");
        } else if (hum < 60) {
            advice.append("💧 Humidité modérée (").append((int) hum)
                    .append("%) — Surveillez le sol. Irrigation légère possible.\n\n");
        } else {
            advice.append("💧 Humidité suffisante (").append((int) hum)
                    .append("%) — Risque fongique si prolongé. Assurez un bon drainage.\n\n");
        }

        if (soil > 0) {
            if (soil < 0.2) {
                advice.append("🌍 Sol sec (").append(String.format("%.3f", soil))
                        .append(" m³/m³) — Irrigation immédiate nécessaire.\n\n");
            } else if (soil < 0.4) {
                advice.append("🌍 Humidité sol correcte (").append(String.format("%.3f", soil))
                        .append(" m³/m³) — Conditions favorables pour les racines.\n\n");
            } else {
                advice.append("🌍 Sol très humide (").append(String.format("%.3f", soil))
                        .append(" m³/m³) — Réduisez l'irrigation. Vérifiez le drainage.\n\n");
            }
        }

        if (wind > 10) {
            advice.append("💨 Vent fort (").append(String.format("%.1f", wind))
                    .append(" m/s) — Évitez les traitements phytosanitaires. Protégez les jeunes plants.\n\n");
        } else if (wind > 5) {
            advice.append("💨 Vent modéré — Conditions moyennes pour les épandages.\n\n");
        }

        if (uv >= 6) {
            advice.append("☀ UV élevé (").append(String.format("%.1f", uv))
                    .append(") — Travaillez tôt le matin ou en soirée. Protection individuelle obligatoire.\n\n");
        }

        float surface = project.getSurface();
        if (surface > 50) {
            advice.append("📏 Grande surface (").append(String.format("%.0f", surface))
                    .append(" Ha) — Envisagez l'utilisation de drones ou matériel mécanisé.\n\n");
        }

        if (cloud < 30) {
            advice.append("🌾 Ciel dégagé — Bonne visibilité satellite. Conditions idéales pour observation par drone.\n");
        }

        if (advice.length() == 0) {
            advice.append("✅ Les conditions météorologiques et pédologiques sont dans des plages normales.\n")
                    .append("Continuez le suivi régulier de vos cultures et maintenez votre calendrier d'irrigation habituel.");
        }

        return advice.toString().trim();
    }

    // ============================================================================
    // 🌿 ASSISTANT PLANTE MALADE — CHAT METHODS
    // ============================================================================

    private void openPlantDiseaseChat(projectagricole project) {
        Stage chatStage = new Stage();
        chatStage.initModality(Modality.APPLICATION_MODAL);
        chatStage.setTitle("🌿 Assistant Plante Malade — " + project.getNomproject());
        chatStage.setWidth(680);
        chatStage.setHeight(750);
        chatStage.setResizable(false);

        VBox root = new VBox();
        root.setStyle("-fx-background-color: #F0F7F0;");

        // Header
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 22, 18, 22));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #076A39, #089647);" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 8, 0, 0, 3);");

        Label headerIcon = new Label("🌿");
        headerIcon.setStyle("-fx-font-size: 30px;");

        VBox headerText = new VBox(2);
        Label headerTitle = new Label("Assistant Plante Malade");
        headerTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label headerSub = new Label("Projet : " + project.getNomproject() + "  •  IA Vision");
        headerSub.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.80);");
        headerText.getChildren().addAll(headerTitle, headerSub);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        // Change API key button
        Button btnChangeKey = new Button("🔑");
        btnChangeKey.setStyle(
                "-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white;" +
                        "-fx-font-size: 16px; -fx-padding: 6 10; -fx-background-radius: 8;" +
                        "-fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.35);" +
                        "-fx-border-width: 1; -fx-border-radius: 8;");
        btnChangeKey.setTooltip(new Tooltip("Changer la clé API Groq"));
        btnChangeKey.setOnAction(ev -> {
            TextInputDialog dlg = new TextInputDialog(GROQ_API_KEY);
            dlg.setTitle("Clé API Groq");
            dlg.setHeaderText("🔑 Nouvelle clé API Groq");
            dlg.setContentText("Obtenez une clé gratuite sur : console.groq.com/keys\nCollez-la ici :");
            dlg.getEditor().setPrefWidth(420);
            dlg.showAndWait().ifPresent(newKey -> {
                String trimmed = newKey.trim();
                if (!trimmed.isEmpty()) {
                    GROQ_API_KEY = trimmed;
                    showAlert(Alert.AlertType.INFORMATION, "Clé mise à jour",
                            "✅ Nouvelle clé API Groq activée.");
                }
            });
        });

        header.getChildren().addAll(headerIcon, headerText, btnChangeKey);

        // Chat area
        VBox chatBox = new VBox(14);
        chatBox.setPadding(new Insets(18, 18, 8, 18));
        ScrollPane scrollPane = new ScrollPane(chatBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Welcome message
        addChatBubble(chatBox, "🌿 Bonjour ! Je suis votre assistant spécialisé en maladies des plantes.\n\n" +
                "📸 Envoyez-moi une photo de votre plante malade et je vous fournirai :\n" +
                "  • Le nom exact de la plante\n  • Le diagnostic de la maladie\n" +
                "  • Un traitement agricole adapté\n\nCliquez sur 📎 pour sélectionner votre photo.", false);

        // Bottom panel
        VBox bottomPanel = new VBox(10);
        bottomPanel.setPadding(new Insets(12, 16, 16, 16));
        bottomPanel.setStyle("-fx-background-color: white; -fx-border-color: #D0E8D0; -fx-border-width: 1 0 0 0;");

        // Image preview
        File[] selectedFileHolder = {null};
        HBox previewBox = new HBox(10);
        previewBox.setAlignment(Pos.CENTER_LEFT);
        previewBox.setVisible(false);
        previewBox.setManaged(false);

        javafx.scene.image.ImageView imagePreview = new javafx.scene.image.ImageView();
        imagePreview.setFitWidth(80);
        imagePreview.setFitHeight(80);
        imagePreview.setPreserveRatio(true);
        imagePreview.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 6, 0, 0, 2);");

        Label previewLabel = new Label("Image sélectionnée");
        previewLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #076A39; -fx-font-weight: bold;");

        Button btnRemoveImg = new Button("✖");
        btnRemoveImg.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white;" +
                "-fx-background-radius: 50%; -fx-padding: 2 6; -fx-cursor: hand; -fx-font-size: 10px;");
        previewBox.getChildren().addAll(imagePreview, previewLabel, btnRemoveImg);

        // Input row
        HBox inputRow = new HBox(8);
        inputRow.setAlignment(Pos.CENTER);

        Button btnAttach = new Button("📎");
        btnAttach.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #E8F5E9, #C8E6C9);" +
                        "-fx-text-fill: #076A39; -fx-font-size: 18px; -fx-padding: 8 12;" +
                        "-fx-background-radius: 10; -fx-cursor: hand;" +
                        "-fx-border-color: #A5D6A7; -fx-border-width: 1; -fx-border-radius: 10;");
        btnAttach.setTooltip(new Tooltip("Joindre une photo de plante malade"));

        Button btnSend = new Button("🔍 Analyser");
        btnSend.setStyle(
                "-fx-background-color: linear-gradient(to right, #076A39, #089647);" +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;" +
                        "-fx-padding: 10 28; -fx-background-radius: 10; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(7,106,57,0.4), 8, 0, 0, 2);");
        btnSend.setDisable(true);

        Label statusLabel = new Label("Sélectionnez une image pour commencer l'analyse");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888; -fx-font-style: italic;");
        statusLabel.setWrapText(true);

        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        inputRow.getChildren().addAll(btnAttach, statusLabel, btnSend);
        bottomPanel.getChildren().addAll(previewBox, inputRow);

        // Events
        btnAttach.setOnAction(ev -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Sélectionner une photo de plante");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));
            File file = fc.showOpenDialog(chatStage);
            if (file != null) {
                selectedFileHolder[0] = file;
                try {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(file.toURI().toString());
                    imagePreview.setImage(img);
                    previewLabel.setText(file.getName());
                    previewBox.setVisible(true);
                    previewBox.setManaged(true);
                    btnSend.setDisable(false);
                    statusLabel.setText("✅ Image prête — Cliquez sur Analyser");
                    statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #076A39; -fx-font-weight: bold;");
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'image.");
                }
            }
        });

        btnRemoveImg.setOnAction(ev -> {
            selectedFileHolder[0] = null;
            imagePreview.setImage(null);
            previewBox.setVisible(false);
            previewBox.setManaged(false);
            btnSend.setDisable(true);
            statusLabel.setText("Sélectionnez une image pour commencer l'analyse");
            statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888; -fx-font-style: italic;");
        });

        btnSend.setOnAction(ev -> {
            File imgFile = selectedFileHolder[0];
            if (imgFile == null) return;

            addChatBubble(chatBox, "📸 " + imgFile.getName(), true);
            scrollToBottom(scrollPane, chatBox);

            btnSend.setDisable(true);
            btnAttach.setDisable(true);
            statusLabel.setText("⏳ Analyse en cours par IA…");
            statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #E1B323; -fx-font-weight: bold;");

            Label typingLabel = new Label("🌿 Assistant analyse votre plante…  ⣾");
            typingLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #076A39; -fx-font-style: italic;");
            chatBox.getChildren().add(typingLabel);
            scrollToBottom(scrollPane, chatBox);

            Thread bgThread = new Thread(() -> {
                String response = callGroqVision(imgFile, project.getNomproject());
                javafx.application.Platform.runLater(() -> {
                    chatBox.getChildren().remove(typingLabel);
                    addChatBubble(chatBox, response, false);
                    scrollToBottom(scrollPane, chatBox);
                    btnSend.setDisable(false);
                    btnAttach.setDisable(false);
                    selectedFileHolder[0] = null;
                    imagePreview.setImage(null);
                    previewBox.setVisible(false);
                    previewBox.setManaged(false);
                    statusLabel.setText("Analyse terminée — Joignez une nouvelle image pour continuer");
                    statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888; -fx-font-style: italic;");
                });
            });
            bgThread.setDaemon(true);
            bgThread.start();
        });

        root.getChildren().addAll(header, scrollPane, bottomPanel);
        chatStage.setScene(new Scene(root));
        chatStage.show();
    }

    private void addChatBubble(VBox chatBox, String text, boolean isUser) {
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(480);
        if (isUser) {
            bubble.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #076A39, #089647);" +
                            "-fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 12 16;" +
                            "-fx-background-radius: 18 18 4 18;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(7,106,57,0.35), 6, 0, 0, 2);");
            HBox row = new HBox(bubble);
            row.setAlignment(Pos.CENTER_RIGHT);
            chatBox.getChildren().add(row);
        } else {
            bubble.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #1A2E1A; -fx-font-size: 13px; -fx-padding: 14 18;" +
                            "-fx-background-radius: 18 18 18 4;" +
                            "-fx-border-color: #D0E8D0; -fx-border-width: 1; -fx-border-radius: 18 18 18 4;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.10), 6, 0, 0, 2);" +
                            "-fx-line-spacing: 3px;");
            HBox row = new HBox(10);
            row.setAlignment(Pos.TOP_LEFT);
            Label avatar = new Label("🌿");
            avatar.setStyle("-fx-font-size: 20px;");
            row.getChildren().addAll(avatar, bubble);
            chatBox.getChildren().add(row);
        }
    }

    private void scrollToBottom(ScrollPane sp, VBox chatBox) {
        javafx.application.Platform.runLater(() -> {
            chatBox.layout();
            sp.layout();
            sp.setVvalue(1.0);
        });
    }

    private String callGroqVision(File imageFile, String projectName) {
        try {
            byte[] imageBytes = java.nio.file.Files.readAllBytes(imageFile.toPath());
            String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);

            String fileName = imageFile.getName().toLowerCase();
            String mimeType = "image/jpeg";
            if (fileName.endsWith(".png")) mimeType = "image/png";
            else if (fileName.endsWith(".gif")) mimeType = "image/gif";
            else if (fileName.endsWith(".webp")) mimeType = "image/webp";
            else if (fileName.endsWith(".bmp")) mimeType = "image/bmp";

            String prompt = "Tu es un expert agronome. " +
                    "Le projet agricole s'appelle : \"" + projectName + "\". " +
                    "Identifie immédiatement le nom de la plante sur cette image, " +
                    "analyse la maladie visible et donne un traitement agricole concret. " +
                    "Réponds en français, structuré ainsi :\n" +
                    "🌱 Plante : [nom]\n" +
                    "🦠 Maladie détectée : [nom de la maladie]\n" +
                    "⚠ Symptômes observés : [description courte]\n" +
                    "💊 Traitement recommandé :\n  • [produit/méthode 1]\n  • [produit/méthode 2]\n  • [produit/méthode 3]\n" +
                    "🔄 Prévention : [conseil court]\n" +
                    "Si la plante est saine, indique-le clairement.";

            String jsonBody = "{"
                    + "\"model\":\"MODEL_PLACEHOLDER\","
                    + "\"messages\":[{"
                    + "\"role\":\"user\","
                    + "\"content\":["
                    + "{\"type\":\"text\",\"text\":\"" + escapeJsonChat(prompt) + "\"},"
                    + "{\"type\":\"image_url\",\"image_url\":{"
                    + "\"url\":\"data:" + mimeType + ";base64," + base64Image + "\""
                    + "}}"
                    + "]"
                    + "}],"
                    + "\"max_tokens\":1024,"
                    + "\"temperature\":0.3"
                    + "}";

            String lastError = "Aucun modèle disponible.";

            for (String model : GROQ_VISION_MODELS) {
                try {
                    String body = jsonBody.replace("MODEL_PLACEHOLDER", model);

                    java.net.URL url = new java.net.URL("https://api.groq.com/openai/v1/chat/completions");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Authorization", "Bearer " + GROQ_API_KEY);
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(60000);

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }

                    int status = conn.getResponseCode();

                    java.io.InputStream is;
                    try {
                        is = conn.getInputStream();
                    } catch (Exception e) {
                        is = conn.getErrorStream();
                    }

                    StringBuilder sb = new StringBuilder();
                    if (is != null) {
                        try (BufferedReader br = new BufferedReader(
                                new InputStreamReader(is, StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = br.readLine()) != null) sb.append(line).append("\n");
                        }
                    }
                    String responseJson = sb.toString();

                    if (status == 429 || status == 503) {
                        lastError = "HTTP " + status + " [" + model + "]";
                        continue;
                    }

                    if (status >= 200 && status < 300) {
                        int idx = responseJson.indexOf("\"content\":");
                        if (idx >= 0) {
                            int start = responseJson.indexOf("\"", idx + 10) + 1;
                            int end = start;
                            while (end < responseJson.length()) {
                                char c = responseJson.charAt(end);
                                if (c == '\\') {
                                    end += 2;
                                    continue;
                                }
                                if (c == '"') break;
                                end++;
                            }
                            String raw = responseJson.substring(start, end)
                                    .replace("\\n", "\n")
                                    .replace("\\t", "\t")
                                    .replace("\\\"", "\"")
                                    .replace("\\\\", "\\");
                            return raw;
                        }
                        return "⚠ Réponse inattendue du modèle " + model + ".";
                    } else {
                        return "❌ Erreur API Groq (HTTP " + status + ")\n\n"
                                + responseJson.substring(0, Math.min(500, responseJson.length()));
                    }

                } catch (Exception modelEx) {
                    lastError = "Erreur réseau [" + model + "] : " + modelEx.getMessage();
                }
            }

            return "❌ Quota épuisé sur tous les modèles Groq.\n\n" +
                    "💡 Solution rapide :\n" +
                    "  1. Allez sur console.groq.com/keys\n" +
                    "  2. Cliquez « Create API Key »\n" +
                    "  3. Copiez la clé et cliquez 🔑 en haut à droite\n" +
                    "  4. Collez la nouvelle clé et réessayez\n\n" +
                    "Dernière erreur : " + lastError;

        } catch (Exception ex) {
            return "❌ Erreur lors de la lecture de l'image :\n" + ex.getMessage();
        }
    }

    // ============================================================================
// 📰 ACTUALITÉS AGRICOLES — NewsAPI METHODS
// ============================================================================

    @FXML
    private void openNewsDialog() {
        Stage newsStage = new Stage();
        newsStage.setTitle("📰 Actualités Agricoles");
        newsStage.initModality(Modality.APPLICATION_MODAL);
        newsStage.setResizable(true);
        newsStage.setMinWidth(740);
        newsStage.setMinHeight(580);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F4F6F8;");

        // HEADER
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #1B5E20, #2E7D32);" +
                        "-fx-padding: 18 28;"
        );
        Label newsIconLbl = new Label("📰");
        newsIconLbl.setStyle("-fx-font-size: 36px;");

        VBox newsTitleBox = new VBox(2);
        Label newsTitleLbl = new Label("Actualités Agricoles");
        newsTitleLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label newsSubLbl = new Label("Dernières nouvelles du monde agricole • NewsAPI.org");
        newsSubLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.80);");
        newsTitleBox.getChildren().addAll(newsTitleLbl, newsSubLbl);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher...");
        searchField.setPrefWidth(200);
        searchField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.18);" +
                        "-fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.60);" +
                        "-fx-border-color: rgba(255,255,255,0.40); -fx-border-radius: 20;" +
                        "-fx-background-radius: 20; -fx-padding: 6 14;"
        );

        Button refreshBtn = new Button("🔄 Rafraîchir");
        refreshBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.22);" +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;" +
                        "-fx-background-radius: 20; -fx-border-radius: 20;" +
                        "-fx-padding: 7 16; -fx-cursor: hand;"
        );
        header.getChildren().addAll(newsIconLbl, newsTitleBox, headerSpacer, searchField, refreshBtn);

        // CATEGORY FILTER BAR
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setStyle(
                "-fx-background-color: white; -fx-padding: 10 24;" +
                        "-fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;"
        );
        Label filterLbl = new Label("Thème :");
        filterLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-weight: bold;");

        String[][] topics = {
                {"Agriculture", "agriculture", "ferme"},
                {"Cultures", "culture", "recolte"},
                {"Elevage", "elevage", "betail"},
                {"Meteo", "meteo", "climat"},
                {"Marche", "prix", "economie"},
                {"Innovation", "innovation", "technologie"}
        };

        ToggleGroup tg = new ToggleGroup();
        ToggleButton[] topicBtns = new ToggleButton[topics.length];
        String styleNormal =
                "-fx-background-color: #F1F8E9; -fx-text-fill: #33691E;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 20; -fx-border-radius: 20;" +
                        "-fx-border-color: #C5E1A5; -fx-padding: 5 12; -fx-cursor: hand;";
        String styleSelected =
                "-fx-background-color: #2E7D32; -fx-text-fill: white;" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-background-radius: 20; -fx-border-radius: 20;" +
                        "-fx-border-color: #1B5E20; -fx-padding: 5 12; -fx-cursor: hand;";

        for (int i = 0; i < topics.length; i++) {
            ToggleButton tb = new ToggleButton(topics[i][0]);
            tb.setUserData(topics[i][1] + "|" + topics[i][2]);
            tb.setToggleGroup(tg);
            tb.setStyle(styleNormal);
            tb.selectedProperty().addListener((obs, wasSelected, isSelected) ->
                    tb.setStyle(isSelected ? styleSelected : styleNormal));
            topicBtns[i] = tb;
        }
        topicBtns[0].setSelected(true);

        filterBar.getChildren().add(filterLbl);
        for (ToggleButton tb : topicBtns) filterBar.getChildren().add(tb);

        VBox topBox = new VBox(0, header, filterBar);
        root.setTop(topBox);

        // NEWS ListView
        ListView<NewsArticle> newsListView = new ListView<>();
        newsListView.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        newsListView.setCellFactory(lv -> new NewsListCell());

        VBox centerBox = new VBox(0);
        centerBox.setStyle("-fx-background-color: #F4F6F8; -fx-padding: 12 20;");
        VBox.setVgrow(newsListView, Priority.ALWAYS);

        Label loadingLabel = new Label("⏳ Chargement des actualités...");
        loadingLabel.setStyle(
                "-fx-font-size: 15px; -fx-text-fill: #2E7D32; -fx-font-weight: bold;" +
                        "-fx-padding: 60 0;"
        );
        loadingLabel.setMaxWidth(Double.MAX_VALUE);
        loadingLabel.setAlignment(Pos.CENTER);

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(50, 50);
        VBox loadingBox = new VBox(12, spinner, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(60, 0, 0, 0));

        centerBox.getChildren().add(loadingBox);
        root.setCenter(centerBox);

        // STATUS BAR
        HBox statusBar = new HBox(10);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle(
                "-fx-background-color: white; -fx-padding: 8 20;" +
                        "-fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;"
        );
        Label statusLbl = new Label("📡 Connexion à NewsAPI...");
        statusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");
        Label poweredBy = new Label("Propulsé par NewsAPI.org");
        poweredBy.setStyle("-fx-font-size: 10px; -fx-text-fill: #bbb;");
        Region statusSpacer = new Region();
        HBox.setHgrow(statusSpacer, Priority.ALWAYS);
        statusBar.getChildren().addAll(statusLbl, statusSpacer, poweredBy);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 760, 620);
        newsStage.setScene(scene);
        newsStage.show();

        // Fetch logic
        final String[] currentQuery = {"agriculture|ferme"};

        Consumer<String> fetchNews = queryData -> {
            currentQuery[0] = queryData;
            String[] parts = queryData.split("\\|", 2);
            String primaryQ = parts[0].trim();
            String fallbackQ = parts.length > 1 ? parts[1].trim() : "";

            centerBox.getChildren().setAll(loadingBox);
            statusLbl.setText("Chargement en cours...");
            statusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

            Task<List<NewsArticle>> task = new Task<List<NewsArticle>>() {
                @Override
                protected List<NewsArticle> call() throws Exception {
                    List<NewsArticle> result = fetchAgricultureNews(primaryQ);
                    if (result.isEmpty() && !fallbackQ.isEmpty()) {
                        result = fetchAgricultureNews(fallbackQ);
                    }
                    if (result.isEmpty()) {
                        result = fetchAgricultureNews("agricole " + primaryQ);
                    }
                    return result;
                }
            };
            task.setOnSucceeded(evt -> {
                List<NewsArticle> articles = task.getValue();
                if (articles.isEmpty()) {
                    Label noResultsLbl = new Label("Aucun article trouvé pour ce thème.");
                    noResultsLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #888; -fx-padding: 60 0;");
                    noResultsLbl.setMaxWidth(Double.MAX_VALUE);
                    noResultsLbl.setAlignment(Pos.CENTER);
                    centerBox.getChildren().setAll(noResultsLbl);
                } else {
                    newsListView.getItems().setAll(articles);
                    centerBox.getChildren().setAll(newsListView);
                    VBox.setVgrow(newsListView, Priority.ALWAYS);
                }
                statusLbl.setText(articles.size() + " articles chargés - NewsAPI.org");
                statusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
            });
            task.setOnFailed(evt -> {
                Label errLbl = new Label("Impossible de charger les actualités.");
                errLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #c0392b; -fx-padding: 60 0;");
                errLbl.setMaxWidth(Double.MAX_VALUE);
                errLbl.setAlignment(Pos.CENTER);
                centerBox.getChildren().setAll(errLbl);
                statusLbl.setText("Erreur de chargement");
                statusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #c0392b;");
            });

            Thread t = new Thread(task);
            t.setDaemon(true);
            t.start();
        };

        tg.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) fetchNews.accept((String) newT.getUserData());
        });
        refreshBtn.setOnAction(e -> fetchNews.accept(currentQuery[0]));
        searchField.setOnAction(e -> {
            String q = searchField.getText().trim();
            if (!q.isEmpty()) {
                tg.selectToggle(null);
                fetchNews.accept(q + "|" + q + " agricole");
            }
        });

        fetchNews.accept("agriculture|ferme");
    }

    private List<NewsArticle> fetchAgricultureNews(String query) throws Exception {
        String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
        String urlStr = "https://newsapi.org/v2/everything"
                + "?q=" + encodedQuery
                + "&sortBy=publishedAt"
                + "&pageSize=40"
                + "&apiKey=" + NEWS_API_KEY;

        java.net.URL url = new java.net.URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "JavaFX-AgricoleApp/1.0");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int status = conn.getResponseCode();
        java.io.InputStream is = (status >= 200 && status < 300)
                ? conn.getInputStream() : conn.getErrorStream();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        conn.disconnect();
        return parseNewsArticles(sb.toString());
    }

    private List<NewsArticle> parseNewsArticles(String json) {
        List<NewsArticle> list = new ArrayList<>();
        int articlesStart = json.indexOf("\"articles\":[");
        if (articlesStart < 0) return list;

        int pos = articlesStart + 12;
        while (list.size() < 30) {
            int objStart = json.indexOf("{", pos);
            if (objStart < 0) break;
            int depth = 0, objEnd = objStart;
            for (int i = objStart; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) { objEnd = i; break; }
                }
            }
            if (objEnd <= objStart) break;
            String obj = json.substring(objStart, objEnd + 1);
            NewsArticle a = new NewsArticle();
            a.title = extractNewsJsonString(obj, "title");
            a.description = extractNewsJsonString(obj, "description");
            a.url = extractNewsJsonString(obj, "url");
            a.source = extractNewsJsonString(obj, "name");
            a.publishedAt = extractNewsJsonString(obj, "publishedAt");
            if (a.title != null && !a.title.isEmpty() && !"[Removed]".equals(a.title)) {
                list.add(a);
            }
            pos = objEnd + 1;
        }
        return list;
    }

    private String extractNewsJsonString(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        int valStart = idx + search.length();
        while (valStart < json.length() && json.charAt(valStart) == ' ') valStart++;
        if (valStart >= json.length()) return "";
        if (json.charAt(valStart) == '"') {
            int start = valStart + 1, end = start;
            while (end < json.length()) {
                char c = json.charAt(end);
                if (c == '\\') { end += 2; continue; }
                if (c == '"') break;
                end++;
            }
            return json.substring(start, end)
                    .replace("\\n", " ").replace("\\\"", "\"").replace("\\/", "/");
        } else if (valStart + 4 <= json.length() && json.substring(valStart, valStart + 4).equals("null")) {
            return "";
        }
        int end = valStart;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
        return json.substring(valStart, end).trim();
    }

    // Data model for News
    private static class NewsArticle {
        String title;
        String description;
        String url;
        String source;
        String publishedAt;
    }

    // Custom ListCell for News
    private class NewsListCell extends ListCell<NewsArticle> {
        private final VBox card = new VBox(7);
        private final HBox topRow = new HBox(8);
        private final Label sourceTag = new Label();
        private final Label dateLbl = new Label();
        private final Label titleLbl = new Label();
        private final Label descLbl = new Label();
        private final Hyperlink linkLbl = new Hyperlink("🔗 Lire l'article complet");

        private static final String CARD_NORMAL =
                "-fx-background-color: white; -fx-background-radius: 12;" +
                        "-fx-border-color: #E8F5E9; -fx-border-width: 1; -fx-border-radius: 12;" +
                        "-fx-padding: 14 16;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 6, 0, 0, 2);";

        private static final String CARD_HOVER =
                "-fx-background-color: #F9FFF9; -fx-background-radius: 12;" +
                        "-fx-border-color: #A5D6A7; -fx-border-width: 1.5; -fx-border-radius: 12;" +
                        "-fx-padding: 14 16;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(46,125,50,0.18), 10, 0, 0, 3);";

        NewsListCell() {
            card.setStyle(CARD_NORMAL);
            card.setOnMouseEntered(e -> card.setStyle(CARD_HOVER));
            card.setOnMouseExited(e -> card.setStyle(CARD_NORMAL));

            sourceTag.setStyle(
                    "-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32;" +
                            "-fx-font-size: 10px; -fx-font-weight: bold;" +
                            "-fx-padding: 3 8; -fx-background-radius: 10;"
            );
            dateLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #AAAAAA;");

            Region rowSpacer = new Region();
            HBox.setHgrow(rowSpacer, Priority.ALWAYS);
            topRow.setAlignment(Pos.CENTER_LEFT);
            topRow.getChildren().addAll(sourceTag, rowSpacer, dateLbl);

            titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1B2D1B;");
            titleLbl.setWrapText(true);
            titleLbl.setMaxWidth(Double.MAX_VALUE);

            descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");
            descLbl.setWrapText(true);
            descLbl.setMaxWidth(Double.MAX_VALUE);

            linkLbl.setStyle("-fx-text-fill: #1976D2; -fx-font-size: 11px; -fx-font-weight: bold;");

            Separator sep = new Separator();
            card.getChildren().addAll(topRow, titleLbl, descLbl, sep, linkLbl);

            setStyle("-fx-background-color: transparent; -fx-padding: 5 0;");
        }

        @Override
        protected void updateItem(NewsArticle article, boolean empty) {
            super.updateItem(article, empty);
            if (empty || article == null) {
                setGraphic(null);
                return;
            }

            sourceTag.setText("📰 " + (article.source != null ? article.source : "Source inconnue"));
            dateLbl.setText(article.publishedAt != null && article.publishedAt.length() >= 10
                    ? article.publishedAt.substring(0, 10) : "");
            titleLbl.setText(article.title != null ? article.title : "(Sans titre)");
            descLbl.setText(article.description != null && !article.description.isEmpty()
                    ? article.description : "Aucune description disponible.");

            linkLbl.setOnAction(e -> {
                if (article.url != null && !article.url.isEmpty()) {
                    openArticleInBrowser(article.url);
                }
            });

            setGraphic(card);
        }
    }

    private void openArticleInBrowser(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le navigateur: " + e.getMessage());
        }
    }

    private String escapeJsonChat(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ============================================================================
    // NAVIGATION
    // ============================================================================

    @FXML
    void goToRessources(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/agrifund/fxml/ressourceproject.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Gestion des Ressources");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible de charger la page Ressources: " + e.getMessage());
        }
    }

    // ============================================================================
    // UTILITY
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
