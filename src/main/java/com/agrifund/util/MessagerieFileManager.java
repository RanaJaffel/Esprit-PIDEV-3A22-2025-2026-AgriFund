package com.agrifund.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Gestionnaire de fichiers pour la messagerie
 * Gère les uploads d'images, documents et audios
 */
public class MessagerieFileManager {

    // Répertoires de base
    private static final String BASE_DIR = "uploads/messagerie/";
    private static final String IMAGES_DIR = BASE_DIR + "images/";
    private static final String DOCUMENTS_DIR = BASE_DIR + "documents/";
    private static final String AUDIOS_DIR = BASE_DIR + "audios/";
    private static final String VIDEOS_DIR = BASE_DIR + "videos/";

    // Extensions autorisées
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "odt", "ods"
    );

    private static final List<String> AUDIO_EXTENSIONS = Arrays.asList(
            "mp3", "wav", "ogg", "m4a", "aac", "flac", "wma"
    );

    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList(
            "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm"
    );

    // Limites de taille (en octets)
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;    // 10 Mo
    private static final long MAX_DOCUMENT_SIZE = 25 * 1024 * 1024; // 25 Mo
    private static final long MAX_AUDIO_SIZE = 15 * 1024 * 1024;    // 15 Mo
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024;    // 50 Mo

    /**
     * Initialiser les répertoires de stockage
     */
    public static void initialiserRepertoires() {
        try {
            Files.createDirectories(Paths.get(IMAGES_DIR));
            Files.createDirectories(Paths.get(DOCUMENTS_DIR));
            Files.createDirectories(Paths.get(AUDIOS_DIR));
            Files.createDirectories(Paths.get(VIDEOS_DIR));

            System.out.println("✓ Répertoires messagerie initialisés:");
            System.out.println("  - Images: " + new File(IMAGES_DIR).getAbsolutePath());
            System.out.println("  - Documents: " + new File(DOCUMENTS_DIR).getAbsolutePath());
            System.out.println("  - Audios: " + new File(AUDIOS_DIR).getAbsolutePath());
            System.out.println("  - Vidéos: " + new File(VIDEOS_DIR).getAbsolutePath());

        } catch (IOException e) {
            System.err.println("✗ Erreur création répertoires messagerie: " + e.getMessage());
        }
    }

    /**
     * Uploader une image
     */
    public static String uploadImage(String cheminSource, int messageId) throws IOException {
        File source = new File(cheminSource);

        // Vérifications
        if (!source.exists()) {
            throw new IOException("Fichier introuvable: " + cheminSource);
        }

        String extension = getExtension(source.getName()).toLowerCase();
        if (!IMAGE_EXTENSIONS.contains(extension)) {
            throw new IOException("Extension non autorisée. Images acceptées: " + IMAGE_EXTENSIONS);
        }

        if (source.length() > MAX_IMAGE_SIZE) {
            throw new IOException("Image trop volumineuse (max " +
                    FileManager.formaterTaille(MAX_IMAGE_SIZE) + ")");
        }

        // Nom unique
        String nomUnique = genererNomUnique(messageId, "img", extension);
        Path destination = Paths.get(IMAGES_DIR + nomUnique);

        // Copier le fichier
        Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        return IMAGES_DIR + nomUnique;
    }

    /**
     * Uploader un document
     */
    public static String uploadDocument(String cheminSource, int messageId) throws IOException {
        File source = new File(cheminSource);

        // Vérifications
        if (!source.exists()) {
            throw new IOException("Fichier introuvable: " + cheminSource);
        }

        String extension = getExtension(source.getName()).toLowerCase();
        if (!DOCUMENT_EXTENSIONS.contains(extension)) {
            throw new IOException("Extension non autorisée. Documents acceptés: " + DOCUMENT_EXTENSIONS);
        }

        if (source.length() > MAX_DOCUMENT_SIZE) {
            throw new IOException("Document trop volumineux (max " +
                    FileManager.formaterTaille(MAX_DOCUMENT_SIZE) + ")");
        }

        // Nom unique
        String nomUnique = genererNomUnique(messageId, "doc", extension);
        Path destination = Paths.get(DOCUMENTS_DIR + nomUnique);

        // Copier le fichier
        Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        return DOCUMENTS_DIR + nomUnique;
    }

    /**
     * Uploader un fichier audio
     */
    public static String uploadAudio(String cheminSource, int messageId) throws IOException {
        File source = new File(cheminSource);

        // Vérifications
        if (!source.exists()) {
            throw new IOException("Fichier introuvable: " + cheminSource);
        }

        String extension = getExtension(source.getName()).toLowerCase();
        if (!AUDIO_EXTENSIONS.contains(extension)) {
            throw new IOException("Extension non autorisée. Audios acceptés: " + AUDIO_EXTENSIONS);
        }

        if (source.length() > MAX_AUDIO_SIZE) {
            throw new IOException("Audio trop volumineux (max " +
                    FileManager.formaterTaille(MAX_AUDIO_SIZE) + ")");
        }

        // Nom unique
        String nomUnique = genererNomUnique(messageId, "audio", extension);
        Path destination = Paths.get(AUDIOS_DIR + nomUnique);

        // Copier le fichier
        Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        return AUDIOS_DIR + nomUnique;
    }

    /**
     * Uploader une vidéo
     */
    public static String uploadVideo(String cheminSource, int messageId) throws IOException {
        File source = new File(cheminSource);

        // Vérifications
        if (!source.exists()) {
            throw new IOException("Fichier introuvable: " + cheminSource);
        }

        String extension = getExtension(source.getName()).toLowerCase();
        if (!VIDEO_EXTENSIONS.contains(extension)) {
            throw new IOException("Extension non autorisée. Vidéos acceptées: " + VIDEO_EXTENSIONS);
        }

        if (source.length() > MAX_VIDEO_SIZE) {
            throw new IOException("Vidéo trop volumineuse (max " +
                    FileManager.formaterTaille(MAX_VIDEO_SIZE) + ")");
        }

        // Nom unique
        String nomUnique = genererNomUnique(messageId, "video", extension);
        Path destination = Paths.get(VIDEOS_DIR + nomUnique);

        // Copier le fichier
        Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        return VIDEOS_DIR + nomUnique;
    }

    /**
     * Déterminer le type de fichier
     */
    public static String determinerTypeFichier(String nomFichier) {
        String extension = getExtension(nomFichier).toLowerCase();

        if (IMAGE_EXTENSIONS.contains(extension)) return "image";
        if (DOCUMENT_EXTENSIONS.contains(extension)) return "document";
        if (AUDIO_EXTENSIONS.contains(extension)) return "audio";
        if (VIDEO_EXTENSIONS.contains(extension)) return "video";

        return "autre";
    }

    /**
     * Générer un nom unique pour un fichier
     */
    private static String genererNomUnique(int messageId, String prefixe, String extension) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        return String.format("msg_%d_%s_%s.%s", messageId, prefixe, timestamp, extension);
    }

    /**
     * Obtenir l'extension d'un fichier
     */
    private static String getExtension(String nomFichier) {
        int lastDot = nomFichier.lastIndexOf('.');
        if (lastDot == -1) return "";
        return nomFichier.substring(lastDot + 1);
    }

    /**
     * Supprimer un fichier
     */
    public static boolean supprimerFichier(String cheminFichier) {
        try {
            File file = new File(cheminFichier);
            if (file.exists()) {
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            System.err.println("Erreur suppression fichier: " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifier si un fichier existe
     */
    public static boolean fichierExiste(String cheminFichier) {
        return new File(cheminFichier).exists();
    }

    /**
     * Obtenir la taille d'un fichier
     */
    public static long getTailleFichier(String cheminFichier) {
        File file = new File(cheminFichier);
        return file.exists() ? file.length() : 0;
    }

    /**
     * Afficher les statistiques de stockage
     */
    public static void afficherStatistiques() {
        System.out.println("\n═══ STATISTIQUES STOCKAGE MESSAGERIE ═══");

        long tailleImages = calculerTailleRepertoire(IMAGES_DIR);
        long tailleDocuments = calculerTailleRepertoire(DOCUMENTS_DIR);
        long tailleAudios = calculerTailleRepertoire(AUDIOS_DIR);
        long tailleVideos = calculerTailleRepertoire(VIDEOS_DIR);
        long total = tailleImages + tailleDocuments + tailleAudios + tailleVideos;

        System.out.println("Images    : " + FileManager.formaterTaille(tailleImages));
        System.out.println("Documents : " + FileManager.formaterTaille(tailleDocuments));
        System.out.println("Audios    : " + FileManager.formaterTaille(tailleAudios));
        System.out.println("Vidéos    : " + FileManager.formaterTaille(tailleVideos));
        System.out.println("─────────────────────────────────");
        System.out.println("TOTAL     : " + FileManager.formaterTaille(total));
    }

    /**
     * Calculer la taille d'un répertoire
     */
    private static long calculerTailleRepertoire(String chemin) {
        File dir = new File(chemin);
        if (!dir.exists() || !dir.isDirectory()) return 0;

        long taille = 0;
        File[] fichiers = dir.listFiles();
        if (fichiers != null) {
            for (File f : fichiers) {
                if (f.isFile()) {
                    taille += f.length();
                }
            }
        }
        return taille;
    }

    /**
     * Obtenir les limites de taille par type
     */
    public static String getLimitesInfo() {
        return "Limites de taille:\n" +
                "  Images    : max " + FileManager.formaterTaille(MAX_IMAGE_SIZE) + "\n" +
                "  Documents : max " + FileManager.formaterTaille(MAX_DOCUMENT_SIZE) + "\n" +
                "  Audios    : max " + FileManager.formaterTaille(MAX_AUDIO_SIZE) + "\n" +
                "  Vidéos    : max " + FileManager.formaterTaille(MAX_VIDEO_SIZE);
    }
}