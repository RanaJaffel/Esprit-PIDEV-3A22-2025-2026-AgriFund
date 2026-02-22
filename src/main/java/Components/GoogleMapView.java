package Components;

import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.util.function.BiConsumer;

public class GoogleMapView extends StackPane {

    private WebView webView;
    private WebEngine webEngine;
    private BiConsumer<Double, Double> onLocationSelected;
    private BiConsumer<String, double[]> onRegionSelected;
    private boolean mapReady = false;

    private double lastLat = 34.0;
    private double lastLon = 9.0;
    private String lastLocationName = "";

    public GoogleMapView() {
        // Créer le WebView
        webView = new WebView();
        webView.setPrefSize(800, 500);
        webView.setMinSize(400, 300);

        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        // Debug: afficher les erreurs JavaScript
        webEngine.setOnError(event -> System.err.println("❌ WebView Error: " + event.getMessage()));
        webEngine.setOnAlert(event -> System.out.println("⚠️ JS Alert: " + event.getData()));

        // Écouter le chargement
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            System.out.println("�� WebView State: " + newState);

            if (newState == Worker.State.SUCCEEDED) {
                mapReady = true;
                System.out.println("✅ Carte chargée avec succès");
                startPolling();
            } else if (newState == Worker.State.FAILED) {
                System.err.println("❌ Échec du chargement de la carte");
                Throwable ex = webEngine.getLoadWorker().getException();
                if (ex != null) ex.printStackTrace();
            }
        });

        // Charger la carte
        loadMap();

        // Ajouter au StackPane
        this.getChildren().add(webView);
        this.setMinSize(400, 300);
        this.setPrefSize(800, 500);
        this.setStyle("-fx-background-color: #e8f5e9;");

        // S'assurer que le WebView prend tout l'espace
        webView.prefWidthProperty().bind(this.widthProperty());
        webView.prefHeightProperty().bind(this.heightProperty());
    }

    private void loadMap() {
        String html = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Carte</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" crossorigin=""/>
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" crossorigin=""></script>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        html, body { width: 100%; height: 100%; overflow: hidden; }
        #map { width: 100%; height: 100%; background: #e8f5e9; }
        .leaflet-popup-content-wrapper {
            background: linear-gradient(135deg, #089647, #065a2c);
            color: white;
            border-radius: 10px;
        }
        .leaflet-popup-tip { background: #089647; }
        .leaflet-popup-content { margin: 10px 12px; }
    </style>
</head>
<body>
    <div id="map"></div>
    <script>
        var map, currentMarker;
        var selectedData = { lat: null, lon: null, name: null, updated: false };

        // Initialisation
        document.addEventListener('DOMContentLoaded', function() {
            initMap();
        });
        
        // Fallback si DOMContentLoaded déjà passé
        if (document.readyState === 'complete' || document.readyState === 'interactive') {
            setTimeout(initMap, 100);
        }

        function initMap() {
            if (map) return; // Déjà initialisé
            
            try {
                map = L.map('map', {
                    center: [34.0, 9.0],
                    zoom: 7,
                    zoomControl: true
                });

                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    attribution: '© OpenStreetMap',
                    maxZoom: 19
                }).addTo(map);

                // Clic sur la carte
                map.on('click', function(e) {
                    var lat = e.latlng.lat;
                    var lon = e.latlng.lng;
                    placeMarker(lat, lon, 'Chargement...');
                    reverseGeocode(lat, lon);
                });

                console.log('✅ Carte initialisée');
                
                // Forcer le redimensionnement
                setTimeout(function() {
                    map.invalidateSize();
                }, 200);
                
            } catch (err) {
                console.error('❌ Erreur init carte:', err);
            }
        }

        function placeMarker(lat, lon, name) {
            if (currentMarker) map.removeLayer(currentMarker);
            
            currentMarker = L.marker([lat, lon])
                .addTo(map)
                .bindPopup('<b>' + name + '</b><br>Lat: ' + lat.toFixed(5) + '<br>Lon: ' + lon.toFixed(5))
                .openPopup();

            selectedData = { lat: lat, lon: lon, name: name, updated: true };
        }

        function reverseGeocode(lat, lon) {
            fetch('https://nominatim.openstreetmap.org/reverse?format=json&lat=' + lat + '&lon=' + lon + '&zoom=10')
                .then(r => r.json())
                .then(data => {
                    var name = 'Position';
                    if (data.address) {
                        var parts = [];
                        if (data.address.city || data.address.town || data.address.village) 
                            parts.push(data.address.city || data.address.town || data.address.village);
                        if (data.address.state) parts.push(data.address.state);
                        if (data.address.country) parts.push(data.address.country);
                        name = parts.join(', ') || data.display_name.split(',').slice(0,2).join(',');
                    }
                    if (currentMarker) {
                        currentMarker.setPopupContent('<b>' + name + '</b><br>Lat: ' + lat.toFixed(5) + '<br>Lon: ' + lon.toFixed(5));
                    }
                    selectedData.name = name;
                    selectedData.updated = true;
                })
                .catch(e => console.log('Geocode error:', e));
        }

        function getSelectedData() {
            if (selectedData.updated) {
                selectedData.updated = false;
                return JSON.stringify(selectedData);
            }
            return null;
        }

        function searchLocation(query) {
            fetch('https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(query) + '&limit=1')
                .then(r => r.json())
                .then(data => {
                    if (data && data.length > 0) {
                        var lat = parseFloat(data[0].lat);
                        var lon = parseFloat(data[0].lon);
                        var name = data[0].display_name.split(',').slice(0,2).join(',');
                        placeMarker(lat, lon, name);
                        map.setView([lat, lon], 12);
                    }
                })
                .catch(e => console.log('Search error:', e));
        }

        function selectLocation(lat, lon) {
            placeMarker(lat, lon, 'Position');
            map.setView([lat, lon], 12);
            reverseGeocode(lat, lon);
        }

        function centerMap(lat, lon, zoom) {
            if (map) map.setView([lat, lon], zoom || 12);
        }
    </script>
</body>
</html>
""";

        webEngine.loadContent(html);
    }

    private void startPolling() {
        Thread pollingThread = new Thread(() -> {
            while (mapReady) {
                try {
                    Thread.sleep(300);
                    javafx.application.Platform.runLater(this::checkForUpdates);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    private void checkForUpdates() {
        try {
            Object result = webEngine.executeScript("typeof getSelectedData === 'function' ? getSelectedData() : null");

            if (result != null && !"null".equals(result.toString())) {
                String json = result.toString();
                double lat = extractDouble(json, "lat");
                double lon = extractDouble(json, "lon");
                String name = extractString(json, "name");

                if (lat != 0 && lon != 0) {
                    lastLat = lat;
                    lastLon = lon;
                    lastLocationName = name;

                    if (onLocationSelected != null) onLocationSelected.accept(lat, lon);
                    if (onRegionSelected != null) onRegionSelected.accept(name, new double[]{lat, lon});
                }
            }
        } catch (Exception ignored) {}
    }

    private double extractDouble(String json, String key) {
        try {
            int start = json.indexOf("\"" + key + "\":") + key.length() + 3;
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return Double.parseDouble(json.substring(start, end).trim());
        } catch (Exception e) { return 0; }
    }

    private String extractString(String json, String key) {
        try {
            String search = "\"" + key + "\":\"";
            int start = json.indexOf(search) + search.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) { return ""; }
    }

    public void setOnLocationSelected(BiConsumer<Double, Double> callback) { this.onLocationSelected = callback; }
    public void setOnRegionSelected(BiConsumer<String, double[]> callback) { this.onRegionSelected = callback; }
    public void searchLocation(String query) { if (mapReady) webEngine.executeScript("searchLocation('" + query.replace("'", "\\'") + "')"); }
    public void selectLocation(double lat, double lon) { if (mapReady) webEngine.executeScript("selectLocation(" + lat + ", " + lon + ")"); }
    public void centerMap(double lat, double lon, int zoom) { if (mapReady) webEngine.executeScript("centerMap(" + lat + ", " + lon + ", " + zoom + ")"); }
    public double[] getLastSelectedCoordinates() { return new double[]{lastLat, lastLon}; }
    public String getLastLocationName() { return lastLocationName; }
    public boolean isMapReady() { return mapReady; }
}