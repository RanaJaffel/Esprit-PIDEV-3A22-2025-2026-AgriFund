package model;

public class OffreFinanciere {
    private int idOffre;
    private String nomOffre;
    private String conditions;
    private String statut;
    private int idProduit;
    private ProduitFinancier produitFinancier;

    // Constructeurs
    public OffreFinanciere() {}

    public OffreFinanciere(String nomOffre, String conditions,
                           String statut, int idProduit) {
        this.nomOffre = nomOffre;
        this.conditions = conditions;
        this.statut = statut;
        this.idProduit = idProduit;
    }

    // Getters et Setters
    public int getIdOffre() {
        return idOffre;
    }

    public void setIdOffre(int idOffre) {
        this.idOffre = idOffre;
    }

    public String getNomOffre() {
        return nomOffre;
    }

    public void setNomOffre(String nomOffre) {
        this.nomOffre = nomOffre;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public ProduitFinancier getProduitFinancier() {
        return produitFinancier;
    }

    public void setProduitFinancier(ProduitFinancier produitFinancier) {
        this.produitFinancier = produitFinancier;
    }

    @Override
    public String toString() {
        return "OffreFinanciere{" +
                "idOffre=" + idOffre +
                ", nomOffre='" + nomOffre + '\'' +
                ", statut='" + statut + '\'' +
                ", idProduit=" + idProduit +
                '}';
    }
}