package com.agrifund.entities;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Utilisateur {

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String tel;
    private LocalDateTime dateInscrit;
    private String photo;
    private LocalDateTime derniereConnexion;
    private boolean estEnLigne;

    // Constructeurs
    public Utilisateur() {
        this.dateInscrit = LocalDateTime.now();
    }

    public Utilisateur(String nom, String prenom, String email, String password) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.dateInscrit = LocalDateTime.now();
    }

    // Getters et Setters existants...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }

    public LocalDateTime getDateInscrit() { return dateInscrit; }
    public void setDateInscrit(LocalDateTime dateInscrit) { this.dateInscrit = dateInscrit; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    // Nouveaux getters/setters pour le statut en ligne
    public LocalDateTime getDerniereConnexion() { return derniereConnexion; }
    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    public boolean isEstEnLigne() { return estEnLigne; }
    public void setEstEnLigne(boolean estEnLigne) { this.estEnLigne = estEnLigne; }

    /**
     * Obtenir le statut formaté (En ligne, Vu il y a X minutes, etc.)
     */
    public String getStatutEnLigne() {
        if (estEnLigne) {
            return "🟢 En ligne";
        }

        if (derniereConnexion == null) {
            return "⚪ Hors ligne";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(derniereConnexion, now);
        long heures = ChronoUnit.HOURS.between(derniereConnexion, now);
        long jours = ChronoUnit.DAYS.between(derniereConnexion, now);

        if (minutes < 1) {
            return "🟢 En ligne";
        } else if (minutes < 60) {
            return "🕐 Vu il y a " + minutes + " min";
        } else if (heures < 24) {
            return "🕐 Vu il y a " + heures + "h";
        } else if (jours < 7) {
            return "🕐 Vu il y a " + jours + " jour" + (jours > 1 ? "s" : "");
        } else {
            return "⚪ Vu le " + derniereConnexion.format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    /**
     * Vérifier si l'utilisateur est considéré comme en ligne (actif < 5 min)
     */
    public boolean estRecentementActif() {
        if (derniereConnexion == null) return false;
        long minutes = ChronoUnit.MINUTES.between(derniereConnexion, LocalDateTime.now());
        return minutes < 5;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}