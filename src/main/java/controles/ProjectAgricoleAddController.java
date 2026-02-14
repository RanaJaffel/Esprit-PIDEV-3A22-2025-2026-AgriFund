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
    @FXML private DatePicker dpDateSoumission;
    @FXML private Button btnSave;

    // ✅ NEW: Beautiful status badge (replaces ComboBox)
    @FXML private Label lblStatusBadge;

    private final projectagricoleCRUD service = new projectagricoleCRUD();
    private projectagricolecontroller mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // ✅ Setup beautiful status badge with mustard yellow gradient
        if (lblStatusBadge != null) {
            lblStatusBadge.setText("📋 En cours");
            lblStatusBadge.setStyle(
                    "-fx-background-color: linear-gradient(to right, #E1B323, #9A951F);" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 14px;" +
                            "-fx-padding: 10 24;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-radius: 20;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(225, 179, 35, 0.4), 8, 0, 0, 2);" +
                            "-fx-cursor: hand;"
            );

            // Add tooltip
            Tooltip tooltip = new Tooltip(
                    "Le statut initial est toujours 'En cours'.\n" +
                            "Il sera modifié automatiquement par les décisions financières."
            );
            lblStatusBadge.setTooltip(tooltip);
        }
    }

    public void setMainController(projectagricolecontroller mainController) {
        this.mainController = mainController;
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (!validateInputs()) return;

        try {
            // ✅ Always create project with 'en cours' status
            projectagricole p = new projectagricole(
                    tfNomProject.getText().trim(),
                    Float.parseFloat(tfSurface.getText().trim()),
                    new BigDecimal(tfBudget.getText().trim()),
                    "en cours", // ✅ FIXED: Always 'en cours'
                    Date.valueOf(dpDateSoumission.getValue())
            );

            service.ajouter(p);
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Projet ajouté avec succès!\n\n" +
                            "Statut: En cours (en attente de décision financière)");
            closeWindow();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL",
                    "Erreur lors de l'ajout: " + e.getMessage());
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