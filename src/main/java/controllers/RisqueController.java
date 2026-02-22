package controllers;

import entities.EvaluationRisque;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.json.JSONObject;
import services.AIService;
import services.ServiceEvaluationRisque;
import services.ServiceProjectAgricole;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
        cbNiveauRisque.getItems().addAll("Faible", "Moyen", "Élevé");
        cbFiabiliteDonnees.getItems().addAll("Faible", "Moyenne", "Élevée");
        cbRecommandation.getItems().addAll("Je recommande ce projet", "Je recommande ce projet avec surveillance", "Je ne recommande pas ce projet", "Aucune");

        // Désactiver les dates passées pour dpDateEvaluation
        dpDateEvaluation.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.compareTo(today) < 0);
            }
        });

        // Formater l'affichage de la date
        dpDateEvaluation.setConverter(new StringConverter<LocalDate>() {
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

        loadProjetIds();
        updateStatus("Prêt");

        // Écouteur pour la sélection d'un projet
        cbIdProjet.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                handleIdProjetSelection();
            }
        });
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
        if (!validateFields()) return;

        try {
            int scoreGlobal = Integer.parseInt(tfScoreGlobal.getText());
            String niveauRisque = cbNiveauRisque.getValue();
            String fiabiliteDonnees = cbFiabiliteDonnees.getValue();
            String facteurPrincipal = tfFacteurPrincipal.getText();
            int recommandation = cbRecommandation.getSelectionModel().getSelectedIndex();
            Date dateEvaluation = java.sql.Date.valueOf(dpDateEvaluation.getValue());
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

        if (tfScoreGlobal.getText().isEmpty()) errors.append("- Le score global est obligatoire\n");
        if (cbNiveauRisque.getValue() == null) errors.append("- Le niveau de risque est obligatoire\n");
        if (cbFiabiliteDonnees.getValue() == null) errors.append("- La fiabilité des données est obligatoire\n");
        if (tfFacteurPrincipal.getText().isEmpty()) errors.append("- Le facteur principal est obligatoire\n");
        if (cbRecommandation.getValue() == null) errors.append("- La recommandation est obligatoire\n");
        if (dpDateEvaluation.getValue() == null) errors.append("- La date d'évaluation est obligatoire\n");
        if (cbIdProjet.getValue() == null) errors.append("- Le projet est obligatoire\n");

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

                dpDateEvaluation.setValue(evaluation.getDateEvaluation()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

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
        if (cbIdProjet.getValue() != null) {
            int idProjet = cbIdProjet.getValue();
            System.out.println("ID Projet sélectionné: " + idProjet);
            String response = AIService.evaluateRisk(idProjet);
            System.out.println("Réponse de l'API: " + response);
            if (response != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.has("error")) {
                        showAlert(Alert.AlertType.WARNING, "Attention", jsonResponse.getString("error"));
                    } else {
                        tfScoreGlobal.setText(String.valueOf(jsonResponse.optInt("scoreGlobal", 0)));
                        cbNiveauRisque.setValue(jsonResponse.optString("niveauRisque", "Faible"));
                        cbFiabiliteDonnees.setValue(jsonResponse.optString("fiabiliteDonnees", "Faible"));
                        tfFacteurPrincipal.setText(jsonResponse.optString("facteurPrincipal", "Aucune donnée récente"));
                        String recommandation = jsonResponse.optString("recommandation", "Aucune");
                        System.out.println("Recommandation reçue: " + recommandation);
                        cbRecommandation.setValue(recommandation);
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du traitement de la réponse: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de récupérer les données d'évaluation.");
            }
        }
    }
}
