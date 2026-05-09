package com.agrifund.entities;

import java.time.LocalDateTime;

public class releve_terrain {

    private int idReleve;
    private String typeMesure;
    private double valeurMesuree;
    private String unite;
    private LocalDateTime dateHeure;
    private int idCapteur;


    public releve_terrain() {
    }

    public releve_terrain(int idReleve, String typeMesure, double valeurMesuree,
                          String unite, LocalDateTime dateHeure, int idCapteur) {
        this.idReleve = idReleve;
        this.typeMesure = typeMesure;
        this.valeurMesuree = valeurMesuree;
        this.unite = unite;
        this.dateHeure = dateHeure;
        this.idCapteur = idCapteur;
    }

    public releve_terrain(String typeMesure, double valeurMesuree, String unite, int idCapteur) {
        this.typeMesure = typeMesure;
        this.valeurMesuree = valeurMesuree;
        this.unite = unite;
        this.idCapteur = idCapteur;
        this.dateHeure = LocalDateTime.now();
    }


    public int getIdReleve() {
        return idReleve;
    }

    public void setIdReleve(int idReleve) {
        this.idReleve = idReleve;
    }

    public String getTypeMesure() {
        return typeMesure;
    }

    public void setTypeMesure(String typeMesure) {
        this.typeMesure = typeMesure;
    }

    public double getValeurMesuree() {
        return valeurMesuree;
    }

    public void setValeurMesuree(double valeurMesuree) {
        this.valeurMesuree = valeurMesuree;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public int getIdCapteur() {
        return idCapteur;
    }

    public void setIdCapteur(int idCapteur) {
        this.idCapteur = idCapteur;
    }

    @Override
    public String toString() {
        return "ReleveT{" +
                "id=" + idReleve +
                ", type='" + typeMesure + '\'' +
                ", valeur=" + valeurMesuree +
                ", unite='" + unite + '\'' +
                ", date=" + dateHeure +
                ", capteur=" + idCapteur +
                '}';
    }
}