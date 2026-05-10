package com.agrifund.controller.banque;

import com.agrifund.entities.Banque;
import com.agrifund.entities.Utilisateur;
import com.agrifund.services.AIService;
import com.agrifund.entities.EvaluationRisque;
import com.agrifund.services.BanqueService;
import com.agrifund.services.ServiceEvaluationRisque;
import com.agrifund.services.ServiceProjectAgricoleChedy;
import com.agrifund.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.json.JSONObject;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class BanqueRisqueController {

    @FXML private TextField tfIdEvaluation;
    @FXML private ComboBox<String> cbNomProjet;  // ✅ ComboBox avec NOM du projet
    @FXML private TextField tfScoreGlobal;
    @FXML private ComboBox<String> cbNiveauRisque;
    @FXML private ComboBox<String> cbFiabiliteDonnees;
    @FXML private TextArea tfFacteurPrincipal;
    @FXML private ComboBox<String> cbRecommandation;
    @FXML private DatePicker dpDateEvaluation;
    @FXML private Label lblStatus;
    @FXML private Label lblBanqueInfo;

    private ServiceEvaluationRisque serviceEvaluation;
    private ServiceProjectAgricoleChedy serviceProjet;
    private BanqueService banqueService;

    private EvaluationRisque evaluationEnCours;
    private Banque currentBanque;
    private Map<String, Integer> projetNomIdMap;

    @FXML
    public void initialize() {
        serviceEvaluation = new ServiceEvaluationRisque();
        serviceProjet = new ServiceProjectAgricoleChedy();

        try {
            banqueService = new BanqueService();
            loadCurrentBanque();
            setupComboBoxes();
            loadProjectNames();
            setupDatePicker();
            updateStatus("Prêt");

            // ✅ Listener pour auto-évaluation AI quand on sélectionne un projet
            cbNomProjet.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    handleProjetSelection();
                }
            });

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
        cbNiveauRisque.getItems().addAll("Faible", "Moyen", "Élevé", "Critique");
        cbFiabiliteDonnees.getItems().addAll("Faible", "Moyenne", "Élevée");
        cbRecommandation.getItems().addAll(
                "Recommandé",
                "Surveillance",
                "Non recommandé",
                "Aucune"
        );
    }

    private void loadProjectNames() throws SQLException {
        projetNomIdMap = serviceProjet.getProjectNamesMap();
        List<String> noms = serviceProjet.getAllProjectNames();
        cbNomProjet.getItems().setAll(noms);
    }

    private void setupDatePicker() {
        dpDateEvaluation.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) < 0);
            }
        });

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
    }

    /**
     * ✅ Auto-remplissage via AI quand on sélectionne un projet
     */
    private void handleProjetSelection() {
        String nomProjet = cbNomProjet.getValue();
        if (nomProjet == null || !projetNomIdMap.containsKey(nomProjet)) return;

        int idProjet = projetNomIdMap.get(nomProjet);
        updateStatus("🔄 Analyse AI en cours...");

        // Appel AI
        String response = AIService.evaluateRisk(idProjet);

        if (response != null) {
            try {
                JSONObject json = new JSONObject(response);

                if (!json.has("error")) {
                    tfScoreGlobal.setText(String.valueOf(json.optInt("scoreGlobal", 0)));
                    cbNiveauRisque.setValue(json.optString("niveauRisque", "Faible"));
                    cbFiabiliteDonnees.setValue(json.optString("fiabiliteDonnees", "Moyenne"));
                    tfFacteurPrincipal.setText(json.optString("facteurPrincipal", ""));
                    cbRecommandation.setValue(json.optString("recommandation", "Aucune"));

                    updateStatus("✅ Analyse AI terminée");
                } else {
                    showAlert(Alert.AlertType.WARNING, "AI", json.getString("error"));
                    updateStatus("⚠️ Analyse partielle");
                }
            } catch (Exception e) {
                updateStatus("⚠️ Erreur AI - Remplissage manuel requis");
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateFields()) return;

        try {
            String nomProjet = cbNomProjet.getValue();
            int idProjet = projetNomIdMap.get(nomProjet);

            int scoreGlobal = Integer.parseInt(tfScoreGlobal.getText());
            String niveauRisque = cbNiveauRisque.getValue();
            String fiabiliteDonnees = cbFiabiliteDonnees.getValue();
            String facteurPrincipal = tfFacteurPrincipal.getText();
            String recommandation = cbRecommandation.getValue(); // ✅ String au lieu de int
            Date dateEvaluation = java.sql.Date.valueOf(dpDateEvaluation.getValue());

            if (evaluationEnCours == null) {
                // ✅ Nouvelle évaluation avec banqueId
                EvaluationRisque newEval = new EvaluationRisque(
                        scoreGlobal, niveauRisque, fiabiliteDonnees, facteurPrincipal,
                        recommandation, dateEvaluation, idProjet, currentBanque.getUtilisateurId()
                );
                serviceEvaluation.ajouter(newEval);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation ajoutée avec succès!");
            } else {
                evaluationEnCours.setScoreGlobal(scoreGlobal);
                evaluationEnCours.setNiveauRisque(niveauRisque);
                evaluationEnCours.setFiabiliteDonnees(fiabiliteDonnees);
                evaluationEnCours.setFacteurPrincipal(facteurPrincipal);
                evaluationEnCours.setRecommandation(recommandation); // ✅ String
                evaluationEnCours.setDateEvaluation(dateEvaluation);
                evaluationEnCours.setIdProjet(idProjet);

                serviceEvaluation.modifier(evaluationEnCours);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Évaluation modifiée!");
            }

            closeWindow();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Score global doit être un nombre!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", e.getMessage());
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (cbNomProjet.getValue() == null) errors.append("- Sélectionnez un projet\n");
        if (tfScoreGlobal.getText().isEmpty()) errors.append("- Le score global est obligatoire\n");
        if (cbNiveauRisque.getValue() == null) errors.append("- Le niveau de risque est obligatoire\n");
        if (cbFiabiliteDonnees.getValue() == null) errors.append("- La fiabilité est obligatoire\n");
        if (tfFacteurPrincipal.getText().isEmpty()) errors.append("- Les facteurs sont obligatoires\n");
        if (cbRecommandation.getValue() == null) errors.append("- La recommandation est obligatoire\n");
        if (dpDateEvaluation.getValue() == null) errors.append("- La date est obligatoire\n");

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Champs incomplets", errors.toString());
            return false;
        }
        return true;
    }

    public void loadEvaluation(int idEvaluation) {
        try {
            evaluationEnCours = serviceEvaluation.getById(idEvaluation);

            if (evaluationEnCours != null) {
                tfIdEvaluation.setText(String.valueOf(evaluationEnCours.getIdEvaluation()));
                tfScoreGlobal.setText(String.valueOf(evaluationEnCours.getScoreGlobal()));
                cbNiveauRisque.setValue(evaluationEnCours.getNiveauRisque());
                cbFiabiliteDonnees.setValue(evaluationEnCours.getFiabiliteDonnees());
                tfFacteurPrincipal.setText(evaluationEnCours.getFacteurPrincipal());

                // ✅ Récupérer le nom du projet
                String nomProjet = serviceProjet.getNameById(evaluationEnCours.getIdProjet());
                cbNomProjet.setValue(nomProjet);

                // ✅ Directement String
                cbRecommandation.setValue(evaluationEnCours.getRecommandation());

                java.sql.Date sqlDate = new java.sql.Date(evaluationEnCours.getDateEvaluation().getTime());
                dpDateEvaluation.setValue(sqlDate.toLocalDate());

                updateStatus("Mode modification");
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
        tfIdEvaluation.clear();
        tfScoreGlobal.clear();
        cbNomProjet.setValue(null);
        cbNiveauRisque.setValue(null);
        cbFiabiliteDonnees.setValue(null);
        tfFacteurPrincipal.clear();
        cbRecommandation.setValue(null);
        dpDateEvaluation.setValue(null);
        evaluationEnCours = null;
        updateStatus("Formulaire effacé");
    }

    private void closeWindow() {
        Stage stage = (Stage) tfIdEvaluation.getScene().getWindow();
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
