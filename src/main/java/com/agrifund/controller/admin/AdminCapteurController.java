package com.agrifund.controller.admin;

import com.agrifund.entities.capteur;
import com.agrifund.services.ServiceCapteur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AdminCapteurController {

    @FXML private TableView<Map<String, Object>> tableCapteur;
    @FXML private TableColumn<Map<String, Object>, String> colId;
    @FXML private TableColumn<Map<String, Object>, String> colType;
    @FXML private TableColumn<Map<String, Object>, String> colLocalisation;
    @FXML private TableColumn<Map<String, Object>, String> colStatut;
    @FXML private TableColumn<Map<String, Object>, String> colProjet;
    @FXML private TableColumn<Map<String, Object>, String> colAgriculteur;

    @FXML private TextField tfRecherche;
    @FXML private Label lblTotal;
    @FXML private Label lblActifs;
    @FXML private Label lblInactifs;

    private final ServiceCapteur service = new ServiceCapteur();
    private ObservableList<Map<String, Object>> allCapteurs;

    @FXML
    public void initialize() {
        // Configuration des colonnes
        colId.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().get("idCapteur"))));
        colType.setCellValueFactory(data ->
                new SimpleStringProperty((String) data.getValue().get("typeCapteur")));
        colLocalisation.setCellValueFactory(data ->
                new SimpleStringProperty((String) data.getValue().get("localisation")));
        colStatut.setCellValueFactory(data ->
                new SimpleStringProperty((String) data.getValue().get("statut")));
        colProjet.setCellValueFactory(data ->
                new SimpleStringProperty((String) data.getValue().get("nomProjet")));
        colAgriculteur.setCellValueFactory(data ->
                new SimpleStringProperty((String) data.getValue().get("agriculteur")));

        // Styliser la colonne statut
        styliserStatut();

        // Charger les données
        chargerCapteurs();

        // Recherche en temps réel
        tfRecherche.textProperty().addListener((obs, oldVal, newVal) -> filtrer(newVal));
    }

    private void chargerCapteurs() {
        try {
            List<Map<String, Object>> list = service.afficherTousAvecAgriculteur();
            allCapteurs = FXCollections.observableArrayList(list);
            tableCapteur.setItems(allCapteurs);

            // Stats
            long total = list.size();
            long actifs = list.stream().filter(m -> "ACTIF".equals(m.get("statut"))).count();
            long inactifs = total - actifs;

            lblTotal.setText(String.valueOf(total));
            lblActifs.setText(String.valueOf(actifs));
            lblInactifs.setText(String.valueOf(inactifs));

        } catch (SQLException e) {
            afficherErreur("Erreur de chargement", e.getMessage());
        }
    }

    private void filtrer(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            tableCapteur.setItems(allCapteurs);
            return;
        }

        String lower = keyword.toLowerCase();
        ObservableList<Map<String, Object>> filtered = allCapteurs.filtered(m ->
                String.valueOf(m.get("typeCapteur")).toLowerCase().contains(lower) ||
                        String.valueOf(m.get("localisation")).toLowerCase().contains(lower) ||
                        String.valueOf(m.get("nomProjet")).toLowerCase().contains(lower) ||
                        String.valueOf(m.get("agriculteur")).toLowerCase().contains(lower)
        );
        tableCapteur.setItems(filtered);
    }

    private void styliserStatut() {
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label(statut);
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add(statut.equals("ACTIF") ? "badge-actif" : "badge-inactif");
                setGraphic(badge);
                setText(null);
            }
        });
    }

    @FXML
    private void rafraichir() {
        tfRecherche.clear();
        chargerCapteurs();
    }

    @FXML
    private void exporterCSV() {
        // TODO: Implémenter l'export CSV si nécessaire
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export");
        alert.setHeaderText("Fonctionnalité à venir");
        alert.setContentText("L'export CSV sera disponible prochainement.");
        alert.show();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(titre);
        alert.setContentText(message);
        alert.show();
    }
}
