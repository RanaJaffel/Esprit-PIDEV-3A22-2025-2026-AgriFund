package com.agrifund;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/view/ProduitFinancierView.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 700);

            primaryStage.setTitle("🌾 AgriFund - Gestion Produits Financiers");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();

            System.out.println("✅ Application lancée avec succès!");

        } catch (Exception e) {
            System.err.println("❌ ERREUR:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}