package com.agrifund.controller.agriculteur;

import com.agrifund.entities.Agriculteur;
import com.agrifund.entities.Utilisateur;
import com.agrifund.entities.projectagricole;
import com.agrifund.entities.ressourceproject;
import com.agrifund.services.AgriculteurService;
import com.agrifund.services.projectagricoleCRUD;
import com.agrifund.services.ressourceprojectCRUD;
import com.agrifund.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AgriculteurRessourceFormController implements Initializable {

    @FXML private ComboBox<String> cbIdProjectDialog;
    @FXML private TextField tfNomRessourceDialog;
    @FXML private ComboBox<String> cbTypeRessourceDialog;
    @FXML private TextField tfQuantiteDialog;
    @FXML private TextField tfCoutDialog;
    @FXML private TextField tfFournisseurDialog;
    @FXML private ComboBox<String> cbStatutRessourceDialog;
    @FXML private DatePicker dpDateAjoutDialog;
    @FXML private Button btnSave;
    @FXML private Label lblTitle;
    @FXML private Label lblSubtitle;

    private ressourceprojectCRUD rService;
    private projectagricoleCRUD pService;
    private AgriculteurService agriculteurService;

    private Map<String, Integer> projectNameToIdMap = new HashMap<>();
    private ressourceproject currentRessource = null;
    private String dialogMode = "add";

    private AgriculteurRessourceController mainController;

    private Utilisateur currentUser;
    private Agriculteur currentAgriculteur;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("========================================");
        System.out.println("INITIALISATION FORMULAIRE RESSOURCE");
        System.out.println("========================================");

        try {
            rService = new ressourceprojectCRUD();
            pService = new projectagricoleCRUD();
            agriculteurService = new AgriculteurService();

            // Récupérer l'utilisateur connecté
            currentUser = SessionManager.getInstance().getUtilisateurConnecte();

            if (currentUser == null) {
                System.err.println("[ERREUR] Aucun utilisateur connecté!");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez vous reconnecter.");
                return;
            }

            System.out.println("[OK] Utilisateur connecté:");
            System.out.println("    - utilisateur.id = " + currentUser.getId());
            System.out.println("    - Nom: " + currentUser.getNom() + " " + currentUser.getPrenom());

            // Récupérer l'agriculteur
            currentAgriculteur = agriculteurService.rechercherParUtilisateurId(currentUser.getId());

            if (currentAgriculteur == null) {
                System.err.println("[ERREUR] Aucun agriculteur pour utilisateur_id: " + currentUser.getId());
                showAlert(Alert.AlertType.ERROR, "Erreur", "Profil agriculteur non trouvé.");
                return;
            }

            // IMPORTANT : Utiliser getAgriculteurId() et NON getId()
            System.out.println("[OK] Agriculteur trouvé:");
            System.out.println("    - agriculteur.getAgriculteurId() = " + currentAgriculteur.getAgriculteurId());
            System.out.println("    - agriculteur.getUtilisateurId() = " + currentAgriculteur.getUtilisateurId());
            System.out.println("    - agriculteur.getId() (parent) = " + currentAgriculteur.getId());

            // Initialiser ComboBox
            setupComboBoxes();

            // Charger les projets
            loadProjects();

            if (dpDateAjoutDialog != null) {
                dpDateAjoutDialog.setValue(LocalDate.now());
            }

            System.out.println("[OK] Initialisation terminée");
            System.out.println("========================================");

        } catch (SQLException e) {
            System.err.println("[ERREUR SQL] " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void setupComboBoxes() {
        if (cbTypeRessourceDialog != null) {
            cbTypeRessourceDialog.getItems().clear();
            cbTypeRessourceDialog.getItems().addAll("equipement", "materiaux", "service");
        }
        if (cbStatutRessourceDialog != null) {
            cbStatutRessourceDialog.getItems().clear();
            cbStatutRessourceDialog.getItems().addAll("prevu", "achete");
            cbStatutRessourceDialog.setValue("prevu");
        }
    }

    private void loadProjects() {
        if (cbIdProjectDialog == null) {
            System.err.println("[ERREUR] cbIdProjectDialog est NULL!");
            return;
        }

        cbIdProjectDialog.getItems().clear();
        projectNameToIdMap.clear();

        if (currentAgriculteur == null) {
            System.err.println("[ERREUR] currentAgriculteur est NULL!");
            return;
        }

        try {
            // =============================================
            // CORRECTION ICI : Utiliser getAgriculteurId()
            // =============================================
            int agriculteurId = currentAgriculteur.getAgriculteurId();

            System.out.println("[INFO] Chargement des projets pour agriculteur_id: " + agriculteurId);

            // Debug : afficher tous les projets
            List<projectagricole> tousProjets = pService.afficher();
            System.out.println("[DEBUG] Tous les projets dans la BDD:");
            for (projectagricole p : tousProjets) {
                System.out.println("    - " + p.getNomproject() +
                        " (idproject=" + p.getIdproject() +
                        ", agriculteur_id=" + p.getAgriculteurId() + ")");
            }

            // Charger les projets de cet agriculteur
            List<projectagricole> mesProjets = pService.afficherParAgriculteur(agriculteurId);

            System.out.println("[INFO] Projets trouvés pour agriculteur_id=" + agriculteurId + ": " + mesProjets.size());

            if (mesProjets.isEmpty()) {
                cbIdProjectDialog.setPromptText("Aucun projet disponible");
                showAlert(Alert.AlertType.WARNING, "Aucun projet",
                        "Vous n'avez aucun projet.\nCréez d'abord un projet.");
            } else {
                for (projectagricole p : mesProjets) {
                    projectNameToIdMap.put(p.getNomproject(), p.getIdproject());
                    cbIdProjectDialog.getItems().add(p.getNomproject());
                    System.out.println("    - Ajouté: " + p.getNomproject());
                }
                cbIdProjectDialog.setPromptText("Sélectionner un projet...");
            }

        } catch (SQLException e) {
            System.err.println("[ERREUR SQL] " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================================================================
    // SETTERS
    // ============================================================================
    public void setRessource(ressourceproject ressource) {
        this.currentRessource = ressource;
        this.dialogMode = "modify";
        if (lblTitle != null) lblTitle.setText("Modifier la Ressource");
        if (lblSubtitle != null) lblSubtitle.setText("Mettre à jour les informations");
        populateFields();
    }

    public void setMainController(AgriculteurRessourceController controller) {
        this.mainController = controller;
    }

    public void setDialogMode(String mode) {
        this.dialogMode = mode;
    }

    private void populateFields() {
        if (currentRessource == null) return;

        int projectId = currentRessource.getIdproject();
        String projectName = projectNameToIdMap.entrySet().stream()
                .filter(e -> e.getValue() == projectId)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (projectName != null) cbIdProjectDialog.setValue(projectName);

        tfNomRessourceDialog.setText(currentRessource.getNomressource());
        cbTypeRessourceDialog.setValue(currentRessource.getTyperessource());
        tfQuantiteDialog.setText(String.valueOf(currentRessource.getQuantite()));
        tfCoutDialog.setText(currentRessource.getCout().toString());
        tfFournisseurDialog.setText(currentRessource.getFournisseur());
        cbStatutRessourceDialog.setValue(currentRessource.getStatut());

        if (currentRessource.getDateajout() != null) {
            dpDateAjoutDialog.setValue(currentRessource.getDateajout().toLocalDate());
        }
    }

    // ============================================================================
    // HANDLERS
    // ============================================================================
    @FXML
    void handleSave(ActionEvent event) {
        if (!validateInputs()) return;

        try {
            String selectedProjectName = cbIdProjectDialog.getValue();
            Integer projectId = projectNameToIdMap.get(selectedProjectName);

            if (projectId == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Projet introuvable!");
                return;
            }

            if ("modify".equals(dialogMode) && currentRessource != null) {
                currentRessource.setIdproject(projectId);
                currentRessource.setNomressource(tfNomRessourceDialog.getText().trim());
                currentRessource.setTyperessource(cbTypeRessourceDialog.getValue());
                currentRessource.setQuantite(Integer.parseInt(tfQuantiteDialog.getText().trim()));
                currentRessource.setCout(new BigDecimal(tfCoutDialog.getText().trim()));
                currentRessource.setFournisseur(tfFournisseurDialog.getText().trim());
                currentRessource.setStatut(cbStatutRessourceDialog.getValue());
                currentRessource.setDateajout(Date.valueOf(dpDateAjoutDialog.getValue()));

                rService.modifier(currentRessource);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource modifiée!");
            } else {
                ressourceproject newRessource = new ressourceproject(
                        tfNomRessourceDialog.getText().trim(),
                        cbTypeRessourceDialog.getValue(),
                        Integer.parseInt(tfQuantiteDialog.getText().trim()),
                        new BigDecimal(tfCoutDialog.getText().trim()),
                        tfFournisseurDialog.getText().trim(),
                        cbStatutRessourceDialog.getValue(),
                        Date.valueOf(dpDateAjoutDialog.getValue()),
                        projectId
                );
                rService.ajouter(newRessource);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource ajoutée!");
            }

            if (mainController != null) mainController.refreshDataFromDB();
            closeDialog();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (cbIdProjectDialog.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Sélectionnez un projet!");
            return false;
        }
        if (tfNomRessourceDialog.getText() == null || tfNomRessourceDialog.getText().trim().length() < 2) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Nom invalide (min 2 caractères)!");
            return false;
        }
        if (cbTypeRessourceDialog.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Sélectionnez un type!");
            return false;
        }
        try {
            int q = Integer.parseInt(tfQuantiteDialog.getText().trim());
            if (q <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Quantité invalide!");
            return false;
        }
        try {
            BigDecimal c = new BigDecimal(tfCoutDialog.getText().trim());
            if (c.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Coût invalide!");
            return false;
        }
        if (tfFournisseurDialog.getText() == null || tfFournisseurDialog.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Saisissez le fournisseur!");
            return false;
        }
        if (cbStatutRessourceDialog.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Sélectionnez un statut!");
            return false;
        }
        if (dpDateAjoutDialog.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Sélectionnez une date!");
            return false;
        }
        return true;
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeDialog();
    }

    private void closeDialog() {
        if (btnSave != null && btnSave.getScene() != null) {
            Stage stage = (Stage) btnSave.getScene().getWindow();
            stage.close();
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
