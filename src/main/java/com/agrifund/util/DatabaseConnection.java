package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/agrifund?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Mettez votre mot de passe MySQL ici
    private static Connection connection = null;

    private DatabaseConnection() {}

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Charger le driver MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Établir la connexion
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✓ Connexion à la base de données établie avec succès!");

            } catch (ClassNotFoundException e) {
                System.err.println("❌ ERREUR: Driver MySQL non trouvé!");
                System.err.println("Assurez-vous d'avoir ajouté mysql-connector-java au classpath");
                System.err.println("Détails: " + e.getMessage());
                e.printStackTrace();

            } catch (SQLException e) {
                System.err.println("❌ ERREUR: Impossible de se connecter à la base de données!");
                System.err.println("Vérifiez:");
                System.err.println("  1. MySQL est démarré");
                System.err.println("  2. La base de données 'agrifund' existe");
                System.err.println("  3. Les identifiants sont corrects");
                System.err.println("Détails: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("✓ Connexion fermée");
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la fermeture : " + e.getMessage());
            }
        }
    }

    // Méthode pour tester la connexion
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Test de connexion réussi!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Test de connexion échoué: " + e.getMessage());
        }
        return false;
    }
}