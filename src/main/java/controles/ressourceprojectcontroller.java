package controles;

import entities.projectagricole;
import entities.ressourceproject;
import services.projectagricoleCRUD;
import services.ressourceprojectCRUD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ressourceprojectcontroller implements Initializable {

    @FXML private ComboBox<Integer> cbIdProject;
    @FXML private TextField tfNomRessource;
    @FXML private ComboBox<String> cbTypeRessource;
    @FXML private TextField tfQuantite;
    @FXML private TextField tfCout;
    @FXML private TextField tfFournisseur;
    @FXML private ComboBox<String> cbStatutRessource;
    @FXML private DatePicker dpDateAjout;
    @FXML private FlowPane ressourcesContainer;

    // --- NOUVEAUX ELEMENTS POUR RECHERCHE ET FILTRE ---
    @FXML private TextField tfSearchRessource;
    @FXML private ComboBox<String> cbFilterTypeList;
    private List<ressourceproject> allRessources = new ArrayList<>();
    // --------------------------------------------------

    private ressourceproject selectedRessource = null;
    private final ressourceprojectCRUD rService = new ressourceprojectCRUD();
    private final projectagricoleCRUD pService = new projectagricoleCRUD();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbTypeRessource.getItems().addAll("equipement", "materiaux", "service");
        cbStatutRessource.getItems().addAll("prevu", "achete");

        // Initialisation du filtre
        cbFilterTypeList.getItems().addAll("Tous", "equipement", "materiaux", "service");
        cbFilterTypeList.setValue("Tous");

        // Écouteurs pour la recherche dynamique (en temps réel)
        tfSearchRessource.textProperty().addListener((observable, oldValue, newValue) -> updateCardsDisplay());
        cbFilterTypeList.setOnAction(event -> updateCardsDisplay());

        try {
            for (projectagricole p : pService.afficher()) {
                cbIdProject.getItems().add(p.getIdproject());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les ID des projets.");
        }

        refreshDataFromDB();
    }

    /**
     * Charge les données depuis MySQL
     */
    private void refreshDataFromDB() {
        try {
            allRessources = rService.afficher(); // On stocke tout localement
            updateCardsDisplay(); // On applique les filtres
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Erreur lors du chargement : " + e.getMessage());
        }
    }

    /**
     * Filtre la liste locale et redessine les cartes
     */
    private void updateCardsDisplay() {
        ressourcesContainer.getChildren().clear();

        String searchText = tfSearchRessource.getText().toLowerCase();
        String filterType = cbFilterTypeList.getValue();

        // Utilisation des Streams Java pour filtrer instantanément
        List<ressourceproject> filteredList = allRessources.stream()
                .filter(r -> r.getNomressource().toLowerCase().contains(searchText)) // Filtre par nom
                .filter(r -> filterType.equals("Tous") || r.getTyperessource().equals(filterType)) // Filtre par type
                .collect(Collectors.toList());

        for (ressourceproject r : filteredList) {
            ressourcesContainer.getChildren().add(createCard(r));
        }
    }

    private VBox createCard(ressourceproject r) {
        VBox card = new VBox(8);
        card.getStyleClass().add("project-card");
        card.setPrefWidth(260);

        Label lblNom = new Label(r.getNomressource() + " (Proj #" + r.getIdproject() + ")");
        lblNom.getStyleClass().add("card-title");

        Label lblStatut = new Label(r.getStatut().toUpperCase());
        if (r.getStatut().equals("achete")) lblStatut.getStyleClass().add("status-badge-accepte");
        else lblStatut.getStyleClass().add("status-badge-encours");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topRow = new HBox(lblNom, spacer, lblStatut);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblType = new Label("📦 Type: " + r.getTyperessource());
        lblType.getStyleClass().add("card-info");

        Label lblQte = new Label("🔢 Quantité: " + r.getQuantite());
        lblQte.getStyleClass().add("card-info");

        Label lblCout = new Label("💲 Coût: " + r.getCout() + " DT");
        lblCout.getStyleClass().add("card-info");

        Label lblFournisseur = new Label("🏢 Fournisseur: " + r.getFournisseur());
        lblFournisseur.getStyleClass().add("card-info");

        // --- NOUVEAU : Création des boutons sur la carte ---
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.getStyleClass().add("btn-secondary");
        btnModifier.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
        btnModifier.setOnAction(e -> {
            selectedRessource = r;
            updateRessource(null); // Déclenche la mise à jour
        });

        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.getStyleClass().add("btn-danger");
        btnSupprimer.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
        btnSupprimer.setOnAction(e -> {
            selectedRessource = r;
            deleteRessource(null); // Déclenche la suppression
        });

        actionBox.getChildren().addAll(btnModifier, btnSupprimer);
        // --------------------------------------------------

        card.getChildren().addAll(topRow, new Separator(), lblType, lblQte, lblCout, lblFournisseur, actionBox);

        card.setOnMouseClicked(event -> populateForm(r));
        return card;
    }

    private void populateForm(ressourceproject r) {
        selectedRessource = r;
        cbIdProject.setValue(r.getIdproject());
        tfNomRessource.setText(r.getNomressource());
        cbTypeRessource.setValue(r.getTyperessource());
        tfQuantite.setText(String.valueOf(r.getQuantite()));
        tfCout.setText(r.getCout().toString());
        tfFournisseur.setText(r.getFournisseur());
        cbStatutRessource.setValue(r.getStatut());
        dpDateAjout.setValue(r.getDateajout().toLocalDate());
    }

    @FXML
    void addRessource(ActionEvent event) {
        if (!validateInputs()) return;
        try {
            ressourceproject r = new ressourceproject(
                    tfNomRessource.getText(), cbTypeRessource.getValue(),
                    Integer.parseInt(tfQuantite.getText()), new BigDecimal(tfCout.getText()),
                    tfFournisseur.getText(), cbStatutRessource.getValue(),
                    Date.valueOf(dpDateAjout.getValue()), cbIdProject.getValue()
            );
            rService.ajouter(r);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource ajoutée avec succès !");
            clearFields(null);
            refreshDataFromDB(); // Recharge et filtre
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void updateRessource(ActionEvent event) {
        if (selectedRessource == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une carte à modifier.");
            return;
        }
        if (!validateInputs()) return;

        try {
            selectedRessource.setIdproject(cbIdProject.getValue());
            selectedRessource.setNomressource(tfNomRessource.getText());
            selectedRessource.setTyperessource(cbTypeRessource.getValue());
            selectedRessource.setQuantite(Integer.parseInt(tfQuantite.getText()));
            selectedRessource.setCout(new BigDecimal(tfCout.getText()));
            selectedRessource.setFournisseur(tfFournisseur.getText());
            selectedRessource.setStatut(cbStatutRessource.getValue());
            selectedRessource.setDateajout(Date.valueOf(dpDateAjout.getValue()));

            rService.modifier(selectedRessource);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource modifiée avec succès !");
            clearFields(null);
            refreshDataFromDB(); // Recharge et filtre
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        }
    }

    @FXML
    void deleteRessource(ActionEvent event) {
        if (selectedRessource == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une carte à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cette ressource ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                rService.supprimer(selectedRessource.getIdressource());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Ressource supprimée !");
                clearFields(null);
                refreshDataFromDB(); // Recharge et filtre
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
            }
        }
    }

    @FXML
    void clearFields(ActionEvent event) {
        selectedRessource = null;
        cbIdProject.getSelectionModel().clearSelection();
        tfNomRessource.clear(); cbTypeRessource.getSelectionModel().clearSelection();
        tfQuantite.clear(); tfCout.clear(); tfFournisseur.clear();
        cbStatutRessource.getSelectionModel().clearSelection(); dpDateAjout.setValue(null);
    }

    @FXML
    void goToProjects(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/projectagricole.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        if (tfNomRessource.getText().isEmpty() || tfQuantite.getText().isEmpty() || tfCout.getText().isEmpty() ||
                cbIdProject.getValue() == null || cbTypeRessource.getValue() == null || cbStatutRessource.getValue() == null || dpDateAjout.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir tous les champs !");
            return false;
        }
        try {
            Integer.parseInt(tfQuantite.getText());
            new BigDecimal(tfCout.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "La quantité et le coût doivent être des nombres valides !");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}