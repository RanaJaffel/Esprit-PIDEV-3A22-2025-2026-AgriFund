package com.agrifund.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    final String URL = "jdbc:mysql://localhost:3306/agrifund";
    final String USER = "root";
    final String PASS = "";

    private Connection con;
    private static MyConnection instance;

    private MyConnection() {
        try {
            con = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    public Connection getCon() {
        return con;
    }


    public static void closeConnection() {
        try {
            if (instance != null && instance.con != null && !instance.con.isClosed()) {
                instance.con.close();
                System.out.println("Database connection closed successfully");
            }
        } catch (SQLException e) {
            System.out.println("Error closing database connection: " + e.getMessage());
        }
    }
}
