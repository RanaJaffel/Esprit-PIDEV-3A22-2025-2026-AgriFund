package model;

public class ProduitFinancier {
    private int idProduit;
    private String nomProduit;
    private String typeFinancement;
    private double tauxInteret;
    private double montantMin;
    private double montantMax;
    private String reglesFinancieres;

    // Constructeurs
    public ProduitFinancier() {}

    public ProduitFinancier(String nomProduit, String typeFinancement,
                            double tauxInteret, double montantMin,
                            double montantMax, String reglesFinancieres) {
        this.nomProduit = nomProduit;
        this.typeFinancement = typeFinancement;
        this.tauxInteret = tauxInteret;
        this.montantMin = montantMin;
        this.montantMax = montantMax;
        this.reglesFinancieres = reglesFinancieres;
    }

    // Getters et Setters
    public int getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public String getTypeFinancement() {
        return typeFinancement;
    }

    public void setTypeFinancement(String typeFinancement) {
        this.typeFinancement = typeFinancement;
    }

    public double getTauxInteret() {
        return tauxInteret;
    }

    public void setTauxInteret(double tauxInteret) {
        this.tauxInteret = tauxInteret;
    }

    public double getMontantMin() {
        return montantMin;
    }

    public void setMontantMin(double montantMin) {
        this.montantMin = montantMin;
    }

    public double getMontantMax() {
        return montantMax;
    }

    public void setMontantMax(double montantMax) {
        this.montantMax = montantMax;
    }

    public String getReglesFinancieres() {
        return reglesFinancieres;
    }

    public void setReglesFinancieres(String reglesFinancieres) {
        this.reglesFinancieres = reglesFinancieres;
    }

    @Override
    public String toString() {
        return "ProduitFinancier{" +
                "idProduit=" + idProduit +
                ", nomProduit='" + nomProduit + '\'' +
                ", typeFinancement='" + typeFinancement + '\'' +
                ", tauxInteret=" + tauxInteret +
                ", montantMin=" + montantMin +
                ", montantMax=" + montantMax +
                '}';
    }
}