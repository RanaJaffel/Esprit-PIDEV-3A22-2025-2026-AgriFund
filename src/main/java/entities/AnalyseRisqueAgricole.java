package entities;

import java.time.LocalDateTime;

public class AnalyseRisqueAgricole {

    private int id;
    private int banqueId;
    private Integer agriculteurId;
    private String region;
    private double latitude;
    private double longitude;
    private int scoreRisque;
    private String niveauRisque;
    private String facteursRisque;
    private String recommandations;
    private LocalDateTime dateAnalyse;

    // Données climatiques associées
    private Double temperature;
    private Double precipitation;
    private Double humidite;
    private Double ndvi;
    private Double indiceSecheresse;

    public AnalyseRisqueAgricole() {
        this.dateAnalyse = LocalDateTime.now();
    }

    public AnalyseRisqueAgricole(int banqueId, String region) {
        this();
        this.banqueId = banqueId;
        this.region = region;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBanqueId() { return banqueId; }
    public void setBanqueId(int banqueId) { this.banqueId = banqueId; }

    public Integer getAgriculteurId() { return agriculteurId; }
    public void setAgriculteurId(Integer agriculteurId) { this.agriculteurId = agriculteurId; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getScoreRisque() { return scoreRisque; }
    public void setScoreRisque(int scoreRisque) { this.scoreRisque = scoreRisque; }

    public String getNiveauRisque() { return niveauRisque; }
    public void setNiveauRisque(String niveauRisque) { this.niveauRisque = niveauRisque; }

    public String getFacteursRisque() { return facteursRisque; }
    public void setFacteursRisque(String facteursRisque) { this.facteursRisque = facteursRisque; }

    public String getRecommandations() { return recommandations; }
    public void setRecommandations(String recommandations) { this.recommandations = recommandations; }

    public LocalDateTime getDateAnalyse() { return dateAnalyse; }
    public void setDateAnalyse(LocalDateTime dateAnalyse) { this.dateAnalyse = dateAnalyse; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getPrecipitation() { return precipitation; }
    public void setPrecipitation(Double precipitation) { this.precipitation = precipitation; }

    public Double getHumidite() { return humidite; }
    public void setHumidite(Double humidite) { this.humidite = humidite; }

    public Double getNdvi() { return ndvi; }
    public void setNdvi(Double ndvi) { this.ndvi = ndvi; }

    public Double getIndiceSecheresse() { return indiceSecheresse; }
    public void setIndiceSecheresse(Double indiceSecheresse) { this.indiceSecheresse = indiceSecheresse; }
}