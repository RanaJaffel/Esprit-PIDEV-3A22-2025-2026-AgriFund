package Services;

import java.sql.*;

/**
 * Configuration de la base de données H2 pour les tests
 */
public class TestDatabaseConfig {

    private static Connection connection;
    private static final String DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    /**
     * Obtenir la connexion à la base de test
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    }

    /**
     * Initialiser toutes les tables
     */
    public static void initTables() throws SQLException {
        Connection conn = getConnection();

        try (Statement st = conn.createStatement()) {

            // Table Utilisateur
            st.execute("""
                CREATE TABLE IF NOT EXISTS Utilisateur (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    nom VARCHAR(100) NOT NULL,
                    prenom VARCHAR(100) NOT NULL,
                    email VARCHAR(150) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    tel VARCHAR(20),
                    date_inscrit TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    photo VARCHAR(255)
                )
            """);

            // Table Admin
            st.execute("""
                CREATE TABLE IF NOT EXISTS Admin (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    utilisateur_id INT NOT NULL UNIQUE,
                    FOREIGN KEY (utilisateur_id) REFERENCES Utilisateur(id) ON DELETE CASCADE
                )
            """);

            // Table Agriculteur
            st.execute("""
                CREATE TABLE IF NOT EXISTS Agriculteur (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    utilisateur_id INT NOT NULL UNIQUE,
                    adresseferme VARCHAR(255),
                    superficieferme DECIMAL(10,2),
                    typeCulture VARCHAR(100),
                    statuscompte VARCHAR(20) DEFAULT 'en_attente',
                    compteverifie BOOLEAN DEFAULT FALSE,
                    FOREIGN KEY (utilisateur_id) REFERENCES Utilisateur(id) ON DELETE CASCADE
                )
            """);

            // Table Banque
            st.execute("""
                CREATE TABLE IF NOT EXISTS Banque (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    utilisateur_id INT NOT NULL UNIQUE,
                    codebanque VARCHAR(20),
                    addresseSiege VARCHAR(255),
                    representantLegal VARCHAR(100),
                    adresseAgence VARCHAR(255),
                    logo VARCHAR(255),
                    siteweb VARCHAR(255),
                    statusCompte VARCHAR(20) DEFAULT 'en_attente',
                    compteVerfiee BOOLEAN DEFAULT FALSE,
                    FOREIGN KEY (utilisateur_id) REFERENCES Utilisateur(id) ON DELETE CASCADE
                )
            """);

            // Table Document
            st.execute("""
                CREATE TABLE IF NOT EXISTS Document (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    utilisateur_id INT NOT NULL,
                    nom VARCHAR(255) NOT NULL,
                    type_document VARCHAR(50),
                    chemin_fichier VARCHAR(500),
                    taille INT,
                    date_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    date_expiration DATE,
                    statut VARCHAR(20) DEFAULT 'en_attente',
                    FOREIGN KEY (utilisateur_id) REFERENCES Utilisateur(id) ON DELETE CASCADE
                )
            """);

            // Table TokenReinitialisation
            st.execute("""
                CREATE TABLE IF NOT EXISTS TokenReinitialisation (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    utilisateur_id INT NOT NULL,
                    token VARCHAR(255) NOT NULL UNIQUE,
                    date_expiration TIMESTAMP NOT NULL,
                    utilise BOOLEAN DEFAULT FALSE,
                    date_utilisation TIMESTAMP,
                    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (utilisateur_id) REFERENCES Utilisateur(id) ON DELETE CASCADE
                )
            """);
        }
    }

    /**
     * Nettoyer toutes les tables (dans l'ordre des dépendances)
     */
    public static void cleanTables() throws SQLException {
        Connection conn = getConnection();

        try (Statement st = conn.createStatement()) {
            st.execute("DELETE FROM Document");
            st.execute("DELETE FROM TokenReinitialisation");
            st.execute("DELETE FROM Admin");
            st.execute("DELETE FROM Agriculteur");
            st.execute("DELETE FROM Banque");
            st.execute("DELETE FROM Utilisateur");
        }
    }

    /**
     * Réinitialiser les auto-increments
     */
    public static void resetAutoIncrements() throws SQLException {
        Connection conn = getConnection();

        try (Statement st = conn.createStatement()) {
            st.execute("ALTER TABLE Utilisateur ALTER COLUMN id RESTART WITH 1");
            st.execute("ALTER TABLE Admin ALTER COLUMN id RESTART WITH 1");
            st.execute("ALTER TABLE Document ALTER COLUMN id RESTART WITH 1");
        }
    }

    /**
     * Fermer la connexion
     */
    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
    }
}