package com.agrifund.controller.admin;

import com.agrifund.Main;
import com.agrifund.util.SessionManager;
import com.agrifund.entities.Utilisateur;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.io.File;

public class AdminSidebarController {

    @FXML private ImageView userAvatar;
    @FXML private Label userName;

    // ══════════════════════════════════════════════════════════
    // PRINCIPAL
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnAgriculteurs;
    @FXML private Button btnBanques;

    // ══════════════════════════════════════════════════════════
    // GESTION
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnDocuments;
    @FXML private Button btnMessagerie;

    // ══════════════════════════════════════════════════════════
    // FINANCE
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnProduitsFinanciers;
    @FXML private Button btnOffresFinancieres;

    // ══════════════════════════════════════════════════════════
    // AGRICULTURE
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnProjetsAgricoles;
    @FXML private Button btnRessources;

    // ══════════════════════════════════════════════════════════
    // DÉCISIONS & RISQUES
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnDecisions;
    @FXML private Button btnRisques;

    // ══════════════════════════════════════════════════════════
    // IoT & RAPPORTS
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnCapteurs;
    @FXML private Button btnDashboardIoT;
    @FXML private Button btnRapports;
    @FXML private Button btnMeteo;
    // ══════════════════════════════════════════════════════════
    // COMPTE
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnProfile;
    @FXML private Button btnSecurity;

    private String currentPage = "dashboard";

    @FXML
    public void initialize() {
        loadUserInfo();
        updateActiveButton();
    }

    public void setCurrentPage(String page) {
        this.currentPage = page;
        updateActiveButton();
    }

    private void updateActiveButton() {
        // Liste de tous les boutons
        Button[] allButtons = {
                btnDashboard, btnUsers, btnAgriculteurs, btnBanques,
                btnDocuments, btnMessagerie,
                btnProduitsFinanciers, btnOffresFinancieres,
                btnProjetsAgricoles, btnRessources,
                btnDecisions, btnRisques,
                btnCapteurs, btnDashboardIoT, btnRapports,
                btnProfile, btnSecurity
        };

        // Retirer la classe active de tous les boutons
        for (Button btn : allButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("sidebar-menu-item-active");
            }
        }

        // Ajouter la classe active au bouton actuel
        Button activeBtn = null;
        switch (currentPage) {
            case "dashboard":
                activeBtn = btnDashboard;
                break;
            case "users":
                activeBtn = btnUsers;
                break;
            case "agriculteurs":
                activeBtn = btnAgriculteurs;
                break;
            case "banques":
                activeBtn = btnBanques;
                break;
            case "documents":
                activeBtn = btnDocuments;
                break;
            case "messagerie":
                activeBtn = btnMessagerie;
                break;
            case "produits-financiers":
                activeBtn = btnProduitsFinanciers;
                break;
            case "offres-financieres":
                activeBtn = btnOffresFinancieres;
                break;
            case "projets-agricoles":
                activeBtn = btnProjetsAgricoles;
                break;
            case "ressources":
                activeBtn = btnRessources;
                break;
            case "decisions":
                activeBtn = btnDecisions;
                break;
            case "risques":
                activeBtn = btnRisques;
                break;
            case "capteurs":
                activeBtn = btnCapteurs;
                break;
            case "dashboard-iot":
                activeBtn = btnDashboardIoT;
                break;
            case "rapports":
                activeBtn = btnRapports;
                break;
            case "meteo":
                activeBtn = btnMeteo;
            case "profile":
                activeBtn = btnProfile;
                break;
            case "security":
                activeBtn = btnSecurity;
                break;
        }

        if (activeBtn != null) {
            activeBtn.getStyleClass().add("sidebar-menu-item-active");
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

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - PRINCIPAL
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToDashboard() {
        currentPage = "dashboard";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/admin/admin-dashboard.fxml");
    }

    @FXML
    public void goToUsers() {
        currentPage = "users";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/admin/admin-users.fxml");
    }

    @FXML
    public void goToAgriculteurs() {
        currentPage = "agriculteurs";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/admin/admin-agriculteurs.fxml");
    }

    @FXML
    public void goToBanques() {
        currentPage = "banques";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/admin/admin-banques.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - GESTION
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToDocuments() {
        currentPage = "documents";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/admin/admin-documents.fxml");
    }

    @FXML
    public void goToMessagerie() {
        currentPage = "messagerie";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/admin/admin-messagerie.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - FINANCE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToProduitsFinanciers() {
        currentPage = "produits-financiers";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/view/ProduitFinancierView.fxml");
    }

    @FXML
    public void goToOffresFinancieres() {
        currentPage = "offres-financieres";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/view/OffreFinanciereView.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - AGRICULTURE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToProjetsAgricoles() {
        currentPage = "projets-agricoles";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/admin/admin-projects.fxml");
    }

    @FXML
    public void goToRessources() {
        currentPage = "ressources";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/admin/ressourcesAdmin.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - DÉCISIONS & RISQUES
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToDecisions() {
        currentPage = "decisions";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/admin/admin-decision-list.fxml");
    }

    @FXML
    public void goToRisques() {
        currentPage = "risques";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/admin/admin-risque-list.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - IoT & RAPPORTS
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToCapteurs() {
        currentPage = "capteurs";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/admin/admin-capteur.fxml");
    }

    @FXML
    public void goToDashboardIoT() {
        currentPage = "dashboard-iot";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/dashboard-rana.fxml");
    }

    @FXML
    public void goToRapports() {
        currentPage = "rapports";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/rapport.fxml");
    }

    @FXML
    public void goToMeteo(){
        currentPage="meteo";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/meteo.fxml");
    }
    // ══════════════════════════════════════════════════════════
    // NAVIGATION - COMPTE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToProfile() {
        currentPage = "profile";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/admin/admin-profile.fxml");
    }

    @FXML
    public void goToSecurity() {
        currentPage = "security";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/admin/admin-security.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // DÉCONNEXION
    // ══════════════════════════════════════════════════════════

    @FXML
    public void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Voulez-vous vraiment vous déconnecter ?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Main.handleLogout();
        }
    }
}