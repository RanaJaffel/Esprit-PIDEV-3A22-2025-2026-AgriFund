package Controllers;

import Services.AuthService;
import Services.UtilisateurService;
import entities.Utilisateur;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

public class ForgotPasswordController {

    // Steps content
    @FXML private VBox step1Content;
    @FXML private VBox step2Content;
    @FXML private VBox step3Content;

    // Step indicators
    @FXML private StackPane step1Indicator;
    @FXML private StackPane step2Indicator;
    @FXML private StackPane step3Indicator;
    @FXML private Label step1Number;
    @FXML private Label step2Number;
    @FXML private Label step3Number;
    @FXML private Pane connector1;
    @FXML private Pane connector2;

    // Header elements
    @FXML private Label headerIcon;
    @FXML private Label headerTitle;
    @FXML private Label headerSubtitle;

    // Main card
    @FXML private VBox mainCard;

    // Form fields
    @FXML private TextField emailField;
    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    // Input containers
    @FXML private HBox emailContainer;
    @FXML private HBox tokenContainer;
    @FXML private HBox newPasswordContainer;
    @FXML private HBox confirmPasswordContainer;

    // Hints and validation
    @FXML private Label emailHint;
    @FXML private Label confirmHint;
    @FXML private ProgressBar passwordStrength;
    @FXML private Label passwordStrengthLabel;
    @FXML private Label timerLabel;
    @FXML private Label step2Description;

    // Alerts
    @FXML private HBox alertBox;
    @FXML private Label alertMessage;
    @FXML private HBox successBox;
    @FXML private Label successMessage;

    // Buttons
    @FXML private Button sendCodeBtn;
    @FXML private Button resetBtn;

    private AuthService authService;
    private UtilisateurService utilisateurService;
    private String currentEmail;
    private String originalSendBtnText;
    private String originalResetBtnText;

    // Timer
    private Timeline codeExpiryTimer;
    private int secondsRemaining = 900; // 15 minutes

    @FXML
    public void initialize() {
        try {
            authService = new AuthService();
            utilisateurService = new UtilisateurService();
        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données");
        }

        // Animation d'entrée
        animateCardEntry();

        // Setup focus listeners
        setupFocusListeners();

        // Setup validation en temps réel
        setupEmailValidation();
        setupPasswordStrengthIndicator();
        setupPasswordConfirmValidation();

        // Sauvegarder les textes originaux des boutons
        if (sendCodeBtn != null) originalSendBtnText = sendCodeBtn.getText();
        if (resetBtn != null) originalResetBtnText = resetBtn.getText();

        // Setup Enter key handlers
        setupEnterKeyHandlers();
    }

    private void animateCardEntry() {
        if (mainCard != null) {
            mainCard.setTranslateY(50);
            mainCard.setOpacity(0);

            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(800), mainCard);
            translateTransition.setFromY(50);
            translateTransition.setToY(0);

            FadeTransition fadeTransition = new FadeTransition(Duration.millis(800), mainCard);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);

            translateTransition.play();
            fadeTransition.play();
        }
    }

    private void setupFocusListeners() {
        setupFieldFocusListener(emailField, emailContainer);
        setupFieldFocusListener(tokenField, tokenContainer);
        setupFieldFocusListener(newPasswordField, newPasswordContainer);
        setupFieldFocusListener(confirmPasswordField, confirmPasswordContainer);
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
                    setHint(emailHint, "✓ Email valide", "#089647");
                } else {
                    emailContainer.getStyleClass().remove("field-success");
                    setHint(emailHint, "Format email invalide", "#dc3545");
                }
            } else {
                emailContainer.getStyleClass().removeAll("field-error", "field-success");
                hideHint(emailHint);
            }
        });
    }

    private void setupPasswordStrengthIndicator() {
        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
            validatePasswordConfirmation();
        });
    }

    private void setupPasswordConfirmValidation() {
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePasswordConfirmation();
        });
    }

    private void validatePasswordConfirmation() {
        String password = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (confirm != null && !confirm.isEmpty()) {
            if (confirm.equals(password)) {
                confirmPasswordContainer.getStyleClass().remove("field-error");
                confirmPasswordContainer.getStyleClass().add("field-success");
                setHint(confirmHint, "✓ Les mots de passe correspondent", "#089647");
            } else {
                confirmPasswordContainer.getStyleClass().remove("field-success");
                confirmPasswordContainer.getStyleClass().add("field-error");
                setHint(confirmHint, "Les mots de passe ne correspondent pas", "#dc3545");
            }
        } else {
            confirmPasswordContainer.getStyleClass().removeAll("field-error", "field-success");
            hideHint(confirmHint);
        }
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
            passwordStrength.setStyle("-fx-accent: #E1B323;");
            passwordStrengthLabel.setText("Faible");
            passwordStrengthLabel.setStyle("-fx-text-fill: #E1B323;");
        } else if (strength < 75) {
            passwordStrength.setStyle("-fx-accent: #9A951F;");
            passwordStrengthLabel.setText("Moyen");
            passwordStrengthLabel.setStyle("-fx-text-fill: #9A951F;");
        } else {
            passwordStrength.setStyle("-fx-accent: #089647;");
            passwordStrengthLabel.setText("Fort");
            passwordStrengthLabel.setStyle("-fx-text-fill: #089647;");
        }
    }

    private int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;

        int strength = 0;

        if (password.length() >= 6) strength += 20;
        if (password.length() >= 8) strength += 10;
        if (password.length() >= 12) strength += 10;
        if (password.matches(".*[A-Z].*")) strength += 15;
        if (password.matches(".*[a-z].*")) strength += 15;
        if (password.matches(".*\\d.*")) strength += 15;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) strength += 15;

        return Math.min(100, strength);
    }

    private void setHint(Label hintLabel, String text, String color) {
        if (hintLabel != null) {
            hintLabel.setText(text);
            hintLabel.setStyle("-fx-text-fill: " + color + ";");
            hintLabel.setVisible(true);
            hintLabel.setManaged(true);
        }
    }

    private void hideHint(Label hintLabel) {
        if (hintLabel != null) {
            hintLabel.setVisible(false);
            hintLabel.setManaged(false);
        }
    }

    private void setupEnterKeyHandlers() {
        emailField.setOnAction(event -> handleSendCode());
        tokenField.setOnAction(event -> newPasswordField.requestFocus());
        newPasswordField.setOnAction(event -> confirmPasswordField.requestFocus());
        confirmPasswordField.setOnAction(event -> handleResetPassword());
    }

    @FXML
    public void handleSendCode() {
        hideMessages();

        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError("Veuillez entrer votre adresse email");
            highlightField(emailField, emailContainer);
            return;
        }

        if (!isValidEmail(email)) {
            showError("L'adresse email n'est pas valide");
            highlightField(emailField, emailContainer);
            return;
        }

        // Loading state
        setLoadingState(sendCodeBtn, true, "⏳ Envoi en cours...", originalSendBtnText);

        new Thread(() -> {
            try {
                // Vérifier si l'email existe
                Utilisateur user = utilisateurService.rechercherParEmail(email);

                javafx.application.Platform.runLater(() -> {
                    setLoadingState(sendCodeBtn, false, originalSendBtnText, originalSendBtnText);

                    if (user == null) {
                        showError("Aucun compte associé à cet email");
                        highlightField(emailField, emailContainer);
                        return;
                    }

                    try {
                        // Envoyer le code
                        String token = authService.demanderReinitialisationMotDePasse(email);

                        if (token != null) {
                            currentEmail = email;
                            showSuccess("📧 Un code de réinitialisation a été envoyé à " + maskEmail(email));

                            // Passer à l'étape 2 avec animation
                            transitionToStep(2);

                            // Démarrer le timer
                            startExpiryTimer();

                            // Mettre à jour la description
                            step2Description.setText("Un code a été envoyé à " + maskEmail(email) + ". Entrez-le avec votre nouveau mot de passe.");
                        } else {
                            showError("Erreur lors de l'envoi du code. Veuillez réessayer.");
                        }
                    } catch (SQLException e) {
                        showError("Erreur: " + e.getMessage());
                    }
                });

            } catch (SQLException e) {
                javafx.application.Platform.runLater(() -> {
                    setLoadingState(sendCodeBtn, false, originalSendBtnText, originalSendBtnText);
                    showError("Erreur: " + e.getMessage());
                });
            }
        }).start();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;

        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];

        if (name.length() <= 2) {
            return name + "***@" + domain;
        }

        return name.substring(0, 2) + "***@" + domain;
    }

    private void transitionToStep(int step) {
        VBox currentContent = null;
        VBox nextContent = null;

        if (step == 2) {
            currentContent = step1Content;
            nextContent = step2Content;
            updateStepIndicator(1, "completed");
            updateStepIndicator(2, "active");
            connector1.getStyleClass().add("step-connector-active");
            headerIcon.setText("📨");
            headerTitle.setText("Vérification");
            headerSubtitle.setText("Entrez le code reçu par email");
        } else if (step == 3) {
            currentContent = step2Content;
            nextContent = step3Content;
            updateStepIndicator(2, "completed");
            updateStepIndicator(3, "active");
            connector2.getStyleClass().add("step-connector-active");
            headerIcon.setText("✅");
            headerTitle.setText("Succès !");
            headerSubtitle.setText("Votre mot de passe a été modifié");
            stopExpiryTimer();
        }

        if (currentContent != null && nextContent != null) {
            // ✅ Créer des copies finales pour utilisation dans la lambda
            final VBox finalCurrentContent = currentContent;
            final VBox finalNextContent = nextContent;

            // Fade out current
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), finalCurrentContent);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            fadeOut.setOnFinished(e -> {
                // ✅ Utiliser les variables finales
                finalCurrentContent.setVisible(false);
                finalCurrentContent.setManaged(false);

                finalNextContent.setVisible(true);
                finalNextContent.setManaged(true);
                finalNextContent.setOpacity(0);

                // Fade in next
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), finalNextContent);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });

            fadeOut.play();
        }
    }

    private void updateStepIndicator(int step, String state) {
        StackPane indicator = null;
        Label number = null;

        switch (step) {
            case 1:
                indicator = step1Indicator;
                number = step1Number;
                break;
            case 2:
                indicator = step2Indicator;
                number = step2Number;
                break;
            case 3:
                indicator = step3Indicator;
                number = step3Number;
                break;
        }

        if (indicator != null && number != null) {
            indicator.getStyleClass().removeAll("step-indicator-active", "step-indicator-completed");
            number.getStyleClass().removeAll("step-number-active", "step-number-completed");

            if (state.equals("active")) {
                indicator.getStyleClass().add("step-indicator-active");
                number.getStyleClass().add("step-number-active");
            } else if (state.equals("completed")) {
                indicator.getStyleClass().add("step-indicator-completed");
                number.getStyleClass().add("step-number-completed");
                number.setText("✓");
            }
        }
    }

    private void startExpiryTimer() {
        secondsRemaining = 900; // 15 minutes

        codeExpiryTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            updateTimerDisplay();

            if (secondsRemaining <= 0) {
                stopExpiryTimer();
                showError("Le code a expiré. Veuillez en demander un nouveau.");
            }
        }));

        codeExpiryTimer.setCycleCount(Timeline.INDEFINITE);
        codeExpiryTimer.play();
    }

    private void updateTimerDisplay() {
        int minutes = secondsRemaining / 60;
        int seconds = secondsRemaining % 60;

        String time = String.format("%02d:%02d", minutes, seconds);
        timerLabel.setText("Code expire dans: " + time);

        // Changer la couleur si peu de temps restant
        if (secondsRemaining <= 120) { // 2 minutes
            timerLabel.setStyle("-fx-text-fill: #dc3545;");
        } else if (secondsRemaining <= 300) { // 5 minutes
            timerLabel.setStyle("-fx-text-fill: #E1B323;");
        }
    }

    private void stopExpiryTimer() {
        if (codeExpiryTimer != null) {
            codeExpiryTimer.stop();
        }
    }

    @FXML
    public void handleResendCode() {
        if (currentEmail != null && !currentEmail.isEmpty()) {
            stopExpiryTimer();
            emailField.setText(currentEmail);

            // Retourner à l'étape 1 temporairement pour renvoyer
            try {
                String token = authService.demanderReinitialisationMotDePasse(currentEmail);

                if (token != null) {
                    showSuccess("📧 Un nouveau code a été envoyé à " + maskEmail(currentEmail));
                    secondsRemaining = 900;
                    startExpiryTimer();
                    tokenField.clear();
                } else {
                    showError("Erreur lors de l'envoi du code");
                }
            } catch (SQLException e) {
                showError("Erreur: " + e.getMessage());
            }
        }
    }

    @FXML
    public void goBackToStep1() {
        stopExpiryTimer();

        // Reset step indicators
        step1Indicator.getStyleClass().removeAll("step-indicator-completed");
        step1Indicator.getStyleClass().add("step-indicator-active");
        step1Number.setText("1");
        step1Number.getStyleClass().add("step-number-active");

        step2Indicator.getStyleClass().removeAll("step-indicator-active");
        step2Number.getStyleClass().removeAll("step-number-active");

        connector1.getStyleClass().remove("step-connector-active");

        // Reset header
        headerIcon.setText("🔐");
        headerTitle.setText("Mot de passe oublié ?");
        headerSubtitle.setText("Pas de panique ! Nous allons vous aider.");

        // Transition
        step2Content.setVisible(false);
        step2Content.setManaged(false);
        step1Content.setVisible(true);
        step1Content.setManaged(true);

        hideMessages();

        // Clear fields
        tokenField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
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
            highlightField(tokenField, tokenContainer);
            return;
        }

        if (newPassword.isEmpty()) {
            showError("Veuillez entrer votre nouveau mot de passe");
            highlightField(newPasswordField, newPasswordContainer);
            return;
        }

        if (newPassword.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            highlightField(newPasswordField, newPasswordContainer);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            highlightField(confirmPasswordField, confirmPasswordContainer);
            return;
        }

        // Loading state
        setLoadingState(resetBtn, true, "⏳ Réinitialisation...", originalResetBtnText);

        new Thread(() -> {
            try {
                boolean success = authService.reinitialiserMotDePasse(token, newPassword);

                javafx.application.Platform.runLater(() -> {
                    setLoadingState(resetBtn, false, originalResetBtnText, originalResetBtnText);

                    if (success) {
                        // Passer à l'étape 3
                        transitionToStep(3);
                        hideMessages();
                    } else {
                        showError("Code invalide ou expiré. Veuillez réessayer.");
                        highlightField(tokenField, tokenContainer);
                    }
                });

            } catch (SQLException e) {
                javafx.application.Platform.runLater(() -> {
                    setLoadingState(resetBtn, false, originalResetBtnText, originalResetBtnText);
                    showError("Erreur: " + e.getMessage());
                });
            }
        }).start();
    }

    private void setLoadingState(Button button, boolean loading, String loadingText, String normalText) {
        if (button != null) {
            button.setDisable(loading);
            button.setText(loading ? loadingText : normalText);
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

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    private void showError(String message) {
        alertBox.getStyleClass().removeAll("alert-success", "alert-info");
        alertBox.getStyleClass().add("alert-error");
        alertBox.setVisible(true);
        alertBox.setManaged(true);
        alertMessage.setText(message);
        alertMessage.setStyle("-fx-text-fill: #dc3545;");

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

    @FXML
    public void goToLogin() {
        stopExpiryTimer();
        loadPage("login");
    }

    @FXML
    public void goToHome() {
        stopExpiryTimer();
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