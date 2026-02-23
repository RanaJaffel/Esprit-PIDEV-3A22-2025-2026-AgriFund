package services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiGeoService {

    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Appelle l'API Nominatim (OpenStreetMap) pour convertir une adresse
     * en coordonnées GPS.
     *
     * @param query localisation, ex : "Sfax, Tunisie"
     * @return un objet Coordinates (lat, lon) ou null si introuvable / erreur
     */
    public Coordinates geocode(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search"
                    + "?format=json&limit=1&q=" + encoded;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    // Nominatim demande un User-Agent identifiable
                    .header("User-Agent", "PidevAgrifund/1.0 (contact@exemple.com)")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("❌ Erreur API geocoding, statut = " + response.statusCode());
                System.err.println(response.body());
                return null;
            }

            JSONArray array = new JSONArray(response.body());
            if (array.isEmpty()) {
                System.err.println("❌ Aucune coordonnée trouvée pour : " + query);
                return null;
            }

            JSONObject first = array.getJSONObject(0);

            // lat/lon sont retournés sous forme de texte
            double lat = Double.parseDouble(first.getString("lat"));
            double lon = Double.parseDouble(first.getString("lon"));

            System.out.println("📍 " + query + " -> lat=" + lat + ", lon=" + lon);
            return new Coordinates(lat, lon);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Petit objet pour transporter les coordonnées */
    public static class Coordinates {
        private final double lat;
        private final double lon;

        public Coordinates(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public double getLat() { return lat; }
        public double getLon() { return lon; }
    }
}