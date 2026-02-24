package com.agrifund.util;

import java.net.URI;
import java.net.URL;
import java.sql.*;
public class MyDabase {
    final String URL ="jdbc:mysql://localhost:3306/agrifund";
    final String USER ="root";
    final String PASS ="";

    private Connection con;
    private static  MyDabase instance;
    private MyDabase() {
        try {
            con=DriverManager.getConnection(URL,USER,PASS);
            System.out.println("connected");
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }


    }
    public static MyDabase getInstance(){
        if(instance==null){
            instance=new MyDabase();
        }
        return instance;
    }

    public Connection getCon() {
        return con;
    }
}
