package com.agrifund.controller.admin;

import com.agrifund.Main;
import com.agrifund.services.DocumentService;
import com.agrifund.services.UtilisateurService;
import com.agrifund.entities.Document;
import com.agrifund.entities.Utilisateur;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminDocumentsController {

    @FXML private VBox documentsList;
    @FXML private Label pendingCount;
    @FXML private Label validatedCount;
    @FXML private Label rejectedCount;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Button btnPending;
    @FXML private Button btnAll;

    private DocumentService documentService;
    private UtilisateurService utilisateurService;
    private List<Document> allDocuments;
    private String currentFilter = "pending";

    @FXML
    public void initialize() {
        try {
            documentService = new DocumentService();
            utilisateurService = new UtilisateurService();

            setupTypeFilter();
            loadDocuments();

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupTypeFilter() {
        typeFilter.setItems(FXCollections.observableArrayList(
                "Tous les types",
                "CNI",
                "Passeport",
                "Certificat Bio",
                "Cadastre",
                "Agrément",
                "KBIS",
                "Autre"
        ));
        typeFilter.getSelectionModel().selectFirst();
        typeFilter.setOnAction(e -> applyFilter());
    }

    private void loadDocuments() throws SQLException {
        // Charger tous les documents de tous les utilisateurs
        allDocuments = new ArrayList<>();

        List<Utilisateur> users = utilisateurService.afficherTous();
        for (Utilisateur user : users) {
            List<Document> userDocs = documentService.afficherParUtilisateur(user.getId());
            allDocuments.addAll(userDocs);
        }

        updateStats();
        applyFilter();
    }

    private void updateStats() {
        long pending = allDocuments.stream().filter(d -> "en_attente".equals(d.getStatut())).count();
        long validated = allDocuments.stream().filter(d -> "valide".equals(d.getStatut())).count();
        long rejected = allDocuments.stream().filter(d -> "rejete".equals(d.getStatut())).count();

        pendingCount.setText(String.valueOf(pending));
        validatedCount.setText(String.valueOf(validated));
        rejectedCount.setText(String.valueOf(rejected));
    }

    private void applyFilter() {
        documentsList.getChildren().clear();

        String selectedType = typeFilter.getValue();

        List<Document> filtered = allDocuments.stream()
                .filter(d -> {
                    if (currentFilter.equals("pending")) {
                        return "en_attente".equals(d.getStatut());
                    }
                    return true;
                })
                .filter(d -> {
                    if (selectedType == null || selectedType.equals("Tous les types")) {
                        return true;
                    }
                    return d.getTypeDocument().equalsIgnoreCase(selectedType.replace(" ", "_"));
                })
                .collect(Collectors.toList());

        for (Document doc : filtered) {
            try {
                VBox card = createDocumentCard(doc);
                documentsList.getChildren().add(card);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (filtered.isEmpty()) {
            Label empty = new Label("Aucun document trouvé");
            empty.setStyle("-fx-text-fill: #848A86; -fx-font-size: 16px; -fx-padding: 50;");
            documentsList.getChildren().add(empty);
        }
    }

    private VBox createDocumentCard(Document doc) throws SQLException {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 20;");

        // Récupérer l'utilisateur
        Utilisateur user = utilisateurService.rechercherParId(doc.getUtilisateurId());

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Icône selon le type
        Label icon = new Label(getDocumentIcon(doc.getTypeDocument()));
        icon.setStyle("-fx-font-size: 40px;");

        // Infos
        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(doc.getNom());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label typeLabel = new Label("Type: " + formatDocType(doc.getTypeDocument()));
        typeLabel.setStyle("-fx-text-fill: #089647;");

        Label userLabel = new Label("📤 Soumis par: " +
                (user != null ? user.getPrenom() + " " + user.getNom() : "Utilisateur #" + doc.getUtilisateurId()));
        userLabel.setStyle("-fx-text-fill: #848A86;");

        info.getChildren().addAll(nameLabel, typeLabel, userLabel);

        // Statut
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

        String dateStr = doc.getDateUpload() != null ?
                doc.getDateUpload().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A";
        Label dateLabel = new Label("📅 " + dateStr);

        Label sizeLabel = new Label("📦 " + formatSize(doc.getTaille()));

        Label pathLabel = new Label("📁 " + doc.getCheminFichier());
        pathLabel.setStyle("-fx-text-fill: #848A86; -fx-font-size: 11px;");

        details.getChildren().addAll(dateLabel, sizeLabel);

        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnView = new Button("👁️ Voir");
        btnView.getStyleClass().addAll("btn", "btn-secondary");
        btnView.setOnAction(e -> openDocument(doc));

        if ("en_attente".equals(doc.getStatut())) {
            Button btnValidate = new Button("✓ Valider");
            btnValidate.getStyleClass().addAll("btn", "btn-success");
            btnValidate.setOnAction(e -> validateDocument(doc, "valide"));

            Button btnReject = new Button("✗ Rejeter");
            btnReject.getStyleClass().addAll("btn", "btn-danger");
            btnReject.setOnAction(e -> validateDocument(doc, "rejete"));

            actions.getChildren().addAll(btnView, btnValidate, btnReject);
        } else {
            Button btnDelete = new Button("🗑️ Supprimer");
            btnDelete.getStyleClass().addAll("btn", "btn-danger");
            btnDelete.setOnAction(e -> deleteDocument(doc));

            actions.getChildren().addAll(btnView, btnDelete);
        }

        card.getChildren().addAll(header, details, pathLabel, actions);
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
            case "kbis":
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
            case "kbis": return "KBIS";
            default: return type;
        }
    }

    private String formatSize(int bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
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

    private void validateDocument(Document doc, String status) {
        String action = status.equals("valide") ? "valider" : "rejeter";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Voulez-vous " + action + " ce document ?");
        confirm.setContentText(doc.getNom());

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                documentService.validerDocument(doc.getId(), status);
                loadDocuments();

                String message = status.equals("valide") ?
                        "Document validé avec succès!" : "Document rejeté.";
                showAlert(message, Alert.AlertType.INFORMATION);

            } catch (SQLException e) {
                showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void deleteDocument(Document doc) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Voulez-vous supprimer ce document ?");
        confirm.setContentText("Cette action est irréversible.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                documentService.supprimer(doc.getId());
                loadDocuments();
                showAlert("Document supprimé!", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    public void filterPending() {
        currentFilter = "pending";
        btnPending.setStyle("-fx-background-color: #E1B323; -fx-text-fill: #060806;");
        btnAll.setStyle("");
        applyFilter();
    }

    @FXML
    public void filterAll() {
        currentFilter = "all";
        btnAll.setStyle("-fx-background-color: #089647; -fx-text-fill: white;");
        btnPending.setStyle("");
        applyFilter();
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
