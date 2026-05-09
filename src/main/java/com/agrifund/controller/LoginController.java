package com.agrifund.controller;

import java.sql.SQLException;
import java.util.Optional;

import com.agrifund.Main;
import com.agrifund.components.CaptchaComponent;
import com.agrifund.entities.Code2FA;
import com.agrifund.entities.Utilisateur;
import com.agrifund.services.AuthService;
import com.agrifund.services.TwoFactorAuthService;
import com.agrifund.util.SessionManager;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMe;
    @FXML private HBox alertBox;
    @FXML private Label alertMessage;
    @FXML private VBox loginCard;
    @FXML private Button loginButton;

    // Input containers pour gestion du focus
    @FXML private HBox emailContainer;
    @FXML private HBox passwordContainer;

    // CAPTCHA Component
    @FXML private CaptchaComponent captchaComponent;

    // Compteur de tentatives échouées
    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS_BEFORE_CAPTCHA = 2;
    private static final int MAX_ATTEMPTS_TOTAL = 5;
    private long lastFailedAttemptTime = 0;
    private static final long LOCKOUT_DURATION = 30000; // 30 secondes

    private AuthService authService;
    private TwoFactorAuthService twoFAService;

    private String originalButtonText;

    @FXML
    public void initialize() {
        try {
            authService = new AuthService();
            twoFAService = new TwoFactorAuthService();
        } catch (SQLException e) {
            showAlert("Erreur de connexion à la base de données", Alert.AlertType.ERROR);
        }

        // Animation d'entrée
        animateFormEntry();

        // Setup focus listeners
        setupFocusListeners();

        // Sauvegarder le texte original du bouton
        if (loginButton != null) {
            originalButtonText = loginButton.getText();
        }

        // Validation en temps réel de l'email
        setupEmailValidation();

        // Enter key pour soumettre le formulaire
        setupEnterKeyHandler();

        // Cacher le CAPTCHA initialement (l'afficher après N échecs)
        updateCaptchaVisibility();
    }

    private void updateCaptchaVisibility() {
        if (captchaComponent != null) {
            boolean showCaptcha = failedAttempts >= MAX_ATTEMPTS_BEFORE_CAPTCHA;
            captchaComponent.setVisible(showCaptcha);
            captchaComponent.setManaged(showCaptcha);

            if (showCaptcha) {
                captchaComponent.refreshCaptcha();
            }
        }
    }

    private void animateFormEntry() {
        if (loginCard != null) {
            loginCard.setTranslateX(50);
            loginCard.setOpacity(0);

            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(800), loginCard);
            translateTransition.setFromX(50);
            translateTransition.setToX(0);

            FadeTransition fadeTransition = new FadeTransition(Duration.millis(800), loginCard);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);

            translateTransition.play();
            fadeTransition.play();
        }
    }

    private void setupFocusListeners() {
        setupFieldFocusListener(emailField, emailContainer);
        setupFieldFocusListener(passwordField, passwordContainer);
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

    private void setupEmailValidation() {
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (isValidEmail(newValue)) {
                    emailContainer.getStyleClass().remove("field-error");
                    emailContainer.getStyleClass().add("field-success");
                } else {
                    emailContainer.getStyleClass().remove("field-success");
                }
            } else {
                emailContainer.getStyleClass().removeAll("field-error", "field-success");
            }
        });
    }

    private void setupEnterKeyHandler() {
        passwordField.setOnAction(event -> handleLogin());
        emailField.setOnAction(event -> passwordField.requestFocus());
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    @FXML
    public void handleLogin() {
        hideError();

        // Vérifier le verrouillage temporaire
        if (isLockedOut()) {
            long remainingTime = (LOCKOUT_DURATION - (System.currentTimeMillis() - lastFailedAttemptTime)) / 1000;
            showError("Trop de tentatives. Réessayez dans " + remainingTime + " secondes.");
            return;
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation de base
        if (email.isEmpty()) {
            showError("Veuillez entrer votre email");
            highlightField(emailField, emailContainer);
            return;
        }

        if (!isValidEmail(email)) {
            showError("Format d'email invalide");
            highlightField(emailField, emailContainer);
            return;
        }

        if (password.isEmpty()) {
            showError("Veuillez entrer votre mot de passe");
            highlightField(passwordField, passwordContainer);
            return;
        }

        // Vérification CAPTCHA si nécessaire
        if (failedAttempts >= MAX_ATTEMPTS_BEFORE_CAPTCHA && captchaComponent != null) {
            if (!captchaComponent.verify()) {
                return; // Le composant CAPTCHA affiche son propre message d'erreur
            }
        }

        // Afficher l'état de chargement
        setLoadingState(true);

        // Exécuter la connexion dans un thread séparé
        new Thread(() -> {
            try {
                Utilisateur user = authService.login(email, password);

                javafx.application.Platform.runLater(() -> {
                    setLoadingState(false);

                    if (user != null) {
                        // Réinitialiser le compteur de tentatives
                        failedAttempts = 0;
                        updateCaptchaVisibility();

                        try {
                            // Vérifier si 2FA est activée
                            if (twoFAService.is2FAActive(user.getId())) {
                                handle2FA(user);
                            } else {
                                showSuccessAndRedirect(user);
                            }
                        } catch (SQLException e) {
                            showError("Erreur lors de la vérification 2FA: " + e.getMessage());
                        }
                    } else {
                        handleFailedAttempt();
                    }
                });

            } catch (SQLException e) {
                javafx.application.Platform.runLater(() -> {
                    setLoadingState(false);
                    showError("Erreur lors de la connexion: " + e.getMessage());
                });
            }
        }).start();
    }

    private void handleFailedAttempt() {
        failedAttempts++;
        lastFailedAttemptTime = System.currentTimeMillis();

        if (failedAttempts >= MAX_ATTEMPTS_TOTAL) {
            showError("Compte temporairement verrouillé. Réessayez dans 30 secondes.");
        } else if (failedAttempts >= MAX_ATTEMPTS_BEFORE_CAPTCHA) {
            showError("Email ou mot de passe incorrect. Veuillez compléter le CAPTCHA.");
            updateCaptchaVisibility();
        } else {
            int remaining = MAX_ATTEMPTS_BEFORE_CAPTCHA - failedAttempts;
            showError("Email ou mot de passe incorrect. " + remaining + " tentative(s) restante(s).");
        }

        shakeLoginCard();

        // Rafraîchir le CAPTCHA après chaque échec
        if (captchaComponent != null && captchaComponent.isVisible()) {
            captchaComponent.refreshCaptcha();
        }
    }

    private boolean isLockedOut() {
        if (failedAttempts >= MAX_ATTEMPTS_TOTAL) {
            long timeSinceLastAttempt = System.currentTimeMillis() - lastFailedAttemptTime;
            if (timeSinceLastAttempt < LOCKOUT_DURATION) {
                return true;
            } else {
                // Réinitialiser après le délai
                failedAttempts = 0;
                updateCaptchaVisibility();
                return false;
            }
        }
        return false;
    }

    private void setLoadingState(boolean loading) {
        if (loginButton != null) {
            loginButton.setDisable(loading);
            if (loading) {
                loginButton.setText("⏳ Connexion en cours...");
            } else {
                loginButton.setText(originalButtonText);
            }
        }
        emailField.setDisable(loading);
        passwordField.setDisable(loading);

        if (captchaComponent != null) {
            captchaComponent.setDisable(loading);
        }
    }

    private void shakeLoginCard() {
        if (loginCard != null) {
            TranslateTransition shake = new TranslateTransition(Duration.millis(50), loginCard);
            shake.setCycleCount(6);
            shake.setAutoReverse(true);
            shake.setByX(10);
            shake.setOnFinished(e -> loginCard.setTranslateX(0));
            shake.play();
        }
    }

    private void highlightField(TextInputControl field, HBox container) {
        if (container != null) {
            container.getStyleClass().add("field-error");
        }
        field.requestFocus();

        TranslateTransition shake = new TranslateTransition(Duration.millis(50), container != null ? container : field);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setByX(8);
        shake.setOnFinished(e -> {
            if (container != null) container.setTranslateX(0);
            else field.setTranslateX(0);
        });
        shake.play();

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

    private void handle2FA(Utilisateur user) throws SQLException {
        Code2FA code = twoFAService.envoyerCode2FA(user.getId());

        if (code != null) {
            Dialog<String> dialog = create2FADialog();

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent() && !result.get().isEmpty()) {
                String codeEntre = result.get();

                if (twoFAService.verifierCode(user.getId(), codeEntre)) {
                    twoFAService.enregistrerConnexion(user.getId(), true, "2fa");
                    showSuccessAndRedirect(user);
                } else {
                    twoFAService.enregistrerConnexion(user.getId(), false, "2fa");
                    showError("Code incorrect ou expiré");
                }
            }
        } else {
            showError("Erreur lors de l'envoi du code 2FA");
        }
    }

    private Dialog<String> create2FADialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Authentification à Deux Facteurs");
        dialog.setHeaderText(null);

        VBox content = new VBox(20);
        content.setStyle("-fx-padding: 30; -fx-alignment: center;");

        Label iconLabel = new Label("🔐");
        iconLabel.setStyle("-fx-font-size: 50px;");

        Label titleLabel = new Label("Code de vérification");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #133D03;");

        Label descLabel = new Label("Un code a été envoyé à votre email.\nVeuillez l'entrer ci-dessous.");
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #848A86; -fx-text-alignment: center;");
        descLabel.setWrapText(true);

        TextField codeField = new TextField();
        codeField.setPromptText("Entrez le code à 6 chiffres");
        codeField.setStyle("-fx-font-size: 18px; -fx-alignment: center; -fx-padding: 15; " +
                "-fx-background-radius: 12; -fx-border-radius: 12; " +
                "-fx-border-color: #c8e6c9; -fx-border-width: 2;");
        codeField.setMaxWidth(250);

        codeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 6) {
                codeField.setText(oldVal);
            }
            if (!newVal.matches("\\d*")) {
                codeField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        content.getChildren().addAll(iconLabel, titleLabel, descLabel, codeField);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.getDialogPane().setStyle("-fx-background-color: white; -fx-background-radius: 20;");

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("✓ Vérifier");
        okButton.setStyle("-fx-background-color: #089647; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 10;");

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Annuler");
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #848A86; -fx-padding: 10 20;");

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return codeField.getText();
            }
            return null;
        });

        javafx.application.Platform.runLater(() -> codeField.requestFocus());

        return dialog;
    }

    private void showSuccessAndRedirect(Utilisateur user) {
        alertBox.getStyleClass().removeAll("alert-error");
        alertBox.getStyleClass().add("alert-success");
        alertBox.setVisible(true);
        alertBox.setManaged(true);
        alertMessage.setText("✅ Connexion réussie ! Redirection...");
        alertMessage.setStyle("-fx-text-fill: #089647;");

        FadeTransition fade = new FadeTransition(Duration.millis(300), alertBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        new Thread(() -> {
            try {
                Thread.sleep(1500);
                javafx.application.Platform.runLater(() -> {
                    try {
                        completeLogin(user);
                    } catch (SQLException e) {
                        showError("Erreur lors de la redirection: " + e.getMessage());
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void completeLogin(Utilisateur user) throws SQLException {
        String typeUtilisateur = authService.getTypeUtilisateur(user.getId());

        SessionManager session = SessionManager.getInstance();
        session.setUtilisateurConnecte(user);
        session.setTypeUtilisateur(typeUtilisateur);

        twoFAService.enregistrerConnexion(user.getId(), true, "password");

        // Switch sidebar and load role-specific dashboard
        Main.onLoginSuccess(typeUtilisateur);
    }

    @FXML
    public void goToForgotPassword() {
        Main.navigateTo("/com/agrifund/fxml/forgot-password.fxml");
    }

    @FXML
    public void goToRegisterAgriculteur() {
        Main.navigateTo("/com/agrifund/fxml/register-agriculteur.fxml");
    }

    @FXML
    public void goToRegisterBanque() {
        Main.navigateTo("/com/agrifund/fxml/register-banque.fxml");
    }

    @FXML
    public void goToHome() {
        Main.navigateTo("/com/agrifund/fxml/home.fxml");
    }

    private void showError(String message) {
        alertBox.getStyleClass().removeAll("alert-success");
        alertBox.getStyleClass().add("alert-error");
        alertBox.setVisible(true);
        alertBox.setManaged(true);
        alertMessage.setText(message);
        alertMessage.setStyle("-fx-text-fill: #dc3545;");

        FadeTransition fade = new FadeTransition(Duration.millis(300), alertBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
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
        Main.navigateTo("/com/agrifund/fxml/" + fxmlFile + ".fxml");
    }
}