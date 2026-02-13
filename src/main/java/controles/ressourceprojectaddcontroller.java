package controles;

import entities.projectagricole;
import entities.ressourceproject;
import services.projectagricoleCRUD;
import services.ressourceprojectCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ressourceprojectaddcontroller implements Initializable {

    @FXML private ComboBox<Integer> cbIdProject;
    @FXML private TextField tfNomRessource;
    @FXML private ComboBox<String> cbTypeRessource;
    @FXML private TextField tfQuantite;
    @FXML private TextField tfCout;
    @FXML private TextField tfFournisseur;
    @FXML private ComboBox<String> cbStatutRessource;
    @FXML private DatePicker dpDateAjout;
    @FXML private Button btnSave;

    private final ressourceprojectCRUD rService = new ressourceprojectCRUD();
    private final projectagricoleCRUD pService = new projectagricoleCRUD();
    private ressourceprojectcontroller mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize combo boxes
        cbTypeRessource.getItems().addAll("equipement", "materiaux", "service");
        cbStatutRessource.getItems().addAll("prevu", "achete");
        cbStatutRessource.setValue("prevu"); // Default value

        // Load project IDs
        try {
            for (projectagricole p : pService.afficher()) {
                cbIdProject.getItems().add(p.getIdproject());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les ID des projets.");
        }
    }

    public void setMainController(ressourceprojectcontroller mainController) {
        this.mainController = mainController;
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (!validateInputs()) return;

        try {
            ressourceproject r = new ressourceproject(
                    tfNomRessource.getText(),
                    cbTypeRessource.getValue(),
                    Integer.parseInt(tfQuantite.getText()),
                    new BigDecimal(tfCout.getText()),
                    tfFournisseur.getText(),
                    cbStatutRessource.getValue(),
                    Date.valueOf(dpDateAjout.getValue()),
                    cbIdProject.getValue()
            );

            rService.ajouter(r);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource ajoutée avec succès!");
            closeWindow();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    private boolean validateInputs() {
        // Check for empty fields
        if (tfNomRessource.getText().trim().isEmpty() || tfQuantite.getText().trim().isEmpty() ||
                tfCout.getText().trim().isEmpty() || tfFournisseur.getText().trim().isEmpty() ||
                cbIdProject.getValue() == null || cbTypeRessource.getValue() == null ||
                cbStatutRessource.getValue() == null || dpDateAjout.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir tous les champs!");
            return false;
        }

        // Validate resource name length
        if (tfNomRessource.getText().trim().length() < 2) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Le nom de la ressource doit contenir au moins 2 caractères!");
            return false;
        }

        // Validate supplier name length
        if (tfFournisseur.getText().trim().length() < 2) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Le nom du fournisseur doit contenir au moins 2 caractères!");
            return false;
        }

        // Validate numeric fields
        try {
            int quantite = Integer.parseInt(tfQuantite.getText().trim());
            if (quantite <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", "La quantité doit être un nombre positif!");
                return false;
            }

            BigDecimal cout = new BigDecimal(tfCout.getText().trim());
            if (cout.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le coût doit être un nombre positif!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "La quantité et le coût doivent être des nombres valides!");
            return false;
        }

        // Validate date is not in the past
        if (dpDateAjout.getValue().isBefore(java.time.LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "La date d'ajout ne peut pas être dans le passé!");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}