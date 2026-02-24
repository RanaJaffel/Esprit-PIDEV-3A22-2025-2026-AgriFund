package com.agrifund.controller;

import com.agrifund.entities.releve_terrain;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import com.agrifund.services.ApiMeteoService;
import com.agrifund.services.ServiceReleveTerrain;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReleveController {

    @FXML private TableView<releve_terrain> tableReleve;
    @FXML private TableColumn<releve_terrain, String> colType;
    @FXML private TableColumn<releve_terrain, Double> colValeur;
    @FXML private TableColumn<releve_terrain, String> colUnite;
    @FXML private TableColumn<releve_terrain, Integer> colCapteur;
    @FXML
    private TableColumn<releve_terrain, String> colEtat;

    @FXML private LineChart<String, Number> chart;

    @FXML private TextField txtType;
    @FXML private TextField txtValeur;
    @FXML private TextField txtUnite;
    @FXML private TextField txtCapteur;


    private final ServiceReleveTerrain service = new ServiceReleveTerrain();
    @FXML
    private Label lblMeteo;

    private ApiMeteoService apiMeteoService = new ApiMeteoService();


    @FXML
    public void initialize() {

        colType.setCellValueFactory(new PropertyValueFactory<>("typeMesure"));
        colValeur.setCellValueFactory(new PropertyValueFactory<>("valeurMesuree"));
        colUnite.setCellValueFactory(new PropertyValueFactory<>("unite"));
        colCapteur.setCellValueFactory(new PropertyValueFactory<>("idCapteur"));

        // ===== MÉTIER AVANCÉ : Détection anomalie =====
        colEtat.setCellValueFactory(cellData -> {

            releve_terrain r = cellData.getValue();

            try {
                boolean anomalie = service.detecterAnomalieStatistique(
                        r.getIdCapteur(),
                        r.getValeurMesuree()
                );

                return new javafx.beans.property.SimpleStringProperty(
                        anomalie ? "ANOMALIE" : "NORMAL"
                );

            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("?");
            }
        });

        // ===== STYLE VISUEL =====
        colEtat.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String etat, boolean empty) {
                super.updateItem(etat, empty);

                if (empty || etat == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(etat);

                    if (etat.equals("ANOMALIE")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        chargerReleves();
        gererSelection();

        double temp = apiMeteoService.getTemperature("Tunis");

        if (temp != -999) {
            lblMeteo.setText("Température météo (Tunis) : " + temp + " °C");
        } else {
            lblMeteo.setText("Erreur récupération météo");
        }
    }



    private void chargerReleves() {
        try {
            ObservableList<releve_terrain> list =
                    FXCollections.observableArrayList(service.afficher());

            tableReleve.setItems(list);
            alimenterGraphique(list);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void alimenterGraphique(List<releve_terrain> list) {

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Mesures IoT");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        for (releve_terrain r : list) {
            if (r.getDateHeure() != null) {
                series.getData().add(
                        new XYChart.Data<>(
                                r.getDateHeure().format(formatter),
                                r.getValeurMesuree()
                        )
                );
            }
        }

        chart.getData().clear();
        chart.getData().add(series);
    }


    private void gererSelection() {
        tableReleve.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, selected) -> {
                    if (selected != null) {
                        txtType.setText(selected.getTypeMesure());
                        txtValeur.setText(String.valueOf(selected.getValeurMesuree()));
                        txtUnite.setText(selected.getUnite());
                        txtCapteur.setText(String.valueOf(selected.getIdCapteur()));
                    }
                }
        );
    }


    @FXML
    private void ajouterReleve() {

        // Réinitialiser le style des champs
        resetStyle(txtType, txtValeur, txtUnite, txtCapteur);

        // Vérification des champs obligatoires
        if (txtType.getText().trim().isEmpty()
                || txtValeur.getText().trim().isEmpty()
                || txtUnite.getText().trim().isEmpty()
                || txtCapteur.getText().trim().isEmpty()) {

            marquerErreur(txtType, txtValeur, txtUnite, txtCapteur);
            afficherErreur("Formulaire invalide", "Tous les champs sont obligatoires");
            return;
        }

        try {
            double valeur = Double.parseDouble(txtValeur.getText());
            int idCapteur = Integer.parseInt(txtCapteur.getText());

            // Création de l'objet relevé
            releve_terrain r = new releve_terrain(
                    txtType.getText().trim(),
                    valeur,
                    txtUnite.getText().trim(),
                    idCapteur
            );

            // ===== Sauvegarde en base =====
            service.ajouter(r);

            // ===== Appel API météo =====
            // On réutilise le champ "apiMeteoService" déclaré en haut du contrôleur
            double tempMeteo = apiMeteoService.getTemperature("Tunis");

            if (tempMeteo != -999) {
                double ecart = Math.abs(valeur - tempMeteo);

                if (ecart > 10) {  // seuil métier
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Alerte Anomalie");
                    alert.setHeaderText("Écart important détecté !");
                    alert.setContentText(
                            "Température capteur : " + valeur + " °C\n" +
                                    "Température météo : " + tempMeteo + " °C\n" +
                                    "Écart : " + ecart + " °C"
                    );
                    alert.show();
                }
            }

            // Recharger l'affichage + vider les champs
            chargerReleves();
            viderChamps();

        } catch (NumberFormatException e) {
            marquerErreur(txtValeur, txtCapteur);
            afficherErreur("Erreur de saisie",
                    "Valeur et ID Capteur doivent être numériques");

        } catch (Exception e) {
            afficherErreur("Erreur", e.getMessage());
        }
    }


    /* ===== UTILS ===== */
    private void marquerErreur(TextField... fields) {
        for (TextField f : fields) {
            f.setStyle("-fx-border-color:red;");
        }
    }

    private void resetStyle(TextField... fields) {
        for (TextField f : fields) {
            f.setStyle(null);
        }
    }

    private void afficherErreur(String titre, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(titre);
        alert.setContentText(msg);
        alert.show();
    }


    @FXML
    private void modifierReleve() {
        releve_terrain selected = tableReleve.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                selected.setTypeMesure(txtType.getText());
                selected.setValeurMesuree(Double.parseDouble(txtValeur.getText()));
                selected.setUnite(txtUnite.getText());
                selected.setIdCapteur(Integer.parseInt(txtCapteur.getText()));

                service.modifier(selected);
                chargerReleves();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void supprimerReleve() {
        releve_terrain selected = tableReleve.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                service.supprimer(selected.getIdReleve());
                chargerReleves();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void viderGraphique() {
        try {
            service.genererRapportJournalier();
            service.supprimerTousLesRelevesDuJour();
            chart.getData().clear();
            tableReleve.getItems().clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void viderChamps() {
        txtType.clear();
        txtValeur.clear();
        txtUnite.clear();
        txtCapteur.clear();
    }
    @FXML
    private void ouvrirRapports() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/agrifund/fxml/rapport.fxml")
            );

            Scene scene = new Scene(loader.load(), 900, 600);
            Stage stage = new Stage();
            stage.setTitle("Rapports journaliers");
            stage.setScene(scene);
            stage.show();

            System.out.println("✅ Fenêtre Rapport ouverte");

        } catch (Exception e) {
            System.err.println("❌ IMPOSSIBLE DE CHARGER rapport.fxml");
            e.printStackTrace();
        }
    }


}
