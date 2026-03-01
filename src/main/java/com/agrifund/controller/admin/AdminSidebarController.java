package com.agrifund.controller.admin;

import com.agrifund.Main;
import com.agrifund.util.NavigationManager;
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
import java.util.HashMap;
import java.util.Map;

public class AdminSidebarController {

    @FXML private ImageView userAvatar;
    @FXML private Label userName;

    // ══════════════════════════════════════════════════════════
    // BOUTONS DE NAVIGATION
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnAgriculteurs;
    @FXML private Button btnBanques;
    @FXML private Button btnDocuments;
    @FXML private Button btnMessagerie;
    @FXML private Button btnProduits;
    @FXML private Button btnOffres;
    @FXML private Button btnProjetsAgricoles;
    @FXML private Button btnRessources;
    @FXML private Button btnDecisions;
    @FXML private Button btnRisques;
    @FXML private Button btnCapteurs;
    @FXML private Button btnDashboardIoT;
    @FXML private Button btnRapports;
    @FXML private Button btnMeteo;
    @FXML private Button btnCarte;
    @FXML private Button btnProfile;
    @FXML private Button btnSecurity;

    // Map pour associer les pages aux boutons
    private Map<String, Button> pageButtonMap;

    // Gestionnaire de navigation
    private final NavigationManager navigationManager = NavigationManager.getInstance();

    @FXML
    public void initialize() {
        initializePageButtonMap();
        loadUserInfo();

        // Mettre à jour le bouton actif selon la page courante
        updateActiveButton(navigationManager.getCurrentPage());

        // Écouter les changements de page
        navigationManager.currentPageProperty().addListener((obs, oldPage, newPage) -> {
            updateActiveButton(newPage);
        });
    }

    /**
     * Initialise la map des pages et boutons correspondants
     */
    private void initializePageButtonMap() {
        pageButtonMap = new HashMap<>();
        pageButtonMap.put("dashboard", btnDashboard);
        pageButtonMap.put("users", btnUsers);
        pageButtonMap.put("agriculteurs", btnAgriculteurs);
        pageButtonMap.put("banques", btnBanques);
        pageButtonMap.put("documents", btnDocuments);
        pageButtonMap.put("messagerie", btnMessagerie);
        pageButtonMap.put("produits-financiers", btnProduits);
        pageButtonMap.put("offres-financieres", btnOffres);
        pageButtonMap.put("projets-agricoles", btnProjetsAgricoles);
        pageButtonMap.put("ressources", btnRessources);
        pageButtonMap.put("decisions", btnDecisions);
        pageButtonMap.put("risques", btnRisques);
        pageButtonMap.put("capteurs", btnCapteurs);
        pageButtonMap.put("dashboard-iot", btnDashboardIoT);
        pageButtonMap.put("rapports", btnRapports);
        pageButtonMap.put("meteo", btnMeteo);
        pageButtonMap.put("carte", btnCarte);

        pageButtonMap.put("profile", btnProfile);
        pageButtonMap.put("security", btnSecurity);
    }

    /**
     * Met à jour le style du bouton actif
     */
    private void updateActiveButton(String currentPage) {
        // Retirer la classe active de tous les boutons
        for (Button btn : pageButtonMap.values()) {
            if (btn != null) {
                btn.getStyleClass().remove("sidebar-menu-item-active");
            }
        }

        // Ajouter la classe active au bouton correspondant à la page courante
        Button activeBtn = pageButtonMap.get(currentPage);
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("sidebar-menu-item-active");
        }
    }

    /**
     * Méthode générique pour la navigation
     */
    private void navigateTo(String page, String fxmlPath) {
        navigationManager.setCurrentPage(page);
        Main.navigateTo(fxmlPath);
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
                System.err.println("Erreur chargement avatar par défaut: " + e.getMessage());
            }
        }

        if (image != null) {
            userAvatar.setImage(image);
            Circle clip = new Circle(35, 35, 35);
            userAvatar.setClip(clip);
        }
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - PRINCIPAL
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToDashboard() {
        navigateTo("dashboard", "/com/agrifund/fxml/admin/admin-dashboard.fxml");
    }

    @FXML
    public void goToUsers() {
        navigateTo("users", "/com/agrifund/fxml/admin/admin-users.fxml");
    }

    @FXML
    public void goToAgriculteurs() {
        navigateTo("agriculteurs", "/com/agrifund/fxml/admin/admin-agriculteurs.fxml");
    }

    @FXML
    public void goToBanques() {
        navigateTo("banques", "/com/agrifund/fxml/admin/admin-banques.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - GESTION
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToDocuments() {
        navigateTo("documents", "/com/agrifund/fxml/admin/admin-documents.fxml");
    }

    @FXML
    public void goToMessagerie() {
        navigateTo("messagerie", "/com/agrifund/fxml/admin/admin-messagerie.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - FINANCE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToProduitsFinanciers() {
        navigateTo("produits-financiers", "/com/agrifund/fxml/admin/AdminProduitView.fxml");
    }

    @FXML
    public void goToOffresFinancieres() {
        navigateTo("offres-financieres", "/com/agrifund/fxml/admin/AdminOffreView.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - AGRICULTURE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToProjetsAgricoles() {
        navigateTo("projets-agricoles", "/com/agrifund/fxml/admin/admin-projects.fxml");
    }

    @FXML
    public void goToRessources() {
        navigateTo("ressources", "/com/agrifund/fxml/admin/ressourcesAdmin.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - DÉCISIONS & RISQUES
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToDecisions() {
        navigateTo("decisions", "/com/agrifund/fxml/admin/admin-decision-list.fxml");
    }

    @FXML
    public void goToRisques() {
        navigateTo("risques", "/com/agrifund/fxml/admin/admin-risque-list.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - IoT & RAPPORTS
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToCapteurs() {
        navigateTo("capteurs", "/com/agrifund/fxml/admin/admin-capteur.fxml");
    }

    @FXML
    public void goToDashboardIoT() {
        navigateTo("dashboard-iot", "/com/agrifund/fxml/dashboard-rana.fxml");
    }

    @FXML
    public void goToRapports() {
        navigateTo("rapports", "/com/agrifund/fxml/rapport.fxml");
    }

    @FXML
    public void goToMeteo() {
        navigateTo("meteo", "/com/agrifund/fxml/meteo.fxml");
    }
    public void goToCarte() {
        navigateTo("carte", "/com/agrifund/view/MapView.fxml");
    }


    // ══════════════════════════════════════════════════════════
    // NAVIGATION - COMPTE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToProfile() {
        navigateTo("profile", "/com/agrifund/fxml/admin/admin-profile.fxml");
    }

    @FXML
    public void goToSecurity() {
        navigateTo("security", "/com/agrifund/fxml/admin/admin-security.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // DÉCONNEXION
    // ══════════════════════════════════════════════════════════

    @FXML
    public void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
        alert.setContentText("Vous serez redirigé vers la page de connexion.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Réinitialiser la page courante
            navigationManager.setCurrentPage("dashboard");
            Main.handleLogout();
        }
    }
}