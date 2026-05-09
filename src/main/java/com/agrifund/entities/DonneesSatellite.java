package com.agrifund.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DonneesSatellite {

    private int id;
    private Integer agriculteurId;
    private double latitude;
    private double longitude;
    private LocalDate dateMesure;
    private Double ndvi;
    private Double temperatureMoyenne;
    private Double precipitation;
    private Double humidite;
    private Double indiceSecheresse;
    private String risqueAgricole;
    private String donneesBrutes;
    private LocalDateTime dateCreation;
    private String region;

    public DonneesSatellite() {
        this.dateCreation = LocalDateTime.now();
    }

    public DonneesSatellite(double latitude, double longitude, LocalDate dateMesure) {
        this();
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateMesure = dateMesure;
    }

    public String calculerRisque() {
        double score = 0;
        int facteurs = 0;

        if (ndvi != null) {
            if (ndvi < 0.2) score += 40;
            else if (ndvi < 0.3) score += 25;
            else if (ndvi < 0.5) score += 10;
            facteurs++;
        }

        if (precipitation != null) {
            if (precipitation < 10) score += 35;
            else if (precipitation > 200) score += 25;
            else if (precipitation < 30) score += 15;
            facteurs++;
        }

        if (temperatureMoyenne != null) {
            if (temperatureMoyenne > 40 || temperatureMoyenne < 0) score += 30;
            else if (temperatureMoyenne > 35 || temperatureMoyenne < 5) score += 20;
            facteurs++;
        }

        if (indiceSecheresse != null) {
            if (indiceSecheresse > 70) score += 35;
            else if (indiceSecheresse > 50) score += 20;
            else if (indiceSecheresse > 30) score += 10;
            facteurs++;
        }

        if (facteurs == 0) return "inconnu";

        double moyenne = score / facteurs;

        if (moyenne >= 30) return "critique";
        if (moyenne >= 20) return "eleve";
        if (moyenne >= 10) return "moyen";
        return "faible";
    }

    public int getScoreRisque() {
        int score = 0;

        if (indiceSecheresse != null) {
            score += (int) (indiceSecheresse * 0.4);
        }

        if (ndvi != null) {
            score += (int) ((1 - Math.max(0, ndvi)) * 30);
        }

        if (temperatureMoyenne != null) {
            if (temperatureMoyenne > 40) score += 20;
            else if (temperatureMoyenne > 35) score += 10;
            else if (temperatureMoyenne < 5) score += 15;
        }

        if (precipitation != null && precipitation < 10) {
            score += 15;
        }

        return Math.min(100, Math.max(0, score));
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getAgriculteurId() { return agriculteurId; }
    public void setAgriculteurId(Integer agriculteurId) { this.agriculteurId = agriculteurId; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public LocalDate getDateMesure() { return dateMesure; }
    public void setDateMesure(LocalDate dateMesure) { this.dateMesure = dateMesure; }

    public Double getNdvi() { return ndvi; }
    public void setNdvi(Double ndvi) { this.ndvi = ndvi; }

    public Double getTemperatureMoyenne() { return temperatureMoyenne; }
    public void setTemperatureMoyenne(Double temperatureMoyenne) { this.temperatureMoyenne = temperatureMoyenne; }

    public Double getPrecipitation() { return precipitation; }
    public void setPrecipitation(Double precipitation) { this.precipitation = precipitation; }

    public Double getHumidite() { return humidite; }
    public void setHumidite(Double humidite) { this.humidite = humidite; }

    public Double getIndiceSecheresse() { return indiceSecheresse; }
    public void setIndiceSecheresse(Double indiceSecheresse) { this.indiceSecheresse = indiceSecheresse; }

    public String getRisqueAgricole() { return risqueAgricole; }
    public void setRisqueAgricole(String risqueAgricole) { this.risqueAgricole = risqueAgricole; }

    public String getDonneesBrutes() { return donneesBrutes; }
    public void setDonneesBrutes(String donneesBrutes) { this.donneesBrutes = donneesBrutes; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}