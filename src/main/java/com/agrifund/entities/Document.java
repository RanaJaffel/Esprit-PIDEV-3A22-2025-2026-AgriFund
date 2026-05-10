package com.agrifund.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité Document
 * Correspond à la table Document dans la base de données
 */
public class Document {

    private int id;
    private int utilisateurId;
    private String nom;
    private String typeDocument;
    private String cheminFichier;
    private int taille;
    private LocalDateTime dateUpload;
    private LocalDate dateExpiration;
    private String statut;

    // Constructeurs
    public Document() {
        this.dateUpload = LocalDateTime.now();
        this.statut = "en_attente";
    }

    public Document(int utilisateurId, String nom, String typeDocument, String cheminFichier) {
        this.utilisateurId = utilisateurId;
        this.nom = nom;
        this.typeDocument = typeDocument;
        this.cheminFichier = cheminFichier;
        this.dateUpload = LocalDateTime.now();
        this.statut = "en_attente";
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

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTypeDocument() {
        return typeDocument;
    }

    public void setTypeDocument(String typeDocument) {
        this.typeDocument = typeDocument;
    }

    public String getCheminFichier() {
        return cheminFichier;
    }

    public void setCheminFichier(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    public int getTaille() {
        return taille;
    }

    public void setTaille(int taille) {
        this.taille = taille;
    }

    public LocalDateTime getDateUpload() {
        return dateUpload;
    }

    public void setDateUpload(LocalDateTime dateUpload) {
        this.dateUpload = dateUpload;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", utilisateurId=" + utilisateurId +
                ", nom='" + nom + '\'' +
                ", typeDocument='" + typeDocument + '\'' +
                ", statut='" + statut + '\'' +
                ", dateUpload=" + dateUpload +
                '}';
    }
}
