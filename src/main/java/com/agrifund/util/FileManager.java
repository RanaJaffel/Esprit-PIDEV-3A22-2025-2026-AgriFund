package com.agrifund.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service de gestion des fichiers (photos de profil, documents)
 */
public class FileManager {

    // Répertoires de stockage
    private static final String BASE_DIR = "uploads/";
    private static final String PHOTOS_DIR = BASE_DIR + "photos/";
    private static final String DOCUMENTS_DIR = BASE_DIR + "documents/";

    // Extensions autorisées
    private static final String[] PHOTO_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};
    private static final String[] DOCUMENT_EXTENSIONS = {".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx"};

    // Taille maximale (en Mo)
    private static final long MAX_PHOTO_SIZE = 5 * 1024 * 1024; // 5 Mo
    private static final long MAX_DOCUMENT_SIZE = 10 * 1024 * 1024; // 10 Mo

    /**
     * Initialiser les répertoires de stockage
     */
    public static void initialiserRepertoires() {
        try {
            // Créer les répertoires s'ils n'existent pas
            Files.createDirectories(Paths.get(PHOTOS_DIR));
            Files.createDirectories(Paths.get(DOCUMENTS_DIR));
            System.out.println("✓ Répertoires de stockage initialisés");
            System.out.println("  - Photos : " + new File(PHOTOS_DIR).getAbsolutePath());
            System.out.println("  - Documents : " + new File(DOCUMENTS_DIR).getAbsolutePath());
        } catch (IOException e) {
            System.err.println("✗ Erreur lors de la création des répertoires: " + e.getMessage());
        }
    }

    /**
     * Uploader une photo de profil
     * @param cheminSource Chemin du fichier source sur l'ordinateur
     * @param utilisateurId ID de l'utilisateur
     * @return Chemin relatif du fichier uploadé, ou null si erreur
     */
    public static String uploadPhoto(String cheminSource, int utilisateurId) {
        try {
            File fichierSource = new File(cheminSource);

            // Vérifications
            if (!fichierSource.exists()) {
                System.err.println("✗ Fichier introuvable: " + cheminSource);
                return null;
            }

            if (!estExtensionValide(cheminSource, PHOTO_EXTENSIONS)) {
                System.err.println("✗ Extension de photo non autorisée (autorisées: jpg, jpeg, png, gif)");
                return null;
            }

            if (fichierSource.length() > MAX_PHOTO_SIZE) {
                System.err.println("✗ Photo trop volumineuse (max 5 Mo)");
                return null;
            }

            // Générer un nom de fichier unique
            String extension = getExtension(cheminSource);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomFichier = "user_" + utilisateurId + "_" + timestamp + extension;
            String cheminDestination = PHOTOS_DIR + nomFichier;

            // Copier le fichier
            Files.copy(fichierSource.toPath(),
                    Paths.get(cheminDestination),
                    StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✓ Photo uploadée: " + nomFichier);
            return cheminDestination;

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de l'upload de la photo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Uploader un document
     * @param cheminSource Chemin du fichier source
     * @param utilisateurId ID de l'utilisateur
     * @param typeDocument Type de document (cni, certificat_bio, etc.)
     * @return Chemin relatif du fichier uploadé, ou null si erreur
     */
    public static String uploadDocument(String cheminSource, int utilisateurId, String typeDocument) {
        try {
            File fichierSource = new File(cheminSource);

            // Vérifications
            if (!fichierSource.exists()) {
                System.err.println("✗ Fichier introuvable: " + cheminSource);
                return null;
            }

            if (!estExtensionValide(cheminSource, DOCUMENT_EXTENSIONS)) {
                System.err.println("✗ Extension de document non autorisée");
                System.err.println("  Autorisées: pdf, jpg, jpeg, png, doc, docx");
                return null;
            }

            if (fichierSource.length() > MAX_DOCUMENT_SIZE) {
                System.err.println("✗ Document trop volumineux (max 10 Mo)");
                return null;
            }

            // Générer un nom de fichier unique
            String extension = getExtension(cheminSource);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String nomFichier = "doc_" + utilisateurId + "_" + typeDocument + "_" + timestamp + extension;
            String cheminDestination = DOCUMENTS_DIR + nomFichier;

            // Copier le fichier
            Files.copy(fichierSource.toPath(),
                    Paths.get(cheminDestination),
                    StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✓ Document uploadé: " + nomFichier);
            return cheminDestination;

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de l'upload du document: " + e.getMessage());
            return null;
        }
    }

    /**
     * Supprimer un fichier
     */
    public static boolean supprimerFichier(String cheminFichier) {
        try {
            if (cheminFichier == null || cheminFichier.isEmpty()) {
                return false;
            }

            File fichier = new File(cheminFichier);
            if (fichier.exists()) {
                boolean supprime = fichier.delete();
                if (supprime) {
                    System.out.println("✓ Fichier supprimé: " + cheminFichier);
                }
                return supprime;
            }
            return false;

        } catch (Exception e) {
            System.err.println("✗ Erreur lors de la suppression: " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifier si un fichier existe
     */
    public static boolean fichierExiste(String cheminFichier) {
        if (cheminFichier == null || cheminFichier.isEmpty()) {
            return false;
        }
        return new File(cheminFichier).exists();
    }

    /**
     * Obtenir la taille d'un fichier en octets
     */
    public static long getTailleFichier(String cheminFichier) {
        if (cheminFichier == null || !fichierExiste(cheminFichier)) {
            return 0;
        }
        return new File(cheminFichier).length();
    }

    /**
     * Formater la taille d'un fichier pour affichage
     */
    public static String formaterTaille(long tailleOctets) {
        if (tailleOctets < 1024) {
            return tailleOctets + " octets";
        } else if (tailleOctets < 1024 * 1024) {
            return String.format("%.2f Ko", tailleOctets / 1024.0);
        } else {
            return String.format("%.2f Mo", tailleOctets / (1024.0 * 1024.0));
        }
    }

    /**
     * Vérifier si l'extension du fichier est valide
     */
    private static boolean estExtensionValide(String nomFichier, String[] extensionsAutorisees) {
        String extension = getExtension(nomFichier).toLowerCase();
        for (String ext : extensionsAutorisees) {
            if (extension.equals(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtenir l'extension d'un fichier
     */
    private static String getExtension(String nomFichier) {
        int index = nomFichier.lastIndexOf('.');
        if (index > 0) {
            return nomFichier.substring(index).toLowerCase();
        }
        return "";
    }

    /**
     * Lister tous les fichiers d'un utilisateur
     */
    public static void listerFichiersUtilisateur(int utilisateurId) {
        System.out.println("\n═══ FICHIERS DE L'UTILISATEUR " + utilisateurId + " ═══");

        // Photos
        File photosDir = new File(PHOTOS_DIR);
        File[] photos = photosDir.listFiles((dir, name) -> name.startsWith("user_" + utilisateurId + "_"));

        System.out.println("\n📷 PHOTOS :");
        if (photos != null && photos.length > 0) {
            for (File photo : photos) {
                System.out.println("  - " + photo.getName() + " (" + formaterTaille(photo.length()) + ")");
            }
        } else {
            System.out.println("  Aucune photo");
        }

        // Documents
        File docsDir = new File(DOCUMENTS_DIR);
        File[] docs = docsDir.listFiles((dir, name) -> name.startsWith("doc_" + utilisateurId + "_"));

        System.out.println("\n📄 DOCUMENTS :");
        if (docs != null && docs.length > 0) {
            for (File doc : docs) {
                System.out.println("  - " + doc.getName() + " (" + formaterTaille(doc.length()) + ")");
            }
        } else {
            System.out.println("  Aucun document");
        }
    }

    /**
     * Nettoyer les anciens fichiers (maintenance)
     */
    public static void nettoyerAnciensfichiers(int joursAnciennete) {
        System.out.println("\n🧹 Nettoyage des fichiers de plus de " + joursAnciennete + " jours...");

        long maintenant = System.currentTimeMillis();
        long seuilTemps = joursAnciennete * 24 * 60 * 60 * 1000L;

        int filesDeleted = 0;

        // Nettoyer les photos
        File photosDir = new File(PHOTOS_DIR);
        if (photosDir.exists()) {
            File[] photos = photosDir.listFiles();
            if (photos != null) {
                for (File photo : photos) {
                    if (maintenant - photo.lastModified() > seuilTemps) {
                        if (photo.delete()) {
                            filesDeleted++;
                        }
                    }
                }
            }
        }

        // Nettoyer les documents
        File docsDir = new File(DOCUMENTS_DIR);
        if (docsDir.exists()) {
            File[] docs = docsDir.listFiles();
            if (docs != null) {
                for (File doc : docs) {
                    if (maintenant - doc.lastModified() > seuilTemps) {
                        if (doc.delete()) {
                            filesDeleted++;
                        }
                    }
                }
            }
        }

        System.out.println("✓ " + filesDeleted + " fichier(s) supprimé(s)");
    }

    /**
     * Obtenir des statistiques sur le stockage
     */
    public static void afficherStatistiques() {
        System.out.println("\n📊 STATISTIQUES DE STOCKAGE");

        // Photos
        File photosDir = new File(PHOTOS_DIR);
        File[] photos = photosDir.listFiles();
        long taillePhotos = 0;
        int nbPhotos = 0;

        if (photos != null) {
            nbPhotos = photos.length;
            for (File photo : photos) {
                taillePhotos += photo.length();
            }
        }

        // Documents
        File docsDir = new File(DOCUMENTS_DIR);
        File[] docs = docsDir.listFiles();
        long tailleDocs = 0;
        int nbDocs = 0;

        if (docs != null) {
            nbDocs = docs.length;
            for (File doc : docs) {
                tailleDocs += doc.length();
            }
        }

        System.out.println("📷 Photos : " + nbPhotos + " fichier(s) - " + formaterTaille(taillePhotos));
        System.out.println("📄 Documents : " + nbDocs + " fichier(s) - " + formaterTaille(tailleDocs));
        System.out.println("💾 Total : " + (nbPhotos + nbDocs) + " fichier(s) - " + formaterTaille(taillePhotos + tailleDocs));
    }
}
