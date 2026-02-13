package tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class mainjavafx extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("🌾 AGRIFUND SMART - MODULE IoT");
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.println("📦 Chargement de l'application...");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/MainLayout.fxml")
            );
            Parent root = loader.load();


            Scene scene = new Scene(root, 1400, 850);


            try {
                scene.getStylesheets().add(
                        getClass().getResource("/style.css").toExternalForm()
                );
                System.out.println("✅ CSS chargé");
            } catch (Exception e) {
                System.out.println("⚠️ style.css non trouvé (optionnel)");
            }


            primaryStage.setTitle("AgriFund Smart – Module IoT 🌾");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(700);
            primaryStage.centerOnScreen();
            primaryStage.show();

            System.out.println("✅ Application lancée avec succès !");
            System.out.println("═══════════════════════════════════════════════════════");

        } catch (Exception e) {
            System.err.println("❌ ERREUR AU DÉMARRAGE");
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de démarrage");
            alert.setHeaderText("Impossible de lancer l'application");
            alert.setContentText(
                    "Vérifiez :\n" +
                            "• MainLayout.fxml dans resources\n" +
                            "• style.css dans resources\n" +
                            "• Connexion MySQL\n" +
                            "• JavaFX configuré\n\n" +
                            "Détail : " + e.getMessage()
            );
            alert.showAndWait();

            System.exit(1);
        }
    }

    @Override
    public void stop() {
        System.out.println("👋 Fermeture d'AgriFund Smart");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
