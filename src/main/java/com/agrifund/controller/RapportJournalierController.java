package com.agrifund.controller;

import com.agrifund.entities.rapport_journalier;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.agrifund.services.ServiceRapportJournalier;

import java.sql.SQLException;

public class RapportJournalierController {

    @FXML private TableView<rapport_journalier> tableRapport;
    @FXML private TableColumn<rapport_journalier, String> colDate;
    @FXML private TableColumn<rapport_journalier, String> colType;
    @FXML private TableColumn<rapport_journalier, Double> colMoy;
    @FXML private TableColumn<rapport_journalier, Double> colMin;
    @FXML private TableColumn<rapport_journalier, Double> colMax;
    @FXML private TableColumn<rapport_journalier, Integer> colCapteur;

    private final ServiceRapportJournalier service = new ServiceRapportJournalier();

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDateRapport().toString()
                )
        );
        colType.setCellValueFactory(new PropertyValueFactory<>("typeMesure"));
        colMoy.setCellValueFactory(new PropertyValueFactory<>("moyenne"));
        colMin.setCellValueFactory(new PropertyValueFactory<>("min"));
        colMax.setCellValueFactory(new PropertyValueFactory<>("max"));
        colCapteur.setCellValueFactory(new PropertyValueFactory<>("idCapteur"));

        chargerRapports();
    }

    private void chargerRapports() {
        try {
            tableRapport.setItems(
                    FXCollections.observableArrayList(service.afficherTous())
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
