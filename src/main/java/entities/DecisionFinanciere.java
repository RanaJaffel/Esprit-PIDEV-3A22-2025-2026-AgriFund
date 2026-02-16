package entities;

import java.util.Date;

public class DecisionFinanciere {
    private int idDecision;
    private String statut;
    private String justification;
    private Date dateDecision;
    private int idEvaluation;

    // Constructeurs
    public DecisionFinanciere() {}

    public DecisionFinanciere(int idDecision, String statut, String justification, Date dateDecision, int idEvaluation) {
        this.idDecision = idDecision;
        this.statut = statut;
        this.justification = justification;
        this.dateDecision = dateDecision;
        this.idEvaluation = idEvaluation;
    }

    public DecisionFinanciere(String statut, String justification, Date dateDecision, int idEvaluation) {
        this.statut = statut;
        this.justification = justification;
        this.dateDecision = dateDecision;
        this.idEvaluation = idEvaluation;
    }

    // Getters et Setters
    public int getIdDecision() {
        return idDecision;
    }

    public void setIdDecision(int idDecision) {
        this.idDecision = idDecision;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public Date getDateDecision() {
        return dateDecision;
    }

    public void setDateDecision(Date dateDecision) {
        this.dateDecision = dateDecision;
    }

    public int getIdEvaluation() {
        return idEvaluation;
    }

    public void setIdEvaluation(int idEvaluation) {
        this.idEvaluation = idEvaluation;
    }

    @Override
    public String toString() {
        return "DecisionFinanciere{" +
                "idDecision=" + idDecision +
                ", statut='" + statut + '\'' +
                ", justification='" + justification + '\'' +
                ", dateDecision=" + dateDecision +
                ", idEvaluation=" + idEvaluation +
                '}';
    }
}
