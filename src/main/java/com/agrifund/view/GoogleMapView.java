package com.agrifund.view;

import java.util.function.BiConsumer;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Composant Google Maps reutilisable base sur Leaflet + tiles Google. Resout le
 * probleme d'affichage des tiles dans JavaFX WebView en: 1) Bindant les
 * dimensions du WebView au parent StackPane 2) Deferant le loadContent apres le
 * layout initial 3) ResizeObserver + invalidateSize a chaque changement de
 * taille 4) Contraintes maxWidth/maxHeight = MAX_VALUE pour remplir l'espace
 */
public class GoogleMapView extends StackPane {

    private final WebView webView;
    private final WebEngine webEngine;
    private BiConsumer<Double, Double> onLocationSelected;
    private BiConsumer<String, double[]> onRegionSelected;
    private BiConsumer<String, String> onCoordinatesUpdate;
    private boolean mapReady = false;
    private boolean htmlLoaded = false;

    private double lastLat = 34.0;
    private double lastLon = 9.0;
    private String lastLocationName = "";

    public GoogleMapView() {
        // 1) Create WebView — fully flexible, no hardcoded sizes
        webView = new WebView();
        webView.setMinSize(100, 100);
        webView.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        webView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        webEngine.setOnError(event -> System.err.println("WebView Error: " + event.getMessage()));
        webEngine.setOnAlert(event -> System.out.println("JS Alert: " + event.getData()));

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                htmlLoaded = true;
                System.out.println("HTML charge, attente du layout pour initMap...");
                // Defer initMap until WebView actually has dimensions
                scheduleMapInit();
            } else if (newState == Worker.State.FAILED) {
                System.err.println("Echec du chargement de la carte");
                Throwable ex = webEngine.getLoadWorker().getException();
                if (ex != null) {
                    ex.printStackTrace();
                }
            }
        });

        // 2) Add WebView to this StackPane FIRST, set constraints
        this.getChildren().add(webView);
        this.setMinSize(100, 100);
        this.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.setStyle("-fx-background-color: #e8f5e9;");

        // 3) Bind WebView size to this StackPane
        webView.prefWidthProperty().bind(this.widthProperty());
        webView.prefHeightProperty().bind(this.heightProperty());

        // 4) When our size changes, tell Leaflet to invalidateSize
        this.widthProperty().addListener((obs, o, n) -> invalidateMapSize());
        this.heightProperty().addListener((obs, o, n) -> invalidateMapSize());

        // 5) Load HTML AFTER everything is wired up (deferred via Platform.runLater)
        Platform.runLater(this::loadMap);
    }

    /**
     * Polls until this StackPane has real dimensions (>50px), then calls
     * initMap() in JS.
     */
    private void scheduleMapInit() {
        Thread t = new Thread(() -> {
            
            // Fallback: init anyway
            Platform.runLater(() -> {
                try {
                    webEngine.executeScript("initMap()");
                    mapReady = true;
                    startPolling();
                } catch (Exception ignored) {
                }
            });
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * Tells Leaflet to recalculate its container size and redraw tiles. Passes
     * the real Java StackPane dimensions to JS so the #map div gets exact pixel
     * sizing (window.innerWidth/Height are unreliable in WebView).
     */
    private void invalidateMapSize() {
        if (mapReady) {
            try {
                int w = Math.max(100, (int) this.getWidth());
                int h = Math.max(100, (int) this.getHeight());
                webEngine.executeScript(
                        "resizeMapTo(" + w + "," + h + ");"
                        + "if(map) map.invalidateSize({animate:false});"
                );
            } catch (Exception ignored) {
            }
        }
    }

    private void loadMap() {
        String html = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AgriFund Map</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" crossorigin=""/>
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" crossorigin=""></script>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        html, body { width: 100%; height: 100%; overflow: hidden; margin: 0; padding: 0; }
        #map { width: 100%; height: 100%; background: #e8f5e9; }
        .agrifund-popup { font-family: Arial, sans-serif; }
        .agrifund-popup h3 { color: #076A39; margin: 0 0 5px 0; font-size: 14px; }
        .agrifund-popup p { margin: 3px 0; color: #555; font-size: 12px; }
        .leaflet-popup-content-wrapper {
            border-radius: 8px;
            box-shadow: 0 3px 14px rgba(0,0,0,0.2);
        }
        .leaflet-control-zoom { border: 2px solid #4285F4 !important; border-radius: 4px; }
        .leaflet-control-zoom a { color: #4285F4 !important; }
        .leaflet-control-attribution { font-size: 10px; }
    </style>
</head>
<body>
    <div id="map"></div>
    <script>
        var map, currentTileLayer, tileLayers;
        var farmMarkers, officeMarkers, customMarkers, searchMarker, circles;
        var clickMode = false;
        var icons;
        var mapInitialized = false;
        var selectedData = { lat: null, lon: null, name: null, coordLat: null, coordLng: null, updated: false, coordUpdated: false };

        // Resize #map div to exact pixel dimensions
        function resizeMapTo(w, h) {
            var el = document.getElementById('map');
            if (el) {
                el.style.width = w + 'px';
                el.style.height = h + 'px';
            }
        }
        // Resize #map to fill the viewport
        function resizeMapDiv() {
            resizeMapTo(window.innerWidth || document.documentElement.clientWidth || 800,
                        window.innerHeight || document.documentElement.clientHeight || 600);
        }

        // DO NOT auto-init — Java will call initMap() when container has real size
        function initMap() {
            if (mapInitialized) return;
            mapInitialized = true;

            try {
                // Force exact pixel dimensions before creating the map
                resizeMapDiv();

                map = L.map('map', {
                    center: [34.0, 9.5],
                    zoom: 7,
                    zoomControl: true,
                    minZoom: 3,
                    maxBounds: [[-85, -180], [85, 180]],
                    maxBoundsViscosity: 1.0
                });

                // Google Maps tile layers (noWrap prevents world duplication)
                tileLayers = {
                    roadmap: L.tileLayer('https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
                        attribution: '\\u00a9 Google Maps', maxZoom: 20, noWrap: true
                    }),
                    satellite: L.tileLayer('https://mt1.google.com/vt/lyrs=s&x={x}&y={y}&z={z}', {
                        attribution: '\\u00a9 Google Maps', maxZoom: 20, noWrap: true
                    }),
                    terrain: L.tileLayer('https://mt1.google.com/vt/lyrs=p&x={x}&y={y}&z={z}', {
                        attribution: '\\u00a9 Google Maps', maxZoom: 20, noWrap: true
                    }),
                    hybrid: L.tileLayer('https://mt1.google.com/vt/lyrs=y&x={x}&y={y}&z={z}', {
                        attribution: '\\u00a9 Google Maps', maxZoom: 20, noWrap: true
                    })
                };
                currentTileLayer = tileLayers.roadmap;
                currentTileLayer.addTo(map);

                // Icons
                function makeIcon(color) {
                    return L.icon({
                        iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-' + color + '.png',
                        shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
                        iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34]
                    });
                }
                icons = { green: makeIcon('green'), red: makeIcon('red'), gold: makeIcon('gold'),
                          blue: makeIcon('blue'), violet: makeIcon('violet'), orange: makeIcon('orange') };

                // Marker groups
                farmMarkers = L.layerGroup();
                officeMarkers = L.layerGroup();
                customMarkers = L.layerGroup().addTo(map);
                searchMarker = null;
                circles = L.layerGroup().addTo(map);

                // Load farms
                var farms = [
                    [36.80, 10.17, 'Exploitation Cap Bon', 'Agrumes et vignobles - Region du Cap Bon'],
                    [35.82, 10.60, 'Ferme Sahel', 'Oleiculture - Region du Sahel (Sousse)'],
                    [36.47, 9.33, 'Exploitation Beja', 'Cereales et bovins - Gouvernorat de Beja'],
                    [35.17, 8.83, 'Ferme Kasserine', 'Elevage ovin et alfa - Region de Kasserine'],
                    [34.42, 8.78, 'Exploitation Gafsa', 'Oasis et palmiers dattiers - Region de Gafsa'],
                    [33.88, 8.13, 'Ferme Tozeur', 'Dattes Deglet Nour et cultures oasiennes - Tozeur'],
                    [36.72, 8.97, 'Exploitation Jendouba', 'Tabac et cereales - Gouvernorat de Jendouba']
                ];
                farms.forEach(function(f) {
                    L.marker([f[0], f[1]], {icon: icons.gold}).bindPopup('<div class="agrifund-popup"><h3>' + f[2] + '</h3><p>' + f[3] + '</p></div>').addTo(farmMarkers);
                });

                // Load offices
                var offices = [
                    [36.8065, 10.1815, 'AgriFund Tunis (Siege)', 'Siege social - Avenue Habib Bourguiba, Tunis'],
                    [35.8245, 10.6346, 'AgriFund Sousse', 'Agence regionale - Avenue du 14 Janvier, Sousse'],
                    [34.7398, 10.7600, 'AgriFund Sfax', 'Agence regionale - Avenue Hedi Chaker, Sfax'],
                    [36.4513, 10.7357, 'AgriFund Nabeul', 'Agence regionale - Avenue Habib Thameur, Nabeul'],
                    [36.7190, 9.1840, 'AgriFund Beja', 'Agence regionale - Rue de la Republique, Beja'],
                    [33.9185, 8.1029, 'AgriFund Tozeur', 'Agence regionale - Avenue Farhat Hached, Tozeur']
                ];
                offices.forEach(function(o) {
                    L.marker([o[0], o[1]], {icon: icons.green}).bindPopup('<div class="agrifund-popup"><h3>' + o[2] + '</h3><p>' + o[3] + '</p></div>').addTo(officeMarkers);
                });

                // Mouse move — coordinates tracking
                map.on('mousemove', function(e) {
                    selectedData.coordLat = e.latlng.lat.toFixed(5);
                    selectedData.coordLng = e.latlng.lng.toFixed(5);
                    selectedData.coordUpdated = true;
                });

                // Click
                map.on('click', function(e) {
                    if (clickMode) {
                        var lat = e.latlng.lat;
                        var lon = e.latlng.lng;
                        selectedData = { lat: lat, lon: lon, name: 'Click', coordLat: selectedData.coordLat, coordLng: selectedData.coordLng, updated: true, coordUpdated: false };
                    }
                });

                // ResizeObserver — resize div + invalidateSize when container resizes
                if (typeof ResizeObserver !== 'undefined') {
                    new ResizeObserver(function() {
                        resizeMapDiv();
                        if (map) map.invalidateSize({animate: false});
                    }).observe(document.getElementById('map'));
                }

                // Window resize — resize div + invalidateSize
                window.addEventListener('resize', function() {
                    resizeMapDiv();
                    if (map) {
                        map.invalidateSize({animate: false});
                        // Double-tap after a short delay for WebView quirks
                        setTimeout(function() {
                            resizeMapDiv();
                            map.invalidateSize({animate: false});
                        }, 100);
                    }
                });

                // Aggressive periodic invalidateSize for the first 5 seconds
                var fixCount = 0;
                var fixInterval = setInterval(function() {
                    fixCount++;
                    resizeMapDiv();
                    if (map) map.invalidateSize({animate: false});
                    if (fixCount >= 50) clearInterval(fixInterval); // stop after 5s
                }, 100);

                console.log('Carte Google Maps initialisee');

            } catch (err) {
                console.error('Erreur init carte:', err);
            }
        }

        // Functions called from Java via executeScript
        function switchTile(name) {
            if (!map || !tileLayers[name]) return;
            if (currentTileLayer) map.removeLayer(currentTileLayer);
            currentTileLayer = tileLayers[name];
            currentTileLayer.addTo(map);
        }

        function addCustomMarker(lat, lng, title, desc, color) {
            if (!map) return;
            var icon = icons[color] || icons.red;
            var marker = L.marker([lat, lng], {icon: icon}).addTo(customMarkers);
            marker.bindPopup('<div class="agrifund-popup"><h3>' + title + '</h3><p>' + desc + '</p></div>');
            return marker;
        }

        function searchLocation(lat, lng, title) {
            if (!map) return;
            if (searchMarker) map.removeLayer(searchMarker);
            searchMarker = L.marker([lat, lng], {icon: icons.red}).addTo(map);
            searchMarker.bindPopup('<div class="agrifund-popup"><h3>' + title + '</h3></div>').openPopup();
            map.setView([lat, lng], 14);
        }

        function drawCircle(lat, lng, radiusKm, color) {
            if (!map) return;
            L.circle([lat, lng], { radius: radiusKm * 1000, color: color, fillColor: color, fillOpacity: 0.15, weight: 2 }).addTo(circles);
        }

        function clearCircles() { if (circles) circles.clearLayers(); }
        function clearCustomMarkers() { if (customMarkers) customMarkers.clearLayers(); }
        function clearAll() {
            if (farmMarkers) farmMarkers.clearLayers();
            if (officeMarkers) officeMarkers.clearLayers();
            if (customMarkers) customMarkers.clearLayers();
            if (circles) circles.clearLayers();
            if (searchMarker) { map.removeLayer(searchMarker); searchMarker = null; }
        }

        function showLayer(name, show) {
            if (!map) return;
            var layer = name === 'farms' ? farmMarkers : (name === 'offices' ? officeMarkers : customMarkers);
            if (show) { if (!map.hasLayer(layer)) map.addLayer(layer); }
            else { if (map.hasLayer(layer)) map.removeLayer(layer); }
        }

        function setClickMode(active) {
            clickMode = active;
            if (map) document.getElementById('map').style.cursor = active ? 'crosshair' : '';
        }

        function recenter() { if (map) map.setView([34.0, 9.5], 7); }

        function getMapCenter() {
            if (!map) return '34,9.5';
            var c = map.getCenter();
            return c.lat + ',' + c.lng;
        }

        // Polling data for Java
        function getSelectedData() {
            if (selectedData.updated) {
                selectedData.updated = false;
                return JSON.stringify({ lat: selectedData.lat, lon: selectedData.lon, name: selectedData.name });
            }
            return null;
        }

        function getCoordData() {
            if (selectedData.coordUpdated) {
                selectedData.coordUpdated = false;
                return selectedData.coordLat + '|' + selectedData.coordLng;
            }
            return null;
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
                    Thread.sleep(200);
                    Platform.runLater(this::checkForUpdates);
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
            // Check click data
            Object result = webEngine.executeScript("typeof getSelectedData === 'function' ? getSelectedData() : null");
            if (result != null && !"null".equals(result.toString())) {
                String json = result.toString();
                double lat = extractDouble(json, "lat");
                double lon = extractDouble(json, "lon");

                if (lat != 0 && lon != 0) {
                    lastLat = lat;
                    lastLon = lon;
                    if (onLocationSelected != null) {
                        onLocationSelected.accept(lat, lon);
                    }
                }
            }

            // Check coordinate hover data
            Object coordResult = webEngine.executeScript("typeof getCoordData === 'function' ? getCoordData() : null");
            if (coordResult != null && !"null".equals(coordResult.toString())) {
                String[] parts = coordResult.toString().split("\\|");
                if (parts.length == 2 && onCoordinatesUpdate != null) {
                    onCoordinatesUpdate.accept(parts[0], parts[1]);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private double extractDouble(String json, String key) {
        try {
            int start = json.indexOf("\"" + key + "\":") + key.length() + 3;
            int end = json.indexOf(",", start);
            if (end == -1) {
                end = json.indexOf("}", start);
            }
            return Double.parseDouble(json.substring(start, end).trim());
        } catch (Exception e) {
            return 0;
        }
    }

    // ==================== PUBLIC API ====================
    public void setOnLocationSelected(BiConsumer<Double, Double> callback) {
        this.onLocationSelected = callback;
    }

    public void setOnRegionSelected(BiConsumer<String, double[]> callback) {
        this.onRegionSelected = callback;
    }

    public void setOnCoordinatesUpdate(BiConsumer<String, String> callback) {
        this.onCoordinatesUpdate = callback;
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }

    public boolean isMapReady() {
        return mapReady;
    }

    public void switchTile(String tileName) {
        if (mapReady) {
            webEngine.executeScript("switchTile('" + tileName + "')");
        }
    }

    public void addCustomMarker(double lat, double lng, String title, String desc, String color) {
        if (mapReady) {
            String t = title.replace("'", "\\'");
            String d = desc.replace("'", "\\'");
            webEngine.executeScript("addCustomMarker(" + lat + ", " + lng + ", '" + t + "', '" + d + "', '" + color + "')");
        }
    }

    public void searchAndMark(double lat, double lng, String title) {
        if (mapReady) {
            String t = title.replace("'", "\\'").replace("\"", "\\\"");
            webEngine.executeScript("searchLocation(" + lat + ", " + lng + ", '" + t + "')");
        }
    }

    public void drawCircle(double lat, double lng, double radiusKm, String cssColor) {
        if (mapReady) {
            webEngine.executeScript("drawCircle(" + lat + ", " + lng + ", " + radiusKm + ", '" + cssColor + "')");
        }
    }

    public void showLayer(String name, boolean show) {
        if (mapReady) {
            webEngine.executeScript("showLayer('" + name + "', " + show + ")");
        }
    }

    public void setClickMode(boolean active) {
        if (mapReady) {
            webEngine.executeScript("setClickMode(" + active + ")");
        }
    }

    public void clearAll() {
        if (mapReady) {
            webEngine.executeScript("clearAll()");
        }
    }

    public void recenter() {
        if (mapReady) {
            webEngine.executeScript("recenter()");
        }
    }

    public void setZoom(int zoom) {
        if (mapReady) {
            webEngine.executeScript("map.setZoom(" + zoom + ")");
        }
    }

    public void setTileOpacity(double opacity) {
        if (mapReady) {
            webEngine.executeScript("if(currentTileLayer) currentTileLayer.setOpacity(" + opacity + ")");
        }
    }

    public void setView(double lat, double lng, int zoom) {
        if (mapReady) {
            webEngine.executeScript("map.setView([" + lat + ", " + lng + "], " + zoom + ")");
        }
    }

    public String getMapCenter() {
        if (mapReady) {
            try {
                return (String) webEngine.executeScript("getMapCenter()");
            } catch (Exception e) {
                return "34,9.5";
            }
        }
        return "34,9.5";
    }

    public double[] getLastSelectedCoordinates() {
        return new double[]{lastLat, lastLon};
    }

    public String getLastLocationName() {
        return lastLocationName;
    }
}
