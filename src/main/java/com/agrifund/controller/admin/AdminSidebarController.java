package com.agrifund.controller.admin;

import com.agrifund.Main;
import com.agrifund.util.SessionManager;
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
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class AdminSidebarController {

    @FXML private ImageView userAvatar;
    @FXML private Label userName;

    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnAgriculteurs;
    @FXML private Button btnBanques;
    @FXML private Button btnDocuments;
    @FXML private Button btnMessagerie;
    @FXML private Button btnProfile;
    @FXML private Button btnSecurity;

    private String currentPage = "dashboard";

    @FXML
    public void initialize() {
        loadUserInfo();
    }

    public void setCurrentPage(String page) {
        this.currentPage = page;
        updateActiveButton();
    }

    private void updateActiveButton() {
        // Retirer la classe active de tous les boutons
        btnDashboard.getStyleClass().remove("sidebar-menu-item-active");
        btnUsers.getStyleClass().remove("sidebar-menu-item-active");
        btnAgriculteurs.getStyleClass().remove("sidebar-menu-item-active");
        btnBanques.getStyleClass().remove("sidebar-menu-item-active");
        btnDocuments.getStyleClass().remove("sidebar-menu-item-active");
        btnMessagerie.getStyleClass().remove("sidebar-menu-item-active");
        btnProfile.getStyleClass().remove("sidebar-menu-item-active");
        btnSecurity.getStyleClass().remove("sidebar-menu-item-active");

        // Ajouter la classe active au bouton actuel
        switch (currentPage) {
            case "dashboard":
                btnDashboard.getStyleClass().add("sidebar-menu-item-active");
                break;
            case "users":
                btnUsers.getStyleClass().add("sidebar-menu-item-active");
                break;
            case "agriculteurs":
                btnAgriculteurs.getStyleClass().add("sidebar-menu-item-active");
                break;
            case "banques":
                btnBanques.getStyleClass().add("sidebar-menu-item-active");
                break;
            case "documents":
                btnDocuments.getStyleClass().add("sidebar-menu-item-active");
                break;
            case "messagerie":
                btnMessagerie.getStyleClass().add("sidebar-menu-item-active");
                break;
            case "profile":
                btnProfile.getStyleClass().add("sidebar-menu-item-active");
                break;
            case "security":
                btnSecurity.getStyleClass().add("sidebar-menu-item-active");
                break;
        }
    }

    private void loadUserInfo() {
        SessionManager session = SessionManager.getInstance();
        Utilisateur user = session.getUtilisateurConnecte();

        if (user != null) {
            userName.setText(user.getPrenom() + " " + user.getNom());
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

        if (image == null) {
            try {
                image = new Image(getClass().getResourceAsStream("/com/agrifund/images/default-avatar.png"));
            } catch (Exception e) {
                // Fallback
            }
        }

        if (image != null) {
            userAvatar.setImage(image);
            Circle clip = new Circle(30, 30, 30);
            userAvatar.setClip(clip);
        }
    }

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
}