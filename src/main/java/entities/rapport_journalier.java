package entities;

import java.time.LocalDate;

public class rapport_journalier {

    private int idRapport;
    private LocalDate dateRapport;
    private String typeMesure;
    private double moyenne;
    private double min;
    private double max;
    private int idCapteur;

    public rapport_journalier() {}

    public rapport_journalier(LocalDate dateRapport, String typeMesure,
                              double moyenne, double min, double max, int idCapteur) {
        this.dateRapport = dateRapport;
        this.typeMesure = typeMesure;
        this.moyenne = moyenne;
        this.min = min;
        this.max = max;
        this.idCapteur = idCapteur;
    }

    public int getIdRapport() {
        return idRapport;
    }

    public void setIdRapport(int idRapport) {
        this.idRapport = idRapport;
    }

    public LocalDate getDateRapport() {
        return dateRapport;
    }

    public void setDateRapport(LocalDate dateRapport) {
        this.dateRapport = dateRapport;
    }

    public String getTypeMesure() {
        return typeMesure;
    }

    public void setTypeMesure(String typeMesure) {
        this.typeMesure = typeMesure;
    }

    public double getMoyenne() {
        return moyenne;
    }

    public void setMoyenne(double moyenne) {
        this.moyenne = moyenne;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public int getIdCapteur() {
        return idCapteur;
    }

    public void setIdCapteur(int idCapteur) {
        this.idCapteur = idCapteur;
    }

    @Override
    public String toString() {
        return "rapport_journalier{" +
                "idRapport=" + idRapport +
                ", dateRapport=" + dateRapport +
                ", typeMesure='" + typeMesure + '\'' +
                ", moyenne=" + moyenne +
                ", min=" + min +
                ", max=" + max +
                ", idCapteur=" + idCapteur +
                '}';
    }
}