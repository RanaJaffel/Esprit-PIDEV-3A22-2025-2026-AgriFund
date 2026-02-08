package entities;

import java.time.LocalDateTime;

/**
 * Entité Message
 * Représente un message dans une conversation
 */
public class Message {

    private int id;
    private int conversationId;
    private int expediteurId;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private LocalDateTime dateModification;
    private boolean estLu;
    private boolean estSupprime;

    // Informations supplémentaires (non en base)
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
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public int getExpediteurId() {
        return expediteurId;
    }

    public void setExpediteurId(int expediteurId) {
        this.expediteurId = expediteurId;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public boolean isEstLu() {
        return estLu;
    }

    public void setEstLu(boolean estLu) {
        this.estLu = estLu;
    }

    public boolean isEstSupprime() {
        return estSupprime;
    }

    public void setEstSupprime(boolean estSupprime) {
        this.estSupprime = estSupprime;
    }

    public String getNomExpediteur() {
        return nomExpediteur;
    }

    public void setNomExpediteur(String nomExpediteur) {
        this.nomExpediteur = nomExpediteur;
    }

    /**
     * Obtenir un aperçu du message (premiers 50 caractères)
     */
    public String getApercu() {
        if (contenu == null || contenu.isEmpty()) {
            return "";
        }
        return contenu.length() > 50 ? contenu.substring(0, 50) + "..." : contenu;
    }

    /**
     * Vérifier si le message a été modifié
     */
    public boolean estModifie() {
        return dateModification != null;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", conversationId=" + conversationId +
                ", expediteurId=" + expediteurId +
                ", contenu='" + getApercu() + '\'' +
                ", dateEnvoi=" + dateEnvoi +
                ", estLu=" + estLu +
                '}';
    }
}