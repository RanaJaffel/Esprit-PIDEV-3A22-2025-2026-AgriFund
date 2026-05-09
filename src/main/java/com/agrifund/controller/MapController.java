package com.agrifund.controller;

import com.agrifund.services.GoogleMapsService;
import com.agrifund.services.GoogleMapsService.PlaceDetails;
import com.agrifund.services.GoogleMapsService.PlaceSuggestion;
import com.agrifund.view.GoogleMapView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapController {

    @FXML
    private StackPane mapContainer;
    @FXML
    private TextField txtSearchAddress;
    @FXML
    private ListView<PlaceSuggestion> listSuggestions;
    @FXML
    private Label lblMapStatus;
    @FXML
    private Label lblCoordinates;

    // Tile layer controls
    @FXML
    private ToggleGroup tileGroup;
    @FXML
    private RadioButton rbOSM;
    @FXML
    private RadioButton rbSatellite;
    @FXML
    private RadioButton rbTerrain;
    @FXML
    private RadioButton rbDark;

    // Marker toggles
    @FXML
    private CheckBox cbShowFarms;
    @FXML
    private CheckBox cbShowOffices;
    @FXML
    private CheckBox cbShowCustom;

    // Custom marker fields
    @FXML
    private TextField txtMarkerTitle;
    @FXML
    private TextField txtMarkerDesc;
    @FXML
    private TextField txtMarkerLat;
    @FXML
    private TextField txtMarkerLng;
    @FXML
    private ComboBox<String> cbMarkerColor;
    @FXML
    private Button btnClickMode;

    // Circle zone
    @FXML
    private Slider sliderRadius;
    @FXML
    private Label lblRadiusValue;
    @FXML
    private ComboBox<String> cbCircleColor;

    // Display controls
    @FXML
    private Slider sliderZoom;
    @FXML
    private Slider sliderOpacity;

    private GoogleMapView googleMapView;
    private boolean clickModeActive = false;
    private GoogleMapsService googleMapsService;
    private Timer autocompleteTimer;

    @FXML
    public void initialize() {
        googleMapsService = new GoogleMapsService();

        // Create the GoogleMapView component and add it to the container
        googleMapView = new GoogleMapView();
        mapContainer.getChildren().add(googleMapView);

        // Bind GoogleMapView to fill the container completely
        googleMapView.prefWidthProperty().bind(mapContainer.widthProperty());
        googleMapView.prefHeightProperty().bind(mapContainer.heightProperty());
        googleMapView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Wire up callbacks from the map component
        googleMapView.setOnCoordinatesUpdate((lat, lng)
                -> Platform.runLater(() -> lblCoordinates.setText("Lat: " + lat + " | Lng: " + lng)));

        googleMapView.setOnLocationSelected((lat, lng)
                -> Platform.runLater(() -> {
                    txtMarkerLat.setText(String.valueOf(lat));
                    txtMarkerLng.setText(String.valueOf(lng));
                    lblMapStatus.setText("📍 Position selectionnee: " + lat + ", " + lng);
                }));

        lblMapStatus.setText("✅ Carte Google Maps chargee");

        // ---- Autocomplete setup ----
        setupAutocomplete();

        // Init color combo boxes
        cbMarkerColor.setItems(FXCollections.observableArrayList(
                "Vert", "Rouge", "Or", "Bleu", "Violet", "Orange"));
        cbMarkerColor.getSelectionModel().selectFirst();

        cbCircleColor.setItems(FXCollections.observableArrayList(
                "Vert", "Rouge", "Bleu", "Orange", "Violet"));
        cbCircleColor.getSelectionModel().selectFirst();

        // Radius slider listener
        sliderRadius.valueProperty().addListener((obs, oldVal, newVal)
                -> lblRadiusValue.setText(String.format("%.0f km", newVal.doubleValue())));

        // Zoom slider listener
        sliderZoom.valueProperty().addListener((obs, oldVal, newVal) -> {
            int zoom = newVal.intValue();
            googleMapView.setZoom(zoom);
        });

        // Opacity slider listener
        sliderOpacity.valueProperty().addListener((obs, oldVal, newVal) -> {
            double opacity = newVal.doubleValue();
            googleMapView.setTileOpacity(opacity);
        });
    }

    // ==================== TILE LAYER HANDLERS ====================
    @FXML
    private void handleTileChange() {
        if (rbOSM.isSelected()) {
            googleMapView.switchTile("roadmap");
            lblMapStatus.setText("Couche: Google Maps - Plan");
        } else if (rbSatellite.isSelected()) {
            googleMapView.switchTile("satellite");
            lblMapStatus.setText("Couche: Google Maps - Satellite");
        } else if (rbTerrain.isSelected()) {
            googleMapView.switchTile("terrain");
            lblMapStatus.setText("Couche: Google Maps - Relief");
        } else if (rbDark.isSelected()) {
            googleMapView.switchTile("hybrid");
            lblMapStatus.setText("Couche: Google Maps - Hybride");
        }
    }

    // ==================== MARKER TOGGLE HANDLERS ====================
    @FXML
    private void handleMarkerToggle() {
        googleMapView.showLayer("farms", cbShowFarms.isSelected());
        googleMapView.showLayer("offices", cbShowOffices.isSelected());
        googleMapView.showLayer("custom", cbShowCustom.isSelected());

        StringBuilder sb = new StringBuilder("Affichage: ");
        if (cbShowFarms.isSelected()) {
            sb.append("Exploitations ");
        }
        if (cbShowOffices.isSelected()) {
            sb.append("Bureaux ");
        }
        if (cbShowCustom.isSelected()) {
            sb.append("Perso ");
        }
        lblMapStatus.setText(sb.toString().trim());
    }

    // ==================== CUSTOM MARKER ====================
    @FXML
    private void handleAddCustomMarker() {
        String title = txtMarkerTitle.getText().trim();
        String desc = txtMarkerDesc.getText().trim();
        String latStr = txtMarkerLat.getText().trim();
        String lngStr = txtMarkerLng.getText().trim();

        if (title.isEmpty() || latStr.isEmpty() || lngStr.isEmpty()) {
            lblMapStatus.setText("⚠️ Remplissez titre, latitude et longitude");
            return;
        }

        try {
            double lat = Double.parseDouble(latStr);
            double lng = Double.parseDouble(lngStr);
            String color = mapColorToJs(cbMarkerColor.getValue());

            googleMapView.addCustomMarker(lat, lng, title, desc, color);
            googleMapView.setView(lat, lng, 12);

            lblMapStatus.setText("📌 Marqueur ajoute: " + title);

            txtMarkerTitle.clear();
            txtMarkerDesc.clear();
            txtMarkerLat.clear();
            txtMarkerLng.clear();
        } catch (NumberFormatException e) {
            lblMapStatus.setText("⚠️ Latitude/Longitude invalide");
        }
    }

    @FXML
    private void handleToggleClickMode() {
        clickModeActive = !clickModeActive;
        googleMapView.setClickMode(clickModeActive);
        if (clickModeActive) {
            btnClickMode.setText("🖱️ Desactiver mode clic");
            btnClickMode.setStyle("-fx-background-color: #B2D944; -fx-text-fill: #076A39; -fx-font-size: 11; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;");
            lblMapStatus.setText("🖱️ Cliquez sur la carte pour placer un marqueur");
        } else {
            btnClickMode.setText("🖱️ Mode clic sur carte");
            btnClickMode.setStyle("-fx-background-color: rgba(178,217,68,0.3); -fx-text-fill: #B2D944; -fx-font-size: 11; -fx-background-radius: 5; -fx-cursor: hand; -fx-border-color: #B2D944; -fx-border-radius: 5;");
            lblMapStatus.setText("Mode clic desactive");
        }
    }

    // ==================== CIRCLE ZONE ====================
    @FXML
    private void handleDrawCircle() {
        try {
            String centerStr = googleMapView.getMapCenter();
            String[] parts = centerStr.split(",");
            double lat = Double.parseDouble(parts[0]);
            double lng = Double.parseDouble(parts[1]);
            double radius = sliderRadius.getValue();
            String color = mapColorToCss(cbCircleColor.getValue());

            googleMapView.drawCircle(lat, lng, radius, color);
            lblMapStatus.setText("⭕ Cercle trace: " + (int) radius + " km autour du centre");
        } catch (Exception e) {
            lblMapStatus.setText("⚠️ Erreur lors du trace du cercle");
        }
    }

    // ==================== ACTION HANDLERS ====================
    @FXML
    private void handleClearAll() {
        googleMapView.clearAll();
        cbShowFarms.setSelected(false);
        cbShowOffices.setSelected(false);
        lblMapStatus.setText("🗑️ Tous les marqueurs effaces");
    }

    @FXML
    private void handleSearchAddress() {
        String address = txtSearchAddress.getText().trim();
        if (address.isEmpty()) {
            return;
        }

        hideSuggestions();
        lblMapStatus.setText("🔍 Recherche Google Maps en cours...");

        new Thread(() -> {
            try {
                List<PlaceDetails> results = googleMapsService.searchText(address);

                Platform.runLater(() -> {
                    if (!results.isEmpty()) {
                        PlaceDetails place = results.get(0);
                        String displayName = place.name + " - " + place.address;
                        googleMapView.searchAndMark(place.latitude, place.longitude, displayName);
                        lblMapStatus.setText("✅ Google Maps: " + place.name + " — "
                                + place.address.substring(0, Math.min(50, place.address.length())));
                    } else {
                        lblMapStatus.setText("❌ Aucun resultat Google Maps pour: " + address);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblMapStatus.setText("❌ Erreur Google Maps: " + e.getMessage()));
            }
        }).start();
    }

    // ==================== AUTOCOMPLETE ====================
    private void setupAutocomplete() {
        txtSearchAddress.textProperty().addListener((obs, oldText, newText) -> {
            if (autocompleteTimer != null) {
                autocompleteTimer.cancel();
            }
            if (newText == null || newText.trim().length() < 3) {
                hideSuggestions();
                return;
            }
            autocompleteTimer = new Timer(true);
            autocompleteTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    fetchAutocomplete(newText.trim());
                }
            }, 400);
        });

        listSuggestions.setOnMouseClicked(event -> {
            PlaceSuggestion selected = listSuggestions.getSelectionModel().getSelectedItem();
            if (selected != null) {
                onSuggestionSelected(selected);
            }
        });

        txtSearchAddress.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                new Timer(true).schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> hideSuggestions());
                    }
                }, 250);
            }
        });
    }

    private void fetchAutocomplete(String input) {
        try {
            List<PlaceSuggestion> suggestions = googleMapsService.autocomplete(input);
            Platform.runLater(() -> {
                if (!suggestions.isEmpty()) {
                    listSuggestions.getItems().setAll(suggestions);
                    listSuggestions.setPrefHeight(Math.min(suggestions.size() * 30 + 5, 180));
                    listSuggestions.setVisible(true);
                    listSuggestions.setManaged(true);
                } else {
                    hideSuggestions();
                }
            });
        } catch (Exception e) {
            System.err.println("Autocomplete error: " + e.getMessage());
            Platform.runLater(this::hideSuggestions);
        }
    }

    private void onSuggestionSelected(PlaceSuggestion suggestion) {
        txtSearchAddress.setText(suggestion.description);
        hideSuggestions();
        lblMapStatus.setText("🔍 Chargement du lieu...");

        new Thread(() -> {
            try {
                PlaceDetails details = googleMapsService.getPlaceDetails(suggestion.placeId);
                Platform.runLater(() -> {
                    String displayName = details.name + " - " + details.address;
                    googleMapView.searchAndMark(details.latitude, details.longitude, displayName);
                    lblMapStatus.setText("✅ " + details.name + " — "
                            + details.address.substring(0, Math.min(50, details.address.length())));
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblMapStatus.setText("❌ Erreur details lieu: " + e.getMessage()));
            }
        }).start();
    }

    private void hideSuggestions() {
        listSuggestions.setVisible(false);
        listSuggestions.setManaged(false);
        listSuggestions.getItems().clear();
    }

    @FXML
    private void handleRecenter() {
        googleMapView.recenter();
        sliderZoom.setValue(7);
        lblMapStatus.setText("🗺️ Carte recentree sur la Tunisie");
    }

    // ==================== UTILITY ====================
    private String mapColorToJs(String color) {
        if (color == null) {
            return "red";
        }
        return switch (color) {
            case "Vert" ->
                "green";
            case "Rouge" ->
                "red";
            case "Or" ->
                "gold";
            case "Bleu" ->
                "blue";
            case "Violet" ->
                "violet";
            case "Orange" ->
                "orange";
            default ->
                "red";
        };
    }

    private String mapColorToCss(String color) {
        if (color == null) {
            return "#089647";
        }
        return switch (color) {
            case "Vert" ->
                "#089647";
            case "Rouge" ->
                "#c0392b";
            case "Bleu" ->
                "#2980b9";
            case "Orange" ->
                "#e67e22";
            case "Violet" ->
                "#8e44ad";
            default ->
                "#089647";
        };
    }
}
