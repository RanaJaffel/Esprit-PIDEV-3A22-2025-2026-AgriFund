package com.agrifund;

import com.agrifund.util.DatabaseConnection;
import com.agrifund.util.SessionManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main {

    private static Stage primaryStage;
    private static StackPane contentArea;
    private static BorderPane mainLayout;

    public static void main(String[] args) {
        Application.launch(AgriFundApp.class, args);
    }

    static void start(Stage stage) {
        try {
            primaryStage = stage;

            // Test database connection
            DatabaseConnection.testConnection();

            // Set window icon
            try {
                Image icon = new Image(Main.class.getResourceAsStream(
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

            // Load default view
            loadView("/com/agrifund/fxml/home.fxml");

            Scene scene = new Scene(mainLayout, 1400, 800);
            try {
                scene.getStylesheets().add(
                        Main.class.getResource("/com/agrifund/css/styles.css").toExternalForm());
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
    // PUBLIC API — called from controllers
    // ═══════════════════════════════════════════════════════════
    /**
     * Called after successful login to load the appropriate dashboard.
     */
    public static void onLoginSuccess(String role) {
        Platform.runLater(() -> {
            switch (role) {
                case "ADMIN":
                    loadView("/com/agrifund/fxml/admin/admin-dashboard.fxml");
                    break;
                case "AGRICULTEUR":
                    loadView("/com/agrifund/fxml/agriculteur/agriculteur-dashboard.fxml");
                    break;
                case "BANQUE":
                    loadView("/com/agrifund/fxml/banque/banque-dashboard.fxml");
                    break;
                default:
                    loadView("/com/agrifund/fxml/home.fxml");
            }
        });
    }

    /**
     * Called on logout to return to home.
     */
    public static void handleLogout() {
        SessionManager.getInstance().deconnecter();
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
     *
     * @param fxmlPath absolute resource path, e.g.
     * "/com/agrifund/fxml/login.fxml"
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
}
