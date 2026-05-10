package com.agrifund.entities;

import java.time.LocalDateTime;

/**
 * Entité TokenReinitialisation
 * Correspond à la table TokenReinitialisation dans la base de données
 * Utilisée pour la réinitialisation des mots de passe
 */
public class TokenReinitialisation {

    private int id;
    private int utilisateurId;
    private String token;
    private LocalDateTime dateExpiration;
    private boolean utilise;
    private LocalDateTime dateUtilisation;
    private LocalDateTime dateCreation;

    // Constructeurs
    public TokenReinitialisation() {
        this.dateCreation = LocalDateTime.now();
        this.utilise = false;
    }

    public TokenReinitialisation(int utilisateurId, String token, LocalDateTime dateExpiration) {
        this.utilisateurId = utilisateurId;
        this.token = token;
        this.dateExpiration = dateExpiration;
        this.dateCreation = LocalDateTime.now();
        this.utilise = false;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public boolean isUtilise() {
        return utilise;
    }

    public void setUtilise(boolean utilise) {
        this.utilise = utilise;
    }

    public LocalDateTime getDateUtilisation() {
        return dateUtilisation;
    }

    public void setDateUtilisation(LocalDateTime dateUtilisation) {
        this.dateUtilisation = dateUtilisation;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    /**
     * Vérifier si le token est valide (non expiré et non utilisé)
     */
    public boolean isValide() {
        return !utilise && LocalDateTime.now().isBefore(dateExpiration);
    }

    @Override
    public String toString() {
        return "TokenReinitialisation{" +
                "id=" + id +
                ", utilisateurId=" + utilisateurId +
                ", token='" + token + '\'' +
                ", dateExpiration=" + dateExpiration +
                ", utilise=" + utilise +
                '}';
    }
}
