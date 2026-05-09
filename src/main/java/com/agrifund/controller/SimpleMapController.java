package com.agrifund.controller;

import com.agrifund.view.GoogleMapView;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

/**
 * Controller for the simple view-only map — just a full-screen Google Map, like
 * opening Google Maps directly. No overlays, no controls.
 */
public class SimpleMapController {

    @FXML
    private StackPane mapContainer;

    private GoogleMapView googleMapView;

    @FXML
    public void initialize() {
        googleMapView = new GoogleMapView();
        googleMapView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        mapContainer.getChildren().add(googleMapView);

        googleMapView.prefWidthProperty().bind(mapContainer.widthProperty());
        googleMapView.prefHeightProperty().bind(mapContainer.heightProperty());
    }
}
