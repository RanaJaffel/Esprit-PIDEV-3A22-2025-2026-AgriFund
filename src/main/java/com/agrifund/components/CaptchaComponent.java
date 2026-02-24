package com.agrifund.components;

import com.agrifund.services.CaptchaService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CaptchaComponent extends VBox {

    private final CaptchaService captchaService;
    private final ImageView captchaImage;
    private final TextField captchaInput;
    private final Label errorLabel;
    private final Button refreshButton;

    public CaptchaComponent() {
        captchaService = new CaptchaService();

        // Style du conteneur
        this.setSpacing(12);
        this.setAlignment(Pos.CENTER_LEFT);
        this.getStyleClass().add("captcha-container");

        // Label
        Label captchaLabel = new Label("🔒 Vérification de sécurité");
        captchaLabel.getStyleClass().add("captcha-label");

        // Conteneur image + refresh
        HBox imageContainer = new HBox(15);
        imageContainer.setAlignment(Pos.CENTER_LEFT);
        imageContainer.getStyleClass().add("captcha-image-container");
        imageContainer.setPadding(new Insets(10));

        // Image CAPTCHA
        captchaImage = new ImageView();
        captchaImage.setFitWidth(200);
        captchaImage.setFitHeight(80);
        captchaImage.getStyleClass().add("captcha-image");

        // Bouton refresh
        refreshButton = new Button("🔄");
        refreshButton.getStyleClass().add("captcha-refresh-btn");
        refreshButton.setOnAction(e -> refreshCaptcha());

        imageContainer.getChildren().addAll(captchaImage, refreshButton);

        // Champ de saisie
        HBox inputContainer = new HBox(10);
        inputContainer.setAlignment(Pos.CENTER_LEFT);
        inputContainer.getStyleClass().add("captcha-input-container");
        inputContainer.setPadding(new Insets(0, 15, 0, 15));

        Label inputIcon = new Label("✏️");
        inputIcon.setStyle("-fx-font-size: 18px;");

        captchaInput = new TextField();
        captchaInput.setPromptText("Entrez le code ci-dessus");
        captchaInput.getStyleClass().add("captcha-input");
        captchaInput.setPrefWidth(250);

        // Convertir en majuscules automatiquement
        captchaInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                captchaInput.setText(newVal.toUpperCase());
            }
        });

        inputContainer.getChildren().addAll(inputIcon, captchaInput);

        // Label d'erreur
        errorLabel = new Label();
        errorLabel.getStyleClass().add("captcha-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Ajouter tous les éléments
        this.getChildren().addAll(captchaLabel, imageContainer, inputContainer, errorLabel);

        // Générer le premier CAPTCHA
        refreshCaptcha();
    }

    /**
     * Rafraîchit le CAPTCHA avec une nouvelle image
     */
    public void refreshCaptcha() {
        Image image = captchaService.generateCaptcha();
        captchaImage.setImage(image);
        captchaInput.clear();
        hideError();

        // Animation de rotation du bouton
        javafx.animation.RotateTransition rotate = new javafx.animation.RotateTransition(
                javafx.util.Duration.millis(300), refreshButton
        );
        rotate.setByAngle(360);
        rotate.play();
    }

    /**
     * Vérifie si le CAPTCHA est correct
     */
    public boolean verify() {
        String userInput = captchaInput.getText();

        if (userInput == null || userInput.trim().isEmpty()) {
            showError("Veuillez entrer le code de sécurité");
            return false;
        }

        boolean isValid = captchaService.verifyCaptcha(userInput);

        if (!isValid) {
            showError("Code incorrect. Veuillez réessayer.");
            refreshCaptcha();
            return false;
        }

        hideError();
        return true;
    }

    /**
     * Retourne le texte saisi
     */
    public String getInput() {
        return captchaInput.getText();
    }

    /**
     * Efface le champ de saisie
     */
    public void clear() {
        captchaInput.clear();
        hideError();
    }

    private void showError(String message) {
        errorLabel.setText("⚠️ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        // Animation
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(200), errorLabel
        );
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}