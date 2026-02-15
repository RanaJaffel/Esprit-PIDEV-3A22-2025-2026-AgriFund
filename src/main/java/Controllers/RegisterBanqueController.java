package Controllers;

import Services.AuthService;
import entities.Banque;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class RegisterBanqueController {

    @FXML private TextField nomBanqueField;
    @FXML private TextField codeBanqueField;
    @FXML private TextField representantField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField adresseSiegeField;
    @FXML private TextField adresseAgenceField;
    @FXML private TextField siteWebField;
    @FXML private TextField telField;
    @FXML private CheckBox termsCheckbox;

    @FXML private HBox alertBox;
    @FXML private Label alertMessage;
    @FXML private HBox successBox;
    @FXML private Label successMessage;

    private AuthService authService;

    @FXML
    public void initialize() {
        try {
            authService = new AuthService();
        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données");
        }
    }

    @FXML
    public void handleRegister() {
        hideMessages();

        // Validation
        if (!validateForm()) {
            return;
        }

        try {
            // Créer la banque
            Banque banque = new Banque(
                    nomBanqueField.getText().trim(),
                    "", // prenom (pas utilisé pour les banques)
                    emailField.getText().trim(),
                    passwordField.getText(),
                    codeBanqueField.getText().trim(),
                    adresseSiegeField.getText().trim(),
                    representantField.getText().trim()
            );

            banque.setAdresseAgence(adresseAgenceField.getText().trim());
            banque.setSiteWeb(siteWebField.getText().trim());
            banque.setTel(telField.getText().trim());

            // Inscrire
            Banque result = authService.inscrireBanque(banque);

            if (result != null) {
                showSuccess("Compte créé avec succès ! Votre compte est en attente de vérification par un administrateur.");
                clearForm();

                // Rediriger vers login après 3 secondes
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        javafx.application.Platform.runLater(() -> goToLogin());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();

            } else {
                showError("Erreur lors de l'inscription. Cet email est peut-être déjà utilisé.");
            }

        } catch (SQLException e) {
            showError("Erreur lors de l'inscription: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        // Champs obligatoires
        if (nomBanqueField.getText().trim().isEmpty()) {
            showError("Le nom de la banque est obligatoire");
            nomBanqueField.requestFocus();
            return false;
        }

        if (codeBanqueField.getText().trim().isEmpty()) {
            showError("Le code banque est obligatoire");
            codeBanqueField.requestFocus();
            return false;
        }

        if (representantField.getText().trim().isEmpty()) {
            showError("Le nom du représentant légal est obligatoire");
            representantField.requestFocus();
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            showError("L'email est obligatoire");
            emailField.requestFocus();
            return false;
        }

        // Validation email
        if (!isValidEmail(emailField.getText().trim())) {
            showError("L'email n'est pas valide");
            emailField.requestFocus();
            return false;
        }

        if (passwordField.getText().isEmpty()) {
            showError("Le mot de passe est obligatoire");
            passwordField.requestFocus();
            return false;
        }

        // Validation mot de passe (min 6 caractères)
        if (passwordField.getText().length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            passwordField.requestFocus();
            return false;
        }

        // Confirmation mot de passe
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Les mots de passe ne correspondent pas");
            confirmPasswordField.requestFocus();
            return false;
        }

        if (adresseSiegeField.getText().trim().isEmpty()) {
            showError("L'adresse du siège est obligatoire");
            adresseSiegeField.requestFocus();
            return false;
        }

        if (!termsCheckbox.isSelected()) {
            showError("Vous devez accepter les conditions d'utilisation");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    private void showError(String message) {
        alertBox.setVisible(true);
        alertBox.setManaged(true);
        alertMessage.setText(message);

        successBox.setVisible(false);
        successBox.setManaged(false);
    }

    private void showSuccess(String message) {
        successBox.setVisible(true);
        successBox.setManaged(true);
        successMessage.setText(message);

        alertBox.setVisible(false);
        alertBox.setManaged(false);
    }

    private void hideMessages() {
        alertBox.setVisible(false);
        alertBox.setManaged(false);
        successBox.setVisible(false);
        successBox.setManaged(false);
    }

    private void clearForm() {
        nomBanqueField.clear();
        codeBanqueField.clear();
        representantField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        adresseSiegeField.clear();
        adresseAgenceField.clear();
        siteWebField.clear();
        telField.clear();
        termsCheckbox.setSelected(false);
    }

    @FXML
    public void goToHome() {
        loadPage("home");
    }

    @FXML
    public void goToLogin() {
        loadPage("login");
    }

    @FXML
    public void goToRegisterAgriculteur() {
        loadPage("register-agriculteur");
    }

    private void loadPage(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fxmlFile + ".fxml"));
            Stage stage = MainApp.getPrimaryStage();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la page");
        }
    }
}