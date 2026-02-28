package com.agrifund.services;

import com.agrifund.util.MyDabase;
import com.agrifund.util.MyDabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceProjectAgricoleRana {

    private Connection connection;

    public ServiceProjectAgricoleRana(MyDabase MyConnection) {
        this.connection = MyConnection.getInstance().getCon();
    }

    public List<Integer> getAllProjectIds() throws SQLException {
        List<Integer> projectIds = new ArrayList<>();
        String req = "SELECT idproject FROM projectagricole";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                projectIds.add(rs.getInt("idproject"));
            }
        }
        return projectIds;
    }
}