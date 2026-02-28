package com.agrifund.controller.banque;

import com.agrifund.Main;
import com.agrifund.services.BanqueService;
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

public class BanqueSidebarController {

    @FXML private ImageView userAvatar;
    @FXML private Label userName;
    @FXML private Label codeBanque;
    @FXML private HBox verificationBadge;
    @FXML private Label siegeLabel;

    // ══════════════════════════════════════════════════════════
    // PRINCIPAL
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;
    @FXML private Button btnDocuments;

    // ══════════════════════════════════════════════════════════
    // FINANCE
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnProduitsFinanciers;
    @FXML private Button btnOffresFinancieres;
    @FXML private Button btnDemandeFinancement;

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
    // DONNÉES SATELLITE
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnSatellite;

    // ══════════════════════════════════════════════════════════
    // IoT & RAPPORTS
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnCapteurs;
    @FXML private Button btnDashboardIoT;
    @FXML private Button btnRapports;

    // ══════════════════════════════════════════════════════════
    // COMMUNICATION
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnMessagerie;

    // ══════════════════════════════════════════════════════════
    // OUTILS
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnCarte;
    @FXML private Button btnChatbot;

    // ══════════════════════════════════════════════════════════
    // COMPTE
    // ══════════════════════════════════════════════════════════
    @FXML private Button btnSecurity;

    private BanqueService banqueService;
    private String currentPage = "dashboard";

    @FXML
    public void initialize() {
        try {
            banqueService = new BanqueService();
            loadUserInfo();
            updateActiveButton();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentPage(String page) {
        this.currentPage = page;
        updateActiveButton();
    }

    private void updateActiveButton() {
        // Liste de tous les boutons
        Button[] allButtons = {
                btnDashboard, btnProfile, btnDocuments,
                btnProduitsFinanciers, btnOffresFinancieres, btnDemandeFinancement,
                btnProjetsAgricoles, btnRessources,
                btnDecisions, btnRisques,
                btnSatellite,
                btnCapteurs, btnDashboardIoT, btnRapports,
                btnMessagerie,
                btnCarte, btnChatbot,
                btnSecurity
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
            case "profile":
                activeBtn = btnProfile;
                break;
            case "documents":
                activeBtn = btnDocuments;
                break;
            case "produits-financiers":
                activeBtn = btnProduitsFinanciers;
                break;
            case "offres-financieres":
                activeBtn = btnOffresFinancieres;
                break;
            case "demande-financement":
                activeBtn = btnDemandeFinancement;
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
            case "satellite":
                activeBtn = btnSatellite;
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
            case "messagerie":
                activeBtn = btnMessagerie;
                break;
            case "carte":
                activeBtn = btnCarte;
                break;
            case "chatbot":
                activeBtn = btnChatbot;
                break;
            case "security":
                activeBtn = btnSecurity;
                break;
        }

        if (activeBtn != null) {
            activeBtn.getStyleClass().add("sidebar-menu-item-active");
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
                if (codeBanque != null) {
                    codeBanque.setText("[" + banque.getCodeBanque() + "]");
                }

                // Siège (tronqué si trop long)
                if (siegeLabel != null) {
                    String siege = banque.getAddresseSiege();
                    if (siege != null && siege.length() > 30) {
                        siege = siege.substring(0, 30) + "...";
                    }
                    siegeLabel.setText(siege != null ? siege : "Non défini");
                }

                // Badge de vérification
                if (verificationBadge != null) {
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
        Main.navigateTo("/com/agrifund/fxml/banque/banque-dashboard.fxml");
    }

    @FXML
    public void goToProfile() {
        currentPage = "profile";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/banque/banque-profile.fxml");
    }

    @FXML
    public void goToDocuments() {
        currentPage = "documents";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/banque/banque-documents.fxml");
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

    @FXML
    public void goToDemandeFinancement() {
        currentPage = "demande-financement";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/view/DemandeFinancementView.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - AGRICULTURE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToProjetsAgricoles() {
        currentPage = "projets-agricoles";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/banque/banque-projects.fxml");
    }

    @FXML
    public void goToRessources() {
        currentPage = "ressources";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/banque/ressourcesBanque.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - DÉCISIONS & RISQUES
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToDecisions() {
        currentPage = "decisions";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/banque/banque-decision-list.fxml");
    }

    @FXML
    public void goToRisques() {
        currentPage = "risques";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/banque/banque-risque-list.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - DONNÉES SATELLITE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToSatellite() {
        currentPage = "satellite";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/banque/banque-satellite.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - IoT & RAPPORTS
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToCapteurs() {
        currentPage = "capteurs";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/capteur.fxml");
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

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - COMMUNICATION
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToMessagerie() {
        currentPage = "messagerie";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/banque/banque-messagerie.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - OUTILS
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToCarte() {
        currentPage = "carte";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/view/MapView.fxml");
    }

    @FXML
    public void goToChatbot() {
        currentPage = "chatbot";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/view/ChatbotView.fxml");
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION - COMPTE
    // ══════════════════════════════════════════════════════════

    @FXML
    public void goToSecurity() {
        currentPage = "security";
        updateActiveButton();
        Main.navigateTo("/com/agrifund/fxml/banque/banque-security.fxml");
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