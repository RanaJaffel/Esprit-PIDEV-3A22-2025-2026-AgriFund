package com.agrifund.controller;

import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Document;
import com.agrifund.entities.releve_terrain;
import com.agrifund.services.ApiMeteoService;
import com.agrifund.services.ExportService;
import com.agrifund.services.ServiceReleveTerrain;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReleveController {

    // ═══════════════════════════════════════════════════
    //  FXML Components
    // ═══════════════════════════════════════════════════

    @FXML
    private TableView<releve_terrain> tableReleve;

    @FXML
    private TableColumn<releve_terrain, String> colType;

    @FXML
    private TableColumn<releve_terrain, Double> colValeur;

    @FXML
    private TableColumn<releve_terrain, String> colUnite;

    @FXML
    private TableColumn<releve_terrain, Integer> colCapteur;

    @FXML
    private TableColumn<releve_terrain, String> colEtat;

    @FXML
    private LineChart<String, Number> chart;

    @FXML
    private TextField txtType;

    @FXML
    private TextField txtValeur;

    @FXML
    private TextField txtUnite;

    @FXML
    private TextField txtCapteur;

    @FXML
    private TextField txtSearch;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblAnomalies;

    @FXML
    private Label lblMeteo;

    // ═══════════════════════════════════════════════════
    //  Services
    // ═══════════════════════════════════════════════════

    private final ServiceReleveTerrain service = new ServiceReleveTerrain();
    private final ApiMeteoService apiMeteoService = new ApiMeteoService();
    private final ExportService exportService = new ExportService();

    private ObservableList<releve_terrain> masterData = FXCollections.observableArrayList();

    // ═══════════════════════════════════════════════════
    //  Initialisation
    // ═══════════════════════════════════════════════════

    @FXML
    public void initialize() {

        // Configuration des colonnes
        colType.setCellValueFactory(new PropertyValueFactory<>("typeMesure"));
        colValeur.setCellValueFactory(new PropertyValueFactory<>("valeurMesuree"));
        colUnite.setCellValueFactory(new PropertyValueFactory<>("unite"));
        colCapteur.setCellValueFactory(new PropertyValueFactory<>("idCapteur"));

        // Colonne État (détection anomalie)
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

        // Style de la colonne État
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

        // Météo
        double temp = apiMeteoService.getTemperature("Tunis");
        if (temp != -999) {
            lblMeteo.setText("Température météo (Tunis) : " + temp + " °C");
        } else {
            lblMeteo.setText("Erreur récupération météo");
        }

        chart.setCreateSymbols(false);
    }

    // ═══════════════════════════════════════════════════
    //  Chargement des données
    // ═══════════════════════════════════════════════════

    private void chargerReleves() {
        try {
            masterData = FXCollections.observableArrayList(service.afficher());
            tableReleve.setItems(masterData);
            alimenterGraphique(masterData);
            updateStats(masterData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    // ═══════════════════════════════════════════════════
    //  CRUD Operations
    // ═══════════════════════════════════════════════════

    @FXML
    private void ajouterReleve() {

        resetStyle(txtType, txtValeur, txtUnite, txtCapteur);

        // Validation
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

            releve_terrain r = new releve_terrain(
                    txtType.getText().trim(),
                    valeur,
                    txtUnite.getText().trim(),
                    idCapteur
            );

            service.ajouter(r);

            // Comparaison avec météo
            double tempMeteo = apiMeteoService.getTemperature("Tunis");
            if (tempMeteo != -999) {
                double ecart = Math.abs(valeur - tempMeteo);
                if (ecart > 10) {
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

            chargerReleves();
            viderChamps();

        } catch (NumberFormatException e) {
            marquerErreur(txtValeur, txtCapteur);
            afficherErreur("Erreur de saisie", "Valeur et ID Capteur doivent être numériques");

        } catch (Exception e) {
            afficherErreur("Erreur", e.getMessage());
        }
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

    // ═══════════════════════════════════════════════════
    //  Validation & Erreurs
    // ═══════════════════════════════════════════════════

    private void marquerErreur(TextField... fields) {
        for (TextField f : fields) {
            f.setStyle("-fx-border-color: red;");
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

    // ═══════════════════════════════════════════════════
    //  Filtrage & Recherche
    // ═══════════════════════════════════════════════════

    @FXML
    private void filtrerLive() {

        if (txtSearch == null) return;

        String filter = txtSearch.getText().toLowerCase();

        ObservableList<releve_terrain> filtered =
                masterData.filtered(r ->
                        r.getTypeMesure().toLowerCase().contains(filter) ||
                                String.valueOf(r.getIdCapteur()).contains(filter)
                );

        tableReleve.setItems(filtered);
        alimenterGraphique(filtered);
        updateStats(filtered);
    }

    @FXML
    private void afficherAnomalies() {

        ObservableList<releve_terrain> anomalies =
                masterData.filtered(r -> {
                    try {
                        return service.detecterAnomalieStatistique(
                                r.getIdCapteur(),
                                r.getValeurMesuree()
                        );
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

        tableReleve.setItems(anomalies);
        alimenterGraphique(anomalies);
        updateStats(anomalies);
    }

    // ═══════════════════════════════════════════════════
    //  Statistiques
    // ═══════════════════════════════════════════════════

    private void updateStats(List<releve_terrain> list) {

        if (lblTotal == null || lblAnomalies == null) return;

        long anomalies = list.stream()
                .filter(r -> {
                    try {
                        return service.detecterAnomalieStatistique(
                                r.getIdCapteur(),
                                r.getValeurMesuree()
                        );
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }).count();

        lblTotal.setText("Total: " + list.size());
        lblAnomalies.setText("Anomalies: " + anomalies);
    }

    // ═══════════════════════════════════════════════════
    //  Graphique
    // ═══════════════════════════════════════════════════

    private void alimenterGraphique(List<releve_terrain> list) {

        chart.getData().clear();

        int maxPoints = 500;

        List<releve_terrain> dataToShow;
        if (list.size() > maxPoints) {
            dataToShow = list.subList(list.size() - maxPoints, list.size());
        } else {
            dataToShow = list;
        }

        Map<Integer, List<releve_terrain>> grouped =
                dataToShow.stream()
                        .collect(Collectors.groupingBy(releve_terrain::getIdCapteur));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        for (Integer capteur : grouped.keySet()) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Capteur " + capteur);

            for (releve_terrain r : grouped.get(capteur)) {
                if (r.getDateHeure() != null) {
                    series.getData().add(
                            new XYChart.Data<>(
                                    r.getDateHeure().format(formatter),
                                    r.getValeurMesuree()
                            )
                    );
                }
            }

            chart.getData().add(series);
        }
    }

    // ═══════════════════════════════════════════════════
    //  Navigation
    // ═══════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════
    //  Export PDF (CORRIGÉ)
    // ═══════════════════════════════════════════════════

    @FXML
    private void exportPDF() {

        // Vérifier qu'il y a des données
        if (tableReleve.getItems() == null || tableReleve.getItems().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Aucun relevé à exporter.").show();
            return;
        }

        // Dialogue de sauvegarde
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport PDF");
        fileChooser.setInitialFileName("releves_" + LocalDate.now() + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf")
        );

        // Récupérer la fenêtre parente
        Stage stage = (Stage) tableReleve.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file == null) return;   // L'utilisateur a annulé

        // Générer le PDF via le service
        try {

            List<releve_terrain> data = new ArrayList<>(tableReleve.getItems());
            exportService.exportPDF(data, file);

            // Confirmation + ouverture automatique
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export réussi");
            alert.setHeaderText(null);
            alert.setContentText("PDF exporté avec succès :\n" + file.getAbsolutePath());

            ButtonType ouvrirBtn = new ButtonType("Ouvrir le fichier");
            ButtonType fermerBtn = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(ouvrirBtn, fermerBtn);

            alert.showAndWait().ifPresent(response -> {
                if (response == ouvrirBtn) {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur export PDF");
            alert.setHeaderText("Impossible de générer le PDF");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    // ═══════════════════════════════════════════════════
    //  Export CSV
    // ═══════════════════════════════════════════════════

    @FXML
    private void exportCSV() {

        // Vérifier qu'il y a des données
        if (tableReleve.getItems() == null || tableReleve.getItems().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Aucun relevé à exporter.").show();
            return;
        }

        // Dialogue de sauvegarde
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le fichier CSV");
        fileChooser.setInitialFileName("releves_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers CSV (*.csv)", "*.csv")
        );

        Stage stage = (Stage) tableReleve.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file == null) return;

        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {

            writer.println("=================================================");
            writer.println("                RAPPORT RELEVÉS IoT              ");
            writer.println("=================================================");
            writer.println("Date : " + LocalDate.now());
            writer.println("");

            writer.println("Type;Valeur;Unité;Capteur");

            for (releve_terrain r : tableReleve.getItems()) {
                String type = safe(r.getTypeMesure());
                String valeur = String.valueOf(r.getValeurMesuree());
                String unite = safe(r.getUnite());
                String capteur = String.valueOf(r.getIdCapteur());

                writer.println(type + ";" + valeur + ";" + unite + ";" + capteur);
            }

            writer.println("");
            writer.println("Total relevés : " + tableReleve.getItems().size());

            // Confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export réussi");
            alert.setHeaderText(null);
            alert.setContentText("CSV exporté avec succès :\n" + file.getAbsolutePath());

            ButtonType ouvrirBtn = new ButtonType("Ouvrir le fichier");
            ButtonType fermerBtn = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(ouvrirBtn, fermerBtn);

            alert.showAndWait().ifPresent(response -> {
                if (response == ouvrirBtn) {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'export CSV").show();
        }
    }

    // ═══════════════════════════════════════════════════
    //  Analyse IA
    // ═══════════════════════════════════════════════════

    @FXML
    private void analyseIA() {

        releve_terrain selected = tableReleve.getSelectionModel().getSelectedItem();

        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez un relevé").show();
            return;
        }

        try {

            String analyse = service.analyseIntelligente(
                    selected.getIdCapteur(),
                    selected.getValeurMesuree()
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Analyse intelligente locale");
            alert.setContentText(analyse);
            alert.getDialogPane().setPrefWidth(450);
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void predictionLocale() {

        releve_terrain selected = tableReleve.getSelectionModel().getSelectedItem();

        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez un relevé").show();
            return;
        }

        try {

            double prediction = service.predictionSimple(selected.getIdCapteur());

            new Alert(Alert.AlertType.INFORMATION,
                    "Prochaine valeur estimée : " + prediction).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════
    //  Utilitaires
    // ═══════════════════════════════════════════════════

    private String safe(String value) {
        return value == null ? "" : value.replace(";", ",");
    }
}
