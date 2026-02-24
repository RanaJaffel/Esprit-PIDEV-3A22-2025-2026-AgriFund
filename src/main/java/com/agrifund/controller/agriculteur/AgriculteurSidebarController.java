package com.agrifund.controller.agriculteur;

import com.agrifund.Main;
import com.agrifund.services.AgriculteurService;
import com.agrifund.util.SessionManager;
import com.agrifund.entities.Agriculteur;
import com.agrifund.entities.Utilisateur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class AgriculteurSidebarController {

    @FXML private ImageView userAvatar;
    @FXML private Label userName;
    @FXML private HBox verificationBadge;
    @FXML private Label typeCultureLabel;
    @FXML private Label superficieLabel;

    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;
    @FXML private Button btnDocuments;
    @FXML private Button btnMessagerie;
    @FXML private Button btnSecurity;

    private AgriculteurService agriculteurService;

    @FXML
    public void initialize() {
        try {
            agriculteurService = new AgriculteurService();
            loadUserInfo();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUserInfo() throws SQLException {
        SessionManager session = SessionManager.getInstance();
        Utilisateur user = session.getUtilisateurConnecte();

        if (user != null) {
            userName.setText(user.getPrenom() + " " + user.getNom());
            loadUserPhoto(user.getPhoto());

            // Charger les infos agriculteur
            Agriculteur agri = agriculteurService.rechercherParUtilisateurId(user.getId());
            if (agri != null) {
                // Badge de vérification
                verificationBadge.getChildren().clear();
                Label badge = new Label();
                if (agri.isCompteVerifie()) {
                    badge.setText("✓ Vérifié");
                    badge.setStyle("-fx-background-color: #089647; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px;");
                } else {
                    badge.setText("⏳ En attente");
                    badge.setStyle("-fx-background-color: #E1B323; -fx-text-fill: #060806; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px;");
                }
                verificationBadge.getChildren().add(badge);

                // Infos ferme
                typeCultureLabel.setText(agri.getTypeCulture());
                superficieLabel.setText(agri.getSuperficieFerme() + " ha");
            }
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

        if (image == null) {
            try {
                image = new Image(getClass().getResourceAsStream("/com/agrifund/images/default-avatar.png"));
            } catch (Exception e) {}
        }

        if (image != null) {
            userAvatar.setImage(image);
            Circle clip = new Circle(30, 30, 30);
            userAvatar.setClip(clip);
        }
    }

    @FXML
    public void goToDashboard() {
        loadPage("agriculteur/agriculteur-dashboard");
    }

    @FXML
    public void goToProfile() {
        loadPage("agriculteur/agriculteur-profile");
    }

    @FXML
    public void goToDocuments() {
        loadPage("agriculteur/agriculteur-documents");
    }

    @FXML
    public void goToMessagerie() {
        loadPage("agriculteur/agriculteur-messagerie");
    }

    @FXML
    public void goToSecurity() {
        loadPage("agriculteur/agriculteur-security");
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
}