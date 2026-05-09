package com.agrifund.services;

import com.agrifund.entities.Admin;
import com.agrifund.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les Administrateurs
 */
public class AdminService {

    private Connection connection;
    private UtilisateurService utilisateurService;

    public AdminService() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
        this.utilisateurService = new UtilisateurService();
    }

    /**
     * CRÉER un administrateur
     */
    public int creer(Admin admin) throws SQLException {
        PreparedStatement pstAdmin = null;
        int adminId = -1;

        try {
            connection.setAutoCommit(false);

            // 1. Insérer dans Utilisateur
            int utilisateurId = utilisateurService.ajouter(admin);

            if (utilisateurId > 0) {
                // 2. Insérer dans Admin
                String sqlAdmin = "INSERT INTO Admin (utilisateur_id) VALUES (?)";

                pstAdmin = connection.prepareStatement(sqlAdmin, Statement.RETURN_GENERATED_KEYS);
                pstAdmin.setInt(1, utilisateurId);

                int rowsAffected = pstAdmin.executeUpdate();

                if (rowsAffected > 0) {
                    ResultSet rs = pstAdmin.getGeneratedKeys();
                    if (rs.next()) {
                        adminId = rs.getInt(1);
                        admin.setAdminId(adminId);
                        admin.setUtilisateurId(utilisateurId);
                    }
                    rs.close();
                }

                connection.commit();
                System.out.println("✓ Admin créé avec succès! ID: " + adminId);
            }

        } catch (SQLException e) {
            connection.rollback();
            System.err.println("✗ Erreur lors de la création: " + e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
            if (pstAdmin != null) pstAdmin.close();
        }

        return adminId;
    }

    /**
     * AFFICHER tous les administrateurs
     */
    public List<Admin> afficherTous() throws SQLException {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT a.*, u.nom, u.prenom, u.email, u.tel, u.date_inscrit, u.photo " +
                "FROM Admin a " +
                "INNER JOIN Utilisateur u ON a.utilisateur_id = u.id";

        Statement st = null;
        ResultSet rs = null;

        try {
            st = connection.createStatement();
            rs = st.executeQuery(sql);

            while (rs.next()) {
                Admin admin = new Admin();

                admin.setAdminId(rs.getInt("a.id"));
                admin.setUtilisateurId(rs.getInt("utilisateur_id"));

                admin.setId(rs.getInt("utilisateur_id"));
                admin.setNom(rs.getString("nom"));
                admin.setPrenom(rs.getString("prenom"));
                admin.setEmail(rs.getString("email"));
                admin.setTel(rs.getString("tel"));

                Timestamp timestamp = rs.getTimestamp("date_inscrit");
                if (timestamp != null) {
                    admin.setDateInscrit(timestamp.toLocalDateTime());
                }

                admin.setPhoto(rs.getString("photo"));

                admins.add(admin);
            }

        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
        }

        return admins;
    }

    /**
     * VÉRIFIER si un utilisateur est admin
     */
    public boolean estAdmin(int utilisateurId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Admin WHERE utilisateur_id = ?";

        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, utilisateurId);
            rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return false;
    }
}