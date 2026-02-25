package com.agrifund;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    private VBox contentArea;
    private Button btnUtilisateur;
    private Button btnProduits;
    private Button btnOffres;
    private Button btnCarte;
    private Button btnVueCarte;
    private Button btnChatbot;

    private String activeStyle = "-fx-background-color: linear-gradient(to bottom, #B2D944, #476C1A);"
            + "-fx-text-fill: white;"
            + "-fx-font-weight: bold;"
            + "-fx-font-size: 14;"
            + "-fx-background-radius: 20;"
            + "-fx-padding: 10 30 10 30;"
            + "-fx-cursor: hand;";

    private String inactiveStyle = "-fx-background-color: linear-gradient(to bottom, #076A39, #095032);"
            + "-fx-text-fill: #B2D944;"
            + "-fx-font-weight: bold;"
            + "-fx-font-size: 14;"
            + "-fx-background-radius: 20;"
            + "-fx-padding: 10 30 10 30;"
            + "-fx-cursor: hand;";

    @Override
    public void start(Stage primaryStage) {
        try {

            // ==================== SET WINDOW ICON ====================
            try {
                Image icon = new Image(getClass().getResourceAsStream("resources/com/agrifund/images/image_2026-01-25_212530957-removebg-preview.png"));
                primaryStage.getIcons().add(icon);
                System.out.println("✅ Logo charge avec succes!");
            } catch (Exception e) {
                System.err.println("⚠️ Logo non trouve: " + e.getMessage());
            }

            // ==================== NAVBAR ====================
            HBox navbar = new HBox(20);
            navbar.setAlignment(Pos.CENTER_LEFT);
            navbar.setStyle("-fx-background-color: linear-gradient(to right, #076A39, #089647, #076A39);"
                    + "-fx-padding: 15 30 15 30;");

            DropShadow navShadow = new DropShadow();
            navShadow.setColor(Color.web("#060806"));
            navShadow.setOffsetY(5);
            navShadow.setRadius(15);
            navbar.setEffect(navShadow);

            // ==================== LOGO IN NAVBAR ====================
            HBox logoBox = new HBox(10);
            logoBox.setAlignment(Pos.CENTER_LEFT);

            try {
                Image logoImage = new Image(getClass().getResourceAsStream("/com/agrifund/images/image_2026-01-25_212530957-removebg-preview.png"));
                ImageView logoView = new ImageView(logoImage);
                logoView.setFitHeight(40);
                logoView.setFitWidth(40);
                logoView.setPreserveRatio(true);
                logoBox.getChildren().add(logoView);
            } catch (Exception e) {
                System.err.println("⚠️ Logo pour navbar non charge");
            }

            Label logo = new Label("AgriFund");
            logo.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 24; -fx-font-weight: bold;");
            logoBox.getChildren().add(logo);

            // Spacer
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Bouton Utilisateur (simplified user interface)
            btnUtilisateur = new Button("👤 Mon Espace");
            btnUtilisateur.setStyle(inactiveStyle);
            DropShadow btnShadow0 = new DropShadow();
            btnShadow0.setColor(Color.web("#060806"));
            btnShadow0.setOffsetY(3);
            btnShadow0.setRadius(8);
            btnUtilisateur.setEffect(btnShadow0);
            btnUtilisateur.setOnAction(e -> {
                chargerInterface("AccueilUtilisateurView.fxml");
                btnUtilisateur.setStyle(activeStyle);
                btnProduits.setStyle(inactiveStyle);
                btnOffres.setStyle(inactiveStyle);
                btnCarte.setStyle(inactiveStyle);
                btnVueCarte.setStyle(inactiveStyle);
                btnChatbot.setStyle(inactiveStyle);
            });

            // Bouton Produits
            btnProduits = new Button("Produits Financiers");
            btnProduits.setStyle(activeStyle);
            DropShadow btnShadow1 = new DropShadow();
            btnShadow1.setColor(Color.web("#060806"));
            btnShadow1.setOffsetY(3);
            btnShadow1.setRadius(8);
            btnProduits.setEffect(btnShadow1);
            btnProduits.setOnAction(e -> {
                chargerInterface("ProduitFinancierView.fxml");
                btnUtilisateur.setStyle(inactiveStyle);
                btnProduits.setStyle(activeStyle);
                btnOffres.setStyle(inactiveStyle);
                btnCarte.setStyle(inactiveStyle);
                btnVueCarte.setStyle(inactiveStyle);
                btnChatbot.setStyle(inactiveStyle);
            });

            // Bouton Offres
            btnOffres = new Button("Offres Financieres");
            btnOffres.setStyle(inactiveStyle);
            DropShadow btnShadow2 = new DropShadow();
            btnShadow2.setColor(Color.web("#060806"));
            btnShadow2.setOffsetY(3);
            btnShadow2.setRadius(8);
            btnOffres.setEffect(btnShadow2);
            btnOffres.setOnAction(e -> {
                chargerInterface("OffreFinanciereView.fxml");
                btnUtilisateur.setStyle(inactiveStyle);
                btnProduits.setStyle(inactiveStyle);
                btnOffres.setStyle(activeStyle);
                btnCarte.setStyle(inactiveStyle);
                btnVueCarte.setStyle(inactiveStyle);
                btnChatbot.setStyle(inactiveStyle);
            });

            // Bouton Carte
            btnCarte = new Button("Carte");
            btnCarte.setStyle(inactiveStyle);
            DropShadow btnShadow3 = new DropShadow();
            btnShadow3.setColor(Color.web("#060806"));
            btnShadow3.setOffsetY(3);
            btnShadow3.setRadius(8);
            btnCarte.setEffect(btnShadow3);
            btnCarte.setOnAction(e -> {
                chargerInterface("MapView.fxml");
                btnUtilisateur.setStyle(inactiveStyle);
                btnProduits.setStyle(inactiveStyle);
                btnOffres.setStyle(inactiveStyle);
                btnCarte.setStyle(activeStyle);
                btnVueCarte.setStyle(inactiveStyle);
                btnChatbot.setStyle(inactiveStyle);
            });

            // Bouton Vue Carte (simple design map)
            btnVueCarte = new Button("\uD83C\uDF0D Vue Carte");
            btnVueCarte.setStyle(inactiveStyle);
            DropShadow btnShadow5 = new DropShadow();
            btnShadow5.setColor(Color.web("#060806"));
            btnShadow5.setOffsetY(3);
            btnShadow5.setRadius(8);
            btnVueCarte.setEffect(btnShadow5);
            btnVueCarte.setOnAction(e -> {
                chargerInterface("SimpleMapView.fxml");
                btnUtilisateur.setStyle(inactiveStyle);
                btnProduits.setStyle(inactiveStyle);
                btnOffres.setStyle(inactiveStyle);
                btnCarte.setStyle(inactiveStyle);
                btnVueCarte.setStyle(activeStyle);
                btnChatbot.setStyle(inactiveStyle);
            });

            // Bouton Chatbot
            btnChatbot = new Button("🤖 Chatbot");
            btnChatbot.setStyle(inactiveStyle);
            DropShadow btnShadow4 = new DropShadow();
            btnShadow4.setColor(Color.web("#060806"));
            btnShadow4.setOffsetY(3);
            btnShadow4.setRadius(8);
            btnChatbot.setEffect(btnShadow4);
            btnChatbot.setOnAction(e -> {
                chargerInterface("ChatbotView.fxml");
                btnUtilisateur.setStyle(inactiveStyle);
                btnProduits.setStyle(inactiveStyle);
                btnOffres.setStyle(inactiveStyle);
                btnCarte.setStyle(inactiveStyle);
                btnVueCarte.setStyle(inactiveStyle);
                btnChatbot.setStyle(activeStyle);
            });

            navbar.getChildren().addAll(logoBox, spacer, btnUtilisateur, btnProduits, btnOffres, btnCarte, btnVueCarte, btnChatbot);
            // ==================== CONTENT AREA ====================
            contentArea = new VBox();
            contentArea.setStyle("-fx-background-color: #095032;");
            VBox.setVgrow(contentArea, Priority.ALWAYS);

            // ==================== MAIN LAYOUT ====================
            VBox mainLayout = new VBox();
            mainLayout.setStyle("-fx-background-color: #095032;");
            mainLayout.getChildren().addAll(navbar, contentArea);

            // Charger Produits par defaut
            chargerInterface("ProduitFinancierView.fxml");

            Scene scene = new Scene(mainLayout, 1200, 800);

            primaryStage.setTitle("AgriFund - Plateforme de Financement Agricole");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.show();

            System.out.println("✅ Application lancee avec succes!");

        } catch (Exception e) {
            System.err.println("❌ ERREUR:");
            e.printStackTrace();
        }
    }

    private void chargerInterface(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/view/" + fxmlFile)
            );
            Parent root = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
            VBox.setVgrow(root, Priority.ALWAYS);

            System.out.println("✅ Interface chargee: " + fxmlFile);

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
