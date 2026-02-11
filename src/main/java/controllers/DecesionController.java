package controllers;

import entities.DecisionFinanciere;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.ServiceDecisionFinanciere;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

public class DecesionController {

    @FXML private VBox root;
    @FXML private TextField tfIdDecision;
    @FXML private TextField tfIdProjet;
    @FXML private ComboBox<String> cbStatut;
    @FXML private DatePicker dpDateDecision;
    @FXML private TextArea taJustification;

    @FXML private Label lblPreviewId;
    @FXML private Label lblPreviewProjet;
    @FXML private Label lblPreviewStatut;
    @FXML private Label lblPreviewDate;
    @FXML private TextArea taPreviewJustification;

    @FXML private Label lblTotalDecisions;
    @FXML private Label lblApprouves;
    @FXML private Label lblEnAttente;
    @FXML private Label lblStatus;

    private ServiceDecisionFinanciere service;
    private DecisionFinanciere decisionEnCours;

    @FXML
    public void initialize() {
        service = new ServiceDecisionFinanciere();
        cbStatut.getItems().addAll("En attente", "Approuvé", "Rejeté");

        tfIdProjet.textProperty().addListener((obs, old, newVal) -> updatePreview());
        cbStatut.valueProperty().addListener((obs, old, newVal) -> updatePreview());
        dpDateDecision.valueProperty().addListener((obs, old, newVal) -> updatePreview());
        taJustification.textProperty().addListener((obs, old, newVal) -> updatePreview());

        loadStatistics();
        updateStatus("Prêt");
    }

    private void updatePreview() {
        lblPreviewId.setText(tfIdDecision.getText().isEmpty() ? "-" : tfIdDecision.getText());
        lblPreviewProjet.setText(tfIdProjet.getText().isEmpty() ? "-" : tfIdProjet.getText());
        lblPreviewStatut.setText(cbStatut.getValue() == null ? "-" : cbStatut.getValue());
        lblPreviewDate.setText(dpDateDecision.getValue() == null ? "-" : dpDateDecision.getValue().toString());
        taPreviewJustification.setText(taJustification.getText().isEmpty() ? "-" : taJustification.getText());
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        try {
            int idProjet = Integer.parseInt(tfIdProjet.getText());
            String statut = cbStatut.getValue();
            Date dateDecision = Date.from(dpDateDecision.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            String justification = taJustification.getText();

            if (decisionEnCours == null) {
                DecisionFinanciere nouvelleDecision = new DecisionFinanciere(statut, justification, dateDecision, idProjet);
                service.ajouter(nouvelleDecision);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Décision ajoutée avec succès!");
                updateStatus("Décision ajoutée");
            } else {
                decisionEnCours.setIdProjet(idProjet);
                decisionEnCours.setStatut(statut);
                decisionEnCours.setDateDecision(dateDecision);
                decisionEnCours.setJustification(justification);
                service.modifier(decisionEnCours);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Décision modifiée avec succès!");
                updateStatus("Décision modifiée");
            }

            handleClear(null);
            loadStatistics();

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
    private void handleCancel() {
        if (!tfIdProjet.getText().isEmpty() || cbStatut.getValue() != null ||
                dpDateDecision.getValue() != null || !taJustification.getText().isEmpty()) {

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
        tfIdDecision.clear();
        tfIdProjet.clear();
        cbStatut.setValue(null);
        dpDateDecision.setValue(null);
        taJustification.clear();

        decisionEnCours = null;

        lblPreviewId.setText("-");
        lblPreviewProjet.setText("-");
        lblPreviewStatut.setText("-");
        lblPreviewDate.setText("-");
        taPreviewJustification.setText("-");

        updateStatus("Formulaire effacé");
    }

    @FXML
    private void handleViewList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DecisionList.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Liste des Décisions Financières");
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

    public void loadDecision(int idDecision) {
        try {
            DecisionFinanciere decision = service.getById(idDecision);

            if (decision != null) {
                decisionEnCours = decision;

                tfIdDecision.setText(String.valueOf(decision.getIdDecision()));
                tfIdProjet.setText(String.valueOf(decision.getIdProjet()));
                cbStatut.setValue(decision.getStatut());

                // Correction : Conversion de java.sql.Date en LocalDate
                java.util.Date utilDate = new java.util.Date(decision.getDateDecision().getTime());
                dpDateDecision.setValue(utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

                taJustification.setText(decision.getJustification());
                updateStatus("Modification en cours");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la décision: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void deleteDecision(int idDecision) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer cette décision?");
        alert.setContentText("Cette action est irréversible!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimer(idDecision);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Décision supprimée avec succès!");
                handleClear(null);
                loadStatistics();
                updateStatus("Décision supprimée");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void loadStatistics() {
        try {
            var decisions = service.afficher();

            int total = decisions.size();
            int approuves = (int) decisions.stream().filter(d -> "Approuvé".equals(d.getStatut())).count();
            int enAttente = (int) decisions.stream().filter(d -> "En attente".equals(d.getStatut())).count();

            lblTotalDecisions.setText(String.valueOf(total));
            lblApprouves.setText(String.valueOf(approuves));
            lblEnAttente.setText(String.valueOf(enAttente));

        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des statistiques: " + e.getMessage());
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
