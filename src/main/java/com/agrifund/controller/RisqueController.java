package com.agrifund.controller;

import com.agrifund.entities.EvaluationRisque;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.json.JSONObject;
import com.agrifund.services.AIService;
import com.agrifund.services.ServiceEvaluationRisque;
import com.agrifund.services.ServiceProjectAgricoleChedy;
import com.agrifund.entities.EvaluationRisque;           // ✅ IMPORT MANQUANT AJOUTÉ

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class RisqueController {

    @FXML private TextField tfIdEvaluation;
    @FXML private ComboBox<Integer> cbIdProjet;
    @FXML private TextField tfScoreGlobal;
    @FXML private ComboBox<String> cbNiveauRisque;
    @FXML private ComboBox<String> cbFiabiliteDonnees;
    @FXML private TextArea tfFacteurPrincipal;
    @FXML private ComboBox<String> cbRecommandation;
    @FXML private DatePicker dpDateEvaluation;
    @FXML private Label lblStatus;

    private ServiceEvaluationRisque service;
    private EvaluationRisque evaluationEnCours;

    // ✅ Les valeurs du ComboBox correspondent exactement à ce qui est stocké en DB
    private static final String REC_RECOMMANDE       = "Je recommande ce projet";
    private static final String REC_SURVEILLANCE     = "Je recommande ce projet avec surveillance";
    private static final String REC_PAS_RECOMMANDE   = "Je ne recommande pas ce projet";
    private static final String REC_AUCUNE           = "Aucune";

    @FXML
    public void initialize() {
        service = new ServiceEvaluationRisque();

        cbNiveauRisque.getItems().addAll("Faible", "Moyen", "Élevé");
        cbFiabiliteDonnees.getItems().addAll("Faible", "Moyenne", "Élevée");
        cbRecommandation.getItems().addAll(
                REC_RECOMMANDE,
                REC_SURVEILLANCE,
                REC_PAS_RECOMMANDE,
                REC_AUCUNE
        );

        // Désactiver les dates passées
        dpDateEvaluation.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) < 0);
            }
        });

        // Formater la date
        dpDateEvaluation.setConverter(new StringConverter<LocalDate>() {
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

        loadProjetIds();
        updateStatus("Prêt");

        cbIdProjet.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handleIdProjetSelection();
            }
        });
    }

    private void loadProjetIds() {
        List<Integer> projetIds = new ServiceProjectAgricoleChedy().getAllProjectIds();
        cbIdProjet.getItems().setAll(projetIds);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateFields()) return;

        try {
            int scoreGlobal          = Integer.parseInt(tfScoreGlobal.getText().trim());
            String niveauRisque      = cbNiveauRisque.getValue();
            String fiabiliteDonnees  = cbFiabiliteDonnees.getValue();
            String facteurPrincipal  = tfFacteurPrincipal.getText();
            String recommandation    = cbRecommandation.getValue(); // ✅ String directement
            Date dateEvaluation      = java.sql.Date.valueOf(dpDateEvaluation.getValue());
            int idProjet             = cbIdProjet.getValue();

            if (evaluationEnCours == null) {
                // ✅ Constructeur avec String recommandation
                EvaluationRisque nouvelleEvaluation = new EvaluationRisque(
                        scoreGlobal, niveauRisque, fiabiliteDonnees,
                        facteurPrincipal, recommandation, dateEvaluation, idProjet
                );
                service.ajouter(nouvelleEvaluation);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation ajoutée avec succès!");
            } else {
                evaluationEnCours.setScoreGlobal(scoreGlobal);
                evaluationEnCours.setNiveauRisque(niveauRisque);
                evaluationEnCours.setFiabiliteDonnees(fiabiliteDonnees);
                evaluationEnCours.setFacteurPrincipal(facteurPrincipal);
                evaluationEnCours.setRecommandation(recommandation); // ✅ String
                evaluationEnCours.setDateEvaluation(dateEvaluation);
                evaluationEnCours.setIdProjet(idProjet);
                service.modifier(evaluationEnCours);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation modifiée avec succès!");
            }

            closeWindow();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Le score global doit être un nombre entier valide!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (tfScoreGlobal.getText() == null || tfScoreGlobal.getText().trim().isEmpty())
            errors.append("- Le score global est obligatoire\n");
        if (cbNiveauRisque.getValue() == null)
            errors.append("- Le niveau de risque est obligatoire\n");
        if (cbFiabiliteDonnees.getValue() == null)
            errors.append("- La fiabilité des données est obligatoire\n");
        if (tfFacteurPrincipal.getText() == null || tfFacteurPrincipal.getText().trim().isEmpty())
            errors.append("- Les facteurs principaux sont obligatoires\n");
        if (cbRecommandation.getValue() == null)
            errors.append("- La recommandation est obligatoire\n");
        if (dpDateEvaluation.getValue() == null)
            errors.append("- La date d'évaluation est obligatoire\n");
        if (cbIdProjet.getValue() == null)
            errors.append("- Le projet est obligatoire\n");

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Champs incomplets", errors.toString());
            return false;
        }

        // Valider que scoreGlobal est bien un entier
        try {
            int score = Integer.parseInt(tfScoreGlobal.getText().trim());
            if (score < 0 || score > 100) {
                showAlert(Alert.AlertType.WARNING, "Valeur invalide",
                        "Le score global doit être entre 0 et 100.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Valeur invalide",
                    "Le score global doit être un nombre entier.");
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

                // ✅ recommandation est déjà un String — on le met directement
                cbRecommandation.setValue(evaluation.getRecommandation());

                cbIdProjet.setValue(evaluation.getIdProjet());

                java.sql.Date sqlDate = new java.sql.Date(evaluation.getDateEvaluation().getTime());
                dpDateEvaluation.setValue(sqlDate.toLocalDate());

                updateStatus("Modification en cours");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Aucune évaluation trouvée avec cet ID.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger l'évaluation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) tfIdEvaluation.getScene().getWindow();
        stage.close();
    }

    private void updateStatus(String message) {
        if (lblStatus != null) lblStatus.setText(message);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleIdProjetSelection() {
        if (cbIdProjet.getValue() == null) return;

        int idProjet = cbIdProjet.getValue();
        System.out.println("ID Projet sélectionné: " + idProjet);

        String response = AIService.evaluateRisk(idProjet);
        System.out.println("Réponse de l'API: " + response);

        if (response == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de récupérer les données d'évaluation.");
            return;
        }

        try {
            JSONObject jsonResponse = new JSONObject(response);

            if (jsonResponse.has("error")) {
                showAlert(Alert.AlertType.WARNING, "Attention",
                        jsonResponse.getString("error"));
                return;
            }

            tfScoreGlobal.setText(String.valueOf(jsonResponse.optInt("scoreGlobal", 0)));
            cbNiveauRisque.setValue(jsonResponse.optString("niveauRisque", "Faible"));
            cbFiabiliteDonnees.setValue(jsonResponse.optString("fiabiliteDonnees", "Faible"));
            tfFacteurPrincipal.setText(jsonResponse.optString("facteurPrincipal", "Aucune donnée récente"));

            // ✅ recommandation vient de l'IA en String — on le met directement
            String recommandation = jsonResponse.optString("recommandation", REC_AUCUNE);
            System.out.println("Recommandation reçue: " + recommandation);

            // Si la valeur IA n'est pas dans le ComboBox, on met "Aucune" par défaut
            if (cbRecommandation.getItems().contains(recommandation)) {
                cbRecommandation.setValue(recommandation);
            } else {
                cbRecommandation.setValue(REC_AUCUNE);
                System.out.println("⚠ Recommandation IA non reconnue: '" + recommandation
                        + "' → remplacée par '" + REC_AUCUNE + "'");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors du traitement de la réponse: " + e.getMessage());
            e.printStackTrace();
        }
    }
}