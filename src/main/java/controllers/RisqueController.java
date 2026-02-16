package controllers;

import entities.EvaluationRisque;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceEvaluationRisque;
import services.ServiceProjectAgricole;

import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class RisqueController {

    @FXML private TextField tfIdEvaluation;
    @FXML private ComboBox<Integer> cbIdProjet;
    @FXML private TextField tfScoreGlobal;
    @FXML private ComboBox<String> cbNiveauRisque;
    @FXML private ComboBox<String> cbFiabiliteDonnees;
    @FXML private TextField tfFacteurPrincipal;
    @FXML private ComboBox<String> cbRecommandation;
    @FXML private DatePicker dpDateEvaluation;
    @FXML private Label lblStatus;

    private ServiceEvaluationRisque service;
    private EvaluationRisque evaluationEnCours;

    @FXML
    public void initialize() {
        service = new ServiceEvaluationRisque();

        cbNiveauRisque.getItems().addAll("Faible", "Moyen", "Élevé", "Critique");
        cbFiabiliteDonnees.getItems().addAll("Faible", "Moyenne", "Élevée");
        cbRecommandation.getItems().addAll("Aucune", "Surveillance", "Action immédiate");
        loadProjetIds();
        updateStatus("Prêt");
    }

    private void loadProjetIds() {
        try {
            List<Integer> projetIds = new ServiceProjectAgricole().getAllProjectIds();
            cbIdProjet.getItems().setAll(projetIds);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les projets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        try {
            int scoreGlobal = Integer.parseInt(tfScoreGlobal.getText());
            String niveauRisque = cbNiveauRisque.getValue();
            String fiabiliteDonnees = cbFiabiliteDonnees.getValue();
            String facteurPrincipal = tfFacteurPrincipal.getText();
            int recommandation = cbRecommandation.getSelectionModel().getSelectedIndex();
            java.util.Date dateEvaluation = java.sql.Date.valueOf(dpDateEvaluation.getValue());
            int idProjet = cbIdProjet.getValue();

            if (evaluationEnCours == null) {
                EvaluationRisque nouvelleEvaluation = new EvaluationRisque(
                        scoreGlobal, niveauRisque, fiabiliteDonnees, facteurPrincipal,
                        recommandation, dateEvaluation, idProjet
                );
                service.ajouter(nouvelleEvaluation);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation ajoutée avec succès!");
            } else {
                evaluationEnCours.setScoreGlobal(scoreGlobal);
                evaluationEnCours.setNiveauRisque(niveauRisque);
                evaluationEnCours.setFiabiliteDonnees(fiabiliteDonnees);
                evaluationEnCours.setFacteurPrincipal(facteurPrincipal);
                evaluationEnCours.setRecommandation(recommandation);
                evaluationEnCours.setDateEvaluation(dateEvaluation);
                evaluationEnCours.setIdProjet(idProjet);
                service.modifier(evaluationEnCours);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation modifiée avec succès!");
            }

            closeWindow();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Les champs numériques doivent être valides!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données", "Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (tfScoreGlobal.getText().isEmpty()) {
            errors.append("- Le score global est obligatoire\n");
        }

        if (cbNiveauRisque.getValue() == null) {
            errors.append("- Le niveau de risque est obligatoire\n");
        }

        if (cbFiabiliteDonnees.getValue() == null) {
            errors.append("- La fiabilité des données est obligatoire\n");
        }

        if (tfFacteurPrincipal.getText().isEmpty()) {
            errors.append("- Le facteur principal est obligatoire\n");
        }

        if (cbRecommandation.getValue() == null) {
            errors.append("- La recommandation est obligatoire\n");
        }

        if (dpDateEvaluation.getValue() == null) {
            errors.append("- La date d'évaluation est obligatoire\n");
        }

        if (cbIdProjet.getValue() == null) {
            errors.append("- Le projet est obligatoire\n");
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
    private void handleClear(ActionEvent event) {
        tfIdEvaluation.clear();
        tfScoreGlobal.clear();
        cbNiveauRisque.setValue(null);
        cbFiabiliteDonnees.setValue(null);
        tfFacteurPrincipal.clear();
        cbRecommandation.setValue(null);
        dpDateEvaluation.setValue(null);
        cbIdProjet.setValue(null);

        evaluationEnCours = null;
        updateStatus("Formulaire effacé");
    }

    public void loadEvaluation(int idEvaluation) {
        try {
            EvaluationRisque evaluation = service.getById(idEvaluation);

            if (evaluation != null) {
                evaluationEnCours = evaluation;

                tfIdEvaluation.setText(String.valueOf(evaluation.getIdEvaluation()));
                tfScoreGlobal.setText(String.valueOf(evaluation.getScoreGlobal()));
                cbNiveauRisque.setValue(evaluation.getNiveauRisque());
                cbFiabiliteDonnees.setValue(evaluation.getFiabiliteDonnees());
                tfFacteurPrincipal.setText(evaluation.getFacteurPrincipal());
                cbRecommandation.setValue(getRecommandationString(evaluation.getRecommandation()));
                cbIdProjet.setValue(evaluation.getIdProjet());

                java.util.Date utilDate = new java.util.Date(evaluation.getDateEvaluation().getTime());
                dpDateEvaluation.setValue(utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

                updateStatus("Modification en cours");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune évaluation trouvée avec cet ID.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'évaluation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getRecommandationString(int recommandation) {
        switch (recommandation) {
            case 0: return "Aucune";
            case 1: return "Surveillance";
            case 2: return "Action immédiate";
            default: return "Inconnu";
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) tfIdEvaluation.getScene().getWindow();
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
