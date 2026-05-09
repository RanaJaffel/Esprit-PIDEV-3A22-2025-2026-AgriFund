package com.agrifund.controller;

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class MapControler {

    @FXML
    private WebView webView;

    @FXML
    public void initialize() {

        WebEngine engine = webView.getEngine();

        String html = """
        <html>
        <body>
        <h1 style='color:green;'>Carte chargée ✅</h1>
        </body>
        </html>
        """;

        engine.loadContent("""
<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet"
href="https://unpkg.com/leaflet/dist/leaflet.css"/>
<script src="https://unpkg.com/leaflet/dist/leaflet.js"></script>
<script src="https://unpkg.com/leaflet.heat/dist/leaflet-heat.js"></script>
</head>
<body>
<div id="map" style="width:100%;height:100%;"></div>
<script>
var map = L.map('map').setView([36.8, 10.1], 7);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png')
.addTo(map);

var heat = L.heatLayer([
    [36.8, 10.1, 0.8],
    [35.8, 9.5, 0.5],
    [34.5, 8.2, 0.9]
], {radius: 25}).addTo(map);

</script>
</body>
</html>
""");
    }
}