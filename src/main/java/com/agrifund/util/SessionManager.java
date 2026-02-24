package com.agrifund.util;

import com.agrifund.entities.Utilisateur;
import com.agrifund.entities.Agriculteur;
import com.agrifund.entities.Banque;
import com.agrifund.entities.Admin;

/**
 * Gestionnaire de session utilisateur (Singleton)
 */
public class SessionManager {

    private static SessionManager instance;

    private Utilisateur utilisateurConnecte;
    private String typeUtilisateur;
    private Agriculteur agriculteurConnecte;
    private Banque banqueConnectee;
    private Admin adminConnecte;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Getters et Setters
    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public void setUtilisateurConnecte(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }

    public String getTypeUtilisateur() {
        return typeUtilisateur;
    }

    public void setTypeUtilisateur(String type) {
        this.typeUtilisateur = type;
    }

    public Agriculteur getAgriculteurConnecte() {
        return agriculteurConnecte;
    }

    public void setAgriculteurConnecte(Agriculteur agriculteur) {
        this.agriculteurConnecte = agriculteur;
    }

    public Banque getBanqueConnectee() {
        return banqueConnectee;
    }

    public void setBanqueConnectee(Banque banque) {
        this.banqueConnectee = banque;
    }

    public Admin getAdminConnecte() {
        return adminConnecte;
    }

    public void setAdminConnecte(Admin admin) {
        this.adminConnecte = admin;
    }

    public boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    public void deconnecter() {
        utilisateurConnecte = null;
        typeUtilisateur = null;
        agriculteurConnecte = null;
        banqueConnectee = null;
        adminConnecte = null;
    }

    public String getPhotoPath() {
        if (utilisateurConnecte != null && utilisateurConnecte.getPhoto() != null) {
            return utilisateurConnecte.getPhoto();
        }
        return null;
    }

    public String getNomComplet() {
        if (utilisateurConnecte != null) {
            return utilisateurConnecte.getPrenom() + " " + utilisateurConnecte.getNom();
        }
        return "Utilisateur";
    }
}