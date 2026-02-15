package Controllers;

import Services.AuthService;
import entities.Agriculteur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

public class RegisterAgriculteurController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField telField;
    @FXML private TextField adresseFermeField;
    @FXML private TextField superficieField;
    @FXML private ComboBox<String> typeCultureCombo;
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
            // Créer l'agriculteur
            Agriculteur agriculteur = new Agriculteur(
                    nomField.getText().trim(),
                    prenomField.getText().trim(),
                    emailField.getText().trim(),
                    passwordField.getText(),
                    adresseFermeField.getText().trim(),
                    new BigDecimal(superficieField.getText().trim()),
                    typeCultureCombo.getValue()
            );
            agriculteur.setTel(telField.getText().trim());

            // Inscrire
            Agriculteur result = authService.inscrireAgriculteur(agriculteur);

            if (result != null) {
                showSuccess("Compte créé avec succès ! Votre compte est en attente de vérification.");
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

        } catch (NumberFormatException e) {
            showError("La superficie doit être un nombre valide (ex: 10.5)");
        } catch (SQLException e) {
            showError("Erreur lors de l'inscription: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        // Champs obligatoires
        if (nomField.getText().trim().isEmpty()) {
            showError("Le nom est obligatoire");
            nomField.requestFocus();
            return false;
        }

        if (prenomField.getText().trim().isEmpty()) {
            showError("Le prénom est obligatoire");
            prenomField.requestFocus();
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

        if (adresseFermeField.getText().trim().isEmpty()) {
            showError("L'adresse de la ferme est obligatoire");
            adresseFermeField.requestFocus();
            return false;
        }

        if (superficieField.getText().trim().isEmpty()) {
            showError("La superficie est obligatoire");
            superficieField.requestFocus();
            return false;
        }

        // Validation superficie (nombre)
        try {
            double superficie = Double.parseDouble(superficieField.getText().trim());
            if (superficie <= 0) {
                showError("La superficie doit être supérieure à 0");
                superficieField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("La superficie doit être un nombre valide");
            superficieField.requestFocus();
            return false;
        }

        if (typeCultureCombo.getValue() == null) {
            showError("Veuillez sélectionner un type de culture");
            typeCultureCombo.requestFocus();
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
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        telField.clear();
        adresseFermeField.clear();
        superficieField.clear();
        typeCultureCombo.getSelectionModel().clearSelection();
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
    public void goToRegisterBanque() {
        loadPage("register-banque");
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