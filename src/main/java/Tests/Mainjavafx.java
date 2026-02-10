package Tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Mainjavafx extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger le fichier FXML de base (Projets)
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/projectagricole.fxml")));

        // Créer la scène (taille 1000x600 pixels)
        Scene scene = new Scene(root, 1000, 600);

        // Configurer la fenêtre (Stage)
        primaryStage.setTitle("Gestion Agricole - Pidev");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen(); // Centrer la fenêtre
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Lancer l'application JavaFX
        launch(args);
    }
}