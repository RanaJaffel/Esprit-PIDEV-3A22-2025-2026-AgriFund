package controlers;

import entities.releve_terrain;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ServiceReleveTerrain;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReleveController {

    @FXML private TableView<releve_terrain> tableReleve;
    @FXML private TableColumn<releve_terrain, String> colType;
    @FXML private TableColumn<releve_terrain, Double> colValeur;
    @FXML private TableColumn<releve_terrain, String> colUnite;
    @FXML private TableColumn<releve_terrain, Integer> colCapteur;

    @FXML private LineChart<String, Number> chart;

    private final ServiceReleveTerrain service = new ServiceReleveTerrain();

    @FXML
    public void initialize() {

        colType.setCellValueFactory(new PropertyValueFactory<>("typeMesure"));
        colValeur.setCellValueFactory(new PropertyValueFactory<>("valeurMesuree"));
        colUnite.setCellValueFactory(new PropertyValueFactory<>("unite"));
        colCapteur.setCellValueFactory(new PropertyValueFactory<>("idCapteur"));

        chargerReleves();
        gererSelection();
    }

    /* ============================
       CHARGEMENT
       ============================ */
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

    /* ============================
       GRAPHIQUE TEMPOREL
       ============================ */
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

    /* ============================
       FILTRER PAR CAPTEUR
       ============================ */
    private void gererSelection() {
        tableReleve.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        afficherParCapteur(newVal.getIdCapteur());
                    }
                }
        );
    }

    private void afficherParCapteur(int idCapteur) {
        try {
            ObservableList<releve_terrain> list =
                    FXCollections.observableArrayList(
                            service.getRelevesByCapteur(idCapteur)
                    );

            tableReleve.setItems(list);
            alimenterGraphique(list);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
