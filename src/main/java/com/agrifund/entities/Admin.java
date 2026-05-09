package com.agrifund.entities;

/**
 * Entité Admin - hérite de Utilisateur
 * Correspond à la table Admin dans la base de données
 */
public class Admin extends Utilisateur {

    private int adminId; // ID dans la table Admin
    private int utilisateurId; // Référence vers la table Utilisateur

    // Constructeurs
    public Admin() {
        super();
    }

    public Admin(String nom, String prenom, String email, String password) {
        super(nom, prenom, email, password);
    }

    public Admin(int adminId, int utilisateurId) {
        super();
        this.adminId = adminId;
        this.utilisateurId = utilisateurId;
    }

    // Getters et Setters
    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "adminId=" + adminId +
                ", utilisateurId=" + utilisateurId +
                ", " + super.toString() +
                '}';
    }
}