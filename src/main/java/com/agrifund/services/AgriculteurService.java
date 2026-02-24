package com.agrifund.services;
import com.agrifund.entities.Agriculteur;
import com.agrifund.entities.Utilisateur;
import com.agrifund.util.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les Agriculteurs
 * Gère à la fois la table Utilisateur et Agriculteur (transaction)
 */
public class AgriculteurService {

    private Connection connection;
    private UtilisateurService utilisateurService;

    public AgriculteurService() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
        this.utilisateurService = new UtilisateurService();
    }

    /**
     * INSCRIPTION d'un agriculteur (Transaction sur 2 tables)
     */
    public int inscrire(Agriculteur agriculteur) throws SQLException {
        PreparedStatement pstAgri = null;
        int agriculteurId = -1;

        try {
            // Désactiver l'auto-commit pour gérer la transaction
            connection.setAutoCommit(false);

            // 1. Insérer dans la table Utilisateur
            int utilisateurId = utilisateurService.ajouter(agriculteur);

            if (utilisateurId > 0) {
                // 2. Insérer dans la table Agriculteur
                String sqlAgri = "INSERT INTO Agriculteur (utilisateur_id, adresseferme, " +
                        "superficieferme, typeCulture, statuscompte, compteverifie) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

                pstAgri = connection.prepareStatement(sqlAgri, Statement.RETURN_GENERATED_KEYS);
                pstAgri.setInt(1, utilisateurId);
                pstAgri.setString(2, agriculteur.getAdresseFerme());
                pstAgri.setBigDecimal(3, agriculteur.getSuperficieFerme());
                pstAgri.setString(4, agriculteur.getTypeCulture());
                pstAgri.setString(5, "en_attente");
                pstAgri.setBoolean(6, false);

                int rowsAffected = pstAgri.executeUpdate();

                if (rowsAffected > 0) {
                    ResultSet rs = pstAgri.getGeneratedKeys();
                    if (rs.next()) {
                        agriculteurId = rs.getInt(1);
                        agriculteur.setAgriculteurId(agriculteurId);
                        agriculteur.setUtilisateurId(utilisateurId);
                    }
                    rs.close();
                }

                // Valider la transaction
                connection.commit();
                System.out.println("✓ Agriculteur inscrit avec succès! ID: " + agriculteurId);
            }

        } catch (SQLException e) {
            // Annuler la transaction en cas d'erreur
            connection.rollback();
            System.err.println("✗ Erreur lors de l'inscription: " + e.getMessage());
            throw e;
        } finally {
            // Réactiver l'auto-commit
            connection.setAutoCommit(true);
            if (pstAgri != null) pstAgri.close();
        }

        return agriculteurId;
    }

    /**
     * MODIFIER les informations d'un agriculteur
     */
    public void modifier(Agriculteur agriculteur) throws SQLException {
        String sql = "UPDATE Agriculteur SET adresseferme=?, superficieferme=?, " +
                "typeCulture=? WHERE id=?";

        PreparedStatement pst = null;

        try {
            // Mettre à jour la table Utilisateur
            utilisateurService.modifier(agriculteur);

            // Mettre à jour la table Agriculteur
            pst = connection.prepareStatement(sql);
            pst.setString(1, agriculteur.getAdresseFerme());
            pst.setBigDecimal(2, agriculteur.getSuperficieFerme());
            pst.setString(3, agriculteur.getTypeCulture());
            pst.setInt(4, agriculteur.getAgriculteurId());

            pst.executeUpdate();
            System.out.println("✓ Agriculteur modifié avec succès!");

        } finally {
            if (pst != null) pst.close();
        }
    }

    /**
     * VÉRIFIER le compte d'un agriculteur (Admin seulement)
     */
    public void verifierCompte(int agriculteurId, boolean verifier) throws SQLException {
        String sql = "UPDATE Agriculteur SET compteverifie=?, statuscompte=? WHERE id=?";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setBoolean(1, verifier);
            pst.setString(2, verifier ? "actif" : "refuse");
            pst.setInt(3, agriculteurId);

            pst.executeUpdate();
            System.out.println("✓ Compte " + (verifier ? "vérifié" : "refusé") + "!");

        } finally {
            if (pst != null) pst.close();
        }
    }

    /**
     * AFFICHER tous les agriculteurs
     */
    public List<Agriculteur> afficherTous() throws SQLException {
        List<Agriculteur> agriculteurs = new ArrayList<>();
        String sql = "SELECT a.*, u.nom, u.prenom, u.email, u.tel, u.date_inscrit, u.photo " +
                "FROM Agriculteur a " +
                "INNER JOIN Utilisateur u ON a.utilisateur_id = u.id";

        Statement st = null;
        ResultSet rs = null;

        try {
            st = connection.createStatement();
            rs = st.executeQuery(sql);

            while (rs.next()) {
                Agriculteur agri = new Agriculteur();

                // Données Agriculteur
                agri.setAgriculteurId(rs.getInt("a.id"));
                agri.setUtilisateurId(rs.getInt("utilisateur_id"));
                agri.setAdresseFerme(rs.getString("adresseferme"));
                agri.setSuperficieFerme(rs.getBigDecimal("superficieferme"));
                agri.setTypeCulture(rs.getString("typeCulture"));
                agri.setStatusCompte(rs.getString("statuscompte"));
                agri.setCompteVerifie(rs.getBoolean("compteverifie"));

                // Données Utilisateur
                agri.setId(rs.getInt("utilisateur_id"));
                agri.setNom(rs.getString("nom"));
                agri.setPrenom(rs.getString("prenom"));
                agri.setEmail(rs.getString("email"));
                agri.setTel(rs.getString("tel"));

                Timestamp timestamp = rs.getTimestamp("date_inscrit");
                if (timestamp != null) {
                    agri.setDateInscrit(timestamp.toLocalDateTime());
                }

                agri.setPhoto(rs.getString("photo"));

                agriculteurs.add(agri);
            }

            System.out.println("✓ " + agriculteurs.size() + " agriculteur(s) récupéré(s)");

        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
        }

        return agriculteurs;
    }

    /**
     * RECHERCHER un agriculteur par ID utilisateur
     */
    public Agriculteur rechercherParUtilisateurId(int utilisateurId) throws SQLException {
        String sql = "SELECT a.*, u.nom, u.prenom, u.email, u.tel, u.date_inscrit, u.photo " +
                "FROM Agriculteur a " +
                "INNER JOIN Utilisateur u ON a.utilisateur_id = u.id " +
                "WHERE a.utilisateur_id = ?";

        Agriculteur agri = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, utilisateurId);
            rs = pst.executeQuery();

            if (rs.next()) {
                agri = new Agriculteur();

                agri.setAgriculteurId(rs.getInt("id"));
                agri.setUtilisateurId(rs.getInt("utilisateur_id"));
                agri.setAdresseFerme(rs.getString("adresseferme"));
                agri.setSuperficieFerme(rs.getBigDecimal("superficieferme"));
                agri.setTypeCulture(rs.getString("typeCulture"));
                agri.setStatusCompte(rs.getString("statuscompte"));
                agri.setCompteVerifie(rs.getBoolean("compteverifie"));

                agri.setId(utilisateurId);
                agri.setNom(rs.getString("nom"));
                agri.setPrenom(rs.getString("prenom"));
                agri.setEmail(rs.getString("email"));
                agri.setTel(rs.getString("tel"));

                Timestamp timestamp = rs.getTimestamp("date_inscrit");
                if (timestamp != null) {
                    agri.setDateInscrit(timestamp.toLocalDateTime());
                }

                agri.setPhoto(rs.getString("photo"));
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return agri;
    }

    /**
     * SUPPRIMER un agriculteur
     */
    public void supprimer(int utilisateurId) throws SQLException {
        // Grâce à ON DELETE CASCADE, la suppression de l'utilisateur
        // supprimera automatiquement l'agriculteur
        utilisateurService.supprimer(utilisateurId);
    }
}