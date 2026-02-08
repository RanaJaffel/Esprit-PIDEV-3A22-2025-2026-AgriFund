package Services;

import entities.Document;
import Utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les Documents
 */
public class DocumentService {

    private Connection connection;

    public DocumentService() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    /**
     * AJOUTER un document
     */
    public int ajouter(Document document) throws SQLException {
        String sql = "INSERT INTO Document (utilisateur_id, nom, type_document, chemin_fichier, " +
                "taille, date_upload, date_expiration, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = null;
        ResultSet rs = null;
        int documentId = -1;

        try {
            pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pst.setInt(1, document.getUtilisateurId());
            pst.setString(2, document.getNom());
            pst.setString(3, document.getTypeDocument());
            pst.setString(4, document.getCheminFichier());
            pst.setInt(5, document.getTaille());
            pst.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            if (document.getDateExpiration() != null) {
                pst.setDate(7, Date.valueOf(document.getDateExpiration()));
            } else {
                pst.setNull(7, Types.DATE);
            }

            pst.setString(8, "en_attente");

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    documentId = rs.getInt(1);
                    document.setId(documentId);
                    System.out.println("✓ Document ajouté avec succès! ID: " + documentId);
                }
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return documentId;
    }

    /**
     * VALIDER ou REJETER un document (Admin)
     */
    public void validerDocument(int documentId, String statut) throws SQLException {
        if (!statut.equals("valide") && !statut.equals("rejete")) {
            throw new IllegalArgumentException("Statut invalide. Utilisez 'valide' ou 'rejete'");
        }

        String sql = "UPDATE Document SET statut=? WHERE id=?";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setString(1, statut);
            pst.setInt(2, documentId);

            pst.executeUpdate();
            System.out.println("✓ Document " + statut + "!");

        } finally {
            if (pst != null) pst.close();
        }
    }

    /**
     * AFFICHER tous les documents d'un utilisateur
     */
    public List<Document> afficherParUtilisateur(int utilisateurId) throws SQLException {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM Document WHERE utilisateur_id=? ORDER BY date_upload DESC";

        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, utilisateurId);
            rs = pst.executeQuery();

            while (rs.next()) {
                Document doc = new Document();
                doc.setId(rs.getInt("id"));
                doc.setUtilisateurId(rs.getInt("utilisateur_id"));
                doc.setNom(rs.getString("nom"));
                doc.setTypeDocument(rs.getString("type_document"));
                doc.setCheminFichier(rs.getString("chemin_fichier"));
                doc.setTaille(rs.getInt("taille"));

                Timestamp timestamp = rs.getTimestamp("date_upload");
                if (timestamp != null) {
                    doc.setDateUpload(timestamp.toLocalDateTime());
                }

                Date dateExp = rs.getDate("date_expiration");
                if (dateExp != null) {
                    doc.setDateExpiration(dateExp.toLocalDate());
                }

                doc.setStatut(rs.getString("statut"));

                documents.add(doc);
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return documents;
    }

    /**
     * AFFICHER tous les documents en attente (Admin)
     */
    public List<Document> afficherEnAttente() throws SQLException {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM Document WHERE statut='en_attente' ORDER BY date_upload DESC";

        Statement st = null;
        ResultSet rs = null;

        try {
            st = connection.createStatement();
            rs = st.executeQuery(sql);

            while (rs.next()) {
                Document doc = new Document();
                doc.setId(rs.getInt("id"));
                doc.setUtilisateurId(rs.getInt("utilisateur_id"));
                doc.setNom(rs.getString("nom"));
                doc.setTypeDocument(rs.getString("type_document"));
                doc.setCheminFichier(rs.getString("chemin_fichier"));
                doc.setTaille(rs.getInt("taille"));

                Timestamp timestamp = rs.getTimestamp("date_upload");
                if (timestamp != null) {
                    doc.setDateUpload(timestamp.toLocalDateTime());
                }

                Date dateExp = rs.getDate("date_expiration");
                if (dateExp != null) {
                    doc.setDateExpiration(dateExp.toLocalDate());
                }

                doc.setStatut(rs.getString("statut"));

                documents.add(doc);
            }

        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
        }

        return documents;
    }

    /**
     * SUPPRIMER un document
     */
    public void supprimer(int documentId) throws SQLException {
        String sql = "DELETE FROM Document WHERE id=?";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, documentId);

            pst.executeUpdate();
            System.out.println("✓ Document supprimé!");

        } finally {
            if (pst != null) pst.close();
        }
    }
}