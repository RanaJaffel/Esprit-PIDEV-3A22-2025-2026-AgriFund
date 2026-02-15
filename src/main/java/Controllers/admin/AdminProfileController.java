package Controllers.admin;

import Controllers.MainApp;
import Services.UtilisateurService;
import Utils.SessionManager;
import Utils.FileManager;
import entities.Utilisateur;

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

public class AdminProfileController {

    @FXML private ImageView profilePhoto;
    @FXML private Label userFullName;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telField;
    @FXML private PasswordField currentPassword;
    @FXML private PasswordField newPassword;
    @FXML private PasswordField confirmPassword;
    @FXML private Label dateInscription;
    @FXML private Label userId;
    @FXML private HBox alertBox;
    @FXML private Label alertMessage;

    private UtilisateurService utilisateurService;
    private Utilisateur currentUser;

    @FXML
    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();
            currentUser = SessionManager.getInstance().getUtilisateurConnecte();

            loadProfile();

        } catch (SQLException e) {
            showAlert("Erreur: " + e.getMessage(), "danger");
        }
    }

    private void loadProfile() {
        if (currentUser != null) {
            // Photo
            loadPhoto();

            // Infos
            userFullName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            nomField.setText(currentUser.getNom());
            prenomField.setText(currentUser.getPrenom());
            emailField.setText(currentUser.getEmail());
            telField.setText(currentUser.getTel());

            // Meta
            userId.setText("#" + currentUser.getId());
            if (currentUser.getDateInscrit() != null) {
                dateInscription.setText(currentUser.getDateInscrit()
                        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            }
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
                image = new Image(getClass().getResourceAsStream("/images/default-avatar.png"));
            } catch (Exception e) {}
        }

        if (image != null) {
            profilePhoto.setImage(image);
            Circle clip = new Circle(75, 75, 75);
            profilePhoto.setClip(clip);
        }
    }

    @FXML
    public void changePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(MainApp.getPrimaryStage());

        if (file != null) {
            try {
                // Upload la photo
                String storedPath = FileManager.uploadPhoto(file.getAbsolutePath(), currentUser.getId());

                if (storedPath != null) {
                    // Supprimer l'ancienne photo
                    if (currentUser.getPhoto() != null && !currentUser.getPhoto().isEmpty()) {
                        FileManager.supprimerFichier(currentUser.getPhoto());
                    }

                    // Mettre à jour en base
                    currentUser.setPhoto(storedPath);
                    utilisateurService.modifier(currentUser);

                    // Recharger
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

            // Vérifier si l'email a changé et s'il existe déjà
            if (!emailField.getText().equals(currentUser.getEmail())) {
                if (utilisateurService.emailExiste(emailField.getText())) {
                    showAlert("Cet email est déjà utilisé", "danger");
                    return;
                }
            }

            // Mettre à jour
            currentUser.setNom(nomField.getText().trim());
            currentUser.setPrenom(prenomField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setTel(telField.getText().trim());

            utilisateurService.modifier(currentUser);

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

            // Validation
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

            // Changer le mot de passe
            utilisateurService.modifierMotDePasse(currentUser.getId(), newPwd);

            // Vider les champs
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
        alertMessage.setStyle(type.equals("success") ? "-fx-text-fill: #089647;" :
                type.equals("danger") ? "-fx-text-fill: #dc3545;" : "-fx-text-fill: #E1B323;");
    }

    private void hideAlert() {
        alertBox.setVisible(false);
        alertBox.setManaged(false);
    }
}