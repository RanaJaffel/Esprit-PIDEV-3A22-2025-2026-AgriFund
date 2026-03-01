package com.agrifund.controller.admin;

import com.agrifund.Main;
import com.agrifund.services.*;
import com.agrifund.util.SessionManager;
import com.agrifund.util.FileManager;
import com.agrifund.entities.*;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AdminDashboardController {

    @FXML private ImageView userAvatar;
    @FXML private ImageView topBarAvatar;
    @FXML private Label userName;
    @FXML private Label topBarUserName;
    @FXML private Label unreadBadge;

    @FXML private Label totalUsers;
    @FXML private Label totalAgriculteurs;
    @FXML private Label totalBanques;
    @FXML private Label totalDocuments;
    @FXML private Label pendingAgriculteurs;
    @FXML private Label pendingBanques;
    @FXML private Label pendingDocuments;
    @FXML private Label storageStatus;

    @FXML private VBox recentUsersContainer;
    @FXML private VBox pendingVerificationsContainer;

    private UtilisateurService utilisateurService;
    private AgriculteurService agriculteurService;
    private BanqueService banqueService;
    private DocumentService documentService;
    private MessagerieService messagerieService;

    @FXML
    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();
            agriculteurService = new AgriculteurService();
            banqueService = new BanqueService();
            documentService = new DocumentService();
            messagerieService = new MessagerieService();

            loadUserInfo();
            loadStatistics();
            loadRecentUsers();
            loadPendingVerifications();
            loadUnreadMessages();

        } catch (SQLException e) {
            showAlert("Erreur de chargement: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadUserInfo() {
        SessionManager session = SessionManager.getInstance();
        Utilisateur user = session.getUtilisateurConnecte();

        if (user != null) {
            String nomComplet = user.getPrenom() + " " + user.getNom();
            if (userName != null) userName.setText(nomComplet);
            if (topBarUserName != null) topBarUserName.setText(nomComplet);

            // Charger la photo de profil
            loadUserPhoto(user.getPhoto());
        }
    }

    private void loadUserPhoto(String photoPath) {
        Image image = null;

        if (photoPath != null && !photoPath.isEmpty()) {
            File photoFile = new File(photoPath);
            if (photoFile.exists()) {
                image = new Image(photoFile.toURI().toString());
            }
        }

        // Si pas de photo, utiliser l'avatar par défaut
        if (image == null) {
            image = new Image(getClass().getResourceAsStream("/com/agrifund/images/default-avatar.png"));
        }

        if (userAvatar != null) {
            userAvatar.setImage(image);
            // Appliquer un clip circulaire
            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(30, 30, 30);
            userAvatar.setClip(clip);
        }

        if (topBarAvatar != null) {
            topBarAvatar.setImage(image);
            javafx.scene.shape.Circle clipTop = new javafx.scene.shape.Circle(17.5, 17.5, 17.5);
            topBarAvatar.setClip(clipTop);
        }
    }

    private void loadStatistics() throws SQLException {
        List<Utilisateur> users = utilisateurService.afficherTous();
        List<Agriculteur> agriculteurs = agriculteurService.afficherTous();
        List<Banque> banques = banqueService.afficherTous();
        List<Document> documents = documentService.afficherEnAttente();

        totalUsers.setText(String.valueOf(users.size()));
        totalAgriculteurs.setText(String.valueOf(agriculteurs.size()));
        totalBanques.setText(String.valueOf(banques.size()));
        totalDocuments.setText(String.valueOf(documents.size()));

        // Compter les en attente
        long agriculteursPending = agriculteurs.stream()
                .filter(a -> !a.isCompteVerifie())
                .count();
        pendingAgriculteurs.setText(agriculteursPending + " en attente");

        long banquesPending = banques.stream()
                .filter(b -> !b.isCompteVerifie())
                .count();
        pendingBanques.setText(banquesPending + " en attente");

        pendingDocuments.setText(documents.size() + " à valider");
    }

    private void loadRecentUsers() throws SQLException {
        recentUsersContainer.getChildren().clear();

        List<Utilisateur> users = utilisateurService.afficherTous();

        // Afficher les 5 derniers
        int count = Math.min(5, users.size());
        for (int i = users.size() - 1; i >= users.size() - count && i >= 0; i--) {
            Utilisateur user = users.get(i);
            HBox userRow = createUserRow(user);
            recentUsersContainer.getChildren().add(userRow);
        }

        if (users.isEmpty()) {
            recentUsersContainer.getChildren().add(
                    new Label("Aucun utilisateur récent")
            );
        }
    }

    private HBox createUserRow(Utilisateur user) {
        HBox row = new HBox(15);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        // Avatar
        ImageView avatar = new ImageView();
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);

        Image image = null;
        if (user.getPhoto() != null && !user.getPhoto().isEmpty()) {
            File photoFile = new File(user.getPhoto());
            if (photoFile.exists()) {
                image = new Image(photoFile.toURI().toString());
            }
        }

        if (image == null) {
            try {
                image = new Image(getClass().getResourceAsStream("/com/agrifund/images/default-avatar.png"));
            } catch (Exception e) {
                // Créer un placeholder
            }
        }

        if (image != null) {
            avatar.setImage(image);
            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(20, 20, 20);
            avatar.setClip(clip);
        }

        // Infos
        VBox info = new VBox(2);
        Label nameLabel = new Label(user.getPrenom() + " " + user.getNom());
        nameLabel.setStyle("-fx-font-weight: bold;-fx-text-fill: #876c00;");
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-text-fill: #848A86; -fx-font-size: 12px;");
        info.getChildren().addAll(nameLabel, emailLabel);

        // Date
        Label dateLabel = new Label();
        if (user.getDateInscrit() != null) {
            dateLabel.setText(user.getDateInscrit().toLocalDate().toString());
        }
        dateLabel.setStyle("-fx-text-fill: #848A86;");

        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);
        row.getChildren().addAll(avatar, info, dateLabel);

        return row;
    }

    private void loadPendingVerifications() throws SQLException {
        pendingVerificationsContainer.getChildren().clear();

        List<Agriculteur> agriculteurs = agriculteurService.afficherTous();

        int count = 0;
        for (Agriculteur agri : agriculteurs) {
            if (!agri.isCompteVerifie() && count < 5) {
                HBox row = createPendingRow(agri);
                pendingVerificationsContainer.getChildren().add(row);
                count++;
            }
        }

        if (count == 0) {
            pendingVerificationsContainer.getChildren().add(
                    new Label("Aucune vérification en attente")
            );
        }
    }

    private HBox createPendingRow(Agriculteur agri) {
        HBox row = new HBox(15);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 10; -fx-background-color: rgba(225, 179, 35, 0.1); -fx-background-radius: 8;");

        // Avatar
        ImageView avatar = new ImageView();
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);

        Image image = null;
        if (agri.getPhoto() != null && !agri.getPhoto().isEmpty()) {
            File photoFile = new File(agri.getPhoto());
            if (photoFile.exists()) {
                image = new Image(photoFile.toURI().toString());
            }
        }

        if (image == null) {
            try {
                image = new Image(getClass().getResourceAsStream("/com/agrifund/images/default-avatar.png"));
            } catch (Exception e) {}
        }

        if (image != null) {
            avatar.setImage(image);
            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(20, 20, 20);
            avatar.setClip(clip);
        }

        // Infos
        VBox info = new VBox(2);
        Label nameLabel = new Label(agri.getPrenom() + " " + agri.getNom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ecc500");
        Label typeLabel = new Label("🌾 Agriculteur - " + agri.getTypeCulture());
        typeLabel.setStyle("-fx-text-fill: #848A86; -fx-font-size: 12px;");
        info.getChildren().addAll(nameLabel, typeLabel);

        // Boutons
        Button btnVerify = new Button("✓");
        btnVerify.getStyleClass().addAll("btn", "btn-small", "btn-success");
        btnVerify.setOnAction(e -> verifyAccount(agri.getAgriculteurId(), true));

        Button btnReject = new Button("✗");
        btnReject.getStyleClass().addAll("btn", "btn-small", "btn-danger");
        btnReject.setOnAction(e -> verifyAccount(agri.getAgriculteurId(), false));

        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);
        row.getChildren().addAll(avatar, info, btnVerify, btnReject);

        return row;
    }

    private void verifyAccount(int agriculteurId, boolean verify) {
        try {
            agriculteurService.verifierCompte(agriculteurId, verify);
            loadStatistics();
            loadPendingVerifications();

            String message = verify ? "Compte vérifié avec succès!" : "Compte refusé!";
            showAlert(message, Alert.AlertType.INFORMATION);

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadUnreadMessages() throws SQLException {
        SessionManager session = SessionManager.getInstance();
        int unread = messagerieService.compterMessagesNonLus(session.getUtilisateurConnecte().getId());

        if (unread > 0) {
            unreadBadge.setText(String.valueOf(unread));
            unreadBadge.setVisible(true);
        } else {
            unreadBadge.setVisible(false);
        }
    }

    // Navigation Methods
    @FXML
    public void goToDashboard() {
        loadPage("admin/admin-dashboard");
    }

    @FXML
    public void goToUsers() {
        loadPage("admin/admin-users");
    }

    @FXML
    public void goToAgriculteurs() {
        loadPage("admin/admin-agriculteurs");
    }

    @FXML
    public void goToBanques() {
        loadPage("admin/admin-banques");
    }

    @FXML
    public void goToDocuments() {
        loadPage("admin/admin-documents");
    }

    @FXML
    public void goToMessagerie() {
        loadPage("admin/admin-messagerie");
    }

    @FXML
    public void goToStatistics() {
        loadPage("admin/admin-statistics");
    }

    @FXML
    public void goToProfile() {
        loadPage("admin/admin-profile");
    }

    @FXML
    public void goToSecurity() {
        loadPage("admin/admin-security");
    }

    @FXML
    public void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Voulez-vous vraiment vous déconnecter ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            Main.handleLogout();
        }
    }

    private void loadPage(String fxmlFile) {
        Main.navigateTo("/com/agrifund/fxml/" + fxmlFile + ".fxml");
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}