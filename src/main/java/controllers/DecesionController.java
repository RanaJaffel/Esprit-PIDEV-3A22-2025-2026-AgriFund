package controllers;

import entities.DecisionFinanciere;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import services.ServiceDecisionFinanciere;
import services.ServiceEvaluationRisque;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class DecesionController {

    @FXML private TextField tfIdDecision;
    @FXML private ComboBox<String> cbStatut;
    @FXML private DatePicker dpDateDecision;
    @FXML private TextArea taJustification;
    @FXML private ComboBox<Integer> cbIdEvaluation;
    @FXML private Label lblStatus;

    private ServiceDecisionFinanciere service;
    private DecisionFinanciere decisionEnCours;

    @FXML
    public void initialize() {
        service = new ServiceDecisionFinanciere();
        cbStatut.getItems().addAll("En attente", "Approuvé", "Rejeté");

        // Désactiver les dates passées pour dpDateDecision
        dpDateDecision.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.compareTo(today) < 0);
            }
        });

        // Formater l'affichage de la date
        dpDateDecision.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });

        loadEvaluationIds();
        updateStatus("Prêt");
    }

    private void loadEvaluationIds() {
        try {
            List<Integer> evaluationIds = new ServiceEvaluationRisque().getAllEvaluationIds();
            cbIdEvaluation.getItems().setAll(evaluationIds);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les évaluations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        try {
            String statut = cbStatut.getValue();
            String justification = taJustification.getText();
            java.util.Date dateDecision = java.sql.Date.valueOf(dpDateDecision.getValue());
            int idEvaluation = cbIdEvaluation.getValue();

            if (decisionEnCours == null) {
                DecisionFinanciere nouvelleDecision = new DecisionFinanciere(statut, justification, dateDecision, idEvaluation);
                service.ajouter(nouvelleDecision);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Décision ajoutée avec succès!");
            } else {
                decisionEnCours.setStatut(statut);
                decisionEnCours.setJustification(justification);
                decisionEnCours.setDateDecision(dateDecision);
                decisionEnCours.setIdEvaluation(idEvaluation);
                service.modifier(decisionEnCours);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Décision modifiée avec succès!");
            }

            closeWindow();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (cbStatut.getValue() == null) {
            errors.append("- Le statut est obligatoire\n");
        }

        if (dpDateDecision.getValue() == null) {
            errors.append("- La date de décision est obligatoire\n");
        }

        if (taJustification.getText().isEmpty()) {
            errors.append("- La justification est obligatoire\n");
        }

        if (cbIdEvaluation.getValue() == null) {
            errors.append("- L'évaluation est obligatoire\n");
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
        cbStatut.setValue(null);
        dpDateDecision.setValue(null);
        taJustification.clear();
        cbIdEvaluation.setValue(null);

        decisionEnCours = null;
        updateStatus("Formulaire effacé");
    }

    public void loadDecision(int idDecision) {
        try {
            DecisionFinanciere decision = service.getById(idDecision);

            if (decision != null) {
                decisionEnCours = decision;

                tfIdDecision.setText(String.valueOf(decision.getIdDecision()));
                cbStatut.setValue(decision.getStatut());

                java.util.Date utilDate = new java.util.Date(decision.getDateDecision().getTime());
                dpDateDecision.setValue(utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

                taJustification.setText(decision.getJustification());
                cbIdEvaluation.setValue(decision.getIdEvaluation());

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
