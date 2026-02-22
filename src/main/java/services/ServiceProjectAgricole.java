package services;

import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceProjectAgricole {

    private Connection connection;

    public ServiceProjectAgricole() {
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

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des IDs projets: " + e.getMessage());
            throw e;
        }

        return projectIds;
    }
}