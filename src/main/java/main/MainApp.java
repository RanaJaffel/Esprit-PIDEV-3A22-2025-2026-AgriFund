package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Decesion.fxml"));
            Parent root = loader.load();

            // Créer la scène
            Scene scene = new Scene(root);

            // Configuration de la fenêtre
            primaryStage.setTitle("Gestion Agricole Pro - Décisions Financières");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);

            // Afficher la fenêtre
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de l'interface: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        // Fermer la connexion à la base de données lors de la fermeture de l'application
        utils.MyConnection.closeConnection();
        System.out.println("Application fermée - Connexion BDD fermée");
    }

    public static void main(String[] args) {
        launch(args);
    }
}