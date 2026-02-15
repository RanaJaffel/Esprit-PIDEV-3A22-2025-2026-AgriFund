package Controllers;

import Services.AuthService;
import Services.UtilisateurService;
import entities.Utilisateur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class ForgotPasswordController {

    @FXML private VBox step1;
    @FXML private VBox step2;
    @FXML private VBox step3;

    @FXML private TextField emailField;
    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private HBox alertBox;
    @FXML private Label alertMessage;
    @FXML private HBox successBox;
    @FXML private Label successMessage;

    private AuthService authService;
    private UtilisateurService utilisateurService;
    private String currentEmail;

    @FXML
    public void initialize() {
        try {
            authService = new AuthService();
            utilisateurService = new UtilisateurService();
        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données");
        }
    }

    @FXML
    public void handleSendCode() {
        hideMessages();

        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError("Veuillez entrer votre adresse email");
            return;
        }

        if (!isValidEmail(email)) {
            showError("L'adresse email n'est pas valide");
            return;
        }

        try {
            // Vérifier si l'email existe
            Utilisateur user = utilisateurService.rechercherParEmail(email);

            if (user == null) {
                showError("Aucun compte associé à cet email");
                return;
            }

            // Envoyer le code
            String token = authService.demanderReinitialisationMotDePasse(email);

            if (token != null) {
                currentEmail = email;
                showSuccess("Un code de réinitialisation a été envoyé à " + email);

                // Passer à l'étape 2
                step1.setVisible(false);
                step1.setManaged(false);
                step2.setVisible(true);
                step2.setManaged(true);
            } else {
                showError("Erreur lors de l'envoi du code");
            }

        } catch (SQLException e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    public void handleResendCode() {
        if (currentEmail != null && !currentEmail.isEmpty()) {
            emailField.setText(currentEmail);
            handleSendCode();
        }
    }

    @FXML
    public void handleResetPassword() {
        hideMessages();

        String token = tokenField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (token.isEmpty()) {
            showError("Veuillez entrer le code de réinitialisation");
            tokenField.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            showError("Veuillez entrer votre nouveau mot de passe");
            newPasswordField.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            newPasswordField.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            confirmPasswordField.requestFocus();
            return;
        }

        try {
            boolean success = authService.reinitialiserMotDePasse(token, newPassword);

            if (success) {
                // Passer à l'étape 3
                step2.setVisible(false);
                step2.setManaged(false);
                step3.setVisible(true);
                step3.setManaged(true);

                hideMessages();
            } else {
                showError("Code invalide ou expiré. Veuillez réessayer.");
            }

        } catch (SQLException e) {
            showError("Erreur: " + e.getMessage());
        }
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

    @FXML
    public void goToLogin() {
        loadPage("login");
    }

    @FXML
    public void goToHome() {
        loadPage("home");
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