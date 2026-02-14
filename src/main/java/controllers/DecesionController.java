package controllers;

import entities.DecisionFinanciere;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceDecisionFinanciere;

import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

public class DecesionController {

    @FXML private TextField tfIdDecision;
    @FXML private TextField tfIdProjet;
    @FXML private ComboBox<String> cbStatut;
    @FXML private DatePicker dpDateDecision;
    @FXML private TextArea taJustification;
    @FXML private Label lblStatus;

    private ServiceDecisionFinanciere service;
    private DecisionFinanciere decisionEnCours;

    @FXML
    public void initialize() {
        service = new ServiceDecisionFinanciere();
        cbStatut.getItems().addAll("En attente", "Approuvé", "Rejeté");
        updateStatus("Prêt");
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        try {
            int idProjet = Integer.parseInt(tfIdProjet.getText());
            String statut = cbStatut.getValue();
            java.util.Date dateDecision = java.sql.Date.valueOf(dpDateDecision.getValue());
            String justification = taJustification.getText();

            if (decisionEnCours == null) {
                DecisionFinanciere nouvelleDecision = new DecisionFinanciere(statut, justification, dateDecision, idProjet);
                service.ajouter(nouvelleDecision);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Décision ajoutée avec succès!");
            } else {
                decisionEnCours.setIdProjet(idProjet);
                decisionEnCours.setStatut(statut);
                decisionEnCours.setDateDecision(dateDecision);
                decisionEnCours.setJustification(justification);
                service.modifier(decisionEnCours);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Décision modifiée avec succès!");
            }

            closeWindow();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "L'ID Projet doit être un nombre valide!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données", "Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (tfIdProjet.getText().isEmpty()) {
            errors.append("- L'ID Projet est obligatoire\n");
        }

        if (cbStatut.getValue() == null) {
            errors.append("- Le statut est obligatoire\n");
        }

        if (dpDateDecision.getValue() == null) {
            errors.append("- La date de décision est obligatoire\n");
        }

        if (taJustification.getText().isEmpty()) {
            errors.append("- La justification est obligatoire\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Champs incomplets", errors.toString());
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    @FXML
    public void handleClear(ActionEvent event) {
        tfIdDecision.clear();
        tfIdProjet.clear();
        cbStatut.setValue(null);
        dpDateDecision.setValue(null);
        taJustification.clear();

        decisionEnCours = null;
        updateStatus("Formulaire effacé");
    }

    public void loadDecision(int idDecision) {
        try {
            DecisionFinanciere decision = service.getById(idDecision);

            if (decision != null) {
                decisionEnCours = decision;

                tfIdDecision.setText(String.valueOf(decision.getIdDecision()));
                tfIdProjet.setText(String.valueOf(decision.getIdProjet()));
                cbStatut.setValue(decision.getStatut());

                // Conversion de java.sql.Date en LocalDate
                java.util.Date utilDate = new java.util.Date(decision.getDateDecision().getTime());
                dpDateDecision.setValue(utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

                taJustification.setText(decision.getJustification());

                updateStatus("Modification en cours");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune décision trouvée avec cet ID.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la décision: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void closeWindow() {
        Stage stage = (Stage) tfIdDecision.getScene().getWindow();
        stage.close();
    }

    private void updateStatus(String message) {
        if (lblStatus != null) {
            lblStatus.setText(message);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
