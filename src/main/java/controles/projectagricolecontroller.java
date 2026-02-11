package controles;

import entities.projectagricole;
import services.projectagricoleCRUD;
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

public class projectagricolecontroller implements Initializable {

    @FXML private TextField tfNomProject;
    @FXML private TextField tfSurface;
    @FXML private TextField tfBudget;
    @FXML private ComboBox<String> cbStatut;
    @FXML private DatePicker dpDateSoumission;
    @FXML private FlowPane projectsContainer;

    // --- NOUVEAUX ELEMENTS POUR RECHERCHE ET FILTRE ---
    @FXML private TextField tfSearchProject;
    @FXML private ComboBox<String> cbFilterStatutList;
    private List<projectagricole> allProjects = new ArrayList<>(); // Stockage local pour la recherche
    // --------------------------------------------------

    private projectagricole selectedProject = null;
    private final projectagricoleCRUD service = new projectagricoleCRUD();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbStatut.getItems().addAll("en cours", "accepte", "refuse");

        // Initialisation du filtre
        cbFilterStatutList.getItems().addAll("Tous", "en cours", "accepte", "refuse");
        cbFilterStatutList.setValue("Tous");

        // Écouteurs pour la recherche dynamique (en temps réel)
        tfSearchProject.textProperty().addListener((observable, oldValue, newValue) -> updateCardsDisplay());
        cbFilterStatutList.setOnAction(event -> updateCardsDisplay());

        refreshDataFromDB();
    }

    /**
     * Charge les données depuis MySQL et met à jour l'affichage
     */
    private void refreshDataFromDB() {
        try {
            allProjects = service.afficher(); // On stocke tout localement
            updateCardsDisplay(); // On applique les filtres et on affiche
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Base de données", "Impossible de charger les projets : " + e.getMessage());
        }
    }

    /**
     * Filtre la liste locale et redessine les cartes
     */
    private void updateCardsDisplay() {
        projectsContainer.getChildren().clear();

        String searchText = tfSearchProject.getText().toLowerCase();
        String filterStatut = cbFilterStatutList.getValue();

        // Utilisation des Streams Java pour filtrer instantanément
        List<projectagricole> filteredList = allProjects.stream()
                .filter(p -> p.getNomproject().toLowerCase().contains(searchText)) // Filtre par nom
                .filter(p -> filterStatut.equals("Tous") || p.getStatut().equals(filterStatut)) // Filtre par statut
                .collect(Collectors.toList());

        for (projectagricole p : filteredList) {
            projectsContainer.getChildren().add(createCard(p));
        }
    }

    private VBox createCard(projectagricole p) {
        VBox card = new VBox(10);
        card.getStyleClass().add("project-card");
        card.setPrefWidth(260);

        Label lblNom = new Label(p.getNomproject());
        lblNom.getStyleClass().add("card-title");

        Label lblStatut = new Label(p.getStatut().toUpperCase());
        if (p.getStatut().equals("accepte")) lblStatut.getStyleClass().add("status-badge-accepte");
        else if (p.getStatut().equals("refuse")) lblStatut.getStyleClass().add("status-badge-refuse");
        else lblStatut.getStyleClass().add("status-badge-encours");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topRow = new HBox(lblNom, spacer, lblStatut);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblSurface = new Label("🌱 Surface: " + p.getSurface() + " Ha");
        lblSurface.getStyleClass().add("card-info");
        Label lblBudget = new Label("💰 Budget: " + p.getBudgetdemande() + " DT");
        lblBudget.getStyleClass().add("card-info");
        Label lblDate = new Label("📅 Soumis le: " + p.getDatesoumission());
        lblDate.getStyleClass().add("card-info");

        // --- NOUVEAU : Création des boutons sur la carte ---
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new javafx.geometry.Insets(10, 0, 0, 0)); // Marge en haut

        Button btnModifier = new Button("✏️ Modifier");
        btnModifier.getStyleClass().add("btn-secondary");
        btnModifier.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
        btnModifier.setOnAction(e -> {
            selectedProject = p;
            updateProject(null); // Déclenche la méthode de mise à jour existante
        });

        Button btnSupprimer = new Button("🗑️ Supprimer");
        btnSupprimer.getStyleClass().add("btn-danger");
        btnSupprimer.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
        btnSupprimer.setOnAction(e -> {
            selectedProject = p;
            deleteProject(null); // Déclenche la méthode de suppression existante
        });

        actionBox.getChildren().addAll(btnModifier, btnSupprimer);
        // --------------------------------------------------

        card.getChildren().addAll(topRow, new Separator(), lblSurface, lblBudget, lblDate, actionBox);

        // Au clic sur n'importe quel endroit de la carte, on remplit le formulaire de gauche
        card.setOnMouseClicked(event -> {
            populateForm(p);
            // verifierRentabiliteProjet(p); // (Décommentez si vous avez ajouté la méthode métier)
        });

        return card;
    }

    private void populateForm(projectagricole p) {
        selectedProject = p;
        tfNomProject.setText(p.getNomproject());
        tfSurface.setText(String.valueOf(p.getSurface()));
        tfBudget.setText(p.getBudgetdemande().toString());
        cbStatut.setValue(p.getStatut());
        dpDateSoumission.setValue(p.getDatesoumission().toLocalDate());
    }

    @FXML
    void addProject(ActionEvent event) {
        if (!validateInputs()) return;
        try {
            projectagricole p = new projectagricole(
                    tfNomProject.getText(), Float.parseFloat(tfSurface.getText()),
                    new BigDecimal(tfBudget.getText()), cbStatut.getValue(), Date.valueOf(dpDateSoumission.getValue())
            );
            service.ajouter(p);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Projet ajouté avec succès !");
            clearFields(null);
            refreshDataFromDB();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @FXML
    void updateProject(ActionEvent event) {
        if (selectedProject == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une carte à modifier.");
            return;
        }
        if (!validateInputs()) return;

        try {
            selectedProject.setNomproject(tfNomProject.getText());
            selectedProject.setSurface(Float.parseFloat(tfSurface.getText()));
            selectedProject.setBudgetdemande(new BigDecimal(tfBudget.getText()));
            selectedProject.setStatut(cbStatut.getValue());
            selectedProject.setDatesoumission(Date.valueOf(dpDateSoumission.getValue()));

            service.modifier(selectedProject);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Projet modifié avec succès !");
            clearFields(null);
            refreshDataFromDB();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de la modification : " + e.getMessage());
        }
    }

    @FXML
    void deleteProject(ActionEvent event) {
        if (selectedProject == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une carte à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer ce projet ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                service.supprimer(selectedProject.getIdproject());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Projet supprimé !");
                clearFields(null);
                refreshDataFromDB();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Impossible de supprimer : " + e.getMessage());
            }
        }
    }

    @FXML
    void clearFields(ActionEvent event) {
        selectedProject = null;
        tfNomProject.clear(); tfSurface.clear(); tfBudget.clear();
        cbStatut.getSelectionModel().clearSelection(); dpDateSoumission.setValue(null);
    }

    @FXML
    void goToRessources(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/ressourceproject.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        if (tfNomProject.getText().isEmpty() || tfSurface.getText().isEmpty() || tfBudget.getText().isEmpty() || cbStatut.getValue() == null || dpDateSoumission.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir tous les champs !");
            return false;
        }
        try {
            Float.parseFloat(tfSurface.getText());
            new BigDecimal(tfBudget.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Surface et Budget doivent être des nombres !");
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