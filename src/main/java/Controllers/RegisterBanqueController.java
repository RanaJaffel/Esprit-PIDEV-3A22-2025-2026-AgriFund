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
import java.util.function.UnaryOperator;

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

        // ✅ AJOUT : Contrôle de saisie en temps réel pour le téléphone
        setupPhoneFieldValidation();
    }

    /**
     * Configure le contrôle de saisie pour le champ téléphone
     * Autorise uniquement : chiffres, +, espaces, tirets, parenthèses
     */
    private void setupPhoneFieldValidation() {
        UnaryOperator<TextFormatter.Change> phoneFilter = change -> {
            String newText = change.getControlNewText();

            // Autorise uniquement les caractères valides pour un numéro de téléphone
            if (newText.matches("[+]?[0-9\\s\\-()]*")) {
                return change;
            }
            return null; // Rejette la modification
        };

        telField.setTextFormatter(new TextFormatter<>(phoneFilter));

        // Optionnel : Formater automatiquement le numéro pendant la saisie
        telField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                // Supprimer les espaces pour vérifier la longueur
                String digitsOnly = newValue.replaceAll("[^0-9]", "");

                // Limiter à 12 chiffres maximum (pour +216 XX XXX XXX)
                if (digitsOnly.length() > 12) {
                    telField.setText(oldValue);
                }
            }
        });
    }

    /**
     * Valide le format du numéro de téléphone tunisien
     * Formats acceptés :
     * - +216 XX XXX XXX
     * - 00216 XX XXX XXX
     * - XX XXX XXX (8 chiffres)
     * - XXXXXXXX (8 chiffres sans espaces)
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Le téléphone est optionnel
        }

        // Supprimer tous les caractères non numériques sauf +
        String cleanPhone = phone.replaceAll("[^0-9+]", "");

        // Format international avec +216
        if (cleanPhone.startsWith("+216")) {
            String localNumber = cleanPhone.substring(4);
            return localNumber.length() == 8 && localNumber.matches("[2-9][0-9]{7}");
        }

        // Format avec 00216
        if (cleanPhone.startsWith("00216")) {
            String localNumber = cleanPhone.substring(5);
            return localNumber.length() == 8 && localNumber.matches("[2-9][0-9]{7}");
        }

        // Format local (8 chiffres commençant par 2-9)
        if (cleanPhone.length() == 8) {
            return cleanPhone.matches("[2-9][0-9]{7}");
        }

        return false;
    }

    /**
     * Retourne un message d'erreur détaillé pour le téléphone
     */
    private String getPhoneErrorMessage(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }

        String cleanPhone = phone.replaceAll("[^0-9+]", "");

        if (cleanPhone.startsWith("+") && !cleanPhone.startsWith("+216")) {
            return "Seuls les numéros tunisiens (+216) sont acceptés";
        }

        String localNumber;
        if (cleanPhone.startsWith("+216")) {
            localNumber = cleanPhone.substring(4);
        } else if (cleanPhone.startsWith("00216")) {
            localNumber = cleanPhone.substring(5);
        } else {
            localNumber = cleanPhone;
        }

        if (localNumber.length() < 8) {
            return "Le numéro de téléphone doit contenir 8 chiffres";
        }

        if (localNumber.length() > 8) {
            return "Le numéro de téléphone ne doit pas dépasser 8 chiffres";
        }

        if (!localNumber.matches("[2-9][0-9]{7}")) {
            return "Le numéro doit commencer par 2, 3, 4, 5, 7 ou 9";
        }

        return "Format de téléphone invalide";
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

            // ✅ AJOUT : Formater le téléphone avant de l'enregistrer
            banque.setTel(formatPhoneNumber(telField.getText().trim()));

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

    /**
     * Formate le numéro de téléphone au format standard +216 XX XXX XXX
     */
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }

        String cleanPhone = phone.replaceAll("[^0-9]", "");

        // Si commence par 216, ajouter le +
        if (cleanPhone.startsWith("216") && cleanPhone.length() == 11) {
            cleanPhone = cleanPhone.substring(3);
        }

        // Si 8 chiffres, formater avec +216
        if (cleanPhone.length() == 8) {
            return "+216 " + cleanPhone.substring(0, 2) + " " +
                    cleanPhone.substring(2, 5) + " " + cleanPhone.substring(5);
        }

        return phone; // Retourner tel quel si format non reconnu
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

        // ✅ AJOUT : Validation du téléphone (optionnel mais doit être valide si rempli)
        String phone = telField.getText().trim();
        if (!phone.isEmpty() && !isValidPhoneNumber(phone)) {
            String errorMsg = getPhoneErrorMessage(phone);
            showError(errorMsg != null ? errorMsg : "Le numéro de téléphone n'est pas valide");
            telField.requestFocus();
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