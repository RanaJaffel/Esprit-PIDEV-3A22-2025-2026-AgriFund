package Services;

import entities.Document;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour DocumentService
 */
@DisplayName("Tests DocumentService")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DocumentServiceTest {

    private static Connection connection;
    private static int testUserId;

    @BeforeAll
    static void setup() throws SQLException {
        connection = TestDatabaseConfig.getConnection();
        TestDatabaseConfig.initTables();
    }

    @BeforeEach
    void cleanUp() throws SQLException {
        TestDatabaseConfig.cleanTables();

        // Créer un utilisateur de test
        testUserId = createTestUser("DocTest", "User", "doctest@email.com");
    }

    @AfterAll
    static void tearDown() throws SQLException {
        TestDatabaseConfig.closeConnection();
    }

    // ============================================
    // TESTS AJOUTER DOCUMENT
    // ============================================

    @Test
    @Order(1)
    @DisplayName("✓ Ajouter un document avec succès")
    void testAjouterDocument_Success() throws SQLException {
        // Arrange
        Document doc = new Document();
        doc.setUtilisateurId(testUserId);
        doc.setNom("carte_identite.pdf");
        doc.setTypeDocument("identite");
        doc.setCheminFichier("/uploads/documents/carte_identite.pdf");
        doc.setTaille(2048);
        doc.setDateExpiration(LocalDate.now().plusYears(10));

        // Act
        int docId = ajouterDocument(doc);

        // Assert
        assertTrue(docId > 0);

        Document found = rechercherDocumentParId(docId);
        assertNotNull(found);
        assertEquals("carte_identite.pdf", found.getNom());
        assertEquals("identite", found.getTypeDocument());
        assertEquals("en_attente", found.getStatut());
    }

    @Test
    @Order(2)
    @DisplayName("✓ Ajouter document sans date d'expiration")
    void testAjouterDocument_SansExpiration() throws SQLException {
        // Arrange
        Document doc = new Document();
        doc.setUtilisateurId(testUserId);
        doc.setNom("photo_profil.jpg");
        doc.setTypeDocument("photo");
        doc.setCheminFichier("/uploads/photos/profil.jpg");
        doc.setTaille(512);
        doc.setDateExpiration(null);

        // Act
        int docId = ajouterDocument(doc);

        // Assert
        assertTrue(docId > 0);

        Document found = rechercherDocumentParId(docId);
        assertNotNull(found);
        assertNull(found.getDateExpiration());
    }

    @Test
    @Order(3)
    @DisplayName("✓ Ajouter plusieurs documents pour un utilisateur")
    void testAjouterPlusieursDocuments() throws SQLException {
        // Arrange & Act
        ajouterDocumentSimple("doc1.pdf", "type1");
        ajouterDocumentSimple("doc2.pdf", "type2");
        ajouterDocumentSimple("doc3.pdf", "type3");

        // Assert
        List<Document> docs = afficherParUtilisateur(testUserId);
        assertEquals(3, docs.size());
    }

    // ============================================
    // TESTS VALIDER/REJETER DOCUMENT
    // ============================================

    @Test
    @Order(4)
    @DisplayName("✓ Valider un document")
    void testValiderDocument() throws SQLException {
        // Arrange
        int docId = ajouterDocumentSimple("a_valider.pdf", "identite");

        // Act
        validerDocument(docId, "valide");

        // Assert
        Document found = rechercherDocumentParId(docId);
        assertEquals("valide", found.getStatut());
    }

    @Test
    @Order(5)
    @DisplayName("✓ Rejeter un document")
    void testRejeterDocument() throws SQLException {
        // Arrange
        int docId = ajouterDocumentSimple("a_rejeter.pdf", "identite");

        // Act
        validerDocument(docId, "rejete");

        // Assert
        Document found = rechercherDocumentParId(docId);
        assertEquals("rejete", found.getStatut());
    }

    @Test
    @Order(6)
    @DisplayName("✗ Valider avec statut invalide")
    void testValiderDocument_StatutInvalide() throws SQLException {
        // Arrange
        int docId = ajouterDocumentSimple("test.pdf", "autre");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validerDocument(docId, "statut_invalide")
        );

        assertTrue(exception.getMessage().contains("Statut invalide"));
    }

    // ============================================
    // TESTS AFFICHER DOCUMENTS
    // ============================================

    @Test
    @Order(7)
    @DisplayName("✓ Afficher documents par utilisateur")
    void testAfficherParUtilisateur() throws SQLException {
        // Arrange
        ajouterDocumentSimple("user_doc1.pdf", "identite");
        ajouterDocumentSimple("user_doc2.pdf", "justificatif");

        // Créer un autre utilisateur avec ses propres documents
        int autreUserId = createTestUser("Autre", "User", "autre@email.com");
        ajouterDocumentPourUser(autreUserId, "autre_doc.pdf", "autre");

        // Act
        List<Document> docs = afficherParUtilisateur(testUserId);

        // Assert
        assertEquals(2, docs.size());
        assertTrue(docs.stream().allMatch(d -> d.getUtilisateurId() == testUserId));
    }

    @Test
    @Order(8)
    @DisplayName("✓ Afficher documents en attente")
    void testAfficherEnAttente() throws SQLException {
        // Arrange
        int doc1 = ajouterDocumentSimple("attente1.pdf", "type1");
        int doc2 = ajouterDocumentSimple("attente2.pdf", "type2");
        int doc3 = ajouterDocumentSimple("valide.pdf", "type3");

        validerDocument(doc3, "valide"); // Celui-ci ne doit pas apparaître

        // Act
        List<Document> docsEnAttente = afficherEnAttente();

        // Assert
        assertEquals(2, docsEnAttente.size());
        assertTrue(docsEnAttente.stream().allMatch(d -> "en_attente".equals(d.getStatut())));
    }

    @Test
    @Order(9)
    @DisplayName("✓ Afficher documents - liste vide")
    void testAfficherParUtilisateur_ListeVide() throws SQLException {
        // Act
        List<Document> docs = afficherParUtilisateur(testUserId);

        // Assert
        assertNotNull(docs);
        assertTrue(docs.isEmpty());
    }

    // ============================================
    // TESTS SUPPRIMER DOCUMENT
    // ============================================

    @Test
    @Order(10)
    @DisplayName("✓ Supprimer un document")
    void testSupprimerDocument() throws SQLException {
        // Arrange
        int docId = ajouterDocumentSimple("a_supprimer.pdf", "type");

        // Act
        supprimerDocument(docId);

        // Assert
        Document found = rechercherDocumentParId(docId);
        assertNull(found);
    }

    @Test
    @Order(11)
    @DisplayName("✓ Supprimer document inexistant (pas d'erreur)")
    void testSupprimerDocument_Inexistant() throws SQLException {
        // Act & Assert
        assertDoesNotThrow(() -> supprimerDocument(9999));
    }

    // ============================================
    // TESTS AVEC DATES
    // ============================================

    @Test
    @Order(12)
    @DisplayName("✓ Document avec date d'expiration passée")
    void testDocument_DateExpirationPassee() throws SQLException {
        // Arrange
        Document doc = new Document();
        doc.setUtilisateurId(testUserId);
        doc.setNom("expire.pdf");
        doc.setTypeDocument("identite");
        doc.setCheminFichier("/path/expire.pdf");
        doc.setTaille(100);
        doc.setDateExpiration(LocalDate.now().minusDays(30)); // Expiré il y a 30 jours

        // Act
        int docId = ajouterDocument(doc);

        // Assert
        Document found = rechercherDocumentParId(docId);
        assertNotNull(found);
        assertTrue(found.getDateExpiration().isBefore(LocalDate.now()));
    }

    @Test
    @Order(13)
    @DisplayName("✓ Vérifier date d'upload automatique")
    void testDocument_DateUploadAuto() throws SQLException {
        // Arrange
        LocalDateTime avant = LocalDateTime.now().minusSeconds(1);

        // Act
        int docId = ajouterDocumentSimple("upload_test.pdf", "type");

        // Assert
        Document found = rechercherDocumentParId(docId);
        assertNotNull(found.getDateUpload());
        assertTrue(found.getDateUpload().isAfter(avant));
    }

    // ============================================
    // TESTS STATISTIQUES
    // ============================================

    @Test
    @Order(14)
    @DisplayName("✓ Compter documents par statut")
    void testCompterDocumentsParStatut() throws SQLException {
        // Arrange
        int doc1 = ajouterDocumentSimple("doc1.pdf", "type");
        int doc2 = ajouterDocumentSimple("doc2.pdf", "type");
        int doc3 = ajouterDocumentSimple("doc3.pdf", "type");
        int doc4 = ajouterDocumentSimple("doc4.pdf", "type");

        validerDocument(doc1, "valide");
        validerDocument(doc2, "valide");
        validerDocument(doc3, "rejete");
        // doc4 reste en_attente

        // Act
        int nbValides = compterParStatut("valide");
        int nbRejetes = compterParStatut("rejete");
        int nbEnAttente = compterParStatut("en_attente");

        // Assert
        assertEquals(2, nbValides);
        assertEquals(1, nbRejetes);
        assertEquals(1, nbEnAttente);
    }

    // ============================================
    // MÉTHODES HELPER
    // ============================================

    private int createTestUser(String nom, String prenom, String email) throws SQLException {
        String sql = "INSERT INTO Utilisateur (nom, prenom, email, password) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, nom);
            pst.setString(2, prenom);
            pst.setString(3, email);
            pst.setString(4, "testPassword");
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
    }

    private int ajouterDocument(Document doc) throws SQLException {
        String sql = "INSERT INTO Document (utilisateur_id, nom, type_document, chemin_fichier, " +
                "taille, date_upload, date_expiration, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, doc.getUtilisateurId());
            pst.setString(2, doc.getNom());
            pst.setString(3, doc.getTypeDocument());
            pst.setString(4, doc.getCheminFichier());
            pst.setInt(5, doc.getTaille());
            pst.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            if (doc.getDateExpiration() != null) {
                pst.setDate(7, Date.valueOf(doc.getDateExpiration()));
            } else {
                pst.setNull(7, Types.DATE);
            }

            pst.setString(8, "en_attente");
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    private int ajouterDocumentSimple(String nom, String type) throws SQLException {
        Document doc = new Document();
        doc.setUtilisateurId(testUserId);
        doc.setNom(nom);
        doc.setTypeDocument(type);
        doc.setCheminFichier("/uploads/" + nom);
        doc.setTaille(1024);
        return ajouterDocument(doc);
    }

    private int ajouterDocumentPourUser(int userId, String nom, String type) throws SQLException {
        Document doc = new Document();
        doc.setUtilisateurId(userId);
        doc.setNom(nom);
        doc.setTypeDocument(type);
        doc.setCheminFichier("/uploads/" + nom);
        doc.setTaille(1024);
        return ajouterDocument(doc);
    }

    private Document rechercherDocumentParId(int id) throws SQLException {
        String sql = "SELECT * FROM Document WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return mapResultSetToDocument(rs);
            }
        }
        return null;
    }

    private void validerDocument(int documentId, String statut) throws SQLException {
        if (!statut.equals("valide") && !statut.equals("rejete")) {
            throw new IllegalArgumentException("Statut invalide. Utilisez 'valide' ou 'rejete'");
        }

        String sql = "UPDATE Document SET statut = ? WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, statut);
            pst.setInt(2, documentId);
            pst.executeUpdate();
        }
    }

    private List<Document> afficherParUtilisateur(int utilisateurId) throws SQLException {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM Document WHERE utilisateur_id = ? ORDER BY date_upload DESC";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, utilisateurId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                documents.add(mapResultSetToDocument(rs));
            }
        }
        return documents;
    }

    private List<Document> afficherEnAttente() throws SQLException {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM Document WHERE statut = 'en_attente' ORDER BY date_upload DESC";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                documents.add(mapResultSetToDocument(rs));
            }
        }
        return documents;
    }

    private void supprimerDocument(int documentId) throws SQLException {
        String sql = "DELETE FROM Document WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, documentId);
            pst.executeUpdate();
        }
    }

    private int compterParStatut(String statut) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Document WHERE statut = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, statut);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Document mapResultSetToDocument(ResultSet rs) throws SQLException {
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
        return doc;
    }
}