package services;

import entities.MeteoData;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiMeteoService {

    // Mets ici ta clé OpenWeatherMap
    private static final String API_KEY = "81e47e8697241e1f54c3215137a8b5bf"; // ex: "b1234abcd..."
    private static final String BASE_URL =
            "https://api.openweathermap.org/data/2.5/weather";

    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Retourne la température en °C pour une ville donnée.
     * Retourne -999 en cas d'erreur (ce que ton contrôleur teste déjà).
     */
    public double getTemperature(String city) {
        try {
            String cityEncoded = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = BASE_URL
                    + "?q=" + cityEncoded
                    + "&units=metric"
                    + "&appid=" + API_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("❌ Erreur API météo, statut = " + response.statusCode());
                System.err.println(response.body());
                return -999;
            }

            JSONObject json = new JSONObject(response.body());
            // { "main": { "temp": 23.5, ... } }
            double temp = json.getJSONObject("main").getDouble("temp");
            System.out.println("🌤 Température API pour " + city + " = " + temp);
            return temp;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return -999;
        } catch (Exception e) {
            System.err.println("❌ Erreur de parsing JSON météo : " + e.getMessage());
            return -999;
        }
    }
    public MeteoData getWeather(String city) {

        try {

            String cityEncoded = URLEncoder.encode(city, StandardCharsets.UTF_8);

            String url = BASE_URL
                    + "?q=" + cityEncoded
                    + "&units=metric"
                    + "&lang=fr"
                    + "&appid=" + API_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            JSONObject json = new JSONObject(response.body());

            double temp = json.getJSONObject("main").getDouble("temp");
            double feels = json.getJSONObject("main").getDouble("feels_like");
            int humidity = json.getJSONObject("main").getInt("humidity");
            double wind = json.getJSONObject("wind").getDouble("speed");

            String description =
                    json.getJSONArray("weather")
                            .getJSONObject(0)
                            .getString("description");

            String icon =
                    json.getJSONArray("weather")
                            .getJSONObject(0)
                            .getString("icon");

            return new MeteoData(
                    temp,
                    feels,
                    humidity,
                    wind,
                    description,
                    icon
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public double getTemperatureByCoords(double lat, double lon) {

        try {

            String url = BASE_URL
                    + "?lat=" + lat
                    + "&lon=" + lon
                    + "&units=metric"
                    + "&appid=" + API_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request,
                            HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return -999;

            JSONObject json = new JSONObject(response.body());
            return json.getJSONObject("main").getDouble("temp");

        } catch (Exception e) {
            return -999;
        }
    }
}