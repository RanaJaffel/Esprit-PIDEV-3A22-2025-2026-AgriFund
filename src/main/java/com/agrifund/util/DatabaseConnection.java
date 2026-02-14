package com.agrifund.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/agrifund";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Changez selon votre configuration

    private static Connection connection = null;

    // Obtenir la connexion (Singleton)
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion à la base de données réussie!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL non trouvé!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données!");
            e.printStackTrace();
        }
        return connection;
    }

    // Fermer la connexion
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connexion fermée.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}