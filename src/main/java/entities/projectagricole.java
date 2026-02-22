package entities;

import java.math.BigDecimal;
import java.sql.Date;

public class projectagricole {
    private int idproject;
    private String nomproject;
    private float surface;
    private BigDecimal budgetdemande;
    private String statut;
    private Date datesoumission;
    private Double latitude;
    private Double longitude;

    public projectagricole() {}

    public projectagricole(String nomproject, float surface, BigDecimal budgetdemande, String statut, Date datesoumission) {
        this.nomproject = nomproject;
        this.surface = surface;
        this.budgetdemande = budgetdemande;
        this.statut = statut;
        this.datesoumission = datesoumission;
    }

    public projectagricole(int idproject, String nomproject, float surface, BigDecimal budgetdemande, String statut, Date datesoumission) {
        this.idproject = idproject;
        this.nomproject = nomproject;
        this.surface = surface;
        this.budgetdemande = budgetdemande;
        this.statut = statut;
        this.datesoumission = datesoumission;
    }

    public projectagricole(int idproject, String nomproject, float surface, BigDecimal budgetdemande, String statut, Date datesoumission, Double latitude, Double longitude) {
        this.idproject = idproject;
        this.nomproject = nomproject;
        this.surface = surface;
        this.budgetdemande = budgetdemande;
        this.statut = statut;
        this.datesoumission = datesoumission;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getIdproject() { return idproject; }
    public void setIdproject(int idproject) { this.idproject = idproject; }

    public String getNomproject() { return nomproject; }
    public void setNomproject(String nomproject) { this.nomproject = nomproject; }

    public float getSurface() { return surface; }
    public void setSurface(float surface) { this.surface = surface; }

    public BigDecimal getBudgetdemande() { return budgetdemande; }
    public void setBudgetdemande(BigDecimal budgetdemande) { this.budgetdemande = budgetdemande; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Date getDatesoumission() { return datesoumission; }
    public void setDatesoumission(Date datesoumission) { this.datesoumission = datesoumission; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    @Override
    public String toString() {
        return "projectagricole{" +
                "idproject=" + idproject +
                ", nomproject='" + nomproject + '\'' +
                ", surface=" + surface +
                ", budgetdemande=" + budgetdemande +
                ", statut='" + statut + '\'' +
                ", datesoumission=" + datesoumission +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
