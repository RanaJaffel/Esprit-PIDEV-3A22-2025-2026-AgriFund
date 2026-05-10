package com.agrifund.controller.banque;

import com.agrifund.Main;
import com.agrifund.services.BanqueService;
import com.agrifund.services.UtilisateurService;
import com.agrifund.util.SessionManager;
import com.agrifund.util.FileManager;
import com.agrifund.entities.Banque;
import com.agrifund.entities.Utilisateur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class BanqueProfileController {

    @FXML private ImageView profilePhoto;
    @FXML private Label bankName;
    @FXML private Label bankCode;
    @FXML private HBox verificationBadge;

    @FXML private TextField nomField;
    @FXML private TextField codeField;
    @FXML private TextField emailField;
    @FXML private TextField telField;
    @FXML private TextField representantField;
    @FXML private TextField siegeField;
    @FXML private TextField agenceField;
    @FXML private TextField siteWebField;

    @FXML private PasswordField currentPassword;
    @FXML private PasswordField newPassword;
    @FXML private PasswordField confirmPassword;

    @FXML private Label dateInscription;
    @FXML private Label statusLabel;
    @FXML private HBox verificationStatus;

    @FXML private HBox alertBox;
    @FXML private Label alertMessage;

    private UtilisateurService utilisateurService;
    private BanqueService banqueService;
    private Utilisateur currentUser;
    private Banque currentBanque;

    @FXML
    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();
            banqueService = new BanqueService();

            currentUser = SessionManager.getInstance().getUtilisateurConnecte();
            currentBanque = banqueService.rechercherParUtilisateurId(currentUser.getId());

            loadProfile();

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), "danger");
        }
    }

    private void loadProfile() {
        // Photo
        loadPhoto();

        // Nom et code
        bankName.setText(currentUser.getNom());

        if (currentBanque != null) {
            bankCode.setText("[" + currentBanque.getCodeBanque() + "]");
        }

        // Badge de vérification
        loadVerificationBadge();

        // Infos générales
        nomField.setText(currentUser.getNom());
        emailField.setText(currentUser.getEmail());
        telField.setText(currentUser.getTel());

        // Infos banque
        if (currentBanque != null) {
            codeField.setText(currentBanque.getCodeBanque());
            representantField.setText(currentBanque.getRepresentantLegal());
            siegeField.setText(currentBanque.getAddresseSiege());
            agenceField.setText(currentBanque.getAdresseAgence());
            siteWebField.setText(currentBanque.getSiteWeb());

            // Statut
            statusLabel.setText(currentBanque.getStatusCompte());

            // Vérification
            verificationStatus.getChildren().clear();
            Label badge = new Label();
            if (currentBanque.isCompteVerifie()) {
                badge.setText("✓ Vérifiée");
                badge.setStyle("-fx-background-color: #089647; -fx-text-fill: white; -fx-padding: 3 10; -fx-background-radius: 10;");
            } else {
                badge.setText("⏳ En attente");
                badge.setStyle("-fx-background-color: #E1B323; -fx-text-fill: #060806; -fx-padding: 3 10; -fx-background-radius: 10;");
            }
            verificationStatus.getChildren().add(badge);
        }

        // Date d'inscription
        if (currentUser.getDateInscrit() != null) {
            dateInscription.setText(currentUser.getDateInscrit()
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        }
    }

    private void loadPhoto() {
        Image image = null;

        String photoPath = currentUser.getPhoto();
        if (photoPath == null && currentBanque != null) {
            photoPath = currentBanque.getLogo();
        }

        if (photoPath != null && !photoPath.isEmpty()) {
            File photoFile = new File(photoPath);
            if (photoFile.exists()) {
                image = new Image(photoFile.toURI().toString());
            }
        }

        if (image == null) {
            try {
                image = new Image(getClass().getResourceAsStream("/com/agrifund/images/default-avatar.png"));
            } catch (Exception e) {}
        }

        if (image != null) {
            profilePhoto.setImage(image);
            Circle clip = new Circle(75, 75, 75);
            profilePhoto.setClip(clip);
        }
    }

    private void loadVerificationBadge() {
        verificationBadge.getChildren().clear();

        if (currentBanque != null) {
            Label badge = new Label();
            if (currentBanque.isCompteVerifie()) {
                badge.setText("✓ Compte vérifié");
                badge.getStyleClass().addAll("badge", "badge-success");
            } else {
                badge.setText("⏳ En attente de vérification");
                badge.getStyleClass().addAll("badge", "badge-warning");
            }
            verificationBadge.getChildren().add(badge);
        }
    }

    @FXML
    public void changePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un logo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(Main.getPrimaryStage());

        if (file != null) {
            try {
                String storedPath = FileManager.uploadPhoto(file.getAbsolutePath(), currentUser.getId());

                if (storedPath != null) {
                    if (currentUser.getPhoto() != null && !currentUser.getPhoto().isEmpty()) {
                        FileManager.supprimerFichier(currentUser.getPhoto());
                    }

                    currentUser.setPhoto(storedPath);
                    utilisateurService.modifier(currentUser);

                    loadPhoto();
                    showAlert("Logo mis à jour avec succès!", "success");
                }

            } catch (SQLException e) {
                showAlert("Erreur: " + e.getMessage(), "danger");
            }
        }
    }

    @FXML
    public void deletePhoto() {
        if (currentUser.getPhoto() == null || currentUser.getPhoto().isEmpty()) {
            showAlert("Aucun logo à supprimer", "warning");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le logo ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                FileManager.supprimerFichier(currentUser.getPhoto());
                currentUser.setPhoto(null);
                utilisateurService.modifier(currentUser);
                loadPhoto();
                showAlert("Logo supprimé!", "success");

            } catch (SQLException e) {
                showAlert("Erreur: " + e.getMessage(), "danger");
            }
        }
    }

    @FXML
    public void saveProfile() {
        try {
            // Validation
            if (nomField.getText().trim().isEmpty()) {
                showAlert("Le nom de la banque est obligatoire", "danger");
                return;
            }

            if (emailField.getText().trim().isEmpty()) {
                showAlert("L'email est obligatoire", "danger");
                return;
            }

            if (representantField.getText().trim().isEmpty()) {
                showAlert("Le représentant légal est obligatoire", "danger");
                return;
            }

            if (siegeField.getText().trim().isEmpty()) {
                showAlert("L'adresse du siège est obligatoire", "danger");
                return;
            }

            // Vérifier email unique
            if (!emailField.getText().equals(currentUser.getEmail())) {
                if (utilisateurService.emailExiste(emailField.getText())) {
                    showAlert("Cet email est déjà utilisé", "danger");
                    return;
                }
            }

            // Mettre à jour utilisateur
            currentUser.setNom(nomField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setTel(telField.getText().trim());
            utilisateurService.modifier(currentUser);

            // Mettre à jour banque
            currentBanque.setNom(nomField.getText().trim());
            currentBanque.setRepresentantLegal(representantField.getText().trim());
            currentBanque.setAddresseSiege(siegeField.getText().trim());
            currentBanque.setAdresseAgence(agenceField.getText().trim());
            currentBanque.setSiteWeb(siteWebField.getText().trim());
            banqueService.modifier(currentBanque);

            // Mettre à jour la session
            SessionManager.getInstance().setUtilisateurConnecte(currentUser);

            bankName.setText(currentUser.getNom());
            showAlert("Profil mis à jour avec succès!", "success");

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), "danger");
        }
    }

    @FXML
    public void cancelChanges() {
        loadProfile();
        hideAlert();
    }

    @FXML
    public void changePassword() {
        try {
            String current = currentPassword.getText();
            String newPwd = newPassword.getText();
            String confirm = confirmPassword.getText();

            if (current.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
                showAlert("Veuillez remplir tous les champs", "danger");
                return;
            }

            if (!utilisateurService.verifierPassword(current, currentUser.getPassword())) {
                showAlert("Mot de passe actuel incorrect", "danger");
                return;
            }

            if (newPwd.length() < 6) {
                showAlert("Le nouveau mot de passe doit contenir au moins 6 caractères", "danger");
                return;
            }

            if (!newPwd.equals(confirm)) {
                showAlert("Les mots de passe ne correspondent pas", "danger");
                return;
            }

            utilisateurService.modifierMotDePasse(currentUser.getId(), newPwd);

            currentPassword.clear();
            newPassword.clear();
            confirmPassword.clear();

            showAlert("Mot de passe changé avec succès!", "success");

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), "danger");
        }
    }

    private void showAlert(String message, String type) {
        alertBox.setVisible(true);
        alertBox.setManaged(true);
        alertMessage.setText(message);

        alertBox.getStyleClass().removeAll("alert-success", "alert-danger", "alert-warning");
        alertBox.getStyleClass().add("alert-" + type);

        String color = type.equals("success") ? "#089647" :
                type.equals("danger") ? "#dc3545" : "#E1B323";
        alertMessage.setStyle("-fx-text-fill: " + color + ";");
    }

    private void hideAlert() {
        alertBox.setVisible(false);
        alertBox.setManaged(false);
    }
}
