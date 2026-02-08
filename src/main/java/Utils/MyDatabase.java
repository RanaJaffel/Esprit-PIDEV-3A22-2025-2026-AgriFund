package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    // 🔹 paramètres de connexion
    private static final String URL = "jdbc:mysql://localhost:3306/pidev5ar";
    private static final String USER = "root";
    private static final String PASS = "";

    // 🔹 instance Singleton
    private static MyDatabase instance;
    private Connection con;

    // 🔹 constructeur privé
    private MyDatabase() {
        try {
            con = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // 🔹 méthode Singleton
    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    // 🔹 getter de la connexion
    public Connection getConnection() {
        return con;
    }
}
