package com.agrifund.services;

import com.agrifund.entities.Utilisateur;
import com.agrifund.util.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {

    private Connection connection;

    public UtilisateurService() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    // ========================================================================
    // CRUD - CREATE
    // ========================================================================

    public int ajouter(Utilisateur user) throws SQLException {
        String sql = "INSERT INTO Utilisateur (nom, prenom, email, password, tel, date_inscrit, photo, roles) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = null;
        ResultSet rs = null;
        int generatedId = -1;

        try {
            pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, hashPassword(user.getPassword()));
            pst.setString(5, user.getTel());
            pst.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pst.setString(7, user.getPhoto() != null ? user.getPhoto() : "");

            // ✅ Gestion du champ 'roles' avec valeur par défaut
            String role = (user.getRoles() != null && !user.getRoles().isEmpty())
                    ? user.getRoles() : "agriculteur";
            pst.setString(8, role);

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                    user.setId(generatedId);
                }
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (pst != null) try { pst.close(); } catch (SQLException e) { /* ignore */ }
        }
        return generatedId;
    }

    // ========================================================================
    // CRUD - UPDATE
    // ========================================================================

    public void modifier(Utilisateur user) throws SQLException {
        String sql = "UPDATE Utilisateur SET nom=?, prenom=?, email=?, tel=?, photo=?, roles=? WHERE id=?";

        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement(sql);
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getTel());
            pst.setString(5, user.getPhoto() != null ? user.getPhoto() : "");
            pst.setString(6, user.getRoles() != null ? user.getRoles() : "agriculteur");
            pst.setInt(7, user.getId());
            pst.executeUpdate();
        } finally {
            if (pst != null) try { pst.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    public void modifierMotDePasse(int userId, String nouveauPassword) throws SQLException {
        String sql = "UPDATE Utilisateur SET password=? WHERE id=?";

        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement(sql);
            pst.setString(1, hashPassword(nouveauPassword));
            pst.setInt(2, userId);
            pst.executeUpdate();
        } finally {
            if (pst != null) try { pst.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    // ========================================================================
    // CRUD - DELETE
    // ========================================================================

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Utilisateur WHERE id=?";

        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, id);
            pst.executeUpdate();
        } finally {
            if (pst != null) try { pst.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    // ========================================================================
    // CRUD - READ
    // ========================================================================

    public List<Utilisateur> afficherTous() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM Utilisateur";

        Statement st = null;
        ResultSet rs = null;
        try {
            st = connection.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (st != null) try { st.close(); } catch (SQLException e) { /* ignore */ }
        }
        return utilisateurs;
    }

    public Utilisateur rechercherParId(int id) throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE id=?";

        Utilisateur user = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                user = mapResultSetToUtilisateur(rs);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (pst != null) try { pst.close(); } catch (SQLException e) { /* ignore */ }
        }
        return user;
    }

    public Utilisateur rechercherParEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE email=?";

        Utilisateur user = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = connection.prepareStatement(sql);
            pst.setString(1, email);
            rs = pst.executeQuery();
            if (rs.next()) {
                user = mapResultSetToUtilisateur(rs);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (pst != null) try { pst.close(); } catch (SQLException e) { /* ignore */ }
        }
        return user;
    }

    public boolean emailExiste(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Utilisateur WHERE email=?";

        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = connection.prepareStatement(sql);
            pst.setString(1, email);
            rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (pst != null) try { pst.close(); } catch (SQLException e) { /* ignore */ }
        }
        return false;
    }

    // ========================================================================
    // GESTION STATUT EN LIGNE
    // ========================================================================

    public void mettreAJourStatutEnLigne(int userId, boolean enLigne) throws SQLException {
        String sql = "UPDATE Utilisateur SET est_en_ligne = ?, derniere_connexion = ? WHERE id = ?";

        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement(sql);
            pst.setBoolean(1, enLigne);
            pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pst.setInt(3, userId);
            pst.executeUpdate();
        } finally {
            if (pst != null) try { pst.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    public void mettreAJourDerniereActivite(int userId) throws SQLException {
        String sql = "UPDATE Utilisateur SET derniere_connexion = ?, est_en_ligne = TRUE WHERE id = ?";

        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement(sql);
            pst.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pst.setInt(2, userId);
            pst.executeUpdate();
        } finally {
            if (pst != null) try { pst.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    public void marquerHorsLigne(int userId) throws SQLException {
        String sql = "UPDATE Utilisateur SET est_en_ligne = FALSE WHERE id = ?";

        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, userId);
            pst.executeUpdate();
        } finally {
            if (pst != null) try { pst.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    public String getStatutEnLigne(int userId) throws SQLException {
        Utilisateur user = rechercherParId(userId);
        return (user != null) ? user.getStatutEnLigne() : "⚪ Hors ligne";
    }

    public boolean estEnLigne(int userId) throws SQLException {
        String sql = "SELECT est_en_ligne, derniere_connexion FROM Utilisateur WHERE id = ?";

        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, userId);
            rs = pst.executeQuery();
            if (rs.next()) {
                boolean enLigne = rs.getBoolean("est_en_ligne");
                Timestamp derniere = rs.getTimestamp("derniere_connexion");

                if (enLigne && derniere != null) {
                    LocalDateTime derniereActivite = derniere.toLocalDateTime();
                    long minutes = java.time.temporal.ChronoUnit.MINUTES.between(
                            derniereActivite, LocalDateTime.now());
                    return minutes < 5;
                }
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (pst != null) try { pst.close(); } catch (SQLException e) { /* ignore */ }
        }
        return false;
    }

    // ========================================================================
    // MÉTHODES UTILITAIRES
    // ========================================================================

    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur user = new Utilisateur();

        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setTel(rs.getString("tel"));

        // ✅ Lecture sécurisée du champ 'roles'
        try {
            String roles = rs.getString("roles");
            user.setRoles(roles != null ? roles : "agriculteur");
        } catch (SQLException e) {
            user.setRoles("agriculteur"); // Valeur par défaut si colonne absente
        }

        Timestamp dateInscrit = rs.getTimestamp("date_inscrit");
        if (dateInscrit != null) {
            user.setDateInscrit(dateInscrit.toLocalDateTime());
        }

        user.setPhoto(rs.getString("photo"));

        Timestamp derniereConnexion = rs.getTimestamp("derniere_connexion");
        if (derniereConnexion != null) {
            user.setDerniereConnexion(derniereConnexion.toLocalDateTime());
        }

        try {
            user.setEstEnLigne(rs.getBoolean("est_en_ligne"));
        } catch (SQLException e) {
            user.setEstEnLigne(false);
        }

        return user;
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Vérifie un mot de passe en clair contre un hash BCrypt
     * Compatible avec les hashes générés par PHP ($2y$) et Java ($2a$/$2b$)
     */
    public boolean verifierPassword(String password, String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            System.out.println("✗ Hash manquant en base de données");
            return false;
        }

        // Normaliser $2y$ (PHP) → $2a$ (Java) pour compatibilité
        String hashNormalise = hashedPassword;
        if (hashedPassword.startsWith("$2y$")) {
            hashNormalise = "$2a$" + hashedPassword.substring(4);
        }

        // Vérifier que c'est bien un hash BCrypt valide
        if (!hashNormalise.startsWith("$2a$") && !hashNormalise.startsWith("$2b$")) {
            System.out.println("✗ Le mot de passe en base n'est pas un hash BCrypt valide");
            return false;
        }

        try {
            return BCrypt.checkpw(password, hashNormalise);
        } catch (IllegalArgumentException e) {
            System.out.println("✗ Hash BCrypt invalide : " + e.getMessage());
            return false;
        }
    }
}
