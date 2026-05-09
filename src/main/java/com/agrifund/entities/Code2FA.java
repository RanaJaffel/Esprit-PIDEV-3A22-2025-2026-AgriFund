package com.agrifund.entities;

import java.time.LocalDateTime;

/**
 * Entité Code2FA
 * Représente un code de vérification à deux facteurs
 */
public class Code2FA {

    private int id;
    private int utilisateurId;
    private String code;
    private LocalDateTime dateCreation;
    private LocalDateTime dateExpiration;
    private boolean estUtilise;
    private LocalDateTime dateUtilisation;
    private String typeEnvoi; // email, sms

    // Constructeurs
    public Code2FA() {
        this.dateCreation = LocalDateTime.now();
        this.estUtilise = false;
    }

    public Code2FA(int utilisateurId, String code, LocalDateTime dateExpiration, String typeEnvoi) {
        this.utilisateurId = utilisateurId;
        this.code = code;
        this.dateCreation = LocalDateTime.now();
        this.dateExpiration = dateExpiration;
        this.typeEnvoi = typeEnvoi;
        this.estUtilise = false;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public boolean isEstUtilise() {
        return estUtilise;
    }

    public void setEstUtilise(boolean estUtilise) {
        this.estUtilise = estUtilise;
    }

    public LocalDateTime getDateUtilisation() {
        return dateUtilisation;
    }

    public void setDateUtilisation(LocalDateTime dateUtilisation) {
        this.dateUtilisation = dateUtilisation;
    }

    public String getTypeEnvoi() {
        return typeEnvoi;
    }

    public void setTypeEnvoi(String typeEnvoi) {
        this.typeEnvoi = typeEnvoi;
    }

    /**
     * Vérifier si le code est valide (non expiré et non utilisé)
     */
    public boolean isValide() {
        if (estUtilise) {
            return false;
        }

        if (dateExpiration == null) {
            return false;
        }

        return LocalDateTime.now().isBefore(dateExpiration);
    }

    /**
     * Obtenir le temps restant avant expiration (en minutes)
     */
    public long getMinutesRestantes() {
        if (dateExpiration == null) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(dateExpiration)) {
            return 0;
        }

        return java.time.Duration.between(now, dateExpiration).toMinutes();
    }

    @Override
    public String toString() {
        return "Code2FA{" +
                "id=" + id +
                ", utilisateurId=" + utilisateurId +
                ", code='" + code + '\'' +
                ", typeEnvoi='" + typeEnvoi + '\'' +
                ", valide=" + isValide() +
                '}';
    }
}