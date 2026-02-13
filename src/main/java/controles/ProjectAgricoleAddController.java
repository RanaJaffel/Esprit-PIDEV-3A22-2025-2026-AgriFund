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
        if (tfNomProject.getText().isEmpty() || tfSurface.getText().isEmpty() ||
                tfBudget.getText().isEmpty() || cbStatut.getValue() == null ||
                dpDateSoumission.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir tous les champs!");
            return false;
        }

        try {
            Float.parseFloat(tfSurface.getText());
            new BigDecimal(tfBudget.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Surface et Budget doivent être des nombres!");
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