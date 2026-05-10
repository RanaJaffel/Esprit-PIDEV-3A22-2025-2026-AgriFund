package com.agrifund.model;

public class OffreFinanciere {
    private int idOffre;
    private String nomOffre;
    private String conditions;
    private String statut;
    private int idProduit;
    private String nomProduit;
    private ProduitFinancier produitFinancier;

    // NOUVEAU: Lien avec la banque
    private Integer banqueId;
    private String nomBanque;

    public OffreFinanciere() {
    }

    public OffreFinanciere(int idOffre, String nomOffre, String conditions,
                           String statut, int idProduit) {
        this.idOffre = idOffre;
        this.nomOffre = nomOffre;
        this.conditions = conditions;
        this.statut = statut;
        this.idProduit = idProduit;
    }

    // Getters et Setters existants...
    public int getIdOffre() { return idOffre; }
    public void setIdOffre(int idOffre) { this.idOffre = idOffre; }

    public String getNomOffre() { return nomOffre; }
    public void setNomOffre(String nomOffre) { this.nomOffre = nomOffre; }

    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getIdProduit() { return idProduit; }
    public void setIdProduit(int idProduit) { this.idProduit = idProduit; }

    public String getNomProduit() { return nomProduit; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }

    public ProduitFinancier getProduitFinancier() { return produitFinancier; }
    public void setProduitFinancier(ProduitFinancier produitFinancier) {
        this.produitFinancier = produitFinancier;
        if (produitFinancier != null) {
            this.nomProduit = produitFinancier.getNomProduit();
        }
    }

    // NOUVEAUX Getters/Setters
    public Integer getBanqueId() { return banqueId; }
    public void setBanqueId(Integer banqueId) { this.banqueId = banqueId; }

    public String getNomBanque() { return nomBanque; }
    public void setNomBanque(String nomBanque) { this.nomBanque = nomBanque; }

    @Override
    public String toString() {
        return nomOffre + " (" + statut + ")";
    }
}
