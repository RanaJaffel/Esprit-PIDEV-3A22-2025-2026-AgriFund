package com.agrifund.controller.banque;

import com.agrifund.Main;
import com.agrifund.services.BanqueService;
import com.agrifund.util.NavigationManager;
import com.agrifund.util.SessionManager;
import com.agrifund.entities.Banque;
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

public class BanqueSidebarController {

    @FXML private ImageView userAvatar;
    @FXML private Label userName;
    @FXML private Label codeBanque;
    @FXML private HBox verificationBadge;
    @FXML private Label siegeLabel;
    @FXML private Label totalClientsLabel;

    // ══════════════════════════════════════════════════════════
    // BOUTONS DE NAVIGATION
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;
    @FXML private Button btnDocuments;
    @FXML private Button btnProduit;
    @FXML private Button btnOffre;
    @FXML private Button btnProjetsAgricoles;
    @FXML private Button btnRessources;
    @FXML private Button btnDecisions;
    @FXML private Button btnRisques;
    @FXML private Button btnSatellite;
    @FXML private Button btnCapteurs;
    @FXML private Button btnDashboardIoT;
    @FXML private Button btnRapports;
    @FXML private Button btnMessagerie;
    @FXML private Button btnCarte;
    @FXML private Button btnChatbot;
    @FXML private Button btnSecurity;

    // Map pour associer les pages aux boutons
    private Map<String, Button> pageButtonMap;

    // Gestionnaire de navigation
    private final NavigationManager navigationManager = NavigationManager.getInstance();

    private BanqueService banqueService;

    @FXML
    public void initialize() {
        try {
            banqueService = new BanqueService();
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
        pageButtonMap.put("produits-financiers", btnProduit);
        pageButtonMap.put("offres-financieres", btnOffre);
        pageButtonMap.put("projets-agricoles", btnProjetsAgricoles);
        pageButtonMap.put("ressources", btnRessources);
        pageButtonMap.put("decisions", btnDecisions);
        pageButtonMap.put("risques", btnRisques);
        pageButtonMap.put("satellite", btnSatellite);
        pageButtonMap.put("capteurs", btnCapteurs);
        pageButtonMap.put("dashboard-iot", btnDashboardIoT);
        pageButtonMap.put("rapports", btnRapports);
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
            userName.setText(user.getNom());
            loadUserPhoto(user.getPhoto());

            // Charger les infos banque
            Banque banque = banqueService.rechercherParUtilisateurId(user.getId());
            if (banque != null) {
                if (codeBanque != null) {
                    codeBanque.setText(banque.getCodeBanque());
                }

                // Siège
                if (siegeLabel != null) {
                    String siege = banque.getAddresseSiege();
                    if (siege != null && siege.length() > 25) {
                        siege = siege.substring(0, 25) + "...";
                    }
                    siegeLabel.setText(siege != null ? siege : "Non défini");
                }

                // Badge de vérification
                if (verificationBadge != null) {
                    verificationBadge.getChildren().clear();
                    Label badge = new Label();
                    if (banque.isCompteVerifie()) {
                        badge.setText("✓ Vérifiée");
                        badge.getStyleClass().add("verification-badge-verified");
                    } else {
                        badge.setText("⏳ En attente");
                        badge.getStyleClass().add("verification-badge-pending");
                    }
                    verificationBadge.getChildren().add(badge);
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
                image = new Image(getClass().getResourceAsStream("/com/agrifund/images/default-bank-avatar.png"));
            } catch (Exception e) {
                try {
                    image = new Image(getClass().getResourceAsStream("/com/agrifund/images/default-avatar.png"));
                } catch (Exception ex) {
                    System.err.println("Erreur chargement avatar: " + ex.getMessage());
                }
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
        navigateTo("dashboard", "/com/agrifund/fxml/banque/banque-dashboard.fxml");
    }

    @FXML
    public void goToProfile() {
        navigateTo("profile", "/com/agrifund/fxml/banque/banque-profile.fxml");
    }

    @FXML
    public void goToDocuments() {
        navigateTo("documents", "/com/agrifund/fxml/banque/banque-documents.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - FINANCE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToProduitsFinanciers() {
        navigateTo("produits-financiers", "/com/agrifund/fxml/banque/BanqueProduitView.fxml");
    }

    @FXML
    public void goToOffresFinancieres() {
        navigateTo("offres-financieres", "/com/agrifund/fxml/banque/BanqueOffreView.fxml");
    }

    @FXML
    public void goToDemandeFinancement() {
        navigateTo("demande-financement", "/com/agrifund/view/DemandeFinancementView.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - AGRICULTURE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToProjetsAgricoles() {
        navigateTo("projets-agricoles", "/com/agrifund/fxml/banque/banque-projects.fxml");
    }

    @FXML
    public void goToRessources() {
        navigateTo("ressources", "/com/agrifund/fxml/banque/ressourcesBanque.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - DÉCISIONS & RISQUES
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToDecisions() {
        navigateTo("decisions", "/com/agrifund/fxml/banque/banque-decision-list.fxml");
    }

    @FXML
    public void goToRisques() {
        navigateTo("risques", "/com/agrifund/fxml/banque/banque-risque-list.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - DONNÉES SATELLITE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToSatellite() {
        navigateTo("satellite", "/com/agrifund/fxml/banque/banque-satellite.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - IoT & RAPPORTS
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToCapteurs() {
        navigateTo("capteurs", "/com/agrifund/fxml/capteur.fxml");
    }

    @FXML
    public void goToDashboardIoT() {
        navigateTo("dashboard-iot", "/com/agrifund/fxml/dashboard-rana.fxml");
    }

    @FXML
    public void goToRapports() {
        navigateTo("rapports", "/com/agrifund/fxml/rapport.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - COMMUNICATION
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToMessagerie() {
        navigateTo("messagerie", "/com/agrifund/fxml/banque/banque-messagerie.fxml");
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
        navigateTo("chatbot", "/com/agrifund/view/ChatbotView.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - COMPTE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToSecurity() {
        navigateTo("security", "/com/agrifund/fxml/banque/banque-security.fxml");
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