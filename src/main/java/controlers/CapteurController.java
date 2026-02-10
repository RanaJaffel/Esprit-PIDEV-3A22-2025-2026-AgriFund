package controlers;

import entities.capteur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ServiceCapteur;

import java.sql.SQLException;

public class CapteurController {

    /* ============================
       TABLE & COLUMNS
       ============================ */
    @FXML private TableView<capteur> tableCapteur;
    @FXML private TableColumn<capteur, Integer> colId;
    @FXML private TableColumn<capteur, String> colType;
    @FXML private TableColumn<capteur, String> colLocalisation;
    @FXML private TableColumn<capteur, String> colStatut;

    /* ============================
       FORM FIELDS
       ============================ */
    @FXML private TextField tfType;
    @FXML private TextField tfLocalisation;
    @FXML private TextField tfProjet;
    @FXML private ComboBox<String> cbStatut;

    /* ============================
       SERVICES
       ============================ */
    private final ServiceCapteur service = new ServiceCapteur();

    /* ============================
       INITIALISATION
       ============================ */
    @FXML
    public void initialize() {

        // Liaison TableView ↔ Entité
        colId.setCellValueFactory(new PropertyValueFactory<>("idCapteur"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeCapteur"));
        colLocalisation.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Statuts possibles
        cbStatut.setItems(FXCollections.observableArrayList("ACTIF", "INACTIF"));

        chargerCapteurs();
        styliserStatut();
        gererSelection();
    }

    /* ============================
       CHARGEMENT DES DONNÉES
       ============================ */
    private void chargerCapteurs() {
        try {
            ObservableList<capteur> list =
                    FXCollections.observableArrayList(service.afficher());
            tableCapteur.setItems(list);
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement", e.getMessage());
        }
    }

    /* ============================
       AJOUT
       ============================ */
    @FXML
    private void ajouterCapteur() {
        if (!formulaireValide()) return;

        try {
            capteur c = new capteur();
            c.setTypeCapteur(tfType.getText());
            c.setLocalisation(tfLocalisation.getText());
            c.setStatut(cbStatut.getValue());
            c.setIdProjet(Integer.parseInt(tfProjet.getText()));

            service.ajouter(c);
            chargerCapteurs();
            clearForm();

        } catch (Exception e) {
            afficherErreur("Ajout impossible", e.getMessage());
        }
    }

    /* ============================
       MODIFICATION
       ============================ */
    @FXML
    private void modifierCapteur() {
        capteur selected = tableCapteur.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Modification", "Veuillez sélectionner un capteur");
            return;
        }

        if (!formulaireValide()) return;

        try {
            selected.setTypeCapteur(tfType.getText());
            selected.setLocalisation(tfLocalisation.getText());
            selected.setStatut(cbStatut.getValue());
            selected.setIdProjet(Integer.parseInt(tfProjet.getText()));

            service.modifier(selected);
            chargerCapteurs();
            clearForm();

        } catch (Exception e) {
            afficherErreur("Modification impossible", e.getMessage());
        }
    }

    /* ============================
       SUPPRESSION
       ============================ */
    @FXML
    private void supprimerCapteur() {
        capteur selected = tableCapteur.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Suppression", "Veuillez sélectionner un capteur");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le capteur ?");
        confirm.setContentText("Capteur : " + selected.getTypeCapteur());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                service.supprimer(selected.getIdCapteur());
                chargerCapteurs();
                clearForm();
            } catch (SQLException e) {
                afficherErreur("Suppression impossible", e.getMessage());
            }
        }
    }

    /* ============================
       SÉLECTION TABLE → FORM
       ============================ */
    private void gererSelection() {
        tableCapteur.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        tfType.setText(newVal.getTypeCapteur());
                        tfLocalisation.setText(newVal.getLocalisation());
                        cbStatut.setValue(newVal.getStatut());
                        tfProjet.setText(String.valueOf(newVal.getIdProjet()));
                    }
                }
        );
    }

    /* ============================
       BADGES DE STATUT
       ============================ */
    private void styliserStatut() {
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);

                if (empty || statut == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(statut);
                    badge.getStyleClass().add("badge");

                    if ("ACTIF".equalsIgnoreCase(statut)) {
                        badge.getStyleClass().add("badge-actif");
                    } else {
                        badge.getStyleClass().add("badge-inactif");
                    }
                    setGraphic(badge);
                }
            }
        });
    }

    /* ============================
       UTILITAIRES
       ============================ */
    private boolean formulaireValide() {
        if (tfType.getText().isEmpty()
                || tfLocalisation.getText().isEmpty()
                || tfProjet.getText().isEmpty()
                || cbStatut.getValue() == null) {

            afficherErreur("Formulaire invalide",
                    "Veuillez remplir tous les champs");
            return false;
        }
        return true;
    }

    private void clearForm() {
        tfType.clear();
        tfLocalisation.clear();
        tfProjet.clear();
        cbStatut.setValue(null);
        tableCapteur.getSelectionModel().clearSelection();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(titre);
        alert.setContentText(message);
        alert.show();
    }
}
