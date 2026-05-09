package com.agrifund.controller;

import com.agrifund.entities.Agriculteur;
import com.agrifund.entities.Utilisateur;
import com.agrifund.entities.projectagricole;
import com.agrifund.services.AgriculteurService;
import com.agrifund.services.projectagricoleCRUD;
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
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.PauseTransition;

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

// Mapbox / WebView
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import javafx.scene.layout.StackPane;

// Agromonitoring / HTTP
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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

    // Location fields (map picker)
    @FXML private TextField tfLatitudeDialog;
    @FXML private TextField tfLongitudeDialog;
    @FXML private Label lblLocationStatus;
    @FXML private Button btnOpenMap;

    // Picked coordinates (null = not set)
    private Double pickedLatitude = null;
    private Double pickedLongitude = null;

    // ============================================================================
    // SHARED FIELDS
    // ============================================================================
    private List<projectagricole> allProjects = new ArrayList<>();
    private projectagricole selectedProject = null;
    private projectagricole currentProject = null; // For modify operation
    private final projectagricoleCRUD service = new projectagricoleCRUD();
    private Timeline autoRefreshTimeline;
    private static final int REFRESH_INTERVAL_SECONDS = 3; // Faster polling for near-real-time updates
    // ============================================================================
// SESSION - AGRICULTEUR CONNECTÉ
// ============================================================================
    private Utilisateur currentUser;
    private Agriculteur currentAgriculteur;
    private AgriculteurService agriculteurService;
    // Tracks the last known status of each project (idproject -> statut)
    // Used to detect status changes triggered by decisionfinanciere inserts
    private Map<Integer, String> previousStatuses = new HashMap<>();

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
                // Seed the previousStatuses snapshot so the first auto-refresh
                // can detect any status changes made while the app was closed
                for (projectagricole p : allProjects) {
                    previousStatuses.put(p.getIdproject(), p.getStatut());
                }
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

                // Pre-fill coordinates if project already has them
                if (currentProject.getLatitude() != null && currentProject.getLongitude() != null) {
                    pickedLatitude  = currentProject.getLatitude();
                    pickedLongitude = currentProject.getLongitude();
                    String latStr = String.format("%.6f", pickedLatitude);
                    String lngStr = String.format("%.6f", pickedLongitude);
                    if (tfLatitudeDialog  != null) tfLatitudeDialog.setText(latStr);
                    if (tfLongitudeDialog != null) tfLongitudeDialog.setText(lngStr);
                    if (lblLocationStatus != null) {
                        lblLocationStatus.setText("✅ Localisation définie : " + latStr + ", " + lngStr);
                        lblLocationStatus.setStyle("-fx-text-fill: #076A39; -fx-font-size: 11px; -fx-font-weight: bold;");
                    }
                }
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
                currentProject.setLatitude(pickedLatitude);
                currentProject.setLongitude(pickedLongitude);
                // Do NOT update status - it's managed by financial decisions

                service.modifier(currentProject);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Projet modifié avec succès!\n\n" +
                                "Note: Le statut reste inchangé (" +
                                getStatusDisplayName(currentProject.getStatut()) + ")");
            } else {
                // ADD MODE
                // ✅ NOUVEAU CODE - avec agriculteurId
                projectagricole p = new projectagricole(
                        currentAgriculteur.getAgriculteurId(),  // ID de l'agriculteur connecté
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

    // ============================================================================
    // MAP PICKER (MAPBOX via WebView)
    // ============================================================================

    /**
     * Opens the Mapbox location picker in a modal WebView dialog.
     * Called from the FXML button "Choisir sur la carte".
     */
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

        java.net.URL mapUrl = getClass().getResource("/mapbox_picker.html");
        if (mapUrl == null) {
            showAlert(Alert.AlertType.ERROR, "Fichier manquant",
                    "Le fichier mapbox_picker.html est introuvable.\n" +
                            "Assurez-vous qu'il est dans src/main/resources/");
            return;
        }

        // PRIMARY: title-based callback — JS sets document.title = 'COORDS|lat|lng'
        // Using pipe '|' as separator to avoid conflicts with colons in negative coords
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

        // SECONDARY: inject Java bridge object into JS window after page loads
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                // Non-static bridge — required for JavaFX WebView JSObject bridge
                MapBridge bridge = new MapBridge(mapStage, this);
                JSObject jsWindow = (JSObject) engine.executeScript("window");
                jsWindow.setMember("javaBridge", bridge);

                // Pre-place existing marker when editing a project
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

    /**
     * Called when coordinates are confirmed (from either bridge or title listener).
     * Runs on the JavaFX Application Thread.
     */
    public void onLocationPicked(double lat, double lng, Stage stageToClose) {
        pickedLatitude  = lat;
        pickedLongitude = lng;
        String latStr = String.format(java.util.Locale.US, "%.6f", lat);
        String lngStr = String.format(java.util.Locale.US, "%.6f", lng);

        javafx.application.Platform.runLater(() -> {
            // Close the map window first
            if (stageToClose != null && stageToClose.isShowing()) {
                stageToClose.close();
            }
            // Then update the form fields
            if (tfLatitudeDialog  != null) tfLatitudeDialog.setText(latStr);
            if (tfLongitudeDialog != null) tfLongitudeDialog.setText(lngStr);
            if (lblLocationStatus != null) {
                lblLocationStatus.setText("Localisation definie : " + latStr + ", " + lngStr);
                lblLocationStatus.setStyle("-fx-text-fill: #076A39; -fx-font-size: 11px; -fx-font-weight: bold;");
            }
        });
    }

    /**
     * Non-static JS<->Java bridge class.
     * Must be non-static and have public methods for JavaFX WebView JSObject to call it.
     */
    public class MapBridge {
        private final Stage mapStage;
        private final projectagricolecontroller controller;

        public MapBridge(Stage mapStage, projectagricolecontroller controller) {
            this.mapStage   = mapStage;
            this.controller = controller;
        }

        /** Called directly from JS: window.javaBridge.setCoordinates("33.123", "9.456") */
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

        // Check uniqueness of project name
        String newName = getDialogFieldValue(tfNomProjectDialog);
        try {
            List<projectagricole> existingProjects = service.afficher();
            boolean nameExistsOnOtherProject = existingProjects.stream()
                    .filter(p -> "modify".equals(dialogMode) && currentProject != null
                            ? p.getIdproject() != currentProject.getIdproject()
                            : true)
                    .anyMatch(p -> p.getNomproject().equalsIgnoreCase(newName));
            if (nameExistsOnOtherProject) {
                showAlert(Alert.AlertType.ERROR, "Nom déjà utilisé",
                        "Un projet avec le nom \"" + newName + "\" existe déjà.\n" +
                                "Le nom du projet doit être unique!");
                return false;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Impossible de vérifier l'unicité du nom: " + e.getMessage());
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
            List<projectagricole> freshProjects = service.afficher();
            if (freshProjects == null) {
                freshProjects = new ArrayList<>();
            }

            // Detect which projects had their status changed by a decisionfinanciere trigger
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

            // After cards are rendered, flash the ones that changed
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

    /**
     * Flashes (scale pulse + glow border) each card whose project id is in changedIds.
     * Cards are identified by the userData tag set in createEnhancedProjectCard.
     */
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

    /**
     * Short scale-pulse + golden border flash to draw attention to a status-changed card.
     */
    private void playCardChangedAnimation(VBox card) {
        // Save original style then apply highlight border
        String originalStyle = card.getStyle();
        card.setStyle(originalStyle +
                "-fx-border-color: #E1B323; -fx-border-width: 3; -fx-border-radius: 12;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(225,179,35,0.7), 16, 0, 0, 0);");

        // Scale up slightly then back
        ScaleTransition scaleUp = new ScaleTransition(javafx.util.Duration.millis(180), card);
        scaleUp.setToX(1.04);
        scaleUp.setToY(1.04);

        ScaleTransition scaleDown = new ScaleTransition(javafx.util.Duration.millis(180), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        PauseTransition hold = new PauseTransition(javafx.util.Duration.seconds(2.5));

        SequentialTransition seq = new SequentialTransition(scaleUp, scaleDown, hold);
        seq.setOnFinished(e -> card.setStyle(originalStyle)); // restore
        seq.play();
    }

    /**
     * Shows a brief non-blocking status-change notification at the top of the card container.
     */
    private void showStatusChangeToast(List<Integer> changedIds, List<projectagricole> projects) {
        if (projectsContainer == null) return;

        StringBuilder msg = new StringBuilder("🔔 Statut mis à jour par décision financière:\n");
        for (int id : changedIds) {
            projects.stream()
                    .filter(p -> p.getIdproject() == id)
                    .findFirst()
                    .ifPresent(p -> msg.append("  • ").append(p.getNomproject())
                            .append(" → ").append(capitalizeStatus(p.getStatut())).append("\n"));
        }

        Label toast = new Label(msg.toString().trim());
        toast.setStyle(
                "-fx-background-color: #076A39;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 20;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.35), 10, 0, 0, 3);" +
                        "-fx-wrap-text: true;"
        );
        toast.setWrapText(true);
        toast.setMaxWidth(500);

        // Insert toast at top of container (index 0) so it's visible
        javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) projectsContainer.getParent();
        if (parent == null) return;

        // Use a dedicated overlay StackPane if available, otherwise use an Alert-free approach
        // We repurpose the projectsContainer's scene root to overlay a floating label
        javafx.scene.layout.StackPane overlay = new javafx.scene.layout.StackPane(toast);
        overlay.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        overlay.setPadding(new javafx.geometry.Insets(12));
        overlay.setMouseTransparent(true);
        overlay.setStyle("-fx-background-color: transparent;");

        javafx.scene.layout.BorderPane sceneRoot;
        try {
            sceneRoot = (javafx.scene.layout.BorderPane) projectsContainer.getScene().getRoot();
        } catch (ClassCastException ex) {
            // If the root is not a BorderPane, fall back to a simple alert
            showAlert(Alert.AlertType.INFORMATION, "Décision financière reçue", msg.toString().trim());
            return;
        }

        // Add overlay on top and remove it after 3.5 seconds with a fade-out
        sceneRoot.getChildren().add(overlay);
        javafx.scene.layout.BorderPane.setAlignment(overlay, javafx.geometry.Pos.TOP_CENTER);

        FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.seconds(1.2), toast);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        PauseTransition wait = new PauseTransition(javafx.util.Duration.seconds(2.5));
        SequentialTransition toastAnim = new SequentialTransition(wait, fadeOut);
        toastAnim.setOnFinished(e -> sceneRoot.getChildren().remove(overlay));
        toastAnim.play();
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
        // Tag card with project ID so highlight animation can locate it after refresh
        card.setUserData(project.getIdproject());

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

        // Conseils button — full width, below icon buttons
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectagricolemodify.fxml"));
            Parent root = loader.load();

            projectagricolecontroller controller = loader.getController();
            controller.dialogMode = "modify";
            controller.setProject(project);
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier Projet Agricole — " + project.getNomproject());
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
        Stage detailStage = new Stage();
        detailStage.initModality(Modality.APPLICATION_MODAL);
        detailStage.setTitle("Détails du Projet — " + project.getNomproject());

        boolean hasLocation = project.getLatitude() != null && project.getLongitude() != null;

        // ── Left panel: project info ──────────────────────────────────
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
            addDetailRow(detailsGrid, 3, "📍 Latitude :",  String.format(java.util.Locale.US, "%.6f", project.getLatitude()));
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

        // ── Right panel: map WebView (only if location exists) ────────
        javafx.scene.web.WebView mapView = null;
        if (hasLocation) {
            mapView = new javafx.scene.web.WebView();
            mapView.setPrefWidth(480);
            mapView.setPrefHeight(500);
            mapView.setMinWidth(480);

            final javafx.scene.web.WebEngine engine = mapView.getEngine();
            engine.setJavaScriptEnabled(true);

            java.net.URL mapUrl = getClass().getResource("/mapbox_picker.html");
            if (mapUrl != null) {
                final double lat = project.getLatitude();
                final double lng = project.getLongitude();
                engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                        // Show read-only marker — disable click-to-place and bottom bar
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

        // ── Assemble layout ───────────────────────────────────────────
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


    @FXML
    void openAddProjectForm(ActionEvent event) {
        openAddDialog();
    }

    // ============================================================================
    // ALL-PROJECTS MAP  ← FIXED
    // ============================================================================

    /**
     * Opens a full-screen map showing every project as a coloured pin:
     *   green  = accepté  |  orange = en cours  |  red = refusé
     *
     * FIX 1: budget is serialised as a JSON string (not a bare number) to avoid
     *         commas inside a numeric literal when budget >= 1000.
     * FIX 2: projectsJson is passed directly to engine.executeScript() as a JS
     *         array literal — no single-quote wrapping, no jsEscape().
     */
    @FXML
    void handleShowAllProjectsMap(ActionEvent event) {
        if (allProjects == null || allProjects.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucun projet",
                    "Aucun projet disponible à afficher sur la carte.");
            return;
        }

        java.net.URL mapUrl = getClass().getResource("/projects_map.html");
        if (mapUrl == null) {
            showAlert(Alert.AlertType.ERROR, "Fichier manquant",
                    "projects_map.html introuvable dans src/main/resources/");
            return;
        }

        // Build a valid JSON array — budget serialised as a JSON *string* so that
        // comma-formatted values like "12,345.67" do not break JSON.parse().
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < allProjects.size(); i++) {
            projectagricole p = allProjects.get(i);
            if (i > 0) json.append(",");
            json.append("{");
            json.append("\"nom\":").append(jsonStr(p.getNomproject())).append(",");
            json.append("\"statut\":").append(jsonStr(p.getStatut())).append(",");
            // surface stays a number — no comma grouping in "%.2f"
            json.append("\"surface\":").append(String.format(java.util.Locale.US, "%.2f", p.getSurface())).append(",");
            // budget as a *string* to keep the comma-formatted display safe
            json.append("\"budget\":").append(jsonStr(
                    String.format(java.util.Locale.US, "%,.2f", p.getBudgetdemande()))).append(",");
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

        // Build the stage with a WebView
        Stage mapStage = new Stage();
        mapStage.initModality(Modality.APPLICATION_MODAL);
        mapStage.setTitle("🗺 Carte des Projets Agricoles");
        mapStage.setWidth(1100);
        mapStage.setHeight(720);

        javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
        javafx.scene.web.WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);

        // Keep a final copy for use inside the lambda
        final String projectsJson = json.toString();

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                // FIX: pass the JSON array directly as a JS expression.
                // projectsJson is already a valid JS array literal — no quoting needed.
                engine.executeScript("loadProjects(" + projectsJson + ")");
            }
        });

        engine.load(mapUrl.toExternalForm());

        mapStage.setScene(new Scene(new StackPane(webView)));
        mapStage.show();
    }

    /** Wraps a Java string as a JSON string literal (escapes quotes and backslashes). */
    private String jsonStr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
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
    // AGROMONITORING + AI CONSEILS
    // ============================================================================

    private static final String AGRO_API_KEY  = "15ec96f6a29c1d2c59280c84a585bb66";
    private static final String CLAUDE_API_KEY = ""; // optional — leave empty to skip AI advice

    /**
     * Entry point for the "Conseils du projet" button.
     * Runs HTTP calls on a background thread then updates the UI on FX thread.
     */
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
        javafx.scene.control.ProgressIndicator spinner = new javafx.scene.control.ProgressIndicator();
        VBox loadingBox = new VBox(15, spinner, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(30));
        loadingStage.setScene(new Scene(loadingBox, 340, 160));
        loadingStage.show();

        double lat = project.getLatitude();
        double lng = project.getLongitude();

        // Run API calls off the FX thread
        Thread thread = new Thread(() -> {
            try {
                // ── 1. Current weather ────────────────────────────────
                String weatherJson = httpGet(
                        "http://api.agromonitoring.com/agro/1.0/weather?lat=" + lat
                                + "&lon=" + lng + "&appid=" + AGRO_API_KEY);

                // ── 2. Soil data ──────────────────────────────────────
                String soilJson = httpGet(
                        "http://api.agromonitoring.com/agro/1.0/soil?polyid=&lat=" + lat
                                + "&lon=" + lng + "&appid=" + AGRO_API_KEY);

                // ── 3. UV index ───────────────────────────────────────
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

    /** Simple HTTP GET returning the response body as a String. */
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

    /** Parse a numeric value from a simple JSON string by key name. */
    private String parseJson(String json, String key) {
        if (json == null || json.isEmpty()) return "—";
        try {
            // Match  "key":value  or  "key":"value"
            int idx = json.indexOf("\"" + key + "\"");
            if (idx < 0) return "—";
            int colon = json.indexOf(":", idx);
            if (colon < 0) return "—";
            // skip whitespace
            int start = colon + 1;
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
            if (start >= json.length()) return "—";
            char first = json.charAt(start);
            if (first == '"') {
                // string value
                int end = json.indexOf('"', start + 1);
                return end > start ? json.substring(start + 1, end) : "—";
            } else {
                // numeric / boolean / null
                int end = start;
                while (end < json.length() && ",}]\n\r".indexOf(json.charAt(end)) < 0) end++;
                return json.substring(start, end).trim();
            }
        } catch (Exception e) { return "—"; }
    }

    /** Parse a nested value like  "main":{"temp":...} */
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
        } catch (Exception e) { return "—"; }
    }

    /**
     * Converts Kelvin to Celsius string.
     */
    private String kelvinToCelsius(String kelvinStr) {
        try {
            double k = Double.parseDouble(kelvinStr);
            return String.format("%.1f°C", k - 273.15);
        } catch (Exception e) { return kelvinStr; }
    }

    /**
     * Builds and shows the full advice dialog from parsed API data.
     */
    private void showAdviceDialog(projectagricole project,
                                  String weatherJson, String soilJson, String uvJson) {

        // ── Parse weather ─────────────────────────────────────────
        String tempK      = parseNestedJson(weatherJson, "main", "temp");
        String humidity   = parseNestedJson(weatherJson, "main", "humidity");
        String windSpeed  = parseNestedJson(weatherJson, "wind", "speed");
        String pressure   = parseNestedJson(weatherJson, "main", "pressure");
        String cloudiness = parseNestedJson(weatherJson, "clouds", "all");
        String weatherDesc= parseNestedJson(weatherJson, "weather", "description");
        String cityName   = parseJson(weatherJson, "name");
        String tempC      = kelvinToCelsius(tempK);

        // ── Parse soil ────────────────────────────────────────────
        String soilMoisture = parseJson(soilJson, "moisture");
        String soilTempK    = parseJson(soilJson, "t0");   // surface temp
        String soilTempC    = kelvinToCelsius(soilTempK);

        // ── Parse UV ─────────────────────────────────────────────
        String uvIndex = parseJson(uvJson, "value");
        if ("—".equals(uvIndex)) uvIndex = parseJson(uvJson, "uvi");

        // ── Generate advice text locally ──────────────────────────
        String advice = generateAgriculturalAdvice(
                project, tempK, humidity, windSpeed, soilMoisture, uvIndex, cloudiness);

        // ── Build dialog ──────────────────────────────────────────
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("🌱 Conseils — " + project.getNomproject());
        stage.setWidth(720);
        stage.setHeight(700);

        // Scrollable content
        VBox content = new VBox(18);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F8FFF9;");

        // ── Header ────────────────────────────────────────────────
        VBox headerBox = new VBox(4);
        headerBox.setStyle(
                "-fx-background-color: linear-gradient(to right,#076A39,#089647);" +
                        "-fx-padding: 18 24; -fx-background-radius: 12;");
        Label hTitle = new Label("🌱 Conseils Agricoles — " + project.getNomproject());
        hTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label hSub = new Label("📍 " + String.format("%.4f, %.4f", project.getLatitude(), project.getLongitude())
                + (cityName.equals("—") ? "" : "   🏙 " + cityName));
        hSub.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.85);");
        headerBox.getChildren().addAll(hTitle, hSub);

        // ── Weather card ──────────────────────────────────────────
        VBox weatherCard = buildInfoCard("🌦 Météo Actuelle", new String[][]{
                {"🌡 Température",    tempC},
                {"💧 Humidité",       humidity + "%"},
                {"💨 Vent",           windSpeed + " m/s"},
                {"🌫 Pression",       pressure + " hPa"},
                {"☁ Nébulosité",     cloudiness + "%"},
                {"📝 Conditions",     weatherDesc}
        }, "#E8F5E9", "#2E7D32");

        // ── Soil card ─────────────────────────────────────────────
        VBox soilCard = buildInfoCard("🌍 Données du Sol", new String[][]{
                {"💧 Humidité sol",   soilMoisture.equals("—") ? "—" : String.format("%.3f m³/m³", safeDouble(soilMoisture))},
                {"🌡 Temp. surface",  soilTempC}
        }, "#FFF8E1", "#F57F17");

        // ── UV card ───────────────────────────────────────────────
        String uvLevel = uvLevel(uvIndex);
        VBox uvCard = buildInfoCard("☀ Rayonnement UV", new String[][]{
                {"🔆 Indice UV",  uvIndex},
                {"⚠ Niveau",     uvLevel}
        }, "#E3F2FD", "#1565C0");

        // ── Advice card ───────────────────────────────────────────
        VBox adviceCard = new VBox(10);
        adviceCard.setStyle(
                "-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 10;" +
                        "-fx-border-color: #076A39; -fx-border-width: 2; -fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box,rgba(7,106,57,0.15),8,0,0,2);");
        Label adviceTitle = new Label("🤖 Recommandations Agricoles");
        adviceTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #076A39;");
        Label adviceText = new Label(advice);
        adviceText.setWrapText(true);
        adviceText.setStyle("-fx-font-size: 13px; -fx-text-fill: #2C3E50; -fx-line-spacing: 3px;");
        adviceCard.getChildren().addAll(adviceTitle, adviceText);

        // ── Satellite note ────────────────────────────────────────
        Label satNote = new Label(
                "🛰 Données satellitaires via Agromonitoring API  •  Surface: "
                        + String.format("%.2f Ha", project.getSurface())
                        + "  •  Projet: " + capitalizeStatus(project.getStatut()));
        satNote.setStyle("-fx-font-size: 11px; -fx-text-fill: #888; -fx-font-style: italic;");

        // ── Close button ──────────────────────────────────────────
        Button closeBtn = new Button("✖  Fermer");
        closeBtn.setStyle(
                "-fx-background-color: #076A39; -fx-text-fill: white; -fx-font-weight: bold;" +
                        "-fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13px;");
        closeBtn.setOnAction(e -> stage.close());
        HBox btnRow = new HBox(closeBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(
                headerBox, weatherCard, soilCard, uvCard, adviceCard, satNote, btnRow);

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F8FFF9; -fx-border-width: 0;");

        stage.setScene(new Scene(scroll));
        stage.show();
    }

    /** Builds a styled info card with a list of label→value rows. */
    private VBox buildInfoCard(String title, String[][] rows, String bgColor, String accentColor) {
        VBox card = new VBox(8);
        card.setStyle(
                "-fx-background-color: " + bgColor + "; -fx-padding: 14 18;" +
                        "-fx-background-radius: 10; -fx-border-color: " + accentColor + "33;" +
                        "-fx-border-width: 1; -fx-border-radius: 10;");
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");
        card.getChildren().add(lbl);
        card.getChildren().add(new Separator());

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(6);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setMinWidth(150);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setMinWidth(200);
        grid.getColumnConstraints().addAll(c1, c2);

        int row = 0;
        for (String[] pair : rows) {
            Label k = new Label(pair[0]);
            k.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
            Label v = new Label(pair.length > 1 ? pair[1] : "—");
            v.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
            grid.add(k, 0, row); grid.add(v, 1, row); row++;
        }
        card.getChildren().add(grid);
        return card;
    }

    private double safeDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    private String uvLevel(String uvStr) {
        try {
            double uv = Double.parseDouble(uvStr);
            if (uv < 3)  return "Faible — Travail normal";
            if (uv < 6)  return "Modéré — Protection recommandée";
            if (uv < 8)  return "Élevé — Éviter 11h-16h";
            if (uv < 11) return "Très élevé — Protection obligatoire";
            return "Extrême — Éviter l'exposition";
        } catch (Exception e) { return "—"; }
    }

    /**
     * Generates local agricultural advice based on the sensor data.
     */
    private String generateAgriculturalAdvice(projectagricole project,
                                              String tempK, String humidity, String windSpeed,
                                              String soilMoisture, String uvIndex, String cloudiness) {

        StringBuilder advice = new StringBuilder();
        double tempC = 0, hum = 0, wind = 0, soil = 0, uv = 0, cloud = 0;

        try { tempC  = Double.parseDouble(tempK) - 273.15; } catch (Exception ignored) {}
        try { hum    = Double.parseDouble(humidity);       } catch (Exception ignored) {}
        try { wind   = Double.parseDouble(windSpeed);      } catch (Exception ignored) {}
        try { soil   = Double.parseDouble(soilMoisture);   } catch (Exception ignored) {}
        try { uv     = Double.parseDouble(uvIndex);        } catch (Exception ignored) {}
        try { cloud  = Double.parseDouble(cloudiness);     } catch (Exception ignored) {}

        // Temperature advice
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

        // Humidity advice
        if (hum < 30) {
            advice.append("💧 Humidité très basse (").append((int)hum)
                    .append("%) — Irrigation urgente recommandée. Risque de stress hydrique.\n\n");
        } else if (hum < 60) {
            advice.append("💧 Humidité modérée (").append((int)hum)
                    .append("%) — Surveillez le sol. Irrigation légère possible.\n\n");
        } else {
            advice.append("💧 Humidité suffisante (").append((int)hum)
                    .append("%) — Risque fongique si prolongé. Assurez un bon drainage.\n\n");
        }

        // Soil moisture advice
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

        // Wind advice
        if (wind > 10) {
            advice.append("💨 Vent fort (").append(String.format("%.1f", wind))
                    .append(" m/s) — Évitez les traitements phytosanitaires. Protégez les jeunes plants.\n\n");
        } else if (wind > 5) {
            advice.append("💨 Vent modéré — Conditions moyennes pour les épandages.\n\n");
        }

        // UV advice
        if (uv >= 6) {
            advice.append("☀ UV élevé (").append(String.format("%.1f", uv))
                    .append(") — Travaillez tôt le matin ou en soirée. Protection individuelle obligatoire.\n\n");
        }

        // Surface-based advice
        float surface = project.getSurface();
        if (surface > 50) {
            advice.append("📏 Grande surface (").append(String.format("%.0f", surface))
                    .append(" Ha) — Envisagez l'utilisation de drones ou matériel mécanisé.\n\n");
        }

        // Cloud/spray advice
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