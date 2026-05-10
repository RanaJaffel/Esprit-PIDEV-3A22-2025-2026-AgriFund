package com.agrifund.controller.banque;

import com.agrifund.entities.Banque;
import com.agrifund.entities.DecisionFinanciere;
import com.agrifund.entities.Utilisateur;
import com.agrifund.services.BanqueService;
import com.agrifund.services.ServiceDecisionFinanciere;
import com.agrifund.services.ServiceEvaluationRisque;
import com.agrifund.services.ServiceProjectAgricoleChedy;
import com.agrifund.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import com.agrifund.entities.EvaluationRisque;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BanqueDecisionController {

    @FXML private TextField tfIdDecision;
    @FXML private ComboBox<String> cbEvaluation; // Affiche "Projet X - Score Y - Niveau Z"
    @FXML private ComboBox<String> cbStatut;
    @FXML private DatePicker dpDateDecision;
    @FXML private TextArea taJustification;
    @FXML private Label lblStatus;
    @FXML private Label lblBanqueInfo;

    private ServiceDecisionFinanciere serviceDecision;
    private ServiceEvaluationRisque serviceEvaluation;
    private ServiceProjectAgricoleChedy serviceProjet;
    private BanqueService banqueService;

    private DecisionFinanciere decisionEnCours;
    private Banque currentBanque;
    private Map<String, Integer> evaluationDisplayMap; // "Display text" -> idEvaluation

    @FXML
    public void initialize() {
        serviceDecision = new ServiceDecisionFinanciere();
        serviceEvaluation = new ServiceEvaluationRisque();
        serviceProjet = new ServiceProjectAgricoleChedy();
        evaluationDisplayMap = new HashMap<>();

        try {
            banqueService = new BanqueService();
            loadCurrentBanque();
            setupComboBoxes();
            loadEvaluationsForBanque();
            setupDatePicker();
            updateStatus("Prêt");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'initialisation: " + e.getMessage());
        }
    }

    private void loadCurrentBanque() throws SQLException {
        Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
        currentBanque = banqueService.rechercherParUtilisateurId(user.getId());

        if (currentBanque != null && lblBanqueInfo != null) {
            lblBanqueInfo.setText("🏦 " + currentBanque.getNom() + " [" + currentBanque.getCodeBanque() + "]");
        }
    }

    private void setupComboBoxes() {
        cbStatut.getItems().addAll("En attente", "Approuvé", "Rejeté");
    }

    private void loadEvaluationsForBanque() throws SQLException {
        evaluationDisplayMap.clear();
        cbEvaluation.getItems().clear();

        // Récupérer uniquement les évaluations de cette banque
        List<EvaluationRisque> evaluations = serviceEvaluation.afficherParBanque(currentBanque.getUtilisateurId());

        for (EvaluationRisque eval : evaluations) {
            String nomProjet = serviceProjet.getNameById(eval.getIdProjet());
            if (nomProjet == null) nomProjet = "Projet #" + eval.getIdProjet();

            // Format d'affichage : "NomProjet - Score: XX - Risque: Niveau"
            String displayText = String.format("%s - Score: %d - Risque: %s",
                    nomProjet,
                    eval.getScoreGlobal(),
                    eval.getNiveauRisque());

            evaluationDisplayMap.put(displayText, eval.getIdEvaluation());
            cbEvaluation.getItems().add(displayText);
        }

        if (evaluations.isEmpty()) {
            cbEvaluation.setPromptText("Aucune évaluation disponible");
            cbEvaluation.setDisable(true);
        }
    }

    private void setupDatePicker() {
        // Désactiver les dates passées
        dpDateDecision.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) < 0);
            }
        });

        // Formater l'affichage
        dpDateDecision.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                return date != null ? fmt.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, fmt) : null;
            }
        });

        // Date par défaut : aujourd'hui
        dpDateDecision.setValue(LocalDate.now());
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateFields()) return;

        try {
            String evalDisplay = cbEvaluation.getValue();
            int idEvaluation = evaluationDisplayMap.get(evalDisplay);

            String statut = cbStatut.getValue();
            String justification = taJustification.getText().trim();
            Date dateDecision = java.sql.Date.valueOf(dpDateDecision.getValue());

            if (decisionEnCours == null) {
                // Nouvelle décision
                DecisionFinanciere newDecision = new DecisionFinanciere(
                        statut,
                        justification,
                        dateDecision,
                        idEvaluation,
                        currentBanque.getUtilisateurId()
                );
                serviceDecision.ajouter(newDecision);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "✅ Décision enregistrée!\n\n📧 Un email a été envoyé à l'agriculteur concerné.");
            } else {
                // Modification
                decisionEnCours.setStatut(statut);
                decisionEnCours.setJustification(justification);
                decisionEnCours.setDateDecision(dateDecision);
                decisionEnCours.setIdEvaluation(idEvaluation);

                serviceDecision.modifier(decisionEnCours);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "✅ Décision modifiée!\n\n📧 L'agriculteur a été notifié de la mise à jour.");
            }

            closeWindow();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", e.getMessage());
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (cbEvaluation.getValue() == null) {
            errors.append("- Sélectionnez une évaluation\n");
        }
        if (cbStatut.getValue() == null) {
            errors.append("- Le statut est obligatoire\n");
        }
        if (dpDateDecision.getValue() == null) {
            errors.append("- La date est obligatoire\n");
        }
        if (taJustification.getText().trim().isEmpty()) {
            errors.append("- La justification est obligatoire\n");
        }
        if (taJustification.getText().trim().length() < 20) {
            errors.append("- La justification doit contenir au moins 20 caractères\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Champs incomplets", errors.toString());
            return false;
        }
        return true;
    }

    public void loadDecision(int idDecision) {
        try {
            decisionEnCours = serviceDecision.getById(idDecision);

            if (decisionEnCours != null) {
                tfIdDecision.setText(String.valueOf(decisionEnCours.getIdDecision()));
                cbStatut.setValue(decisionEnCours.getStatut());
                taJustification.setText(decisionEnCours.getJustification());

                // Retrouver l'affichage de l'évaluation
                EvaluationRisque eval = serviceEvaluation.getById(decisionEnCours.getIdEvaluation());
                if (eval != null) {
                    String nomProjet = serviceProjet.getNameById(eval.getIdProjet());
                    if (nomProjet == null) nomProjet = "Projet #" + eval.getIdProjet();

                    String displayText = String.format("%s - Score: %d - Risque: %s",
                            nomProjet,
                            eval.getScoreGlobal(),
                            eval.getNiveauRisque());

                    cbEvaluation.setValue(displayText);
                }

                // Date
                java.sql.Date sqlDate = new java.sql.Date(decisionEnCours.getDateDecision().getTime());
                dpDateDecision.setValue(sqlDate.toLocalDate());

                updateStatus("Mode modification - Décision #" + idDecision);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Décision introuvable!");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    @FXML
    private void handleClear(ActionEvent event) {
        tfIdDecision.clear();
        cbEvaluation.setValue(null);
        cbStatut.setValue(null);
        dpDateDecision.setValue(LocalDate.now());
        taJustification.clear();
        decisionEnCours = null;
        updateStatus("Formulaire effacé");
    }

    private void closeWindow() {
        Stage stage = (Stage) tfIdDecision.getScene().getWindow();
        stage.close();
    }

    private void updateStatus(String msg) {
        if (lblStatus != null) lblStatus.setText(msg);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
