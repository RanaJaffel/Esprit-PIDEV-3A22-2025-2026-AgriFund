package com.agrifund.services;

import com.agrifund.entities.Conversation;
import com.agrifund.entities.Message;
import com.agrifund.entities.Utilisateur;
import com.agrifund.util.DatabaseConnection;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de Messagerie
 * Gère les conversations et messages entre utilisateurs
 */
public class MessagerieService {

    private Connection connection;
    private UtilisateurService utilisateurService;

    public MessagerieService() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
        this.utilisateurService = new UtilisateurService();
    }

    // ============================================
    // GESTION DES CONVERSATIONS
    // ============================================

    /**
     * Créer ou récupérer une conversation entre deux utilisateurs
     */
    public Conversation creerOuRecupererConversation(int utilisateur1Id, int utilisateur2Id) throws SQLException {
        // Vérifier si la conversation existe déjà
        Conversation conv = trouverConversation(utilisateur1Id, utilisateur2Id);

        if (conv != null) {
            return conv;
        }

        // Créer une nouvelle conversation
        String sql = "INSERT INTO Conversation (utilisateur1_id, utilisateur2_id) VALUES (?, ?)";
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Toujours mettre le plus petit ID en premier (pour unicité)
            int user1 = Math.min(utilisateur1Id, utilisateur2Id);
            int user2 = Math.max(utilisateur1Id, utilisateur2Id);

            pst.setInt(1, user1);
            pst.setInt(2, user2);

            pst.executeUpdate();

            rs = pst.getGeneratedKeys();
            if (rs.next()) {
                conv = new Conversation(user1, user2);
                conv.setId(rs.getInt(1));
                System.out.println("✓ Nouvelle conversation créée (ID: " + conv.getId() + ")");
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return conv;
    }

    /**
     * Trouver une conversation existante entre deux utilisateurs
     */
    private Conversation trouverConversation(int utilisateur1Id, int utilisateur2Id) throws SQLException {
        String sql = "SELECT * FROM Conversation " +
                "WHERE (utilisateur1_id = ? AND utilisateur2_id = ?) " +
                "   OR (utilisateur1_id = ? AND utilisateur2_id = ?)";

        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, Math.min(utilisateur1Id, utilisateur2Id));
            pst.setInt(2, Math.max(utilisateur1Id, utilisateur2Id));
            pst.setInt(3, Math.min(utilisateur1Id, utilisateur2Id));
            pst.setInt(4, Math.max(utilisateur1Id, utilisateur2Id));

            rs = pst.executeQuery();

            if (rs.next()) {
                Conversation conv = new Conversation();
                conv.setId(rs.getInt("id"));
                conv.setUtilisateur1Id(rs.getInt("utilisateur1_id"));
                conv.setUtilisateur2Id(rs.getInt("utilisateur2_id"));

                Timestamp creation = rs.getTimestamp("date_creation");
                if (creation != null) {
                    conv.setDateCreation(creation.toLocalDateTime());
                }

                Timestamp activite = rs.getTimestamp("derniere_activite");
                if (activite != null) {
                    conv.setDerniereActivite(activite.toLocalDateTime());
                }

                return conv;
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return null;
    }

    /**
     * Lister toutes les conversations d'un utilisateur
     */
    public List<Conversation> listerConversations(int utilisateurId) throws SQLException {
        List<Conversation> conversations = new ArrayList<>();

        String sql = "SELECT c.*, " +
                "  CASE WHEN c.utilisateur1_id = ? THEN c.utilisateur2_id ELSE c.utilisateur1_id END AS correspondant_id, " +
                "  (SELECT COUNT(*) FROM Message m WHERE m.conversation_id = c.id AND m.expediteur_id != ? AND m.est_lu = FALSE AND m.est_supprime = FALSE) AS nb_non_lus, " +
                "  u.prenom, u.nom, u.photo " +
                "FROM Conversation c " +
                "LEFT JOIN Utilisateur u ON u.id = CASE WHEN c.utilisateur1_id = ? THEN c.utilisateur2_id ELSE c.utilisateur1_id END " +
                "WHERE c.utilisateur1_id = ? OR c.utilisateur2_id = ? " +
                "ORDER BY c.derniere_activite DESC";

        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, utilisateurId);
            pst.setInt(2, utilisateurId);
            pst.setInt(3, utilisateurId);
            pst.setInt(4, utilisateurId);
            pst.setInt(5, utilisateurId);

            rs = pst.executeQuery();

            while (rs.next()) {
                Conversation conv = new Conversation();
                conv.setId(rs.getInt("id"));
                conv.setUtilisateur1Id(rs.getInt("utilisateur1_id"));
                conv.setUtilisateur2Id(rs.getInt("utilisateur2_id"));

                Timestamp activite = rs.getTimestamp("derniere_activite");
                if (activite != null) {
                    conv.setDerniereActivite(activite.toLocalDateTime());
                }

                // Correspondant avec photo
                String prenom = rs.getString("prenom");
                String nom = rs.getString("nom");
                String photo = rs.getString("photo");

                conv.setNomCorrespondant((prenom != null ? prenom : "") + " " + (nom != null ? nom : ""));

                // Stocker la photo dans un attribut supplémentaire (à ajouter dans Conversation)
                // Pour l'instant, on peut l'ajouter au nom
                if (photo != null && !photo.isEmpty() && com.agrifund.util.FileManager.fichierExiste(photo)) {
                    conv.setNomCorrespondant("📷 " + conv.getNomCorrespondant());
                }

                // Messages non lus
                conv.setNbMessagesNonLus(rs.getInt("nb_non_lus"));

                // Dernier message
                Message dernierMsg = getDernierMessage(conv.getId());
                if (dernierMsg != null) {
                    conv.setDernierMessage(dernierMsg.getApercu());
                }

                conversations.add(conv);
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return conversations;
    }

    // ============================================
    // GESTION DES MESSAGES
    // ============================================

    /**
     * Envoyer un message
     */
    public Message envoyerMessage(int conversationId, int expediteurId, String contenu) throws SQLException {
        String sql = "INSERT INTO Message (conversation_id, expediteur_id, contenu) VALUES (?, ?, ?)";

        PreparedStatement pst = null;
        ResultSet rs = null;
        Message message = null;

        try {
            pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pst.setInt(1, conversationId);
            pst.setInt(2, expediteurId);
            pst.setString(3, contenu);

            pst.executeUpdate();

            rs = pst.getGeneratedKeys();
            if (rs.next()) {
                message = new Message(conversationId, expediteurId, contenu);
                message.setId(rs.getInt(1));
            }

            // Mettre à jour l'activité de la conversation
            mettreAJourActiviteConversation(conversationId);

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return message;
    }

    /**
     * Modifier un message
     */
    public boolean modifierMessage(int messageId, int utilisateurId, String nouveauContenu) throws SQLException {
        // Vérifier que l'utilisateur est l'expéditeur
        String sqlVerif = "SELECT expediteur_id FROM Message WHERE id = ?";
        PreparedStatement pstVerif = null;
        ResultSet rs = null;

        try {
            pstVerif = connection.prepareStatement(sqlVerif);
            pstVerif.setInt(1, messageId);
            rs = pstVerif.executeQuery();

            if (rs.next()) {
                int expediteurId = rs.getInt("expediteur_id");
                if (expediteurId != utilisateurId) {
                    System.out.println("✗ Vous ne pouvez modifier que vos propres messages!");
                    return false;
                }
            } else {
                System.out.println("✗ Message non trouvé!");
                return false;
            }
        } finally {
            if (rs != null) rs.close();
            if (pstVerif != null) pstVerif.close();
        }

        // Modifier le message
        String sql = "UPDATE Message SET contenu = ?, date_modification = ? WHERE id = ?";
        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setString(1, nouveauContenu);
            pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pst.setInt(3, messageId);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Message modifié!");
                return true;
            }

        } finally {
            if (pst != null) pst.close();
        }

        return false;
    }

    /**
     * Supprimer un message
     */
    public boolean supprimerMessage(int messageId, int utilisateurId) throws SQLException {
        // Vérifier que l'utilisateur est l'expéditeur
        String sqlVerif = "SELECT expediteur_id FROM Message WHERE id = ?";
        PreparedStatement pstVerif = null;
        ResultSet rs = null;

        try {
            pstVerif = connection.prepareStatement(sqlVerif);
            pstVerif.setInt(1, messageId);
            rs = pstVerif.executeQuery();

            if (rs.next()) {
                int expediteurId = rs.getInt("expediteur_id");
                if (expediteurId != utilisateurId) {
                    System.out.println("✗ Vous ne pouvez supprimer que vos propres messages!");
                    return false;
                }
            } else {
                System.out.println("✗ Message non trouvé!");
                return false;
            }
        } finally {
            if (rs != null) rs.close();
            if (pstVerif != null) pstVerif.close();
        }

        // Marquer comme supprimé (suppression logique)
        String sql = "UPDATE Message SET est_supprime = TRUE WHERE id = ?";
        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, messageId);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Message supprimé!");
                return true;
            }

        } finally {
            if (pst != null) pst.close();
        }

        return false;
    }

    /**
     * Lister les messages d'une conversation
     */
    public List<Message> listerMessages(int conversationId, int utilisateurId) throws SQLException {
        List<Message> messages = new ArrayList<>();

        String sql = "SELECT m.*, u.prenom, u.nom " +
                "FROM Message m " +
                "INNER JOIN Utilisateur u ON m.expediteur_id = u.id " +
                "WHERE m.conversation_id = ? AND m.est_supprime = FALSE " +
                "ORDER BY m.date_envoi ASC";

        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, conversationId);

            rs = pst.executeQuery();

            while (rs.next()) {
                Message msg = new Message();
                msg.setId(rs.getInt("id"));
                msg.setConversationId(rs.getInt("conversation_id"));
                msg.setExpediteurId(rs.getInt("expediteur_id"));
                msg.setContenu(rs.getString("contenu"));
                msg.setEstLu(rs.getBoolean("est_lu"));
                msg.setaPieceJointe(rs.getBoolean("a_piece_jointe"));
                msg.setNbPiecesJointes(rs.getInt("nb_pieces_jointes"));

                Timestamp envoi = rs.getTimestamp("date_envoi");
                if (envoi != null) {
                    msg.setDateEnvoi(envoi.toLocalDateTime());
                }

                Timestamp modif = rs.getTimestamp("date_modification");
                if (modif != null) {
                    msg.setDateModification(modif.toLocalDateTime());
                }

                msg.setNomExpediteur(rs.getString("prenom") + " " + rs.getString("nom"));

                messages.add(msg);
            }

            // Marquer les messages comme lus
            marquerMessagesCommelus(conversationId, utilisateurId);

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return messages;
    }

    /**
     * Marquer les messages comme lus
     */
    private void marquerMessagesCommelus(int conversationId, int utilisateurId) throws SQLException {
        String sql = "UPDATE Message SET est_lu = TRUE " +
                "WHERE conversation_id = ? AND expediteur_id != ? AND est_lu = FALSE";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, conversationId);
            pst.setInt(2, utilisateurId);

            pst.executeUpdate();

        } finally {
            if (pst != null) pst.close();
        }
    }

    /**
     * Obtenir le dernier message d'une conversation
     */
    private Message getDernierMessage(int conversationId) throws SQLException {
        String sql = "SELECT * FROM Message " +
                "WHERE conversation_id = ? AND est_supprime = FALSE " +
                "ORDER BY date_envoi DESC LIMIT 1";

        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, conversationId);

            rs = pst.executeQuery();

            if (rs.next()) {
                Message msg = new Message();
                msg.setId(rs.getInt("id"));
                msg.setContenu(rs.getString("contenu"));
                return msg;
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return null;
    }

    /**
     * Mettre à jour l'activité de la conversation
     */
    private void mettreAJourActiviteConversation(int conversationId) throws SQLException {
        String sql = "UPDATE Conversation SET derniere_activite = ? WHERE id = ?";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pst.setInt(2, conversationId);

            pst.executeUpdate();

        } finally {
            if (pst != null) pst.close();
        }
    }

    /**
     * Compter les messages non lus d'un utilisateur
     */
    public int compterMessagesNonLus(int utilisateurId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Message m " +
                "INNER JOIN Conversation c ON m.conversation_id = c.id " +
                "WHERE (c.utilisateur1_id = ? OR c.utilisateur2_id = ?) " +
                "  AND m.expediteur_id != ? " +
                "  AND m.est_lu = FALSE " +
                "  AND m.est_supprime = FALSE";

        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, utilisateurId);
            pst.setInt(2, utilisateurId);
            pst.setInt(3, utilisateurId);

            rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return 0;
    }

    // ============================================
    // GESTION DES PIÈCES JOINTES
    // ============================================

    /**
     * Ajouter une pièce jointe à un message
     */
    public com.agrifund.entities.PieceJointe ajouterPieceJointe(int messageId, String cheminFichier,
                                                   String nomOriginal) throws SQLException {
        File file = new File(cheminFichier);

        if (!file.exists()) {
            throw new SQLException("Fichier introuvable: " + cheminFichier);
        }

        String extension = getFileExtension(nomOriginal);
        String typeFichier = com.agrifund.util.MessagerieFileManager.determinerTypeFichier(nomOriginal);
        String nomStockage = new File(cheminFichier).getName();
        long taille = file.length();

        String sql = "INSERT INTO PieceJointe (message_id, type_fichier, nom_original, " +
                "nom_stockage, chemin_fichier, taille_octets, extension) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pst.setInt(1, messageId);
            pst.setString(2, typeFichier);
            pst.setString(3, nomOriginal);
            pst.setString(4, nomStockage);
            pst.setString(5, cheminFichier);
            pst.setLong(6, taille);
            pst.setString(7, extension);

            pst.executeUpdate();

            rs = pst.getGeneratedKeys();
            if (rs.next()) {
                com.agrifund.entities.PieceJointe pj = new com.agrifund.entities.PieceJointe();
                pj.setId(rs.getInt(1));
                pj.setMessageId(messageId);
                pj.setTypeFichier(typeFichier);
                pj.setNomOriginal(nomOriginal);
                pj.setNomStockage(nomStockage);
                pj.setCheminFichier(cheminFichier);
                pj.setTailleOctets(taille);
                pj.setExtension(extension);

                return pj;
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return null;
    }

    /**
     * Lister les pièces jointes d'un message
     */
    public List<com.agrifund.entities.PieceJointe> listerPiecesJointes(int messageId) throws SQLException {
        List<com.agrifund.entities.PieceJointe> pieces = new ArrayList<>();

        String sql = "SELECT * FROM PieceJointe WHERE message_id = ? ORDER BY date_upload ASC";

        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, messageId);

            rs = pst.executeQuery();

            while (rs.next()) {
                com.agrifund.entities.PieceJointe pj = new com.agrifund.entities.PieceJointe();
                pj.setId(rs.getInt("id"));
                pj.setMessageId(rs.getInt("message_id"));
                pj.setTypeFichier(rs.getString("type_fichier"));
                pj.setNomOriginal(rs.getString("nom_original"));
                pj.setNomStockage(rs.getString("nom_stockage"));
                pj.setCheminFichier(rs.getString("chemin_fichier"));
                pj.setTailleOctets(rs.getLong("taille_octets"));
                pj.setExtension(rs.getString("extension"));

                Timestamp upload = rs.getTimestamp("date_upload");
                if (upload != null) {
                    pj.setDateUpload(upload.toLocalDateTime());
                }

                pieces.add(pj);
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return pieces;
    }

    /**
     * Supprimer une pièce jointe
     */
    public boolean supprimerPieceJointe(int pieceJointeId) throws SQLException {
        // Récupérer le chemin du fichier
        String sqlSelect = "SELECT chemin_fichier FROM PieceJointe WHERE id = ?";
        PreparedStatement pstSelect = null;
        ResultSet rs = null;
        String cheminFichier = null;

        try {
            pstSelect = connection.prepareStatement(sqlSelect);
            pstSelect.setInt(1, pieceJointeId);
            rs = pstSelect.executeQuery();

            if (rs.next()) {
                cheminFichier = rs.getString("chemin_fichier");
            }
        } finally {
            if (rs != null) rs.close();
            if (pstSelect != null) pstSelect.close();
        }

        // Supprimer de la base
        String sqlDelete = "DELETE FROM PieceJointe WHERE id = ?";
        PreparedStatement pstDelete = null;

        try {
            pstDelete = connection.prepareStatement(sqlDelete);
            pstDelete.setInt(1, pieceJointeId);

            int deleted = pstDelete.executeUpdate();

            // Supprimer le fichier physique
            if (deleted > 0 && cheminFichier != null) {
                com.agrifund.util.MessagerieFileManager.supprimerFichier(cheminFichier);
                System.out.println("✓ Pièce jointe supprimée");
                return true;
            }

        } finally {
            if (pstDelete != null) pstDelete.close();
        }

        return false;
    }

    /**
     * Obtenir l'extension d'un fichier
     */
    private String getFileExtension(String nomFichier) {
        int lastDot = nomFichier.lastIndexOf('.');
        if (lastDot == -1) return "";
        return nomFichier.substring(lastDot + 1).toLowerCase();
    }


}
