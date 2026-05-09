package com.agrifund.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;

public class MainControllerRana {

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        openCapteurs();
    }

    @FXML
    private void openCapteurs() {
        loadPage("/capteur.fxml");
    }

    @FXML
    private void openDashboard() {
        loadPage("/dashboard.fxml");
    }

    @FXML
    private void openMeteo() {
        loadPage("/meteo.fxml");
    }

    private void loadPage(String fxml) {
        try {
            Pane page = FXMLLoader.load(getClass().getResource(fxml));
            contentPane.getChildren().setAll(page);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}