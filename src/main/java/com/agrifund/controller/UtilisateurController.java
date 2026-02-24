package com.agrifund.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class UtilisateurController {

    @FXML private ListView<String> lstProjects;
    @FXML private ListView<String> lstOffers;
    @FXML private Label lblNoProjects;
    @FXML private Label lblNoOffers;

    @FXML
    public void initialize() {
        loadProjects();
        loadOffers();
    }

    private void loadProjects() {
        lstProjects.getItems().addAll(
            "Projet 1: Expansion Agricole 2026",
            "Projet 2: Achat Equipement",
            "Projet 3: Installation Irrigation"
        );
        lblNoProjects.setVisible(lstProjects.getItems().isEmpty());
    }

    private void loadOffers() {
        lstOffers.getItems().addAll(
            "Offre Speciale: Credit Equipement (5.5% / 7 ans)",
            "Offre Pro: Pret Court Terme (6.2% / 3 ans)"
        );
        lblNoOffers.setVisible(lstOffers.getItems().isEmpty());
    }
}
