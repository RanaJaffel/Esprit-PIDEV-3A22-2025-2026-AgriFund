package controllers;

import entities.EvaluationRisque;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.ServiceEvaluationRisque;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

public class RisqueController {

    @FXML private VBox root;
    @FXML private TextField tfIdEvaluation;
    @FXML private TextField tfIdProjet;
    @FXML private TextField tfScoreGlobal;
    @FXML private ComboBox<String> cbNiveauRisque;
    @FXML private ComboBox<String> cbFiabiliteDonnees;
    @FXML private TextField tfFacteurPrincipal;
    @FXML private ComboBox<String> cbRecommandation;
    @FXML private DatePicker dpDateEvaluation;

    @FXML private Label lblPreviewId;
    @FXML private Label lblPreviewProjet;
    @FXML private Label lblPreviewScore;
    @FXML private Label lblPreviewNiveau;
    @FXML private Label lblPreviewFiabilite;
    @FXML private Label lblPreviewFacteur;
    @FXML private Label lblPreviewRecommandation;
    @FXML private Label lblPreviewDate;
    @FXML private TextArea taPreviewFacteur;

    @FXML private Label lblStatus;

    private ServiceEvaluationRisque service;
    private EvaluationRisque evaluationEnCours;

    @FXML
    public void initialize() {
        service = new ServiceEvaluationRisque();

        // Initialiser les ComboBox
        cbNiveauRisque.getItems().addAll("Faible", "Moyen", "Élevé", "Critique");
        cbFiabiliteDonnees.getItems().addAll("Faible", "Moyenne", "Élevée");
        cbRecommandation.getItems().addAll("Aucune", "Surveillance", "Action immédiate");

        // Ajouter des listeners pour la mise à jour en temps réel de l'aperçu
        tfIdProjet.textProperty().addListener((obs, old, newVal) -> updatePreview());
        tfScoreGlobal.textProperty().addListener((obs, old, newVal) -> updatePreview());
        cbNiveauRisque.valueProperty().addListener((obs, old, newVal) -> updatePreview());
        cbFiabiliteDonnees.valueProperty().addListener((obs, old, newVal) -> updatePreview());
        tfFacteurPrincipal.textProperty().addListener((obs, old, newVal) -> updatePreview());
        cbRecommandation.valueProperty().addListener((obs, old, newVal) -> updatePreview());
        dpDateEvaluation.valueProperty().addListener((obs, old, newVal) -> updatePreview());

        updateStatus("Prêt");
    }

    private void updatePreview() {
        lblPreviewId.setText(tfIdEvaluation.getText().isEmpty() ? "-" : tfIdEvaluation.getText());
        lblPreviewProjet.setText(tfIdProjet.getText().isEmpty() ? "-" : tfIdProjet.getText());
        lblPreviewScore.setText(tfScoreGlobal.getText().isEmpty() ? "-" : tfScoreGlobal.getText());
        lblPreviewNiveau.setText(cbNiveauRisque.getValue() == null ? "-" : cbNiveauRisque.getValue());
        lblPreviewFiabilite.setText(cbFiabiliteDonnees.getValue() == null ? "-" : cbFiabiliteDonnees.getValue());
        lblPreviewFacteur.setText(tfFacteurPrincipal.getText().isEmpty() ? "-" : tfFacteurPrincipal.getText());
        lblPreviewRecommandation.setText(cbRecommandation.getValue() == null ? "-" : cbRecommandation.getValue());
        lblPreviewDate.setText(dpDateEvaluation.getValue() == null ? "-" : dpDateEvaluation.getValue().toString());
        taPreviewFacteur.setText(tfFacteurPrincipal.getText().isEmpty() ? "-" : tfFacteurPrincipal.getText());
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        try {
            int idProjet = Integer.parseInt(tfIdProjet.getText());
            int scoreGlobal = Integer.parseInt(tfScoreGlobal.getText());
            String niveauRisque = cbNiveauRisque.getValue();
            String fiabiliteDonnees = cbFiabiliteDonnees.getValue();
            String facteurPrincipal = tfFacteurPrincipal.getText();
            int recommandation = cbRecommandation.getSelectionModel().getSelectedIndex();
            Date dateEvaluation = Date.from(dpDateEvaluation.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

            if (evaluationEnCours == null) {
                EvaluationRisque nouvelleEvaluation = new EvaluationRisque(
                        scoreGlobal, niveauRisque, fiabiliteDonnees, facteurPrincipal,
                        recommandation, dateEvaluation, idProjet
                );
                service.ajouter(nouvelleEvaluation);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation ajoutée avec succès!");
                updateStatus("Évaluation ajoutée");
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
                updateStatus("Évaluation modifiée");
            }

            handleClear(null);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Les champs numériques doivent être valides!");
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

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Champs incomplets", errors.toString());
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        if (!tfIdProjet.getText().isEmpty() || !tfScoreGlobal.getText().isEmpty() ||
                cbNiveauRisque.getValue() != null || cbFiabiliteDonnees.getValue() != null ||
                !tfFacteurPrincipal.getText().isEmpty() || cbRecommandation.getValue() != null ||
                dpDateEvaluation.getValue() != null) {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Annuler les modifications?");
            alert.setContentText("Les données non enregistrées seront perdues.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                handleClear(null);
                updateStatus("Annulé");
            }
        } else {
            handleClear(null);
        }
    }

    @FXML
    public void handleClear(ActionEvent actionEvent) {
        tfIdEvaluation.clear();
        tfIdProjet.clear();
        tfScoreGlobal.clear();
        cbNiveauRisque.setValue(null);
        cbFiabiliteDonnees.setValue(null);
        tfFacteurPrincipal.clear();
        cbRecommandation.setValue(null);
        dpDateEvaluation.setValue(null);

        evaluationEnCours = null;

        lblPreviewId.setText("-");
        lblPreviewProjet.setText("-");
        lblPreviewScore.setText("-");
        lblPreviewNiveau.setText("-");
        lblPreviewFiabilite.setText("-");
        lblPreviewFacteur.setText("-");
        lblPreviewRecommandation.setText("-");
        lblPreviewDate.setText("-");
        taPreviewFacteur.setText("-");

        updateStatus("Formulaire effacé");
    }

    @FXML
    private void handleViewList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RisqueList.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Liste des Évaluations de Risque");
            stage.setScene(new Scene(root));
            stage.setMinWidth(1400);
            stage.setMinHeight(800);
            stage.show();

            updateStatus("Liste ouverte");

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la liste: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewDecisionList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DecisionList.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Liste des Décisions Financières");
            stage.setScene(new Scene(root));
            stage.setMinWidth(1400);
            stage.setMinHeight(800);
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la liste des décisions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadEvaluation(int idEvaluation) {
        try {
            EvaluationRisque evaluation = service.getById(idEvaluation);

            if (evaluation != null) {
                evaluationEnCours = evaluation;

                tfIdEvaluation.setText(String.valueOf(evaluation.getIdEvaluation()));
                tfIdProjet.setText(String.valueOf(evaluation.getIdProjet()));
                tfScoreGlobal.setText(String.valueOf(evaluation.getScoreGlobal()));
                cbNiveauRisque.setValue(evaluation.getNiveauRisque());
                cbFiabiliteDonnees.setValue(evaluation.getFiabiliteDonnees());
                tfFacteurPrincipal.setText(evaluation.getFacteurPrincipal());
                cbRecommandation.setValue(getRecommandationString(evaluation.getRecommandation()));
                java.util.Date utilDate = new java.util.Date(evaluation.getDateEvaluation().getTime());
                dpDateEvaluation.setValue(utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

                updateStatus("Modification en cours");
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
