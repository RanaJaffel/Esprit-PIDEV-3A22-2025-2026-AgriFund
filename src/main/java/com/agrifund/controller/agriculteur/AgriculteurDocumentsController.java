package com.agrifund.controller.agriculteur;

import com.agrifund.Main;
import com.agrifund.services.DocumentService;
import com.agrifund.util.SessionManager;
import com.agrifund.util.FileManager;
import com.agrifund.entities.Document;
import com.agrifund.entities.Utilisateur;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AgriculteurDocumentsController {

    @FXML private VBox documentsContainer;
    @FXML private Label totalCount;
    @FXML private Label validatedCount;
    @FXML private Label pendingCount;
    @FXML private Label rejectedCount;

    @FXML private Button btnAll;
    @FXML private Button btnValidated;
    @FXML private Button btnPending;
    @FXML private Button btnRejected;

    private DocumentService documentService;
    private Utilisateur currentUser;
    private List<Document> allDocuments;
    private String currentFilter = "all";

    @FXML
    public void initialize() {
        try {
            documentService = new DocumentService();
            currentUser = SessionManager.getInstance().getUtilisateurConnecte();

            loadDocuments();

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadDocuments() throws SQLException {
        allDocuments = documentService.afficherParUtilisateur(currentUser.getId());
        updateStats();
        applyFilter();
    }

    private void updateStats() {
        totalCount.setText(String.valueOf(allDocuments.size()));

        long validated = allDocuments.stream().filter(d -> "valide".equals(d.getStatut())).count();
        long pending = allDocuments.stream().filter(d -> "en_attente".equals(d.getStatut())).count();
        long rejected = allDocuments.stream().filter(d -> "rejete".equals(d.getStatut())).count();

        validatedCount.setText(String.valueOf(validated));
        pendingCount.setText(String.valueOf(pending));
        rejectedCount.setText(String.valueOf(rejected));
    }

    private void applyFilter() {
        documentsContainer.getChildren().clear();

        List<Document> filtered = allDocuments.stream()
                .filter(d -> {
                    switch (currentFilter) {
                        case "validated": return "valide".equals(d.getStatut());
                        case "pending": return "en_attente".equals(d.getStatut());
                        case "rejected": return "rejete".equals(d.getStatut());
                        default: return true;
                    }
                })
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            VBox empty = new VBox(20);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(50));

            Label icon = new Label("📄");
            icon.setStyle("-fx-font-size: 60px;");

            Label text = new Label("Aucun document trouvé");
            text.setStyle("-fx-font-size: 18px; -fx-text-fill: #848A86;");

            Button addBtn = new Button("➕ Ajouter un document");
            addBtn.getStyleClass().addAll("btn", "btn-primary");
            addBtn.setOnAction(e -> showAddDocumentDialog());

            empty.getChildren().addAll(icon, text, addBtn);
            documentsContainer.getChildren().add(empty);
            return;
        }

        for (Document doc : filtered) {
            VBox card = createDocumentCard(doc);
            documentsContainer.getChildren().add(card);
        }
    }

    private VBox createDocumentCard(Document doc) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 20;");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Icône
        Label icon = new Label(getDocumentIcon(doc.getTypeDocument()));
        icon.setStyle("-fx-font-size: 40px;");

        // Info
        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(doc.getNom());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label typeLabel = new Label("Type: " + formatDocType(doc.getTypeDocument()));
        typeLabel.setStyle("-fx-text-fill: #089647;");

        String dateStr = doc.getDateUpload() != null ?
                doc.getDateUpload().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) : "";
        Label dateLabel = new Label("📅 Uploadé le: " + dateStr);
        dateLabel.setStyle("-fx-text-fill: #848A86; -fx-font-size: 12px;");

        info.getChildren().addAll(nameLabel, typeLabel, dateLabel);

        // Badge statut
        Label statusBadge = new Label();
        switch (doc.getStatut()) {
            case "valide":
                statusBadge.setText("✓ Validé");
                statusBadge.getStyleClass().addAll("badge", "badge-success");
                break;
            case "rejete":
                statusBadge.setText("✗ Rejeté");
                statusBadge.getStyleClass().addAll("badge", "badge-danger");
                break;
            default:
                statusBadge.setText("⏳ En attente");
                statusBadge.getStyleClass().addAll("badge", "badge-warning");
        }

        header.getChildren().addAll(icon, info, statusBadge);

        // Détails
        HBox details = new HBox(30);
        details.setStyle("-fx-padding: 10 0;");

        Label sizeLabel = new Label("📦 Taille: " + formatSize(doc.getTaille()));
        sizeLabel.setStyle("-fx-text-fill: #848A86;");

        Label pathLabel = new Label("📁 " + doc.getCheminFichier());
        pathLabel.setStyle("-fx-text-fill: #848A86; -fx-font-size: 11px;");
        pathLabel.setMaxWidth(400);

        details.getChildren().addAll(sizeLabel);

        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnView = new Button("👁️ Voir");
        btnView.getStyleClass().addAll("btn", "btn-secondary");
        btnView.setOnAction(e -> openDocument(doc));

        Button btnDelete = new Button("🗑️ Supprimer");
        btnDelete.getStyleClass().addAll("btn", "btn-danger");
        btnDelete.setOnAction(e -> deleteDocument(doc));

        actions.getChildren().addAll(btnView, btnDelete);

        // Message si rejeté
        if ("rejete".equals(doc.getStatut())) {
            HBox alertBox = new HBox(10);
            alertBox.setAlignment(Pos.CENTER_LEFT);
            alertBox.setStyle("-fx-padding: 10; -fx-background-color: rgba(220,53,69,0.1); -fx-background-radius: 8;");

            Label alertIcon = new Label("⚠️");
            Label alertText = new Label("Ce document a été rejeté. Veuillez en soumettre un nouveau.");
            alertText.setStyle("-fx-text-fill: #dc3545;");

            alertBox.getChildren().addAll(alertIcon, alertText);
            card.getChildren().addAll(header, details, pathLabel, alertBox, actions);
        } else {
            card.getChildren().addAll(header, details, pathLabel, actions);
        }

        return card;
    }

    private String getDocumentIcon(String type) {
        switch (type.toLowerCase()) {
            case "cni":
            case "passeport":
                return "🪪";
            case "certificat_bio":
                return "🌿";
            case "cadastre":
                return "🗺️";
            case "agrement":
                return "📜";
            default:
                return "📄";
        }
    }

    private String formatDocType(String type) {
        switch (type.toLowerCase()) {
            case "cni": return "Carte Nationale d'Identité";
            case "passeport": return "Passeport";
            case "certificat_bio": return "Certificat Biologique";
            case "cadastre": return "Cadastre";
            case "agrement": return "Agrément";
            default: return type;
        }
    }

    private String formatSize(int bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    @FXML
    public void showAddDocumentDialog() {
        Dialog<Document> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un document");
        dialog.setHeaderText("Télécharger un nouveau document");

        ButtonType uploadButtonType = new ButtonType("📤 Télécharger", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(uploadButtonType, ButtonType.CANCEL);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setPrefWidth(450);

        // Nom du document
        TextField nomField = new TextField();
        nomField.setPromptText("Nom du document");
        nomField.getStyleClass().add("form-control");

        // Type de document
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(
                "CNI", "Passeport", "Certificat Bio", "Cadastre", "Agrément", "Autre"
        ));
        typeCombo.setPromptText("Type de document");
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        // Fichier
        HBox fileBox = new HBox(10);
        fileBox.setAlignment(Pos.CENTER_LEFT);

        TextField fileField = new TextField();
        fileField.setPromptText("Aucun fichier sélectionné");
        fileField.setEditable(false);
        fileField.getStyleClass().add("form-control");
        HBox.setHgrow(fileField, Priority.ALWAYS);

        Button browseBtn = new Button("📁 Parcourir");
        browseBtn.getStyleClass().addAll("btn", "btn-secondary");

        final File[] selectedFile = {null};

        browseBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir un document");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.jpg", "*.jpeg", "*.png", "*.doc", "*.docx")
            );

            File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                selectedFile[0] = file;
                fileField.setText(file.getName());
            }
        });

        fileBox.getChildren().addAll(fileField, browseBtn);

        content.getChildren().addAll(
                new Label("Nom du document:"),
                nomField,
                new Label("Type de document:"),
                typeCombo,
                new Label("Fichier:"),
                fileBox
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == uploadButtonType) {
                if (nomField.getText().trim().isEmpty()) {
                    showAlert("Le nom du document est obligatoire", Alert.AlertType.WARNING);
                    return null;
                }
                if (typeCombo.getValue() == null) {
                    showAlert("Veuillez sélectionner un type de document", Alert.AlertType.WARNING);
                    return null;
                }
                if (selectedFile[0] == null) {
                    showAlert("Veuillez sélectionner un fichier", Alert.AlertType.WARNING);
                    return null;
                }

                try {
                    // Upload le fichier
                    String typeDoc = typeCombo.getValue().toLowerCase().replace(" ", "_");
                    String storedPath = FileManager.uploadDocument(
                            selectedFile[0].getAbsolutePath(),
                            currentUser.getId(),
                            typeDoc
                    );

                    if (storedPath != null) {
                        // Créer le document
                        Document doc = new Document(
                                currentUser.getId(),
                                nomField.getText().trim(),
                                typeDoc,
                                storedPath
                        );
                        doc.setTaille((int) selectedFile[0].length());

                        documentService.ajouter(doc);
                        loadDocuments();

                        showAlert("Document ajouté avec succès! Il sera vérifié par un administrateur.", Alert.AlertType.INFORMATION);
                    }

                } catch (Exception e) {
                    showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void openDocument(Document doc) {
        try {
            File file = new File(doc.getCheminFichier());
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                showAlert("Fichier introuvable: " + doc.getCheminFichier(), Alert.AlertType.WARNING);
            }
        } catch (IOException e) {
            showAlert("Impossible d'ouvrir le fichier: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteDocument(Document doc) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce document ?");
        confirm.setContentText(doc.getNom());

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                // Supprimer le fichier
                FileManager.supprimerFichier(doc.getCheminFichier());

                // Supprimer de la base
                documentService.supprimer(doc.getId());

                loadDocuments();
                showAlert("Document supprimé!", Alert.AlertType.INFORMATION);

            } catch (SQLException e) {
                showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void filterAll() {
        currentFilter = "all";
        updateFilterButtons();
        applyFilter();
    }

    @FXML
    public void filterValidated() {
        currentFilter = "validated";
        updateFilterButtons();
        applyFilter();
    }

    @FXML
    public void filterPending() {
        currentFilter = "pending";
        updateFilterButtons();
        applyFilter();
    }

    @FXML
    public void filterRejected() {
        currentFilter = "rejected";
        updateFilterButtons();
        applyFilter();
    }

    private void updateFilterButtons() {
        btnAll.setStyle(currentFilter.equals("all") ? "-fx-background-color: #089647; -fx-text-fill: white;" : "");
        btnValidated.setStyle(currentFilter.equals("validated") ? "-fx-background-color: #089647; -fx-text-fill: white;" : "");
        btnPending.setStyle(currentFilter.equals("pending") ? "-fx-background-color: #E1B323; -fx-text-fill: #060806;" : "");
        btnRejected.setStyle(currentFilter.equals("rejected") ? "-fx-background-color: #dc3545; -fx-text-fill: white;" : "");
    }

    @FXML
    public void refreshList() {
        try {
            loadDocuments();
        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
