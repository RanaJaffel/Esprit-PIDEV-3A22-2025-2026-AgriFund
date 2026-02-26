package com.agrifund.util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe singleton pour gérer la connexion à la base de données MySQL
 * Suit les bonnes pratiques JDBC présentées dans le cours
 */
public class DatabaseConnection {

    // Attributs de connexion (Etape 5 du cours)
    private static final String URL = "jdbc:mysql://localhost:3306/pidev2";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Instance unique (Singleton)
    private static Connection connection;

    /**
     * Constructeur privé pour empêcher l'instanciation
     */
    private DatabaseConnection() {}

    /**
     * Obtenir la connexion à la base de données (Etape 3 du cours)
     * @return Connection l'objet de connexion
     * @throws SQLException en cas d'erreur de connexion
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Charger le driver MySQL (implicite avec JDBC 4.0+)
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Etablir la connexion
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✓ Connexion à la base de données établie avec succès!");

            } catch (ClassNotFoundException e) {
                System.err.println("✗ Driver MySQL non trouvé!");
                throw new SQLException("Driver MySQL non trouvé", e);
            } catch (SQLException e) {
                System.err.println("✗ Erreur de connexion à la base de données: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    /**
     * Fermer la connexion (bonne pratique - Etape de fermeture)
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✓ Connexion fermée avec succès");
            } catch (SQLException e) {
                System.err.println("✗ Erreur lors de la fermeture: " + e.getMessage());
            }
        }
    }

    /**
     * Tester la connexion
     */
    public static void testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Test de connexion réussi!");
                System.out.println("  Base de données: gestion_utilisateurs");
                System.out.println("  URL: " + URL);
            }
        } catch (SQLException e) {
            System.err.println("✗ Test de connexion échoué: " + e.getMessage());
        }
    }
}