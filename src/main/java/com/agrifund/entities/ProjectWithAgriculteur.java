package com.agrifund.entities;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * DTO pour afficher un projet avec les informations de l'agriculteur
 */
public class ProjectWithAgriculteur {

    // Informations du projet
    private int idproject;
    private int agriculteurId;
    private String nomproject;
    private float surface;
    private BigDecimal budgetdemande;
    private String statut;
    private Date datesoumission;
    private Double latitude;
    private Double longitude;

    // Informations de l'agriculteur
    private String agriculteurNom;
    private String agriculteurPrenom;
    private String agriculteurEmail;
    private String agriculteurTel;
    private String adresseFerme;
    private String typeCulture;
    private BigDecimal superficieFerme;
    private boolean compteVerifie;

    // Constructeur vide
    public ProjectWithAgriculteur() {}

    // Constructeur complet
    public ProjectWithAgriculteur(int idproject, int agriculteurId, String nomproject, float surface,
                                  BigDecimal budgetdemande, String statut, Date datesoumission,
                                  Double latitude, Double longitude, String agriculteurNom,
                                  String agriculteurPrenom, String agriculteurEmail, String agriculteurTel,
                                  String adresseFerme, String typeCulture, BigDecimal superficieFerme,
                                  boolean compteVerifie) {
        this.idproject = idproject;
        this.agriculteurId = agriculteurId;
        this.nomproject = nomproject;
        this.surface = surface;
        this.budgetdemande = budgetdemande;
        this.statut = statut;
        this.datesoumission = datesoumission;
        this.latitude = latitude;
        this.longitude = longitude;
        this.agriculteurNom = agriculteurNom;
        this.agriculteurPrenom = agriculteurPrenom;
        this.agriculteurEmail = agriculteurEmail;
        this.agriculteurTel = agriculteurTel;
        this.adresseFerme = adresseFerme;
        this.typeCulture = typeCulture;
        this.superficieFerme = superficieFerme;
        this.compteVerifie = compteVerifie;
    }

    // Getters et Setters - Projet
    public int getIdproject() { return idproject; }
    public void setIdproject(int idproject) { this.idproject = idproject; }

    public int getAgriculteurId() { return agriculteurId; }
    public void setAgriculteurId(int agriculteurId) { this.agriculteurId = agriculteurId; }

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

    // Getters et Setters - Agriculteur
    public String getAgriculteurNom() { return agriculteurNom; }
    public void setAgriculteurNom(String agriculteurNom) { this.agriculteurNom = agriculteurNom; }

    public String getAgriculteurPrenom() { return agriculteurPrenom; }
    public void setAgriculteurPrenom(String agriculteurPrenom) { this.agriculteurPrenom = agriculteurPrenom; }

    public String getAgriculteurEmail() { return agriculteurEmail; }
    public void setAgriculteurEmail(String agriculteurEmail) { this.agriculteurEmail = agriculteurEmail; }

    public String getAgriculteurTel() { return agriculteurTel; }
    public void setAgriculteurTel(String agriculteurTel) { this.agriculteurTel = agriculteurTel; }

    public String getAdresseFerme() { return adresseFerme; }
    public void setAdresseFerme(String adresseFerme) { this.adresseFerme = adresseFerme; }

    public String getTypeCulture() { return typeCulture; }
    public void setTypeCulture(String typeCulture) { this.typeCulture = typeCulture; }

    public BigDecimal getSuperficieFerme() { return superficieFerme; }
    public void setSuperficieFerme(BigDecimal superficieFerme) { this.superficieFerme = superficieFerme; }

    public boolean isCompteVerifie() { return compteVerifie; }
    public void setCompteVerifie(boolean compteVerifie) { this.compteVerifie = compteVerifie; }

    // Méthodes utilitaires
    public String getAgriculteurNomComplet() {
        return agriculteurPrenom + " " + agriculteurNom;
    }

    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    @Override
    public String toString() {
        return "ProjectWithAgriculteur{" +
                "idproject=" + idproject +
                ", nomproject='" + nomproject + '\'' +
                ", agriculteur='" + getAgriculteurNomComplet() + '\'' +
                ", statut='" + statut + '\'' +
                ", budgetdemande=" + budgetdemande +
                '}';
    }
}