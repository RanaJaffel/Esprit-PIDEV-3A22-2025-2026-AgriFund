package com.agrifund.entities;

import java.util.Date;

public class DecisionFinanciere {
    private int idDecision;
    private String statut;
    private String justification;
    private Date dateDecision;
    private int idEvaluation;
    private int banqueId; // ✅ NOUVEAU

    public DecisionFinanciere() {}

    public DecisionFinanciere(int idDecision, String statut, String justification,
                              Date dateDecision, int idEvaluation, int banqueId) {
        this.idDecision = idDecision;
        this.statut = statut;
        this.justification = justification;
        this.dateDecision = dateDecision;
        this.idEvaluation = idEvaluation;
        this.banqueId = banqueId;
    }

    public DecisionFinanciere(String statut, String justification, Date dateDecision,
                              int idEvaluation, int banqueId) {
        this.statut = statut;
        this.justification = justification;
        this.dateDecision = dateDecision;
        this.idEvaluation = idEvaluation;
        this.banqueId = banqueId;
    }

    public DecisionFinanciere(String statut, String justification, Date dateDecision, int idEvaluation) {
    }

    // Getters et Setters
    public int getIdDecision() { return idDecision; }
    public void setIdDecision(int idDecision) { this.idDecision = idDecision; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public Date getDateDecision() { return dateDecision; }
    public void setDateDecision(Date dateDecision) { this.dateDecision = dateDecision; }

    public int getIdEvaluation() { return idEvaluation; }
    public void setIdEvaluation(int idEvaluation) { this.idEvaluation = idEvaluation; }

    public int getBanqueId() { return banqueId; }
    public void setBanqueId(int banqueId) { this.banqueId = banqueId; }
}