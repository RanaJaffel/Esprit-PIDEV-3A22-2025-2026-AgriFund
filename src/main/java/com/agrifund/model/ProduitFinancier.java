package com.agrifund.model;

public class ProduitFinancier {
    private int idProduit;
    private String nomProduit;
    private String typeFinancement;
    private double tauxInteret;
    private double prixFixe;
    private String reglesFinancieres;

    // NOUVEAU: Lien avec la banque
    private Integer banqueId;
    private String nomBanque; // Pour affichage

    // Constructeur vide
    public ProduitFinancier() {
    }

    // Constructeur complet
    public ProduitFinancier(int idProduit, String nomProduit, String typeFinancement,
                            double tauxInteret, double prixFixe,
                            String reglesFinancieres) {
        this.idProduit = idProduit;
        this.nomProduit = nomProduit;
        this.typeFinancement = typeFinancement;
        this.tauxInteret = tauxInteret;
        this.prixFixe = prixFixe;
        this.reglesFinancieres = reglesFinancieres;
    }

    // Getters et Setters existants...
    public int getIdProduit() { return idProduit; }
    public void setIdProduit(int idProduit) { this.idProduit = idProduit; }

    public String getNomProduit() { return nomProduit; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }

    public String getTypeFinancement() { return typeFinancement; }
    public void setTypeFinancement(String typeFinancement) { this.typeFinancement = typeFinancement; }

    public double getTauxInteret() { return tauxInteret; }
    public void setTauxInteret(double tauxInteret) { this.tauxInteret = tauxInteret; }

    public double getPrixFixe() { return prixFixe; }
    public void setPrixFixe(double prixFixe) { this.prixFixe = prixFixe; }

    public String getReglesFinancieres() { return reglesFinancieres; }
    public void setReglesFinancieres(String reglesFinancieres) { this.reglesFinancieres = reglesFinancieres; }

    // NOUVEAUX Getters/Setters
    public Integer getBanqueId() { return banqueId; }
    public void setBanqueId(Integer banqueId) { this.banqueId = banqueId; }

    public String getNomBanque() { return nomBanque; }
    public void setNomBanque(String nomBanque) { this.nomBanque = nomBanque; }

    @Override
    public String toString() {
        return nomProduit + " (" + typeFinancement + ")";
    }
}
