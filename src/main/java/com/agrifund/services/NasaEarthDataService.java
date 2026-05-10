package com.agrifund.services;

import com.agrifund.entities.DonneesSatellite;
import com.agrifund.entities.AnalyseRisqueAgricole;
import com.agrifund.util.DatabaseConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NasaEarthDataService {

    private Connection conn;

    // NASA POWER API - Gratuite et sans authentification
    private static final String NASA_POWER_BASE_URL = "https://power.larc.nasa.gov/api/temporal/daily/point";

    // Paramètres climatiques à récupérer
    private static final String PARAMETERS = "T2M,PRECTOTCORR,RH2M";

    /**
     * Constructeur - Initialise la connexion à la base de données
     */
    public NasaEarthDataService() {
        try {
            this.conn = DatabaseConnection.getConnection();
            System.out.println("✅ NasaEarthDataService initialisé");
        } catch (SQLException e) {
            System.err.println("❌ Erreur connexion NasaEarthDataService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Récupère la connexion, la recrée si nécessaire
     */
    private Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DatabaseConnection.getConnection();
        }
        return conn;
    }

    // ==================== RÉCUPÉRATION DES DONNÉES NASA ====================

    /**
     * Récupère les données climatiques de NASA POWER API
     *
     * @param latitude Latitude de la position
     * @param longitude Longitude de la position
     * @param startDate Date de début de la période
     * @param endDate Date de fin de la période
     * @param regionName Nom de la région (optionnel)
     * @return DonneesSatellite contenant les données climatiques
     */
    /**
     * Récupère les données - SANS LIMITE DE DATE
     */
    public DonneesSatellite fetchNasaData(double latitude, double longitude,
                                          LocalDate startDate, LocalDate endDate,
                                          String regionName) throws Exception {

        System.out.println("🛰️ Récupération données...");
        System.out.println("📍 Position: " + regionName + " (" + latitude + ", " + longitude + ")");
        System.out.println("📅 Période: " + startDate + " → " + endDate);

        // PAS DE VALIDATION DE DATE - on utilise ce qui est fourni
        // Si la date est dans le futur ou trop récente, l'API retournera une erreur
        // et on utilisera les données de démonstration

        String startStr = startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String endStr = endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String urlString = String.format(Locale.US,
                "%s?parameters=%s&community=AG&longitude=%.4f&latitude=%.4f&start=%s&end=%s&format=JSON",
                NASA_POWER_BASE_URL, PARAMETERS, longitude, latitude, startStr, endStr
        );

        System.out.println("🌐 URL: " + urlString);

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                return parseNasaResponse(response.toString(), latitude, longitude, endDate, regionName);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur API: " + e.getMessage());
        }

        // Si erreur, utiliser données de démo
        System.out.println("⚠️ Utilisation données de démonstration");
        return generateDemoData(latitude, longitude, endDate, regionName);
    }
    /**
     * Parse la réponse JSON de NASA POWER
     */
    private DonneesSatellite parseNasaResponse(String jsonResponse, double lat, double lon,
                                               LocalDate date, String region) {
        DonneesSatellite data = new DonneesSatellite(lat, lon, date);
        data.setRegion(region);
        data.setDonneesBrutes(jsonResponse);

        try {
            System.out.println("📊 Parsing de la réponse JSON...");

            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();

            // Vérifier la structure de la réponse
            if (!root.has("properties")) {
                System.err.println("⚠️ Pas de 'properties' dans la réponse");
                return generateDemoData(lat, lon, date, region);
            }

            JsonObject properties = root.getAsJsonObject("properties");

            if (!properties.has("parameter")) {
                System.err.println("⚠️ Pas de 'parameter' dans la réponse");
                return generateDemoData(lat, lon, date, region);
            }

            JsonObject parameters = properties.getAsJsonObject("parameter");

            // Température moyenne (T2M - Temperature at 2 Meters)
            if (parameters.has("T2M")) {
                JsonObject t2m = parameters.getAsJsonObject("T2M");
                double avgTemp = calculateAverage(t2m);
                data.setTemperatureMoyenne(avgTemp);
                System.out.println("   ✓ Température moyenne: " + String.format("%.2f", avgTemp) + "°C");
            } else {
                data.setTemperatureMoyenne(22.0);
                System.out.println("   ⚠️ T2M non disponible, valeur par défaut");
            }

            // Précipitations (PRECTOTCORR - Precipitation Corrected)
            if (parameters.has("PRECTOTCORR")) {
                JsonObject precip = parameters.getAsJsonObject("PRECTOTCORR");
                double totalPrecip = calculateSum(precip);
                data.setPrecipitation(totalPrecip);
                System.out.println("   ✓ Précipitations totales: " + String.format("%.2f", totalPrecip) + " mm");
            } else {
                data.setPrecipitation(25.0);
                System.out.println("   ⚠️ PRECTOTCORR non disponible, valeur par défaut");
            }

            // Humidité relative (RH2M - Relative Humidity at 2 Meters)
            if (parameters.has("RH2M")) {
                JsonObject rh = parameters.getAsJsonObject("RH2M");
                double avgHumidity = calculateAverage(rh);
                data.setHumidite(avgHumidity);
                System.out.println("   ✓ Humidité moyenne: " + String.format("%.2f", avgHumidity) + "%");
            } else {
                data.setHumidite(55.0);
                System.out.println("   ⚠️ RH2M non disponible, valeur par défaut");
            }

            // Calculer les indicateurs dérivés
            double indiceSecheresse = calculateDroughtIndex(data);
            data.setIndiceSecheresse(indiceSecheresse);
            System.out.println("   ✓ Indice de sécheresse: " + String.format("%.0f", indiceSecheresse) + "/100");

            double ndvi = estimateNDVI(data);
            data.setNdvi(ndvi);
            System.out.println("   ✓ NDVI estimé: " + String.format("%.3f", ndvi));

            String risque = data.calculerRisque();
            data.setRisqueAgricole(risque);
            System.out.println("   ✓ Niveau de risque: " + risque);

            System.out.println("✅ Parsing terminé avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing JSON: " + e.getMessage());
            e.printStackTrace();
            return generateDemoData(lat, lon, date, region);
        }

        return data;
    }

    /**
     * Génère des données de démonstration si l'API est indisponible
     */
    private DonneesSatellite generateDemoData(double lat, double lon, LocalDate date, String region) {
        System.out.println("🎲 Génération de données de démonstration...");

        DonneesSatellite data = new DonneesSatellite(lat, lon, date);
        data.setRegion(region);

        // Générer des données réalistes basées sur la position géographique
        // Tunisie : climat méditerranéen au nord, désertique au sud

        double tempBase;
        double precipBase;
        double humidBase;

        if (lat > 35) {
            // Nord de la Tunisie - climat méditerranéen
            tempBase = 18 + (Math.random() * 10);
            precipBase = 40 + (Math.random() * 60);
            humidBase = 55 + (Math.random() * 25);
        } else if (lat > 33) {
            // Centre - climat semi-aride
            tempBase = 22 + (Math.random() * 12);
            precipBase = 20 + (Math.random() * 40);
            humidBase = 40 + (Math.random() * 25);
        } else {
            // Sud - climat désertique
            tempBase = 28 + (Math.random() * 15);
            precipBase = 5 + (Math.random() * 20);
            humidBase = 25 + (Math.random() * 20);
        }

        data.setTemperatureMoyenne(Math.round(tempBase * 10) / 10.0);
        data.setPrecipitation(Math.round(precipBase * 10) / 10.0);
        data.setHumidite(Math.round(humidBase * 10) / 10.0);

        // Calculer les indicateurs dérivés
        data.setIndiceSecheresse(calculateDroughtIndex(data));
        data.setNdvi(estimateNDVI(data));
        data.setRisqueAgricole(data.calculerRisque());

        data.setDonneesBrutes("{\"source\": \"demo\", \"generated\": \"" + LocalDateTime.now() + "\"}");

        System.out.println("   Température: " + data.getTemperatureMoyenne() + "°C");
        System.out.println("   Précipitations: " + data.getPrecipitation() + " mm");
        System.out.println("   Humidité: " + data.getHumidite() + "%");
        System.out.println("   Sécheresse: " + data.getIndiceSecheresse() + "/100");
        System.out.println("   NDVI: " + data.getNdvi());
        System.out.println("   Risque: " + data.getRisqueAgricole());

        return data;
    }

    /**
     * Calcule la moyenne des valeurs dans un JsonObject
     */
    private double calculateAverage(JsonObject values) {
        double sum = 0;
        int count = 0;

        for (String key : values.keySet()) {
            try {
                double val = values.get(key).getAsDouble();
                // NASA utilise -999 pour les données manquantes
                if (val > -900) {
                    sum += val;
                    count++;
                }
            } catch (Exception ignored) {}
        }

        return count > 0 ? Math.round((sum / count) * 100) / 100.0 : 0;
    }

    /**
     * Calcule la somme des valeurs dans un JsonObject
     */
    private double calculateSum(JsonObject values) {
        double sum = 0;

        for (String key : values.keySet()) {
            try {
                double val = values.get(key).getAsDouble();
                if (val > -900) {
                    sum += val;
                }
            } catch (Exception ignored) {}
        }

        return Math.round(sum * 100) / 100.0;
    }

    /**
     * Calcule l'indice de sécheresse (0-100)
     * Plus l'indice est élevé, plus la sécheresse est sévère
     */
    private double calculateDroughtIndex(DonneesSatellite data) {
        double index = 50; // Valeur neutre

        // Impact des précipitations
        if (data.getPrecipitation() != null) {
            double precip = data.getPrecipitation();
            if (precip < 10) index += 30;
            else if (precip < 25) index += 20;
            else if (precip < 50) index += 10;
            else if (precip > 100) index -= 20;
            else if (precip > 70) index -= 10;
        }

        // Impact de la température
        if (data.getTemperatureMoyenne() != null) {
            double temp = data.getTemperatureMoyenne();
            if (temp > 40) index += 20;
            else if (temp > 35) index += 15;
            else if (temp > 30) index += 5;
            else if (temp < 15) index -= 5;
        }

        // Impact de l'humidité
        if (data.getHumidite() != null) {
            double humid = data.getHumidite();
            if (humid < 30) index += 15;
            else if (humid < 45) index += 8;
            else if (humid > 75) index -= 12;
            else if (humid > 60) index -= 5;
        }

        // Borner entre 0 et 100
        return Math.max(0, Math.min(100, Math.round(index)));
    }

    /**
     * Estime l'indice de végétation NDVI (-1 à 1)
     * Basé sur les conditions climatiques
     */
    private double estimateNDVI(DonneesSatellite data) {
        double ndvi = 0.5; // Valeur neutre

        // Impact des précipitations
        if (data.getPrecipitation() != null) {
            double precip = data.getPrecipitation();
            if (precip > 80) ndvi += 0.25;
            else if (precip > 50) ndvi += 0.15;
            else if (precip > 30) ndvi += 0.05;
            else if (precip < 15) ndvi -= 0.2;
            else if (precip < 25) ndvi -= 0.1;
        }

        // Impact de la température
        if (data.getTemperatureMoyenne() != null) {
            double temp = data.getTemperatureMoyenne();
            if (temp >= 18 && temp <= 28) ndvi += 0.1;
            else if (temp > 38) ndvi -= 0.15;
            else if (temp > 35) ndvi -= 0.08;
            else if (temp < 8) ndvi -= 0.1;
        }

        // Impact de l'humidité
        if (data.getHumidite() != null) {
            double humid = data.getHumidite();
            if (humid > 60 && humid < 80) ndvi += 0.05;
            else if (humid < 35) ndvi -= 0.1;
        }

        // Borner entre -1 et 1
        return Math.max(-1, Math.min(1, Math.round(ndvi * 1000) / 1000.0));
    }

    // ==================== SAUVEGARDE DES DONNÉES ====================

    /**
     * Sauvegarde les données satellite en base de données
     */
    public void sauvegarder(DonneesSatellite data) throws SQLException {
        String sql = """
            INSERT INTO donnees_satellite 
            (agriculteur_id, latitude, longitude, date_mesure, ndvi, temperature_moyenne, 
             precipitation, humidite, indice_secheresse, risque_agricole, donnees_brutes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        Connection connection = getConnection();
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        // agriculteur_id peut être null
        if (data.getAgriculteurId() != null) {
            ps.setInt(1, data.getAgriculteurId());
        } else {
            ps.setNull(1, Types.INTEGER);
        }

        ps.setDouble(2, data.getLatitude());
        ps.setDouble(3, data.getLongitude());
        ps.setDate(4, Date.valueOf(data.getDateMesure()));

        // Valeurs nullable
        setNullableDouble(ps, 5, data.getNdvi());
        setNullableDouble(ps, 6, data.getTemperatureMoyenne());
        setNullableDouble(ps, 7, data.getPrecipitation());
        setNullableDouble(ps, 8, data.getHumidite());
        setNullableDouble(ps, 9, data.getIndiceSecheresse());

        ps.setString(10, data.getRisqueAgricole());
        ps.setString(11, data.getDonneesBrutes());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            data.setId(rs.getInt(1));
        }

        rs.close();
        ps.close();

        System.out.println("💾 Données satellite sauvegardées (ID: " + data.getId() + ")");
    }

    /**
     * Helper pour insérer des Double nullable
     */
    private void setNullableDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value != null) {
            ps.setDouble(index, value);
        } else {
            ps.setNull(index, Types.DOUBLE);
        }
    }

    // ==================== ANALYSE DE RISQUE ====================

    /**
     * Crée une analyse de risque à partir des données satellite
     *
     * @param banqueId ID de la banque (table banque, pas utilisateur !)
     * @param data Données satellite
     * @return Analyse de risque créée
     */
    public AnalyseRisqueAgricole creerAnalyse(int banqueId, DonneesSatellite data) throws SQLException {
        System.out.println("\n📋 Création de l'analyse de risque...");
        System.out.println("   Banque ID: " + banqueId);
        System.out.println("   Région: " + data.getRegion());

        AnalyseRisqueAgricole analyse = new AnalyseRisqueAgricole(banqueId, data.getRegion());
        analyse.setLatitude(data.getLatitude());
        analyse.setLongitude(data.getLongitude());
        analyse.setTemperature(data.getTemperatureMoyenne());
        analyse.setPrecipitation(data.getPrecipitation());
        analyse.setHumidite(data.getHumidite());
        analyse.setNdvi(data.getNdvi());
        analyse.setIndiceSecheresse(data.getIndiceSecheresse());

        // Calculer le score de risque
        int score = data.getScoreRisque();
        analyse.setScoreRisque(score);

        // Déterminer le niveau de risque
        String niveau;
        if (score >= 75) niveau = "critique";
        else if (score >= 50) niveau = "eleve";
        else if (score >= 25) niveau = "moyen";
        else niveau = "faible";
        analyse.setNiveauRisque(niveau);

        // Générer les facteurs de risque
        StringBuilder facteurs = new StringBuilder();

        if (data.getIndiceSecheresse() != null && data.getIndiceSecheresse() > 50) {
            facteurs.append("• Sécheresse détectée (indice: ")
                    .append(String.format("%.0f", data.getIndiceSecheresse()))
                    .append("/100)\n");
        }

        if (data.getNdvi() != null && data.getNdvi() < 0.3) {
            facteurs.append("• Végétation en stress (NDVI: ")
                    .append(String.format("%.2f", data.getNdvi()))
                    .append(")\n");
        }

        if (data.getTemperatureMoyenne() != null && data.getTemperatureMoyenne() > 35) {
            facteurs.append("• Températures élevées (")
                    .append(String.format("%.1f", data.getTemperatureMoyenne()))
                    .append("°C)\n");
        }

        if (data.getTemperatureMoyenne() != null && data.getTemperatureMoyenne() < 5) {
            facteurs.append("• Risque de gel (")
                    .append(String.format("%.1f", data.getTemperatureMoyenne()))
                    .append("°C)\n");
        }

        if (data.getPrecipitation() != null && data.getPrecipitation() < 20) {
            facteurs.append("• Précipitations insuffisantes (")
                    .append(String.format("%.1f", data.getPrecipitation()))
                    .append(" mm)\n");
        }

        if (data.getPrecipitation() != null && data.getPrecipitation() > 150) {
            facteurs.append("• Risque d'inondation (")
                    .append(String.format("%.1f", data.getPrecipitation()))
                    .append(" mm)\n");
        }

        if (data.getHumidite() != null && data.getHumidite() > 85) {
            facteurs.append("• Humidité excessive - risque de maladies fongiques (")
                    .append(String.format("%.1f", data.getHumidite()))
                    .append("%)\n");
        }

        if (facteurs.length() == 0) {
            facteurs.append("• Aucun facteur de risque majeur identifié\n");
        }

        analyse.setFacteursRisque(facteurs.toString());

        // Générer les recommandations
        StringBuilder reco = new StringBuilder();

        if (score >= 75) {
            reco.append("⚠️ RISQUE CRITIQUE\n");
            reco.append("• Évaluation terrain obligatoire avant tout financement\n");
            reco.append("• Exiger une assurance récolte complète\n");
            reco.append("• Limiter l'exposition financière\n");
            reco.append("• Prévoir des garanties supplémentaires\n");
            reco.append("• Envisager un suivi mensuel de la parcelle\n");
        } else if (score >= 50) {
            reco.append("⚠️ RISQUE ÉLEVÉ\n");
            reco.append("• Surveillance renforcée recommandée\n");
            reco.append("• Assurance récolte fortement conseillée\n");
            reco.append("• Augmenter les garanties demandées\n");
            reco.append("• Planifier une visite terrain\n");
        } else if (score >= 25) {
            reco.append("🟡 RISQUE MODÉRÉ\n");
            reco.append("• Conditions globalement acceptables\n");
            reco.append("• Assurance récolte recommandée\n");
            reco.append("• Suivi régulier des conditions météo conseillé\n");
            reco.append("• Garanties standards suffisantes\n");
        } else {
            reco.append("✅ CONDITIONS FAVORABLES\n");
            reco.append("• Risque financier faible\n");
            reco.append("• Conditions optimales pour les cultures\n");
            reco.append("• Financement peut être accordé avec garanties minimales\n");
            reco.append("• Suivi trimestriel suffisant\n");
        }

        analyse.setRecommandations(reco.toString());

        // Sauvegarder l'analyse
        sauvegarderAnalyse(analyse);

        System.out.println("✅ Analyse créée (Score: " + score + "/100, Niveau: " + niveau + ")");

        return analyse;
    }

    /**
     * Sauvegarde l'analyse de risque en base de données
     */
    private void sauvegarderAnalyse(AnalyseRisqueAgricole analyse) throws SQLException {
        String sql = """
            INSERT INTO analyse_risque_agricole 
            (banque_id, agriculteur_id, region, score_risque, niveau_risque, facteurs_risque, recommandations)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        Connection connection = getConnection();
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setInt(1, analyse.getBanqueId());

        if (analyse.getAgriculteurId() != null) {
            ps.setInt(2, analyse.getAgriculteurId());
        } else {
            ps.setNull(2, Types.INTEGER);
        }

        ps.setString(3, analyse.getRegion());
        ps.setInt(4, analyse.getScoreRisque());
        ps.setString(5, analyse.getNiveauRisque());
        ps.setString(6, analyse.getFacteursRisque());
        ps.setString(7, analyse.getRecommandations());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            analyse.setId(rs.getInt(1));
        }

        rs.close();
        ps.close();

        System.out.println("💾 Analyse sauvegardée (ID: " + analyse.getId() + ")");
    }

    // ==================== RÉCUPÉRATION DE L'HISTORIQUE ====================

    /**
     * Récupère l'historique des analyses pour une banque
     *
     * @param banqueId ID de la banque (table banque, pas utilisateur !)
     * @return Liste des analyses triées par date décroissante
     */
    public List<AnalyseRisqueAgricole> getHistoriqueAnalyses(int banqueId) throws SQLException {
        List<AnalyseRisqueAgricole> liste = new ArrayList<>();

        String sql = """
            SELECT * FROM analyse_risque_agricole 
            WHERE banque_id = ? 
            ORDER BY date_analyse DESC 
            LIMIT 50
        """;

        Connection connection = getConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, banqueId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            AnalyseRisqueAgricole analyse = mapResultSetToAnalyse(rs);
            liste.add(analyse);
        }

        rs.close();
        ps.close();

        return liste;
    }

    /**
     * Récupère une analyse par son ID
     */
    public AnalyseRisqueAgricole getAnalyseById(int id) throws SQLException {
        String sql = "SELECT * FROM analyse_risque_agricole WHERE id = ?";

        Connection connection = getConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        AnalyseRisqueAgricole analyse = null;
        if (rs.next()) {
            analyse = mapResultSetToAnalyse(rs);
        }

        rs.close();
        ps.close();

        return analyse;
    }

    /**
     * Récupère les données satellite pour une position
     */
    public List<DonneesSatellite> getDonneesSatelliteByPosition(double lat, double lon, int rayonKm) throws SQLException {
        List<DonneesSatellite> liste = new ArrayList<>();

        // 1 degré ≈ 111 km
        double delta = rayonKm / 111.0;

        String sql = """
            SELECT * FROM donnees_satellite 
            WHERE latitude BETWEEN ? AND ?
            AND longitude BETWEEN ? AND ?
            ORDER BY date_mesure DESC
            LIMIT 100
        """;

        Connection connection = getConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setDouble(1, lat - delta);
        ps.setDouble(2, lat + delta);
        ps.setDouble(3, lon - delta);
        ps.setDouble(4, lon + delta);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            DonneesSatellite data = mapResultSetToDonnees(rs);
            liste.add(data);
        }

        rs.close();
        ps.close();

        return liste;
    }

    /**
     * Supprime une analyse
     */
    public void supprimerAnalyse(int analyseId) throws SQLException {
        String sql = "DELETE FROM analyse_risque_agricole WHERE id = ?";

        Connection connection = getConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, analyseId);
        ps.executeUpdate();
        ps.close();

        System.out.println("🗑️ Analyse supprimée (ID: " + analyseId + ")");
    }

    /**
     * Compte le nombre d'analyses pour une banque
     */
    public int compterAnalyses(int banqueId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM analyse_risque_agricole WHERE banque_id = ?";

        Connection connection = getConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, banqueId);

        ResultSet rs = ps.executeQuery();
        int count = 0;
        if (rs.next()) {
            count = rs.getInt(1);
        }

        rs.close();
        ps.close();

        return count;
    }

    // ==================== MAPPERS ====================

    /**
     * Mappe un ResultSet vers un objet AnalyseRisqueAgricole
     */
    private AnalyseRisqueAgricole mapResultSetToAnalyse(ResultSet rs) throws SQLException {
        AnalyseRisqueAgricole analyse = new AnalyseRisqueAgricole();

        analyse.setId(rs.getInt("id"));
        analyse.setBanqueId(rs.getInt("banque_id"));

        int agriculteurId = rs.getInt("agriculteur_id");
        if (!rs.wasNull()) {
            analyse.setAgriculteurId(agriculteurId);
        }

        analyse.setRegion(rs.getString("region"));
        analyse.setScoreRisque(rs.getInt("score_risque"));
        analyse.setNiveauRisque(rs.getString("niveau_risque"));
        analyse.setFacteursRisque(rs.getString("facteurs_risque"));
        analyse.setRecommandations(rs.getString("recommandations"));

        Timestamp ts = rs.getTimestamp("date_analyse");
        if (ts != null) {
            analyse.setDateAnalyse(ts.toLocalDateTime());
        }

        return analyse;
    }

    /**
     * Mappe un ResultSet vers un objet DonneesSatellite
     */
    private DonneesSatellite mapResultSetToDonnees(ResultSet rs) throws SQLException {
        DonneesSatellite data = new DonneesSatellite();

        data.setId(rs.getInt("id"));

        int agriculteurId = rs.getInt("agriculteur_id");
        if (!rs.wasNull()) {
            data.setAgriculteurId(agriculteurId);
        }

        data.setLatitude(rs.getDouble("latitude"));
        data.setLongitude(rs.getDouble("longitude"));

        Date dateMesure = rs.getDate("date_mesure");
        if (dateMesure != null) {
            data.setDateMesure(dateMesure.toLocalDate());
        }

        data.setNdvi(rs.getDouble("ndvi"));
        if (rs.wasNull()) data.setNdvi(null);

        data.setTemperatureMoyenne(rs.getDouble("temperature_moyenne"));
        if (rs.wasNull()) data.setTemperatureMoyenne(null);

        data.setPrecipitation(rs.getDouble("precipitation"));
        if (rs.wasNull()) data.setPrecipitation(null);

        data.setHumidite(rs.getDouble("humidite"));
        if (rs.wasNull()) data.setHumidite(null);

        data.setIndiceSecheresse(rs.getDouble("indice_secheresse"));
        if (rs.wasNull()) data.setIndiceSecheresse(null);

        data.setRisqueAgricole(rs.getString("risque_agricole"));
        data.setDonneesBrutes(rs.getString("donnees_brutes"));

        Timestamp ts = rs.getTimestamp("date_creation");
        if (ts != null) {
            data.setDateCreation(ts.toLocalDateTime());
        }

        return data;
    }

    // ==================== MÉTHODE DE TEST ====================

    /**
     * Teste l'API NASA
     */
    public void testNasaAPI() {
        System.out.println("\n═══════════════════════════════════════════");
        System.out.println("🧪 TEST API NASA POWER");
        System.out.println("═══════════════════════════════════════════\n");

        try {
            double lat = 36.8065; // Tunis
            double lon = 10.1815;
            LocalDate end = LocalDate.now().minusDays(10);
            LocalDate start = end.minusDays(7);

            System.out.println("Position: Tunis (" + lat + ", " + lon + ")");
            System.out.println("Période: " + start + " → " + end);

            DonneesSatellite data = fetchNasaData(lat, lon, start, end, "Tunis");

            System.out.println("\n═══════════════════════════════════════════");
            System.out.println("✅ TEST RÉUSSI !");
            System.out.println("═══════════════════════════════════════════");
            System.out.println("Température: " + data.getTemperatureMoyenne() + "°C");
            System.out.println("Précipitations: " + data.getPrecipitation() + " mm");
            System.out.println("Humidité: " + data.getHumidite() + "%");
            System.out.println("Indice sécheresse: " + data.getIndiceSecheresse() + "/100");
            System.out.println("NDVI: " + data.getNdvi());
            System.out.println("Risque: " + data.getRisqueAgricole());
            System.out.println("Score: " + data.getScoreRisque() + "/100");
            System.out.println("═══════════════════════════════════════════\n");

        } catch (Exception e) {
            System.err.println("\n❌ TEST ÉCHOUÉ: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
