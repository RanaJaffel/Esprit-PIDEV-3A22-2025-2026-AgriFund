package Controllers.banque;

import Controllers.MainApp;
import Services.BanqueService;
import Services.DocumentService;
import Services.MessagerieService;
import Services.AgriculteurService;
import Utils.SessionManager;
import entities.Banque;
import entities.Document;
import entities.Utilisateur;
import entities.Agriculteur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BanqueDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private ImageView topBarAvatar;
    @FXML private Label topBarUserName;
    @FXML private Label unreadBadge;

    @FXML private HBox verificationAlert;

    @FXML private Label documentsCount;
    @FXML private Label documentsStatus;
    @FXML private Label messagesCount;
    @FXML private Label agriculteurCount;

    @FXML private ImageView profilePhoto;
    @FXML private Label profileName;
    @FXML private Label profileCode;
    @FXML private Label profileEmail;
    @FXML private Label profileTel;
    @FXML private Label profileRepresentant;
    @FXML private Label profileSiege;
    @FXML private Label profileSiteWeb;

    @FXML private VBox recentDocumentsContainer;

    private BanqueService banqueService;
    private DocumentService documentService;
    private MessagerieService messagerieService;
    private AgriculteurService agriculteurService;
    private Utilisateur currentUser;
    private Banque currentBanque;

    @FXML
    public void initialize() {
        try {
            banqueService = new BanqueService();
            documentService = new DocumentService();
            messagerieService = new MessagerieService();
            agriculteurService = new AgriculteurService();

            currentUser = SessionManager.getInstance().getUtilisateurConnecte();
            currentBanque = banqueService.rechercherParUtilisateurId(currentUser.getId());

            loadUserInfo();
            loadStats();
            loadProfileSummary();
            loadRecentDocuments();
            checkVerificationStatus();

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadUserInfo() throws SQLException {
        welcomeLabel.setText("Bienvenue, " + currentUser.getNom() + " !");
        topBarUserName.setText(currentUser.getNom());

        // Avatar
        loadPhoto(topBarAvatar, currentUser.getPhoto(), 35);

        // Messages non lus
        int unread = messagerieService.compterMessagesNonLus(currentUser.getId());
        if (unread > 0) {
            unreadBadge.setText(String.valueOf(unread));
            unreadBadge.setVisible(true);
        }
    }

    private void loadStats() throws SQLException {
        // Documents
        List<Document> docs = documentService.afficherParUtilisateur(currentUser.getId());
        documentsCount.setText(String.valueOf(docs.size()));

        long validated = docs.stream().filter(d -> "valide".equals(d.getStatut())).count();
        documentsStatus.setText(validated + " validé(s)");

        // Messages
        int unread = messagerieService.compterMessagesNonLus(currentUser.getId());
        messagesCount.setText(String.valueOf(unread));

        // Agriculteurs
        List<Agriculteur> agriculteurs = agriculteurService.afficherTous();
        long verifiedAgri = agriculteurs.stream().filter(Agriculteur::isCompteVerifie).count();
        agriculteurCount.setText(String.valueOf(verifiedAgri));
    }

    private void loadProfileSummary() {
        loadPhoto(profilePhoto, currentUser.getPhoto(), 80);

        profileName.setText(currentUser.getNom());
        profileEmail.setText("📧 " + currentUser.getEmail());
        profileTel.setText("📱 " + (currentUser.getTel() != null ? currentUser.getTel() : "Non renseigné"));

        if (currentBanque != null) {
            profileCode.setText("[" + currentBanque.getCodeBanque() + "]");
            profileRepresentant.setText(currentBanque.getRepresentantLegal());
            profileSiege.setText(currentBanque.getAddresseSiege());
            profileSiteWeb.setText(currentBanque.getSiteWeb() != null ? currentBanque.getSiteWeb() : "Non renseigné");
        }
    }

    private void loadRecentDocuments() throws SQLException {
        recentDocumentsContainer.getChildren().clear();

        List<Document> docs = documentService.afficherParUtilisateur(currentUser.getId());

        if (docs.isEmpty()) {
            Label empty = new Label("Aucun document. Ajoutez vos documents officiels !");
            empty.setStyle("-fx-text-fill: #848A86;");

            Button addBtn = new Button("➕ Ajouter un document");
            addBtn.getStyleClass().addAll("btn", "btn-accent");
            addBtn.setOnAction(e -> goToDocuments());

            recentDocumentsContainer.getChildren().addAll(empty, addBtn);
            return;
        }

        // Afficher les 5 derniers
        int count = Math.min(5, docs.size());
        for (int i = 0; i < count; i++) {
            Document doc = docs.get(i);
            HBox docRow = createDocumentRow(doc);
            recentDocumentsContainer.getChildren().add(docRow);
        }
    }

    private HBox createDocumentRow(Document doc) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 12; -fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        // Icône
        Label icon = new Label(getDocumentIcon(doc.getTypeDocument()));
        icon.setStyle("-fx-font-size: 24px;");

        // Info
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(doc.getNom());
        nameLabel.setStyle("-fx-font-weight: bold;");

        String dateStr = doc.getDateUpload() != null ?
                doc.getDateUpload().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-text-fill: #848A86; -fx-font-size: 11px;");

        info.getChildren().addAll(nameLabel, dateLabel);

        // Badge statut
        Label badge = new Label();
        switch (doc.getStatut()) {
            case "valide":
                badge.setText("✓ Validé");
                badge.getStyleClass().addAll("badge", "badge-success");
                break;
            case "rejete":
                badge.setText("✗ Rejeté");
                badge.getStyleClass().addAll("badge", "badge-danger");
                break;
            default:
                badge.setText("⏳ En attente");
                badge.getStyleClass().addAll("badge", "badge-warning");
        }

        row.getChildren().addAll(icon, info, badge);
        return row;
    }

    private String getDocumentIcon(String type) {
        switch (type.toLowerCase()) {
            case "kbis":
                return "📜";
            case "agrement":
                return "🏛️";
            case "licence":
                return "📋";
            default:
                return "📄";
        }
    }

    private void checkVerificationStatus() {
        if (currentBanque != null && !currentBanque.isCompteVerifie()) {
            verificationAlert.setVisible(true);
            verificationAlert.setManaged(true);
        }
    }

    private void loadPhoto(ImageView imageView, String photoPath, double size) {
        Image image = null;

        if (photoPath != null && !photoPath.isEmpty()) {
            File photoFile = new File(photoPath);
            if (photoFile.exists()) {
                image = new Image(photoFile.toURI().toString());
            }
        }

        if (image == null) {
            try {
                image = new Image(getClass().getResourceAsStream("/images/default-avatar.png"));
            } catch (Exception e) {}
        }

        if (image != null) {
            imageView.setImage(image);
            Circle clip = new Circle(size / 2, size / 2, size / 2);
            imageView.setClip(clip);
        }
    }

    @FXML
    public void goToProfile() {
        loadPage("banque/banque-profile");
    }

    @FXML
    public void goToDocuments() {
        loadPage("banque/banque-documents");
    }

    @FXML
    public void goToMessagerie() {
        loadPage("banque/banque-messagerie");
    }

    private void loadPage(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fxmlFile + ".fxml"));
            Stage stage = MainApp.getPrimaryStage();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur de chargement de la page", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}