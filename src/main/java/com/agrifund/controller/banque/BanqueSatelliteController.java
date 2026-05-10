package com.agrifund.controller.banque;

import com.agrifund.components.GoogleMapView;
import com.agrifund.Main;
import com.agrifund.services.BanqueService;
import com.agrifund.services.NasaEarthDataService;
import com.agrifund.services.PdfExportService;
import com.agrifund.util.SessionManager;
import com.agrifund.entities.AnalyseRisqueAgricole;
import com.agrifund.entities.Banque;
import com.agrifund.entities.DonneesSatellite;
import com.agrifund.entities.Utilisateur;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class BanqueSatelliteController {

    // Map
    @FXML private StackPane mapContainer;
    @FXML private Label selectedLocationLabel;
    @FXML private Label coordinatesLabel;
    @FXML private TextField searchField;

    // UN SEUL DatePicker
    @FXML private DatePicker analysisDatePicker;

    // Boutons
    @FXML private Button analyzeButton;
    @FXML private Button exportButton;
    @FXML private ProgressIndicator loadingIndicator;

    // Résultats
    @FXML private VBox resultsContainer;
    @FXML private StackPane chartContainer;
    @FXML private StackPane scoreContainer;
    @FXML private Label scoreNiveauLabel;

    // Valeurs
    @FXML private Label tempValue;
    @FXML private Label precipValue;
    @FXML private Label humidValue;
    @FXML private Label ndviValue;
    @FXML private Label droughtValue;

    // Analyse
    @FXML private Label factorsLabel;
    @FXML private Label recoLabel;

    // Historique
    @FXML private VBox historyContainer;

    // Composants
    private GoogleMapView mapView;
    private NasaEarthDataService nasaService;
    private BanqueService banqueService;
    private PdfExportService pdfService;

    // Données
    private Utilisateur currentUser;
    private Banque currentBanque;

    private double selectedLat = 36.8065;
    private double selectedLon = 10.1815;
    private String selectedRegion = "Tunis, Tunisie";

    private DonneesSatellite lastData;
    private AnalyseRisqueAgricole lastAnalyse;

    @FXML
    public void initialize() {
        System.out.println("🚀 Initialisation...");

        try {
            nasaService = new NasaEarthDataService();
            banqueService = new BanqueService();
            pdfService = new PdfExportService();

            currentUser = SessionManager.getInstance().getUtilisateurConnecte();
            if (currentUser != null) {
                currentBanque = banqueService.rechercherParUtilisateurId(currentUser.getId());
            }

            setupMap();
            setupDatePicker();
            loadHistory();

            if (exportButton != null) exportButton.setDisable(true);
            if (resultsContainer != null) resultsContainer.setVisible(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupMap() {
        System.out.println("🗺️ Création de la carte...");
        mapView = new GoogleMapView();

        // Debug: vérifier que le conteneur a une taille
        System.out.println("📐 mapContainer size: " + mapContainer.getWidth() + "x" + mapContainer.getHeight());

        mapView.setOnRegionSelected((name, coords) -> {
            System.out.println("✅ Région sélectionnée: " + name);
            selectedRegion = name;
            selectedLat = coords[0];
            selectedLon = coords[1];
            updateLocationDisplay();
        });

        mapContainer.getChildren().clear();
        mapContainer.getChildren().add(mapView);

        // Forcer une taille minimale
        if (mapContainer.getWidth() == 0) {
            mapContainer.setMinSize(500, 400);
        }
    }

    /**
     * Configuration simple du DatePicker - SANS LIMITE
     */
    private void setupDatePicker() {
        // Date par défaut : aujourd'hui
        analysisDatePicker.setValue(LocalDate.now());

        // PAS DE RESTRICTION - l'utilisateur peut choisir n'importe quelle date
    }

    private void updateLocationDisplay() {
        if (selectedLocationLabel != null) {
            selectedLocationLabel.setText("📍 " + selectedRegion);
        }
        if (coordinatesLabel != null) {
            coordinatesLabel.setText(String.format(Locale.US, "Lat: %.6f | Lon: %.6f", selectedLat, selectedLon));
        }
    }

    @FXML
    public void searchAddress() {
        if (searchField != null && !searchField.getText().trim().isEmpty()) {
            mapView.searchLocation(searchField.getText().trim());
        }
    }

    @FXML
    public void analyzeRegion() {
        // Récupérer la date sélectionnée
        LocalDate selectedDate = analysisDatePicker.getValue();

        if (selectedDate == null) {
            selectedDate = LocalDate.now();
            analysisDatePicker.setValue(selectedDate);
        }

        System.out.println("═══════════════════════════════════════════");
        System.out.println("🔍 ANALYSE");
        System.out.println("📍 " + selectedRegion + " (" + selectedLat + ", " + selectedLon + ")");
        System.out.println("📅 Date: " + selectedDate);
        System.out.println("═══════════════════════════════════════════");

        startAnalysis(selectedDate);
    }

    private void startAnalysis(LocalDate date) {
        analyzeButton.setDisable(true);
        loadingIndicator.setVisible(true);

        Task<DonneesSatellite> task = new Task<>() {
            @Override
            protected DonneesSatellite call() throws Exception {
                // Utiliser la date sélectionnée - 30 jours avant jusqu'à la date
                LocalDate endDate = date;
                LocalDate startDate = date.minusDays(30);

                return nasaService.fetchNasaData(selectedLat, selectedLon, startDate, endDate, selectedRegion);
            }
        };

        task.setOnSucceeded(e -> {
            lastData = task.getValue();
            Platform.runLater(() -> {
                try {
                    nasaService.sauvegarder(lastData);

                    if (currentBanque != null) {
                        lastAnalyse = nasaService.creerAnalyse(currentBanque.getBanqueId(), lastData);
                    }

                    displayResults();
                    loadHistory();

                    if (exportButton != null) exportButton.setDisable(false);

                } catch (SQLException ex) {
                    showAlert("Erreur: " + ex.getMessage(), Alert.AlertType.ERROR);
                } finally {
                    analyzeButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                    resultsContainer.setVisible(true);
                }
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                analyzeButton.setDisable(false);
                loadingIndicator.setVisible(false);
                showAlert("Erreur: " + task.getException().getMessage(), Alert.AlertType.ERROR);
            });
        });

        new Thread(task).start();
    }

    private void displayResults() {
        displayRiskScore();
        displayChart();
        displayStats();
        displayAnalysis();
    }

    private void displayRiskScore() {
        int score = lastData.getScoreRisque();
        String niveau = lastAnalyse != null ? lastAnalyse.getNiveauRisque() : lastData.getRisqueAgricole();

        scoreContainer.getChildren().clear();

        Circle bg = new Circle(60);
        bg.setFill(Color.web("#e9ecef"));

        Arc arc = new Arc(0, 0, 55, 55, 90, -3.6 * score);
        arc.setType(ArcType.OPEN);
        arc.setFill(null);
        arc.setStrokeWidth(10);
        arc.setStroke(getScoreColor(score));

        VBox text = new VBox(-5);
        text.setAlignment(Pos.CENTER);

        Label num = new Label(String.valueOf(score));
        num.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + getScoreColorHex(score) + ";");

        Label unit = new Label("/100");
        unit.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        text.getChildren().addAll(num, unit);
        scoreContainer.getChildren().addAll(bg, arc, text);

        if (scoreNiveauLabel != null) {
            scoreNiveauLabel.setText(formatNiveau(niveau));
            scoreNiveauLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + getScoreColorHex(score) + ";");
        }
    }

    private void displayChart() {
        chartContainer.getChildren().clear();

        PieChart chart = new PieChart();
        chart.setLegendVisible(true);
        chart.setPrefSize(300, 250);

        double temp = lastData.getTemperatureMoyenne() != null ? Math.abs(lastData.getTemperatureMoyenne()) : 0;
        double precip = lastData.getPrecipitation() != null ? lastData.getPrecipitation() : 0;
        double humid = lastData.getHumidite() != null ? lastData.getHumidite() : 0;
        double drought = lastData.getIndiceSecheresse() != null ? lastData.getIndiceSecheresse() : 0;

        chart.getData().addAll(
                new PieChart.Data("Temp " + String.format("%.1f°C", temp), temp),
                new PieChart.Data("Précip " + String.format("%.1fmm", precip), precip),
                new PieChart.Data("Humid " + String.format("%.1f%%", humid), humid),
                new PieChart.Data("Séch " + String.format("%.0f", drought), drought)
        );

        chartContainer.getChildren().add(chart);
    }

    private void displayStats() {
        if (tempValue != null)
            tempValue.setText(lastData.getTemperatureMoyenne() != null ? String.format("%.1f°C", lastData.getTemperatureMoyenne()) : "N/A");
        if (precipValue != null)
            precipValue.setText(lastData.getPrecipitation() != null ? String.format("%.1f mm", lastData.getPrecipitation()) : "N/A");
        if (humidValue != null)
            humidValue.setText(lastData.getHumidite() != null ? String.format("%.1f%%", lastData.getHumidite()) : "N/A");
        if (ndviValue != null)
            ndviValue.setText(lastData.getNdvi() != null ? String.format("%.3f", lastData.getNdvi()) : "N/A");
        if (droughtValue != null)
            droughtValue.setText(lastData.getIndiceSecheresse() != null ? String.format("%.0f/100", lastData.getIndiceSecheresse()) : "N/A");
    }

    private void displayAnalysis() {
        if (lastAnalyse == null) return;

        if (factorsLabel != null)
            factorsLabel.setText(lastAnalyse.getFacteursRisque() != null ? lastAnalyse.getFacteursRisque() : "Aucun");
        if (recoLabel != null)
            recoLabel.setText(lastAnalyse.getRecommandations() != null ? lastAnalyse.getRecommandations() : "Aucune");
    }

    private void loadHistory() {
        if (historyContainer == null || currentBanque == null) return;

        historyContainer.getChildren().clear();

        try {
            List<AnalyseRisqueAgricole> analyses = nasaService.getHistoriqueAnalyses(currentBanque.getBanqueId());

            if (analyses.isEmpty()) {
                historyContainer.getChildren().add(new Label("Aucune analyse"));
                return;
            }

            for (AnalyseRisqueAgricole a : analyses) {
                historyContainer.getChildren().add(createHistoryRow(a));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createHistoryRow(AnalyseRisqueAgricole a) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 6; -fx-cursor: hand;");

        Label icon = new Label(getRiskIcon(a.getNiveauRisque()));

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label region = new Label(a.getRegion() != null ? a.getRegion() : "Position");
        region.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        region.setMaxWidth(180);
        region.setWrapText(true);

        Label date = new Label(a.getDateAnalyse() != null ?
                a.getDateAnalyse().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
        date.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 10px;");

        info.getChildren().addAll(region, date);

        Label score = new Label(a.getScoreRisque() + "/100");
        score.setStyle("-fx-font-weight: bold; -fx-text-fill: " + getScoreColorHex(a.getScoreRisque()) + ";");

        row.setOnMouseClicked(e -> showDetail(a));
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 6; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 6; -fx-cursor: hand;"));

        row.getChildren().addAll(icon, info, score);
        return row;
    }

    private void showDetail(AnalyseRisqueAgricole a) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails");
        alert.setHeaderText("📊 " + (a.getRegion() != null ? a.getRegion() : "Position"));

        String content = "📅 " + (a.getDateAnalyse() != null ?
                a.getDateAnalyse().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A") + "\n\n" +
                "📈 Score: " + a.getScoreRisque() + "/100\n" +
                "⚠️ Niveau: " + formatNiveau(a.getNiveauRisque()) + "\n\n" +
                "🔍 Facteurs:\n" + (a.getFacteursRisque() != null ? a.getFacteursRisque() : "N/A") + "\n" +
                "💡 Recommandations:\n" + (a.getRecommandations() != null ? a.getRecommandations() : "N/A");

        alert.setContentText(content);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }

    @FXML
    public void exportReport() {
        if (lastData == null || lastAnalyse == null) {
            showAlert("Effectuez d'abord une analyse", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le PDF");
        fc.setInitialDirectory(new File(System.getProperty("user.home") + "/Downloads"));
        fc.setInitialFileName("rapport_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        File file = fc.showSaveDialog(analyzeButton.getScene().getWindow());

        if (file != null) {
            loadingIndicator.setVisible(true);

            Task<File> task = new Task<>() {
                @Override
                protected File call() throws Exception {
                    return pdfService.exportAnalyse(lastData, lastAnalyse, currentBanque, file.getAbsolutePath());
                }
            };

            task.setOnSucceeded(e -> {
                loadingIndicator.setVisible(false);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("✅ PDF généré");
                alert.setContentText("Ouvrir le fichier ?");

                ButtonType open = new ButtonType("Ouvrir");
                ButtonType close = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(open, close);

                alert.showAndWait().ifPresent(r -> {
                    if (r == open) {
                        try { java.awt.Desktop.getDesktop().open(file); } catch (Exception ex) {}
                    }
                });
            });

            task.setOnFailed(e -> {
                loadingIndicator.setVisible(false);
                showAlert("Erreur: " + task.getException().getMessage(), Alert.AlertType.ERROR);
            });

            new Thread(task).start();
        }
    }

    // Utilitaires
    private Color getScoreColor(int s) {
        if (s >= 75) return Color.web("#dc3545");
        if (s >= 50) return Color.web("#fd7e14");
        if (s >= 25) return Color.web("#ffc107");
        return Color.web("#28a745");
    }

    private String getScoreColorHex(int s) {
        if (s >= 75) return "#dc3545";
        if (s >= 50) return "#fd7e14";
        if (s >= 25) return "#ffc107";
        return "#28a745";
    }

    private String getRiskIcon(String n) {
        if (n == null) return "⚪";
        return switch (n.toLowerCase()) {
            case "critique" -> "🔴";
            case "eleve" -> "🟠";
            case "moyen" -> "🟡";
            case "faible" -> "🟢";
            default -> "⚪";
        };
    }

    private String formatNiveau(String n) {
        if (n == null) return "INCONNU";
        return switch (n.toLowerCase()) {
            case "critique" -> "🔴 CRITIQUE";
            case "eleve" -> "🟠 ÉLEVÉ";
            case "moyen" -> "🟡 MOYEN";
            case "faible" -> "🟢 FAIBLE";
            default -> n.toUpperCase();
        };
    }

    @FXML
    public void goToDashboard() {
        Main.navigateTo("/com/agrifund/fxml/banque/banque-dashboard.fxml");
    }

    @FXML
    public void refreshHistory() {
        loadHistory();
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
