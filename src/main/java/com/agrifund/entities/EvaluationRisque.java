package com.agrifund.entities;

import java.util.Date;

public class EvaluationRisque {
    private int idEvaluation;
    private int scoreGlobal;
    private String niveauRisque;
    private String fiabiliteDonnees;
    private String facteurPrincipal;
    private int recommandation;
    private Date dateEvaluation;
    private int idProjet;
    private int banqueId; // ✅ NOUVEAU - ID de la banque qui a créé l'évaluation

    // Constructeurs
    public EvaluationRisque() {}

    public EvaluationRisque(int idEvaluation, int scoreGlobal, String niveauRisque,
                            String fiabiliteDonnees, String facteurPrincipal,
                            int recommandation, Date dateEvaluation, int idProjet, int banqueId) {
        this.idEvaluation = idEvaluation;
        this.scoreGlobal = scoreGlobal;
        this.niveauRisque = niveauRisque;
        this.fiabiliteDonnees = fiabiliteDonnees;
        this.facteurPrincipal = facteurPrincipal;
        this.recommandation = recommandation;
        this.dateEvaluation = dateEvaluation;
        this.idProjet = idProjet;
        this.banqueId = banqueId;
    }

    public EvaluationRisque(int scoreGlobal, String niveauRisque, String fiabiliteDonnees,
                            String facteurPrincipal, int recommandation, Date dateEvaluation,
                            int idProjet, int banqueId) {
        this.scoreGlobal = scoreGlobal;
        this.niveauRisque = niveauRisque;
        this.fiabiliteDonnees = fiabiliteDonnees;
        this.facteurPrincipal = facteurPrincipal;
        this.recommandation = recommandation;
        this.dateEvaluation = dateEvaluation;
        this.idProjet = idProjet;
        this.banqueId = banqueId;
    }

    public EvaluationRisque(int scoreGlobal, String niveauRisque, String fiabiliteDonnees, String facteurPrincipal, int recommandation, Date dateEvaluation, int idProjet) {
    }

    // Getters et Setters existants...
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

    public int getRecommandation() { return recommandation; }
    public void setRecommandation(int recommandation) { this.recommandation = recommandation; }

    public Date getDateEvaluation() { return dateEvaluation; }
    public void setDateEvaluation(Date dateEvaluation) { this.dateEvaluation = dateEvaluation; }

    public int getIdProjet() { return idProjet; }
    public void setIdProjet(int idProjet) { this.idProjet = idProjet; }

    // ✅ NOUVEAU
    public int getBanqueId() { return banqueId; }
    public void setBanqueId(int banqueId) { this.banqueId = banqueId; }

    @Override
    public String toString() {
        return "EvaluationRisque{" +
                "idEvaluation=" + idEvaluation +
                ", scoreGlobal=" + scoreGlobal +
                ", niveauRisque='" + niveauRisque + '\'' +
                ", banqueId=" + banqueId +
                ", idProjet=" + idProjet +
                '}';
    }
}