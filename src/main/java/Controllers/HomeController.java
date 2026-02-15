package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class HomeController {

    @FXML
    public void goToLogin() {
        loadPage("login");
    }

    @FXML
    public void goToAbout() {
        loadPage("about");
    }

    @FXML
    public void goToAgriculteurSpace() {
        loadPage("login");
    }

    @FXML
    public void goToBanqueSpace() {
        loadPage("login");
    }

    @FXML
    public void showRegisterOptions() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Inscription");
        alert.setHeaderText("Choisissez votre type de compte");
        alert.setContentText("Vous souhaitez vous inscrire en tant que :");

        ButtonType btnAgriculteur = new ButtonType("🌾 Agriculteur");
        ButtonType btnBanque = new ButtonType("🏦 Banque");
        ButtonType btnAnnuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnAgriculteur, btnBanque, btnAnnuler);

        // Appliquer le style
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm()
        );

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == btnAgriculteur) {
                loadPage("register-agriculteur");
            } else if (result.get() == btnBanque) {
                loadPage("register-banque");
            }
        }
    }

    private void loadPage(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fxmlFile + ".fxml"));
            Stage stage = MainApp.getPrimaryStage();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la page: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}