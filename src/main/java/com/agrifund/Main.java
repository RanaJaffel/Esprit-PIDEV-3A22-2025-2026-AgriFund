package com.agrifund;

import java.io.File;

import com.agrifund.entities.Utilisateur;
import com.agrifund.util.DatabaseConnection;
import com.agrifund.util.SessionManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;
    private static StackPane contentArea;
    private static BorderPane mainLayout;
    private static Button activeButton;

    // ── Styles ──
    private static final String SIDEBAR_BG_PUBLIC = "-fx-background-color: linear-gradient(to bottom, #076A39, #064E2B);";
    private static final String SIDEBAR_BG_ADMIN  = "-fx-background-color: linear-gradient(to bottom, #076A39, #064E2B);";
    private static final String SIDEBAR_BG_AGRICULTEUR = "-fx-background-color: linear-gradient(to bottom, #076A39, #064E2B);";
    private static final String SIDEBAR_BG_BANQUE = "-fx-background-color: linear-gradient(to bottom, #076A39, #064E2B);";

    private static final String BTN_NORMAL =
            "-fx-background-color: transparent; -fx-text-fill: #C8E6C9; -fx-font-size: 13px; "
                    + "-fx-padding: 10 20; -fx-alignment: CENTER-LEFT; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String BTN_ACTIVE =
            "-fx-background-color: rgba(178,217,68,0.25); -fx-text-fill: #B2D944; -fx-font-size: 13px; "
                    + "-fx-padding: 10 20; -fx-alignment: CENTER-LEFT; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;";
    private static final String BTN_HOVER =
            "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; -fx-font-size: 13px; "
                    + "-fx-padding: 10 20; -fx-alignment: CENTER-LEFT; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String SECTION_LABEL =
            "-fx-text-fill: #81C784; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 15 20 5 20;";
    private static final String BTN_LOGOUT =
            "-fx-background-color: transparent; -fx-text-fill: #ff6b6b; -fx-font-size: 13px; "
                    + "-fx-padding: 10 20; -fx-alignment: CENTER-LEFT; -fx-background-radius: 8; -fx-cursor: hand;";

    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;

            // Test database connection
            DatabaseConnection.testConnection();

            // Set window icon
            try {
                Image icon = new Image(getClass().getResourceAsStream(
                        "/com/agrifund/images/image_2026-01-25_212530957-removebg-preview.png"));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("Logo non trouve: " + e.getMessage());
            }

            // ── Content area ──
            contentArea = new StackPane();
            contentArea.setStyle("-fx-background-color: #F5F5F5;");

            // ── Main layout ──
            mainLayout = new BorderPane();
            mainLayout.setCenter(contentArea);

            // Show public sidebar (before login)
            showPublicSidebar();

            // Load default view
            loadView("/com/agrifund/fxml/home.fxml");

            Scene scene = new Scene(mainLayout, 1400, 800);
            try {
                scene.getStylesheets().add(
                        getClass().getResource("/com/agrifund/css/styles.css").toExternalForm());
            } catch (Exception e) {
                System.err.println("CSS non trouve: " + e.getMessage());
            }

            stage.setTitle("AgriFund - Plateforme de Financement Agricole");
            stage.setScene(scene);
            stage.setMinWidth(1100);
            stage.setMinHeight(700);
            stage.show();

            System.out.println("Application lancee avec succes!");

        } catch (Exception e) {
            System.err.println("ERREUR au demarrage:");
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PUBLIC SIDEBAR (before login)
    // ═══════════════════════════════════════════════════════════
    private static void showPublicSidebar() {
        activeButton = null;
        mainLayout.setLeft(null);
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN SIDEBAR (after admin login)
    // ═══════════════════════════════════════════════════════════
    private static void showAdminSidebar() {
        activeButton = null;
        Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
        VBox sidebar = buildSidebarShell(SIDEBAR_BG_ADMIN, "Administration", user);
        VBox navItems = new VBox(2);
        navItems.setPadding(new Insets(5, 10, 10, 10));

        // -- Principal --
        navItems.getChildren().add(sectionLabel("PRINCIPAL"));
        navItems.getChildren().add(navButton("📊  Tableau de bord", "/com/agrifund/fxml/admin/admin-dashboard.fxml"));
        navItems.getChildren().add(navButton("👥  Utilisateurs", "/com/agrifund/fxml/admin/admin-users.fxml"));
        navItems.getChildren().add(navButton("🌾  Agriculteurs", "/com/agrifund/fxml/admin/admin-agriculteurs.fxml"));
        navItems.getChildren().add(navButton("🏦  Banques", "/com/agrifund/fxml/admin/admin-banques.fxml"));

        // -- Gestion --
        navItems.getChildren().add(sectionLabel("GESTION"));
        navItems.getChildren().add(navButton("📄  Documents", "/com/agrifund/fxml/admin/admin-documents.fxml"));
        navItems.getChildren().add(navButton("💬  Messagerie", "/com/agrifund/fxml/admin/admin-messagerie.fxml"));

        // -- Finance --
        navItems.getChildren().add(sectionLabel("FINANCE"));
        navItems.getChildren().add(navButton("💰  Produits Financiers", "/com/agrifund/view/ProduitFinancierView.fxml"));
        navItems.getChildren().add(navButton("📋  Offres Financières", "/com/agrifund/view/OffreFinanciereView.fxml"));

        // -- Agriculture --
        navItems.getChildren().add(sectionLabel("AGRICULTURE"));
        navItems.getChildren().add(navButton("🌾  Projets Agricoles", "/com/agrifund/fxml/projectagricole.fxml"));
        navItems.getChildren().add(navButton("🌿  Ressources", "/com/agrifund/fxml/ressourceproject.fxml"));

        // -- Decisions --
        navItems.getChildren().add(sectionLabel("DÉCISIONS & RISQUES"));
        navItems.getChildren().add(navButton("⚖  Décisions", "/com/agrifund/fxml/DecisionList.fxml"));
        navItems.getChildren().add(navButton("📊  Risques", "/com/agrifund/fxml/RisqueList.fxml"));

        // -- IoT --
        navItems.getChildren().add(sectionLabel("IoT & RAPPORTS"));
        navItems.getChildren().add(navButton("📡  Capteurs", "/com/agrifund/fxml/capteur.fxml"));
        navItems.getChildren().add(navButton("📈  Dashboard IoT", "/com/agrifund/fxml/dashboard-rana.fxml"));
        navItems.getChildren().add(navButton("📝  Rapports", "/com/agrifund/fxml/rapport.fxml"));

        // -- Compte --
        navItems.getChildren().add(sectionLabel("COMPTE"));
        navItems.getChildren().add(navButton("👤  Mon profil", "/com/agrifund/fxml/admin/admin-profile.fxml"));
        navItems.getChildren().add(navButton("🔐  Sécurité", "/com/agrifund/fxml/admin/admin-security.fxml"));
        navItems.getChildren().add(logoutButton());

        addScrollableNav(sidebar, navItems);
        mainLayout.setLeft(sidebar);
    }

    // ═══════════════════════════════════════════════════════════
    // AGRICULTEUR SIDEBAR (after agriculteur login)
    // ═══════════════════════════════════════════════════════════
    private static void showAgriculteurSidebar() {
        activeButton = null;
        Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
        VBox sidebar = buildSidebarShell(SIDEBAR_BG_AGRICULTEUR, "Espace Agriculteur", user);
        VBox navItems = new VBox(2);
        navItems.setPadding(new Insets(5, 10, 10, 10));

        // -- Principal --
        navItems.getChildren().add(sectionLabel("PRINCIPAL"));
        navItems.getChildren().add(navButton("📊  Tableau de bord", "/com/agrifund/fxml/agriculteur/agriculteur-dashboard.fxml"));
        navItems.getChildren().add(navButton("👤  Mon profil", "/com/agrifund/fxml/agriculteur/agriculteur-profile.fxml"));
        navItems.getChildren().add(navButton("📄  Mes documents", "/com/agrifund/fxml/agriculteur/agriculteur-documents.fxml"));

        // -- Finance --
        navItems.getChildren().add(sectionLabel("FINANCE"));
        navItems.getChildren().add(navButton("💰  Produits Financiers", "/com/agrifund/view/ProduitFinancierView.fxml"));
        navItems.getChildren().add(navButton("📋  Offres Financières", "/com/agrifund/view/OffreFinanciereView.fxml"));
        navItems.getChildren().add(navButton("💳  Demande Financement", "/com/agrifund/view/DemandeFinancementView.fxml"));
        navItems.getChildren().add(navButton("🏪  Accueil Produits", "/com/agrifund/view/AccueilUtilisateurView.fxml"));

        // -- Agriculture --
        navItems.getChildren().add(sectionLabel("AGRICULTURE"));
        navItems.getChildren().add(navButton("🌾  Projets Agricoles", "/com/agrifund/fxml/projectagricole.fxml"));
        navItems.getChildren().add(navButton("🌿  Ressources", "/com/agrifund/fxml/ressourceproject.fxml"));

        // -- Decisions --
        navItems.getChildren().add(sectionLabel("DÉCISIONS & RISQUES"));
        navItems.getChildren().add(navButton("⚖  Décisions", "/com/agrifund/fxml/DecisionList.fxml"));
        navItems.getChildren().add(navButton("📊  Risques", "/com/agrifund/fxml/RisqueList.fxml"));

        // -- IoT --
        navItems.getChildren().add(sectionLabel("IoT & RAPPORTS"));
        navItems.getChildren().add(navButton("📡  Capteurs", "/com/agrifund/fxml/capteur.fxml"));
        navItems.getChildren().add(navButton("📈  Dashboard IoT", "/com/agrifund/fxml/dashboard-rana.fxml"));
        navItems.getChildren().add(navButton("📝  Rapports", "/com/agrifund/fxml/rapport.fxml"));

        // -- Communication --
        navItems.getChildren().add(sectionLabel("COMMUNICATION"));
        navItems.getChildren().add(navButton("💬  Messagerie", "/com/agrifund/fxml/agriculteur/agriculteur-messagerie.fxml"));

        // -- Outils --
        navItems.getChildren().add(sectionLabel("OUTILS"));
        navItems.getChildren().add(navButton("🗺  Carte", "/com/agrifund/view/MapView.fxml"));
        navItems.getChildren().add(navButton("🤖  Chatbot", "/com/agrifund/view/ChatbotView.fxml"));

        // -- Compte --
        navItems.getChildren().add(sectionLabel("COMPTE"));
        navItems.getChildren().add(navButton("🔐  Sécurité", "/com/agrifund/fxml/agriculteur/agriculteur-security.fxml"));
        navItems.getChildren().add(logoutButton());

        addScrollableNav(sidebar, navItems);
        mainLayout.setLeft(sidebar);
    }

    // ═══════════════════════════════════════════════════════════
    // BANQUE SIDEBAR (after banque login)
    // ═══════════════════════════════════════════════════════════
    private static void showBanqueSidebar() {
        activeButton = null;
        Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
        VBox sidebar = buildSidebarShell(SIDEBAR_BG_BANQUE, "Espace Banque", user);
        VBox navItems = new VBox(2);
        navItems.setPadding(new Insets(5, 10, 10, 10));

        // -- Principal --
        navItems.getChildren().add(sectionLabel("PRINCIPAL"));
        navItems.getChildren().add(navButton("📊  Tableau de bord", "/com/agrifund/fxml/banque/banque-dashboard.fxml"));
        navItems.getChildren().add(navButton("🏦  Mon profil", "/com/agrifund/fxml/banque/banque-profile.fxml"));
        navItems.getChildren().add(navButton("📄  Mes documents", "/com/agrifund/fxml/banque/banque-documents.fxml"));

        // -- Finance --
        navItems.getChildren().add(sectionLabel("FINANCE"));
        navItems.getChildren().add(navButton("💰  Produits Financiers", "/com/agrifund/view/ProduitFinancierView.fxml"));
        navItems.getChildren().add(navButton("📋  Offres Financières", "/com/agrifund/view/OffreFinanciereView.fxml"));
        navItems.getChildren().add(navButton("💳  Demande Financement", "/com/agrifund/view/DemandeFinancementView.fxml"));

        // -- Agriculture --
        navItems.getChildren().add(sectionLabel("AGRICULTURE"));
        navItems.getChildren().add(navButton("🌾  Projets Agricoles", "/com/agrifund/fxml/projectagricole.fxml"));
        navItems.getChildren().add(navButton("🌿  Ressources", "/com/agrifund/fxml/ressourceproject.fxml"));

        // -- Decisions --
        navItems.getChildren().add(sectionLabel("DÉCISIONS & RISQUES"));
        navItems.getChildren().add(navButton("⚖  Décisions", "/com/agrifund/fxml/DecisionList.fxml"));
        navItems.getChildren().add(navButton("📊  Risques", "/com/agrifund/fxml/RisqueList.fxml"));

        // -- Satellite --
        navItems.getChildren().add(sectionLabel("DONNÉES SATELLITE"));
        navItems.getChildren().add(navButton("🛰  Satellite", "/com/agrifund/fxml/banque/banque-satellite.fxml"));

        // -- IoT --
        navItems.getChildren().add(sectionLabel("IoT & RAPPORTS"));
        navItems.getChildren().add(navButton("📡  Capteurs", "/com/agrifund/fxml/capteur.fxml"));
        navItems.getChildren().add(navButton("📈  Dashboard IoT", "/com/agrifund/fxml/dashboard-rana.fxml"));
        navItems.getChildren().add(navButton("📝  Rapports", "/com/agrifund/fxml/rapport.fxml"));

        // -- Communication --
        navItems.getChildren().add(sectionLabel("COMMUNICATION"));
        navItems.getChildren().add(navButton("💬  Messagerie", "/com/agrifund/fxml/banque/banque-messagerie.fxml"));

        // -- Outils --
        navItems.getChildren().add(sectionLabel("OUTILS"));
        navItems.getChildren().add(navButton("🗺  Carte", "/com/agrifund/view/MapView.fxml"));
        navItems.getChildren().add(navButton("🤖  Chatbot", "/com/agrifund/view/ChatbotView.fxml"));

        // -- Compte --
        navItems.getChildren().add(sectionLabel("COMPTE"));
        navItems.getChildren().add(navButton("🔐  Sécurité", "/com/agrifund/fxml/banque/banque-security.fxml"));
        navItems.getChildren().add(logoutButton());

        addScrollableNav(sidebar, navItems);
        mainLayout.setLeft(sidebar);
    }

    // ═══════════════════════════════════════════════════════════
    // COMMON SIDEBAR BUILDING HELPERS
    // ═══════════════════════════════════════════════════════════

    private static VBox buildSidebarShell(String bgStyle, String title, Utilisateur user) {
        VBox sidebarRoot = new VBox();
        sidebarRoot.setPrefWidth(250);
        sidebarRoot.setMinWidth(250);
        sidebarRoot.setStyle(bgStyle);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setOffsetX(3);
        shadow.setRadius(10);
        sidebarRoot.setEffect(shadow);

        // ── Logo header ──
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPadding(new Insets(20, 20, 10, 20));

        VBox titleBox = new VBox(2);
        Label logoLabel = new Label("AgriFund");
        logoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        titleBox.getChildren().add(logoLabel);

        if (title != null && !title.isEmpty()) {
            Label subtitleLabel = new Label(title);
            subtitleLabel.setStyle("-fx-text-fill: #B2D944; -fx-font-size: 11px;");
            titleBox.getChildren().add(subtitleLabel);
        }

        logoBox.getChildren().add(titleBox);
        sidebarRoot.getChildren().add(logoBox);

        // ── User info (if logged in) ──
        if (user != null) {
            sidebarRoot.getChildren().add(createSeparator());

            VBox userBox = new VBox(5);
            userBox.setAlignment(Pos.CENTER);
            userBox.setPadding(new Insets(10, 20, 10, 20));

            ImageView avatar = new ImageView();
            avatar.setFitWidth(50);
            avatar.setFitHeight(50);
            avatar.setPreserveRatio(true);

            Image img = null;
            String photo = user.getPhoto();
            if (photo != null && !photo.isEmpty()) {
                File f = new File(photo);
                if (f.exists()) img = new Image(f.toURI().toString());
            }
            if (img == null) {
                try {
                    img = new Image(Main.class.getResourceAsStream("/com/agrifund/images/default-avatar.png"));
                } catch (Exception ignored) {}
            }
            if (img != null) {
                avatar.setImage(img);
                Circle clip = new Circle(25, 25, 25);
                avatar.setClip(clip);
            }

            Label nameLabel = new Label(user.getPrenom() + " " + user.getNom());
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

            String role = SessionManager.getInstance().getTypeUtilisateur();
            Label roleLabel = new Label(role != null ? role : "");
            roleLabel.setStyle("-fx-text-fill: #B2D944; -fx-font-size: 10px;");

            userBox.getChildren().addAll(avatar, nameLabel, roleLabel);
            sidebarRoot.getChildren().add(userBox);
        }

        sidebarRoot.getChildren().add(createSeparator());
        return sidebarRoot;
    }

    private static void addScrollableNav(VBox sidebarRoot, VBox navItems) {
        ScrollPane scrollPane = new ScrollPane(navItems);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; "
                + "-fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        sidebarRoot.getChildren().add(scrollPane);
    }

    private static Label sectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle(SECTION_LABEL);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private static Separator createSeparator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.15);");
        sep.setPadding(new Insets(0, 15, 0, 15));
        return sep;
    }

    private static Button navButton(String text, String fxmlPath) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(BTN_NORMAL);

        btn.setOnMouseEntered(e -> {
            if (btn != activeButton) btn.setStyle(BTN_HOVER);
        });
        btn.setOnMouseExited(e -> {
            if (btn != activeButton) btn.setStyle(BTN_NORMAL);
        });

        btn.setOnAction(e -> {
            if (activeButton != null) activeButton.setStyle(BTN_NORMAL);
            btn.setStyle(BTN_ACTIVE);
            activeButton = btn;
            loadView(fxmlPath);
        });

        return btn;
    }

    private static Button logoutButton() {
        Button btn = new Button("🚪  Déconnexion");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(BTN_LOGOUT);

        btn.setOnMouseEntered(e -> btn.setStyle(BTN_LOGOUT + "-fx-background-color: rgba(255,107,107,0.15);"));
        btn.setOnMouseExited(e -> btn.setStyle(BTN_LOGOUT));

        btn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Déconnexion");
            alert.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                handleLogout();
            }
        });

        return btn;
    }

    // ═══════════════════════════════════════════════════════════
    // PUBLIC API — called from controllers
    // ═══════════════════════════════════════════════════════════

    /**
     * Called after successful login to switch the sidebar and load the dashboard.
     */
    public static void onLoginSuccess(String role) {
        Platform.runLater(() -> {
            switch (role) {
                case "ADMIN":
                    showAdminSidebar();
                    loadView("/com/agrifund/fxml/admin/admin-dashboard.fxml");
                    break;
                case "AGRICULTEUR":
                    showAgriculteurSidebar();
                    loadView("/com/agrifund/fxml/agriculteur/agriculteur-dashboard.fxml");
                    break;
                case "BANQUE":
                    showBanqueSidebar();
                    loadView("/com/agrifund/fxml/banque/banque-dashboard.fxml");
                    break;
                default:
                    showPublicSidebar();
                    loadView("/com/agrifund/fxml/home.fxml");
            }
        });
    }

    /**
     * Called on logout to switch back to public sidebar.
     */
    public static void handleLogout() {
        SessionManager.getInstance().deconnecter();
        showPublicSidebar();
        loadView("/com/agrifund/fxml/home.fxml");
    }

    private static void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(Main.class.getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
            System.out.println("Interface chargee: " + fxmlPath);
        } catch (Exception e) {
            System.err.println("Erreur chargement " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();

            // Show error in content area
            Label errorLabel = new Label("Erreur de chargement:\n" + fxmlPath + "\n\n" + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px; -fx-padding: 40;");
            errorLabel.setWrapText(true);
            contentArea.getChildren().setAll(errorLabel);
        }
    }

    /**
     * Load a view into the content area from anywhere.
     * @param fxmlPath absolute resource path, e.g. "/com/agrifund/fxml/login.fxml"
     */
    public static void navigateTo(String fxmlPath) {
        loadView(fxmlPath);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setRoot(String fxml) throws Exception {
        Parent root = FXMLLoader.load(Main.class.getResource("/com/agrifund/fxml/" + fxml + ".fxml"));
        primaryStage.getScene().setRoot(root);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
