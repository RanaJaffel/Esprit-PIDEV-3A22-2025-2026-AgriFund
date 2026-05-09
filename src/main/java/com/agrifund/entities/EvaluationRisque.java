package com.agrifund.entities;

import java.util.Date;

public class EvaluationRisque {
    private int idEvaluation;
    private int scoreGlobal;
    private String niveauRisque;
    private String fiabiliteDonnees;
    private String facteurPrincipal;
    private String recommandation;  // ✅ CHANGÉ: int → String
    private Date dateEvaluation;
    private int idProjet;
    private int banqueId;

    // Constructeurs
    public EvaluationRisque() {}

    public EvaluationRisque(int idEvaluation, int scoreGlobal, String niveauRisque,
                            String fiabiliteDonnees, String facteurPrincipal,
                            String recommandation, Date dateEvaluation, int idProjet, int banqueId) {
        this.idEvaluation = idEvaluation;
        this.scoreGlobal = scoreGlobal;
        this.niveauRisque = niveauRisque;
        this.fiabiliteDonnees = fiabiliteDonnees;
        this.facteurPrincipal = facteurPrincipal;
        this.recommandation = recommandation;  // ✅ String
        this.dateEvaluation = dateEvaluation;
        this.idProjet = idProjet;
        this.banqueId = banqueId;
    }

    public EvaluationRisque(int scoreGlobal, String niveauRisque, String fiabiliteDonnees,
                            String facteurPrincipal, String recommandation, Date dateEvaluation,
                            int idProjet, int banqueId) {
        this.scoreGlobal = scoreGlobal;
        this.niveauRisque = niveauRisque;
        this.fiabiliteDonnees = fiabiliteDonnees;
        this.facteurPrincipal = facteurPrincipal;
        this.recommandation = recommandation;  // ✅ String
        this.dateEvaluation = dateEvaluation;
        this.idProjet = idProjet;
        this.banqueId = banqueId;
    }

    public EvaluationRisque(int scoreGlobal, String niveauRisque, String fiabiliteDonnees,
                            String facteurPrincipal, String recommandation, Date dateEvaluation, int idProjet) {
        this.scoreGlobal = scoreGlobal;
        this.niveauRisque = niveauRisque;
        this.fiabiliteDonnees = fiabiliteDonnees;
        this.facteurPrincipal = facteurPrincipal;
        this.recommandation = recommandation;  // ✅ String
        this.dateEvaluation = dateEvaluation;
        this.idProjet = idProjet;
    }

    // Getters et Setters
    public int getIdEvaluation() { return idEvaluation; }
    public void setIdEvaluation(int idEvaluation) { this.idEvaluation = idEvaluation; }

    public int getScoreGlobal() { return scoreGlobal; }
    public void setScoreGlobal(int scoreGlobal) { this.scoreGlobal = scoreGlobal; }

    public String getNiveauRisque() { return niveauRisque; }
    public void setNiveauRisque(String niveauRisque) { this.niveauRisque = niveauRisque; }

    public String getFiabiliteDonnees() { return fiabiliteDonnees; }
    public void setFiabiliteDonnees(String fiabiliteDonnees) { this.fiabiliteDonnees = fiabiliteDonnees; }

    public String getFacteurPrincipal() { return facteurPrincipal; }
    public void setFacteurPrincipal(String facteurPrincipal) { this.facteurPrincipal = facteurPrincipal; }

    public String getRecommandation() { return recommandation; }  // ✅ String
    public void setRecommandation(String recommandation) { this.recommandation = recommandation; }  // ✅ String

    public Date getDateEvaluation() { return dateEvaluation; }
    public void setDateEvaluation(Date dateEvaluation) { this.dateEvaluation = dateEvaluation; }

    public int getIdProjet() { return idProjet; }
    public void setIdProjet(int idProjet) { this.idProjet = idProjet; }

    public int getBanqueId() { return banqueId; }
    public void setBanqueId(int banqueId) { this.banqueId = banqueId; }

    // Alias pour compatibilité PropertyValueFactory
    public String getRecommendation() { return getRecommandation(); }

    @Override
    public String toString() {
        return "EvaluationRisque{" +
                "idEvaluation=" + idEvaluation +
                ", scoreGlobal=" + scoreGlobal +
                ", niveauRisque='" + niveauRisque + '\'' +
                ", recommandation='" + recommandation + '\'' +
                ", banqueId=" + banqueId +
                ", idProjet=" + idProjet +
                '}';
    }
}