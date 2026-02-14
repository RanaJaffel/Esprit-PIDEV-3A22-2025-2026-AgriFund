package com.agrifund;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    private VBox contentArea;
    private Button btnProduits;
    private Button btnOffres;

    private String activeStyle = "-fx-background-color: linear-gradient(to bottom, #B2D944, #476C1A);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 10 30 10 30;" +
            "-fx-cursor: hand;";

    private String inactiveStyle = "-fx-background-color: linear-gradient(to bottom, #076A39, #095032);" +
            "-fx-text-fill: #B2D944;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14;" +
            "-fx-background-radius: 20;" +
            "-fx-padding: 10 30 10 30;" +
            "-fx-cursor: hand;";

    @Override
    public void start(Stage primaryStage) {
        try {

            // ==================== NAVBAR ====================
            HBox navbar = new HBox(20);
            navbar.setAlignment(Pos.CENTER);
            navbar.setStyle("-fx-background-color: linear-gradient(to right, #076A39, #089647, #076A39);" +
                    "-fx-padding: 15 30 15 30;");

            DropShadow navShadow = new DropShadow();
            navShadow.setColor(Color.web("#060806"));
            navShadow.setOffsetY(5);
            navShadow.setRadius(15);
            navbar.setEffect(navShadow);

            // Logo
            Label logo = new Label("AgriFund");
            logo.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 24; -fx-font-weight: bold;");

            // Spacer
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);

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
                btnProduits.setStyle(activeStyle);
                btnOffres.setStyle(inactiveStyle);
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
                btnOffres.setStyle(activeStyle);
                btnProduits.setStyle(inactiveStyle);
            });

            navbar.getChildren().addAll(logo, spacer, btnProduits, btnOffres);

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

            System.out.println("Application lancee avec succes!");

        } catch (Exception e) {
            System.err.println("ERREUR:");
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

            System.out.println("Interface chargee: " + fxmlFile);

        } catch (Exception e) {
            System.err.println("Erreur chargement " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}