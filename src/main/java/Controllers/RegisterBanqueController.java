package Controllers;

import Services.AuthService;
import entities.Banque;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

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

    @FXML private Label telErrorLabel;
    @FXML private VBox formCard;

    // Input containers pour gestion du focus
    @FXML private HBox nomBanqueContainer;
    @FXML private HBox codeBanqueContainer;
    @FXML private HBox representantContainer;
    @FXML private HBox emailContainer;
    @FXML private HBox passwordContainer;
    @FXML private HBox confirmPasswordContainer;
    @FXML private HBox adresseSiegeContainer;
    @FXML private HBox adresseAgenceContainer;
    @FXML private HBox siteWebContainer;
    @FXML private HBox telContainer;

    // Password strength indicators
    @FXML private ProgressBar passwordStrength;
    @FXML private Label passwordStrengthLabel;

    private AuthService authService;

    @FXML
    public void initialize() {
        try {
            authService = new AuthService();
        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données");
        }

        // Animation d'entrée pour le formulaire
        animateFormEntry();

        // Setup focus listeners pour tous les champs
        setupFocusListeners();

        // Listener pour le champ téléphone - validation en temps réel
        setupPhoneValidation();

        // Listener pour la force du mot de passe
        setupPasswordStrengthIndicator();

        // Formatage automatique du téléphone
        setupPhoneFormatting();
    }

    private void setupFocusListeners() {
        // Associer chaque champ à son container
        setupFieldFocusListener(nomBanqueField, nomBanqueContainer);
        setupFieldFocusListener(codeBanqueField, codeBanqueContainer);
        setupFieldFocusListener(representantField, representantContainer);
        setupFieldFocusListener(emailField, emailContainer);
        setupFieldFocusListener(passwordField, passwordContainer);
        setupFieldFocusListener(confirmPasswordField, confirmPasswordContainer);
        setupFieldFocusListener(adresseSiegeField, adresseSiegeContainer);
        setupFieldFocusListener(adresseAgenceField, adresseAgenceContainer);
        setupFieldFocusListener(siteWebField, siteWebContainer);
        setupFieldFocusListener(telField, telContainer);
    }

    private void setupFieldFocusListener(TextInputControl field, HBox container) {
        if (field == null || container == null) return;

        field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                container.getStyleClass().add("input-container-focused");
            } else {
                container.getStyleClass().remove("input-container-focused");
            }
        });
    }

    private void animateFormEntry() {
        if (formCard != null) {
            formCard.setTranslateY(50);
            formCard.setOpacity(0);

            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(800), formCard);
            translateTransition.setFromY(50);
            translateTransition.setToY(0);

            FadeTransition fadeTransition = new FadeTransition(Duration.millis(800), formCard);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);

            translateTransition.play();
            fadeTransition.play();
        }
    }

    private void setupPhoneValidation() {
        telField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePhoneRealTime(newValue);
        });

        // Focus lost validation
        telField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // Focus perdu
                validatePhoneFinal(telField.getText());
            }
        });
    }

    private void setupPhoneFormatting() {
        telField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;

            // Supprimer tout sauf les chiffres et le +
            String cleaned = newValue.replaceAll("[^0-9+]", "");

            // Limiter la longueur
            if (cleaned.length() > 12) {
                cleaned = cleaned.substring(0, 12);
            }

            // Formater le numéro
            String formatted = formatPhoneNumber(cleaned);

            if (!formatted.equals(newValue)) {
                telField.setText(formatted);
                telField.positionCaret(formatted.length());
            }
        });
    }

    private String formatPhoneNumber(String phone) {
        if (phone.isEmpty()) return phone;

        // Format: +216 XX XXX XXX
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < phone.length(); i++) {
            char c = phone.charAt(i);

            if (i == 0 && c == '+') {
                formatted.append(c);
            } else if (Character.isDigit(c)) {
                // Ajouter des espaces aux bons endroits
                int digitCount = formatted.toString().replaceAll("[^0-9]", "").length();

                if (digitCount == 3 || digitCount == 5 || digitCount == 8) {
                    formatted.append(" ");
                }
                formatted.append(c);
            }
        }

        return formatted.toString();
    }

    private void validatePhoneRealTime(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            setPhoneFieldState("neutral", "");
            return;
        }

        String cleanPhone = phone.replaceAll("\\s", "");

        // Vérification du format tunisien
        if (cleanPhone.startsWith("+216")) {
            if (cleanPhone.length() < 12) {
                setPhoneFieldState("warning", "Numéro incomplet");
            } else if (cleanPhone.length() == 12 && isValidTunisianPhone(cleanPhone)) {
                setPhoneFieldState("success", "✓ Numéro valide");
            } else {
                setPhoneFieldState("error", "Format invalide");
            }
        } else if (cleanPhone.length() == 8 && cleanPhone.matches("\\d{8}")) {
            // Numéro sans indicatif
            if (isValidTunisianLocalPhone(cleanPhone)) {
                setPhoneFieldState("success", "✓ Numéro valide");
            } else {
                setPhoneFieldState("error", "Numéro tunisien invalide");
            }
        } else if (!cleanPhone.isEmpty()) {
            setPhoneFieldState("warning", "Entrez +216 ou 8 chiffres");
        }
    }

    private void validatePhoneFinal(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return; // Champ optionnel
        }

        if (!isValidPhone(phone)) {
            setPhoneFieldState("error", "Format: +216 XX XXX XXX ou 8 chiffres");
        }
    }

    private void setPhoneFieldState(String state, String message) {
        // Retirer toutes les classes d'état du container
        if (telContainer != null) {
            telContainer.getStyleClass().removeAll("field-success", "field-error", "field-warning");

            switch (state) {
                case "success":
                    telContainer.getStyleClass().add("field-success");
                    telErrorLabel.setStyle("-fx-text-fill: #089647;");
                    break;
                case "error":
                    telContainer.getStyleClass().add("field-error");
                    telErrorLabel.setStyle("-fx-text-fill: #dc3545;");
                    break;
                case "warning":
                    telContainer.getStyleClass().add("field-warning");
                    telErrorLabel.setStyle("-fx-text-fill: #ffc107;");
                    break;
                default:
                    telErrorLabel.setStyle("-fx-text-fill: #848A86;");
            }
        }

        telErrorLabel.setText(message);
        telErrorLabel.setVisible(!message.isEmpty());
        telErrorLabel.setManaged(!message.isEmpty());
    }

    private void setupPasswordStrengthIndicator() {
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
        });
    }

    private void updatePasswordStrength(String password) {
        if (passwordStrength == null || passwordStrengthLabel == null) return;

        int strength = calculatePasswordStrength(password);

        passwordStrength.setProgress(strength / 100.0);

        if (strength < 25) {
            passwordStrength.setStyle("-fx-accent: #dc3545;");
            passwordStrengthLabel.setText("Très faible");
            passwordStrengthLabel.setStyle("-fx-text-fill: #dc3545;");
        } else if (strength < 50) {
            passwordStrength.setStyle("-fx-accent: #ffc107;");
            passwordStrengthLabel.setText("Faible");
            passwordStrengthLabel.setStyle("-fx-text-fill: #ffc107;");
        } else if (strength < 75) {
            passwordStrength.setStyle("-fx-accent: #17a2b8;");
            passwordStrengthLabel.setText("Moyen");
            passwordStrengthLabel.setStyle("-fx-text-fill: #17a2b8;");
        } else {
            passwordStrength.setStyle("-fx-accent: #089647;");
            passwordStrengthLabel.setText("Fort");
            passwordStrengthLabel.setStyle("-fx-text-fill: #089647;");
        }
    }

    private int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;

        int strength = 0;

        // Longueur
        if (password.length() >= 6) strength += 20;
        if (password.length() >= 8) strength += 10;
        if (password.length() >= 12) strength += 10;

        // Majuscules
        if (password.matches(".*[A-Z].*")) strength += 15;

        // Minuscules
        if (password.matches(".*[a-z].*")) strength += 15;

        // Chiffres
        if (password.matches(".*\\d.*")) strength += 15;

        // Caractères spéciaux
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) strength += 15;

        return Math.min(100, strength);
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
                    "",
                    emailField.getText().trim(),
                    passwordField.getText(),
                    codeBanqueField.getText().trim(),
                    adresseSiegeField.getText().trim(),
                    representantField.getText().trim()
            );

            banque.setAdresseAgence(adresseAgenceField.getText().trim());
            banque.setSiteWeb(siteWebField.getText().trim());
            banque.setTel(formatPhoneForStorage(telField.getText().trim()));

            // Inscrire
            Banque result = authService.inscrireBanque(banque);

            if (result != null) {
                showSuccess("🎉 Compte créé avec succès ! Votre compte est en attente de vérification.");
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

    private String formatPhoneForStorage(String phone) {
        if (phone == null || phone.trim().isEmpty()) return "";

        String cleaned = phone.replaceAll("\\s", "");

        // Si 8 chiffres, ajouter +216
        if (cleaned.matches("\\d{8}")) {
            return "+216" + cleaned;
        }

        return cleaned;
    }

    private boolean validateForm() {
        // Champs obligatoires
        if (nomBanqueField.getText().trim().isEmpty()) {
            showError("Le nom de la banque est obligatoire");
            highlightField(nomBanqueField, nomBanqueContainer);
            return false;
        }

        if (codeBanqueField.getText().trim().isEmpty()) {
            showError("Le code banque est obligatoire");
            highlightField(codeBanqueField, codeBanqueContainer);
            return false;
        }

        // Validation code banque (2-10 caractères alphanumériques)
        if (!isValidBankCode(codeBanqueField.getText().trim())) {
            showError("Le code banque doit contenir 2 à 10 caractères alphanumériques");
            highlightField(codeBanqueField, codeBanqueContainer);
            return false;
        }

        if (representantField.getText().trim().isEmpty()) {
            showError("Le nom du représentant légal est obligatoire");
            highlightField(representantField, representantContainer);
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            showError("L'email est obligatoire");
            highlightField(emailField, emailContainer);
            return false;
        }

        if (!isValidEmail(emailField.getText().trim())) {
            showError("L'email n'est pas valide");
            highlightField(emailField, emailContainer);
            return false;
        }

        if (passwordField.getText().isEmpty()) {
            showError("Le mot de passe est obligatoire");
            highlightField(passwordField, passwordContainer);
            return false;
        }

        if (passwordField.getText().length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            highlightField(passwordField, passwordContainer);
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Les mots de passe ne correspondent pas");
            highlightField(confirmPasswordField, confirmPasswordContainer);
            return false;
        }

        if (adresseSiegeField.getText().trim().isEmpty()) {
            showError("L'adresse du siège est obligatoire");
            highlightField(adresseSiegeField, adresseSiegeContainer);
            return false;
        }

        // Validation téléphone (si rempli)
        if (!telField.getText().trim().isEmpty() && !isValidPhone(telField.getText().trim())) {
            showError("Le numéro de téléphone n'est pas valide. Format: +216 XX XXX XXX");
            highlightField(telField, telContainer);
            return false;
        }

        // Validation site web (si rempli)
        if (!siteWebField.getText().trim().isEmpty() && !isValidWebsite(siteWebField.getText().trim())) {
            showError("L'URL du site web n'est pas valide");
            highlightField(siteWebField, siteWebContainer);
            return false;
        }

        if (!termsCheckbox.isSelected()) {
            showError("Vous devez accepter les conditions d'utilisation");
            return false;
        }

        return true;
    }

    private void highlightField(TextInputControl field, HBox container) {
        if (container != null) {
            container.getStyleClass().add("field-error");
        }
        field.requestFocus();

        // Animation de tremblement
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), container != null ? container : field);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setByX(10);
        shake.setOnFinished(e -> {
            if (container != null) container.setTranslateX(0);
            else field.setTranslateX(0);
        });
        shake.play();

        // Retirer la classe d'erreur après 3 secondes
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    if (container != null) {
                        container.getStyleClass().remove("field-error");
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean isValidBankCode(String code) {
        return code.matches("^[A-Za-z0-9]{2,10}$");
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return true; // Optionnel

        String cleaned = phone.replaceAll("\\s", "");

        // Format +216XXXXXXXX (12 caractères)
        if (cleaned.matches("^\\+216[0-9]{8}$")) {
            return isValidTunisianPhone(cleaned);
        }

        // Format 8 chiffres
        if (cleaned.matches("^[0-9]{8}$")) {
            return isValidTunisianLocalPhone(cleaned);
        }

        return false;
    }

    private boolean isValidTunisianPhone(String phone) {
        // +216 suivi de 8 chiffres
        String localPart = phone.substring(4);
        return isValidTunisianLocalPhone(localPart);
    }

    private boolean isValidTunisianLocalPhone(String phone) {
        // Mobiles: 2X, 3X, 4X, 5X, 9X
        // Fixes: 7X
        return phone.matches("^[2-57-9][0-9]{7}$");
    }

    private boolean isValidWebsite(String url) {
        String urlRegex = "^(https?://)?(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+(/.*)?$";
        return url.matches(urlRegex);
    }

    private void showError(String message) {
        alertBox.setVisible(true);
        alertBox.setManaged(true);
        alertMessage.setText(message);

        successBox.setVisible(false);
        successBox.setManaged(false);

        // Animation
        FadeTransition fade = new FadeTransition(Duration.millis(300), alertBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void showSuccess(String message) {
        successBox.setVisible(true);
        successBox.setManaged(true);
        successMessage.setText(message);

        alertBox.setVisible(false);
        alertBox.setManaged(false);

        // Animation
        FadeTransition fade = new FadeTransition(Duration.millis(300), successBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
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