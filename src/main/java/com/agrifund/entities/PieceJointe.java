package com.agrifund.entities;

import java.time.LocalDateTime;

/**
 * Entité PieceJointe
 * Représente un fichier attaché à un message
 */
public class PieceJointe {

    private int id;
    private int messageId;
    private String typeFichier; // image, document, audio, video, autre
    private String nomOriginal;
    private String nomStockage;
    private String cheminFichier;
    private long tailleOctets;
    private String extension;
    private String mimeType;
    private LocalDateTime dateUpload;

    // Constructeurs
    public PieceJointe() {
        this.dateUpload = LocalDateTime.now();
    }

    public PieceJointe(int messageId, String typeFichier, String nomOriginal,
                       String cheminFichier, long tailleOctets) {
        this.messageId = messageId;
        this.typeFichier = typeFichier;
        this.nomOriginal = nomOriginal;
        this.cheminFichier = cheminFichier;
        this.tailleOctets = tailleOctets;
        this.dateUpload = LocalDateTime.now();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getTypeFichier() {
        return typeFichier;
    }

    public void setTypeFichier(String typeFichier) {
        this.typeFichier = typeFichier;
    }

    public String getNomOriginal() {
        return nomOriginal;
    }

    public void setNomOriginal(String nomOriginal) {
        this.nomOriginal = nomOriginal;
    }

    public String getNomStockage() {
        return nomStockage;
    }

    public void setNomStockage(String nomStockage) {
        this.nomStockage = nomStockage;
    }

    public String getCheminFichier() {
        return cheminFichier;
    }

    public void setCheminFichier(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    public long getTailleOctets() {
        return tailleOctets;
    }

    public void setTailleOctets(long tailleOctets) {
        this.tailleOctets = tailleOctets;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public LocalDateTime getDateUpload() {
        return dateUpload;
    }

    public void setDateUpload(LocalDateTime dateUpload) {
        this.dateUpload = dateUpload;
    }

    /**
     * Obtenir l'icône selon le type de fichier
     */
    public String getIcone() {
        switch (typeFichier) {
            case "image": return "🖼️";
            case "document": return "📄";
            case "audio": return "🎵";
            case "video": return "🎬";
            default: return "📎";
        }
    }

    /**
     * Formater la taille en format lisible
     */
    public String getTailleFormatee() {
        return com.agrifund.util.FileManager.formaterTaille(tailleOctets);
    }

    /**
     * Vérifier si c'est une image
     */
    public boolean estImage() {
        return "image".equals(typeFichier);
    }

    /**
     * Vérifier si c'est un audio
     */
    public boolean estAudio() {
        return "audio".equals(typeFichier);
    }

    /**
     * Vérifier si c'est un document
     */
    public boolean estDocument() {
        return "document".equals(typeFichier);
    }

    @Override
    public String toString() {
        return "PieceJointe{" +
                "id=" + id +
                ", type='" + typeFichier + '\'' +
                ", nom='" + nomOriginal + '\'' +
                ", taille=" + getTailleFormatee() +
                '}';
    }
}