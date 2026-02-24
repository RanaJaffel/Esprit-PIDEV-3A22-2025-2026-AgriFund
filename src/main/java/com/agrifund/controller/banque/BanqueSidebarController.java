package com.agrifund.controller.banque;

import com.agrifund.Main;
import com.agrifund.services.BanqueService;
import com.agrifund.util.SessionManager;
import com.agrifund.entities.Banque;
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

public class BanqueSidebarController {

    @FXML private ImageView userAvatar;
    @FXML private Label userName;
    @FXML private Label codeBanque;
    @FXML private HBox verificationBadge;
    @FXML private Label siegeLabel;

    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;
    @FXML private Button btnDocuments;
    @FXML private Button btnMessagerie;
    @FXML private Button btnSecurity;
    @FXML private Button btnSatellite;
    private BanqueService banqueService;

    @FXML
    public void initialize() {
        try {
            banqueService = new BanqueService();
            loadUserInfo();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUserInfo() throws SQLException {
        SessionManager session = SessionManager.getInstance();
        Utilisateur user = session.getUtilisateurConnecte();

        if (user != null) {
            userName.setText(user.getNom());
            loadUserPhoto(user.getPhoto());

            // Charger les infos banque
            Banque banque = banqueService.rechercherParUtilisateurId(user.getId());
            if (banque != null) {
                codeBanque.setText("[" + banque.getCodeBanque() + "]");

                // Siège (tronqué si trop long)
                String siege = banque.getAddresseSiege();
                if (siege != null && siege.length() > 30) {
                    siege = siege.substring(0, 30) + "...";
                }
                siegeLabel.setText(siege);

                // Badge de vérification
                verificationBadge.getChildren().clear();
                Label badge = new Label();
                if (banque.isCompteVerifie()) {
                    badge.setText("✓ Vérifiée");
                    badge.setStyle("-fx-background-color: #089647; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px;");
                } else {
                    badge.setText("⏳ En attente");
                    badge.setStyle("-fx-background-color: #E1B323; -fx-text-fill: #060806; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 10px;");
                }
                verificationBadge.getChildren().add(badge);
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
        loadPage("banque/banque-dashboard");
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

    @FXML
    public void goToSecurity() {
        loadPage("banque/banque-security");
    }
    @FXML
    public void goToSatellite() {
        loadPage("banque/banque-satellite");
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