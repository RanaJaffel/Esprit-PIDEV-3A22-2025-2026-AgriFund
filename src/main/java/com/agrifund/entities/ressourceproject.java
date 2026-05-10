package com.agrifund.entities;

import java.math.BigDecimal;
import java.sql.Date;

public class ressourceproject {
    private int idressource;
    private String nomressource;
    private String typeressource;
    private int quantite;
    private BigDecimal cout;
    private String fournisseur;
    private String statut;
    private Date dateajout;
    private int idproject;

    public ressourceproject() {}

    public ressourceproject(String nomressource, String typeressource, int quantite, BigDecimal cout,
                            String fournisseur, String statut, Date dateajout, int idproject) {
        this.nomressource = nomressource;
        this.typeressource = typeressource;
        this.quantite = quantite;
        this.cout = cout;
        this.fournisseur = fournisseur;
        this.statut = statut;
        this.dateajout = dateajout;
        this.idproject = idproject;
    }


    public ressourceproject(int idressource, String nomressource, String typeressource, int quantite, BigDecimal cout,
                            String fournisseur, String statut, Date dateajout, int idproject) {
        this.idressource = idressource;
        this.nomressource = nomressource;
        this.typeressource = typeressource;
        this.quantite = quantite;
        this.cout = cout;
        this.fournisseur = fournisseur;
        this.statut = statut;
        this.dateajout = dateajout;
        this.idproject = idproject;
    }

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

    public int getIdproject() { return idproject; }
    public void setIdproject(int idproject) { this.idproject = idproject; }

    @Override
    public String toString() {
        return "ressourceproject{" +
                "idressource=" + idressource +
                ", nomressource='" + nomressource + '\'' +
                ", typeressource='" + typeressource + '\'' +
                ", quantite=" + quantite +
                ", cout=" + cout +
                ", fournisseur='" + fournisseur + '\'' +
                ", statut='" + statut + '\'' +
                ", dateajout=" + dateajout +
                ", idproject=" + idproject +
                '}';
    }
}
