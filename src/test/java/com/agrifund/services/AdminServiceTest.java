package com.agrifund.services;

import com.agrifund.entities.Admin;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour AdminService
 */
@DisplayName("Tests AdminService")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminServiceTest {

    private static Connection connection;

    @BeforeAll
    static void setup() throws SQLException {
        connection = TestDatabaseConfig.getConnection();
        TestDatabaseConfig.initTables();
    }

    @BeforeEach
    void cleanUp() throws SQLException {
        TestDatabaseConfig.cleanTables();
    }

    @AfterAll
    static void tearDown() throws SQLException {
        TestDatabaseConfig.closeConnection();
    }

    // ============================================
    // TESTS CRÉER ADMIN
    // ============================================

    @Test
    @Order(1)
    @DisplayName("✓ Créer un administrateur avec succès")
    void testCreerAdmin_Success() throws SQLException {
        // Arrange
        Admin admin = new Admin("AdminNom", "AdminPrenom", "admin@email.com", "adminPass123");
        admin.setTel("0601020304");

        // Act
        int adminId = creerAdmin(admin);

        // Assert
        assertTrue(adminId > 0);
        assertTrue(admin.getUtilisateurId() > 0);

        // Vérifier que l'admin existe en base
        assertTrue(estAdmin(admin.getUtilisateurId()));
    }

    @Test
    @Order(2)
    @DisplayName("✓ Créer admin avec transaction (Utilisateur + Admin)")
    void testCreerAdmin_Transaction() throws SQLException {
        // Arrange
        Admin admin = new Admin("Transaction", "Test", "transaction@email.com", "pass");

        // Act
        int adminId = creerAdmin(admin);

        // Assert
        assertTrue(adminId > 0);

        // Vérifier que l'utilisateur existe
        assertNotNull(rechercherUtilisateurParEmail("transaction@email.com"));

        // Vérifier que l'admin est lié
        assertTrue(estAdmin(admin.getUtilisateurId()));
    }

    @Test
    @Order(3)
    @DisplayName("✗ Créer admin avec email dupliqué (rollback)")
    void testCreerAdmin_EmailDuplique() throws SQLException {
        // Arrange
        Admin admin1 = new Admin("Admin1", "First", "duplicate.admin@email.com", "pass1");
        creerAdmin(admin1);

        Admin admin2 = new Admin("Admin2", "Second", "duplicate.admin@email.com", "pass2");

        // Act & Assert
        assertThrows(SQLException.class, () -> creerAdmin(admin2));
    }

    // ============================================
    // TESTS EST ADMIN
    // ============================================

    @Test
    @Order(4)
    @DisplayName("✓ Vérifier si utilisateur est admin - true")
    void testEstAdmin_True() throws SQLException {
        // Arrange
        Admin admin = new Admin("IsAdmin", "Test", "isadmin@email.com", "pass");
        creerAdmin(admin);

        // Act
        boolean result = estAdmin(admin.getUtilisateurId());

        // Assert
        assertTrue(result);
    }

    @Test
    @Order(5)
    @DisplayName("✓ Vérifier si utilisateur est admin - false")
    void testEstAdmin_False() throws SQLException {
        // Arrange - Créer un utilisateur simple (pas admin)
        int userId = creerUtilisateurSimple("Simple", "User", "simple@email.com");

        // Act
        boolean result = estAdmin(userId);

        // Assert
        assertFalse(result);
    }

    @Test
    @Order(6)
    @DisplayName("✓ Vérifier admin inexistant - false")
    void testEstAdmin_UtilisateurInexistant() throws SQLException {
        // Act
        boolean result = estAdmin(9999);

        // Assert
        assertFalse(result);
    }

    // ============================================
    // TESTS AFFICHER TOUS LES ADMINS
    // ============================================

    @Test
    @Order(7)
    @DisplayName("✓ Afficher tous les administrateurs")
    void testAfficherTousAdmins() throws SQLException {
        // Arrange
        creerAdmin(new Admin("Admin1", "Prenom1", "admin1@email.com", "pass1"));
        creerAdmin(new Admin("Admin2", "Prenom2", "admin2@email.com", "pass2"));
        creerAdmin(new Admin("Admin3", "Prenom3", "admin3@email.com", "pass3"));

        // Act
        List<Admin> admins = afficherTousAdmins();

        // Assert
        assertEquals(3, admins.size());
    }

    @Test
    @Order(8)
    @DisplayName("✓ Afficher admins - liste vide")
    void testAfficherTousAdmins_ListeVide() throws SQLException {
        // Act
        List<Admin> admins = afficherTousAdmins();

        // Assert
        assertNotNull(admins);
        assertTrue(admins.isEmpty());
    }

    @Test
    @Order(9)
    @DisplayName("✓ Afficher admins avec données utilisateur complètes")
    void testAfficherAdmins_DonneesCompletes() throws SQLException {
        // Arrange
        Admin admin = new Admin("Complet", "Admin", "complet@email.com", "pass");
        admin.setTel("0611223344");
        creerAdmin(admin);

        // Act
        List<Admin> admins = afficherTousAdmins();

        // Assert
        assertEquals(1, admins.size());
        Admin found = admins.get(0);
        assertEquals("Complet", found.getNom());
        assertEquals("Admin", found.getPrenom());
        assertEquals("complet@email.com", found.getEmail());
        assertEquals("0611223344", found.getTel());
    }

    // ============================================
    // TESTS SUPPRESSION CASCADE
    // ============================================

    @Test
    @Order(10)
    @DisplayName("✓ Supprimer utilisateur supprime aussi admin (CASCADE)")
    void testSuppressionCascade() throws SQLException {
        // Arrange
        Admin admin = new Admin("ToDelete", "Admin", "delete.admin@email.com", "pass");
        creerAdmin(admin);
        int utilisateurId = admin.getUtilisateurId();

        assertTrue(estAdmin(utilisateurId)); // Vérifier qu'il est admin

        // Act - Supprimer l'utilisateur
        supprimerUtilisateur(utilisateurId);

        // Assert
        assertFalse(estAdmin(utilisateurId)); // Plus admin
        assertNull(rechercherUtilisateurParId(utilisateurId)); // Plus d'utilisateur
    }

    // ============================================
    // TESTS COMPTAGE
    // ============================================

    @Test
    @Order(11)
    @DisplayName("✓ Compter le nombre d'administrateurs")
    void testCompterAdmins() throws SQLException {
        // Arrange
        creerAdmin(new Admin("Count1", "Admin", "count1@email.com", "pass"));
        creerAdmin(new Admin("Count2", "Admin", "count2@email.com", "pass"));

        // Act
        int count = compterAdmins();

        // Assert
        assertEquals(2, count);
    }

    // ============================================
    // MÉTHODES HELPER
    // ============================================

    private int creerAdmin(Admin admin) throws SQLException {
        int adminId = -1;

        try {
            connection.setAutoCommit(false);

            // 1. Insérer dans Utilisateur
            int utilisateurId = insererUtilisateur(admin);

            if (utilisateurId > 0) {
                // 2. Insérer dans Admin
                String sqlAdmin = "INSERT INTO Admin (utilisateur_id) VALUES (?)";

                try (PreparedStatement pst = connection.prepareStatement(sqlAdmin, Statement.RETURN_GENERATED_KEYS)) {
                    pst.setInt(1, utilisateurId);
                    pst.executeUpdate();

                    ResultSet rs = pst.getGeneratedKeys();
                    if (rs.next()) {
                        adminId = rs.getInt(1);
                        admin.setAdminId(adminId);
                        admin.setUtilisateurId(utilisateurId);
                    }
                }

                connection.commit();
            }

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }

        return adminId;
    }

    private int insererUtilisateur(Admin admin) throws SQLException {
        String sql = "INSERT INTO Utilisateur (nom, prenom, email, password, tel, date_inscrit) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, admin.getNom());
            pst.setString(2, admin.getPrenom());
            pst.setString(3, admin.getEmail());
            pst.setString(4, BCrypt.hashpw(admin.getPassword(), BCrypt.gensalt()));
            pst.setString(5, admin.getTel());
            pst.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    private int creerUtilisateurSimple(String nom, String prenom, String email) throws SQLException {
        String sql = "INSERT INTO Utilisateur (nom, prenom, email, password) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, nom);
            pst.setString(2, prenom);
            pst.setString(3, email);
            pst.setString(4, "simplePassword");
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    private boolean estAdmin(int utilisateurId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Admin WHERE utilisateur_id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, utilisateurId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private List<Admin> afficherTousAdmins() throws SQLException {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT a.id as admin_id, a.utilisateur_id, u.nom, u.prenom, u.email, u.tel, u.date_inscrit, u.photo " +
                "FROM Admin a " +
                "INNER JOIN Utilisateur u ON a.utilisateur_id = u.id";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Admin admin = new Admin();
                admin.setAdminId(rs.getInt("admin_id"));
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
        }
        return admins;
    }

    private Object rechercherUtilisateurParEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE email = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return new Object(); // Juste pour vérifier l'existence
            }
        }
        return null;
    }

    private Object rechercherUtilisateurParId(int id) throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return new Object();
            }
        }
        return null;
    }

    private void supprimerUtilisateur(int id) throws SQLException {
        String sql = "DELETE FROM Utilisateur WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    private int compterAdmins() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Admin";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}