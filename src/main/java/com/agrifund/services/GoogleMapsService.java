package com.agrifund.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Service Google Maps Places API via RapidAPI. Fournit l'autocomplete et la
 * recherche de lieux avec coordonnees.
 */
public class GoogleMapsService {

    private static final String RAPIDAPI_KEY = "791da6c58bmsh0a1da3ec10a35dap11c23ajsn4a7e56d67da9";
    private static final String RAPIDAPI_HOST = "google-map-places-new-v2.p.rapidapi.com";
    private static final String BASE_URL = "https://google-map-places-new-v2.p.rapidapi.com/v1/places";

    private final HttpClient httpClient;

    public GoogleMapsService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Resultat d'une suggestion de lieu.
     */
    public static class PlaceSuggestion {

        public final String placeId;
        public final String description;
        public final String mainText;

        public PlaceSuggestion(String placeId, String description, String mainText) {
            this.placeId = placeId;
            this.description = description;
            this.mainText = mainText;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    /**
     * Resultat detaille d'un lieu avec coordonnees.
     */
    public static class PlaceDetails {

        public final String name;
        public final String address;
        public final double latitude;
        public final double longitude;
        public final String placeId;

        public PlaceDetails(String name, String address, double latitude, double longitude, String placeId) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.placeId = placeId;
        }
    }

    /**
     * Autocomplete — retourne les suggestions pour un texte saisi. Biaise vers
     * la Tunisie (lat 34, lng 9.5, rayon 500km).
     */
    public List<PlaceSuggestion> autocomplete(String input) throws Exception {
        JSONObject body = new JSONObject();
        body.put("input", input);

        // Bias towards Tunisia
        JSONObject locationBias = new JSONObject();
        JSONObject circle = new JSONObject();
        JSONObject center = new JSONObject();
        center.put("latitude", 34.0);
        center.put("longitude", 9.5);
        circle.put("center", center);
        circle.put("radius", 500000); // 500 km
        locationBias.put("circle", circle);
        body.put("locationBias", locationBias);

        body.put("languageCode", "fr");
        body.put("regionCode", "TN");
        body.put("includeQueryPredictions", true);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ":autocomplete"))
                .header("x-rapidapi-key", RAPIDAPI_KEY)
                .header("x-rapidapi-host", RAPIDAPI_HOST)
                .header("Content-Type", "application/json")
                .header("X-Goog-FieldMask", "*")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        List<PlaceSuggestion> suggestions = new ArrayList<>();

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            if (json.has("suggestions")) {
                JSONArray suggestionsArr = json.getJSONArray("suggestions");
                for (int i = 0; i < suggestionsArr.length(); i++) {
                    JSONObject suggestion = suggestionsArr.getJSONObject(i);
                    if (suggestion.has("placePrediction")) {
                        JSONObject prediction = suggestion.getJSONObject("placePrediction");
                        String placeId = prediction.optString("placeId", "");
                        String text = "";
                        String mainText = "";
                        if (prediction.has("text")) {
                            text = prediction.getJSONObject("text").optString("text", "");
                        }
                        if (prediction.has("structuredFormat") && prediction.getJSONObject("structuredFormat").has("mainText")) {
                            mainText = prediction.getJSONObject("structuredFormat").getJSONObject("mainText").optString("text", "");
                        }
                        if (!placeId.isEmpty()) {
                            suggestions.add(new PlaceSuggestion(placeId, text, mainText));
                        }
                    }
                }
            }
        } else {
            System.err.println("Google Maps autocomplete error " + response.statusCode() + ": " + response.body());
        }

        return suggestions;
    }

    /**
     * Obtenir les details d'un lieu par son placeId (coordonnees, nom,
     * adresse).
     */
    public PlaceDetails getPlaceDetails(String placeId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + placeId))
                .header("x-rapidapi-key", RAPIDAPI_KEY)
                .header("x-rapidapi-host", RAPIDAPI_HOST)
                .header("Content-Type", "application/json")
                .header("X-Goog-FieldMask", "id,displayName,formattedAddress,location")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            String name = "";
            String address = json.optString("formattedAddress", "");
            double lat = 0, lng = 0;

            if (json.has("displayName")) {
                name = json.getJSONObject("displayName").optString("text", "");
            }
            if (json.has("location")) {
                lat = json.getJSONObject("location").optDouble("latitude", 0);
                lng = json.getJSONObject("location").optDouble("longitude", 0);
            }

            return new PlaceDetails(name, address, lat, lng, placeId);
        } else {
            throw new RuntimeException("Erreur API Google Maps (" + response.statusCode() + "): " + response.body());
        }
    }

    /**
     * Recherche textuelle — retourne directement les lieux avec coordonnees.
     */
    public List<PlaceDetails> searchText(String query) throws Exception {
        JSONObject body = new JSONObject();
        body.put("textQuery", query);

        // Bias towards Tunisia
        JSONObject locationBias = new JSONObject();
        JSONObject circle = new JSONObject();
        JSONObject center = new JSONObject();
        center.put("latitude", 34.0);
        center.put("longitude", 9.5);
        circle.put("center", center);
        circle.put("radius", 500000);
        locationBias.put("circle", circle);
        body.put("locationBias", locationBias);

        body.put("languageCode", "fr");
        body.put("regionCode", "TN");
        body.put("maxResultCount", 5);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + ":searchText"))
                .header("x-rapidapi-key", RAPIDAPI_KEY)
                .header("x-rapidapi-host", RAPIDAPI_HOST)
                .header("Content-Type", "application/json")
                .header("X-Goog-FieldMask", "places.id,places.displayName,places.formattedAddress,places.location")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        List<PlaceDetails> results = new ArrayList<>();

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            if (json.has("places")) {
                JSONArray places = json.getJSONArray("places");
                for (int i = 0; i < places.length(); i++) {
                    JSONObject place = places.getJSONObject(i);
                    String id = place.optString("id", "");
                    String name = "";
                    String address = place.optString("formattedAddress", "");
                    double lat = 0, lng = 0;

                    if (place.has("displayName")) {
                        name = place.getJSONObject("displayName").optString("text", "");
                    }
                    if (place.has("location")) {
                        lat = place.getJSONObject("location").optDouble("latitude", 0);
                        lng = place.getJSONObject("location").optDouble("longitude", 0);
                    }
                    results.add(new PlaceDetails(name, address, lat, lng, id));
                }
            }
        } else {
            System.err.println("Google Maps searchText error " + response.statusCode() + ": " + response.body());
        }

        return results;
    }
}
