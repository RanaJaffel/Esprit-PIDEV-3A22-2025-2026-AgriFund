package com.agrifund.entities;

public class MeteoData {

    private double temperature;
    private double ressentie;
    private int humidite;
    private double vent;
    private String description;
    private String icon;

    public MeteoData(double temperature, double ressentie,
                     int humidite, double vent,
                     String description, String icon) {

        this.temperature = temperature;
        this.ressentie = ressentie;
        this.humidite = humidite;
        this.vent = vent;
        this.description = description;
        this.icon = icon;
    }

    public double getTemperature() { return temperature; }
    public double getRessentie() { return ressentie; }
    public int getHumidite() { return humidite; }
    public double getVent() { return vent; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
}
