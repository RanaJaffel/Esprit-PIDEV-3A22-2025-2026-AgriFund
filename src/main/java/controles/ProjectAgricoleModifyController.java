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
    @FXML private DatePicker dpDateSoumission;
    @FXML private Button btnSave;

    // ✅ NEW: Beautiful dynamic status badge (replaces ComboBox)
    @FXML private Label lblStatusBadge;

    private projectagricole currentProject;
    private final projectagricoleCRUD service = new projectagricoleCRUD();
    private projectagricolecontroller mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Badge will be styled when project is loaded
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
            dpDateSoumission.setValue(currentProject.getDatesoumission().toLocalDate());

            // ✅ Display current status as beautiful dynamic badge
            updateStatusBadge(currentProject.getStatut());
        }
    }

    /**
     * Update status badge with appropriate colors based on status
     */
    private void updateStatusBadge(String statut) {
        if (lblStatusBadge == null) return;

        String displayText;
        String backgroundColor;
        String textColor = "white";
        String tooltipText;
        String icon;

        switch (statut.toLowerCase()) {
            case "accepte":
                icon = "✅";
                displayText = icon + " Accepté";
                backgroundColor = "linear-gradient(to right, #089647, #076A39)";
                tooltipText = "Projet accepté par la décision financière.\nStatut géré automatiquement.";
                break;

            case "refuse":
                icon = "❌";
                displayText = icon + " Refusé";
                backgroundColor = "linear-gradient(to right, #D32F2F, #B71C1C)";
                tooltipText = "Projet refusé par la décision financière.\nStatut géré automatiquement.";
                break;

            case "en cours":
            default:
                icon = "📋";
                displayText = icon + " En cours";
                backgroundColor = "linear-gradient(to right, #E1B323, #9A951F)";
                tooltipText = "Projet en attente de décision financière.\nStatut géré automatiquement.";
                break;
        }

        lblStatusBadge.setText(displayText);
        lblStatusBadge.setStyle(
                "-fx-background-color: " + backgroundColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10 24;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 8, 0, 0, 2);" +
                        "-fx-cursor: hand;"
        );

        Tooltip tooltip = new Tooltip(tooltipText);
        lblStatusBadge.setTooltip(tooltip);
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
                            "Note: Le statut reste inchangé (" +
                            getStatusDisplayName(currentProject.getStatut()) + ")");
            closeWindow();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL",
                    "Erreur lors de la modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get display name for status
     */
    private String getStatusDisplayName(String statut) {
        switch (statut.toLowerCase()) {
            case "accepte": return "Accepté";
            case "refuse": return "Refusé";
            case "en cours": return "En cours";
            default: return statut;
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