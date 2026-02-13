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

public class ressourceprojectmodifycontroller implements Initializable {

    @FXML private ComboBox<Integer> cbIdProject;
    @FXML private TextField tfNomRessource;
    @FXML private ComboBox<String> cbTypeRessource;
    @FXML private TextField tfQuantite;
    @FXML private TextField tfCout;
    @FXML private TextField tfFournisseur;
    @FXML private ComboBox<String> cbStatutRessource;
    @FXML private DatePicker dpDateAjout;
    @FXML private Button btnSave;

    private ressourceproject currentRessource;
    private final ressourceprojectCRUD rService = new ressourceprojectCRUD();
    private final projectagricoleCRUD pService = new projectagricoleCRUD();
    private ressourceprojectcontroller mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize combo boxes
        cbTypeRessource.getItems().addAll("equipement", "materiaux", "service");
        cbStatutRessource.getItems().addAll("prevu", "achete");

        // Load project IDs
        try {
            for (projectagricole p : pService.afficher()) {
                cbIdProject.getItems().add(p.getIdproject());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les ID des projets.");
        }
    }

    public void setRessource(ressourceproject ressource) {
        this.currentRessource = ressource;
        populateFields();
    }

    public void setMainController(ressourceprojectcontroller mainController) {
        this.mainController = mainController;
    }

    private void populateFields() {
        if (currentRessource != null) {
            cbIdProject.setValue(currentRessource.getIdproject());
            tfNomRessource.setText(currentRessource.getNomressource());
            cbTypeRessource.setValue(currentRessource.getTyperessource());
            tfQuantite.setText(String.valueOf(currentRessource.getQuantite()));
            tfCout.setText(currentRessource.getCout().toString());
            tfFournisseur.setText(currentRessource.getFournisseur());
            cbStatutRessource.setValue(currentRessource.getStatut());
            dpDateAjout.setValue(currentRessource.getDateajout().toLocalDate());
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (!validateInputs()) return;

        try {
            currentRessource.setIdproject(cbIdProject.getValue());
            currentRessource.setNomressource(tfNomRessource.getText());
            currentRessource.setTyperessource(cbTypeRessource.getValue());
            currentRessource.setQuantite(Integer.parseInt(tfQuantite.getText()));
            currentRessource.setCout(new BigDecimal(tfCout.getText()));
            currentRessource.setFournisseur(tfFournisseur.getText());
            currentRessource.setStatut(cbStatutRessource.getValue());
            currentRessource.setDateajout(Date.valueOf(dpDateAjout.getValue()));

            rService.modifier(currentRessource);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource modifiée avec succès!");
            closeWindow();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de la modification: " + e.getMessage());
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