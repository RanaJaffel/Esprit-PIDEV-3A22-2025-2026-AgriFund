package com.agrifund.entities;

import java.time.LocalDateTime;

public class Message {

    private int id;
    private int conversationId;
    private int expediteurId;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private LocalDateTime dateModification;
    private LocalDateTime dateLecture;  // NOUVEAU
    private boolean estLu;
    private boolean estSupprime;
    private boolean aPieceJointe;
    private int nbPiecesJointes;
    private String nomExpediteur;

    // Constructeurs
    public Message() {
        this.dateEnvoi = LocalDateTime.now();
        this.estLu = false;
        this.estSupprime = false;
    }

    public Message(int conversationId, int expediteurId, String contenu) {
        this.conversationId = conversationId;
        this.expediteurId = expediteurId;
        this.contenu = contenu;
        this.dateEnvoi = LocalDateTime.now();
        this.estLu = false;
        this.estSupprime = false;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getConversationId() { return conversationId; }
    public void setConversationId(int conversationId) { this.conversationId = conversationId; }

    public int getExpediteurId() { return expediteurId; }
    public void setExpediteurId(int expediteurId) { this.expediteurId = expediteurId; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    public LocalDateTime getDateLecture() { return dateLecture; }
    public void setDateLecture(LocalDateTime dateLecture) { this.dateLecture = dateLecture; }

    public boolean isEstLu() { return estLu; }
    public void setEstLu(boolean estLu) { this.estLu = estLu; }

    public boolean isEstSupprime() { return estSupprime; }
    public void setEstSupprime(boolean estSupprime) { this.estSupprime = estSupprime; }

    public String getNomExpediteur() { return nomExpediteur; }
    public void setNomExpediteur(String nomExpediteur) { this.nomExpediteur = nomExpediteur; }

    public boolean isaPieceJointe() { return aPieceJointe; }
    public void setaPieceJointe(boolean aPieceJointe) { this.aPieceJointe = aPieceJointe; }

    public int getNbPiecesJointes() { return nbPiecesJointes; }
    public void setNbPiecesJointes(int nbPiecesJointes) { this.nbPiecesJointes = nbPiecesJointes; }

    public String getApercu() {
        if (contenu == null || contenu.isEmpty()) return "";
        return contenu.length() > 50 ? contenu.substring(0, 50) + "..." : contenu;
    }

    public boolean estModifie() {
        return dateModification != null;
    }

    /**
     * Obtenir l'indicateur de statut du message (style WhatsApp)
     * ✓ = Envoyé
     * ✓✓ = Délivré
     * ✓✓ (bleu/vert) = Lu
     */
    public String getIndicateurStatut() {
        if (estLu) {
            return "✓✓"; // Deux coches pour lu
        } else {
            return "✓"; // Une coche pour envoyé
        }
    }

    /**
     * Obtenir la couleur de l'indicateur
     */
    public String getCouleurIndicateur() {
        if (estLu) {
            return "#089647"; // Vert pour lu
        } else {
            return "#848A86"; // Gris pour envoyé
        }
    }

    /**
     * Obtenir l'heure de lecture formatée
     */
    public String getHeureLecture() {
        if (dateLecture != null) {
            return "Lu à " + dateLecture.format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        }
        return null;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", conversationId=" + conversationId +
                ", contenu='" + getApercu() + '\'' +
                ", estLu=" + estLu +
                '}';
    }
}