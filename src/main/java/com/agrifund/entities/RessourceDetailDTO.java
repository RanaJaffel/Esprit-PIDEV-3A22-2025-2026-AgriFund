package com.agrifund.entities;

import java.math.BigDecimal;
import java.sql.Date;

public class RessourceDetailDTO {
    // Infos Ressource
    private int idressource;
    private String nomressource;
    private String typeressource;
    private int quantite;
    private BigDecimal cout;
    private String fournisseur;
    private String statut;
    private Date dateajout;

    // Infos Projet
    private int idproject;
    private String nomproject;
    private float surface;
    private BigDecimal budgetdemande;
    private String statutProjet;

    // Infos Agriculteur
    private int agriculteurId;
    private String nomAgriculteur;
    private String prenomAgriculteur;
    private String emailAgriculteur;
    private String telAgriculteur;
    private String adresseFerme;
    private String typeCulture;

    public RessourceDetailDTO() {}

    // Getters et Setters - Ressource
    public int getIdressource() { return idressource; }
    public void setIdressource(int idressource) { this.idressource = idressource; }

    public String getNomressource() { return nomressource; }
    public void setNomressource(String nomressource) { this.nomressource = nomressource; }

    public String getTyperessource() { return typeressource; }
    public void setTyperessource(String typeressource) { this.typeressource = typeressource; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public BigDecimal getCout() { return cout; }
    public void setCout(BigDecimal cout) { this.cout = cout; }

    public String getFournisseur() { return fournisseur; }
    public void setFournisseur(String fournisseur) { this.fournisseur = fournisseur; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Date getDateajout() { return dateajout; }
    public void setDateajout(Date dateajout) { this.dateajout = dateajout; }

    // Getters et Setters - Projet
    public int getIdproject() { return idproject; }
    public void setIdproject(int idproject) { this.idproject = idproject; }

    public String getNomproject() { return nomproject; }
    public void setNomproject(String nomproject) { this.nomproject = nomproject; }

    public float getSurface() { return surface; }
    public void setSurface(float surface) { this.surface = surface; }

    public BigDecimal getBudgetdemande() { return budgetdemande; }
    public void setBudgetdemande(BigDecimal budgetdemande) { this.budgetdemande = budgetdemande; }

    public String getStatutProjet() { return statutProjet; }
    public void setStatutProjet(String statutProjet) { this.statutProjet = statutProjet; }

    // Getters et Setters - Agriculteur
    public int getAgriculteurId() { return agriculteurId; }
    public void setAgriculteurId(int agriculteurId) { this.agriculteurId = agriculteurId; }

    public String getNomAgriculteur() { return nomAgriculteur; }
    public void setNomAgriculteur(String nomAgriculteur) { this.nomAgriculteur = nomAgriculteur; }

    public String getPrenomAgriculteur() { return prenomAgriculteur; }
    public void setPrenomAgriculteur(String prenomAgriculteur) { this.prenomAgriculteur = prenomAgriculteur; }

    public String getEmailAgriculteur() { return emailAgriculteur; }
    public void setEmailAgriculteur(String emailAgriculteur) { this.emailAgriculteur = emailAgriculteur; }

    public String getTelAgriculteur() { return telAgriculteur; }
    public void setTelAgriculteur(String telAgriculteur) { this.telAgriculteur = telAgriculteur; }

    public String getAdresseFerme() { return adresseFerme; }
    public void setAdresseFerme(String adresseFerme) { this.adresseFerme = adresseFerme; }

    public String getTypeCulture() { return typeCulture; }
    public void setTypeCulture(String typeCulture) { this.typeCulture = typeCulture; }

    // Méthodes utilitaires
    public String getNomCompletAgriculteur() {
        return (prenomAgriculteur != null ? prenomAgriculteur : "") + " " +
                (nomAgriculteur != null ? nomAgriculteur : "");
    }

    public BigDecimal getCoutTotal() {
        if (cout == null) return BigDecimal.ZERO;
        return cout.multiply(new BigDecimal(quantite));
    }
}