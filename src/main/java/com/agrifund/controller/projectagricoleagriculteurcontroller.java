package com.agrifund.controller;

import com.agrifund.entities.projectagricole;
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

// Agromonitoring / HTTP
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

// News API
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class projectagricoleagriculteurcontroller implements Initializable {

    // ============================================================================
    // CURRENT LOGGED-IN AGRICULTEUR ID
    // Set this from your session/login before loading this controller.
    // ============================================================================
    private static int currentAgriculteurId = -1;

    public static void setCurrentAgriculteurId(int id) {
        currentAgriculteurId = id;
    }

    // ============================================================================
    // MAIN VIEW FIELDS
    // ============================================================================
    @FXML private FlowPane projectsContainer;

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

    // Picked coordinates
    private Double pickedLatitude = null;
    private Double pickedLongitude = null;

    // ============================================================================
    // SHARED FIELDS
    // ============================================================================
    // Only the projects belonging to this agriculteur
    private List<projectagricole> allProjects = new ArrayList<>();
    private projectagricole selectedProject = null;
    private projectagricole currentProject = null;
    private final projectagricoleCRUD service = new projectagricoleCRUD();
    private Timeline autoRefreshTimeline;
    private static final int REFRESH_INTERVAL_SECONDS = 3;

    private Map<Integer, String> previousStatuses = new HashMap<>();

    private projectagricoleagriculteurcontroller mainController;
    private String dialogMode = "list";

    // ============================================================================
    // INITIALIZATION
    // ============================================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (projectsContainer != null) {
            try {
                refreshDataFromDB();
                for (projectagricole p : allProjects) {
                    previousStatuses.put(p.getIdproject(), p.getStatut());
                }
                startAutoRefresh();
            } catch (Exception e) {
                System.err.println("Error loading initial data: " + e.getMessage());
                e.printStackTrace();
                allProjects = new ArrayList<>();
                updateCardsDisplay();
                updateFooterStats();
                showAlert(Alert.AlertType.ERROR, "Erreur de connexion",
                        "Impossible de se connecter à la base de données.\n\nDétails: " + e.getMessage());
            }
        }

        // Setup for add dialog
        if (lblStatusBadge != null && btnSave != null && dpDateSoumissionDialog != null) {
            setupAddDialog();
        }
    }

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

    public void setProject(projectagricole project) {
        this.currentProject = project;
        this.dialogMode = "modify";
        populateFields();
    }

    public void setMainController(projectagricoleagriculteurcontroller mainController) {
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
        String displayText, backgroundColor;
        String textColor = "white";
        switch (statut.toLowerCase()) {
            case "accepte":
                displayText = "✓ Accepté";
                backgroundColor = "linear-gradient(to right, #089647, #076A39)";
                break;
            case "refuse":
                displayText = "✗ Refusé";
                backgroundColor = "linear-gradient(to right, #D32F2F, #B71C1C)";
                break;
            default:
                displayText = "📋 En cours";
                backgroundColor = "linear-gradient(to right, #E1B323, #9A951F)";
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
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 2);" +
                        "-fx-cursor: hand;"
        );
    }

    // ============================================================================
    // ADD/MODIFY DIALOG HANDLERS
    // ============================================================================

    @FXML
    void handleSave(ActionEvent event) {
        if (!validateDialogInputs()) return;

        try {
            if ("modify".equals(dialogMode)) {
                currentProject.setNomproject(getDialogFieldValue(tfNomProjectDialog));
                currentProject.setSurface(Float.parseFloat(getDialogFieldValue(tfSurfaceDialog)));
                currentProject.setBudgetdemande(new BigDecimal(getDialogFieldValue(tfBudgetDialog)));
                currentProject.setDatesoumission(Date.valueOf(dpDateSoumissionDialog.getValue()));
                currentProject.setLatitude(pickedLatitude);
                currentProject.setLongitude(pickedLongitude);

                service.modifier(currentProject);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Projet modifié avec succès!");
            } else {
                projectagricole p = new projectagricole(
                        getDialogFieldValue(tfNomProjectDialog),
                        Float.parseFloat(getDialogFieldValue(tfSurfaceDialog)),
                        new BigDecimal(getDialogFieldValue(tfBudgetDialog)),
                        "en cours",
                        Date.valueOf(dpDateSoumissionDialog.getValue())
                );
                p.setLatitude(pickedLatitude);
                p.setLongitude(pickedLongitude);
                // Associate with current agriculteur if your entity supports it
                // p.setIdagriculteur(currentAgriculteurId);

                service.ajouter(p);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Projet ajouté avec succès!\n\nStatut: En cours (en attente de décision financière)");
            }

            if (mainController != null) {
                mainController.refreshDataFromDB();
            }
            closeDialogWindow();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de l'opération: " + e.getMessage());
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
                    "Le fichier mapbox_picker.html est introuvable.\nAssurez-vous qu'il est dans src/main/resources/");
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
                    } catch (NumberFormatException ignored) {}
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
        mapStage.setScene(new Scene(new StackPane(webView)));
        mapStage.showAndWait();
    }

    public void onLocationPicked(double lat, double lng, Stage stageToClose) {
        pickedLatitude = lat;
        pickedLongitude = lng;
        String latStr = String.format(java.util.Locale.US, "%.6f", lat);
        String lngStr = String.format(java.util.Locale.US, "%.6f", lng);

        javafx.application.Platform.runLater(() -> {
            if (stageToClose != null && stageToClose.isShowing()) stageToClose.close();
            if (tfLatitudeDialog != null) tfLatitudeDialog.setText(latStr);
            if (tfLongitudeDialog != null) tfLongitudeDialog.setText(lngStr);
            if (lblLocationStatus != null) {
                lblLocationStatus.setText("Localisation definie : " + latStr + ", " + lngStr);
                lblLocationStatus.setStyle("-fx-text-fill: #076A39; -fx-font-size: 11px; -fx-font-weight: bold;");
            }
        });
    }

    public class MapBridge {
        private final Stage mapStage;
        private final projectagricoleagriculteurcontroller controller;

        public MapBridge(Stage mapStage, projectagricoleagriculteurcontroller controller) {
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
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    private String getDialogFieldValue(TextField field) {
        return field != null ? field.getText().trim() : "";
    }

    private boolean validateDialogInputs() {
        if (getDialogFieldValue(tfNomProjectDialog).isEmpty() ||
                getDialogFieldValue(tfSurfaceDialog).isEmpty() ||
                getDialogFieldValue(tfBudgetDialog).isEmpty() ||
                dpDateSoumissionDialog.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir tous les champs!");
            return false;
        }

        if (getDialogFieldValue(tfNomProjectDialog).length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Le nom du projet doit contenir au moins 3 caractères!");
            return false;
        }

        String newName = getDialogFieldValue(tfNomProjectDialog);
        try {
            List<projectagricole> existingProjects = service.afficher();
            boolean nameExists = existingProjects.stream()
                    .filter(p -> "modify".equals(dialogMode) && currentProject != null
                            ? p.getIdproject() != currentProject.getIdproject() : true)
                    .anyMatch(p -> p.getNomproject().equalsIgnoreCase(newName));
            if (nameExists) {
                showAlert(Alert.AlertType.ERROR, "Nom déjà utilisé",
                        "Un projet avec le nom \"" + newName + "\" existe déjà.");
                return false;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Impossible de vérifier l'unicité du nom: " + e.getMessage());
            return false;
        }

        if ("add".equals(dialogMode)) {
            if (!dpDateSoumissionDialog.getValue().isEqual(java.time.LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Erreur de date",
                        "La date de soumission doit être la date d'aujourd'hui uniquement!");
                return false;
            }
        } else if ("modify".equals(dialogMode)) {
            if (dpDateSoumissionDialog.getValue().isAfter(java.time.LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Erreur de date",
                        "La date de soumission ne peut pas être dans le futur!");
                return false;
            }
        }

        try {
            float surface = Float.parseFloat(getDialogFieldValue(tfSurfaceDialog));
            if (surface <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", "La surface doit être un nombre positif!");
                return false;
            }
            BigDecimal budget = new BigDecimal(getDialogFieldValue(tfBudgetDialog));
            if (budget.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le budget doit être un nombre positif!");
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
    // DATA LOADING — filtered by agriculteur
    // ============================================================================

    public void refreshDataFromDB() {
        try {
            List<projectagricole> freshProjects = service.afficher();
            if (freshProjects == null) freshProjects = new ArrayList<>();

            // ── Filter: only keep this agriculteur's projects ──────────
            // Adapt the condition below to match your entity field.
            // If projectagricole has getIdagriculteur(), use:
            //   freshProjects = freshProjects.stream()
            //       .filter(p -> p.getIdagriculteur() == currentAgriculteurId)
            //       .collect(Collectors.toList());
            // For now we load all (remove when session management is ready).
            // ────────────────────────────────────────────────────────────

            // Detect status changes
            List<Integer> changedProjectIds = new ArrayList<>();
            if (!previousStatuses.isEmpty()) {
                for (projectagricole p : freshProjects) {
                    String oldStatut = previousStatuses.get(p.getIdproject());
                    if (oldStatut != null && !oldStatut.equals(p.getStatut())) {
                        changedProjectIds.add(p.getIdproject());
                    }
                }
            }

            previousStatuses.clear();
            for (projectagricole p : freshProjects) {
                previousStatuses.put(p.getIdproject(), p.getStatut());
            }

            allProjects = freshProjects;
            updateCardsDisplay();
            updateFooterStats();

            if (!changedProjectIds.isEmpty()) {
                highlightChangedCards(changedProjectIds);
                showStatusChangeToast(changedProjectIds, freshProjects);
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            if (allProjects == null) allProjects = new ArrayList<>();
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
        scaleUp.setToX(1.04); scaleUp.setToY(1.04);
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(180), card);
        scaleDown.setToX(1.0); scaleDown.setToY(1.0);
        PauseTransition hold = new PauseTransition(Duration.seconds(2.5));
        SequentialTransition seq = new SequentialTransition(scaleUp, scaleDown, hold);
        seq.setOnFinished(e -> card.setStyle(originalStyle));
        seq.play();
    }

    private void showStatusChangeToast(List<Integer> changedIds, List<projectagricole> projects) {
        if (projectsContainer == null) return;
        StringBuilder msg = new StringBuilder("🔔 Statut mis à jour par décision financière:\n");
        for (int id : changedIds) {
            projects.stream().filter(p -> p.getIdproject() == id).findFirst()
                    .ifPresent(p -> msg.append("  • ").append(p.getNomproject())
                            .append(" → ").append(capitalizeStatus(p.getStatut())).append("\n"));
        }

        Label toast = new Label(msg.toString().trim());
        toast.setStyle(
                "-fx-background-color: #076A39; -fx-text-fill: white; -fx-font-size: 13px;" +
                        "-fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 10;" +
                        "-fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box,rgba(0,0,0,0.35),10,0,0,3);"
        );
        toast.setWrapText(true);
        toast.setMaxWidth(500);

        javafx.scene.layout.StackPane overlay = new javafx.scene.layout.StackPane(toast);
        overlay.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        overlay.setPadding(new Insets(12));
        overlay.setMouseTransparent(true);
        overlay.setStyle("-fx-background-color: transparent;");

        javafx.scene.layout.BorderPane sceneRoot;
        try {
            sceneRoot = (javafx.scene.layout.BorderPane) projectsContainer.getScene().getRoot();
        } catch (ClassCastException ex) {
            showAlert(Alert.AlertType.INFORMATION, "Décision financière reçue", msg.toString().trim());
            return;
        }

        sceneRoot.getChildren().add(overlay);
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.2), toast);
        fadeOut.setFromValue(1.0); fadeOut.setToValue(0.0);
        PauseTransition wait = new PauseTransition(Duration.seconds(2.5));
        SequentialTransition toastAnim = new SequentialTransition(wait, fadeOut);
        toastAnim.setOnFinished(e -> sceneRoot.getChildren().remove(overlay));
        toastAnim.play();
    }

    private void updateFooterStats() {
        if (allProjects.isEmpty()) {
            if (lblTotalBudget != null) lblTotalBudget.setText("0.00 DT");
            if (lblTotalSurface != null) lblTotalSurface.setText("0.00 Ha");
            if (lblLastUpdate != null) lblLastUpdate.setText(java.time.LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            return;
        }

        BigDecimal totalBudget = allProjects.stream()
                .map(projectagricole::getBudgetdemande)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        double totalSurface = allProjects.stream().mapToDouble(projectagricole::getSurface).sum();

        if (lblTotalBudget != null) lblTotalBudget.setText(String.format("%,.2f DT", totalBudget));
        if (lblTotalSurface != null) lblTotalSurface.setText(String.format("%.2f Ha", totalSurface));
        if (lblLastUpdate != null) lblLastUpdate.setText(java.time.LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private void updateCardsDisplay() {
        if (projectsContainer == null) return;
        projectsContainer.getChildren().clear();
        for (projectagricole p : allProjects) {
            projectsContainer.getChildren().add(createProjectCard(p));
        }
    }

    // ============================================================================
    // CARD BUILDER
    // ============================================================================

    private VBox createProjectCard(projectagricole project) {
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
        btnView.setMinWidth(40); btnView.setPrefWidth(40);

        Button btnEdit = new Button("✏");
        btnEdit.getStyleClass().add("btn-edit");
        btnEdit.setOnAction(e -> openModifyDialog(project));
        btnEdit.setTooltip(new Tooltip("Modifier"));
        btnEdit.setMinWidth(40); btnEdit.setPrefWidth(40);

        Button btnDelete = new Button("🗑");
        btnDelete.getStyleClass().add("btn-delete");
        btnDelete.setOnAction(e -> handleDelete(project));
        btnDelete.setTooltip(new Tooltip("Supprimer"));
        btnDelete.setMinWidth(40); btnDelete.setPrefWidth(40);

        Button btnChat = new Button("💬");
        btnChat.getStyleClass().add("btn-view");
        btnChat.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #1a73e8, #0d47a1);" +
                        "-fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 6 10;" +
                        "-fx-background-radius: 8; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(26,115,232,0.5), 6, 0, 0, 2);");
        btnChat.setTooltip(new Tooltip("🌿 Assistant Plante Malade — Analyse IA Gemini"));
        btnChat.setMinWidth(40); btnChat.setPrefWidth(40);
        btnChat.setOnAction(e -> openPlantDiseaseChat(project));

        actions.getChildren().addAll(btnView, btnEdit, btnDelete, btnChat);
        HBox.setHgrow(actions, Priority.ALWAYS);

        // Conseils button
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

    // ============================================================================
    // DELETE
    // ============================================================================

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
                    e.printStackTrace();
                }
            }
        });
    }

    // ============================================================================
    // DIALOG OPENERS
    // ============================================================================

    @FXML
    void openAddProjectForm(ActionEvent event) {
        openAddDialog();
    }

    private void openAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectagricoleagriculteureadd.fxml"));
            Parent root = loader.load();

            projectagricoleagriculteurcontroller controller = loader.getController();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectagricoleagriculteurmodify.fxml"));
            Parent root = loader.load();

            projectagricoleagriculteurcontroller controller = loader.getController();
            controller.dialogMode = "modify";
            controller.setProject(project);
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier Projet — " + project.getNomproject());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }

    // ============================================================================
    // AUTO-REFRESH
    // ============================================================================

    private void startAutoRefresh() {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
        autoRefreshTimeline = new Timeline(new KeyFrame(
                Duration.seconds(REFRESH_INTERVAL_SECONDS), event -> refreshDataFromDB()
        ));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    public void cleanup() {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
    }

    // ============================================================================
    // PROJECT DETAILS
    // ============================================================================

    private void showProjectDetails(projectagricole project) {
        Stage detailStage = new Stage();
        detailStage.initModality(Modality.APPLICATION_MODAL);
        detailStage.setTitle("Détails — " + project.getNomproject());

        boolean hasLocation = project.getLatitude() != null && project.getLongitude() != null;

        VBox infoPanel = new VBox(18);
        infoPanel.setPadding(new Insets(25));
        infoPanel.setStyle("-fx-background-color: white;");
        infoPanel.setPrefWidth(420);

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

        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label("Statut :");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #848A86;");
        Label statusBadge = new Label(capitalizeStatus(project.getStatut()));
        statusBadge.getStyleClass().add(getStatusBadgeClass(project.getStatut()));
        statusRow.getChildren().addAll(statusLabel, statusBadge);

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(20); detailsGrid.setVgap(12);
        detailsGrid.setPadding(new Insets(8, 0, 8, 0));
        addDetailRow(detailsGrid, 0, "🌾 Surface :", String.format("%.2f Hectares", project.getSurface()));
        addDetailRow(detailsGrid, 1, "💰 Budget demandé :", String.format("%,.2f DT", project.getBudgetdemande()));
        addDetailRow(detailsGrid, 2, "📅 Date de soumission :", project.getDatesoumission().toString());
        if (hasLocation) {
            addDetailRow(detailsGrid, 3, "📍 Latitude :", String.format(java.util.Locale.US, "%.6f", project.getLatitude()));
            addDetailRow(detailsGrid, 4, "📍 Longitude :", String.format(java.util.Locale.US, "%.6f", project.getLongitude()));
        } else {
            Label noLoc = new Label("📍 Localisation non définie");
            noLoc.setStyle("-fx-text-fill: #848A86; -fx-font-size: 12px; -fx-font-style: italic;");
            detailsGrid.add(noLoc, 0, 3, 2, 1);
        }

        Button closeBtn = new Button("✖  Fermer");
        closeBtn.setStyle("-fx-background-color: #076A39; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-padding: 10 28; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13px;");
        closeBtn.setOnAction(e -> detailStage.close());
        HBox btnRow = new HBox(closeBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(10, 0, 0, 0));

        infoPanel.getChildren().addAll(headerBox, statusRow, new Separator(), detailsGrid, btnRow);

        javafx.scene.web.WebView mapView = null;
        if (hasLocation) {
            mapView = new javafx.scene.web.WebView();
            mapView.setPrefWidth(480); mapView.setPrefHeight(500); mapView.setMinWidth(480);
            final javafx.scene.web.WebEngine engine = mapView.getEngine();
            engine.setJavaScriptEnabled(true);
            java.net.URL mapUrl = getClass().getResource("/mapbox_picker.html");
            if (mapUrl != null) {
                final double lat = project.getLatitude(), lng = project.getLongitude();
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

        HBox root;
        if (mapView != null) {
            root = new HBox(mapView, infoPanel);
            detailStage.setWidth(920); detailStage.setHeight(520);
        } else {
            root = new HBox(infoPanel);
            detailStage.setWidth(450); detailStage.setHeight(420);
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

    // ============================================================================
    // NAVIGATION
    // ============================================================================

    @FXML
    void goToRessources(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Ressourceproject agriculteur.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Mes Ressources");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible de charger la page Ressources: " + e.getMessage());
        }
    }

    // ============================================================================
    // MAP — ALL PROJECTS
    // ============================================================================

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
        mapStage.setTitle("🗺 Carte de Vos Projets");
        mapStage.setWidth(1100); mapStage.setHeight(720);

        javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
        javafx.scene.web.WebEngine engine = webView.getEngine();
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
    // PDF EXPORT
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

        document.add(new Paragraph("Mes Projets Agricoles")
                .setFontSize(20).setBold().setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Généré le: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFontSize(10).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n"));

        float[] columnWidths = {1, 3, 2, 2, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        com.itextpdf.kernel.colors.Color headerColor = new DeviceRgb(7, 106, 57);
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
    // AGROMONITORING
    // ============================================================================

    private static final String AGRO_API_KEY = "15ec96f6a29c1d2c59280c84a585bb66";

    private void handleAgroAdvice(projectagricole project) {
        if (project.getLatitude() == null || project.getLongitude() == null) {
            showAlert(Alert.AlertType.WARNING, "Localisation manquante",
                    "Ce projet n'a pas de coordonnées GPS.");
            return;
        }

        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setTitle("Chargement...");
        Label loadingLabel = new Label("🌐 Récupération des données Agromonitoring...");
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-padding: 30 40;");
        javafx.scene.control.ProgressIndicator spinner = new javafx.scene.control.ProgressIndicator();
        VBox loadingBox = new VBox(15, spinner, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER); loadingBox.setPadding(new Insets(30));
        loadingStage.setScene(new Scene(loadingBox, 340, 160));
        loadingStage.show();

        double lat = project.getLatitude(), lng = project.getLongitude();

        Thread thread = new Thread(() -> {
            try {
                String weatherJson = httpGet("http://api.agromonitoring.com/agro/1.0/weather?lat=" + lat + "&lon=" + lng + "&appid=" + AGRO_API_KEY);
                String soilJson    = httpGet("http://api.agromonitoring.com/agro/1.0/soil?polyid=&lat=" + lat + "&lon=" + lng + "&appid=" + AGRO_API_KEY);
                String uvJson      = httpGet("http://api.agromonitoring.com/agro/1.0/uvi?lat=" + lat + "&lon=" + lng + "&appid=" + AGRO_API_KEY);

                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    showAdviceDialog(project, weatherJson, soilJson, uvJson);
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    showAlert(Alert.AlertType.ERROR, "Erreur API",
                            "Impossible de récupérer les données.\n\n" + ex.getMessage());
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
        conn.setConnectTimeout(8000); conn.setReadTimeout(8000);
        conn.setRequestProperty("Accept", "application/json");
        int code = conn.getResponseCode();
        java.io.InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
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
        } catch (Exception e) { return "—"; }
    }

    private String parseNestedJson(String json, String parent, String key) {
        if (json == null) return "—";
        try {
            int parentIdx = json.indexOf("\"" + parent + "\"");
            if (parentIdx < 0) return "—";
            int braceOpen = json.indexOf("{", parentIdx);
            if (braceOpen < 0) return "—";
            int braceClose = json.indexOf("}", braceOpen);
            return parseJson(json.substring(braceOpen, braceClose + 1), key);
        } catch (Exception e) { return "—"; }
    }

    private String kelvinToCelsius(String kelvinStr) {
        try { return String.format("%.1f°C", Double.parseDouble(kelvinStr) - 273.15); }
        catch (Exception e) { return kelvinStr; }
    }

    private void showAdviceDialog(projectagricole project,
                                  String weatherJson, String soilJson, String uvJson) {
        String tempK      = parseNestedJson(weatherJson, "main", "temp");
        String humidity   = parseNestedJson(weatherJson, "main", "humidity");
        String windSpeed  = parseNestedJson(weatherJson, "wind", "speed");
        String pressure   = parseNestedJson(weatherJson, "main", "pressure");
        String cloudiness = parseNestedJson(weatherJson, "clouds", "all");
        String weatherDesc= parseNestedJson(weatherJson, "weather", "description");
        String cityName   = parseJson(weatherJson, "name");
        String tempC      = kelvinToCelsius(tempK);
        String soilMoisture = parseJson(soilJson, "moisture");
        String soilTempC  = kelvinToCelsius(parseJson(soilJson, "t0"));
        String uvIndex    = parseJson(uvJson, "value");
        if ("—".equals(uvIndex)) uvIndex = parseJson(uvJson, "uvi");

        String advice = generateAgriculturalAdvice(project, tempK, humidity, windSpeed, soilMoisture, uvIndex, cloudiness);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("🌱 Conseils — " + project.getNomproject());
        stage.setWidth(720); stage.setHeight(700);

        VBox content = new VBox(18);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: #F8FFF9;");

        VBox headerBox = new VBox(4);
        headerBox.setStyle("-fx-background-color: linear-gradient(to right,#076A39,#089647);" +
                "-fx-padding: 18 24; -fx-background-radius: 12;");
        Label hTitle = new Label("🌱 Conseils Agricoles — " + project.getNomproject());
        hTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label hSub = new Label("📍 " + String.format("%.4f, %.4f", project.getLatitude(), project.getLongitude())
                + (cityName.equals("—") ? "" : "   🏙 " + cityName));
        hSub.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.85);");
        headerBox.getChildren().addAll(hTitle, hSub);

        VBox weatherCard = buildInfoCard("🌦 Météo Actuelle", new String[][]{
                {"🌡 Température", tempC}, {"💧 Humidité", humidity + "%"},
                {"💨 Vent", windSpeed + " m/s"}, {"🌫 Pression", pressure + " hPa"},
                {"☁ Nébulosité", cloudiness + "%"}, {"📝 Conditions", weatherDesc}
        }, "#E8F5E9", "#2E7D32");

        VBox soilCard = buildInfoCard("🌍 Données du Sol", new String[][]{
                {"💧 Humidité sol", soilMoisture.equals("—") ? "—" : String.format("%.3f m³/m³", safeDouble(soilMoisture))},
                {"🌡 Temp. surface", soilTempC}
        }, "#FFF8E1", "#F57F17");

        VBox uvCard = buildInfoCard("☀ Rayonnement UV", new String[][]{
                {"🔆 Indice UV", uvIndex}, {"⚠ Niveau", uvLevel(uvIndex)}
        }, "#E3F2FD", "#1565C0");

        VBox adviceCard = new VBox(10);
        adviceCard.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 10;" +
                "-fx-border-color: #076A39; -fx-border-width: 2; -fx-border-radius: 10;");
        Label adviceTitle = new Label("🤖 Recommandations Agricoles");
        adviceTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #076A39;");
        Label adviceText = new Label(advice);
        adviceText.setWrapText(true);
        adviceText.setStyle("-fx-font-size: 13px; -fx-text-fill: #2C3E50; -fx-line-spacing: 3px;");
        adviceCard.getChildren().addAll(adviceTitle, adviceText);

        Button closeBtn = new Button("✖  Fermer");
        closeBtn.setStyle("-fx-background-color: #076A39; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13px;");
        closeBtn.setOnAction(e -> stage.close());
        HBox btnRow = new HBox(closeBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        content.getChildren().addAll(headerBox, weatherCard, soilCard, uvCard, adviceCard, btnRow);
        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F8FFF9; -fx-border-width: 0;");
        stage.setScene(new Scene(scroll));
        stage.show();
    }

    private VBox buildInfoCard(String title, String[][] rows, String bgColor, String accentColor) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 14 18;" +
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
            if (uv < 3) return "Faible — Travail normal";
            if (uv < 6) return "Modéré — Protection recommandée";
            if (uv < 8) return "Élevé — Éviter 11h-16h";
            if (uv < 11) return "Très élevé — Protection obligatoire";
            return "Extrême — Éviter l'exposition";
        } catch (Exception e) { return "—"; }
    }

    private String generateAgriculturalAdvice(projectagricole project,
                                              String tempK, String humidity, String windSpeed,
                                              String soilMoisture, String uvIndex, String cloudiness) {
        StringBuilder advice = new StringBuilder();
        double tempC = 0, hum = 0, wind = 0, soil = 0, uv = 0, cloud = 0;
        try { tempC = Double.parseDouble(tempK) - 273.15; } catch (Exception ignored) {}
        try { hum   = Double.parseDouble(humidity);       } catch (Exception ignored) {}
        try { wind  = Double.parseDouble(windSpeed);      } catch (Exception ignored) {}
        try { soil  = Double.parseDouble(soilMoisture);   } catch (Exception ignored) {}
        try { uv    = Double.parseDouble(uvIndex);        } catch (Exception ignored) {}
        try { cloud = Double.parseDouble(cloudiness);     } catch (Exception ignored) {}

        if (tempC < 5) advice.append("❄ Température très froide (").append(String.format("%.1f", tempC))
                .append("°C) — Risque de gel. Protégez les cultures fragiles.\n\n");
        else if (tempC < 15) advice.append("🌤 Température fraîche (").append(String.format("%.1f", tempC))
                .append("°C) — Conditions favorables pour blé, orge et légumes d'hiver.\n\n");
        else if (tempC <= 30) advice.append("☀ Température optimale (").append(String.format("%.1f", tempC))
                .append("°C) — Idéal pour la croissance active des cultures.\n\n");
        else advice.append("🌡 Chaleur élevée (").append(String.format("%.1f", tempC))
                    .append("°C) — Augmentez l'irrigation. Préférez les travaux tôt le matin.\n\n");

        if (hum < 30) advice.append("💧 Humidité très basse (").append((int)hum)
                .append("%) — Irrigation urgente recommandée.\n\n");
        else if (hum < 60) advice.append("💧 Humidité modérée (").append((int)hum)
                .append("%) — Surveillez le sol. Irrigation légère possible.\n\n");
        else advice.append("💧 Humidité suffisante (").append((int)hum)
                    .append("%) — Risque fongique si prolongé. Assurez un bon drainage.\n\n");

        if (soil > 0) {
            if (soil < 0.2) advice.append("🌍 Sol sec (").append(String.format("%.3f", soil))
                    .append(" m³/m³) — Irrigation immédiate nécessaire.\n\n");
            else if (soil < 0.4) advice.append("🌍 Humidité sol correcte (").append(String.format("%.3f", soil))
                    .append(" m³/m³) — Conditions favorables.\n\n");
            else advice.append("🌍 Sol très humide (").append(String.format("%.3f", soil))
                        .append(" m³/m³) — Réduisez l'irrigation.\n\n");
        }

        if (wind > 10) advice.append("💨 Vent fort (").append(String.format("%.1f", wind))
                .append(" m/s) — Évitez les traitements phytosanitaires.\n\n");
        if (uv >= 6) advice.append("☀ UV élevé (").append(String.format("%.1f", uv))
                .append(") — Travaillez tôt le matin. Protection obligatoire.\n\n");
        if (project.getSurface() > 50) advice.append("📏 Grande surface (")
                .append(String.format("%.0f", project.getSurface()))
                .append(" Ha) — Envisagez l'utilisation de drones ou matériel mécanisé.\n\n");
        if (cloud < 30) advice.append("🌾 Ciel dégagé — Conditions idéales pour observation par drone.\n");

        if (advice.length() == 0) advice.append("✅ Les conditions sont dans des plages normales.\n" +
                "Continuez le suivi régulier et maintenez votre calendrier d'irrigation habituel.");

        return advice.toString().trim();
    }

    // ============================================================================
    // HELPERS
    // ============================================================================

    private String getProjectIcon(String statut) {
        switch (statut.toLowerCase()) {
            case "accepte": return "✅";
            case "refuse":  return "❌";
            case "en cours": return "⏳";
            default: return "📋";
        }
    }

    private String capitalizeStatus(String statut) {
        switch (statut.toLowerCase()) {
            case "accepte": return "Accepté";
            case "refuse":  return "Refusé";
            case "en cours": return "En cours";
            default: return statut;
        }
    }

    private String getStatusBadgeClass(String statut) {
        switch (statut.toLowerCase()) {
            case "accepte": return "status-accepted";
            case "refuse":  return "status-refused";
            default:        return "status-progress";
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ============================================================================
    // 🌿 ASSISTANT PLANTE MALADE — GEMINI AI VISION
    // ============================================================================

    // ── Groq API key — can be overridden at runtime via the 🔑 button in the chat ──
    // Groq supports vision via llama-4 and llava models — free tier, very fast.
    private static String GROQ_API_KEY = "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";

    private void openPlantDiseaseChat(projectagricole project) {
        Stage chatStage = new Stage();
        chatStage.initModality(Modality.APPLICATION_MODAL);
        chatStage.setTitle("🌿 Assistant Plante Malade — " + project.getNomproject());
        chatStage.setWidth(680);
        chatStage.setHeight(750);
        chatStage.setResizable(false);

        // ── Root ──────────────────────────────────────────────────────────────
        VBox root = new VBox();
        root.setStyle("-fx-background-color: #F0F7F0;");

        // ── Header ────────────────────────────────────────────────────────────
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
        Label headerSub = new Label("Projet : " + project.getNomproject() + "  •  IA Gemini Vision");
        headerSub.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.80);");
        headerText.getChildren().addAll(headerTitle, headerSub);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        // 🔑 Change API key button
        Button btnChangeKey = new Button("🔑");
        btnChangeKey.setStyle(
                "-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white;" +
                        "-fx-font-size: 16px; -fx-padding: 6 10; -fx-background-radius: 8;" +
                        "-fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.35);" +
                        "-fx-border-width: 1; -fx-border-radius: 8;");
        btnChangeKey.setTooltip(new Tooltip("Changer la clé API Groq (si quota épuisé)"));
        btnChangeKey.setOnAction(ev -> {
            TextInputDialog dlg = new TextInputDialog(GROQ_API_KEY);
            dlg.setTitle("Clé API Groq");
            dlg.setHeaderText("🔑 Nouvelle clé API Groq");
            dlg.setContentText(
                    "Obtenez une clé gratuite sur : console.groq.com/keys\n" +
                            "Collez-la ici :");
            dlg.getEditor().setPrefWidth(420);
            dlg.showAndWait().ifPresent(newKey -> {
                String trimmed = newKey.trim();
                if (!trimmed.isEmpty()) {
                    GROQ_API_KEY = trimmed;
                    showAlert(Alert.AlertType.INFORMATION, "Clé mise à jour",
                            "✅ Nouvelle clé API Groq activée.\nVous pouvez maintenant analyser vos plantes.");
                }
            });
        });

        header.getChildren().addAll(headerIcon, headerText, btnChangeKey);

        // ── Chat scroll area ───────────────────────────────────────────────────
        VBox chatBox = new VBox(14);
        chatBox.setPadding(new Insets(18, 18, 8, 18));
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(chatBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        scrollPane.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Welcome bubble
        addChatBubble(chatBox, "🌿 Bonjour ! Je suis votre assistant spécialisé en maladies des plantes.\n\n" +
                "📸 Envoyez-moi une photo de votre plante malade et je vous fournirai :\n" +
                "  • Le nom exact de la plante\n  • Le diagnostic de la maladie\n" +
                "  • Un traitement agricole adapté\n\nCliquez sur 📎 pour sélectionner votre photo.", false);

        // ── Bottom panel ───────────────────────────────────────────────────────
        VBox bottomPanel = new VBox(10);
        bottomPanel.setPadding(new Insets(12, 16, 16, 16));
        bottomPanel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #D0E8D0; -fx-border-width: 1 0 0 0;");

        // Image preview
        Label[] imageLabelHolder = { null };
        File[] selectedFileHolder = { null };

        HBox previewBox = new HBox(10);
        previewBox.setAlignment(Pos.CENTER_LEFT);
        previewBox.setVisible(false);
        previewBox.setManaged(false);
        javafx.scene.image.ImageView imagePreview = new javafx.scene.image.ImageView();
        imagePreview.setFitWidth(80); imagePreview.setFitHeight(80);
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

        // ── Events ────────────────────────────────────────────────────────────
        btnAttach.setOnAction(ev -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Sélectionner une photo de plante");
            fc.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));
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

            // Show user bubble with image name
            addChatBubble(chatBox, "📸 " + imgFile.getName(), true);
            scrollToBottom(scrollPane, chatBox);

            // Disable buttons during analysis
            btnSend.setDisable(true);
            btnAttach.setDisable(true);
            statusLabel.setText("⏳ Analyse en cours par Gemini AI…");
            statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #E1B323; -fx-font-weight: bold;");

            // Typing indicator
            Label typingLabel = new Label("🌿 Assistant analyse votre plante…  ⣾");
            typingLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #076A39; -fx-font-style: italic;");
            chatBox.getChildren().add(typingLabel);
            scrollToBottom(scrollPane, chatBox);

            // Call Gemini in background thread
            Thread bgThread = new Thread(() -> {
                String response = callGeminiVision(imgFile, project.getNomproject());
                javafx.application.Platform.runLater(() -> {
                    chatBox.getChildren().remove(typingLabel);
                    addChatBubble(chatBox, response, false);
                    scrollToBottom(scrollPane, chatBox);
                    // Reset
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

    /** Add a styled chat bubble (left = AI, right = user) */
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

    private void scrollToBottom(javafx.scene.control.ScrollPane sp, VBox chatBox) {
        javafx.application.Platform.runLater(() -> {
            chatBox.layout();
            sp.layout();
            sp.setVvalue(1.0);
        });
    }

    /**
     * Groq vision models tried in order (fallback on 429/503).
     * All support image input via the OpenAI-compatible chat completions API.
     */
    private static final String[] GROQ_VISION_MODELS = {
            "meta-llama/llama-4-scout-17b-16e-instruct",   // best vision, fast
            "meta-llama/llama-4-maverick-17b-128e-instruct", // alt vision
            "llava-v1.5-7b-4096-preview"                   // legacy fallback
    };

    /** Call Groq Vision API with automatic model fallback. */
    private String callGeminiVision(File imageFile, String projectName) {
        try {
            // Read and base64-encode the image
            byte[] imageBytes = java.nio.file.Files.readAllBytes(imageFile.toPath());
            String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);

            // Detect MIME type
            String fileName = imageFile.getName().toLowerCase();
            String mimeType = "image/jpeg";
            if (fileName.endsWith(".png"))   mimeType = "image/png";
            else if (fileName.endsWith(".gif"))  mimeType = "image/gif";
            else if (fileName.endsWith(".webp")) mimeType = "image/webp";
            else if (fileName.endsWith(".bmp"))  mimeType = "image/bmp";

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

            // Groq uses OpenAI-compatible chat completions with image_url content
            String jsonBody = "{"
                    + "\"model\":\"MODEL_PLACEHOLDER\","
                    + "\"messages\":[{"
                    +   "\"role\":\"user\","
                    +   "\"content\":["
                    +     "{\"type\":\"text\",\"text\":\"" + escapeJson(prompt) + "\"},"
                    +     "{\"type\":\"image_url\",\"image_url\":{"
                    +       "\"url\":\"data:" + mimeType + ";base64," + base64Image + "\""
                    +     "}}"
                    +   "]"
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

                    // Read response (success or error stream)
                    java.io.InputStream is;
                    try { is = conn.getInputStream(); }
                    catch (Exception e) { is = conn.getErrorStream(); }

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
                        System.out.println("Groq model " + model + " quota/unavailable, trying next...");
                        continue;
                    }

                    if (status >= 200 && status < 300) {
                        // Parse: choices[0].message.content
                        // JSON: {"choices":[{"message":{"content":"..."}}]}
                        int idx = responseJson.indexOf("\"content\":");
                        if (idx >= 0) {
                            int start = responseJson.indexOf("\"", idx + 10) + 1;
                            int end = start;
                            while (end < responseJson.length()) {
                                char c = responseJson.charAt(end);
                                if (c == '\\') { end += 2; continue; }
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
                        // Real error — show it
                        return "❌ Erreur API Groq (HTTP " + status + ")\n\n"
                                + responseJson.substring(0, Math.min(500, responseJson.length()));
                    }

                } catch (Exception modelEx) {
                    lastError = "Erreur réseau [" + model + "] : " + modelEx.getMessage();
                    System.err.println("Groq model " + model + " exception: " + modelEx.getMessage());
                }
            }

            // All models exhausted
            return "❌ Quota épuisé sur tous les modèles Groq.\n\n" +
                    "💡 Solution rapide :\n" +
                    "  1. Allez sur console.groq.com/keys\n" +
                    "  2. Cliquez « Create API Key »\n" +
                    "  3. Copiez la clé et cliquez 🔑 en haut à droite\n" +
                    "  4. Collez la nouvelle clé et réessayez\n\n" +
                    "⏱ Ou attendez quelques minutes — le quota se réinitialise automatiquement.\n\n" +
                    "Dernière erreur : " + lastError;

        } catch (Exception ex) {
            return "❌ Erreur lors de la lecture de l'image :\n" + ex.getMessage();
        }
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ============================================================================
    // 📰 ACTUALITÉS AGRICOLES — NewsAPI
    // ============================================================================

    private static final String NEWS_API_KEY = "6263389c98ca4dbbb7ac1eec33269db2";

    /**
     * Opens a beautiful modal window showing the latest agricultural news
     * fetched from NewsAPI.org.
     * Called by the "📰 Actualités" button in the FXML.
     */
    @FXML
    private void openNewsDialog() {
        Stage newsStage = new Stage();
        newsStage.setTitle("📰 Actualités Agricoles");
        newsStage.initModality(Modality.APPLICATION_MODAL);
        newsStage.setResizable(true);
        newsStage.setMinWidth(740);
        newsStage.setMinHeight(580);

        // ── Root ─────────────────────────────────────────────────────────────
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F4F6F8;");

        // ── HEADER ───────────────────────────────────────────────────────────
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

        // ── CATEGORY FILTER BAR ──────────────────────────────────────────────
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setStyle(
                "-fx-background-color: white; -fx-padding: 10 24;" +
                        "-fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;"
        );
        Label filterLbl = new Label("Thème :");
        filterLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-font-weight: bold;");

        String[][] topics = {
                {"Agriculture", "agriculture",  "ferme"},
                {"Cultures",    "culture",      "recolte"},
                {"Elevage",     "elevage",      "betail"},
                {"Meteo",       "meteo",        "climat"},
                {"Marche",      "prix",         "economie"},
                {"Innovation",  "innovation",   "technologie"}
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
            // store "primary|fallback" in userData
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

        // ── NEWS ListView ─────────────────────────────────────────────────────
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

        // ── STATUS BAR ────────────────────────────────────────────────────────
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

        // ── Fetch logic ───────────────────────────────────────────────────────
        // userData format: "primary|fallback"
        final String[] currentQuery = {"agriculture|ferme"};

        Consumer<String> fetchNews = queryData -> {
            currentQuery[0] = queryData;
            String[] parts = queryData.split("\\|", 2);
            String primaryQ  = parts[0].trim();
            String fallbackQ = parts.length > 1 ? parts[1].trim() : "";

            centerBox.getChildren().setAll(loadingBox);
            statusLbl.setText("Chargement en cours...");
            statusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

            Task<List<NewsArticle>> task = new Task<List<NewsArticle>>() {
                @Override
                protected List<NewsArticle> call() throws Exception {
                    // 1st attempt: primary keyword
                    List<NewsArticle> result = fetchAgricultureNews(primaryQ);
                    // 2nd attempt: fallback keyword
                    if (result.isEmpty() && !fallbackQ.isEmpty()) {
                        result = fetchAgricultureNews(fallbackQ);
                    }
                    // 3rd attempt: add "agricole" as context
                    if (result.isEmpty()) {
                        result = fetchAgricultureNews("agricole " + primaryQ);
                    }
                    return result;
                }
            };
            task.setOnSucceeded(evt -> {
                List<NewsArticle> articles = task.getValue();
                if (articles.isEmpty()) {
                    Label noResultsLbl = new Label("Aucun article trouve pour ce theme. Essayez la recherche manuelle.");
                    noResultsLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #888; -fx-padding: 60 0;");
                    noResultsLbl.setMaxWidth(Double.MAX_VALUE);
                    noResultsLbl.setAlignment(Pos.CENTER);
                    noResultsLbl.setWrapText(true);
                    centerBox.getChildren().setAll(noResultsLbl);
                } else {
                    newsListView.getItems().setAll(articles);
                    centerBox.getChildren().setAll(newsListView);
                    VBox.setVgrow(newsListView, Priority.ALWAYS);
                }
                statusLbl.setText(articles.size() + " articles charges - NewsAPI.org");
                statusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
            });
            task.setOnFailed(evt -> {
                Label errLbl = new Label("Impossible de charger les actualites. Verifiez votre connexion.");
                errLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #c0392b; -fx-padding: 60 0;");
                errLbl.setMaxWidth(Double.MAX_VALUE);
                errLbl.setAlignment(Pos.CENTER);
                errLbl.setWrapText(true);
                centerBox.getChildren().setAll(errLbl);
                statusLbl.setText("Erreur de chargement");
                statusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #c0392b;");
            });

            Thread t = new Thread(task);
            t.setDaemon(true);
            t.start();
        };

        // Wire events
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

        // Initial load
        fetchNews.accept("agriculture|ferme");
    }

    /**
     * Calls NewsAPI /v2/everything with the given query.
     * Tries French first, then English as fallback.
     */
    private List<NewsArticle> fetchAgricultureNews(String query) throws Exception {
        return doNewsApiCall(query);
    }

    private List<NewsArticle> doNewsApiCall(String query) throws Exception {
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

    /** Lightweight JSON parser — no external library needed. */
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
                else if (c == '}') { depth--; if (depth == 0) { objEnd = i; break; } }
            }
            if (objEnd <= objStart) break;
            String obj = json.substring(objStart, objEnd + 1);
            NewsArticle a = new NewsArticle();
            a.title       = extractNewsJsonString(obj, "title");
            a.description = extractNewsJsonString(obj, "description");
            a.url         = extractNewsJsonString(obj, "url");
            a.source      = extractNewsJsonString(obj, "name");
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

    // ── Data model ────────────────────────────────────────────────────────────
    private static class NewsArticle {
        String title;
        String description;
        String url;
        String source;
        String publishedAt;
    }

    // ── Custom ListCell — renders each article as a beautiful card ────────────
    private class NewsListCell extends ListCell<NewsArticle> {

        private final VBox card        = new VBox(7);
        private final HBox topRow      = new HBox(8);
        private final Label sourceTag  = new Label();
        private final Label dateLbl    = new Label();
        private final Label titleLbl   = new Label();
        private final Label descLbl    = new Label();
        private final Hyperlink linkLbl = new Hyperlink("🔗 Lire l'article complet");

        private static final String CARD_NORMAL =
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #E8F5E9; -fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-padding: 14 16;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 6, 0, 0, 2);";

        private static final String CARD_HOVER =
                "-fx-background-color: #F9FFF9;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #A5D6A7; -fx-border-width: 1.5;" +
                        "-fx-border-radius: 12;" +
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

            titleLbl.setStyle(
                    "-fx-font-size: 14px; -fx-font-weight: bold;" +
                            "-fx-text-fill: #1B2D1B; -fx-wrap-text: true;"
            );
            titleLbl.setWrapText(true);
            titleLbl.setMaxWidth(Double.MAX_VALUE);

            descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555; -fx-wrap-text: true;");
            descLbl.setWrapText(true);
            descLbl.setMaxWidth(Double.MAX_VALUE);

            linkLbl.setStyle(
                    "-fx-text-fill: #1976D2; -fx-font-size: 11px;" +
                            "-fx-font-weight: bold; -fx-cursor: hand; -fx-border-width: 0;"
            );

            Separator sep = new Separator();
            sep.setStyle("-fx-background-color: #E8F5E9;");
            card.getChildren().addAll(topRow, titleLbl, descLbl, sep, linkLbl);

            setStyle("-fx-background-color: transparent; -fx-padding: 5 0;");
            setGraphic(null);
        }

        @Override
        protected void updateItem(NewsArticle article, boolean empty) {
            super.updateItem(article, empty);
            if (empty || article == null) { setGraphic(null); return; }

            String src = (article.source != null && !article.source.isEmpty())
                    ? article.source : "Source inconnue";
            sourceTag.setText("📰 " + src);

            String date = (article.publishedAt != null && article.publishedAt.length() >= 10)
                    ? article.publishedAt.substring(0, 10) : "";
            dateLbl.setText(date);

            titleLbl.setText(article.title != null ? article.title : "(Sans titre)");
            descLbl.setText((article.description != null && !article.description.isEmpty())
                    ? article.description : "Aucune description disponible.");

            linkLbl.setOnAction(e -> {
                if (article.url != null && !article.url.isEmpty()) {
                    openArticleReader(article);
                }
            });

            setGraphic(card);
        }
    }

    // ── In-app article reader — fetches & renders full content immediately ─────
    private void openArticleReader(NewsArticle article) {
        Stage readerStage = new Stage();
        readerStage.setTitle(article.title != null ? article.title : "Article");
        readerStage.initModality(Modality.APPLICATION_MODAL);
        readerStage.setResizable(true);
        readerStage.setWidth(1050);
        readerStage.setHeight(800);

        // ── Root ──────────────────────────────────────────────────────────────
        BorderPane readerRoot = new BorderPane();
        readerRoot.setStyle("-fx-background-color: #1B2D1B;");

        // ── HEADER BAR ────────────────────────────────────────────────────────
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setStyle(
                "-fx-background-color: linear-gradient(to right, #1B5E20, #2E7D32);" +
                        "-fx-padding: 14 22;"
        );

        Button backBtn = new Button("  Retour");
        backBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 16;" +
                        "-fx-background-radius: 20; -fx-cursor: hand;" +
                        "-fx-border-color: rgba(255,255,255,0.35); -fx-border-radius: 20; -fx-border-width: 1;"
        );
        backBtn.setOnAction(e -> readerStage.close());

        Label articleTitleLbl = new Label(article.title != null ? article.title : "Article");
        articleTitleLbl.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;"
        );
        articleTitleLbl.setMaxWidth(680);
        articleTitleLbl.setWrapText(true);

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        String srcText  = (article.source != null && !article.source.isEmpty()) ? article.source : "";
        String dateText = (article.publishedAt != null && article.publishedAt.length() >= 10)
                ? article.publishedAt.substring(0, 10) : "";
        Label sourceDateLbl = new Label(
                (srcText.isEmpty() ? "" : srcText) +
                        ((!srcText.isEmpty() && !dateText.isEmpty()) ? "  |  " : "") +
                        dateText
        );
        sourceDateLbl.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.70);"
        );

        // Zoom controls in header
        String zBtnStyle =
                "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white;" +
                        "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 4 10;" +
                        "-fx-background-radius: 6; -fx-cursor: hand;";
        Button btnZoomOut = new Button("A-");
        Button btnZoomIn  = new Button("A+");
        btnZoomOut.setStyle(zBtnStyle);
        btnZoomIn.setStyle(zBtnStyle);

        titleBar.getChildren().addAll(backBtn, articleTitleLbl, titleSpacer, sourceDateLbl, btnZoomOut, btnZoomIn);
        readerRoot.setTop(titleBar);

        // ── WebView — shows "Chargement..." immediately ────────────────────────
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.setJavaScriptEnabled(true);

        // Show skeleton loading page right away (no emoji — safe chars only)
        engine.loadContent(buildSkeletonPage());

        readerRoot.setCenter(webView);

        // ── BOTTOM STATUS BAR ─────────────────────────────────────────────────
        HBox bottomBar = new HBox(12);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setStyle(
                "-fx-background-color: #162816;" +
                        "-fx-padding: 8 20;" +
                        "-fx-border-color: #0D1F0D; -fx-border-width: 1 0 0 0;"
        );
        ProgressIndicator fetchSpinner = new ProgressIndicator();
        fetchSpinner.setPrefSize(16, 16);
        fetchSpinner.setStyle("-fx-accent: #66BB6A;");
        Label statusLbl = new Label("Chargement de l'article en cours...");
        statusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #81C784;");
        Region bSpacer = new Region();
        HBox.setHgrow(bSpacer, Priority.ALWAYS);
        Label urlLbl = new Label(article.url != null ? article.url : "");
        urlLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(165,214,167,0.45);");
        urlLbl.setMaxWidth(400);

        Button btnClose = new Button("X  Fermer");
        btnClose.setStyle(
                "-fx-background-color: rgba(211,47,47,0.28); -fx-text-fill: #EF9A9A;" +
                        "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 5 16;" +
                        "-fx-background-radius: 14; -fx-cursor: hand;"
        );
        btnClose.setOnAction(e -> readerStage.close());

        bottomBar.getChildren().addAll(fetchSpinner, statusLbl, bSpacer, urlLbl, btnClose);
        readerRoot.setBottom(bottomBar);

        readerStage.setScene(new Scene(readerRoot));
        readerStage.show();

        // ── Zoom ─────────────────────────────────────────────────────────────
        final double[] zoom = {1.0};
        btnZoomOut.setOnAction(e -> { zoom[0] = Math.max(zoom[0] - 0.15, 0.5); webView.setZoom(zoom[0]); });
        btnZoomIn.setOnAction(e -> { zoom[0] = Math.min(zoom[0] + 0.15, 2.5); webView.setZoom(zoom[0]); });

        // ── Fetch article content in background thread ─────────────────────────
        Task<String> fetchTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return fetchAndExtractArticle(article);
            }
        };

        fetchTask.setOnSucceeded(evt -> {
            String html = fetchTask.getValue();
            engine.loadContent(html, "text/html");
            fetchSpinner.setVisible(false);
            statusLbl.setText("Article charge - " + srcText + (dateText.isEmpty() ? "" : "  |  " + dateText));
            statusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #A5D6A7;");
        });
        fetchTask.setOnFailed(evt -> {
            engine.loadContent(buildErrorPage(article), "text/html");
            fetchSpinner.setVisible(false);
            statusLbl.setText("Impossible de charger l'article complet.");
            statusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #EF9A9A;");
        });

        Thread t = new Thread(fetchTask);
        t.setDaemon(true);
        t.start();
    }

    /**
     * Fetches the raw HTML of the article URL, then extracts meaningful
     * text paragraphs and wraps them in a clean, beautifully styled HTML page.
     * Falls back to the description if fetching fails.
     */
    private String fetchAndExtractArticle(NewsArticle article) {
        String title   = article.title       != null ? article.title       : "";
        String desc    = article.description != null ? article.description : "";
        String srcName = article.source      != null ? article.source      : "";
        String date    = (article.publishedAt != null && article.publishedAt.length() >= 10)
                ? article.publishedAt.substring(0, 10) : "";
        String url     = article.url         != null ? article.url         : "";

        // ── Try to fetch the actual page ──────────────────────────────────────
        String rawHtml = "";
        try {
            java.net.URL urlObj = new java.net.URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(10000);
            // Mimic a real browser to avoid 403s
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/120.0.0.0 Safari/537.36");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,*/*;q=0.9");
            conn.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.9,en;q=0.8");
            int status = conn.getResponseCode();
            if (status >= 200 && status < 400) {
                java.io.InputStream is = conn.getInputStream();
                // Read with detected charset or UTF-8 fallback
                String contentType = conn.getContentType();
                String charset = "UTF-8";
                if (contentType != null && contentType.contains("charset=")) {
                    charset = contentType.substring(contentType.indexOf("charset=") + 8).trim();
                    if (charset.contains(";")) charset = charset.substring(0, charset.indexOf(";")).trim();
                }
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new java.io.InputStreamReader(is, charset))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line).append("\n");
                }
                rawHtml = sb.toString();
            }
            conn.disconnect();
        } catch (Exception e) {
            System.out.println("Fetch failed: " + e.getMessage());
        }

        // ── Extract paragraphs from raw HTML ─────────────────────────────────
        String bodyContent = extractArticleBody(rawHtml, desc);

        // ── Build clean reader HTML ───────────────────────────────────────────
        return buildReaderHtml(title, srcName, date, url, bodyContent);
    }

    /**
     * Pulls readable <p> paragraphs from raw HTML, strips tags, returns
     * them as clean HTML paragraphs. Falls back to description if nothing found.
     */
    /**
     * Decodes common HTML entities to their actual characters so the
     * reader displays clean readable French text instead of &#8217; etc.
     */
    private String decodeHtmlEntities(String text) {
        if (text == null) return "";
        // Named entities most common in French press
        text = text.replace("&nbsp;",   " ")
                .replace("&amp;",    "&")
                .replace("&lt;",     "<")
                .replace("&gt;",     ">")
                .replace("&quot;",   "\"")
                .replace("&apos;",   "'")
                .replace("&laquo;",  "\u00ab")
                .replace("&raquo;",  "\u00bb")
                .replace("&agrave;", "\u00e0")
                .replace("&eacute;", "\u00e9")
                .replace("&egrave;", "\u00e8")
                .replace("&ecirc;",  "\u00ea")
                .replace("&euml;",   "\u00eb")
                .replace("&icirc;",  "\u00ee")
                .replace("&iuml;",   "\u00ef")
                .replace("&ocirc;",  "\u00f4")
                .replace("&ugrave;", "\u00f9")
                .replace("&ucirc;",  "\u00fb")
                .replace("&uuml;",   "\u00fc")
                .replace("&ccedil;", "\u00e7")
                .replace("&oelig;",  "\u0153")
                .replace("&OElig;",  "\u0152")
                .replace("&hellip;", "\u2026")
                .replace("&mdash;",  "\u2014")
                .replace("&ndash;",  "\u2013")
                .replace("&rsquo;",  "\u2019")
                .replace("&lsquo;",  "\u2018")
                .replace("&rdquo;",  "\u201d")
                .replace("&ldquo;",  "\u201c")
                .replace("&euro;",   "\u20ac")
                .replace("&copy;",   "\u00a9")
                .replace("&reg;",    "\u00ae")
                .replace("&times;",  "\u00d7")
                .replace("&divide;", "\u00f7")
                .replace("&deg;",    "\u00b0")
                .replace("&sect;",   "\u00a7")
                .replace("&para;",   "\u00b6")
                .replace("&middot;", "\u00b7")
                .replace("&bull;",   "\u2022")
                .replace("&prime;",  "\u2032")
                .replace("&Prime;",  "\u2033");

        // Numeric decimal entities: &#8217; &#171; etc.
        java.util.regex.Matcher dm = java.util.regex.Pattern
                .compile("&#(\\d{1,6});").matcher(text);
        StringBuffer sbDec = new StringBuffer();
        while (dm.find()) {
            int cp = Integer.parseInt(dm.group(1));
            dm.appendReplacement(sbDec,
                    java.util.regex.Matcher.quoteReplacement(
                            new String(Character.toChars(cp))));
        }
        dm.appendTail(sbDec);
        text = sbDec.toString();

        // Numeric hex entities: &#x2019; etc.
        java.util.regex.Matcher hm = java.util.regex.Pattern
                .compile("&#[xX]([0-9a-fA-F]{1,6});").matcher(text);
        StringBuffer sbHex = new StringBuffer();
        while (hm.find()) {
            int cp = Integer.parseInt(hm.group(1), 16);
            hm.appendReplacement(sbHex,
                    java.util.regex.Matcher.quoteReplacement(
                            new String(Character.toChars(cp))));
        }
        hm.appendTail(sbHex);
        return sbHex.toString();
    }

    private String extractArticleBody(String rawHtml, String fallbackDesc) {
        if (rawHtml == null || rawHtml.isEmpty()) {
            return "<p>" + escapeHtml(decodeHtmlEntities(fallbackDesc)) + "</p>";
        }

        // Remove noisy blocks
        String cleaned = rawHtml
                .replaceAll("(?is)<script[^>]*>.*?</script>", " ")
                .replaceAll("(?is)<style[^>]*>.*?</style>",   " ")
                .replaceAll("(?is)<nav[^>]*>.*?</nav>",       " ")
                .replaceAll("(?is)<header[^>]*>.*?</header>", " ")
                .replaceAll("(?is)<footer[^>]*>.*?</footer>", " ")
                .replaceAll("(?is)<aside[^>]*>.*?</aside>",   " ")
                .replaceAll("(?is)<figure[^>]*>.*?</figure>", " ")
                .replaceAll("(?is)<!--.*?-->",                 " ");

        // Extract <p> paragraphs
        StringBuilder body = new StringBuilder();
        java.util.regex.Pattern pPattern =
                java.util.regex.Pattern.compile("(?is)<p(?:\\s[^>]*)?>(.+?)</p>");
        java.util.regex.Matcher m = pPattern.matcher(cleaned);
        int count = 0;
        while (m.find() && count < 120) {
            // Strip inner tags, decode entities, clean whitespace
            String pText = m.group(1)
                    .replaceAll("<[^>]+>", " ")   // remove inner HTML tags
                    .replaceAll("\\s{2,}", " ")  // collapse whitespace
                    .trim();
            pText = decodeHtmlEntities(pText); // decode &nbsp; &#8217; etc.
            pText = pText.replaceAll("\\s{2,}", " ").trim(); // re-collapse after decode
            if (pText.length() > 60) {
                body.append("<p>").append(escapeHtml(pText)).append("</p>\n");
                count++;
            }
        }

        // Fallback: try <article> block
        if (count < 3) {
            java.util.regex.Pattern articlePattern =
                    java.util.regex.Pattern.compile("(?is)<article[^>]*>(.*?)</article>");
            java.util.regex.Matcher am = articlePattern.matcher(cleaned);
            if (am.find()) {
                String articleText = am.group(1)
                        .replaceAll("<[^>]+>", " ")
                        .replaceAll("\\s{2,}", " ").trim();
                articleText = decodeHtmlEntities(articleText)
                        .replaceAll("\\s{2,}", " ").trim();
                if (articleText.length() > 100) {
                    body = new StringBuilder("<p>")
                            .append(escapeHtml(articleText)).append("</p>");
                }
            }
        }

        if (body.length() < 50) {
            return "<p>" + escapeHtml(decodeHtmlEntities(fallbackDesc)) + "</p>" +
                    "<p style='color:#999;font-style:italic;font-size:13px;'>" +
                    "Le contenu complet de cet article n'a pas pu etre extrait automatiquement. " +
                    "Il est possible que le site protege son contenu.</p>";
        }

        return body.toString();
    }

    /** Builds the final beautifully-styled reader HTML page. */
    private String buildReaderHtml(String title, String source, String date,
                                   String url, String bodyHtml) {
        return "<!DOCTYPE html>\n<html>\n<head>\n" +
                "<meta charset='UTF-8'>\n" +
                "<meta name='viewport' content='width=device-width, initial-scale=1'>\n" +
                "<style>\n" +
                "  * { box-sizing: border-box; margin: 0; padding: 0; }\n" +
                "  html { scroll-behavior: smooth; }\n" +
                "  body {\n" +
                "    font-family: Georgia, 'Times New Roman', serif;\n" +
                "    background: #FAFAF8;\n" +
                "    color: #1A1A1A;\n" +
                "    line-height: 1.85;\n" +
                "    font-size: 17px;\n" +
                "  }\n" +
                "  /* ── Hero header ── */\n" +
                "  .hero {\n" +
                "    background: linear-gradient(160deg, #1B5E20 0%, #2E7D32 55%, #388E3C 100%);\n" +
                "    padding: 48px 60px 40px;\n" +
                "    color: white;\n" +
                "  }\n" +
                "  .meta {\n" +
                "    display: flex; align-items: center; gap: 14px;\n" +
                "    margin-bottom: 20px;\n" +
                "  }\n" +
                "  .source-badge {\n" +
                "    background: rgba(255,255,255,0.22);\n" +
                "    color: white;\n" +
                "    padding: 4px 14px;\n" +
                "    border-radius: 20px;\n" +
                "    font-size: 12px;\n" +
                "    font-weight: 700;\n" +
                "    font-family: Arial, sans-serif;\n" +
                "    letter-spacing: 0.5px;\n" +
                "  }\n" +
                "  .date-badge {\n" +
                "    font-family: Arial, sans-serif;\n" +
                "    font-size: 12px;\n" +
                "    color: rgba(255,255,255,0.72);\n" +
                "  }\n" +
                "  .hero h1 {\n" +
                "    font-size: 30px;\n" +
                "    font-weight: 700;\n" +
                "    line-height: 1.3;\n" +
                "    color: white;\n" +
                "    font-family: Arial, sans-serif;\n" +
                "  }\n" +
                "  /* ── Divider ── */\n" +
                "  .divider {\n" +
                "    height: 4px;\n" +
                "    background: linear-gradient(to right, #4CAF50, #81C784, transparent);\n" +
                "  }\n" +
                "  /* ── Article body ── */\n" +
                "  .article-wrap {\n" +
                "    max-width: 780px;\n" +
                "    margin: 0 auto;\n" +
                "    padding: 44px 40px 60px;\n" +
                "  }\n" +
                "  .article-wrap p {\n" +
                "    margin-bottom: 20px;\n" +
                "    color: #222;\n" +
                "    text-align: justify;\n" +
                "    hyphens: auto;\n" +
                "  }\n" +
                "  .article-wrap p:first-child {\n" +
                "    font-size: 19px;\n" +
                "    font-weight: 600;\n" +
                "    color: #1B5E20;\n" +
                "    border-left: 5px solid #4CAF50;\n" +
                "    padding-left: 18px;\n" +
                "    margin-bottom: 28px;\n" +
                "    line-height: 1.7;\n" +
                "  }\n" +
                "  /* ── Footer ── */\n" +
                "  .article-footer {\n" +
                "    border-top: 2px solid #E8F5E9;\n" +
                "    margin-top: 40px;\n" +
                "    padding-top: 20px;\n" +
                "  }\n" +
                "  .article-footer p {\n" +
                "    font-family: Arial, sans-serif;\n" +
                "    font-size: 12px;\n" +
                "    color: #999;\n" +
                "    word-break: break-all;\n" +
                "  }\n" +
                "  .source-label {\n" +
                "    display: inline-block;\n" +
                "    background: #E8F5E9;\n" +
                "    color: #2E7D32;\n" +
                "    padding: 2px 10px;\n" +
                "    border-radius: 10px;\n" +
                "    font-size: 12px;\n" +
                "    font-weight: bold;\n" +
                "    font-family: Arial, sans-serif;\n" +
                "    margin-bottom: 6px;\n" +
                "  }\n" +
                "</style>\n" +
                "</head>\n<body>\n" +
                "<div class='hero'>\n" +
                "  <div class='meta'>\n" +
                "    <span class='source-badge'>" + escapeHtml(source) + "</span>\n" +
                "    <span class='date-badge'>" + escapeHtml(date) + "</span>\n" +
                "  </div>\n" +
                "  <h1>" + escapeHtml(title) + "</h1>\n" +
                "</div>\n" +
                "<div class='divider'></div>\n" +
                "<div class='article-wrap'>\n" +
                bodyHtml + "\n" +
                "  <div class='article-footer'>\n" +
                "    <p class='source-label'>Source</p>\n" +
                "    <p>" + escapeHtml(url) + "</p>\n" +
                "  </div>\n" +
                "</div>\n" +
                "</body>\n</html>";
    }

    /** Shown instantly while content is being fetched in background. */
    private String buildSkeletonPage() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>" +
                "* { box-sizing: border-box; margin: 0; padding: 0; }" +
                "body { font-family: Arial, sans-serif; background: #FAFAF8;" +
                "       display: flex; flex-direction: column; align-items: center;" +
                "       justify-content: center; height: 100vh; gap: 20px; color: #2E7D32; }" +
                ".spinner { width: 48px; height: 48px; border: 5px solid #C8E6C9;" +
                "           border-top-color: #2E7D32; border-radius: 50%;" +
                "           animation: spin 0.9s linear infinite; }" +
                "@keyframes spin { to { transform: rotate(360deg); } }" +
                "h3 { font-size: 16px; color: #2E7D32; }" +
                "p { font-size: 12px; color: #999; }" +
                "</style></head><body>" +
                "<div class='spinner'></div>" +
                "<h3>Chargement de l'article...</h3>" +
                "<p>Recuperation du contenu en cours</p>" +
                "</body></html>";
    }

    /** Shown when fetch failed completely. */
    private String buildErrorPage(NewsArticle article) {
        String title = article.title != null ? escapeHtml(article.title) : "";
        String desc  = article.description != null ? escapeHtml(article.description) : "";
        String src   = article.source != null ? escapeHtml(article.source) : "";
        String date  = (article.publishedAt != null && article.publishedAt.length() >= 10)
                ? article.publishedAt.substring(0, 10) : "";
        return buildReaderHtml(
                article.title != null ? article.title : "",
                src, date,
                article.url != null ? article.url : "",
                "<p>" + desc + "</p>" +
                        "<p style='color:#999;font-size:13px;font-style:italic;" +
                        "font-family:Arial,sans-serif;margin-top:24px;'>" +
                        "Le contenu complet n'a pas pu etre recupere (site protege ou hors ligne)." +
                        "</p>"
        );
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}