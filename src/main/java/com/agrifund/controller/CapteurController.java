package com.agrifund.controller;

import com.agrifund.entities.capteur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import com.agrifund.services.ApiGeoService;
import com.agrifund.services.ServiceCapteur;

import java.awt.*;
import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CapteurController {

    @FXML private TableView<capteur>            tableCapteur;
    @FXML private TableColumn<capteur, Integer> colId;
    @FXML private TableColumn<capteur, String>  colType;
    @FXML private TableColumn<capteur, String>  colLocalisation;
    @FXML private TableColumn<capteur, String>  colStatut;
    @FXML private TableColumn<capteur, Integer> colProjet;

    @FXML private TextField        tfType;
    @FXML private TextField        tfLocalisation;
    @FXML private ComboBox<String> cbStatut;
    @FXML private ComboBox<String> cbProjet;   // ← String (nom) au lieu de Integer
    @FXML private Label            lblGeo;

    private final ServiceCapteur service    = new ServiceCapteur();
    private final ApiGeoService  geoService = new ApiGeoService();


    private final Map<String, Integer> nomToId = new HashMap<>();

    private final Map<Integer, String> idToNom = new HashMap<>();


    @FXML
    public void initialize() {
        try { service.verifierEtatCapteurs(); }
        catch (Exception e) { e.printStackTrace(); }

        colId.setCellValueFactory(new PropertyValueFactory<>("idCapteur"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeCapteur"));
        colLocalisation.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colProjet.setCellValueFactory(new PropertyValueFactory<>("idProjet"));


        colProjet.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer idProjet, boolean empty) {
                super.updateItem(idProjet, empty);
                setText(empty || idProjet == null ? null
                        : idToNom.getOrDefault(idProjet, "Projet #" + idProjet));
            }
        });

        cbStatut.setItems(FXCollections.observableArrayList("ACTIF", "INACTIF"));

        chargerProjets();
        chargerCapteurs();
        styliserStatut();
        gererSelection();
    }


    private void chargerProjets() {
        try {
            Map<Integer, String> map = service.getProjetsMap();
            nomToId.clear();
            idToNom.clear();

            ObservableList<String> noms = FXCollections.observableArrayList();
            for (Map.Entry<Integer, String> entry : map.entrySet()) {
                nomToId.put(entry.getValue(), entry.getKey());
                idToNom.put(entry.getKey(), entry.getValue());
                noms.add(entry.getValue());
            }
            cbProjet.setItems(noms);

        } catch (SQLException e) {
            afficherErreur("Erreur projets",
                    "Impossible de charger les projets : " + e.getMessage());
        }
    }

    private void chargerCapteurs() {
        try {
            tableCapteur.setItems(
                    FXCollections.observableArrayList(service.afficher()));
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement", e.getMessage());
        }
    }


    @FXML
    private void ajouterCapteur() {
        if (!formulaireValide()) return;
        try {
            capteur c = new capteur();
            c.setTypeCapteur(tfType.getText().trim());
            c.setLocalisation(tfLocalisation.getText().trim());
            c.setStatut(cbStatut.getValue());
            c.setIdProjet(getSelectedProjetId());

            ApiGeoService.Coordinates coords = geoService.geocode(c.getLocalisation());
            if (coords == null) {
                afficherErreur("Localisation introuvable",
                        "Impossible de trouver cette localisation.\n" +
                                "Ex: 'Sfax, Tunisie' ou 'Tunis, Tunisia'.");
                return;
            }

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Localisation validée");
            info.setHeaderText("Coordonnées GPS détectées");
            info.setContentText("Latitude  : " + coords.getLat()
                    + "\nLongitude : " + coords.getLon());
            info.showAndWait();

            service.ajouter(c);
            chargerCapteurs();
            clearForm();

        } catch (Exception e) {
            afficherErreur("Ajout impossible", e.getMessage());
        }
    }


    @FXML
    private void modifierCapteur() {
        capteur selected = tableCapteur.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Modification", "Veuillez sélectionner un capteur");
            return;
        }
        if (!formulaireValide()) return;
        try {
            selected.setTypeCapteur(tfType.getText().trim());
            selected.setLocalisation(tfLocalisation.getText().trim());
            selected.setStatut(cbStatut.getValue());
            selected.setIdProjet(getSelectedProjetId());

            service.modifier(selected);
            chargerCapteurs();
            clearForm();
        } catch (Exception e) {
            afficherErreur("Modification impossible", e.getMessage());
        }
    }


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


    private void gererSelection() {
        tableCapteur.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, c) -> {
                    if (c != null) {
                        tfType.setText(c.getTypeCapteur());
                        tfLocalisation.setText(c.getLocalisation());
                        cbStatut.setValue(c.getStatut());
                        // Afficher le NOM, pas l'ID
                        cbProjet.setValue(c.getIdProjet() != null
                                ? idToNom.getOrDefault(c.getIdProjet(),
                                "Projet #" + c.getIdProjet())
                                : null);
                    }
                }
        );
    }


    private Integer getSelectedProjetId() {
        String nom = cbProjet.getValue();
        return (nom == null || nom.isBlank()) ? null : nomToId.get(nom);
    }


    private void styliserStatut() {
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setGraphic(null); return; }
                Label badge = new Label(statut);
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add(
                        statut.equals("ACTIF") ? "badge-actif" : "badge-inactif");
                setGraphic(badge);
                setText(null);
            }
        });
    }


    private boolean formulaireValide() {
        resetStyle(tfType, tfLocalisation);
        if (tfType.getText().trim().isEmpty()
                || tfLocalisation.getText().trim().isEmpty()
                || cbStatut.getValue() == null) {
            marquerErreur(tfType, tfLocalisation);
            afficherErreur("Formulaire invalide", "Tous les champs sont obligatoires");
            return false;
        }
        return true;
    }


    private void clearForm() {
        tfType.clear();
        tfLocalisation.clear();
        cbStatut.setValue(null);
        cbProjet.setValue(null);
        lblGeo.setText("Coordonnées : --");
        tableCapteur.getSelectionModel().clearSelection();
        resetStyle(tfType, tfLocalisation);
    }

    private void marquerErreur(TextField... fields) {
        for (TextField f : fields) f.setStyle("-fx-border-color: red;");
    }

    private void resetStyle(TextField... fields) {
        for (TextField f : fields) f.setStyle(null);
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(titre);
        alert.setContentText(message);
        alert.show();
    }


    @FXML
    private void testerLocalisation() {
        String loc = tfLocalisation.getText().trim();
        if (loc.isEmpty()) {
            afficherErreur("Localisation vide",
                    "Veuillez saisir une localisation avant de tester.");
            return;
        }
        try {
            ApiGeoService.Coordinates coords = geoService.geocode(loc);
            if (coords == null) {
                lblGeo.setText("Coordonnées : introuvables");
                return;
            }
            lblGeo.setText(String.format("Coordonnées : %.5f , %.5f",
                    coords.getLat(), coords.getLon()));

            String url = "https://www.openstreetmap.org/?mlat=" + coords.getLat()
                    + "&mlon=" + coords.getLon()
                    + "#map=15/" + coords.getLat() + "/" + coords.getLon();
            Desktop.getDesktop().browse(new URI(url));

        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur", "Impossible d'ouvrir la carte.");
        }
    }
}