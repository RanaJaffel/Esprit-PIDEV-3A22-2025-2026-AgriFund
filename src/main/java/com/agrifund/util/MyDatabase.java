package com.agrifund.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    private static final String URL = "jdbc:mysql://localhost:3306/agrifund";
    private static final String USER = "root";
    private static final String PASS = "";

    private static MyDatabase instance;
    private Connection con;

    private MyDatabase() {
        try {
            con = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        return con;
    }
}