package com.agrifund.controller.agriculteur;

import com.agrifund.Main;
import com.agrifund.services.AgriculteurService;
import com.agrifund.util.NavigationManager;
import com.agrifund.util.SessionManager;
import com.agrifund.entities.Agriculteur;
import com.agrifund.entities.Utilisateur;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AgriculteurSidebarController {

    @FXML private ImageView userAvatar;
    @FXML private Label userName;
    @FXML private HBox verificationBadge;
    @FXML private Label typeCultureLabel;
    @FXML private Label superficieLabel;

    // ══════════════════════════════════════════════════════════
    // BOUTONS DE NAVIGATION
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;
    @FXML private Button btnDocuments;
    @FXML private Button btnProduitsFinanciers;
    @FXML private Button btnOffresFinancieres;
    @FXML private Button btnAccueilProduits;
    @FXML private Button btnProjetsAgricoles;
    @FXML private Button btnRessources;
    @FXML private Button btnDecisions;
    @FXML private Button btnRisques;
    @FXML private Button btnCapteurs;
    @FXML private Button btnRapports;
    @FXML private Button btnMeteo;
    @FXML private Button btnMessagerie;
    @FXML private Button btnCarte;
    @FXML private Button btnChatbot;
    @FXML private Button btnSecurity;

    // Map pour associer les pages aux boutons
    private Map<String, Button> pageButtonMap;

    // Gestionnaire de navigation
    private final NavigationManager navigationManager = NavigationManager.getInstance();

    private AgriculteurService agriculteurService;

    @FXML
    public void initialize() {
        try {
            agriculteurService = new AgriculteurService();
            initializePageButtonMap();
            loadUserInfo();

            // Mettre à jour le bouton actif selon la page courante
            updateActiveButton(navigationManager.getCurrentPage());

            // Écouter les changements de page
            navigationManager.currentPageProperty().addListener((obs, oldPage, newPage) -> {
                updateActiveButton(newPage);
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialise la map des pages et boutons correspondants
     */
    private void initializePageButtonMap() {
        pageButtonMap = new HashMap<>();
        pageButtonMap.put("dashboard", btnDashboard);
        pageButtonMap.put("profile", btnProfile);
        pageButtonMap.put("documents", btnDocuments);
        pageButtonMap.put("produits-financiers", btnProduitsFinanciers);
        pageButtonMap.put("offres-financieres", btnOffresFinancieres);
        pageButtonMap.put("accueil-produits", btnAccueilProduits);
        pageButtonMap.put("projets-agricoles", btnProjetsAgricoles);
        pageButtonMap.put("ressources", btnRessources);
        pageButtonMap.put("decisions", btnDecisions);
        pageButtonMap.put("risques", btnRisques);
        pageButtonMap.put("capteurs", btnCapteurs);
        pageButtonMap.put("rapports", btnRapports);
        pageButtonMap.put("meteo", btnMeteo);
        pageButtonMap.put("messagerie", btnMessagerie);
        pageButtonMap.put("carte", btnCarte);
        pageButtonMap.put("chatbot", btnChatbot);
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
        if (activeBtn != null && !activeBtn.getStyleClass().contains("sidebar-menu-item-active")) {
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
                    badge.getStyleClass().add("verification-badge-verified");
                } else {
                    badge.setText("⏳ En attente");
                    badge.getStyleClass().add("verification-badge-pending");
                }
                verificationBadge.getChildren().add(badge);

                // Infos ferme
                if (typeCultureLabel != null) {
                    typeCultureLabel.setText(agri.getTypeCulture() != null ? agri.getTypeCulture() : "Non défini");
                }
                if (superficieLabel != null) {
                    superficieLabel.setText(agri.getSuperficieFerme() + " ha");
                }
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
            } catch (Exception e) {
                System.err.println("Erreur chargement avatar: " + e.getMessage());
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
        navigateTo("dashboard", "/com/agrifund/fxml/agriculteur/agriculteur-dashboard.fxml");
    }

    @FXML
    public void goToProfile() {
        navigateTo("profile", "/com/agrifund/fxml/agriculteur/agriculteur-profile.fxml");
    }

    @FXML
    public void goToDocuments() {
        navigateTo("documents", "/com/agrifund/fxml/agriculteur/agriculteur-documents.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - FINANCE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToProduitsFinanciers() {
        navigateTo("produits-financiers", "/com/agrifund/fxml/agriculteur/AgriculteurProduitView.fxml");
    }

    @FXML
    public void goToOffresFinancieres() {
        navigateTo("offres-financieres", "/com/agrifund/fxml/agriculteur/AgriculteurOffreView.fxml");
    }

    @FXML
    public void goToDemandeFinancement() {
        navigateTo("demande-financement", "/com/agrifund/view/DemandeFinancementView.fxml");
    }

    @FXML
    public void goToAccueilProduits() {
        navigateTo("accueil-produits", "/com/agrifund/view/AccueilUtilisateurView.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - AGRICULTURE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToProjetsAgricoles() {
        navigateTo("projets-agricoles", "/com/agrifund/fxml/agriculteur/agriculteur-projects.fxml");
    }

    @FXML
    public void goToRessources() {
        navigateTo("ressources", "/com/agrifund/fxml/agriculteur/ressourcesAgriculteur.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - DÉCISIONS & RISQUES
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToDecisions() {
        navigateTo("decisions", "/com/agrifund/fxml/DecisionList.fxml");
    }

    @FXML
    public void goToRisques() {
        navigateTo("risques", "/com/agrifund/fxml/RisqueList.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - IoT & RAPPORTS
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToCapteurs() {
        navigateTo("capteurs", "/com/agrifund/fxml/agriculteur/agriculteur-capteur.fxml");
    }

    @FXML
    public void goToMeteo() {
        navigateTo("meteo", "/com/agrifund/fxml/agriculteur/meteo.fxml");
    }

    @FXML
    public void goToRapports() {
        navigateTo("rapports", "/com/agrifund/fxml/agriculteur/rapport.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - COMMUNICATION
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToMessagerie() {
        navigateTo("messagerie", "/com/agrifund/fxml/agriculteur/agriculteur-messagerie.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - OUTILS
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToCarte() {
        navigateTo("carte", "/com/agrifund/view/MapView.fxml");
    }

    @FXML
    public void goToChatbot() {
        navigateTo("chatbot", "/com/agrifund/fxml/agriculteur/AgriculteurChatbotView.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - COMPTE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToSecurity() {
        navigateTo("security", "/com/agrifund/fxml/agriculteur/agriculteur-security.fxml");
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
            navigationManager.reset();
            Main.handleLogout();
        }
    }
}