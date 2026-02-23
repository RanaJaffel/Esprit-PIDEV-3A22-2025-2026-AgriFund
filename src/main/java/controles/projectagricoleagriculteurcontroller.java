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

        actions.getChildren().addAll(btnView, btnEdit, btnDelete);
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
}