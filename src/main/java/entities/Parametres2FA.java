package entities;

import java.time.LocalDateTime;

/**
 * Entité Parametres2FA
 * Représente les paramètres de sécurité 2FA d'un utilisateur
 */
public class Parametres2FA {

    private int id;
    private int utilisateurId;
    private boolean estActive;
    private String methodePreferee; // email, sms, desactive
    private String telephone2fa;
    private LocalDateTime dateActivation;

    // Constructeurs
    public Parametres2FA() {
        this.estActive = false;
        this.methodePreferee = "email";
    }

    public Parametres2FA(int utilisateurId) {
        this.utilisateurId = utilisateurId;
        this.estActive = false;
        this.methodePreferee = "email";
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public boolean isEstActive() {
        return estActive;
    }

    public void setEstActive(boolean estActive) {
        this.estActive = estActive;
    }

    public String getMethodePreferee() {
        return methodePreferee;
    }

    public void setMethodePreferee(String methodePreferee) {
        this.methodePreferee = methodePreferee;
    }

    public String getTelephone2fa() {
        return telephone2fa;
    }

    public void setTelephone2fa(String telephone2fa) {
        this.telephone2fa = telephone2fa;
    }

    public LocalDateTime getDateActivation() {
        return dateActivation;
    }

    public void setDateActivation(LocalDateTime dateActivation) {
        this.dateActivation = dateActivation;
    }

    @Override
    public String toString() {
        return "Parametres2FA{" +
                "id=" + id +
                ", utilisateurId=" + utilisateurId +
                ", estActive=" + estActive +
                ", methodePreferee='" + methodePreferee + '\'' +
                '}';
    }
}