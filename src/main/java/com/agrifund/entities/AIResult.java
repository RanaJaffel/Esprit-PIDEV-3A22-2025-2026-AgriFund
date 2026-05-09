package com.agrifund.entities;

public class AIResult {
    private int scoreGlobal;
    private String niveauRisque;
    private String fiabiliteDonnees;
    private String facteurPrincipal;
    private String recommandation;

    // Constructeur, getters et setters
    public AIResult(int scoreGlobal, String niveauRisque, String fiabiliteDonnees, String facteurPrincipal, String recommandation) {
        this.scoreGlobal = scoreGlobal;
        this.niveauRisque = niveauRisque;
        this.fiabiliteDonnees = fiabiliteDonnees;
        this.facteurPrincipal = facteurPrincipal;
        this.recommandation = recommandation;
    }

    // Getters
    public int getScoreGlobal() { return scoreGlobal; }
    public String getNiveauRisque() { return niveauRisque; }
    public String getFiabiliteDonnees() { return fiabiliteDonnees; }
    public String getFacteurPrincipal() { return facteurPrincipal; }
    public String getRecommandation() { return recommandation; }
}