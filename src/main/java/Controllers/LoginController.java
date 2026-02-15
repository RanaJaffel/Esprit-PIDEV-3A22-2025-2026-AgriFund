package Controllers;

import Services.AuthService;
import Services.TwoFactorAuthService;
import Utils.SessionManager;
import entities.Utilisateur;
import entities.Code2FA;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMe;
    @FXML private HBox alertBox;
    @FXML private Label alertMessage;

    private AuthService authService;
    private TwoFactorAuthService twoFAService;

    @FXML
    public void initialize() {
        try {
            authService = new AuthService();
            twoFAService = new TwoFactorAuthService();
        } catch (SQLException e) {
            showAlert("Erreur de connexion à la base de données", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        try {
            Utilisateur user = authService.login(email, password);

            if (user != null) {
                // Vérifier si 2FA est activée
                if (twoFAService.is2FAActive(user.getId())) {
                    handle2FA(user);
                } else {
                    // Connexion directe
                    completeLogin(user);
                }
            } else {
                showError("Email ou mot de passe incorrect");
            }

        } catch (SQLException e) {
            showError("Erreur lors de la connexion: " + e.getMessage());
        }
    }

    private void handle2FA(Utilisateur user) throws SQLException {
        // Envoyer le code 2FA
        Code2FA code = twoFAService.envoyerCode2FA(user.getId());

        if (code != null) {
            // Afficher le dialogue pour entrer le code
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Authentification à Deux Facteurs");
            dialog.setHeaderText("🔐 Code de vérification envoyé");
            dialog.setContentText("Entrez le code reçu par email:");

            // Style du dialogue
            dialog.getDialogPane().getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm()
            );

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                String codeEntre = result.get();

                if (twoFAService.verifierCode(user.getId(), codeEntre)) {
                    twoFAService.enregistrerConnexion(user.getId(), true, "2fa");
                    completeLogin(user);
                } else {
                    twoFAService.enregistrerConnexion(user.getId(), false, "2fa");
                    showError("Code incorrect ou expiré");
                }
            }
        } else {
            showError("Erreur lors de l'envoi du code 2FA");
        }
    }

    private void completeLogin(Utilisateur user) throws SQLException {
        // Déterminer le type d'utilisateur
        String typeUtilisateur = authService.getTypeUtilisateur(user.getId());

        // Sauvegarder la session
        SessionManager session = SessionManager.getInstance();
        session.setUtilisateurConnecte(user);
        session.setTypeUtilisateur(typeUtilisateur);

        // Enregistrer la connexion
        twoFAService.enregistrerConnexion(user.getId(), true, "password");

        // Rediriger vers le bon dashboard
        String dashboard;
        switch (typeUtilisateur) {
            case "ADMIN":
                dashboard = "admin/admin-dashboard";
                break;
            case "AGRICULTEUR":
                dashboard = "agriculteur/agriculteur-dashboard";
                break;
            case "BANQUE":
                dashboard = "banque/banque-dashboard";
                break;
            default:
                showError("Type d'utilisateur inconnu");
                return;
        }

        loadPage(dashboard);
    }

    @FXML
    public void goToForgotPassword() {
        loadPage("forgot-password");
    }

    @FXML
    public void goToRegisterAgriculteur() {
        loadPage("register-agriculteur");
    }

    @FXML
    public void goToRegisterBanque() {
        loadPage("register-banque");
    }

    @FXML
    public void goToHome() {
        loadPage("home");
    }

    private void showError(String message) {
        alertBox.setVisible(true);
        alertBox.setManaged(true);
        alertMessage.setText(message);
    }

    private void hideError() {
        alertBox.setVisible(false);
        alertBox.setManaged(false);
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadPage(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fxmlFile + ".fxml"));
            Stage stage = MainApp.getPrimaryStage();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement de la page", Alert.AlertType.ERROR);
        }
    }
}