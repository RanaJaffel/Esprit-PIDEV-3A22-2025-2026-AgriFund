package services;

import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceProjectAgricole {

    private Connection connection;

    public ServiceProjectAgricole() {
        this.connection = MyConnection.getInstance();
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
