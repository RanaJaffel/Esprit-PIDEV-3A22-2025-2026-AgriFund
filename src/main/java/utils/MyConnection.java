package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe singleton pour gérer la connexion à la base de données
 */
public class MyConnection {

    // Attributs de connexion
    private static final String URL = "jdbc:mysql://localhost:3306/gestiondesdecesions";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    // Constructeur privé pour empêcher l'instanciation
    private MyConnection() {
    }

    /**
     * Méthode pour obtenir une instance unique de la connexion
     * @return Connection - l'objet de connexion
     */
    public static Connection getInstance() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion établie avec succès!");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données");
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Méthode pour fermer la connexion
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connexion fermée avec succès!");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion");
            e.printStackTrace();
        }
    }
}