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
import java.time.LocalTime;

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

    private void alimenterGraphique(ObservableList<releve_terrain> list) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Mesures IoT");

        for (releve_terrain r : list) {
            series.getData().add(
                    new XYChart.Data<>(
                            LocalTime.now().toString(),
                            r.getValeurMesuree()
                    )
            );
        }

        chart.getData().clear();
        chart.getData().add(series);
    }
}
