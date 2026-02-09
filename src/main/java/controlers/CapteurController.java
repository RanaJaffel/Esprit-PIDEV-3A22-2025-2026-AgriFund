package controlers;

import entities.capteur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ServiceCapteur;

import java.sql.SQLException;

public class CapteurController {

    @FXML private TableView<capteur> tableCapteur;
    @FXML private TableColumn<capteur, Integer> colId;
    @FXML private TableColumn<capteur, String> colType;
    @FXML private TableColumn<capteur, String> colLocalisation;
    @FXML private TableColumn<capteur, String> colStatut;

    private final ServiceCapteur service = new ServiceCapteur();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idCapteur"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeCapteur"));
        colLocalisation.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        chargerCapteurs();
        styliserStatut();
    }

    private void chargerCapteurs() {
        try {
            ObservableList<capteur> list =
                    FXCollections.observableArrayList(service.afficher());
            tableCapteur.setItems(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Badges de statut (ACTIF / INACTIF)
    private void styliserStatut() {
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(statut);
                    badge.getStyleClass().add("badge");
                    if (statut.equalsIgnoreCase("ACTIF")) {
                        badge.getStyleClass().add("badge-actif");
                    } else {
                        badge.getStyleClass().add("badge-inactif");
                    }
                    setGraphic(badge);
                }
            }
        });
    }
}
