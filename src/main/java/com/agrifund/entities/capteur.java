package com.agrifund.entities;

import java.time.LocalDateTime;

public class capteur {

    private int idCapteur;
    private String typeCapteur;
    private String modele;
    private String localisation;
    private String statut;

    private Integer idProjet;
    private Integer idUser;


    private LocalDateTime dateInstallation;
    private LocalDateTime lastSeenAt;

    private Double latitude;
    private Double longitude;

    public capteur() {
    }

    public capteur(int idCapteur, String typeCapteur, String modele,
                   String localisation, String statut,
                   Integer idProjet, Integer idUser) {
        this.idCapteur = idCapteur;
        this.typeCapteur = typeCapteur;
        this.modele = modele;
        this.localisation = localisation;
        this.statut = statut;
        this.idProjet = idProjet;
        this.idUser = idUser;
    }

    public int getIdCapteur() {
        return idCapteur;
    }

    public void setIdCapteur(int idCapteur) {
        this.idCapteur = idCapteur;
    }

    public String getTypeCapteur() {
        return typeCapteur;
    }

    public void setTypeCapteur(String typeCapteur) {
        this.typeCapteur = typeCapteur;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Integer getIdProjet() {
        return idProjet;
    }

    public void setIdProjet(Integer idProjet) {
        this.idProjet = idProjet;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public LocalDateTime getDateInstallation() {
        return dateInstallation;
    }

    public void setDateInstallation(LocalDateTime dateInstallation) {
        this.dateInstallation = dateInstallation;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "capteur{" +
                "idCapteur=" + idCapteur +
                ", typeCapteur='" + typeCapteur + '\'' +
                ", modele='" + modele + '\'' +
                ", localisation='" + localisation + '\'' +
                ", statut='" + statut + '\'' +
                ", idProjet=" + idProjet +
                ", idUser=" + idUser +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
