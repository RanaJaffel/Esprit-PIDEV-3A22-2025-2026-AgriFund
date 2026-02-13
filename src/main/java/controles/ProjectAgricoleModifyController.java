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

public class ProjectAgricoleModifyController implements Initializable {

    @FXML private TextField tfNomProject;
    @FXML private TextField tfSurface;
    @FXML private TextField tfBudget;
    @FXML private ComboBox<String> cbStatut;
    @FXML private DatePicker dpDateSoumission;
    @FXML private Button btnSave;

    private projectagricole currentProject;
    private final projectagricoleCRUD service = new projectagricoleCRUD();
    private projectagricolecontroller mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbStatut.getItems().addAll("en cours", "accepte", "refuse");
    }

    public void setProject(projectagricole project) {
        this.currentProject = project;
        populateFields();
    }

    public void setMainController(projectagricolecontroller mainController) {
        this.mainController = mainController;
    }

    private void populateFields() {
        if (currentProject != null) {
            tfNomProject.setText(currentProject.getNomproject());
            tfSurface.setText(String.valueOf(currentProject.getSurface()));
            tfBudget.setText(currentProject.getBudgetdemande().toString());
            cbStatut.setValue(currentProject.getStatut());
            dpDateSoumission.setValue(currentProject.getDatesoumission().toLocalDate());
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (!validateInputs()) return;

        try {
            currentProject.setNomproject(tfNomProject.getText());
            currentProject.setSurface(Float.parseFloat(tfSurface.getText()));
            currentProject.setBudgetdemande(new BigDecimal(tfBudget.getText()));
            currentProject.setStatut(cbStatut.getValue());
            currentProject.setDatesoumission(Date.valueOf(dpDateSoumission.getValue()));

            service.modifier(currentProject);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Projet modifié avec succès!");
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

    // Copy the same validateInputs and showAlert methods from AddController
    private boolean validateInputs() {
        // Same implementation as in AddController
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