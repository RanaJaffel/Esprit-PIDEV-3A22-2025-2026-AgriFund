package com.agrifund.entities;

import java.math.BigDecimal;

/**
 * Entité Agriculteur - hérite de Utilisateur
 * Correspond à la table Agriculteur dans la base de données
 */
public class Agriculteur extends Utilisateur {

    private int agriculteurId; // ID dans la table Agriculteur
    private int utilisateurId; // Référence vers la table Utilisateur
    private String adresseFerme;
    private BigDecimal superficieFerme;
    private String typeCulture;
    private String statusCompte;
    private boolean compteVerifie;

    // Constructeurs
    public Agriculteur() {
        super();
        this.statusCompte = "en_attente";
        this.compteVerifie = false;
    }

    public Agriculteur(String nom, String prenom, String email, String password,
                       String adresseFerme, BigDecimal superficieFerme, String typeCulture) {
        super(nom, prenom, email, password);
        this.adresseFerme = adresseFerme;
        this.superficieFerme = superficieFerme;
        this.typeCulture = typeCulture;
        this.statusCompte = "en_attente";
        this.compteVerifie = false;
    }

    // Getters et Setters
    public int getAgriculteurId() {
        return agriculteurId;
    }

    public void setAgriculteurId(int agriculteurId) {
        this.agriculteurId = agriculteurId;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public String getAdresseFerme() {
        return adresseFerme;
    }

    public void setAdresseFerme(String adresseFerme) {
        this.adresseFerme = adresseFerme;
    }

    public BigDecimal getSuperficieFerme() {
        return superficieFerme;
    }

    public void setSuperficieFerme(BigDecimal superficieFerme) {
        this.superficieFerme = superficieFerme;
    }

    public String getTypeCulture() {
        return typeCulture;
    }

    public void setTypeCulture(String typeCulture) {
        this.typeCulture = typeCulture;
    }

    public String getStatusCompte() {
        return statusCompte;
    }

    public void setStatusCompte(String statusCompte) {
        this.statusCompte = statusCompte;
    }

    public boolean isCompteVerifie() {
        return compteVerifie;
    }

    public void setCompteVerifie(boolean compteVerifie) {
        this.compteVerifie = compteVerifie;
    }

    @Override
    public String toString() {
        return "Agriculteur{" +
                "agriculteurId=" + agriculteurId +
                ", adresseFerme='" + adresseFerme + '\'' +
                ", superficieFerme=" + superficieFerme +
                ", typeCulture='" + typeCulture + '\'' +
                ", statusCompte='" + statusCompte + '\'' +
                ", compteVerifie=" + compteVerifie +
                ", " + super.toString() +
                '}';
    }
}