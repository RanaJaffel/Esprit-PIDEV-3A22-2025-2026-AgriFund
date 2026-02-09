package entities;

import java.util.Date;


public class DecisionFinanciere {

    private int idDecision;
    private String statut;
    private String justification;
    private Date dateDecision;
    private int idProjet;


    public DecisionFinanciere() {
    }


    public DecisionFinanciere(String statut, String justification, Date dateDecision, int idProjet) {
        this.statut = statut;
        this.justification = justification;
        this.dateDecision = dateDecision;
        this.idProjet = idProjet;
    }


    public DecisionFinanciere(int idDecision, String statut, String justification, Date dateDecision, int idProjet) {
        this.idDecision = idDecision;
        this.statut = statut;
        this.justification = justification;
        this.dateDecision = dateDecision;
        this.idProjet = idProjet;
    }

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

    public int getIdProjet() {
        return idProjet;
    }

    public void setIdProjet(int idProjet) {
        this.idProjet = idProjet;
    }


    @Override
    public String toString() {
        return "DecisionFinanciere{" +
                "idDecision=" + idDecision +
                ", statut='" + statut + '\'' +
                ", justification='" + justification + '\'' +
                ", dateDecision=" + dateDecision +
                ", idProjet=" + idProjet +
                '}';
    }
}