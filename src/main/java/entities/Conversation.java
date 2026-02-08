package entities;

import java.time.LocalDateTime;

/**
 * Entité Conversation
 * Représente une conversation entre deux utilisateurs
 */
public class Conversation {

    private int id;
    private int utilisateur1Id;
    private int utilisateur2Id;
    private LocalDateTime dateCreation;
    private LocalDateTime derniereActivite;

    // Informations supplémentaires (non en base)
    private String nomCorrespondant;
    private String dernierMessage;
    private int nbMessagesNonLus;

    // Constructeurs
    public Conversation() {
        this.dateCreation = LocalDateTime.now();
        this.derniereActivite = LocalDateTime.now();
    }

    public Conversation(int utilisateur1Id, int utilisateur2Id) {
        this.utilisateur1Id = utilisateur1Id;
        this.utilisateur2Id = utilisateur2Id;
        this.dateCreation = LocalDateTime.now();
        this.derniereActivite = LocalDateTime.now();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUtilisateur1Id() {
        return utilisateur1Id;
    }

    public void setUtilisateur1Id(int utilisateur1Id) {
        this.utilisateur1Id = utilisateur1Id;
    }

    public int getUtilisateur2Id() {
        return utilisateur2Id;
    }

    public void setUtilisateur2Id(int utilisateur2Id) {
        this.utilisateur2Id = utilisateur2Id;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDerniereActivite() {
        return derniereActivite;
    }

    public void setDerniereActivite(LocalDateTime derniereActivite) {
        this.derniereActivite = derniereActivite;
    }

    public String getNomCorrespondant() {
        return nomCorrespondant;
    }

    public void setNomCorrespondant(String nomCorrespondant) {
        this.nomCorrespondant = nomCorrespondant;
    }

    public String getDernierMessage() {
        return dernierMessage;
    }

    public void setDernierMessage(String dernierMessage) {
        this.dernierMessage = dernierMessage;
    }

    public int getNbMessagesNonLus() {
        return nbMessagesNonLus;
    }

    public void setNbMessagesNonLus(int nbMessagesNonLus) {
        this.nbMessagesNonLus = nbMessagesNonLus;
    }

    /**
     * Obtenir l'ID du correspondant par rapport à l'utilisateur actuel
     */
    public int getCorrespondantId(int utilisateurActuelId) {
        return (utilisateur1Id == utilisateurActuelId) ? utilisateur2Id : utilisateur1Id;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", utilisateur1Id=" + utilisateur1Id +
                ", utilisateur2Id=" + utilisateur2Id +
                ", derniereActivite=" + derniereActivite +
                '}';
    }
}