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
        // ✅ Show all possible statuses (for display only)
        cbStatut.getItems().addAll("en cours", "accepte", "refuse");

        // ✅ IMPORTANT: Disable the status field
        // Only your friend can change status via decisionfinanciere
        cbStatut.setDisable(true);

        // Add explanation tooltip
        Tooltip tooltip = new Tooltip(
                "Le statut ne peut pas être modifié ici.\n" +
                        "Il est géré automatiquement par les décisions financières."
        );
        cbStatut.setTooltip(tooltip);

        // Visual indicator that field is read-only
        cbStatut.setStyle("-fx-opacity: 0.7;");
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

            // ✅ Display current status (read-only)
            cbStatut.setValue(currentProject.getStatut());

            dpDateSoumission.setValue(currentProject.getDatesoumission().toLocalDate());
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (!validateInputs()) return;

        try {
            // ✅ Update only the fields YOU are responsible for
            currentProject.setNomproject(tfNomProject.getText().trim());
            currentProject.setSurface(Float.parseFloat(tfSurface.getText().trim()));
            currentProject.setBudgetdemande(new BigDecimal(tfBudget.getText().trim()));
            currentProject.setDatesoumission(Date.valueOf(dpDateSoumission.getValue()));

            // ✅ IMPORTANT: Do NOT update status - keep the current one
            // Status is managed by your friend via decisionfinanciere triggers
            // currentProject.setStatut() is NOT called here

            service.modifier(currentProject);
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Projet modifié avec succès!\n\n" +
                            "Note: Le statut reste inchangé (" + currentProject.getStatut() + ")");
            closeWindow();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL",
                    "Erreur lors de la modification: " + e.getMessage());
            e.printStackTrace();
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
        if (tfNomProject.getText().trim().isEmpty() ||
                tfSurface.getText().trim().isEmpty() ||
                tfBudget.getText().trim().isEmpty() ||
                dpDateSoumission.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Veuillez remplir tous les champs!");
            return false;
        }

        // Validate project name length
        if (tfNomProject.getText().trim().length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Le nom du projet doit contenir au moins 3 caractères!");
            return false;
        }

        // Validate numeric fields
        try {
            float surface = Float.parseFloat(tfSurface.getText().trim());
            if (surface <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                        "La surface doit être un nombre positif!");
                return false;
            }

            BigDecimal budget = new BigDecimal(tfBudget.getText().trim());
            if (budget.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                        "Le budget doit être un nombre positif!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format",
                    "Surface et Budget doivent être des nombres valides!");
            return false;
        }

        // ✅ REMOVED: Date validation (allow past dates for historical projects)
        // Users can modify projects with any date

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