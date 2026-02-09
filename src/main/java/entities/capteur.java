package entities;

import java.time.LocalDateTime;

public class capteur {

    private int idCapteur;
    private String typeCapteur;
    private String localisation;
    private String statut;
    private int idProjet;
    private LocalDateTime dateInstallation;

    // Constructeurs
    public capteur() {
    }

    public capteur(int idCapteur, String typeCapteur, String localisation, String statut, int idProjet) {
        this.idCapteur = idCapteur;
        this.typeCapteur = typeCapteur;
        this.localisation = localisation;
        this.statut = statut;
        this.idProjet = idProjet;
    }

    // Getters et Setters
    public int getIdCapteur() {
        return idCapteur;
    }

    public void setIdCapteur(int idCapteur) {
        this.idCapteur = idCapteur;
    }

    public String getTypeCapteur() {
        return typeCapteur;
    }

    public void setTypeCapteur(String typeCapteur) {
        this.typeCapteur = typeCapteur;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getIdProjet() {
        return idProjet;
    }

    public void setIdProjet(int idProjet) {
        this.idProjet = idProjet;
    }

    public LocalDateTime getDateInstallation() {
        return dateInstallation;
    }

    public void setDateInstallation(LocalDateTime dateInstallation) {
        this.dateInstallation = dateInstallation;
    }

    @Override
    public String toString() {
        return "Capteur{" +
                "id=" + idCapteur +
                ", type='" + typeCapteur + '\'' +
                ", localisation='" + localisation + '\'' +
                ", statut='" + statut + '\'' +
                ", projet=" + idProjet +
                '}';
    }
}