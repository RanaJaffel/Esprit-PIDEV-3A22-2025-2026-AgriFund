package Controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import Utils.DatabaseConnection;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Tester la connexion à la base de données
        DatabaseConnection.testConnection();

        // Charger la page d'accueil
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/home.fxml"));

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        stage.setTitle("AgriFund - Plateforme de Financement Agricole");
        stage.setScene(scene);
        stage.setMinWidth(1024);
        stage.setMinHeight(600);
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setRoot(String fxml) throws Exception {
        Parent root = FXMLLoader.load(MainApp.class.getResource("/fxml/" + fxml + ".fxml"));
        primaryStage.getScene().setRoot(root);
    }

    public static void main(String[] args) {
        launch(args);
    }
}