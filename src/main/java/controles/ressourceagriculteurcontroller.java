package controles;

import entities.projectagricole;
import entities.ressourceproject;
import services.projectagricoleCRUD;
import services.ressourceprojectCRUD;
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
import java.util.List;
import java.util.ResourceBundle;

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

public class ressourceagriculteurcontroller implements Initializable {

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
    @FXML private FlowPane ressourcesContainer;

    // ============================================================================
    // ADD/MODIFY DIALOG FIELDS
    // ============================================================================
    @FXML private ComboBox<String> cbIdProjectDialog;
    private java.util.Map<String, Integer> projectNameToIdMap = new java.util.HashMap<>();
    @FXML private TextField tfNomRessourceDialog;
    @FXML private ComboBox<String> cbTypeRessourceDialog;
    @FXML private TextField tfQuantiteDialog;
    @FXML private TextField tfCoutDialog;
    @FXML private TextField tfFournisseurDialog;
    @FXML private ComboBox<String> cbStatutRessourceDialog;
    @FXML private DatePicker dpDateAjoutDialog;
    @FXML private Button btnSave;

    // ============================================================================
    // SHARED FIELDS
    // ============================================================================
    // Only ressources belonging to this agriculteur's projects
    private List<ressourceproject> allRessources = new ArrayList<>();
    private ressourceproject selectedRessource = null;
    private ressourceproject currentRessource = null;
    private final ressourceprojectCRUD rService = new ressourceprojectCRUD();
    private final projectagricoleCRUD pService = new projectagricoleCRUD();

    private ressourceagriculteurcontroller mainController;
    private String dialogMode = "list";

    // ============================================================================
    // INITIALIZATION
    // ============================================================================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Main list view setup
        if (ressourcesContainer != null) {
            refreshDataFromDB();
        }

        // Dialog setup
        if (cbTypeRessourceDialog != null && cbStatutRessourceDialog != null) {
            setupDialog();
        }
    }

    private void setupDialog() {
        if (cbTypeRessourceDialog != null) {
            cbTypeRessourceDialog.getItems().addAll("equipement", "materiaux", "service");
        }
        if (cbStatutRessourceDialog != null) {
            cbStatutRessourceDialog.getItems().addAll("prevu", "achete");
            cbStatutRessourceDialog.setValue("prevu");
        }

        // Load only the projects belonging to this agriculteur
        try {
            if (cbIdProjectDialog != null) {
                projectNameToIdMap.clear();
                List<projectagricole> projects = pService.afficher();
                // ── Filter by agriculteur ─────────────────────────────────
                // Uncomment the filter below when session management is ready:
                // projects = projects.stream()
                //     .filter(p -> p.getIdagriculteur() == currentAgriculteurId)
                //     .collect(java.util.stream.Collectors.toList());
                // ─────────────────────────────────────────────────────────
                for (projectagricole p : projects) {
                    projectNameToIdMap.put(p.getNomproject(), p.getIdproject());
                    cbIdProjectDialog.getItems().add(p.getNomproject());
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les projets.");
        }
    }

    public void setRessource(ressourceproject ressource) {
        this.currentRessource = ressource;
        this.dialogMode = "modify";
        populateFields();
    }

    public void setMainController(ressourceagriculteurcontroller mainController) {
        this.mainController = mainController;
    }

    private void populateFields() {
        if (currentRessource != null && cbIdProjectDialog != null) {
            String projectName = projectNameToIdMap.entrySet().stream()
                    .filter(e -> e.getValue() == currentRessource.getIdproject())
                    .map(java.util.Map.Entry::getKey)
                    .findFirst().orElse(null);
            cbIdProjectDialog.setValue(projectName);
            tfNomRessourceDialog.setText(currentRessource.getNomressource());
            cbTypeRessourceDialog.setValue(currentRessource.getTyperessource());
            tfQuantiteDialog.setText(String.valueOf(currentRessource.getQuantite()));
            tfCoutDialog.setText(currentRessource.getCout().toString());
            tfFournisseurDialog.setText(currentRessource.getFournisseur());
            cbStatutRessourceDialog.setValue(currentRessource.getStatut());
            dpDateAjoutDialog.setValue(currentRessource.getDateajout().toLocalDate());
        }
    }

    // ============================================================================
    // ADD/MODIFY DIALOG HANDLERS
    // ============================================================================

    @FXML
    void handleSave(ActionEvent event) {
        if (!validateDialogInputs()) return;

        try {
            String selectedProjectName = cbIdProjectDialog.getValue();
            Integer resolvedProjectId = projectNameToIdMap.get(selectedProjectName);
            if (resolvedProjectId == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Projet sélectionné introuvable!");
                return;
            }

            if ("modify".equals(dialogMode)) {
                currentRessource.setIdproject(resolvedProjectId);
                currentRessource.setNomressource(tfNomRessourceDialog.getText().trim());
                currentRessource.setTyperessource(cbTypeRessourceDialog.getValue());
                currentRessource.setQuantite(Integer.parseInt(tfQuantiteDialog.getText().trim()));
                currentRessource.setCout(new BigDecimal(tfCoutDialog.getText().trim()));
                currentRessource.setFournisseur(tfFournisseurDialog.getText().trim());
                currentRessource.setStatut(cbStatutRessourceDialog.getValue());
                currentRessource.setDateajout(Date.valueOf(dpDateAjoutDialog.getValue()));

                rService.modifier(currentRessource);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource modifiée avec succès!");
            } else {
                ressourceproject r = new ressourceproject(
                        tfNomRessourceDialog.getText().trim(),
                        cbTypeRessourceDialog.getValue(),
                        Integer.parseInt(tfQuantiteDialog.getText().trim()),
                        new BigDecimal(tfCoutDialog.getText().trim()),
                        tfFournisseurDialog.getText().trim(),
                        cbStatutRessourceDialog.getValue(),
                        Date.valueOf(dpDateAjoutDialog.getValue()),
                        resolvedProjectId
                );
                rService.ajouter(r);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource ajoutée avec succès!");
            }

            if (mainController != null) mainController.refreshDataFromDB();
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

    private void closeDialogWindow() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    private boolean validateDialogInputs() {
        if (tfNomRessourceDialog.getText().trim().isEmpty() ||
                tfQuantiteDialog.getText().trim().isEmpty() ||
                tfCoutDialog.getText().trim().isEmpty() ||
                tfFournisseurDialog.getText().trim().isEmpty() ||
                cbIdProjectDialog.getValue() == null ||
                cbTypeRessourceDialog.getValue() == null ||
                cbStatutRessourceDialog.getValue() == null ||
                dpDateAjoutDialog.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir tous les champs!");
            return false;
        }

        if (tfNomRessourceDialog.getText().trim().length() < 2) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Le nom de la ressource doit contenir au moins 2 caractères!");
            return false;
        }

        if (tfFournisseurDialog.getText().trim().length() < 2) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Le nom du fournisseur doit contenir au moins 2 caractères!");
            return false;
        }

        if ("add".equals(dialogMode)) {
            if (!dpDateAjoutDialog.getValue().isEqual(java.time.LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Erreur de date",
                        "La date d'ajout doit être la date d'aujourd'hui uniquement!");
                return false;
            }
        } else if ("modify".equals(dialogMode)) {
            if (dpDateAjoutDialog.getValue().isBefore(java.time.LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                        "La date d'ajout ne peut pas être dans le passé!");
                return false;
            }
        }

        try {
            int quantite = Integer.parseInt(tfQuantiteDialog.getText().trim());
            if (quantite <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", "La quantité doit être un nombre positif!");
                return false;
            }
            BigDecimal cout = new BigDecimal(tfCoutDialog.getText().trim());
            if (cout.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le coût doit être un nombre positif!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format",
                    "La quantité et le coût doivent être des nombres valides!");
            return false;
        }

        return true;
    }

    // ============================================================================
    // DATA LOADING — filtered by agriculteur's projects
    // ============================================================================

    public void refreshDataFromDB() {
        try {
            List<ressourceproject> all = rService.afficher();
            if (all == null) all = new ArrayList<>();

            // ── Filter: only keep ressources linked to this agriculteur's projects ──
            // Get the list of project IDs owned by this agriculteur
            // Uncomment the block below when session management is ready:
            //
            // List<projectagricole> myProjects = pService.afficher().stream()
            //     .filter(p -> p.getIdagriculteur() == currentAgriculteurId)
            //     .collect(java.util.stream.Collectors.toList());
            // java.util.Set<Integer> myProjectIds = myProjects.stream()
            //     .map(projectagricole::getIdproject)
            //     .collect(java.util.stream.Collectors.toSet());
            // all = all.stream()
            //     .filter(r -> myProjectIds.contains(r.getIdproject()))
            //     .collect(java.util.stream.Collectors.toList());
            // ─────────────────────────────────────────────────────────────────────

            allRessources = all;
            updateCardsDisplay();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Erreur lors du chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCardsDisplay() {
        if (ressourcesContainer == null) return;
        ressourcesContainer.getChildren().clear();
        for (ressourceproject r : allRessources) {
            ressourcesContainer.getChildren().add(createResourceCard(r));
        }
    }

    // ============================================================================
    // CARD BUILDER
    // ============================================================================

    private VBox createResourceCard(ressourceproject resource) {
        VBox card = new VBox(12);
        card.getStyleClass().add("project-card");
        card.setPrefWidth(280); card.setMaxWidth(280);
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
        Label id = new Label("ID: " + resource.getIdressource());
        id.setStyle("-fx-font-size: 10px; -fx-text-fill: #848A86;");
        titleBox.getChildren().addAll(title, id);
        header.getChildren().addAll(icon, titleBox);

        // Badges
        Label typeBadge = new Label(capitalizeType(resource.getTyperessource()));
        typeBadge.getStyleClass().add(getTypeBadgeClass(resource.getTyperessource()));
        Label statusBadge = new Label(capitalizeStatus(resource.getStatut()));
        statusBadge.getStyleClass().add(getStatusBadgeClass(resource.getStatut()));
        HBox badges = new HBox(8);
        badges.getChildren().addAll(typeBadge, statusBadge);

        // Details
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10); detailsGrid.setVgap(8);
        addCardDetailRow(detailsGrid, 0, "📦 Quantité:", String.valueOf(resource.getQuantite()));
        addCardDetailRow(detailsGrid, 1, "💰 Coût:", String.format("%.2f DT", resource.getCout()));
        addCardDetailRow(detailsGrid, 2, "🏭 Fournisseur:", resource.getFournisseur());
        addCardDetailRow(detailsGrid, 3, "🔗 Projet:", "#" + resource.getIdproject());

        // Action buttons
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(5, 0, 0, 0));

        Button btnView = new Button("👁");
        btnView.getStyleClass().add("btn-view");
        btnView.setOnAction(e -> showResourceDetails(resource));
        btnView.setTooltip(new Tooltip("Voir les détails"));
        btnView.setMinWidth(40); btnView.setPrefWidth(40);

        Button btnEdit = new Button("✏");
        btnEdit.getStyleClass().add("btn-edit");
        btnEdit.setOnAction(e -> openModifyDialog(resource));
        btnEdit.setTooltip(new Tooltip("Modifier"));
        btnEdit.setMinWidth(40); btnEdit.setPrefWidth(40);

        Button btnDelete = new Button("🗑");
        btnDelete.getStyleClass().add("btn-delete");
        btnDelete.setOnAction(e -> handleDelete(resource));
        btnDelete.setTooltip(new Tooltip("Supprimer"));
        btnDelete.setMinWidth(40); btnDelete.setPrefWidth(40);

        actions.getChildren().addAll(btnView, btnEdit, btnDelete);
        HBox.setHgrow(actions, Priority.ALWAYS);

        card.getChildren().addAll(header, badges, new Separator(), detailsGrid, actions);
        return card;
    }

    private void addCardDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #848A86;");
        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2D6A4F;");
        lblValue.setWrapText(true);
        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }

    // ============================================================================
    // DELETE
    // ============================================================================

    private void handleDelete(ressourceproject resource) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer la ressource: " + resource.getNomressource());
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette ressource?\nCette action est irréversible.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    rService.supprimer(resource.getIdressource());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource supprimée avec succès!");
                    refreshDataFromDB();
                    selectedRessource = null;
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
    void openAddResourceForm(ActionEvent event) {
        openAddDialog();
    }

    private void openAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ressourceagricoleagriculteuradd.fxml"));
            Parent root = loader.load();

            ressourceagriculteurcontroller controller = loader.getController();
            controller.dialogMode = "add";
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nouvelle Ressource");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage());
        }
    }

    private void openModifyDialog(ressourceproject resource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ressourceagricoleagriculteurmodify.fxml"));
            Parent root = loader.load();

            ressourceagriculteurcontroller controller = loader.getController();
            controller.dialogMode = "modify";
            controller.setRessource(resource);
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier Ressource — " + resource.getNomressource());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }

    // ============================================================================
    // RESOURCE DETAILS
    // ============================================================================

    private void showResourceDetails(ressourceproject resource) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la Ressource");
        dialog.setHeaderText(null);

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white;");

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label iconLarge = new Label(getTypeIcon(resource.getTyperessource()));
        iconLarge.setStyle("-fx-font-size: 48px;");
        VBox titleBox = new VBox(5);
        Label titleLabel = new Label(resource.getNomressource());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2D6A4F;");
        Label idLabel = new Label("Ressource #" + resource.getIdressource());
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #848A86;");
        titleBox.getChildren().addAll(titleLabel, idLabel);
        headerBox.getChildren().addAll(iconLarge, titleBox);

        HBox badgesRow = new HBox(10);
        badgesRow.setAlignment(Pos.CENTER_LEFT);
        Label typeBadge = new Label(capitalizeType(resource.getTyperessource()));
        typeBadge.getStyleClass().add(getTypeBadgeClass(resource.getTyperessource()));
        Label statusBadge = new Label(capitalizeStatus(resource.getStatut()));
        statusBadge.getStyleClass().add(getStatusBadgeClass(resource.getStatut()));
        badgesRow.getChildren().addAll(typeBadge, statusBadge);

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(20); detailsGrid.setVgap(15);
        detailsGrid.setPadding(new Insets(10, 0, 10, 0));
        addDetailRow(detailsGrid, 0, "📦 Quantité:", String.valueOf(resource.getQuantite()));
        addDetailRow(detailsGrid, 1, "💰 Coût unitaire:", String.format("%.2f DT", resource.getCout()));
        addDetailRow(detailsGrid, 2, "🏭 Fournisseur:", resource.getFournisseur());
        addDetailRow(detailsGrid, 3, "🔗 Projet associé:", "Projet #" + resource.getIdproject());
        addDetailRow(detailsGrid, 4, "📅 Date d'ajout:", resource.getDateajout().toString());

        BigDecimal totalCost = resource.getCout().multiply(new BigDecimal(resource.getQuantite()));
        VBox costBox = new VBox(8);
        Label costTitle = new Label("💵 Coût Total");
        costTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D6A4F;");
        Label costValue = new Label(String.format("%.2f DT", totalCost));
        costValue.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #40916C;");
        costBox.getChildren().addAll(costTitle, costValue);

        content.getChildren().addAll(headerBox, badgesRow, detailsGrid, new Separator(), costBox);
        dialog.getDialogPane().setContent(content);

        ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);
        Button closeBtn = (Button) dialog.getDialogPane().lookupButton(closeButton);
        closeBtn.setStyle("-fx-background-color: #2D6A4F; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-padding: 10 30; -fx-background-radius: 6;");

        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #848A86;");
        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D6A4F;");
        grid.add(lblLabel, 0, row);
        grid.add(lblValue, 1, row);
    }

    // ============================================================================
    // NAVIGATION
    // ============================================================================

    @FXML
    void goToProjects(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Projectagricole agriculteur.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Mes Projets Agricoles");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible de charger la page Projets: " + e.getMessage());
        }
    }

    // ============================================================================
    // PDF EXPORT
    // ============================================================================

    @FXML
    void exportToPDF(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.setInitialFileName("Mes_Ressources_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = fileChooser.showSaveDialog(ressourcesContainer.getScene().getWindow());
            if (file != null) {
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                Color headerColor = new DeviceRgb(45, 106, 79);

                document.add(new Paragraph("MES RESSOURCES AGRICOLES")
                        .setFontSize(20).setBold().setFontColor(headerColor)
                        .setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));
                document.add(new Paragraph(
                        "Généré le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) +
                                "\nTotal de ressources: " + allRessources.size())
                        .setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

                Table table = new Table(UnitValue.createPercentArray(new float[]{8, 20, 12, 10, 13, 18, 12, 7}))
                        .useAllAvailableWidth();

                String[] headers = {"ID", "Nom", "Type", "Quantité", "Coût (DT)", "Fournisseur", "Projet", "Statut"};
                for (String header : headers) {
                    table.addHeaderCell(new Cell()
                            .add(new Paragraph(header).setBold().setFontSize(9))
                            .setBackgroundColor(headerColor).setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER).setPadding(6));
                }

                for (ressourceproject r : allRessources) {
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(r.getIdressource()))));
                    table.addCell(new Cell().add(new Paragraph(r.getNomressource())));
                    table.addCell(new Cell().add(new Paragraph(capitalizeType(r.getTyperessource()))));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(r.getQuantite()))));
                    table.addCell(new Cell().add(new Paragraph(r.getCout() != null ? String.format("%.2f", r.getCout()) : "0.00")));
                    table.addCell(new Cell().add(new Paragraph(r.getFournisseur())));
                    table.addCell(new Cell().add(new Paragraph("#" + r.getIdproject())));
                    table.addCell(new Cell().add(new Paragraph(capitalizeStatus(r.getStatut()))));
                }

                document.add(table);
                document.close();

                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Le fichier PDF a été généré avec succès!\n\nEmplacement: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la génération du PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================================================
    // HELPERS
    // ============================================================================

    private String getTypeIcon(String type) {
        switch (type.toLowerCase()) {
            case "equipement": return "🚜";
            case "materiaux":  return "⚙";
            case "service":    return "🔧";
            default:           return "📦";
        }
    }

    private String capitalizeType(String type) {
        switch (type.toLowerCase()) {
            case "equipement": return "Équipement";
            case "materiaux":  return "Matériaux";
            case "service":    return "Service";
            default:           return type;
        }
    }

    private String capitalizeStatus(String statut) {
        switch (statut.toLowerCase()) {
            case "prevu":  return "Prévu";
            case "achete": return "Acheté";
            default:       return statut;
        }
    }

    private String getTypeBadgeClass(String type) {
        switch (type.toLowerCase()) {
            case "equipement": return "badge-equipment";
            case "materiaux":  return "badge-materials";
            case "service":    return "badge-service";
            default:           return "badge-default";
        }
    }

    private String getStatusBadgeClass(String statut) {
        switch (statut.toLowerCase()) {
            case "achete": return "status-accepted";
            case "prevu":  return "status-progress";
            default:       return "status-progress";
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