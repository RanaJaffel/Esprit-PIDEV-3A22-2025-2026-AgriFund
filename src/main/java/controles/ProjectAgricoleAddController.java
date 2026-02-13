package controles;

import entities.projectagricole;
import services.projectagricoleCRUD;
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

public class ProjectAgricoleAddController implements Initializable {

    @FXML private TextField tfNomProject;
    @FXML private TextField tfSurface;
    @FXML private TextField tfBudget;
    @FXML private ComboBox<String> cbStatut;
    @FXML private DatePicker dpDateSoumission;
    @FXML private Button btnSave;

    private final projectagricoleCRUD service = new projectagricoleCRUD();
    private projectagricolecontroller mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbStatut.getItems().addAll("en cours", "accepte", "refuse");
        cbStatut.setValue("en cours"); // Default value
    }

    public void setMainController(projectagricolecontroller mainController) {
        this.mainController = mainController;
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (!validateInputs()) return;

        try {
            projectagricole p = new projectagricole(
                    tfNomProject.getText(),
                    Float.parseFloat(tfSurface.getText()),
                    new BigDecimal(tfBudget.getText()),
                    cbStatut.getValue(),
                    Date.valueOf(dpDateSoumission.getValue())
            );

            service.ajouter(p);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Projet ajouté avec succès!");
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
        if (tfNomProject.getText().trim().isEmpty() || tfSurface.getText().trim().isEmpty() ||
                tfBudget.getText().trim().isEmpty() || cbStatut.getValue() == null ||
                dpDateSoumission.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir tous les champs!");
            return false;
        }

        // Validate project name length
        if (tfNomProject.getText().trim().length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Le nom du projet doit contenir au moins 3 caractères!");
            return false;
        }

        // Validate numeric fields
        try {
            float surface = Float.parseFloat(tfSurface.getText().trim());
            if (surface <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", "La surface doit être un nombre positif!");
                return false;
            }

            BigDecimal budget = new BigDecimal(tfBudget.getText().trim());
            if (budget.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Le budget doit être un nombre positif!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Surface et Budget doivent être des nombres valides!");
            return false;
        }

        // Validate date is not in the past
        if (dpDateSoumission.getValue().isBefore(java.time.LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", "La date de soumission ne peut pas être dans le passé!");
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