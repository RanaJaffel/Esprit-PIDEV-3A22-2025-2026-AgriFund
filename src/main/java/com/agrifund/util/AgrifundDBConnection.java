package com.agrifund.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AgrifundDBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/pidev2";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion a la base de donnees reussie!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouve!");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion a la base de donnees!");
            System.err.println("   Verifiez que MySQL est demarre et que la base 'agrifund' existe.");
            e.printStackTrace();
            return null;
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Connexion fermee.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Test if connection is available
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}