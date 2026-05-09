package com.agrifund.controller;

import com.agrifund.Main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.animation.*;
import javafx.util.Duration;

import java.io.IOException;

public class HomeController {

    @FXML private VBox featuresSection;
    @FXML private VBox contactSection;

    @FXML
    public void initialize() {
        // Animations peuvent être ajoutées ici si nécessaire
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
    public void scrollToFeatures() {
        if (featuresSection != null) {
            featuresSection.requestFocus();
            // Animation de scroll peut être ajoutée
        }
    }

    @FXML
    public void scrollToContact() {
        if (contactSection != null) {
            contactSection.requestFocus();
        }
    }

    @FXML
    public void showRegisterOptions() {
        // Créer un dialogue personnalisé moderne
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initOwner(Main.getPrimaryStage());

        // Container principal avec fond semi-transparent
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        // Card du dialogue
        VBox dialogCard = new VBox(30);
        dialogCard.setAlignment(Pos.CENTER);
        dialogCard.setPadding(new Insets(50));
        dialogCard.setMaxWidth(550);
        dialogCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 30;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 30, 0, 0, 10);"
        );

        // Header
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);

        Label iconLabel = new Label("👤");
        iconLabel.setStyle("-fx-font-size: 50px;");

        Label titleLabel = new Label("Créer un compte");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #133D03;");

        Label subtitleLabel = new Label("Choisissez votre type de compte pour commencer");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #848A86;");

        header.getChildren().addAll(iconLabel, titleLabel, subtitleLabel);

        // Options
        HBox options = new HBox(30);
        options.setAlignment(Pos.CENTER);

        // Option Agriculteur
        VBox agriculteurOption = createRegisterOption(
                "🌱",
                "Agriculteur",
                "Accédez aux financements pour votre exploitation",
                "#B2D944",
                () -> {
                    dialogStage.close();
                    loadPage("register-agriculteur");
                }
        );

        // Option Banque
        VBox banqueOption = createRegisterOption(
                "🏦",
                "Banque",
                "Gérez vos financements agricoles",
                "#089647",
                () -> {
                    dialogStage.close();
                    loadPage("register-banque");
                }
        );

        options.getChildren().addAll(agriculteurOption, banqueOption);

        // Bouton fermer
        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-font-size: 20px;" +
                        "-fx-text-fill: #848A86;" +
                        "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> dialogStage.close());

        // Lien connexion
        HBox loginLink = new HBox(5);
        loginLink.setAlignment(Pos.CENTER);

        Label alreadyLabel = new Label("Déjà inscrit ?");
        alreadyLabel.setStyle("-fx-text-fill: #848A86;");

        Hyperlink loginHyperlink = new Hyperlink("Se connecter");
        loginHyperlink.setStyle("-fx-text-fill: #089647; -fx-font-weight: bold;");
        loginHyperlink.setOnAction(e -> {
            dialogStage.close();
            loadPage("login");
        });

        loginLink.getChildren().addAll(alreadyLabel, loginHyperlink);

        // Assembler le dialogue
        StackPane closeContainer = new StackPane(closeBtn);
        closeContainer.setAlignment(Pos.TOP_RIGHT);

        VBox content = new VBox(25);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(header, options, loginLink);

        dialogCard.getChildren().addAll(closeContainer, content);

        overlay.getChildren().add(dialogCard);

        // Fermer en cliquant sur l'overlay
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                dialogStage.close();
            }
        });

        Scene dialogScene = new Scene(overlay, 800, 600);
        dialogScene.setFill(javafx.scene.paint.Color.TRANSPARENT);

        dialogStage.setScene(dialogScene);

        // Animation d'entrée
        dialogCard.setScaleX(0.8);
        dialogCard.setScaleY(0.8);
        dialogCard.setOpacity(0);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), dialogCard);
        scaleIn.setToX(1);
        scaleIn.setToY(1);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), dialogCard);
        fadeIn.setToValue(1);

        scaleIn.play();
        fadeIn.play();

        dialogStage.showAndWait();
    }

    private VBox createRegisterOption(String emoji, String title, String description, String accentColor, Runnable onClick) {
        VBox option = new VBox(15);
        option.setAlignment(Pos.CENTER);
        option.setPadding(new Insets(30));
        option.setPrefWidth(220);
        option.setStyle(
                "-fx-background-color: #f8fcf5;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: #e8f5e9;" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-width: 2;" +
                        "-fx-cursor: hand;"
        );

        Label iconLabel = new Label(emoji);
        iconLabel.setStyle("-fx-font-size: 50px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #133D03;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #848A86; -fx-text-alignment: center;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(180);

        Button selectBtn = new Button("Choisir →");
        selectBtn.setStyle(
                "-fx-background-color: " + accentColor + ";" +
                        "-fx-text-fill: " + (accentColor.equals("#B2D944") ? "#133D03" : "white") + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 30;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        );
        selectBtn.setOnAction(e -> onClick.run());

        option.getChildren().addAll(iconLabel, titleLabel, descLabel, selectBtn);

        // Hover effects
        option.setOnMouseEntered(e -> {
            option.setStyle(
                    "-fx-background-color: rgba(178, 217, 68, 0.15);" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: " + accentColor + ";" +
                            "-fx-border-radius: 20;" +
                            "-fx-border-width: 2;" +
                            "-fx-cursor: hand;" +
                            "-fx-scale-x: 1.02;" +
                            "-fx-scale-y: 1.02;"
            );
        });

        option.setOnMouseExited(e -> {
            option.setStyle(
                    "-fx-background-color: #f8fcf5;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: #e8f5e9;" +
                            "-fx-border-radius: 20;" +
                            "-fx-border-width: 2;" +
                            "-fx-cursor: hand;" +
                            "-fx-scale-x: 1;" +
                            "-fx-scale-y: 1;"
            );
        });

        option.setOnMouseClicked(e -> onClick.run());

        return option;
    }

    private void loadPage(String fxmlFile) {
        Main.navigateTo("/com/agrifund/fxml/" + fxmlFile + ".fxml");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}