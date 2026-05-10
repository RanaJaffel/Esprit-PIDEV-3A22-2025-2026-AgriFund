package com.agrifund.controller.agriculteur;

import com.agrifund.Main;
import com.agrifund.services.AgriculteurService;
import com.agrifund.services.UtilisateurService;
import com.agrifund.util.SessionManager;
import com.agrifund.util.FileManager;
import com.agrifund.entities.Agriculteur;
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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class AgriculteurProfileController {

    @FXML private ImageView profilePhoto;
    @FXML private Label userFullName;
    @FXML private HBox verificationBadge;

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telField;

    @FXML private TextField adresseFermeField;
    @FXML private TextField superficieField;
    @FXML private ComboBox<String> typeCultureCombo;

    @FXML private PasswordField currentPassword;
    @FXML private PasswordField newPassword;
    @FXML private PasswordField confirmPassword;

    @FXML private Label dateInscription;
    @FXML private Label statusLabel;
    @FXML private HBox verificationStatus;

    @FXML private HBox alertBox;
    @FXML private Label alertMessage;

    private UtilisateurService utilisateurService;
    private AgriculteurService agriculteurService;
    private Utilisateur currentUser;
    private Agriculteur currentAgriculteur;

    @FXML
    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();
            agriculteurService = new AgriculteurService();

            currentUser = SessionManager.getInstance().getUtilisateurConnecte();
            currentAgriculteur = agriculteurService.rechercherParUtilisateurId(currentUser.getId());

            loadProfile();

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), "danger");
        }
    }

    private void loadProfile() {
        // Photo
        loadPhoto();

        // Nom complet
        userFullName.setText(currentUser.getPrenom() + " " + currentUser.getNom());

        // Badge de vérification
        loadVerificationBadge();

        // Infos personnelles
        nomField.setText(currentUser.getNom());
        prenomField.setText(currentUser.getPrenom());
        emailField.setText(currentUser.getEmail());
        telField.setText(currentUser.getTel());

        // Infos ferme
        if (currentAgriculteur != null) {
            adresseFermeField.setText(currentAgriculteur.getAdresseFerme());
            superficieField.setText(currentAgriculteur.getSuperficieFerme().toString());
            typeCultureCombo.setValue(currentAgriculteur.getTypeCulture());

            // Statut
            statusLabel.setText(currentAgriculteur.getStatusCompte());

            // Vérification
            verificationStatus.getChildren().clear();
            Label badge = new Label();
            if (currentAgriculteur.isCompteVerifie()) {
                badge.setText("✓ Vérifié");
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

        if (currentUser.getPhoto() != null && !currentUser.getPhoto().isEmpty()) {
            File photoFile = new File(currentUser.getPhoto());
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

        if (currentAgriculteur != null) {
            Label badge = new Label();
            if (currentAgriculteur.isCompteVerifie()) {
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
        fileChooser.setTitle("Choisir une photo de profil");
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
                    showAlert("Photo mise à jour avec succès!", "success");
                }

            } catch (SQLException e) {
                showAlert("Erreur: " + e.getMessage(), "danger");
            }
        }
    }

    @FXML
    public void deletePhoto() {
        if (currentUser.getPhoto() == null || currentUser.getPhoto().isEmpty()) {
            showAlert("Aucune photo à supprimer", "warning");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la photo de profil ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                FileManager.supprimerFichier(currentUser.getPhoto());
                currentUser.setPhoto(null);
                utilisateurService.modifier(currentUser);
                loadPhoto();
                showAlert("Photo supprimée!", "success");

            } catch (SQLException e) {
                showAlert("Erreur: " + e.getMessage(), "danger");
            }
        }
    }

    @FXML
    public void saveProfile() {
        try {
            // Validation
            if (nomField.getText().trim().isEmpty() || prenomField.getText().trim().isEmpty()) {
                showAlert("Le nom et le prénom sont obligatoires", "danger");
                return;
            }

            if (emailField.getText().trim().isEmpty()) {
                showAlert("L'email est obligatoire", "danger");
                return;
            }

            // Vérifier email unique
            if (!emailField.getText().equals(currentUser.getEmail())) {
                if (utilisateurService.emailExiste(emailField.getText())) {
                    showAlert("Cet email est déjà utilisé", "danger");
                    return;
                }
            }

            // Validation superficie
            BigDecimal superficie;
            try {
                superficie = new BigDecimal(superficieField.getText().trim());
                if (superficie.compareTo(BigDecimal.ZERO) <= 0) {
                    showAlert("La superficie doit être supérieure à 0", "danger");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("La superficie doit être un nombre valide", "danger");
                return;
            }

            // Mettre à jour utilisateur
            currentUser.setNom(nomField.getText().trim());
            currentUser.setPrenom(prenomField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setTel(telField.getText().trim());
            utilisateurService.modifier(currentUser);

            // Mettre à jour agriculteur
            currentAgriculteur.setNom(nomField.getText().trim());
            currentAgriculteur.setPrenom(prenomField.getText().trim());
            currentAgriculteur.setAdresseFerme(adresseFermeField.getText().trim());
            currentAgriculteur.setSuperficieFerme(superficie);
            currentAgriculteur.setTypeCulture(typeCultureCombo.getValue());
            agriculteurService.modifier(currentAgriculteur);

            // Mettre à jour la session
            SessionManager.getInstance().setUtilisateurConnecte(currentUser);

            userFullName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
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
