package Services;

import entities.Utilisateur;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour UtilisateurService
 */
@DisplayName("Tests UtilisateurService")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UtilisateurServiceTest {

    private static Connection connection;

    @BeforeAll
    static void setup() throws SQLException {
        connection = TestDatabaseConfig.getConnection();
        TestDatabaseConfig.initTables();
    }

    @BeforeEach
    void cleanUp() throws SQLException {
        TestDatabaseConfig.cleanTables();
        TestDatabaseConfig.resetAutoIncrements();
    }

    @AfterAll
    static void tearDown() throws SQLException {
        TestDatabaseConfig.closeConnection();
    }

    // ============================================
    // TESTS AJOUTER UTILISATEUR
    // ============================================

    @Test
    @Order(1)
    @DisplayName("✓ Ajouter un utilisateur avec succès")
    void testAjouterUtilisateur_Success() throws SQLException {
        // Arrange
        Utilisateur user = new Utilisateur("Dupont", "Jean", "jean.dupont@email.com", "password123");
        user.setTel("0612345678");

        // Act
        int userId = ajouterUtilisateur(user);

        // Assert
        assertTrue(userId > 0, "L'ID généré doit être positif");

        // Vérifier en base
        Utilisateur found = rechercherParId(userId);
        assertNotNull(found);
        assertEquals("Dupont", found.getNom());
        assertEquals("Jean", found.getPrenom());
        assertEquals("jean.dupont@email.com", found.getEmail());
        assertEquals("0612345678", found.getTel());
    }

    @Test
    @Order(2)
    @DisplayName("✓ Ajouter utilisateur avec données minimales")
    void testAjouterUtilisateur_DonneesMinimales() throws SQLException {
        // Arrange
        Utilisateur user = new Utilisateur("Martin", "Pierre", "pierre@email.com", "pass");

        // Act
        int userId = ajouterUtilisateur(user);

        // Assert
        assertTrue(userId > 0);

        Utilisateur found = rechercherParId(userId);
        assertNotNull(found);
        assertNull(found.getTel());
        assertNull(found.getPhoto());
    }

    @Test
    @Order(3)
    @DisplayName("✗ Ajouter utilisateur avec email dupliqué")
    void testAjouterUtilisateur_EmailDuplique() throws SQLException {
        // Arrange
        Utilisateur user1 = new Utilisateur("User1", "First", "duplicate@email.com", "pass1");
        Utilisateur user2 = new Utilisateur("User2", "Second", "duplicate@email.com", "pass2");

        // Act
        ajouterUtilisateur(user1);

        // Assert
        assertThrows(SQLException.class, () -> ajouterUtilisateur(user2),
                "Doit lever une exception pour email dupliqué");
    }

    // ============================================
    // TESTS RECHERCHER PAR EMAIL
    // ============================================

    @Test
    @Order(4)
    @DisplayName("✓ Rechercher utilisateur par email - trouvé")
    void testRechercherParEmail_Found() throws SQLException {
        // Arrange
        Utilisateur user = new Utilisateur("Test", "Email", "test.email@gmail.com", "password");
        ajouterUtilisateur(user);

        // Act
        Utilisateur found = rechercherParEmail("test.email@gmail.com");

        // Assert
        assertNotNull(found);
        assertEquals("Test", found.getNom());
        assertEquals("Email", found.getPrenom());
    }

    @Test
    @Order(5)
    @DisplayName("✗ Rechercher utilisateur par email - non trouvé")
    void testRechercherParEmail_NotFound() throws SQLException {
        // Act
        Utilisateur found = rechercherParEmail("inexistant@email.com");

        // Assert
        assertNull(found);
    }

    @Test
    @Order(6)
    @DisplayName("✓ Rechercher avec email en majuscules")
    void testRechercherParEmail_CaseInsensitive() throws SQLException {
        // Arrange
        Utilisateur user = new Utilisateur("Case", "Test", "case.test@email.com", "pass");
        ajouterUtilisateur(user);

        // Act - Recherche avec la même casse
        Utilisateur found = rechercherParEmail("case.test@email.com");

        // Assert
        assertNotNull(found);
    }

    // ============================================
    // TESTS EMAIL EXISTE
    // ============================================

    @Test
    @Order(7)
    @DisplayName("✓ Email existe - retourne true")
    void testEmailExiste_True() throws SQLException {
        // Arrange
        Utilisateur user = new Utilisateur("Exist", "User", "exists@email.com", "pass");
        ajouterUtilisateur(user);

        // Act
        boolean existe = emailExiste("exists@email.com");

        // Assert
        assertTrue(existe);
    }

    @Test
    @Order(8)
    @DisplayName("✓ Email existe - retourne false")
    void testEmailExiste_False() throws SQLException {
        // Act
        boolean existe = emailExiste("nouveau@email.com");

        // Assert
        assertFalse(existe);
    }

    // ============================================
    // TESTS MODIFIER UTILISATEUR
    // ============================================

    @Test
    @Order(9)
    @DisplayName("✓ Modifier utilisateur avec succès")
    void testModifierUtilisateur_Success() throws SQLException {
        // Arrange
        Utilisateur user = new Utilisateur("Ancien", "Nom", "modifier@email.com", "pass");
        int userId = ajouterUtilisateur(user);

        user.setId(userId);
        user.setNom("Nouveau");
        user.setPrenom("Prenom");
        user.setTel("0699999999");

        // Act
        modifierUtilisateur(user);

        // Assert
        Utilisateur found = rechercherParId(userId);
        assertNotNull(found);
        assertEquals("Nouveau", found.getNom());
        assertEquals("Prenom", found.getPrenom());
        assertEquals("0699999999", found.getTel());
    }

    @Test
    @Order(10)
    @DisplayName("✓ Modifier uniquement le téléphone")
    void testModifierUtilisateur_TelephoneSeul() throws SQLException {
        // Arrange
        Utilisateur user = new Utilisateur("Keep", "Name", "keep@email.com", "pass");
        int userId = ajouterUtilisateur(user);

        user.setId(userId);
        user.setTel("0611111111");

        // Act
        modifierUtilisateur(user);

        // Assert
        Utilisateur found = rechercherParId(userId);
        assertEquals("Keep", found.getNom()); // Nom inchangé
        assertEquals("0611111111", found.getTel()); // Tel modifié
    }

    // ============================================
    // TESTS MODIFIER MOT DE PASSE
    // ============================================

    @Test
    @Order(11)
    @DisplayName("✓ Modifier mot de passe avec succès")
    void testModifierMotDePasse_Success() throws SQLException {
        // Arrange
        Utilisateur user = new Utilisateur("Password", "Test", "password@email.com", "oldPassword");
        int userId = ajouterUtilisateur(user);

        // Act
        modifierMotDePasse(userId, "newPassword123");

        // Assert
        Utilisateur found = rechercherParId(userId);
        assertNotNull(found);
        assertTrue(BCrypt.checkpw("newPassword123", found.getPassword()),
                "Le nouveau mot de passe doit être vérifié avec succès");
    }

    @Test
    @Order(12)
    @DisplayName("✓ Ancien mot de passe invalide après modification")
    void testModifierMotDePasse_AncienInvalide() throws SQLException {
        // Arrange
        String ancienPassword = "ancienPass123";
        Utilisateur user = new Utilisateur("Pass", "Change", "passchange@email.com", ancienPassword);
        int userId = ajouterUtilisateur(user);

        // Act
        modifierMotDePasse(userId, "nouveauPass456");

        // Assert
        Utilisateur found = rechercherParId(userId);
        assertFalse(BCrypt.checkpw(ancienPassword, found.getPassword()),
                "L'ancien mot de passe ne doit plus être valide");
    }

    // ============================================
    // TESTS SUPPRIMER UTILISATEUR
    // ============================================

    @Test
    @Order(13)
    @DisplayName("✓ Supprimer utilisateur avec succès")
    void testSupprimerUtilisateur_Success() throws SQLException {
        // Arrange
        Utilisateur user = new Utilisateur("ToDelete", "User", "delete@email.com", "pass");
        int userId = ajouterUtilisateur(user);

        // Act
        supprimerUtilisateur(userId);

        // Assert
        Utilisateur found = rechercherParId(userId);
        assertNull(found, "L'utilisateur supprimé ne doit plus exister");
    }

    @Test
    @Order(14)
    @DisplayName("✓ Supprimer utilisateur inexistant (pas d'erreur)")
    void testSupprimerUtilisateur_Inexistant() throws SQLException {
        // Act & Assert - Ne doit pas lever d'exception
        assertDoesNotThrow(() -> supprimerUtilisateur(9999));
    }

    // ============================================
    // TESTS AFFICHER TOUS
    // ============================================

    @Test
    @Order(15)
    @DisplayName("✓ Afficher tous les utilisateurs")
    void testAfficherTous() throws SQLException {
        // Arrange
        ajouterUtilisateur(new Utilisateur("User1", "Prenom1", "user1@email.com", "pass1"));
        ajouterUtilisateur(new Utilisateur("User2", "Prenom2", "user2@email.com", "pass2"));
        ajouterUtilisateur(new Utilisateur("User3", "Prenom3", "user3@email.com", "pass3"));

        // Act
        List<Utilisateur> users = afficherTous();

        // Assert
        assertEquals(3, users.size());
    }

    @Test
    @Order(16)
    @DisplayName("✓ Afficher tous - liste vide")
    void testAfficherTous_ListeVide() throws SQLException {
        // Act
        List<Utilisateur> users = afficherTous();

        // Assert
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    // ============================================
    // TESTS VÉRIFICATION MOT DE PASSE
    // ============================================

    @Test
    @Order(17)
    @DisplayName("✓ Vérifier mot de passe correct")
    void testVerifierPassword_Correct() {
        // Arrange
        String password = "monMotDePasse123";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Act
        boolean result = BCrypt.checkpw(password, hashedPassword);

        // Assert
        assertTrue(result);
    }

    @Test
    @Order(18)
    @DisplayName("✗ Vérifier mot de passe incorrect")
    void testVerifierPassword_Incorrect() {
        // Arrange
        String hashedPassword = BCrypt.hashpw("bonPassword", BCrypt.gensalt());

        // Act
        boolean result = BCrypt.checkpw("mauvaisPassword", hashedPassword);

        // Assert
        assertFalse(result);
    }

    @Test
    @Order(19)
    @DisplayName("✓ Hash différent pour même mot de passe")
    void testHashPassword_UniqueSalt() {
        // Arrange
        String password = "samePassword";

        // Act
        String hash1 = BCrypt.hashpw(password, BCrypt.gensalt());
        String hash2 = BCrypt.hashpw(password, BCrypt.gensalt());

        // Assert
        assertNotEquals(hash1, hash2, "Les hash doivent être différents (salt unique)");
        assertTrue(BCrypt.checkpw(password, hash1));
        assertTrue(BCrypt.checkpw(password, hash2));
    }

    // ============================================
    // MÉTHODES HELPER (Simulent le Service)
    // ============================================

    private int ajouterUtilisateur(Utilisateur user) throws SQLException {
        String sql = "INSERT INTO Utilisateur (nom, prenom, email, password, tel, date_inscrit, photo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            pst.setString(5, user.getTel());
            pst.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pst.setString(7, user.getPhoto());

            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    private Utilisateur rechercherParId(int id) throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            }
        }
        return null;
    }

    private Utilisateur rechercherParEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Utilisateur WHERE email = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            }
        }
        return null;
    }

    private boolean emailExiste(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Utilisateur WHERE email = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private void modifierUtilisateur(Utilisateur user) throws SQLException {
        String sql = "UPDATE Utilisateur SET nom = ?, prenom = ?, email = ?, tel = ?, photo = ? WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getTel());
            pst.setString(5, user.getPhoto());
            pst.setInt(6, user.getId());

            pst.executeUpdate();
        }
    }

    private void modifierMotDePasse(int userId, String nouveauPassword) throws SQLException {
        String sql = "UPDATE Utilisateur SET password = ? WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, BCrypt.hashpw(nouveauPassword, BCrypt.gensalt()));
            pst.setInt(2, userId);

            pst.executeUpdate();
        }
    }

    private void supprimerUtilisateur(int id) throws SQLException {
        String sql = "DELETE FROM Utilisateur WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    private List<Utilisateur> afficherTous() throws SQLException {
        List<Utilisateur> users = new ArrayList<>();
        String sql = "SELECT * FROM Utilisateur";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUtilisateur(rs));
            }
        }
        return users;
    }

    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
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
        return user;
    }
}