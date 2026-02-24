package controlers;

import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import entities.releve_terrain;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.ApiMeteoService;
import services.ServiceReleveTerrain;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.stream.Collectors;

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
    @FXML private TextField txtSearch;
    @FXML private Label lblTotal;
    @FXML private Label lblAnomalies;

    private ObservableList<releve_terrain> masterData = FXCollections.observableArrayList();


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
        chart.setCreateSymbols(false);
    }



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
                    getClass().getResource("/rapport.fxml")
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
                masterData.filtered(r ->
                        {
                            try {
                                return service.detecterAnomalieStatistique(
                                        r.getIdCapteur(),
                                        r.getValeurMesuree()
                                );
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );

        tableReleve.setItems(anomalies);
        alimenterGraphique(anomalies);
        updateStats(anomalies);
    }
    private void updateStats(List<releve_terrain> list) {

        if (lblTotal == null || lblAnomalies == null) return;

        long anomalies = list.stream()
                .filter(r ->
                        {
                            try {
                                return service.detecterAnomalieStatistique(
                                        r.getIdCapteur(),
                                        r.getValeurMesuree()
                                );
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                ).count();

        lblTotal.setText("Total: " + list.size());
        lblAnomalies.setText("Anomalies: " + anomalies);
    }
    private void alimenterGraphique(List<releve_terrain> list) {

        chart.getData().clear();

        // ✅ Limite à 500 derniers points max
        int maxPoints = 500;

        List<releve_terrain> dataToShow;

        if (list.size() > maxPoints) {
            dataToShow = list.subList(
                    list.size() - maxPoints,
                    list.size()
            );
        } else {
            dataToShow = list;
        }

        Map<Integer, List<releve_terrain>> grouped =
                dataToShow.stream()
                        .collect(Collectors.groupingBy(
                                releve_terrain::getIdCapteur
                        ));

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("HH:mm:ss");

        for (Integer capteur : grouped.keySet()) {

            XYChart.Series<String, Number> series =
                    new XYChart.Series<>();

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
    @FXML
    private void exportPDF() {

        try {

            Document document = new Document();
            PdfWriter.getInstance(document,
                    new FileOutputStream("releves.pdf"));

            document.open();

            // ✅ Titre stylé
            com.itextpdf.text.Font titleFont =
                    new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA,
                            18,
                            com.itextpdf.text.Font.BOLD,
                            new com.itextpdf.text.BaseColor(7, 106, 57) // #076A39
                    );

            Paragraph title =
                    new Paragraph("RAPPORT RELEVÉS IoT", titleFont);

            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("Date : "
                    + java.time.LocalDate.now()));
            document.add(new Paragraph(" "));

            // ✅ Tableau 4 colonnes
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            float[] columnWidths = {3f, 2f, 2f, 2f};
            table.setWidths(columnWidths);

            // ✅ En-têtes
            addHeaderCell(table, "Type");
            addHeaderCell(table, "Valeur");
            addHeaderCell(table, "Unité");
            addHeaderCell(table, "Capteur");

            boolean alternate = false;

            for (releve_terrain r : tableReleve.getItems()) {

                com.itextpdf.text.BaseColor bg =
                        alternate
                                ? new com.itextpdf.text.BaseColor(245, 245, 240) // #F5F5F0
                                : com.itextpdf.text.BaseColor.WHITE;

                addBodyCell(table, r.getTypeMesure(), bg);
                addBodyCell(table,
                        String.valueOf(r.getValeurMesuree()), bg);
                addBodyCell(table, r.getUnite(), bg);
                addBodyCell(table,
                        String.valueOf(r.getIdCapteur()), bg);

                alternate = !alternate;
            }

            document.add(table);
            document.close();

            new Alert(Alert.AlertType.INFORMATION,
                    "PDF généré ✅").show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void exportCSV() {

        try (PrintWriter writer =
                     new PrintWriter("releves.csv", "UTF-8")) {

            writer.println("=================================================");
            writer.println("                RAPPORT RELEVÉS IoT              ");
            writer.println("=================================================");
            writer.println("Date : " + java.time.LocalDate.now());
            writer.println("");

            // ✅ En-tête tableau (Excel friendly)
            writer.println("Type;Valeur;Unité;Capteur");

            for (releve_terrain r : tableReleve.getItems()) {

                String type = safe(r.getTypeMesure());
                String valeur = String.valueOf(r.getValeurMesuree());
                String unite = safe(r.getUnite());
                String capteur = String.valueOf(r.getIdCapteur());

                writer.println(type + ";"
                        + valeur + ";"
                        + unite + ";"
                        + capteur);
            }

            writer.println("");
            writer.println("Total relevés : "
                    + tableReleve.getItems().size());

            new Alert(Alert.AlertType.INFORMATION,
                    "CSV exporté avec succès ✅").show();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Erreur lors de l'export CSV").show();
        }
    }
    @FXML
    private void analyseIA() {

        releve_terrain selected =
                tableReleve.getSelectionModel().getSelectedItem();

        if (selected == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Sélectionnez un relevé").show();
            return;
        }

        try {

            String analyse =
                    service.analyseIntelligente(
                            selected.getIdCapteur(),
                            selected.getValeurMesuree()
                    );

            Alert alert =
                    new Alert(Alert.AlertType.INFORMATION);

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

        releve_terrain selected =
                tableReleve.getSelectionModel().getSelectedItem();

        if (selected == null) return;

        try {

            double prediction =
                    service.predictionSimple(
                            selected.getIdCapteur()
                    );

            new Alert(Alert.AlertType.INFORMATION,
                    "Prochaine valeur estimée : "
                            + prediction).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void addHeaderCell(PdfPTable table, String text) {

        com.itextpdf.text.Font font =
                new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA,
                        12,
                        com.itextpdf.text.Font.BOLD,
                        com.itextpdf.text.BaseColor.WHITE
                );

        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(
                new com.itextpdf.text.BaseColor(8, 150, 71) // #089647
        );
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);

        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table,
                             String text,
                             com.itextpdf.text.BaseColor bg) {

        com.itextpdf.text.Font font =
                new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA,
                        11
                );

        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);

        table.addCell(cell);
    }


    private String safe(String value) {
        return value == null ? "" : value.replace(";", ",");
    }

}