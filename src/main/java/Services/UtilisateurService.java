package Services;

import entities.Utilisateur;
import Utils.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les opérations CRUD sur les Utilisateurs
 * Utilise PreparedStatement pour la sécurité (protection contre SQL Injection)
 */
public class UtilisateurService {

    private Connection connection;

    public UtilisateurService() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    /**
     * AJOUTER un utilisateur (INSERT)
     * Utilise PreparedStatement comme dans le cours (slide 24-27)
     */
    public int ajouter(Utilisateur user) throws SQLException {
        String sql = "INSERT INTO Utilisateur (nom, prenom, email, password, tel, date_inscrit, photo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = null;
        ResultSet rs = null;
        int generatedId = -1;

        try {
            // Créer le PreparedStatement avec RETURN_GENERATED_KEYS pour récupérer l'ID
            pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Définir les paramètres (? -> valeurs)
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, hashPassword(user.getPassword())); // Hash du mot de passe
            pst.setString(5, user.getTel());
            pst.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pst.setString(7, user.getPhoto());

            // Exécuter la requête
            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                // Récupérer l'ID auto-généré
                rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                    user.setId(generatedId);
                    System.out.println("✓ Utilisateur ajouté avec succès! ID: " + generatedId);
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de l'ajout: " + e.getMessage());
            throw e;
        } finally {
            // Fermeture des ressources (bonne pratique - slide 22)
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return generatedId;
    }

    /**
     * MODIFIER un utilisateur (UPDATE)
     */
    public void modifier(Utilisateur user) throws SQLException {
        String sql = "UPDATE Utilisateur SET nom=?, prenom=?, email=?, tel=?, photo=? WHERE id=?";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getTel());
            pst.setString(5, user.getPhoto());
            pst.setInt(6, user.getId());

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Utilisateur modifié avec succès!");
            } else {
                System.out.println("⚠ Aucun utilisateur trouvé avec l'ID: " + user.getId());
            }

        } finally {
            if (pst != null) pst.close();
        }
    }

    /**
     * MODIFIER le mot de passe d'un utilisateur
     */
    public void modifierMotDePasse(int userId, String nouveauPassword) throws SQLException {
        String sql = "UPDATE Utilisateur SET password=? WHERE id=?";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setString(1, hashPassword(nouveauPassword)); // Hash du nouveau mot de passe
            pst.setInt(2, userId);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Mot de passe modifié avec succès!");
            } else {
                System.out.println("⚠ Aucun utilisateur trouvé avec l'ID: " + userId);
            }

        } finally {
            if (pst != null) pst.close();
        }
    }

    /**
     * SUPPRIMER un utilisateur (DELETE)
     */
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Utilisateur WHERE id=?";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, id);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Utilisateur supprimé avec succès!");
            } else {
                System.out.println("⚠ Aucun utilisateur trouvé avec l'ID: " + id);
            }

        } finally {
            if (pst != null) pst.close();
        }
    }

    /**
     * AFFICHER tous les utilisateurs (SELECT)
     * Utilise ResultSet pour récupérer les résultats (slide 21)
     */
    public List<Utilisateur> afficherTous() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM Utilisateur";

        Statement st = null;
        ResultSet rs = null;

        try {
            st = connection.createStatement();
            rs = st.executeQuery(sql);

            // Parcourir les résultats
            while (rs.next()) {
                Utilisateur user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setTel(rs.getString("tel"));

                Timestamp timestamp = rs.getTimestamp("date_inscrit");
                if (timestamp != null) {
                    user.setDateInscrit(timestamp.toLocalDateTime());
                }

                user.setPhoto(rs.getString("photo"));
                utilisateurs.add(user);
            }

            System.out.println("✓ " + utilisateurs.size() + " utilisateur(s) récupéré(s)");

        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
        }

        return utilisateurs;
    }

    /**
     * RECHERCHER un utilisateur par ID
     */
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
                user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setTel(rs.getString("tel"));

                Timestamp timestamp = rs.getTimestamp("date_inscrit");
                if (timestamp != null) {
                    user.setDateInscrit(timestamp.toLocalDateTime());
                }

                user.setPhoto(rs.getString("photo"));
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return user;
    }

    /**
     * RECHERCHER un utilisateur par email
     */
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
                user = new Utilisateur();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setTel(rs.getString("tel"));

                Timestamp timestamp = rs.getTimestamp("date_inscrit");
                if (timestamp != null) {
                    user.setDateInscrit(timestamp.toLocalDateTime());
                }

                user.setPhoto(rs.getString("photo"));
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return user;
    }

    /**
     * VÉRIFIER si un email existe déjà
     */
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
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return false;
    }

    /**
     * Hash du mot de passe avec BCrypt
     */
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Vérifier un mot de passe
     */
    public boolean verifierPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}